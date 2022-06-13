package com.jaocb.stopwatch

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import java.util.*
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity(), View.OnClickListener {
    var isRunning = false
    private lateinit var btn_start: Button
    private lateinit var btn_refresh: Button
    private lateinit var tv_millisecond: TextView
    private lateinit var tv_second: TextView
    private lateinit var tv_minute: TextView
    var timer : Timer? = null
    var time = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 뷰 가져오기
        btn_start = findViewById(R.id.btn_start)
        btn_refresh = findViewById(R.id.btn_refresh)
        tv_millisecond = findViewById(R.id.tv_millisecond)
        tv_second = findViewById(R.id.tv_second)
        tv_minute = findViewById(R.id.tv_minute)

        // 버튼별 클릭 이벤트 등록
        btn_start.setOnClickListener(this)
        btn_refresh.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_start -> {
                if(isRunning){
                    pause()
                } else {
                    start()
                }
            }
            R.id.btn_refresh -> {
                refresh()
            }
        }
    }
    // 시작
    private fun start(){
        btn_start.text = "일시정지"
        btn_start.setBackgroundColor(getColor(R.color.red))
        isRunning = true

        // 스톱워치를 시작하는 로직
        timer = timer(period = 10) {
            /*
                timer() 코틀린에서 제공하는 함수로 일정한 주기를 방복하는 동작을 수행
                timer()의 {} 안에 쓰인 코드들은 백그라운드 스레드에서 실행
            */
            time++ // 10밀리초 단위 타이머

            // 시간 계산
            val milli_second = time % 100
            val second = (time % 6000) / 100
            val minute = time / 6000

            // 뷰의 접근은  뷰를 생성한 메인 스레드에서만 할 수 있다
            runOnUiThread{          // UI 스레드 생성(메인 스레드)
                if(isRunning) {     // UI 업데이트 조건 설정
                    // 밀리초
                    tv_millisecond.text =
                        if(milli_second < 10) ".0${milli_second}" else ".${milli_second}"
                    // 초
                    tv_second.text = if(second < 10) ".0${second}" else ".${second}"
                    // 분
                    tv_minute.text = "${minute}"
                }
            }
        }
    }
    // 일시정지
    private fun pause(){
        btn_start.text = "시작"
        btn_start.setBackgroundColor(getColor(R.color.blue))

        isRunning = false
        timer?.cancel()     // 타이머 멈추기
    }
    private fun refresh(){
        timer?.cancel() // 백그라운 스레드에서 실행중인 타이머를 멈춤

        btn_start.text = "시작"
        btn_start.setBackgroundColor(getColor(R.color.black))
        isRunning = false

        // 타이머 초기화
        time = 0
        tv_millisecond.text = ".00"
        tv_second.text = ":00"
        tv_minute.text = "00"
    }
}