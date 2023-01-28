package com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.model.util

import android.graphics.PointF
import android.graphics.RectF

/**
 * The offset to the intersection of the top and right edges of this rectangle.
 */
val RectF.topRight: PointF get() = PointF(right, top)

val RectF.topLeft: PointF get() = PointF(left, top)

/**
 * The offset to the center of the left edge of this rectangle.
 */
val RectF.centerLeft: PointF get() = PointF(left, top + height() / 2.0f)

/**
 * The offset to the center of the right edge of this rectangle.
 */
val RectF.centerRight: PointF get() = PointF(right, top + height() / 2.0f)

/**
 * The offset to the intersection of the bottom and left edges of this rectangle.
 */
val RectF.bottomLeft: PointF get() = PointF(left, bottom)

/**
 * The offset to the intersection of the bottom and right edges of this rectangle.
 */
val RectF.bottomRight: PointF get() = PointF(right, bottom)
