package com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.ui

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.exifinterface.media.ExifInterface
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.R
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.databinding.ActivityShowOnMapBinding
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.model.getExifInterface
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.ui.theme.GPSMapsCameraTheme
import java.io.File
import java.util.Locale

class ShowOnMapActivity : AppCompatActivity() {

    private val binding: ActivityShowOnMapBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityShowOnMapBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUpCView()

    }

    private fun setUpCView() {
        val uri = Uri.parse(intent.getStringExtra("image_selected"))
        val file = intent.getStringExtra("path")?.let { File(it) }
        val exifInterface = if (file?.exists() == true) {
            Log.d("ShowOnMap", "setUpCView: file exist at ${file.absolutePath} ")
            file.getExifInterface()
        } else {
            Log.d("ShowOnMap", "setUpCView: file not found at ${file?.absolutePath}")
            uri.getExifInterface(this)
        }

        binding.cView.setContent {
            GPSMapsCameraTheme {
                ShowOnMapUI(
                    uri = (uri),
                    exifData = exifInterface,
                    showOnMap = { latLng ->
                        startActivity(Intent(this,ShowLocationActivity::class.java)
                            .putExtra("lat",latLng.latitude)
                            .putExtra("lng",latLng.longitude))
                    }
                )
            }
        }
    }

}

@Composable
fun ShowOnMapUI(
    uri: Uri? = null,
    exifData: ExifInterface? = null,
    showOnMap: ((LatLng) -> Unit)? = null
) {
    val context = LocalContext.current
    var latLng = LatLng(0.0, 0.0)
    val address = remember {
        mutableStateOf("loading Address")
    }
    uri?.let {
        exifData?.latLong?.let { location ->
            latLng = LatLng(location[0], location[1])
        }
        getAddress(latLng, context) { add ->
            address.value = add?.getAddressLine(0) ?: "Address not found"
        }
    }
    ElevatedCard(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .background(color = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {

        AsyncImage(
            model = uri ?: R.drawable.demo_girl_resized,
            contentDescription = "",
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(10.dp)
                .clip(RoundedCornerShape(15.dp)),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Image Details",
                modifier = Modifier
                    .padding(10.dp),
                style = MaterialTheme.typography.titleLarge
            )
            Button(onClick = { showOnMap?.invoke(latLng) }, modifier = Modifier
                .padding(10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.som_map),
                    contentDescription = "",
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .size(20.dp),
                    colorFilter = ColorFilter.tint(color = Color.White)
                )
                Text("Show on Map")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ) {

            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            ) {
                val (icon, text, btn) = createRefs()
                Image(
                    painter = painterResource(id = R.drawable.som_date),
                    contentDescription = "",
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .size(30.dp)
                        .constrainAs(icon) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            height = Dimension.fillToConstraints
                        },
                )
                Text(
                    text = "Date & Time",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .constrainAs(text) {
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            start.linkTo(icon.end)
                            width = Dimension.fillToConstraints
                        }
                        .padding(horizontal = 5.dp)
                        .padding(top = 5.dp),
                    color = Color(android.graphics.Color.parseColor("#A6A6A6")),
                )
                Text(
                    text = exifData?.getAttribute(ExifInterface.TAG_DATETIME)
                        ?: "Jan 02 2023, 07:55 am",
                    modifier = Modifier
                        .constrainAs(btn) {
                            start.linkTo(icon.end)
                            end.linkTo(parent.end)
                            top.linkTo(text.bottom)
                            bottom.linkTo(parent.bottom)
                            width = Dimension.fillToConstraints
                        }
                        .padding(horizontal = 5.dp)
                        .padding(top = 5.dp),
                    maxLines = 1
                )

            }
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            ) {
                val (icon, text, btn) = createRefs()
                Image(
                    painter = painterResource(id = R.drawable.som_location),
                    contentDescription = "",
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .size(30.dp)
                        .constrainAs(icon) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            height = Dimension.fillToConstraints
                        },
                )
                Text(
                    text = "Address",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .constrainAs(text) {
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            start.linkTo(icon.end)
                            width = Dimension.fillToConstraints
                        }
                        .padding(horizontal = 5.dp)
                        .padding(top = 5.dp),
                    color = Color(android.graphics.Color.parseColor("#A6A6A6")),
                )
                Text(
                    text = address.value, modifier = Modifier
                        .constrainAs(btn) {
                            start.linkTo(icon.end)
                            end.linkTo(parent.end)
                            top.linkTo(text.bottom)
                            bottom.linkTo(parent.bottom)
                            width = Dimension.fillToConstraints
                        }
                        .padding(horizontal = 5.dp)
                        .padding(top = 5.dp),
                    maxLines = 1
                )

            }
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            ) {
                val (icon, text, btn) = createRefs()
                Image(
                    painter = painterResource(id = R.drawable.som_cordinates),
                    contentDescription = "",
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .size(30.dp)
                        .constrainAs(icon) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            height = Dimension.fillToConstraints
                        },
                )
                Text(
                    text = "Co-ordinates",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .constrainAs(text) {
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            start.linkTo(icon.end)
                            width = Dimension.fillToConstraints
                        }
                        .padding(horizontal = 5.dp)
                        .padding(top = 5.dp),
                    color = Color(android.graphics.Color.parseColor("#A6A6A6")),
                )
                Text(
                    text = "${latLng.latitude}, ${latLng.longitude}", modifier = Modifier
                        .constrainAs(btn) {
                            start.linkTo(icon.end)
                            end.linkTo(parent.end)
                            top.linkTo(text.bottom)
                            bottom.linkTo(parent.bottom)
                            width = Dimension.fillToConstraints
                        }
                        .padding(horizontal = 5.dp)
                        .padding(top = 5.dp),
                    maxLines = 1
                )

            }
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            ) {
                val (icon, text, btn) = createRefs()
                Image(
                    painter = painterResource(id = R.drawable.som_weather),
                    contentDescription = "",
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .size(30.dp)
                        .constrainAs(icon) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            height = Dimension.fillToConstraints
                        },
                )
                Text(
                    text = "Weather",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .constrainAs(text) {
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            start.linkTo(icon.end)
                            width = Dimension.fillToConstraints
                        }
                        .padding(horizontal = 5.dp)
                        .padding(top = 5.dp),
                    color = Color(android.graphics.Color.parseColor("#A6A6A6")),
                )
                Text(
                    text = "32 c", modifier = Modifier
                        .constrainAs(btn) {
                            start.linkTo(icon.end)
                            end.linkTo(parent.end)
                            top.linkTo(text.bottom)
                            bottom.linkTo(parent.bottom)
                            width = Dimension.fillToConstraints
                        }
                        .padding(horizontal = 5.dp)
                        .padding(top = 5.dp),
                    maxLines = 1
                )

            }

        }
    }
}

@Suppress("DEPRECATION")
fun getAddress(latLng: LatLng, context: Context, onAddressFetched: (Address?) -> Unit) {
    Geocoder(context, Locale.getDefault()).let { coder ->
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                coder.getFromLocation(latLng.latitude, latLng.longitude, 10) {
                    onAddressFetched(it[0])
                }
            else coder.getFromLocation(latLng.latitude, latLng.longitude, 10)?.let {
                onAddressFetched(it[0])
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onAddressFetched(null)
        }
    }
}

@Preview
@Composable
fun SOMPreview() {
    GPSMapsCameraTheme {
        ShowOnMapUI()
    }
}