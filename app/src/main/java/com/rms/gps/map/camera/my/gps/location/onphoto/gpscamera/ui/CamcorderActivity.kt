package com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.transition.TransitionManager
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
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Audio
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Hdr
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.controls.WhiteBalance
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.R
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.databinding.ActivityCamBinding
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.model.bitmapFromVector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class CamcorderActivity : AppCompatActivity() {

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
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10 * 1000
        ).build()

    private var time = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        initMap(mapFragment)

        binding.flipCamera.setOnClickListener {
            binding.camera.toggleFacing()
        }

        binding.camera.apply {
            setLifecycleOwner(this@CamcorderActivity)
            mode = Mode.VIDEO
            audio = Audio.ON
            addCameraListener(object : CameraListener() {
                override fun onVideoTaken(result: VideoResult) {
                    super.onVideoTaken(result)
                    binding.captureButton.setImageResource(R.drawable.ic_photo_camera)
                    if (result.file.exists()) {
                        startActivity(
                            Intent(
                                this@CamcorderActivity,
                                PlayerActivity::class.java
                            ).putExtra("video_path", result.file.absolutePath)
                        )
                        finish()
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
                Flash.OFF -> {
                    binding.flash.apply {
                        text = "OFF"
                        setIconResource(R.drawable.icon_flash_off)
                    }
                }

                Flash.ON -> {
                    binding.flash.apply {
                        text = "ON"
                        setIconResource(R.drawable.icon_flash_on)
                    }
                }

                Flash.AUTO -> {
                    binding.flash.text = "AUTO"
                }

                Flash.TORCH -> {
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

            when(audio){
                Audio.OFF -> {
                    binding.sounds.text = "Off"
                    binding.sounds.setIconResource(R.drawable.icon_audio_off)
                }
                Audio.ON -> {
                    binding.sounds.text = "On"
                    binding.sounds.setIconResource(R.drawable.icon_audio_on)
                }
                Audio.MONO -> {
                    binding.sounds.text = "Mono"
                    binding.sounds.setIconResource(R.drawable.icon_mono)
                }
                Audio.STEREO -> {
                    binding.sounds.text = "Sterio"
                    binding.sounds.setIconResource(R.drawable.icon_stero)
                }
            }
        }

        binding.timer.setOnClickListener {
            if (!snapping) {
                TransitionManager.beginDelayedTransition(binding.root)
                if (binding.timerLayout.isVisible) binding.timerLayout.visibility = View.GONE
                else binding.timerLayout.visibility = View.VISIBLE
            }
        }

        binding.sounds.visibility = View.VISIBLE
        binding.sounds.setOnClickListener {
            binding.camera.apply {
                when (audio) {
                    Audio.OFF -> {
                        audio = Audio.ON
                        binding.sounds.text = "On"
                        binding.sounds.setIconResource(R.drawable.icon_audio_on)
                    }

                    Audio.ON -> {
                        audio = Audio.MONO
                        binding.sounds.text = "Mono"
                        binding.sounds.setIconResource(R.drawable.icon_mono)
                    }

                    Audio.MONO -> {
                        audio = Audio.STEREO
                        binding.sounds.text = "Stereo"
                        binding.sounds.setIconResource(R.drawable.icon_stero)
                    }

                    Audio.STEREO -> {
                        audio = Audio.OFF
                        binding.sounds.text = "Off"
                        binding.sounds.setIconResource(R.drawable.icon_audio_off)
                    }
                }
            }
        }

        binding.sec10.setOnClickListener {
            time = 10
            Toast.makeText(this, "10 Seconds video selected", Toast.LENGTH_SHORT).show()
            TransitionManager.beginDelayedTransition(binding.timerLayout)
            binding.timer.text = "10 Sec"

            binding.noTime.setBackgroundColor(Color.TRANSPARENT)
            binding.sec30.setBackgroundColor(Color.TRANSPARENT)
            binding.sec60.setBackgroundColor(Color.TRANSPARENT)
            binding.sec10.setBackgroundResource(R.drawable.semi_grey_curved)
        }

        binding.sec30.setOnClickListener {
            time = 30
            Toast.makeText(this, "30 Seconds video selected", Toast.LENGTH_SHORT).show()
            TransitionManager.beginDelayedTransition(binding.timerLayout)
            binding.timer.text = "30 Sec"

            binding.sec10.setBackgroundColor(Color.TRANSPARENT)
            binding.noTime.setBackgroundColor(Color.TRANSPARENT)
            binding.sec60.setBackgroundColor(Color.TRANSPARENT)
            binding.sec30.setBackgroundResource(R.drawable.semi_grey_curved)
        }

        binding.sec60.setOnClickListener {
            time = 60
            Toast.makeText(this, "60 Seconds video selected", Toast.LENGTH_SHORT).show()
            TransitionManager.beginDelayedTransition(binding.timerLayout)
            binding.timer.text = "60 Sec"

            binding.sec10.setBackgroundColor(Color.TRANSPARENT)
            binding.sec30.setBackgroundColor(Color.TRANSPARENT)
            binding.noTime.setBackgroundColor(Color.TRANSPARENT)
            binding.sec60.setBackgroundResource(R.drawable.semi_grey_curved)
        }

        binding.noTime.setOnClickListener {
            time = -1
            Toast.makeText(this, "Time limit removed", Toast.LENGTH_SHORT).show()
            TransitionManager.beginDelayedTransition(binding.timerLayout)
            binding.timer.text = "Timer"

            binding.sec10.setBackgroundColor(Color.TRANSPARENT)
            binding.sec30.setBackgroundColor(Color.TRANSPARENT)
            binding.sec60.setBackgroundColor(Color.TRANSPARENT)
            binding.noTime.setBackgroundResource(R.drawable.semi_grey_curved)
        }

        binding.flash.setOnClickListener {
            binding.camera.flash = when (binding.camera.flash) {
                Flash.OFF -> {
                    binding.flash.setIconResource(R.drawable.icon_auto_flash)
                    binding.flash.text = "AUTO"
                    Flash.AUTO
                }

                Flash.AUTO -> {
                    binding.flash.setIconResource(R.drawable.icon_flash_on)
                    binding.flash.text = "ON"
                    Flash.ON
                }

                Flash.ON -> {
                    binding.flash.setIconResource(R.drawable.icon_torch_light)
                    binding.flash.text = "TORCH"
                    Flash.TORCH
                }

                else -> {
                    binding.flash.setIconResource(R.drawable.icon_flash_off)
                    binding.flash.text = "OFF"
                    Flash.OFF
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

        binding.captureButton.setOnClickListener {
            if (!snapping) {
                snapping = true
                binding.captureButton.setImageResource(R.drawable.ic_stop)
                map?.snapshot {
                    binding.mapSnap.setImageBitmap(it)
                    binding.mapSnap.visibility = View.VISIBLE
                    val folder = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                        "gpsCamera"
                    )
                    val file = File(folder, "${System.currentTimeMillis()}.mp4")
                    if (folder.exists() || folder.mkdirs()) if (!file.exists() || file.delete()) {
                        if (time == -1) binding.camera.takeVideoSnapshot(file)
                        else binding.camera.takeVideoSnapshot(file, time * 1000)
                    }
                }
            } else {
                snapping = false
                binding.camera.stopVideo()
                binding.captureButton.setImageResource(R.drawable.ic_photo_camera)
            }
        }

        binding.progressCircular.visibility = View.GONE

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
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) fusedLocationProvider.requestLocationUpdates(
            locationRequest, callBack, Looper.getMainLooper()
        )
    }

    private var currentLatLng = LatLng(0.0, 0.0)
    private var currentMarker: Marker? = null

    private val callBack by lazy {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                if (!snapping) result.locations[0]?.let { currentLocation ->
                    currentLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                    map?.apply {
                        if (currentMarker == null) currentMarker = addMarker {
                            position(currentLatLng)
                            icon(
                                bitmapFromVector(
                                    this@CamcorderActivity, R.drawable.icon_map_pointer
                                )
                            )
                        }
                        else currentMarker?.position = currentLatLng
                        moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                currentLatLng, 13f
                            )
                        )
                    }
                    processLocation(currentLocation)
                }
            }
        }
    }

    private fun processLocation(currentLocation: Location) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geoCoder.getFromLocation(
                currentLocation.latitude, currentLocation.longitude, 1
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