package se.keyelementab.findwayhome

//import android.R

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity(), GPSManager.GPSListener, CompassManager.CompassListener {
    private val TAG = "MainActivity"
    private val DEBUG = false

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

    /** Used as starting point for the rotation animation. */
    var previousImageDirection : Float = 0f

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
        val arrowImage = findViewById<ImageView>(R.id.arrowImageView)

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

            override fun onFabOpened() {
                val distanceTextView = findViewById<TextView>(R.id.distanceTextView)
                distanceTextView.visibility = View.INVISIBLE
            }
        }

        fabAnimationHandler.enableFab(onItemClickInterface)

        if (!sharedPrefManager.isDisclaimerAccepted(this) || !sharedPrefManager.isDestinationSet(this)) {
            arrowImage.alpha = 0.2f
        }

        if (!sharedPrefManager.isDisclaimerAccepted(this)) {
            presentDisclaimerDialog()
        } else {
            startGetLocation()
        }
    }

    fun presentSetDestinationNowDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.set_location_now_dialog)

        val yesButton = dialog.findViewById(R.id.yesButton) as Button
        yesButton.setOnClickListener {
            firstReceivedPositionIsDestination = true
            startGetLocation()
            dialog.dismiss()
        }
        val laterButton = dialog.findViewById(R.id.laterButton) as Button
        laterButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun presentDisclaimerDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.disclaimer_dialog)

        val disclaimerHeaderTextView = dialog.findViewById(R.id.disclaimerHeaderTextView) as TextView
        disclaimerHeaderTextView.text = String.format(getString(R.string.disclaimer_header_text),
                getString(R.string.app_name))

        val checkbox = dialog.findViewById(R.id.checkBoxAgree) as CheckBox
        val continueButton = dialog.findViewById(R.id.okayButton) as Button
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
        dialog.setContentView(R.layout.about_dialog)

        val generalBodyText = dialog.findViewById(R.id.generalBodyTextView) as TextView
        generalBodyText.text = String.format(getString(R.string.about_general_body_text), getString(R.string.app_name))

        val okayButton = dialog.findViewById(R.id.okayButton) as Button
        okayButton.setOnClickListener {
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
            findViewById<ImageView>(R.id.arrowImageView).alpha = 1.0f
            Toast.makeText(this, R.string.destination_set, Toast.LENGTH_LONG).show()
        }

        if (sharedPrefManager.isDestinationSet(this)) {
            val destination = Location(LocationManager.GPS_PROVIDER)
            // TODO: possibly replace getLatitude/getLongitude with getLocation
            destination.latitude = sharedPrefManager.getLatitude(this)
            destination.longitude = sharedPrefManager.getLongitude(this)
            bearingToDestination = location.bearingTo(destination)
            Log.d(TAG, "bearingToDestination: " + bearingToDestination)

            Log.d(TAG, "distance: " + location.distanceTo(destination))

            val distanceTextView = findViewById<TextView>(R.id.distanceTextView)
            distanceTextView.text = directionUtil.getDistanceString(location.distanceTo(destination), this)
            distanceTextView.visibility = if (fabAnimationHandler.isOpen) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }

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
            val arrowImageView = findViewById<ImageView>(R.id.arrowImageView)
            val turnDegrees = directionUtil.degreesToTurnImage(bearingToDestination!!, heading)
            Log.d(TAG, "onCompassHeading, animation previousImageDirection: " + previousImageDirection)

            val rotate = RotateAnimation(previousImageDirection, turnDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            rotate.duration = (compassManager.COMPASS_UPDATE_DELAY_MS * 0.8).toLong() // duration = 80% of time to next update
            rotate.interpolator = LinearInterpolator()
            rotate.setFillAfter(true)
            arrowImageView.startAnimation(rotate)
            previousImageDirection = turnDegrees
            //arrowImageView.setRotation(turnDegrees)

            Log.d(TAG, "rotate image: " + turnDegrees)
            //Log.d(TAG, "rotate bearingToDestination: " + bearingToDestination)
            //Log.d(TAG, "rotate degree: " + degree)
        }
    }
}