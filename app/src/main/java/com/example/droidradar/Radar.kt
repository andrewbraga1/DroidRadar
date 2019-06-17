package com.example.droidradar

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Radar: RealmObject {

    @PrimaryKey
    var id: Int? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var type: String? = null

    constructor()
    constructor(id: Int?, latitude: Double?, longitude: Double?, type: String): super()  {
        this.id = id
        this.latitude = latitude
        this.longitude = longitude
        this.type = type
    }
}