package plugin.grails.floodControl

class FloodFilters {

    def floodService

    def floodControl = { params ->

        if( !params.user ){
            params.ip = request.getRemoteAddr()
        }

        params.controllerName = controllerName
        params.actionName = actionName
        params.returnTimeRemaining = true

        def timeRemaining = floodService.flooded( params )

        return timeRemaining

    }
    
}
