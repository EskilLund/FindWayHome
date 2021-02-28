package se.keyelementab.findwayhome

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.TextView

class MainActivity : AppCompatActivity(), LocationListener {
    private val TAG = "MainActivity"

    private val fabAnimationHandler = FabAnimationHandler(this)



    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val onItemClickInterface : FabAnimationHandler.ClickListener = object : FabAnimationHandler.ClickListener {
            override fun onAboutClicked() {
                Log.d(TAG, "onAboutClicked")
            }
            override fun onSetDestinationClicked() {
                Log.d(TAG, "onSetDestinationClicked")
                getLocation()
            }
        }

        fabAnimationHandler.enableFab(onItemClickInterface)
    }

    override fun onPause() {
        super.onPause()
        fabAnimationHandler.disableFab()
    }
/*
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.action_set_current_location -> {
                Log.d(TAG, "Set current location clicked")
                getLocation()
                true
            }
            R.id.action_about -> {
                Log.d(TAG, "About clicked")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
*/
    private fun getLocation() {
        Log.d(TAG, "getLocation")

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            Log.d(TAG, "getLocation requestPermissions")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        } else {
            Log.d(TAG, "getLocation requestLocationUpdates")
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "Latitude: " + location.latitude + " , Longitude: " + location.longitude)
        val latTextView = findViewById<TextView>(R.id.latTextView)
        val longTextView = findViewById<TextView>(R.id.longTextView)
        latTextView.text = "lat " + location.latitude
        longTextView.text = "long " + location.longitude
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult")
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //    getLocation()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}