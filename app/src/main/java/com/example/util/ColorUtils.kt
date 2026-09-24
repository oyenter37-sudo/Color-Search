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
     * With tolerance threshold (typically 75% for game success).
     */
    fun calculateAccuracy(
        sampledR: Int,
        sampledG: Int,
        sampledB: Int,
        targetR: Int,
        targetG: Int,
        targetB: Int
    ): Float {
        val deltaE = calculateDeltaE(sampledR, sampledG, sampledB, targetR, targetG, targetB)
        
        // Also check HSV Hue proximity to reward matching color family
        val hsvSampled = FloatArray(3)
        val hsvTarget = FloatArray(3)
        android.graphics.Color.RGBToHSV(sampledR, sampledG, sampledB, hsvSampled)
        android.graphics.Color.RGBToHSV(targetR, targetG, targetB, hsvTarget)

        var hueDiff = kotlin.math.abs(hsvSampled[0] - hsvTarget[0])
        if (hueDiff > 180f) hueDiff = 360f - hueDiff
        val hueFactor = (1f - (hueDiff / 180f)).coerceIn(0f, 1f)

        // Delta-E mapping: deltaE=0 -> 100%, deltaE=20 -> ~80%, deltaE=45 -> ~50%, deltaE>70 -> low
        val deltaScore = (1.0f - (deltaE / 55.0f)).coerceIn(0f, 1f)
        
        // Combined weighted score
        val finalScore = (deltaScore * 0.75f + hueFactor * 0.25f) * 100f
        return finalScore.coerceIn(0f, 100f)
    }

    fun toHex(r: Int, g: Int, b: Int): String {
        return String.format("#%02X%02X%02X", r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
    }

    fun toColor(r: Int, g: Int, b: Int): Color {
        return Color(r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
    }
}
