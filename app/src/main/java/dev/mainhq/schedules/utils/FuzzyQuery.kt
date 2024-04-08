package dev.mainhq.schedules.utils;

//todo this class is intended to do fuzzy searching. for now very simple
class FuzzyQuery(query : String) {
    val query : String
    init{
        //won't work for stuff like "cote des neiges" or "marche central"
        //var str = query.replace(" ", "")
        this.query = query
    }

    override fun toString(): String {
        return this.query
    }
}