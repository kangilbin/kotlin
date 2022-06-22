package com.jaocb.mediaplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var btn_play : Button
    lateinit var btn_pause : Button
    lateinit var btn_stop : Button
    var mService: MusicPlayerService? = null        // 1. 서비스 변수 선언

    // 2. 서비스와 구성요소 연결 상태 모니터링
    val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // service를 MusicPlayerBinder로 형변환
            mService = (service as MusicPlayerService.MusicPlayerBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // 만약 서비스가 끊기면, mService를 null로 만들어줍니다.
            mService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_play = findViewById(R.id.btn_play)
        btn_pause = findViewById(R.id.btn_pause)
        btn_stop = findViewById(R.id.btn_stop)

        // 리스너 등록
        btn_play.setOnClickListener(this)
        btn_pause.setOnClickListener(this)
        btn_stop.setOnClickListener(this)
    }

    // 뷰가 클릭되면 실행 콜백 함수
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_play -> {
                play()
            }
            R.id.btn_pause -> {
                pause()
            }
            R.id.btn_stop -> {
                stop()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        //3. 서비스 실행
        if(mService == null) {
            // 안드로이드 O 이상이면 startForegroundService를 사용해야 합니다
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, MusicPlayerService::class.java))
            } else {
                startService(Intent(applicationContext, MusicPlayerService::class.java))
            }
            // 4. 액티비티를 서비스와 바인드 시킵니다
            val intent = Intent(this, MusicPlayerService::class.java)
            // 서비스와 바인드
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onPause() {
        super.onPause()

        // 5.사용자가 액티비티를 떠났을 때 처리
        if(mService != null) {
            if(!mService!!.isPlaying()) { // mService가 재생되고 있지 않다면
                mService!!.stopSelf()   // 서비스를 중단해 줍니다.
            }
            unbindService(mServiceConnection)   // 서비스로부터 연결을 끊습니다.
            mService = null
        }
    }

    private fun play() {
        mService?.play()
    }
    private fun pause() {
        mService?.pause()
    }
    private fun stop() {
        mService?.stop()
    }
}