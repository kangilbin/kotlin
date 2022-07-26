package com.jacob.novelview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.jacob.novelview.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {
    // 뷰 바인딩 설정
    private lateinit var binding : ActivityMainBinding
    private var pfd: ParcelFileDescriptor? = null
    private var fileInputStream: FileInputStream? = null
    val REQ_OPEN_FILE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // [불러오기] 클릭
        binding.btnLoad.setOnClickListener {
            var intent = Intent(this, FileLoad::class.java)
            intent.putExtra("storage", "external")

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                openFile(Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)))
            } else {
                startActivity(intent)
            }
        }
        // [보관함] 클릭
        binding.btnStorage.setOnClickListener {
            var intent = Intent(this, FileLoad::class.java)
            intent.putExtra("storage", "interal")
            startActivity(intent)
        }
        setPermission()
    }
    fun openFile(pickerInitialUri: Uri) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"

           // putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        startActivityForResult(intent, REQ_OPEN_FILE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        when (requestCode) {
            REQ_OPEN_FILE -> if (requestCode == REQ_OPEN_FILE && resultCode == Activity.RESULT_OK) {
                resultData?.data?.also { uri ->
                    saveFile(uri)
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, resultData)
        }

    }
    fun saveFile(uri: Uri){
        val fileName = getFileName(uri)
        try {
            pfd = uri.let { applicationContext?.contentResolver?.openFileDescriptor(it, "r") }
            fileInputStream = FileInputStream(pfd?.fileDescriptor)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        var newFile: File? = null
        if(fileName!=null) {
            newFile = File(filesDir, fileName)
        }

        var inChannel: FileChannel? = null
        var outChannel: FileChannel? = null

        try {
            inChannel = fileInputStream?.channel
            outChannel = FileOutputStream(newFile).channel
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        try {
            inChannel?.transferTo(0, inChannel.size(), outChannel)
        } finally {
            inChannel?.close()
            outChannel?.close()
            fileInputStream?.close()
            pfd?.close()
        }
    }
    fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = applicationContext?.contentResolver?.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }
    /**
     * 테드 퍼미션 설정 (권한 설정)
     */
    private fun setPermission() {
        val permission = object : PermissionListener{
            //설정해 놓은 권한을 허용할 경우 수행
            override fun onPermissionGranted() {
                Toast.makeText(this@MainActivity,"권한이 허용 되었습니다.",Toast.LENGTH_SHORT).show()
            }
            // 거부시 수행
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Toast.makeText(this@MainActivity,"권한이 거부 되었습니다.",Toast.LENGTH_SHORT).show()
            }
        }
        // 읽고 쓰는 권한
        TedPermission.with(this)
            .setPermissionListener(permission)
            .setRationaleMessage("앱을 사용하시려면 권한을  허용해 주세요.")                      // 처음 안내 메세지
            .setDeniedMessage("권한을 거부하셨습니다. [앱 설정] -> [권한] 항목에서 허용해 주세요.") // 사용자가 거부 했을 경우 나타나는 안내 메세지
            .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE) //권한 적용
            .check() // 권한 체크
    }
}