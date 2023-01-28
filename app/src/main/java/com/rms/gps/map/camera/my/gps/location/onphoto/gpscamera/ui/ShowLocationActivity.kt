package com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.R
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.databinding.ActivityShowLocationBinding
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.ui.theme.GPSMapsCameraTheme

class ShowLocationActivity : AppCompatActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE){
        ActivityShowLocationBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUpCView()

    }

    private fun setUpCView() {
        binding.cView.setContent {
            val lat =intent.getDoubleExtra("lat",0.0)
            val lng = intent.getDoubleExtra("lng",0.0)
            GPSMapsCameraTheme {
                MapUi(LatLng(lat,lng))
            }
        }
    }
}

@Composable
fun MapUi(
    latLng: LatLng = LatLng(0.0,0.0)
) {
    val cameraPositionState = rememberCameraPositionState{
        position = CameraPosition.fromLatLngZoom(latLng,15f)
    }
    val uiSettings by remember { mutableStateOf(MapUiSettings()) }
    uiSettings.copy(compassEnabled = true)
    ElevatedCard(modifier = Modifier.padding(10.dp).fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        GoogleMap(modifier = Modifier.fillMaxSize(),cameraPositionState = cameraPositionState, uiSettings = uiSettings) {
            Marker(
                state = MarkerState(position = latLng),
                title = "Selected image was snapped here"
            )
        }
    }
}