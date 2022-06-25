package com.jaocb.mediaplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast

class MusicPlayerService : Service() {
    var mMediaPlayer: MediaPlayer? = null
    var mBinder: MusicPlayerBinder = MusicPlayerBinder()

    inner class MusicPlayerBinder : Binder() {
        fun getService(): MusicPlayerService{
            return this@MusicPlayerService
        }
    }

    override fun onCreate() {           // 1. 서비스가 생성될 때 딱 한 번만 실행
        super.onCreate()
        startForegroundService()       // 2. 포그라운드 서비스 시작
    }
   /*
    3. 바인드
    bindService() 함수를 호출할 때 실행되는 함수로 여기서 서비스와 구성요소를 이어주는
    매개체 역할을 하는 IBinder를 반환합니다
    */
    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }
   /*
    4. 시작된 상태 & 백그라운드
    startService()나 startForegroundService()를 호출할 때 실행되는 콜백함수입니다. 이 함수가 실행되면
    서비스는 시작된 상태가 되고 백그라운드에서 쭉 존재할 수 있습니다.
    */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
       // 반드시 정수값을 반환해야 합니다. 이값은 서비스를 종료할 때 서비스를 어떻게 유지할지를 설명합니다.
        return START_STICKY
    }
   /*
    5. 서비스 종료
    생명주기의 마지막 단계로 onCreate()에서 상태 표시줄에 보여주었던 알림을 해제합니다.
    */
    override fun onDestroy() {
       super.onDestroy()
       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           stopForeground(true)
       }
    }
    /*
    6. 알림 채널을 만들고, startForeground()함수를 실행합니다. 안드로이드 O(AIP 26)버전부터 반드시
    startService()가 아닌 startForegroundService()를 실행하여 사용자로 하여금 서비스가 실행되고 있다는 사실을
    알림과 함께 알려야 합니다.
    */
    fun startForegroundService(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE)
                as NotificationManager
            val mChannel = NotificationChannel( // 알림 채널 생성
                "CHANNEL_ID",
                "CHANNEL_NAME",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(mChannel)
        }

        val notification: Notification = Notification.Builder(this, "CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_play)
            .setContentTitle("뮤직 플레이어 앱")
            .setContentText("앱이 실행 중입니다.")
            .build()

        startForeground(1, notification)
    }
    //  재생되고 있는지 확인
    fun isPlaying() : Boolean {
        return (mMediaPlayer != null && mMediaPlayer?.isPlaying ?: false)
    }
    // 1. 음악 재생
    fun play() {
        if(mMediaPlayer == null) {
            // 음악 파일의 리소스를 가져와 미디어 플레이어 객체를 할당
            mMediaPlayer = MediaPlayer.create(this, R.raw.chocolate)

            mMediaPlayer?.setVolume(1.0f, 1.0f); // 볼륨을 지정
            mMediaPlayer?.isLooping = true       // 반복재생 여부를 정해줍니다.
            mMediaPlayer?.start()                // 음악을 재생합니다.
        } else { // 2. 음악 재생 중인 경우
            if(mMediaPlayer!!.isPlaying) {
                Toast.makeText(this, "이미 음악이 실행 중입니다.",
                    Toast.LENGTH_SHORT).show()
            } else {
                mMediaPlayer?.start()	          // 음악을 재생합니다.
            }
        }
    }
    // 3. 일시 정지
    fun pause() {
        mMediaPlayer?.let {
            if(it.isPlaying) {
                it.pause()    // 음악을 일시정지 합니다.
            }
        }
    }
    // 4. 재생 중지
    fun stop() {
        mMediaPlayer?.let {
            if(it.isPlaying) {
                it.stop()     // 음악을 멈춥니다.
                it.release()  // 미디어 플레이어에 할당된 자원을 해제시켜줍니다.
                mMediaPlayer = null
            }
        }
    }
}