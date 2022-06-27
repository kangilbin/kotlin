package com.jaocb.qrcodereader

interface OnDetectListener {
    // QRCodeAnalyzer에서 QR코드가 인식되었을 때 호출할 함수로 데이터 내용을 인수로 갖는다.
    fun onDetect(msg : String)
}