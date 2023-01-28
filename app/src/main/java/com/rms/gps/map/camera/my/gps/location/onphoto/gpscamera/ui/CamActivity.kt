package com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Flash.*
import com.otaliastudios.cameraview.controls.Hdr
import com.otaliastudios.cameraview.controls.WhiteBalance
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.R
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.databinding.ActivityCamBinding
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.model.bitmapFromVector
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.model.saveToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class CamActivity : AppCompatActivity() {

    private var snapping: Boolean = false
    private var map: GoogleMap? = null
    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityCamBinding.inflate(layoutInflater)
    }
    private val fusedLocationProvider by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    private val geoCoder by lazy { Geocoder(this, Locale.getDefault()) }
    private val locationRequest
        get() = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            10 * 1000
        ).build()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        initMap(mapFragment)

        binding.flipCamera.setOnClickListener {
            binding.camera.toggleFacing()
        }

        binding.camera.setLifecycleOwner(this)

        binding.captureButton.setOnClickListener {
            snapping = true
            binding.captureButton.visibility = View.GONE
            binding.progressCircular.visibility = View.VISIBLE
            map?.snapshot {
                binding.mapSnap.setImageBitmap(it)
                binding.mapSnap.visibility = View.VISIBLE
                binding.camera.takePictureSnapshot()
            }
        }

        binding.flash.setOnClickListener {
            binding.camera.flash = when (binding.camera.flash) {
                OFF -> {
                    binding.flash.setIconResource(R.drawable.icon_auto_flash)
                    binding.flash.text = "AUTO"
                    AUTO
                }

                AUTO -> {
                    binding.flash.setIconResource(R.drawable.icon_flash_on)
                    binding.flash.text = "ON"
                    ON
                }

                ON -> {
                    binding.flash.setIconResource(R.drawable.icon_torch_light)
                    binding.flash.text = "TORCH"
                    TORCH
                }

                else -> {
                    binding.flash.setIconResource(R.drawable.icon_flash_off)
                    binding.flash.text = "OFF"
                    OFF
                }
            }
        }

        binding.white.setOnClickListener {
            binding.camera.apply {
                whiteBalance = when (whiteBalance) {
                    WhiteBalance.AUTO -> {
                        binding.white.apply {
                            text = "Incandescent"
                            setIconResource(R.drawable.icon_auto_wb)
                        }
                        WhiteBalance.INCANDESCENT
                    }

                    WhiteBalance.INCANDESCENT -> {
                        binding.white.apply {
                            text = "Fluorescent"
                            setIconResource(R.drawable.icon_floroscent)
                        }
                        WhiteBalance.FLUORESCENT
                    }

                    WhiteBalance.FLUORESCENT -> {
                        binding.white.apply {
                            text = "Day Light"
                            setIconResource(R.drawable.icon_day_light)
                        }
                        WhiteBalance.DAYLIGHT
                    }

                    WhiteBalance.DAYLIGHT -> {
                        binding.white.apply {
                            text = "Cloudy"
                            setIconResource(R.drawable.icon_cloudy)
                        }
                        WhiteBalance.CLOUDY
                    }

                    WhiteBalance.CLOUDY -> {
                        binding.white.apply {
                            text = "Auto"
                            setIconResource(R.drawable.icon_incandesacent)
                        }
                        WhiteBalance.AUTO
                    }
                }
            }
        }

        binding.hdr.setOnClickListener {
            binding.camera.apply {
                hdr = if (hdr == Hdr.OFF) {
                    binding.hdr.setIconResource(R.drawable.icon_hdr_on)
                    binding.hdr.text = "On"
                    Hdr.ON
                } else {
                    binding.hdr.setIconResource(R.drawable.icon_hdr_off)
                    binding.hdr.text = "Off"
                    Hdr.OFF
                }
            }
        }

        binding.timer.visibility = View.GONE


        binding.camera.apply {
            addCameraListener(object : CameraListener() {
                @Suppress("DEPRECATION")
                override fun onPictureTaken(result: PictureResult) {
                    super.onPictureTaken(result)

                    val folder = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "gpsCamera"
                    )
                    val file = File(folder, "${System.currentTimeMillis()}.webp")
                    if (folder.exists() || folder.mkdirs())
                        if (!file.exists() || file.delete()) {
                            result.toBitmap { bitmap ->
                                if (bitmap != null) {
                                    if (bitmap.saveToFile(
                                            file,
                                            if (Build.VERSION.SDK_INT < 30)
                                                Bitmap.CompressFormat.WEBP
                                            else
                                                Bitmap.CompressFormat.WEBP_LOSSLESS
                                        )
                                    ) {
                                        snapping = false
                                        binding.captureButton.visibility = View.VISIBLE
                                        binding.mapSnap.visibility = View.GONE
                                        binding.mapFragment.visibility = View.VISIBLE
                                        binding.progressCircular.visibility = View.GONE
                                        //TODO: change activity here
                                        /*startActivity(
                                            Intent(
                                                this@CamActivity,
                                                EditorActivity::class.java
                                            ).putExtra("filePath", file.absolutePath)
                                        )*/
                                    }
                                }
                            }
                        }
                }
            })

            when (binding.camera.hdr) {
                Hdr.OFF -> {
                    binding.hdr.apply {
                        text = "OFF"
                        setIconResource(R.drawable.icon_hdr_off)
                    }
                }

                Hdr.ON -> {
                    binding.hdr.apply {
                        text = "ON"
                        setIconResource(R.drawable.icon_hdr_on)
                    }
                }
            }

            when (binding.camera.flash) {
                OFF -> {
                    binding.flash.apply {
                        text = "OFF"
                        setIconResource(R.drawable.icon_flash_off)
                    }
                }

                ON -> {
                    binding.flash.apply {
                        text = "ON"
                        setIconResource(R.drawable.icon_flash_on)
                    }
                }

                AUTO -> {
                    binding.flash.text = "AUTO"
                }

                TORCH -> {
                    binding.flash.text = "TORCH"
                }
            }

            when (binding.camera.whiteBalance) {
                WhiteBalance.AUTO -> {
                    binding.white.apply {
                        text = "Auto"
                        setIconResource(R.drawable.icon_incandesacent)
                    }
                }

                WhiteBalance.INCANDESCENT -> {
                    binding.white.apply {
                        text = "Incandescent"
                        setIconResource(R.drawable.icon_auto_wb)
                    }
                }

                WhiteBalance.FLUORESCENT -> {
                    binding.white.apply {
                        text = "Fluorescent"
                        setIconResource(R.drawable.icon_floroscent)
                    }
                }

                WhiteBalance.DAYLIGHT -> {
                    binding.white.apply {
                        text = "Daylight"
                        setIconResource(R.drawable.icon_day_light)
                    }
                }

                WhiteBalance.CLOUDY -> {
                    binding.white.apply {
                        text = "Cloudy"
                        setIconResource(R.drawable.icon_cloudy)
                    }
                }
            }

        }

    }

    private fun initMap(mapFragment: SupportMapFragment) {
        CoroutineScope(Dispatchers.Main).launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                mapFragment.awaitMap().let { googleMap ->
                    map = googleMap
                    startLocationUpdates()
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
            fusedLocationProvider.requestLocationUpdates(
                locationRequest,
                callBack,
                Looper.getMainLooper()
            )
    }

    private var currentLatLng = LatLng(0.0, 0.0)
    private var currentMarker: Marker? = null

    private val callBack by lazy {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                if (!snapping)
                    result.locations[0]?.let { currentLocation ->
                        currentLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                        map?.apply {
                            if (currentMarker == null)
                                currentMarker = addMarker {
                                    position(currentLatLng)
                                    icon(
                                        bitmapFromVector(
                                            this@CamActivity,
                                            R.drawable.icon_map_pointer
                                        )
                                    )
                                }
                            else currentMarker?.position = currentLatLng
                            moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLatLng,
                                    13f
                                )
                            )
                        }
                        processLocation(currentLocation)
                    }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun processLocation(currentLocation: Location) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geoCoder.getFromLocation(
                currentLocation.latitude,
                currentLocation.longitude,
                1
            ) { addressList ->
                if (addressList.isNotEmpty()) {
                    val address: Address = addressList[0]
                    binding.location.text = address.getAddressLine(0)
                }
            }
        } else {
            geoCoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1)
                ?.let { addressList ->
                    if (addressList.isNotEmpty()) {
                        val address: Address = addressList[0]
                        binding.location.text = address.getAddressLine(0)
                    }
                }
        }

        map?.snapshot {
            binding.mapSnap.setImageBitmap(it)
            binding.mapSnap.visibility = View.VISIBLE
        }

    }

}