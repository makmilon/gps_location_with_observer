package com.example.newgps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.lifecycle.LiveData

class GpsStatusListener(private val context: Context) : LiveData<Boolean>() {

    override fun onActive() {
        registerReceiver()
        checkGpsStatus()
    }

    override fun onInactive() {
        unregisterReceiver()
    }

    private val gpsStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            checkGpsStatus()
        }
    }

    private fun checkGpsStatus() {
        if (isLocationEnabled()) {
            postValue(true)
        } else {
            postValue(false)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun registerReceiver() {
        context.registerReceiver(
            gpsStatusReceiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        )
    }

    private fun unregisterReceiver() {
        context.unregisterReceiver(gpsStatusReceiver)
    }
}
