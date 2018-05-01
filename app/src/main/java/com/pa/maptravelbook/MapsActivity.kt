package com.pa.maptravelbook

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    //// set initial location variable nulls
    var locationManager : LocationManager? = null
    var locationListener : LocationListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //// assign the long click listener to the map item
        mMap.setOnMapLongClickListener(myListener)

        /*// Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/

        //// get the location service and cast it as a location manager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //// add the location listener
        locationListener = object : LocationListener {
            override fun onLocationChanged(p0: Location?) {

                //// in case user location cannot be found
                if (p0 != null) {
                    var userLocation = LatLng(p0!!.latitude, p0!!.longitude)
                    //// add marker for user location
                    mMap.addMarker(MarkerOptions().position(userLocation).title("Your Location"))
                    //// zoom map to user location with zoom level of 17
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17f))
                }

            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

            }

            override fun onProviderEnabled(provider: String?) {

            }

            override fun onProviderDisabled(provider: String?) {

            }

        }

        //// check if we have location permission, if not request it.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)

        } else {
            //// request location updates every 2 minutes and distance of 2
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2, 2f, locationListener)

            val intent = intent
            var info = intent.getStringExtra("info")

            //// if this is a new place, zoom into user location. else, get it from listview and show it to the user.
            if (info.equals("new")) {

                mMap.clear()
                val lastLocation = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val lastUserLocation = LatLng (lastLocation.latitude, lastLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 17f))
            } else {

                mMap.clear()
                val latitude = intent.getDoubleExtra("latitude", 0.0)
                val longitude = intent.getDoubleExtra("longitude", 0.0)
                val name = intent.getStringExtra("name")
                val location = LatLng(latitude,longitude)
                mMap.addMarker(MarkerOptions().position(location).title(name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,17f))

            }
        }

    }

    //// on map long click listener and getting address from location
    val myListener = object : GoogleMap.OnMapLongClickListener {
        override fun onMapLongClick(p0: LatLng?) {

            val geocoder = Geocoder(applicationContext, Locale.getDefault())

            var address = ""

            try {

                val addressList = geocoder.getFromLocation(p0!!.latitude, p0!!.longitude, 1)

                if (addressList != null && addressList.size > 0) {
                    if (addressList[0].thoroughfare != null) {
                        address += addressList[0].thoroughfare

                        if (addressList[0].subThoroughfare != null) {
                            address += addressList[0].subThoroughfare
                        }
                    }
                } else {
                    address = "New Place"
                }

            } catch (e: Exception){
                e.printStackTrace()
            }


            mMap.addMarker(MarkerOptions().position(p0!!).title(address))

            namesArray.add(address)
            locationArray.add(p0)

            Toast.makeText(applicationContext, "New Place Created!", Toast.LENGTH_LONG).show()


            //// save information into database
            try {

                val latitude = p0.latitude.toString()
                val longitude = p0.longitude.toString()

                //// open or create database
                val database = openOrCreateDatabase("My Places", Context.MODE_PRIVATE, null)
                //// create table in database named places with columns of name, latitude, and longitude. varchar means string basically.
                database.execSQL("CREATE TABLE IF NOT EXISTS places (name VARCHAR, latitude VARCHAR, longitude VARCHAR)")
                //// insert what?
                val toCompile = "INSERT INTO places (name, latitude, longitude) VALUES(?, ?, ?)"

                val sqLiteStatement = database.compileStatement(toCompile)

                ////bind statements to string
                sqLiteStatement.bindString(1,address)
                sqLiteStatement.bindString(2,latitude)
                sqLiteStatement.bindString(3,longitude)

                //// execute
                sqLiteStatement.execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }



        }

    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        //// if we have the permissions granted
        if (grantResults.size > 0) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                //// request location updates every 2 minutes and distance of 2
                locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2, 2f, locationListener)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }










    //// create the menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        //// which menu do we want to inflate?
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.map_type, menu)


        return super.onCreateOptionsMenu(menu)
    }


    //// what happens with items in the menu?
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item!!.itemId == R.id.normal) {

            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        }

        if (item!!.itemId == R.id.satellite) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        }

        if (item!!.itemId == R.id.hybrid) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        }

        if (item!!.itemId == R.id.terrain) {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        }

        return super.onOptionsItemSelected(item)
    }








    }
