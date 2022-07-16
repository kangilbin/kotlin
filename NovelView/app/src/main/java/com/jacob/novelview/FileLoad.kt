package com.jacob.novelview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.jacob.novelview.DTO.LoadDTO
import com.jacob.novelview.adapter.LoadAdapter
import com.jacob.novelview.databinding.ActivityFileLoadBinding
import kotlinx.android.synthetic.main.activity_file_load.*
import java.io.File

class FileLoad : AppCompatActivity(), LoadAdapter.ClickListener {
    private lateinit var binding : ActivityFileLoadBinding
    var list:ArrayList<LoadDTO> = ArrayList()
    var root = File(Environment.getExternalStorageDirectory().absolutePath)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileLoadBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val recyclerView =  binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = LoadAdapter(list,this)

        buildDisplayData()
    }

    // 최초 파일 그리기
    private fun buildDisplayData(){
        var files = root?.listFiles()

        if(files == null){
            list.add(LoadDTO(R.drawable.ic_folder,"...",root?.parent))
            Toast.makeText(this, "해당 경로가에 파일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
            return;
        }
        for(childFile in files){
            if(childFile.isDirectory){
                list.add(LoadDTO(R.drawable.ic_folder,childFile.name,childFile.absolutePath))
            } else {
                list.add(LoadDTO(R.drawable.ic_text,childFile.name,childFile.absolutePath))
            }
        }
    }
    
    // 폴더 클릭 이벤트
    override fun onItemClick(loadDTO: LoadDTO) {
        var filePath = File(loadDTO.path)
        var files = filePath?.listFiles()

        if(filePath.isDirectory){
            list.clear();
            if(!filePath.equals(root)){
                list.add(LoadDTO(R.drawable.ic_folder,"...",filePath.parent))
            }
            if(files != null){
                for(childFile in files){
                    if(childFile.isDirectory){
                        list.add(LoadDTO(R.drawable.ic_folder,childFile.name,childFile.absolutePath))
                    } else {
                        list.add(LoadDTO(R.drawable.ic_text,childFile.name,childFile.absolutePath))
                    }
                }
            } else {
                Toast.makeText(this, "파일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
            }
            recyclerView.adapter = LoadAdapter(list, this)
        }
    }
}