package com.jacob.novelview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jacob.novelview.databinding.ActivityFileReadBinding
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class FileRead : AppCompatActivity() {
    private lateinit var binding : ActivityFileReadBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFileReadBinding.inflate(layoutInflater)
        val view = binding.root

        val file = File(intent.getStringExtra("path"))
        val reader = BufferedReader(FileReader(file))
        var temp:String? = ""
        val readTxt = StringBuffer()

        while (true) {
            temp = reader.readLine()
            if(temp == null) break
            else readTxt.append(temp).append("\n")
        }
        reader.close()

        binding.tvRead.text = readTxt.toString()

        setContentView(view)
    }

}