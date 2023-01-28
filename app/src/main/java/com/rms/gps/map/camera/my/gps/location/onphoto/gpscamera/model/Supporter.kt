@file:Suppress("unused")

package com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.model

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Rect
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.view.drawToBitmap
import androidx.exifinterface.media.ExifInterface
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.R
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBoxBlurFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageEmbossFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageExposureFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGammaFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHazeFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageKuwaharaFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageRGBFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSharpenFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageVibranceFilter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.math.abs

val supporter = Supporter.getInstance()

class Supporter {

    companion object {

        @Volatile
        private var INSTANCE: Supporter? = null

        fun getInstance(): Supporter {
            return INSTANCE ?: synchronized(this) {
                val instance = Supporter()
                INSTANCE = instance
                instance
            }
        }

    }

    fun editGeoTags(file: File, location: LatLng, listener: (Boolean) -> Boolean) {
        try {
            val exif = ExifInterface(file.canonicalPath)
            val lat = location.latitude
            val aLat = abs(lat)
            var dms = Location.convert(aLat, Location.FORMAT_SECONDS)
            var splits = dms.split(":".toRegex()).toTypedArray()
            var seconds: Array<String?> = splits[2].split("\\.".toRegex()).toTypedArray()
            var second = if (seconds.isEmpty()) splits[2] else seconds[0]!!
            val latitudeStr = splits[0] + "/1," + splits[1] + "/1," + second + "/1"
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latitudeStr)
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, if (lat > 0) "N" else "S")
            val lon = location.longitude
            val aLon = abs(lon)
            dms = Location.convert(aLon, Location.FORMAT_SECONDS)
            splits = dms.split(":".toRegex()).toTypedArray()
            seconds = splits[2].split("\\.".toRegex()).toTypedArray()
            second = if (seconds.isEmpty()) splits[2] else seconds[0]!!
            val longitudeStr = splits[0] + "/1," + splits[1] + "/1," + second + "/1"
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, longitudeStr)
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, if (lon > 0) "E" else "W")
            exif.saveAttributes()
            listener.invoke(true)
        } catch (e: Exception) {
            e.printStackTrace()
            listener.invoke(false)
        }
    }

    private val stickersDays = listOf(
        R.drawable.sticker_days_monday,
        R.drawable.sticker_days_tuesday,
        R.drawable.sticker_days_wednesday,
        R.drawable.sticker_days_thursday,
        R.drawable.sticker_days_friday,
        R.drawable.sticker_days_saturday,
        R.drawable.sticker_days_sunday,
    )

    private val stickersDrinks = listOf(
        R.drawable.sticker_drinks_1,
        R.drawable.sticker_drinks_2,
        R.drawable.sticker_drinks_3,
        R.drawable.sticker_drinks_4,
        R.drawable.sticker_drinks_5,
        R.drawable.sticker_drinks_6,
        R.drawable.sticker_drinks_7,
        R.drawable.sticker_drinks_8,
        R.drawable.sticker_drinks_9,
        R.drawable.sticker_drinks_10,
        R.drawable.sticker_drinks_11,
        R.drawable.sticker_drinks_12,
        R.drawable.sticker_drinks_13,
        R.drawable.sticker_drinks_14,
        R.drawable.sticker_drinks_15,
        R.drawable.sticker_drinks_16,
        R.drawable.sticker_drinks_17,
        R.drawable.sticker_drinks_18,
        R.drawable.sticker_drinks_19,
        R.drawable.sticker_drinks_20,
        R.drawable.sticker_drinks_21,
        R.drawable.sticker_drinks_22,
        R.drawable.sticker_drinks_23,
        R.drawable.sticker_drinks_24,
    )

    private val stickersFitness = listOf(
        R.drawable.sticker_fitness_1,
        R.drawable.sticker_fitness_2,
        R.drawable.sticker_fitness_3,
        R.drawable.sticker_fitness_4,
        R.drawable.sticker_fitness_5,
        R.drawable.sticker_fitness_6,
        R.drawable.sticker_fitness_7,
        R.drawable.sticker_fitness_8,
        R.drawable.sticker_fitness_9,
        R.drawable.sticker_fitness_10,
        R.drawable.sticker_fitness_11,
        R.drawable.sticker_fitness_12,
        R.drawable.sticker_fitness_13,
    )

    private val stickersFood = listOf(
        R.drawable.sticker_food_1,
        R.drawable.sticker_food_2,
        R.drawable.sticker_food_3,
        R.drawable.sticker_food_4,
        R.drawable.sticker_food_5,
        R.drawable.sticker_food_6,
        R.drawable.sticker_food_7,
        R.drawable.sticker_food_8,
        R.drawable.sticker_food_9,
        R.drawable.sticker_food_10,
        R.drawable.sticker_food_11,
        R.drawable.sticker_food_12,
        R.drawable.sticker_food_13,
        R.drawable.sticker_food_14,
        R.drawable.sticker_food_15,
        R.drawable.sticker_food_16,
        R.drawable.sticker_food_17,
        R.drawable.sticker_food_18,
        R.drawable.sticker_food_19,
        R.drawable.sticker_food_20,
        R.drawable.sticker_food_21,
        R.drawable.sticker_food_22,
        R.drawable.sticker_food_23,
        R.drawable.sticker_food_24,
        R.drawable.sticker_food_25,
        R.drawable.sticker_food_26,
        R.drawable.sticker_food_27,
        R.drawable.sticker_food_28,
    )

    private val stickersPlaces = listOf(
        R.drawable.sticker_places_1,
        R.drawable.sticker_places_2,
        R.drawable.sticker_places_3,
        R.drawable.sticker_places_4,
        R.drawable.sticker_places_5,
        R.drawable.sticker_places_6,
        R.drawable.sticker_places_7,
        R.drawable.sticker_places_8,
        R.drawable.sticker_places_9,
        R.drawable.sticker_places_10,
        R.drawable.sticker_places_11,
        R.drawable.sticker_places_12,
        R.drawable.sticker_places_13,
        R.drawable.sticker_places_14,
        R.drawable.sticker_places_15,
    )

    private val stickersQuotes = listOf(
        R.drawable.sticker_quotes_1,
        R.drawable.sticker_quotes_2,
        R.drawable.sticker_quotes_3,
        R.drawable.sticker_quotes_17,
        R.drawable.sticker_quotes_18,
        R.drawable.sticker_quotes_19,
        R.drawable.sticker_quotes_20,
        R.drawable.sticker_quotes_21,
        R.drawable.sticker_quotes_22,
        R.drawable.sticker_quotes_23,
        R.drawable.sticker_quotes_24,
        R.drawable.sticker_quotes_25,
        R.drawable.sticker_quotes_26,
        R.drawable.sticker_quotes_27,
        R.drawable.sticker_quotes_28,
        R.drawable.sticker_quotes_29,
        R.drawable.sticker_quotes_30,
        R.drawable.sticker_quotes_31,
        R.drawable.sticker_quotes_32,
        R.drawable.sticker_quotes_33,
        R.drawable.sticker_quotes_34,
        R.drawable.sticker_quotes_35,
        R.drawable.sticker_quotes_36,
        R.drawable.sticker_quotes_37,
        R.drawable.sticker_quotes_38,
        R.drawable.sticker_quotes_39,
        R.drawable.sticker_quotes_30,
        R.drawable.sticker_quotes_31,
        R.drawable.sticker_quotes_32,
        R.drawable.sticker_quotes_33,
        R.drawable.sticker_quotes_34,
        R.drawable.sticker_quotes_35,
        R.drawable.sticker_quotes_36,
        R.drawable.sticker_quotes_37,
        R.drawable.sticker_quotes_38,
        R.drawable.sticker_quotes_39,
        R.drawable.sticker_quotes_40,
        R.drawable.sticker_quotes_41,
        R.drawable.sticker_quotes_42,
        R.drawable.sticker_quotes_43,
        R.drawable.sticker_quotes_44,
        R.drawable.sticker_quotes_45,
        R.drawable.sticker_quotes_46,
        R.drawable.sticker_quotes_47,
        R.drawable.sticker_quotes_48,
        R.drawable.sticker_quotes_49,
        R.drawable.sticker_quotes_60,
        R.drawable.sticker_quotes_61,
        R.drawable.sticker_quotes_62,
        R.drawable.sticker_quotes_63,
        R.drawable.sticker_quotes_64,
        R.drawable.sticker_quotes_65,
        R.drawable.sticker_quotes_66,
        R.drawable.sticker_quotes_67,
        R.drawable.sticker_quotes_68,
        R.drawable.sticker_quotes_69,
        R.drawable.sticker_quotes_70,
        R.drawable.sticker_quotes_71,
        R.drawable.sticker_quotes_72,
        R.drawable.sticker_quotes_73,
        R.drawable.sticker_quotes_74,
        R.drawable.sticker_quotes_75,
        R.drawable.sticker_quotes_83,
        R.drawable.sticker_quotes_82,
        R.drawable.sticker_quotes_84,
        R.drawable.sticker_quotes_85,
        R.drawable.sticker_quotes_86,
        R.drawable.sticker_quotes_87,
        R.drawable.sticker_quotes_88,
        R.drawable.sticker_quotes_89,
        R.drawable.sticker_quotes_90,
        R.drawable.sticker_quotes_91,
    )

    private val stickersSpooky = listOf(
        R.drawable.sticker_spooky_1,
        R.drawable.sticker_spooky_2,
        R.drawable.sticker_spooky_3,
        R.drawable.sticker_spooky_4,
        R.drawable.sticker_spooky_5,
        R.drawable.sticker_spooky_6,
        R.drawable.sticker_spooky_7,
    )

    private val stickersTravel = listOf(
        R.drawable.sticker_travel_1,
        R.drawable.sticker_travel_2,
        R.drawable.sticker_travel_3,
        R.drawable.sticker_travel_4,
        R.drawable.sticker_travel_5,
        R.drawable.sticker_travel_6,
        R.drawable.sticker_travel_7,
        R.drawable.sticker_travel_8,
        R.drawable.sticker_travel_9,
        R.drawable.sticker_travel_10,
        R.drawable.sticker_travel_11,
        R.drawable.sticker_travel_12,
        R.drawable.sticker_travel_13,
        R.drawable.sticker_travel_14,
        R.drawable.sticker_travel_15,
        R.drawable.sticker_travel_16,
        R.drawable.sticker_travel_17,
        R.drawable.sticker_travel_18,
        R.drawable.sticker_travel_19,
        R.drawable.sticker_travel_20,
        R.drawable.sticker_travel_21,
        R.drawable.sticker_travel_22,
        R.drawable.sticker_travel_23,
        R.drawable.sticker_travel_24,
        R.drawable.sticker_travel_25,
    )

    val stickerMap = listOf(
        StickerType("days", stickersDays, R.drawable.sticker_days_sunday),
        StickerType("drinks", stickersDrinks, R.drawable.sticker_drinks_1),
        StickerType("fitness", stickersFitness, R.drawable.sticker_fitness_1),
        StickerType("food", stickersFood, R.drawable.sticker_food_1),
        StickerType("places", stickersPlaces, R.drawable.sticker_places_1),
        StickerType("quotes", stickersQuotes, R.drawable.sticker_quotes_1),
        StickerType("spooky", stickersSpooky, R.drawable.sticker_spooky_1),
        StickerType("travel", stickersTravel, R.drawable.sticker_travel_1),
    )

    val filterMap = listOf(
        FilterType(
            title = "NONE",
            filter = null,
            thumbnail = R.drawable.thumb_org
        ),
        FilterType(
            title = "Exposure",
            filter = GPUImageExposureFilter(),
            thumbnail = R.drawable.thumb_exposure
        ),
        FilterType(
            title = "Contrast",
            filter = GPUImageContrastFilter(),
            thumbnail = R.drawable.thumb_contrast
        ),
        FilterType(
            title = "Hue",
            filter = GPUImageHueFilter(),
            thumbnail = R.drawable.thumb_hue
        ),
        FilterType(
            title = "Mono",
            filter = GPUImageGrayscaleFilter(),
            thumbnail = R.drawable.thumb_mono
        ),
        FilterType(
            title = "Sepia",
            filter = GPUImageSepiaToneFilter(),
            thumbnail = R.drawable.thumb_sepia
        ),
        FilterType(
            title = "Sharpen",
            filter = GPUImageSharpenFilter(),
            thumbnail = R.drawable.thumb_sharpen
        ),
        FilterType(
            title = "Vibrant",
            filter = GPUImageVibranceFilter(),
            thumbnail = R.drawable.thumb_vibrance
        ),
        FilterType(
            title = "Gamma",
            filter = GPUImageGammaFilter(),
            thumbnail = R.drawable.thumb_gamma
        ),
        FilterType(
            title = "Emboss",
            filter = GPUImageEmbossFilter(),
            thumbnail = R.drawable.thumb_emboss
        ),
        FilterType(
            title = "Gaussian Blur",
            filter = GPUImageGaussianBlurFilter(),
            thumbnail = R.drawable.thumb_gussian_blur
        ),
        FilterType(
            title = "Box Blur",
            filter = GPUImageBoxBlurFilter(),
            thumbnail = R.drawable.thumb_box_blur
        ),
        FilterType(
            title = "Haze",
            filter = GPUImageHazeFilter(),
            thumbnail = R.drawable.thumb_haze
        ),
        FilterType(
            title = "RGB",
            filter = GPUImageRGBFilter(),
            thumbnail = R.drawable.thumb_rgb
        ),
        FilterType(
            title = "Kuwahara",
            filter = GPUImageKuwaharaFilter(),
            thumbnail = R.drawable.thumb_kuwahara
        ),
    )

}

