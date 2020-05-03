package com.vin.booking.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.vin.booking.R
import com.vin.booking.activities.MainActivity
import com.vin.booking.di.InjectableFragment
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.layout_scan_qr.containerScanner
import me.dm7.barcodescanner.core.ViewFinderView
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.util.ArrayList
import javax.inject.Inject

class ScanQRFragment : Fragment(), HasAndroidInjector, InjectableFragment,
    ZXingScannerView.ResultHandler {

    private val mainActivity: MainActivity? get() = activity as? MainActivity

    var listener: ScannerResultListener? = null

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    private lateinit var viewScanner: ZXingScannerView
    private var cameraPermissionGranted = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.layout_scan_qr, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewScanner = object : ZXingScannerView(context) {
            override fun createViewFinderView(context: Context) =
                ViewFinderView(context).apply { setSquareViewFinder(true) }
        }.apply {
            setFormats(ArrayList<BarcodeFormat>().apply { add(BarcodeFormat.QR_CODE) })
            setAutoFocus(true)
            setAspectTolerance(ASPECT_TOLERANCE)
        }
        containerScanner.addView(viewScanner)
    }

    override fun onStart() {
        super.onStart()
        context?.let { checkCameraPermission(it) }
    }

    override fun onStop() {
        super.onStop()
        stopCamera()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSION_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraPermissionGranted = true
                startCamera()
            } else {
                cameraPermissionGranted = false
            }
        } else {
            cameraPermissionGranted = false
        }
    }

    private fun checkCameraPermission(context: Context) {
        cameraPermissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!cameraPermissionGranted) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_PERMISSION_CAMERA
            )
        } else {
            startCamera()
        }
    }

    override fun handleResult(result: Result?) {
        stopCamera()
        result?.text?.let {
            listener?.onResult(it)
            mainActivity?.onBackPressed()
        }
    }

    private fun startCamera() {
        viewScanner.startCamera()
        viewScanner.setResultHandler(this)
    }

    private fun stopCamera() {
        viewScanner.stopCameraPreview()
        viewScanner.stopCamera()
    }

    override fun androidInjector() = dispatchingAndroidInjector

    interface ScannerResultListener {
        fun onResult(result: String)
    }

    companion object {
        private const val ASPECT_TOLERANCE = 0.5F
        private const val REQUEST_CODE_PERMISSION_CAMERA = 0

        fun newInstance() = ScanQRFragment()
    }
}