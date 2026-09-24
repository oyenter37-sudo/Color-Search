package com.example.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    isTorchOn: Boolean,
    useFrontCamera: Boolean,
    onColorSampled: (r: Int, g: Int, b: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraExecutor.shutdown()
            } catch (_: Throwable) {}
        }
    }

    // Safely enable / disable torch with capability checks
    LaunchedEffect(isTorchOn, camera) {
        try {
            val cam = camera
            if (cam != null && cam.cameraInfo.hasFlashUnit()) {
                cam.cameraControl.enableTorch(isTorchOn)
            }
        } catch (_: Throwable) {
        }
    }

    // Bind camera whenever permissions, preview view, or camera lens changes
    LaunchedEffect(hasCameraPermission, useFrontCamera, previewView, cameraProvider) {
        val cp = cameraProvider ?: return@LaunchedEffect
        val pv = previewView ?: return@LaunchedEffect
        if (!hasCameraPermission) return@LaunchedEffect

        try {
            val desiredSelector = if (useFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            val fallbackSelector = if (useFrontCamera) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            val actualSelector = when {
                cp.hasCamera(desiredSelector) -> desiredSelector
                cp.hasCamera(fallbackSelector) -> fallbackSelector
                else -> null
            } ?: return@LaunchedEffect

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = pv.surfaceProvider
            }

            var lastAnalysisTime = 0L

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        try {
                            val now = System.currentTimeMillis()
                            if (now - lastAnalysisTime >= 60) { // ~16 FPS, light on CPU
                                lastAnalysisTime = now
                                extractCenterColor(imageProxy) { r, g, b ->
                                    ContextCompat.getMainExecutor(context).execute {
                                        onColorSampled(r, g, b)
                                    }
                                }
                            }
                        } catch (_: Throwable) {
                        } finally {
                            try {
                                imageProxy.close()
                            } catch (_: Throwable) {}
                        }
                    }
                }

            cp.unbindAll()
            camera = cp.bindToLifecycle(
                lifecycleOwner,
                actualSelector,
                preview,
                imageAnalysis
            )
        } catch (_: Throwable) {
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("camera_preview_view"),
                factory = { ctx ->
                    val pv = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    previewView = pv

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            cameraProvider = cameraProviderFuture.get()
                        } catch (_: Throwable) {
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    pv
                }
            )
        } else {
            // Permission request UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CyberObsidian)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Доступ к камере необходим",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Чтобы искать цвета в реальном мире, приложению нужно видеть ваше окружение через камеру.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CyberObsidian),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("request_camera_permission_button")
                ) {
                    Text("Разрешить камеру", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Safely extracts average RGB from the center of the frame.
 * Uses ImageProxy.toBitmap() when available, with a bulletproof YUV buffer fallback.
 */
private fun extractCenterColor(
    image: ImageProxy,
    onResult: (r: Int, g: Int, b: Int) -> Unit
) {
    try {
        val bitmap: Bitmap? = try {
            image.toBitmap()
        } catch (_: Throwable) {
            null
        }

        if (bitmap != null) {
            val cx = bitmap.width / 2
            val cy = bitmap.height / 2
            var sumR = 0L
            var sumG = 0L
            var sumB = 0L
            var count = 0

            val radius = 4 // sample 9x9 pixels in center
            for (dy in -radius..radius) {
                for (dx in -radius..radius) {
                    val px = (cx + dx).coerceIn(0, bitmap.width - 1)
                    val py = (cy + dy).coerceIn(0, bitmap.height - 1)
                    val pixel = bitmap.getPixel(px, py)
                    sumR += android.graphics.Color.red(pixel)
                    sumG += android.graphics.Color.green(pixel)
                    sumB += android.graphics.Color.blue(pixel)
                    count++
                }
            }

            if (count > 0) {
                onResult((sumR / count).toInt(), (sumG / count).toInt(), (sumB / count).toInt())
                return
            }
        }
    } catch (_: Throwable) {
    }

    // Direct YUV fallback with bounds checking
    try {
        val planes = image.planes
        if (planes.size < 3) return

        val yPlane = planes[0]
        val uPlane = planes[1]
        val vPlane = planes[2]

        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        val width = image.width
        val height = image.height
        val centerX = width / 2
        val centerY = height / 2
        val sampleRadius = 8

        var sumR = 0L
        var sumG = 0L
        var sumB = 0L
        var count = 0

        val yRowStride = yPlane.rowStride
        val yPixelStride = yPlane.pixelStride
        val uRowStride = uPlane.rowStride
        val uPixelStride = uPlane.pixelStride
        val vRowStride = vPlane.rowStride
        val vPixelStride = vPlane.pixelStride

        val yLimit = yBuffer.limit()
        val uLimit = uBuffer.limit()
        val vLimit = vBuffer.limit()

        val startY = (centerY - sampleRadius).coerceAtLeast(0)
        val endY = (centerY + sampleRadius).coerceAtMost(height - 1)
        val startX = (centerX - sampleRadius).coerceAtLeast(0)
        val endX = (centerX + sampleRadius).coerceAtMost(width - 1)

        for (y in startY..endY) {
            val yOffset = y * yRowStride
            val uvY = y / 2
            val uvOffsetU = uvY * uRowStride
            val uvOffsetV = uvY * vRowStride

            for (x in startX..endX) {
                val yIndex = yOffset + x * yPixelStride
                val uvX = x / 2
                val uIndex = uvOffsetU + uvX * uPixelStride
                val vIndex = uvOffsetV + uvX * vPixelStride

                if (yIndex in 0 until yLimit && uIndex in 0 until uLimit && vIndex in 0 until vLimit) {
                    val yVal = (yBuffer.get(yIndex).toInt() and 0xFF)
                    val uVal = (uBuffer.get(uIndex).toInt() and 0xFF) - 128
                    val vVal = (vBuffer.get(vIndex).toInt() and 0xFF) - 128

                    val r = (yVal + 1.402f * vVal).toInt().coerceIn(0, 255)
                    val g = (yVal - 0.344136f * uVal - 0.714136f * vVal).toInt().coerceIn(0, 255)
                    val b = (yVal + 1.772f * uVal).toInt().coerceIn(0, 255)

                    sumR += r
                    sumG += g
                    sumB += b
                    count++
                }
            }
        }

        if (count > 0) {
            onResult((sumR / count).toInt(), (sumG / count).toInt(), (sumB / count).toInt())
        }
    } catch (_: Throwable) {
    }
}
