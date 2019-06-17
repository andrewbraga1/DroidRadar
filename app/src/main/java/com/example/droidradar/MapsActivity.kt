package com.example.droidradar

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.media.MediaFormat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
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
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_maps.*



class MapsActivity : AppCompatActivity(), OnMapReadyCallback, SeekBar.OnSeekBarChangeListener {


    private lateinit var markerOptions: MarkerOptions
    private var locationRequest: LocationRequest? = null
    private lateinit var mMap: GoogleMap
    private lateinit var seekBar: SeekBar
    lateinit var pre : Marker
    lateinit var circle: Circle
    lateinit var circleLatLng: LatLng
    var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var distance : String
    var distanceMeasured = FloatArray(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        //Instanciando realm

        Realm.init(this)

        RadarDatabase.main(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        locationRequest = LocationRequest.create();
        locationRequest?.priority=(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest?.interval=(30*1000)
        //locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        fusedLocationClient = getFusedLocationProviderClient(this)
        seekBar = findViewById(R.id.seekBar)
        seekBar.progress = 3
        distance = seekBar.progress.toString()
        supportActionBar?.subtitle = distance + "km"

        seekBar.setOnSeekBarChangeListener(this)

        ///############################################################//





    }
    fun plotPoint(){
        mMap.clear()
        var c = 0
        var points = RadarDatabase.getAllRadars()
        if (points != null) {
            for(point in points){
                Location.distanceBetween(point.latitude!!, point.longitude!!, circle.center.latitude,circle.center.longitude,distanceMeasured)
                if ( distanceMeasured[c] <= circle.radius)
                {
                    mMap.addMarker(markerOptions
                        .position(LatLng(point.latitude!!,point.longitude!!))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .title(point.type)
                    )
                }
                c++
            }
        }
    }
    //#########################################################
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean){
        distance = seekBar.progress.toString()
        circle.remove()
        circle = mMap.addCircle(CircleOptions()
            .center(circleLatLng)
            .radius(seekBar.progress.toDouble()*1000)
            .strokeColor(Color.BLUE)

        )


        //Location.distanceBetween(latLng.latitude, latLng.longitude, circle.center.latitude,circle.center.longitude,distance);
        supportActionBar?.subtitle = distance + "km"
        //Log.e("atualizando", "Newest distance: "+distance)
        //plotPoint()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {

    }




    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        markerOptions = MarkerOptions()
        if (checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            fusedLocationClient?.lastLocation?.
                addOnSuccessListener(this
                ) { location : Location? ->
                    // Got last known location. In some rare
                    // situations this can be null.
                    //println(RadarDatabase.getAllRadars())
                    if(location == null) {
                        val dialog = AlertDialog.Builder(this)
                            .setTitle("Aviso")
                            .setMessage("Algo inesperado ocorreu. Verifique se o seu gps esta ativado e tente novamente mais tarde!")
                            .setPositiveButton("OK", {id, v ->this.finish()})
                            .create()
                        dialog.show()
                    }
                    if (location != null) {
                        val posLatitude = location.latitude
                        val posLongitude = location.longitude
                        pre = mMap.addMarker(markerOptions
                            .position(LatLng(posLatitude,posLongitude))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                        circle = mMap.addCircle(CircleOptions()
                            .center(LatLng(posLatitude,posLongitude))
                            .radius(seekBar.progress.toDouble()*1000)
                            .strokeColor(Color.BLUE)
                        )
                        //plotPoint()
                    }


                }


            val locationCallback = object : LocationCallback() {

                override fun onLocationResult(lr: LocationResult) {
                    if (lr == null) {
                        return
                    }

                    for (location:Location in lr.locations) {

                        if (location != null) {

                            val posLatitude = lr.locations.last().latitude
                            val posLongitude = lr.locations.last().longitude
                            val stringLocation = location.toString()
                            circleLatLng = LatLng(posLatitude,posLongitude)
                            pre.remove()
                            circle.remove()
                            pre = mMap.addMarker(markerOptions
                                .position(LatLng(posLatitude,posLongitude))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            )
                            circle = mMap.addCircle(CircleOptions()
                                .center(LatLng(posLatitude,posLongitude))
                                .radius(seekBar.progress.toDouble()*1000)
                                .strokeColor(Color.BLUE)
                            )
                            //plotPoint()
                            val oldPos = mMap.cameraPosition
                            val pos = CameraPosition.builder(oldPos)
                                .target(LatLng(posLatitude,posLongitude))
                                .zoom(13f)
                                .build()
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos))

                            Log.e("atualizando", "Newest Location: "+String.format(stringLocation))

                            //val a=(LatLng(pos_latitude,pos_longitude))
                            //Toast.makeText(this@MapsActivity, stringLocation, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            if (fusedLocationClient != null) {
                fusedLocationClient?.removeLocationUpdates(locationCallback)
            }
            //println(RadarDatabase.getRadar(22).type.toString())
            fusedLocationClient?.requestLocationUpdates(locationRequest,locationCallback,null)

        }

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
