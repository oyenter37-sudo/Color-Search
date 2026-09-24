package com.example.util

import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.math.sqrt

data class LabColor(val l: Float, val a: Float, val b: Float)

object ColorUtils {

    /**
     * Converts an RGB color to CIELAB color space.
     */
    fun rgbToLab(r: Int, g: Int, b: Int): LabColor {
        // Convert to linear RGB
        var rNorm = r / 255.0f
        var gNorm = g / 255.0f
        var bNorm = b / 255.0f

        rNorm = if (rNorm > 0.04045f) ((rNorm + 0.055f) / 1.055f).pow(2.4f) else rNorm / 12.92f
        gNorm = if (gNorm > 0.04045f) ((gNorm + 0.055f) / 1.055f).pow(2.4f) else gNorm / 12.92f
        bNorm = if (bNorm > 0.04045f) ((bNorm + 0.055f) / 1.055f).pow(2.4f) else bNorm / 12.92f

        // Convert to XYZ (D65 illuminant)
        val x = (rNorm * 0.4124f + gNorm * 0.3576f + bNorm * 0.1805f) / 0.95047f
        val y = (rNorm * 0.2126f + gNorm * 0.7152f + bNorm * 0.0722f) / 1.00000f
        val z = (rNorm * 0.0193f + gNorm * 0.1192f + bNorm * 0.9505f) / 1.08883f

        fun f(t: Float): Float {
            return if (t > 0.008856f) t.pow(1.0f / 3.0f) else (7.787f * t) + (16.0f / 116.0f)
        }

        val fx = f(x)
        val fy = f(y)
        val fz = f(z)

        val l = (116.0f * fy) - 16.0f
        val a = 500.0f * (fx - fy)
        val bVal = 200.0f * (fy - fz)

        return LabColor(l, a, bVal)
    }

    /**
     * Calculates CIE76 Delta-E between two RGB colors.
     */
    fun calculateDeltaE(r1: Int, g1: Int, b1: Int, r2: Int, g2: Int, b2: Int): Float {
        val lab1 = rgbToLab(r1, g1, b1)
        val lab2 = rgbToLab(r2, g2, b2)
        val dl = lab1.l - lab2.l
        val da = lab1.a - lab2.a
        val db = lab1.b - lab2.b
        return sqrt(dl * dl + da * da + db * db)
    }

    /**
     * Calculates match score (0..100%).
     * Uses a generous, player-friendly curve accounting for smartphone camera lighting,
     * ambient shadows, and sensor white balance variations.
     */
    fun calculateAccuracy(
        sampledR: Int,
        sampledG: Int,
        sampledB: Int,
        targetR: Int,
        targetG: Int,
        targetB: Int
    ): Float {
        val sR = sampledR.coerceIn(0, 255)
        val sG = sampledG.coerceIn(0, 255)
        val sB = sampledB.coerceIn(0, 255)
        val tR = targetR.coerceIn(0, 255)
        val tG = targetG.coerceIn(0, 255)
        val tB = targetB.coerceIn(0, 255)

        val deltaE = calculateDeltaE(sR, sG, sB, tR, tG, tB)

        // Check HSV Hue proximity to reward matching the intended color family
        val hsvSampled = FloatArray(3)
        val hsvTarget = FloatArray(3)
        android.graphics.Color.RGBToHSV(sR, sG, sB, hsvSampled)
        android.graphics.Color.RGBToHSV(tR, tG, tB, hsvTarget)

        var hueDiff = kotlin.math.abs(hsvSampled[0] - hsvTarget[0])
        if (hueDiff > 180f) hueDiff = 360f - hueDiff

        // If target or sample has very low saturation (grays/whites/blacks), hue is less relevant than lightness/value
        val isAchromatic = hsvTarget[1] < 0.15f || hsvSampled[1] < 0.12f
        val hueFactor = if (isAchromatic) {
            val valDiff = kotlin.math.abs(hsvSampled[2] - hsvTarget[2])
            (1f - valDiff).coerceIn(0f, 1f)
        } else {
            (1f - (hueDiff / 140f)).coerceIn(0f, 1f)
        }

        // Forgiving Delta-E curve: deltaE of 40-50 still gives a healthy match in real world lighting
        val deltaScore = (1.0f - (deltaE / 88.0f)).coerceIn(0f, 1f)

        // Balanced combination giving credit to finding the right color object
        val finalScore = (deltaScore * 0.52f + hueFactor * 0.48f) * 100f
        return finalScore.coerceIn(0f, 100f)
    }

    fun toHex(r: Int, g: Int, b: Int): String {
        return String.format("#%02X%02X%02X", r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
    }

    fun toColor(r: Int, g: Int, b: Int): Color {
        return Color(r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
    }
}
