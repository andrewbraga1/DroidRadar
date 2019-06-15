package com.example.droidradar

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import com.google.android.gms.location.*

import com.google.android.gms.maps.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

import com.google.android.gms.location.LocationRequest
import android.widget.Toast
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.model.*

import com.google.android.gms.maps.model.CameraPosition




class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var markerOptions: MarkerOptions
    private var locationRequest: LocationRequest? = null
    private lateinit var mMap: GoogleMap
    //private lateinit var local: Location
    lateinit var pre : Marker
    var fusedLocationClient: FusedLocationProviderClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        locationRequest = LocationRequest.create();
        locationRequest?.priority=(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest?.interval=(30*1000)
        //locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        fusedLocationClient = getFusedLocationProviderClient(this)

        if (checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            fusedLocationClient?.lastLocation?.
                addOnSuccessListener(this
                ) { location : Location? ->
                    // Got last known location. In some rare
                    // situations this can be null.
                    if(location == null) {
                        val dialog = AlertDialog.Builder(this)
                            .setTitle("Aviso")
                            .setMessage("Algo inesperado ocorreu. Tente novamente mais tarde!")
                            .setPositiveButton("OK", {id, v ->})
                            .create()
                        dialog.show()
                    }
                    if (location != null) {
                        val pos_latitude = location.latitude
                        val pos_longitude = location.longitude
                        pre = mMap.addMarker(markerOptions
                            .position(LatLng(pos_latitude,pos_longitude))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

                    }


                }


            val locationCallback = object : LocationCallback() {

                override fun onLocationResult(lr: LocationResult) {
                    if (lr == null) {
                        return;
                    }

                    for (location:Location in lr.locations) {

                        if (location != null) {

                            val pos_latitude = lr.locations.last().latitude
                            val pos_longitude = lr.locations.last().longitude
                            val stringLocation = location.toString()
                            pre.remove()
                            pre = mMap.addMarker(markerOptions
                                    .position(LatLng(pos_latitude,pos_longitude))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            )

                            val oldPos = mMap.cameraPosition
                            val pos = CameraPosition.builder(oldPos)
                                .target(LatLng(pos_latitude,pos_longitude))
                                .zoom(17f)
                                .build()
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos))

                            Log.e("atualizando", "Newest Location: ")



                            //val a=(LatLng(pos_latitude,pos_longitude))
                            //Toast.makeText(this@MapsActivity, stringLocation, Toast.LENGTH_SHORT).show();
                        }
                    }
//                    Log.e("LOG", lr.toString())
//                    Log.e("AQUI", "Newest Location: " + lr.locations.last())
                    // do something with the new location...
                }
            }
            if (fusedLocationClient != null) {
                fusedLocationClient?.removeLocationUpdates(locationCallback)
            }

            fusedLocationClient?.requestLocationUpdates(locationRequest,locationCallback,null)

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        markerOptions = MarkerOptions()


    }

    val PERMISSION_ID = 1


    private fun checkPermission(vararg perm:String) : Boolean {
        val havePermissions = perm.toList().all {
            ContextCompat.checkSelfPermission(this,it) ==
                    PackageManager.PERMISSION_GRANTED
        }
        if (!havePermissions) {
            if(perm.toList().any {
                    ActivityCompat.
                        shouldShowRequestPermissionRationale(this, it)}
            ) {
                ActivityCompat.requestPermissions(this, perm, PERMISSION_ID)
//
            } else {
                ActivityCompat.requestPermissions(this, perm, PERMISSION_ID)
            }
            return false
        }
        return true
    }


}