data class StickerType(
    val title: String = "",
    val stickers: List<Int> = emptyList(),
    val thumbnail: Int = R.drawable.ic_round_sticker
)

data class FilterType(
    val title: String = "",
    val filter: GPUImageFilter? = GPUImageSepiaToneFilter(),
    val thumbnail: Int
)


///Convert a view into bitmap without drawing cache
fun View.toBitmap(): Bitmap {
    return drawToBitmap()
}


///Save a bitmap into file
fun Bitmap.saveToFile(
    file: File,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 85
): Boolean {
    return try {
        FileOutputStream(file).use { out ->
            this.compress(format, quality, out)
        }
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}

fun View.saveToFile(
    file: File,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 85
): Boolean {
    return drawToBitmap().saveToFile(file, format, quality)
}


@Suppress("DEPRECATION")
fun Uri.getBitmap(context: Context): Bitmap? {
    return if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images.Media.getBitmap(
            context.contentResolver,
            this
        )
    } else {
        val source = ImageDecoder.createSource(context.contentResolver, this)
        ImageDecoder.decodeBitmap(source)
    }
}


fun View.performBasicTouchAt(x: Float, y: Float) {
    dispatchTouchEvent(
        MotionEvent.obtain(
            SystemClock.uptimeMillis(),
            SystemClock.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            x, y, 0
        )
    )

}

