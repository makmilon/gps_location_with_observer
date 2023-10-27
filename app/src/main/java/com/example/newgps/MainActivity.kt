package com.example.newgps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {



    //start auto capture location
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var gpsStatusListener: GpsStatusListener

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
        private const val REQUEST_LOCATION_SETTINGS = 123 // Add this constant for onActivityResult
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView=findViewById<TextView>(R.id.textView)


        // Initialize the GPS status listener
        gpsStatusListener = GpsStatusListener(this)
        gpsStatusListener.observe(this) { isGpsOn ->
            if (!isGpsOn) {
                showEnableGPSDialog()
            }
        }

        //start auto capture location

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {

            override fun onLocationChanged(location: Location) {
                // Handle the received location here
                val latitude = location.latitude
                val longitude = location.longitude

                val address = getAddress(latitude, longitude)
                //  Toast.makeText(applicationContext, "Get Success", Toast.LENGTH_SHORT).show()

                textView.text=address

                Toast.makeText(applicationContext, "$address", Toast.LENGTH_SHORT).show()


            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                // Handle status changes here
            }

            override fun onProviderEnabled(provider: String) {
                // Handle provider enabled here
            }

            override fun onProviderDisabled(provider: String) {
                // Handle provider disabled here
            }
        }

        getCurrentPosition()
    }


    private fun showEnableGPSDialog() {
        val dialog1 = Dialog(this@MainActivity)
        dialog1.setContentView(R.layout.gps_is_not_active_layout)
        dialog1.window!!.setBackgroundDrawable(getDrawable(R.drawable.dialogue_background1))
        dialog1.setCancelable(false)
        dialog1.show()
        val yesText = dialog1.findViewById<TextView>(R.id.textYes)

        yesText.setOnClickListener {
            dialog1.dismiss()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(intent, REQUEST_LOCATION_SETTINGS)
        }
    }

    //start get current location
    @SuppressLint("SetTextI18n")
    private fun getCurrentPosition() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                // Request location updates
                requestLocationUpdates()
            } else {
                // Prompt user to enable location services
                val dialog1 = Dialog(this@MainActivity)
                dialog1.setContentView(R.layout.gps_is_not_active_layout)
                dialog1.window!!.setBackgroundDrawable(getDrawable(R.drawable.dialogue_background1))
                dialog1.setCancelable(false)
                dialog1.show()
                val yesText = dialog1.findViewById<TextView>(R.id.textYes)

                yesText.setOnClickListener {
                    dialog1.dismiss()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(intent, REQUEST_LOCATION_SETTINGS)
                }
            }
        } else {
            // Request location permission
            requestPermission()
        }
    }


    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    private fun checkPermission(): Boolean {
        val permission1 = Manifest.permission.ACCESS_FINE_LOCATION
        val permission2 = Manifest.permission.ACCESS_COARSE_LOCATION
        return (ContextCompat.checkSelfPermission(this, permission1) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, permission2) == PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()
                getCurrentPosition()
            } else {
                //   Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun getAddress(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses?.get(0)
                    val addressStringBuilder = StringBuilder()

                    if (address != null) {
                        for (i in 0..address.maxAddressLineIndex) {
                            addressStringBuilder.append(address.getAddressLine(i)).append("\n")
                        }
                    }

                    return addressStringBuilder.toString()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "Address not found"
    }

    // Override onActivityResult to handle the result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_LOCATION_SETTINGS) {
            if (isLocationEnabled()) {
                // Location services are enabled, recreate the current activity
                recreate()

            } else {
                val dialog1 = Dialog(this@MainActivity)
                dialog1.setContentView(R.layout.gps_is_not_active_layout)
                dialog1.window!!.setBackgroundDrawable(getDrawable(R.drawable.dialogue_background1))
                dialog1.setCancelable(false)
                dialog1.show()
                val yesText = dialog1.findViewById<TextView>(R.id.textYes)

                yesText.setOnClickListener {
                    dialog1.dismiss()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(intent, REQUEST_LOCATION_SETTINGS)
                }
            }
        }
    }

    //end get current location
}