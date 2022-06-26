package com.jaocb.qrcodereader

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.jaocb.qrcodereader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 1
    private val PERMISSION_REQUIRED = arrayOf(Manifest.permission.CAMERA)

    private lateinit var binding : ActivityMainBinding  // 1.바인딩 변수 생성
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2.뷰 바인딩 설정
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if(!hasPermssion(this)) {
            // 카메라 권한을 요청합니다.
            requestPermissions(PERMISSION_REQUIRED, PERMISSION_REQUEST_CODE)
        } else {
            // 만약 이미 권한이 있다면 카메라를 시작합니다.
            startCamera() // 카메라 시작
        }
    }
    // 권한 유무 확인
    fun hasPermssion(context : Context) = PERMISSION_REQUIRED.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
    // 권한 요청 콜백 함수
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSION_REQUEST_CODE) {
            if(PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                Toast.makeText(this@MainActivity, "권한 요청이 승인 되었습니다.", Toast.LENGTH_LONG).show()
                startCamera()
            } else {
                Toast.makeText(this@MainActivity, "권한 요청이 거부 되었습니다.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    // 3.미리보기와 이미지 분석 시작
    fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()

            val preview = getPreview()  // 미리보기 객체 가져오기
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA // 후면 카메라 선택

            // 미리보기 기능 선택
            cameraProvider.bindToLifecycle(this, cameraSelector, preview)
        }, ContextCompat.getMainExecutor(this))
    }

    // 4.미리보기 객체 반환
    fun getPreview() : Preview {
        val preview : Preview = Preview.Builder().build() // Preview 객체 생성
        preview.setSurfaceProvider(binding.barcodePreview.getSurfaceProvider())
        /*
        SurfaceProvider는 Preview에 Surface를 제공해주는 인터페이스이다. 화면에 보여지는 픽셀들이
        모여있는 객체가 Surface라고 생각하면 된다. 인수로는 activity_main.xml에서
        PreviewView의 SurfaceProvider를 줍니다.
        */
        return preview
    }
}