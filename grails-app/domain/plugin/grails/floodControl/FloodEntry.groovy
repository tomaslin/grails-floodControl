package plugin.grails.floodControl

class FloodEntry {

    String key
    Date dateCreated

    static mapWith = "redis"

    static mapping = {
        dateCreated index:true
        key index:true
    }

}
