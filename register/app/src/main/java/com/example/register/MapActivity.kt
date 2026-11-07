package com.example.register

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager.isLoggedIn()) {
            finish()
            return
        }

        val playServicesError = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(this)
        if (playServicesError != ConnectionResult.SUCCESS) {
            Toast.makeText(
                this,
                getString(R.string.map_play_services_error),
                Toast.LENGTH_LONG,
            ).show()
            finish()
            return
        }

        setContentView(R.layout.activity_map)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        try {
            val fitnessSpot = LatLng(21.9588, 96.0891) // Mandalay, Myanmar
            map.addMarker(
                MarkerOptions()
                    .position(fitnessSpot)
                    .title(getString(R.string.map_marker_title)),
            )
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(fitnessSpot, 13f))
            map.uiSettings.apply {
                isZoomControlsEnabled = true
                isMapToolbarEnabled = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Map failed to load", e)
            Toast.makeText(
                this,
                getString(R.string.map_load_error),
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    companion object {
        private const val TAG = "MapActivity"
    }
}
