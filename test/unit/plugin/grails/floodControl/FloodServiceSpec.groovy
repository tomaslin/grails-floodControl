package plugin.grails.floodControl

import grails.plugin.spock.UnitSpec
import groovy.time.TimeCategory
import spock.lang.Unroll

class FloodServiceSpec extends UnitSpec{

    def service

    def setup(){
        service = new FloodService()
        mockDomain( FloodEntry )
    }

    def "verify that the proper exceptions are raised when the parameter list is invalid"(){

        when:
            service.flooded( params )
        then:
            def e = thrown(IllegalArgumentException)
            e.message == error

        where:
            params                                      | error
            [ ]                                         | "An user or ip address must be specified"
            [ ip: '198.168.2.1' ]                       | "Either specify a controller or a class name"
            [ user: '1' ]                               | "Either specify a controller or a class name"
            [ user: '1', className: 'com.test.Blip' ]   | "No action specified"
            [ ip: '198.168.2.1', controller: 'sales' ]  | "No action specified"
    }

    def "new entries can be added via the add method"(){

        given:
            FloodEntry.count() == 0
        when:
            service.add( params )
        then:
            FloodEntry.count() == 1

        where:
            testNo  | params
            1       | [ ip: '198.168.2.1', controller: 'sales', action:'add' ]
            2       | [ ip: '198.168.2.1', className: 'com.test.Blip', action: 'create' ]
            3       | [ user: '1', controller: 'sales', action:'add' ]
            4       | [ user: '2', className: 'com.test.Blip', action: 'create' ]
    }

    def "flooded returns false when there are no previous entries"(){

        expect:
            service.flooded( [ ip: '198.168.2.1', className: 'Sale', action:'create' ] ) == false

    }

    def "flooded returns true when there is one entry before"(){

        when:
            def params = [ user: '2', className: 'com.test.Blip', action: 'create' ]
            service.add( params )

        then:
            service.flooded( params ) == true

    }

    def "flooded can be altered via limit"(){

        given:
            def params = [ user: '2', className: 'com.test.Blip', action: 'create' ]

        when:
            3.times{ service.add( params ) }

        then:
            service.flooded( params + [limit: limit ] ) == expected

        where:
            limit | expected
            null  | true
            2     | true
            3     | true
            4     | false

    }

    def "flooded can be altered via durations"(){

        given:
            def params = [ user: '2', className: 'com.test.Blip', action: 'create' ]

        when:
            service.add( params )

        then:
            use( TimeCategory ){
                service.flooded( params ) == true
                service.flooded( params + [ from: new Date() - 40.seconds ]) == false
                service.flooded( params + [ duration:20.seconds ] ) == true
                service.flooded( params + [ duration:20.seconds, from: new Date() - 30.seconds ]) == false
                service.flooded( params + [ duration:40.seconds, from: new Date() - 30.seconds ]) == true
            }
    }
}
