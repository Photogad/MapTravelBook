package com.pa.maptravelbook

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*


var namesArray = ArrayList<String>()
var locationArray = ArrayList<LatLng>()

class MainActivity : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



    }

    //// create the menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        //// which menu do we want to inflate?
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_place, menu)


        return super.onCreateOptionsMenu(menu)
    }


    //// what happens with items in the menu?
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item!!.itemId == R.id.add_place) {

            //// we open the maps activity
            val intent = Intent(applicationContext, MapsActivity::class.java)
            intent.putExtra("info", "new")
            startActivity(intent)

        }



        return super.onOptionsItemSelected(item)
    }


    override fun onResume() {
        //// open or create our database
        try {

            val database = openOrCreateDatabase("Places", Context.MODE_PRIVATE, null)

            //// select data from database
            val cursor = database.rawQuery("SELECT * FROM places", null)

            val nameIndex = cursor.getColumnIndex("name")
            val latitudeIndex = cursor.getColumnIndex("latitude")
            val longitudeIndex = cursor.getColumnIndex("longitude")

            //// brings cursor to first cell
            cursor.moveToFirst()

            //// clear to make sure no repetitive data
            namesArray.clear()
            locationArray.clear()

            while (cursor != null) {
                val nameFromDatabase = cursor.getString(nameIndex)
                val latitudeFromDatabase = cursor.getString(latitudeIndex)
                val longitudeFromDatabase = cursor.getString(longitudeIndex)

                namesArray.add(nameFromDatabase)

                val latitudeCoordinate = latitudeFromDatabase.toDouble()
                val longitudeCoordinate = longitudeFromDatabase.toDouble()

                val location = LatLng(latitudeCoordinate, longitudeCoordinate)

                locationArray.add(location)
                cursor.moveToNext()
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

        //// connect the array adapter to the listview
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, namesArray)
        listView1.adapter = arrayAdapter

        listView1.setOnItemClickListener { adapterView, view, i, l ->

            val intent = Intent(applicationContext,MapsActivity::class.java)
            intent.putExtra("info", "old")
            intent.putExtra("name", namesArray[i])
            intent.putExtra("latitude", locationArray[i].latitude)
            intent.putExtra("longitude", locationArray[i].longitude)
            startActivity(intent)
        }

        super.onResume()
    }
}
