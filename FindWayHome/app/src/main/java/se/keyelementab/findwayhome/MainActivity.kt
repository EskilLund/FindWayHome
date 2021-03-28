package se.keyelementab.findwayhome

//import android.R
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity(), GPSManager.GPSListener, CompassManager.CompassListener {
    private val TAG = "MainActivity"
    private val DEBUG = true

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val fabAnimationHandler = FabAnimationHandler(this)

    /**
     * Flag indicating that the first received position from the location manager is to be stored
     * as the destination.
     */
    private var firstReceivedPositionIsDestination = false

    private lateinit var gpsManager: GPSManager
    private lateinit var compassManager: CompassManager

    // TODO: can these be static?
    private val sharedPrefManager = SharedPrefManager()
    private val directionUtil by lazy { DirectionUtil() }

    /**
     * Direction from current position to the destination position.
     * This is in degrees east of true north.
     */
    var bearingToDestination : Float? = null

    private val LOCATION_PERMISSION_CODE = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        compassManager = CompassManager(this, this)
        gpsManager = GPSManager(this, this)
    }

    override fun onResume() {
        super.onResume()
        val context = this

        val onItemClickInterface : FabAnimationHandler.ClickListener = object : FabAnimationHandler.ClickListener {
            override fun onAboutClicked() {
                Log.d(TAG, "onAboutClicked")
                presentAboutDialog()
            }

            override fun onSetDestinationClicked() {
                Log.d(TAG, "onSetDestinationClicked")
                if (!sharedPrefManager.isDisclaimerAccepted(context)) {
                    presentDisclaimerDialog()
                } else {
                    firstReceivedPositionIsDestination = true
                    startGetLocation()
                }
            }
        }

        fabAnimationHandler.enableFab(onItemClickInterface)

        if (!sharedPrefManager.isDisclaimerAccepted(this) || !sharedPrefManager.isDestinationSet(this)) {
            findViewById<ImageView>(R.id.arroyImageView).alpha = 0.4f
        }

        if (!sharedPrefManager.isDisclaimerAccepted(this)) {
            presentDisclaimerDialog()
        } else {
            startGetLocation()
        }
    }

    fun presentSetDestinationNowDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.set_destination_now_question)

        builder.setPositiveButton(R.string.set_destination_yes) { _, _ ->
            firstReceivedPositionIsDestination = true
            startGetLocation()
        }

        builder.setNegativeButton(R.string.set_destination_later) { _, _ ->
        }
        builder.show()
    }

    fun presentDisclaimerDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.disclaimer)
        val checkbox = dialog.findViewById(R.id.checkBoxAgree) as CheckBox
        val continueButton = dialog.findViewById(R.id.continueButton) as Button
        val cancelButton = dialog.findViewById(R.id.cancelButton) as Button

        checkbox.setOnClickListener {
            continueButton.isEnabled = checkbox.isChecked()
        }

        continueButton.setOnClickListener {
            dialog.dismiss()
            sharedPrefManager.setDisclaimerAccepted(this)

            presentSetDestinationNowDialog()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun presentAboutDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.about)
        val continueButton = dialog.findViewById(R.id.continueButton) as Button
        continueButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onPause() {
        super.onPause()
        fabAnimationHandler.disableFab()

        stopGetLocation()
    }

    private fun startGetLocation() {
        Log.d(TAG, "startGetLocation")

        if (!checkPermissions()) {
            Log.d(TAG, "startGetLocation requestPermissions")
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                LOCATION_PERMISSION_CODE
            )
        } else {
            Log.d(TAG, "startGetLocation requestLocationUpdates")

            compassManager.startCompassManager()
            gpsManager.startGPSManager()
        }
    }

    private fun stopGetLocation() {
        compassManager.stopCompassManager()
        gpsManager.stopGPSManager()
    }

    override fun onGPSUpdate(location: Location) {
        Log.d(
            TAG,
            "onLocationChanged, Latitude: " + location.latitude + " , Longitude: " + location.longitude
        )

        if (DEBUG) {
            val latTextView = findViewById<TextView>(R.id.latTextView)
            val longTextView = findViewById<TextView>(R.id.longTextView)
            latTextView.visibility = View.VISIBLE
            longTextView.visibility = View.VISIBLE
            latTextView.text = "lat: " + location.latitude
            longTextView.text = "long: " + location.longitude
        }

        if (firstReceivedPositionIsDestination) {
            if (DEBUG) {
                // for debugging purposes, set the destination to Turning Torso in Malmö, Sweden
                val locationTurningTorso = Location(LocationManager.GPS_PROVIDER)
                locationTurningTorso.latitude = 55.607997568
                locationTurningTorso.longitude = 12.97249611
                sharedPrefManager.setLongLat(this, locationTurningTorso)
            } else {
                sharedPrefManager.setLongLat(this, location)
            }
            firstReceivedPositionIsDestination = false
            findViewById<ImageView>(R.id.arroyImageView).alpha = 1.0f
            Toast.makeText(this, R.string.destination_set, Toast.LENGTH_LONG).show()
        }

        if (sharedPrefManager.isDestinationSet(this)) {
            val destination = Location(LocationManager.GPS_PROVIDER)
            destination.latitude = sharedPrefManager.getLatitude(this)
            destination.longitude = sharedPrefManager.getLongitude(this)
            bearingToDestination = location.bearingTo(destination)
            Log.d(TAG, "bearingToDestination: " + bearingToDestination)

            Log.d(TAG, "distance: " + location.distanceTo(destination))



//            val locationNewYork = Location(LocationManager.GPS_PROVIDER)
//            locationNewYork.latitude = 40.730610
//            locationNewYork.longitude = -73.935242
//            Log.d(TAG, "newyorkbearing: " + location.bearingTo(locationNewYork))


        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult")
        if (requestCode == LOCATION_PERMISSION_CODE) {
            var allGranted = true
            if (grantResults.isEmpty()) {
                allGranted = false
            }

            grantResults.forEach {
                if (it != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false
                }
            }

            if (allGranted) {
                // call getLocation again, as this is where the permission request was instigated
                startGetLocation()
            } else {
                presentPermissionsAreMandatoryDialog()
            }
        }
    }

    fun presentPermissionsAreMandatoryDialog() {
        // TODO: use AlertDialog, and better text
        Toast.makeText(this, R.string.permissions_are_mandatory, Toast.LENGTH_SHORT).show()
    }

    fun checkPermissions() : Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            val granted = ActivityCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                //TODO: simplify, granted ? "" : "not"
                Log.d(TAG, "Permission " + permission + " granted")
            } else {
                Log.d(TAG, "Permission " + permission + " not granted")
            }

            if (!granted) {
                return false
            }
        }
        return true
    }

    override fun onCompassHeading(heading: Float) {
        if (DEBUG) {
            val bearingTextView = findViewById<TextView>(R.id.bearingTextView)
            bearingTextView.visibility = View.VISIBLE
            bearingTextView.text = "Compass bearing: " + heading
        }
        Log.d(TAG, "onCompassHeading heading: " + heading)

        if (bearingToDestination != null) {
            // TODO: degree is to the magnetic north
            val arrowImageView = findViewById<ImageView>(R.id.arroyImageView)
            val turnDegrees = directionUtil.degreesToTurnImage(bearingToDestination!!, heading)
            arrowImageView.setRotation(turnDegrees)
            Log.d(TAG, "rotate image: " + turnDegrees)
            //Log.d(TAG, "rotate bearingToDestination: " + bearingToDestination)
            //Log.d(TAG, "rotate degree: " + degree)
        }
    }
}