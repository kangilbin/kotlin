package com.jacob.novelview

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.jacob.novelview.DTO.LoadDTO
import com.jacob.novelview.adapter.LoadAdapter
import com.jacob.novelview.databinding.ActivityFileLoadBinding
import com.jacob.novelview.db.FileDAO
import com.jacob.novelview.db.FileDatabase
import com.jacob.novelview.db.FileEntity
import kotlinx.android.synthetic.main.activity_file_load.*
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class FileLoad : AppCompatActivity(), LoadAdapter.ClickListener {
    lateinit var binding : ActivityFileLoadBinding
    lateinit var root : File
    lateinit var type : String
    lateinit var db : FileDatabase
    lateinit var fileDao : FileDAO
    lateinit var fileList: ArrayList<FileEntity>

    var list:ArrayList<LoadDTO> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileLoadBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        db = FileDatabase.getInstance(this)!!
        fileDao = db.getFileoDAO()

        type = intent.getStringExtra("storage").toString()
        if(type == "external"){
            root = File(Environment.getExternalStorageDirectory().absolutePath)
        } else {
            root = File(filesDir.absolutePath)
        }

//        이상하게 리사이클러뷰 구분선이 액티비티에 커스텀 테마를 적용하게되면 적용 안됨
//        val dividerItemDecoration =  DividerItemDecoration(this,LinearLayoutManager.VERTICAL)
//        dividerItemDecoration.setDrawable(this.getResources().getDrawable(R.drawable.recyclerview_divider))
//        recyclerView.addItemDecoration(dividerItemDecoration);
        val recyclerView =  binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = LoadAdapter(list,this, type)

        buildDisplayData()
    }

    private fun buildDisplayData(){
        var files = root?.listFiles()

        if(files == null){
            list.add(LoadDTO(R.drawable.ic_folder,"...",root?.parent))
            Toast.makeText(this, "해당 경로에 파일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
            return;
        }
        for(childFile in files){
            if(childFile.isDirectory){
                list.add(LoadDTO(R.drawable.ic_folder,childFile.name,childFile.absolutePath))
            } else {
                list.add(LoadDTO(R.drawable.ic_txt,childFile.name,childFile.absolutePath))
            }
        }
    }
    // 외부 저장소 -> 내부 저장소로 복사
    private fun saveFile(file:File) {
//        this.openFileOutput(file.name, Context.MODE_PRIVATE).use {
//            it.write(file.readBytes())
//        }
//        fileRead(File(filesDir.absoluteFile, file.name))

        Thread {
            val reader = BufferedReader(FileReader(file))
            var temp:String? = ""
            val readTxt = StringBuffer()

            while (true) {
                temp = reader.readLine()
                if(temp == null) break
                else readTxt.append(temp).append("\n")
            }
            reader.close()

            fileDao.insert(FileEntity(null, file.name, readTxt.toString(), readTxt.lines().count(), 0))

            runOnUiThread {
                // UI 스레드에서 실행
                Toast.makeText(this, "추가 되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.start()
    }
    // 파일 읽기 페이지 이동
    private fun fileRead(file:File) {
        var intent = Intent(this, FileRead::class.java)
        intent.putExtra("path", file.path)
        startActivity(intent)
        finish()
    }
    // 폴더 클릭 이벤트
    override fun onItemClick(loadDTO: LoadDTO) {
        var filePath = File(loadDTO.path)
        var files = filePath?.listFiles()
        try {
            if(type == "external") {
                if(filePath.isDirectory){
                    list.clear();
                    if(!filePath.equals(root)){
                        list.add(LoadDTO(R.drawable.ic_folder,"...",filePath.parent))
                    }
                    if(files != null){
                        for(childFile in files){
                            if(childFile.isDirectory){
                                list.add(LoadDTO(R.drawable.ic_folder,childFile.name,childFile.absolutePath))
                            } else if (childFile.extension.toLowerCase() == "txt") {
                                list.add(LoadDTO(R.drawable.ic_txt,childFile.name,childFile.absolutePath))
                            } else if(childFile.extension.toLowerCase() == "zip"){
                                list.add(LoadDTO(R.drawable.ic_zip,childFile.name,childFile.absolutePath))
                            }
                        }
                    } else {
                        Toast.makeText(this, "파일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                    recyclerView.adapter = LoadAdapter(list, this, type)
                } else if (filePath.extension.toLowerCase() == "txt" || filePath.extension.toLowerCase() == "zip") {
                    saveFile(filePath)
                }
            } else {
                fileRead(filePath)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}