package dev.mainhq.bus2go.utils;

import android.util.Log

//todo this class is intended to do fuzzy searching. for now very simple
class FuzzyQuery(query : String, exo: Boolean = false) {
    val query : String
    init{
        //won't work for stuff like "cote des neiges" or "marche central"
        //var str = query.replace(" ", "")
        if (exo){
            val list = query.split("-", limit = 2)
            if (list.size > 1) this.query = list[1]
            else this.query = list[0]
        }
        else this.query = query
    }

    override fun toString(): String {
        return this.query
    }
}