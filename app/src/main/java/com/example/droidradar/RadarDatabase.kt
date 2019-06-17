package com.example.droidradar

import io.realm.Realm
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.util.ArrayList
import android.R
import android.content.Context
import android.util.Log
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.io.InputStreamReader


object RadarDatabase {
    fun getAllRadars(): MutableList<Radar>? {
        val realm = Realm.getDefaultInstance()
        var radares:MutableList<Radar>?=null
//        realm.executeTransactionAsync {
//            realm.beginTransaction()
//            var radars = realm.copyFromRealm(realm.where(Radar::class.java).findAll())
//            realm.commitTransaction()
//            radares = radars
//        }
        println(radares)
        return radares
    }

    fun getRadar(id: Int): Radar {
        val realm = Realm.getDefaultInstance()

        realm.beginTransaction()
        val radar: Radar = realm.copyFromRealm(realm.where(Radar::class.java).equalTo("id", id).findFirst()!!)
        realm.commitTransaction()
        return radar
    }

     fun saveRadar(radar: Radar){

         val realm = Realm.getDefaultInstance()
            realm.executeTransactionAsync {
                realm.beginTransaction()
                realm.insert(radar)
                realm.commitTransaction()
            }
     }


    private val RADAR_LONG_IDX = 0
    private val RADAR_LAT_IDX = 1
    private val RADAR_TYPE = 2

    fun main(context:Context) { //args: Array<String>?
        var fileReader: BufferedReader? = null

        try {
            val radars = ArrayList<Radar>()
            var line: String?
            var count = 1
            val file = context.resources.openRawResource(com.example.droidradar.R.raw.maparadar)
            // fileReader = BufferedReader(FileReader("maparadar.csv"))
            fileReader= BufferedReader(InputStreamReader(file))
            // fileReader = BufferedReader(FileReader(context.resources.getIdentifier("maparadar",
            //                "raw", context.packageName)))  COLOCando conterxto na chamada
            // Read CSV header
            fileReader.readLine()

            // Read the file line by line starting from the second line
            line = fileReader.readLine()
            while (line != null) {
                val tokens = line.split(",")
                if (tokens.size > 0) {
                    val radar = Radar(
                        count,
                        (tokens[RADAR_LONG_IDX]).toDouble(),
                        (tokens[RADAR_LAT_IDX]).toDouble(),
                        tokens[RADAR_TYPE])
                    radars.add(radar)
                }

                line = fileReader.readLine()
                count++
            }

            // Print the new customer list
            for (radar in radars) {
                //saveRadar(radar)

                println(radar.id)
            }
            println("FIM")
        } catch (e: Exception) {
            println("Reading CSV Error!")
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
            } catch (e: IOException) {
                println("Closing fileReader Error!")
                e.printStackTrace()
            }
        }
    }


}