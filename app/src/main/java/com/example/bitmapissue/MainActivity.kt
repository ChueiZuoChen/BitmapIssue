package com.example.bitmapissue

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainContent(modifier = Modifier.fillMaxSize())
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun MainContent(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }
    val humanPainter = remember { mutableStateOf<Painter?>(null) }
    var screenSize = remember {
        mutableStateOf(IntSize.Zero)
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = {
            if (Build.VERSION.SDK_INT < 28) {
                bitmap.value = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                val bm = ImageDecoder.decodeBitmap(source)
                bitmap.value = resizeBitmap(bm, screenSize.value.height, screenSize.value.width)
                Log.d(TAG, "b - w: ${bitmap.value!!.width} h: ${bitmap.value!!.height}")
                Log.d(TAG, "s - w: ${screenSize.value.width} h: ${screenSize.value.height}")
            }
        }
    )
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        ConstraintLayout(modifier = modifier
            .align(Alignment.Center)
            .onSizeChanged {
                screenSize.value = it
                Log.d(TAG, "s - w: ${it.width} h: ${it.height}")
            }, constraintSet = constrains()) {
            bitmap.value?.let {
                Image(
                    modifier = Modifier
                        .layoutId("image")
                        .fillMaxSize(),
                    painter = humanPainter.value!!,
                    contentDescription = "",
                    contentScale = ContentScale.Crop
                )
            }
            Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.layoutId("button")) { Text(text = "Pick image") }
            LaunchedEffect(Unit) {
                humanPainter.value = object : Painter() {
                    override val intrinsicSize: Size
                        get() = Size(
                            bitmap.value?.width?.toFloat() ?: 0f,
                            bitmap.value?.height?.toFloat() ?: 0f
                        )

                    override fun DrawScope.onDraw() {
                        bitmap.value?.let {
                            drawImage(it.asImageBitmap())
                            drawCircle(Color.Red, radius = 2f)
                        }
                    }
                }
            }
        }
    }
}

fun Bitmap.resizeBitmap(screenSize: IntSize): Bitmap {
    if (screenSize.height > 0 && screenSize.width > 0) {
        var dstW = screenSize.width
        var dstH = screenSize.height
        val srcW = this.width
        val srcH = this.height
        val srcRatio = srcW.toFloat() / srcH.toFloat()
        val dstRatio = dstW.toFloat() / dstH.toFloat()
        if (dstRatio > srcRatio) {
            dstW = (dstH * srcRatio).toInt()
        } else {
            dstH = (dstW / srcRatio).toInt()
        }
        return Bitmap.createScaledBitmap(this, dstW, dstH, true)
    } else {
        return this
    }
}

fun resizeBitmap(image: Bitmap, maxHeight: Int, maxWidth: Int): Bitmap {

    if (maxHeight > 0 && maxWidth > 0) {

        val sourceWidth: Int = image.width
        val sourceHeight: Int = image.height

        var targetWidth = maxWidth
        var targetHeight = maxHeight

        val sourceRatio = sourceWidth.toFloat() / sourceHeight.toFloat()
        val targetRatio = maxWidth.toFloat() / maxHeight.toFloat()

        if (targetRatio > sourceRatio) {
            targetWidth = (maxHeight.toFloat() * sourceRatio).toInt()
        } else {
            targetHeight = (maxWidth.toFloat() / sourceRatio).toInt()
        }

        return Bitmap.createScaledBitmap(
            image, targetWidth, targetHeight, true
        )

    } else {
        throw RuntimeException()
    }
}

fun constrains(): ConstraintSet = ConstraintSet {
    val image = createRefFor("image")
    val button = createRefFor("button")
    constrain(image) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(parent.bottom)
    }
    constrain(button) {
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(parent.bottom, margin = 30.dp)
    }
}




