package com.example.photooverlay

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PhotoOverlayScreen()
                }
            }
        }
    }
}

@Composable
private fun PhotoOverlayScreen() {
    val context = LocalContext.current
    var baseUri by remember { mutableStateOf<Uri?>(null) }
    var overlayUri by remember { mutableStateOf<Uri?>(null) }
    var baseBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var overlayBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var baseAlpha by remember { mutableFloatStateOf(1f) }
    var overlayAlpha by remember { mutableFloatStateOf(0.5f) }
    var loading by remember { mutableStateOf(false) }

    val basePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> baseUri = uri }
    val overlayPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> overlayUri = uri }

    LaunchedEffect(baseUri) {
        baseBitmap = baseUri?.let { uri ->
            withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
            }
        }
    }
    LaunchedEffect(overlayUri) {
        overlayBitmap = overlayUri?.let { uri ->
            withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("照片叠加", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("选择两张照片，分别调整透明度，再把合成结果保存到相册。", style = MaterialTheme.typography.bodyMedium)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.82f)
                    .background(Color(0xFFE5E5E5)),
                contentAlignment = Alignment.Center
            ) {
                if (baseBitmap == null && overlayBitmap == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        androidx.compose.material3.Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("预览区域", color = Color.DarkGray)
                    }
                } else {
                    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                        drawRect(Color(0xFFE8E8E8))
                        val b1 = baseBitmap
                        val b2 = overlayBitmap
                        if (b1 != null) {
                            val scale = minOf(size.width / b1.width, size.height / b1.height)
                            val w = b1.width * scale
                            val h = b1.height * scale
                            drawContext.canvas.nativeCanvas.drawBitmap(
                                b1,
                                null,
                                RectF((size.width - w) / 2f, (size.height - h) / 2f, (size.width + w) / 2f, (size.height + h) / 2f),
                                Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = (baseAlpha * 255).toInt() }
                            )
                        }
                        if (b2 != null) {
                            val scale = minOf(size.width / b2.width, size.height / b2.height)
                            val w = b2.width * scale
                            val h = b2.height * scale
                            drawContext.canvas.nativeCanvas.drawBitmap(
                                b2,
                                null,
                                RectF((size.width - w) / 2f, (size.height - h) / 2f, (size.width + w) / 2f, (size.height + h) / 2f),
                                Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = (overlayAlpha * 255).toInt() }
                            )
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { basePicker.launch("image/*") }, modifier = Modifier.weight(1f)) {
                androidx.compose.material3.Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(Modifier.size(6.dp))
                Text(if (baseBitmap == null) "选择底图" else "更换底图")
            }
            OutlinedButton(onClick = { overlayPicker.launch("image/*") }, modifier = Modifier.weight(1f)) {
                androidx.compose.material3.Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(Modifier.size(6.dp))
                Text(if (overlayBitmap == null) "选择叠加图" else "更换叠加图")
            }
        }

        AlphaControl(title = "底图透明度", value = baseAlpha, onValueChange = { baseAlpha = it })
        AlphaControl(title = "叠加图透明度", value = overlayAlpha, onValueChange = { overlayAlpha = it })

        Button(
            onClick = {
                val first = baseBitmap
                val second = overlayBitmap
                if (first == null || second == null) {
                    Toast.makeText(context, "请先选择底图和叠加图", Toast.LENGTH_SHORT).show()
                } else {
                    loading = true
                    try {
                        val output = compositeBitmaps(first, second, baseAlpha, overlayAlpha)
                        val name = "PhotoOverlay_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".jpg"
                        val values = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, name)
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PhotoOverlay")
                                put(MediaStore.Images.Media.IS_PENDING, 1)
                            }
                        }
                        val resolver = context.contentResolver
                        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        if (uri == null) throw IllegalStateException("无法创建图片文件")
                        resolver.openOutputStream(uri)?.use { stream ->
                            if (!output.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                                throw IllegalStateException("图片编码失败")
                            }
                        } ?: throw IllegalStateException("无法写入图片")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            values.clear()
                            values.put(MediaStore.Images.Media.IS_PENDING, 0)
                            resolver.update(uri, values, null, null)
                        }
                        Toast.makeText(context, "已保存到 Pictures/PhotoOverlay", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "导出失败：${e.localizedMessage ?: "未知错误"}", Toast.LENGTH_LONG).show()
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.material3.Icon(Icons.Default.SaveAlt, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text(if (loading) "正在导出…" else "合成并导出图片")
        }
        Text(
            "说明：两张图片会按各自透明度叠放，并在预览区域居中等比例显示。导出为 JPG，保存到系统相册的 Pictures/PhotoOverlay 文件夹。",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
private fun AlphaControl(title: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("${(value * 100).toInt()}%")
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = 0f..1f)
    }
}

private fun compositeBitmaps(
    base: Bitmap,
    overlay: Bitmap,
    baseAlpha: Float,
    overlayAlpha: Float
): Bitmap {
    val output = Bitmap.createBitmap(base.width, base.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    canvas.drawColor(android.graphics.Color.WHITE)
    val basePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        alpha = (baseAlpha * 255).toInt()
    }
    canvas.drawBitmap(base, null, Rect(0, 0, base.width, base.height), basePaint)

    val scale = minOf(base.width.toFloat() / overlay.width, base.height.toFloat() / overlay.height)
    val w = overlay.width * scale
    val h = overlay.height * scale
    val dst = RectF(
        (base.width - w) / 2f,
        (base.height - h) / 2f,
        (base.width + w) / 2f,
        (base.height + h) / 2f
    )
    val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        alpha = (overlayAlpha * 255).toInt()
    }
    canvas.drawBitmap(overlay, null, dst, overlayPaint)
    return output
}
