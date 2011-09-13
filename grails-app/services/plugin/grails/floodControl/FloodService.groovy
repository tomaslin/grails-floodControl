package plugin.grails.floodControl

import groovy.time.TimeCategory

class FloodService {

    static transactional = true

    def grailsApplication

    def flooded = { params ->

        use(TimeCategory) {

            def isFlooded = false
            def from = params.from ?: new Date()

            verifyParams(params)

            if (!params.duration) {
                params.duration = grailsApplication?.config?.grails?.plugin?.floodControl?.defaultDuration ?: 30.seconds
            }

            if (!params.limit || 1 > (params.limit as int)) {
                params.limit = 1
            }

            def key = generateToken(params)

            isFlooded = FloodEntry.countByDateCreatedGreaterThanEqualsAndKey( from - params.duration, key ) >= params.limit

            if( params.returnTimeRemaining && isFlooded ){
                isFlooded = from - FloodEntry.getByKey( key, [ sort: 'dateCreated', order: 'desc' ] ).dateCreated
            }

            isFlooded

        }
    }

    def drizzle = { params ->
        new FloodEntry(key: generateToken(params)).save( flush: true )
    }

    private generateToken(params) {
        """${ params.user ?: "ip_" + params.ip }-${ params.controller ?: ( "cn_" + params.className ) }-${params.action}"""
    }

    private verifyParams(params) throws IllegalArgumentException {
        if (!(params.user || params.ip)) {
            throw new IllegalArgumentException('An user or ip address must be specified')
        }

        if (!(params.controller || params.className)) {
            throw new IllegalArgumentException('Either specify a controller or a class name')
        }

        if (!(params.action)) {
            throw new IllegalArgumentException('No action specified')
        }
    }

}