fun Uri.getExifInterface(context: Context): ExifInterface? {
    return try {
        val stream = context.contentResolver.openInputStream(this)
        if (stream != null)
            ExifInterface(stream)
        else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun File.getExifInterface(): ExifInterface?{
    return try {
        ExifInterface(inputStream())
    }catch (e: Exception){
        e.printStackTrace()
        null
    }
}

///There will be multiple functions for the same task use as per your requirement
///Write a Bitmap into a file
fun File.writeBitmap(
    bitmap: Bitmap,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 85
) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}


///Correct orientation of a bitmap
///In some devices orientation might be wrong in those cases use this fun
fun Bitmap.rotateBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap? {

    val matrix = Matrix()
    var exif: ExifInterface?
    return try {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.let {
            exif = ExifInterface(inputStream)

            val orientation = exif!!.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            when (orientation) {
                ExifInterface.ORIENTATION_NORMAL -> return this
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                    matrix.setRotate(180f)
                    matrix.postScale(-1f, 1f)
                }

                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.setRotate(90f)
                    matrix.postScale(-1f, 1f)
                }

                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.setRotate(-90f)
                    matrix.postScale(-1f, 1f)
                }

                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
                else -> return this
            }

        } ?: return this
        val bmRotated = Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
        this.recycle()
        bmRotated.copy(Bitmap.Config.ARGB_8888, true)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


