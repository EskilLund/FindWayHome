package se.keyelementab.findwayhome

import android.animation.Animator
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
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), LocationListener {
    private val TAG = "MainActivity"

    private lateinit var fab: FloatingActionButton
    private lateinit var fabBGLayout: View
    private lateinit var fabAbout: LinearLayout
    private lateinit var fabSetDestination: LinearLayout

    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(findViewById(R.id.toolbar))
//        supportActionBar!!.setDisplayShowTitleEnabled(false)

        fab = findViewById<FloatingActionButton>(R.id.fab)
        fabBGLayout = findViewById<View>(R.id.fabBGLayout)
        fabAbout = findViewById<LinearLayout>(R.id.fabAbout)
        fabSetDestination = findViewById<LinearLayout>(R.id.fabSetDestination)


        fab.setOnClickListener {
            if (View.GONE == fabBGLayout.visibility) {
                showFABMenu()
            } else {
                closeFABMenu()
            }
        }

        fabBGLayout.setOnClickListener { closeFABMenu() }
    }

    private fun showFABMenu() {
        fabAbout.visibility = View.VISIBLE
        fabSetDestination.visibility = View.VISIBLE
        fabBGLayout.visibility = View.VISIBLE
        fab.animate().rotationBy(180F)
        fabAbout.animate().translationY(-resources.getDimension(R.dimen.standard_75))
        fabSetDestination.animate().translationY(-resources.getDimension(R.dimen.standard_120))
    }

    private fun closeFABMenu() {
        fabBGLayout.visibility = View.GONE
        fab.animate().rotation(0F)
        fabAbout.animate().translationY(0f)
        fabSetDestination.animate().translationY(0f)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {}
                    override fun onAnimationEnd(animator: Animator) {
                        if (View.GONE == fabBGLayout.visibility) {
                            fabAbout.visibility = View.GONE
                            fabSetDestination.visibility = View.GONE
                        }
                    }

                    override fun onAnimationCancel(animator: Animator) {}
                    override fun onAnimationRepeat(animator: Animator) {}
                })

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
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, this)
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
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}