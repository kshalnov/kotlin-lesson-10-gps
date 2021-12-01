package ru.gb.course1.kotlin_lesson_10_gps

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import ru.gb.course1.kotlin_lesson_10_gps.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            binding.requestPermissionTextView.text = if (isGranted) "УСПЕХ" else "ПРОВАЛ"
            binding.locationContainer.isVisible = isGranted
        }
    private var location: Location? = null
        set(value) {
            field = value
            binding.addressButton.isVisible = value != null
            binding.addressTextView.isVisible = value != null
        }

    private val locationManager by lazy { getSystemService(LOCATION_SERVICE) as LocationManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.locationContainer.isVisible = false
        binding.addressButton.isVisible = false
        binding.addressTextView.isVisible = false
        binding.progressBar.isVisible = false

        binding.requestPermissionButton.setOnClickListener {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        binding.lastKnownLocationButton.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) return@setOnClickListener

            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val locationString = location?.let {
                "[${it.latitude}, ${it.longitude}]"
            } ?: "NULL"
            binding.lastKnownLocationTextView.text = locationString
        }

        binding.addressButton.setOnClickListener {
            location?.run {
                binding.progressBar.isVisible = true
                Thread {
                    val address = try {
                        Geocoder(this@MainActivity).getFromLocation(latitude, longitude, 1)
                            .firstOrNull()
                    } finally {
                        runOnUiThread {
                            binding.progressBar.isVisible = false
                            binding.addressTextView.text = "ERROR"
                        }
                    }

                    runOnUiThread {
                        binding.addressTextView.text = address?.toString()
                    }
                }.start()
            }
        }

        binding.realtimeLocationButton.setOnClickListener {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1_000L,
                10f,
                locationListener
            )
        }

        binding.stopButton.setOnClickListener {
            locationManager.removeUpdates(locationListener)
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            this@MainActivity.location = location
            binding.realtimeLocationTextView.text = location.let {
                "[${it.latitude}, ${it.longitude}]"
            } ?: "NULL"
        }

        override fun onProviderEnabled(provider: String) {
            Toast.makeText(this@MainActivity, "Enabled $provider", Toast.LENGTH_SHORT).show()
        }

        override fun onProviderDisabled(provider: String) {
            Toast.makeText(this@MainActivity, "Disabled $provider", Toast.LENGTH_SHORT).show()
        }
    }
}