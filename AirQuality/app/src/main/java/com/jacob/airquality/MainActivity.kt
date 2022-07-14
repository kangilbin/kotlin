package com.jacob.airquality

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.jacob.airquality.databinding.ActivityMainBinding
import com.jacob.airquality.retrofit.AirQualityResponse
import com.jacob.airquality.retrofit.AirQualityService
import com.jacob.airquality.retrofit.RetrofitConnection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    var mInterstitalAd : InterstitialAd? = null

    lateinit var  binding: ActivityMainBinding
    // 위도와 경도를 저장할 객체 변수 선언
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    // 런타임 권한 요청 시 필요한 요청 코드
    private val PERMISSIONS_REQUEST_CODE = 100
    // 요청할 권한 목록
    var REQUIRED_PERMISSIONS =  arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)

    // 위치 서비스 요청 시 필요한 런처
    lateinit var  getGPSPermissionLauncher: ActivityResultLauncher<Intent>

    // 위도와 경도를 가져올 때 필요
    lateinit var locationProvider: LocationProvider

    var startMapActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult?) {
                if(result?.resultCode ?: 0 == Activity.RESULT_OK) {
                    latitude = result?.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
                    longitude = result?.data?.getDoubleExtra("longitude", 0.0) ?: 0.0
                    updateUI()
                }
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAllPermissions()   // 권한 확인
        updateUI()
        setRefreshButton()
        setFab()
    }

    private fun setFab() {
        binding.fab.setOnClickListener {

            if(mInterstitalAd != null) {                                          // 1)
                mInterstitalAd!!.fullScreenContentCallback =
                    object: FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d("ads log", "전면 광고가 닫혔습니다.")

                            // start of [기존 코드] this만 -> this@MainActivity로 수정   // 2)
                            val intent = Intent(this@MainActivity, MapActivity::class.java)
                            intent.putExtra("currentLat", latitude)
                            intent.putExtra("currentLng", longitude)
                            startMapActivityResult.launch(intent)
                            // end of [기존 코드]
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.d("ads log", "전면 광고가 열리는데 실패했습니다.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d("ads log", "전면 광고가 성공적으로 열렸습니다.")
                            mInterstitalAd = null                                       // 3)
                        }
                    }
                mInterstitalAd!!.show(this@MainActivity)
                // 4) this -> this@MainActivity로 수정
            }  else {
                Log.d("InterstitalAd", "전면 광고가 로딩되지 않았습니다.")
                Toast.makeText(
                    this@MainActivity,
                    "잠시 후 다시 시도해주세요.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun checkAllPermissions() {
        // 1. 위치 서비스(GPS)가 켜져있는지 확인
        if (!isLocationServicesAvailable()) {
            showDialogForLoactionServiceSetting();
        } else {    // 2. 런타임 앱 권한이 모두 허용되어 있는지 확인
            isRunTimePermissionsGranted();
        }
    }

    fun isLocationServicesAvailable() : Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    fun isRunTimePermissionsGranted() {
        // 위치 퍼미션을 가지고 있는지 체크
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if(hasFineLocationPermission != PackageManager.PERMISSION_GRANTED
            || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            // 권한이 한 개라도 없다면 퍼미션 요청
            ActivityCompat.requestPermissions(this@MainActivity,
            REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSIONS_REQUEST_CODE && grantResults.size ==
                REQUIRED_PERMISSIONS.size) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE이고, 요청한 퍼미션 개수만큼 수신되었다면
            var checkResult = true

            // 모든 퍼미션을 허용했는지 체크
            for(result in grantResults) {
                if(result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false
                    break
                }
            }
            if(checkResult) {
                // 위치값을 가져 올 수 있음
                updateUI()
            } else {
                // 퍼미션이 거부되었다면 앱 종료
                Toast.makeText(
                    this@MainActivity,
                    "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }
    private fun showDialogForLoactionServiceSetting() {
        // 먼저 ActivityResultLauncher를 설정해줍니다. 이 런처를 이용하여 결과값을
        // 반환해야 하는 인텐트를 실행할 수 있습니다.
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            // 결과값을 받았을 때 로직
            if(result.resultCode == Activity.RESULT_OK) {
                // 사용자가 GPS를 활성화시켰는지 확인
                if(isLocationServicesAvailable()) {
                    isRunTimePermissionsGranted()   // 런타임 권한 확인
                } else {
                    // 위치 서비스가 허용되지 않았다면 앱 종료
                    Toast.makeText(
                        this@MainActivity,
                        "위치 서비스를 사용할 수 없습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()    // 액티비티 종료
                }
            }
        }

        val builder : AlertDialog.Builder = AlertDialog.Builder(
            this@MainActivity)  // 사용자에게 의사를 물어보는 AlertDialog 생성
        builder.setTitle("위치 서비스 비활성화") // 제목 설정
        builder.setMessage("위치 서비스가 꺼져 있습니다. 설정해야 앱을 사용할 수 있습니다.")  // 내용 설정
        builder.setCancelable(true) // 다이얼로그 창 바깥 터치 시 창 닫힘
        builder.setPositiveButton("설정",
            DialogInterface.OnClickListener{
                dialog, id -> // 확인 버튼 설정
                val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                getGPSPermissionLauncher.launch(callGPSSettingIntent)
            })
        builder.setNegativeButton("취소", // 취소 버튼 설정
            DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
                Toast.makeText(this@MainActivity,
                    "기기에서 위치서비스(GPS) 설정 후 사용해주세요.",
                    Toast.LENGTH_SHORT).show()
                finish()
            })
        builder.create().show() // 다이얼로그 생성 및 보여주기
    }

    private fun updateUI() {
        locationProvider = LocationProvider(this@MainActivity)

        // 위도와 경도 정보를 가져옵니다.
        if (latitude == 0.0 || longitude == 0.0) {
            latitude = locationProvider.getLocationLatitude()
            longitude = locationProvider.getLocationLongitude()
        }

        if (latitude != 0.0 || longitude != 0.0) {
            // 1. 현재 위치를 가져오고 UI 업데이트
            // 현재 위치를 가져오기
            val address = getCurrentAddress(latitude, longitude)
            // 주소가 null이 아닐 경우 UI 업데이트
            address?.let {
                binding.tvLocationTitle.text = "${it.thoroughfare}" // 예시 : 역삼 1동
                binding.tvLocationSubtitle.text = "${it.countryName}"
                "${it.adminArea}"   // 예 : 대한민국 서울 특별시
            }

            // 2. 현재 미세먼지 농도 가져오고 UI 업데이트
            getAirQualityData(latitude, longitude)
        } else {
            Toast.makeText(
                this@MainActivity,
                "위도, 경도 정보를 가져올 수 없습니다. 새로고침을 눌러주세요.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun getCurrentAddress(latitude: Double, longitude: Double) : Address? {
        val geocoder = Geocoder(this, Locale.getDefault())
        // Address 객체는 주소와 관련된여러 정보를 가지고 있습니다.
        // android.location.Address 패키지 참고.
        val addresses: List<Address>?

        addresses = try {
            // Geocoder 객체를 이용하여 위도와 경도로부터 리스트를 가져옵니다.
            geocoder.getFromLocation(latitude, longitude, 7)
        } catch (ioException: IOException) {
            Toast.makeText(this, "지오코더 서비스 사용 불가합니다.",
                Toast.LENGTH_LONG).show()
            return null
        } catch (illegalArgumentException: IllegalAccessException) {
            Toast.makeText(this, "잘못된 위도, 경도 입니다",
                Toast.LENGTH_LONG).show()
            return null
        }

        // 에러는 아니지만 주소가 발견되지 않는 경우
        if(addresses == null || addresses.size == 0) {
            Toast.makeText(this, "주소가 발견되지 않았습니다.",
                Toast.LENGTH_LONG).show()
            return null
        }

        val address: Address = addresses[0]
        return address
    }
    /**
     * 레트로핏 클래스를 이용하여 미세먼지 오염 정보를 가져옵니다.
     */
    private fun getAirQualityData(latitude: Double, longitude: Double) {
        // 레트로핏 객체를 이용해 AirQualityService 인터페이스 구현체를 가져올 수 있음
        val retrofitAPI = RetrofitConnection.getInstance().create(AirQualityService::class.java)

        retrofitAPI.getAirQualityData(
            latitude.toString(),
            longitude.toString(),
            "ad57e475-aa81-4e33-bc22-abac6af0678b" // API key 입력
        ).enqueue(object : Callback<AirQualityResponse> {
            override fun onResponse(
                call: Call<AirQualityResponse>,
                response: Response<AirQualityResponse>
            ) {
                // 정상저인 Response가 왔다면 UI 업데이트
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@MainActivity,
                        "최신 정보 업데이트 완료!", Toast.LENGTH_SHORT
                    ).show()
                    // response.body()가  null이 아니면 updateAirUI()
                    response.body()?.let { updateAirUI(it) }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "업데이트에 실패했습니다.", Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@MainActivity, "업데이트에 실패했습니다.",
                Toast.LENGTH_SHORT).show()
            }
        })
    }
    /**
     * 가져온 데이터 정보를 바탕으로 화면 업데이트
     */
    private fun updateAirUI(airQualityData: AirQualityResponse){
        val pollutionData = airQualityData.data.current.pollution

        // 수치 지정(메인 화면 가운데 숫자)
        binding.tvCount.text = pollutionData.aqius.toString()

        // 측정된 날짜 지정
        val dateTime =
            ZonedDateTime.parse(pollutionData.ts).withZoneSameInstant(
                ZoneId.of("Asia/Seoul"))
                .toLocalDateTime()
        val dateFormatter: DateTimeFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm")

        binding.tvCheckTime.text = dateTime.format(dateFormatter).toString()

        when (pollutionData.aqius) {
            in 0..50 -> {
                binding.tvTitle.text = "좋음"
                binding.imgBg.setImageResource(R.drawable.bg_good)
            }
            in 51..150 -> {
                binding.tvTitle.text = "보통"
                binding.imgBg.setImageResource(R.drawable.bg_soso)
            }
            in 151..200 -> {
                binding.tvTitle.text = "나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_bad)
            }
            else -> {
                binding.tvTitle.text = "매우 나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_worst)
            }
        }
    }
    private fun setRefreshButton() {
        binding.btnRefresh.setOnClickListener { updateUI() }
    }

    /**
     * 배너 광고 설정 함수
     */
    private fun setBannerAds() {
        MobileAds.initialize(this)          // 1) 광고 SDK 초기화
        val adRequest = AdRequest.Builder().build()   // 2)
        binding.adView.loadAd(adRequest)            // 3)

        // 4) 애드뷰 리스너 추가
        binding.adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d("ads log", "배너 광고가 로드되었습니다.") // 로그 출력
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("ads log", "배너 광고가 로드 시패했습니다. ${adError.responseInfo}")
            }

            override fun onAdOpened() {
                Log.d("ads log", "배너 광고를 열었습니다.")
                // 전면 에 광고가 오버레이 되었을 때
            }

            override fun onAdClicked() {
                Log.d("ads log", "배너 광고를 클랙했습니다.")
            }

            override fun onAdClosed() {
                Log.d("ads log", "배너 광고를 닫았습니다.")
            }
        }
    }
    /**
     * 전면 광고 설정
     */
    private fun setInterstitalAds() {

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712",
            adRequest, object : InterstitialAdLoadCallback() { // 3)
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("ads log", "전면 광고가 로드 시패했습니다. ${adError.responseInfo}")
                    mInterstitalAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("ads log", "전면 광고가 로드가 되었습니다.")
                    mInterstitalAd = interstitialAd
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        setInterstitalAds()
    }
}