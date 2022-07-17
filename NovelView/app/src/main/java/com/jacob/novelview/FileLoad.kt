package com.jacob.novelview

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
import kotlinx.android.synthetic.main.activity_file_load.*
import java.io.File
import java.io.IOException
import java.io.InputStream

class FileLoad : AppCompatActivity(), LoadAdapter.ClickListener {
    private lateinit var binding : ActivityFileLoadBinding
    var list:ArrayList<LoadDTO> = ArrayList()
    val root = File(Environment.getExternalStorageDirectory().absolutePath)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileLoadBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val recyclerView =  binding.recyclerView
        val dividerItemDecoration =  DividerItemDecoration(this,LinearLayoutManager.VERTICAL)

        dividerItemDecoration.setDrawable(this.getResources().getDrawable(R.drawable.recyclerview_divider))
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = LoadAdapter(list,this)

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
    private fun saveFile(inputStream: InputStream) {
        val path = filesDir.absolutePath

        File(path).outputStream().use { fileOutput ->
            inputStream.copyTo(fileOutput)
        }
    }

    // 폴더 클릭 이벤트
    override fun onItemClick(loadDTO: LoadDTO) {
        var filePath = File(loadDTO.path)
        var files = filePath?.listFiles()
        try {
            if(filePath.isDirectory){
                list.clear();
                if(!filePath.equals(root)){
                    list.add(LoadDTO(R.drawable.ic_folder,"...",filePath.parent))
                }
                if(files != null){
                    for(childFile in files){
                        Log.d("파일","확장자 : " + childFile.extension)
                        if(childFile.isDirectory){
                            list.add(LoadDTO(R.drawable.ic_folder,childFile.name,childFile.absolutePath))
                        } else if (childFile.extension == "TXT") {
                            list.add(LoadDTO(R.drawable.ic_txt,childFile.name,childFile.absolutePath))
                        } else if(childFile.extension == "zip"){
                            list.add(LoadDTO(R.drawable.ic_zip,childFile.name,childFile.absolutePath))
                        }
                    }
                } else {
                    Toast.makeText(this, "파일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                }
                recyclerView.adapter = LoadAdapter(list, this)
            } else if (filePath.extension == "TXT") {
                saveFile(filePath.inputStream())
                Log.d("파일","TXT")
            } else if(filePath.extension == "zip") {
                saveFile(filePath.inputStream())
                Log.d("파일","zip")
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}