package com.jacob.novelview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.jacob.novelview.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    // 뷰 바인딩 설정
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // [불러오기] 클릭
        binding.btnLoad.setOnClickListener {
            var intent = Intent(this, FileLoad::class.java)
            intent.putExtra("storage", "external")
            startActivity(intent)
        }
        // [보관함] 클릭
        binding.btnStorage.setOnClickListener {
            var intent = Intent(this, FileLoad::class.java)
            intent.putExtra("storage", "interal")
            startActivity(intent)
        }

        setPermission()
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