fun View.getScreenShot(activity: Activity, callback: (Bitmap) -> Unit) {
    activity.window?.let { window ->
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val locationOfViewInWindow = IntArray(2)
        getLocationInWindow(locationOfViewInWindow)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PixelCopy.request(
                    window,
                    Rect(
                        locationOfViewInWindow[0],
                        locationOfViewInWindow[1],
                        locationOfViewInWindow[0] + width,
                        locationOfViewInWindow[1] + height
                    ), bitmap, { copyResult ->
                        when (copyResult) {
                            PixelCopy.SUCCESS -> {
                                callback(bitmap)
                            }

                            else -> {

                            }
                        }
                        // possible to handle other result codes ...
                    },
                    Handler(Looper.getMainLooper())
                )
            }
        } catch (e: IllegalArgumentException) {
            // PixelCopy may throw IllegalArgumentException, make sure to handle it
            e.printStackTrace()
        }
    }
}


fun bitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
    vectorDrawable!!.setBounds(
        0, 0, (vectorDrawable.intrinsicWidth),
        (vectorDrawable.intrinsicHeight)
    )
    val bitmap = Bitmap.createBitmap(
        (vectorDrawable.intrinsicWidth),
        (vectorDrawable.intrinsicHeight),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun InputStream.toFile(path: String) {
    File(path).outputStream().use { this.copyTo(it) }
}

fun Uri.saveToFile(context: Context, path: String): Boolean{
    return try {
        context.contentResolver.openInputStream(this)?.let {
            it.toFile(path)
            it.close()
        }
        true
    }catch (e: Exception){
        e.printStackTrace()
        false
    }
}

