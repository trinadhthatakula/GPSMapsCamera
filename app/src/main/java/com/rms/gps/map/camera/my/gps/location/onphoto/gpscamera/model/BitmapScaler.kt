package com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.model

import android.graphics.Bitmap
import java.util.concurrent.atomic.AtomicInteger


// scale and keep aspect ratio
fun Bitmap.scaleToFitWidth(width: Int): Bitmap? {
    val factor = width / width.toFloat()
    return Bitmap.createScaledBitmap(this, width, (height * factor).toInt(), true)
}


// scale and keep aspect ratio
fun Bitmap.scaleToFitHeight( height: Int): Bitmap? {
    val factor = height / height.toFloat()
    return Bitmap.createScaledBitmap(this, (width * factor).toInt(), height, true)
}

// scale and keep aspect ratio
fun Bitmap.scaleToFitHeight(height: AtomicInteger): Bitmap? {
    val factor = height.toInt() / height.toFloat()
    return Bitmap.createScaledBitmap(this, (width * factor).toInt(), height.toInt(), true)
}


// scale and keep aspect ratio
fun Bitmap.scaleToFill( width: Int, height: Int): Bitmap {
    val factorH = height / height.toFloat()
    val factorW = width / width.toFloat()
    val factorToUse = if (factorH > factorW) factorW else factorH
    return Bitmap.createScaledBitmap(
        this, (width * factorToUse).toInt(),
        (height * factorToUse).toInt(), true
    )
}


// scale and don't keep aspect ratio
fun strechToFill(b: Bitmap, width: Int, height: Int): Bitmap? {
    val factorH = height / b.height.toFloat()
    val factorW = width / b.width.toFloat()
    return Bitmap.createScaledBitmap(
        b, (b.width * factorW).toInt(),
        (b.height * factorH).toInt(), true
    )
}

fun scaleToFitWidth(b: Bitmap, width: AtomicInteger): Bitmap {
    val factor = width.toInt() / b.width.toFloat()
    return Bitmap.createScaledBitmap(b, width.toInt(), (b.height * factor as Float).toInt(), true)
}
