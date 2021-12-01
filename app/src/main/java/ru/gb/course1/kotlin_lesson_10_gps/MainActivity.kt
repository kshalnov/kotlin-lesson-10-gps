package ru.gb.course1.kotlin_lesson_10_gps

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import ru.gb.course1.kotlin_lesson_10_gps.databinding.ActivityMainBinding
import ru.gb.course1.kotlin_lesson_10_gps.util.AsyncGeocoder
import ru.gb.course1.kotlin_lesson_10_gps.util.toPrintString

private const val GPS_UPDATE_DURATION_MS = 1_000L
private const val GPS_UPDATE_DISTANCE_M = 10f

private const val GPS_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION

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

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            this@MainActivity.location = location
            binding.realtimeLocationTextView.text = location.toPrintString()
        }

        override fun onProviderEnabled(provider: String) {
            Toast.makeText(this@MainActivity, "Enabled $provider", Toast.LENGTH_SHORT).show()
        }

        override fun onProviderDisabled(provider: String) {
            Toast.makeText(this@MainActivity, "Disabled $provider", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.locationContainer.isVisible = false
        binding.addressButton.isVisible = false
        binding.addressTextView.isVisible = false
        binding.progressBar.isVisible = false

        initClickListeners()
    }

    private fun initClickListeners() {
        binding.requestPermissionButton.setOnClickListener {
            permissionLauncher.launch(GPS_PERMISSION)
        }

        binding.lastKnownLocationButton.setOnClickListener {
            if (checkSelfPermission(GPS_PERMISSION) != PERMISSION_GRANTED) return@setOnClickListener

            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            binding.lastKnownLocationTextView.text = location?.toPrintString()
        }

        binding.addressButton.setOnClickListener {
            location?.run {
                binding.progressBar.isVisible = true
                AsyncGeocoder(this@MainActivity).getFromLocation(
                    latitude,
                    longitude,
                    1
                ) { address: String? ->
                    binding.progressBar.isVisible = false
                    binding.addressTextView.text = address
                }
            }
        }

        binding.realtimeLocationButton.setOnClickListener {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                GPS_UPDATE_DURATION_MS,
                GPS_UPDATE_DISTANCE_M,
                locationListener
            )
        }

        binding.stopButton.setOnClickListener {
            locationManager.removeUpdates(locationListener)
        }
    }

}
