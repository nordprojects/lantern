package com.example.androidthings.lantern.channels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.androidthings.lantern.Channel
import com.example.androidthings.lantern.hardware.Camera


/**
 * Makes a picture with the camera and projects it afterwards
 */
class ScreenShot : Channel() {

    val TAG = this::class.java.simpleName

    private lateinit var view: ImageView

    private val handler: Handler = Handler()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.view = ImageView(context)
        this.view.scaleType = ImageView.ScaleType.CENTER_INSIDE

        val mCameraThread = HandlerThread("CameraBackground")
        mCameraThread.start()
        val mCameraHandler = Handler(mCameraThread.looper)

        val mCamera = Camera.getInstance()
        mCamera.initializeCamera(this.activity, mCameraHandler, imageAvailableListener)

        handler.postDelayed({ mCamera.takePicture() }, 3000)
        return view
    }

    private val imageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        Log.d(TAG, "ImageAvailable!")
        val image = reader.acquireLatestImage()
        val imageBuffer = image.planes[0].buffer
        val imageBytes = ByteArray(imageBuffer.remaining())
        imageBuffer.get(imageBytes)
        image.close()
        val bitmap = getBitmapFromByteArray(imageBytes)
        handler.post({ this.view.setImageBitmap(bitmap) })
    }

    private fun getBitmapFromByteArray(imageBytes: ByteArray): Bitmap {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix(), true)
    }
}