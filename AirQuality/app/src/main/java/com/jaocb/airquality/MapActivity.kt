package com.jaocb.airquality

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.jaocb.airquality.databinding.ActivityMapBinding

class MapActivity : AppCompatActivity(), OnMapReadyCallback {               // 1)

    lateinit var binding: ActivityMapBinding

    private var mMap: GoogleMap? = null
    var currentLat: Double = 0.0 // MainActivity.kt에서 전달된 위도
    var currentLng: Double = 0.0 // MainActivity.kt에서 전달된 경도
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // MainActivity.kt에서 intent로 전달된 값을 가져옵니다.
        currentLat = intent.getDoubleExtra("currentLat",0.0)
        currentLng = intent.getDoubleExtra("currentLng",0.0)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) // 2)
                as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.btnCheckHere.setOnClickListener{
            mMap?.let {
                val intent = Intent()
                // 버튼이 눌린 시점의 카메라 포지션(위,경도)을 가져옵니다.
                intent.putExtra("latitude", it.cameraPosition.target.latitude)
                intent.putExtra("longitude", it.cameraPosition.target.longitude)
                // MainActivity.kt에 정의해 두었던 onActivityResult()함수가 실행됩니다.
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {                         // 3)
        mMap = googleMap

        mMap?.let {
            val currentLocation = LatLng(currentLat, currentLng)
            it.setMaxZoomPreference(20.0f)  // 줌 최댓값 설정
            it.setMinZoomPreference(12.0f)  // 줌 최솟값 설정
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16f))
            setMarker()

            binding.fabCurrentLoaction.setOnClickListener {                    // 4)
                val locationProvider = LocationProvider(this@MapActivity)
                // 위도와 경도 정보를 가져옵니다.
                val latitude = locationProvider.getLocationLatitude()
                val longitude = locationProvider.getLocationLongitude()
                mMap?.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(LatLng(latitude, longitude), 16f))
                setMarker()
            }
        }
    }

    // 마커 설정하는 함수
    private fun setMarker() {
        mMap?.let {
            it.clear()  // 지도에 있는 마커를 먼저 삭제
            val markerOptions = MarkerOptions()
            markerOptions.position(it.cameraPosition.target)    // 마커의 위치 설정
            markerOptions.title("마커 위치")    // 마커 이름 설정
            val marker = it.addMarker(markerOptions)    // 지도에 마커를 추가하고, 마커 객체를 반환

            it.setOnCameraMoveListener {
                marker?.let { marker ->
                    marker.setPosition(it.cameraPosition.target)    // 5)
                }
            }
        }
    }
}