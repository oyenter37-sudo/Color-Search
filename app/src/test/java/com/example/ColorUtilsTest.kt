package com.example

import com.example.util.ColorUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorUtilsTest {

    @Test
    fun testRgbToLabIdentical() {
        val lab1 = ColorUtils.rgbToLab(255, 0, 0)
        val lab2 = ColorUtils.rgbToLab(255, 0, 0)
        assertEquals(lab1.l, lab2.l, 0.001f)
        assertEquals(lab1.a, lab2.a, 0.001f)
        assertEquals(lab1.b, lab2.b, 0.001f)
    }

    @Test
    fun testDeltaEZeroForSameColor() {
        val deltaE = ColorUtils.calculateDeltaE(120, 200, 50, 120, 200, 50)
        assertEquals(0f, deltaE, 0.001f)
    }

    @Test
    fun testDeltaEPositiveForDifferentColors() {
        val deltaE = ColorUtils.calculateDeltaE(255, 0, 0, 0, 255, 0)
        assertTrue(deltaE > 50f)
    }

    @Test
    fun testToHexFormatting() {
        assertEquals("#FF0000", ColorUtils.toHex(255, 0, 0))
        assertEquals("#00FF00", ColorUtils.toHex(0, 255, 0))
        assertEquals("#0000FF", ColorUtils.toHex(0, 0, 255))
        assertEquals("#000000", ColorUtils.toHex(0, 0, 0))
        assertEquals("#FFFFFF", ColorUtils.toHex(255, 255, 255))
    }
}
