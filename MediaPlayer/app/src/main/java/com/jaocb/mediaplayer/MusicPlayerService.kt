package com.jaocb.mediaplayer

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MusicPlayerService : Service() {
    override fun onCreate() {  // 1. 서비스가 생성될 때 딱 한 번만 실행
        super.onCreate()
        startForegroundService()       // 2. 포그라운드 서비스 시작
    }
    // 3. 바인드
    override fun onBind(intent: Intent?): IBinder? {
        TODO()
    }
    // 4. 시작된 상태 & 백그라운드
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
    // 5. 서비스 종료
    override fun onDestroy() {
        super.onDestroy()
    }

    fun startForegroundService(){}
    fun isPlaying() {}  // 재생 중인지 확인
    fun play() {}   // 재생
    fun pause() {}  // 일시정지
    fun stop() {}   // 완전 정지
}