package jp.techacademy.otowa.kimura.autoslideshowapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import jp.techacademy.otowa.kimura.autoslideshowapp.databinding.ActivityMainBinding
import java.util.*

//MainActivityの定義
//binding
//imageUriList　→　保存されてるURIの格納リスト
//currentIndex　→　現在保存している画像のインデックス保持
//timer
//isPlaying　→　スライドショーが再生中かどうかを管理するための
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val handler = Handler(Looper.getMainLooper())
    private var imageUriList: ArrayList<Uri> = ArrayList()
    private var currentIndex = 0
    private val PERMISSION_REQUEST_CODE = 100
    private var timer: Timer? = null
    private var isPlaying = false

//アプリの初期化をonCreateメソッド内で行う
    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    //外部ストレージの読み取りパーミッションが許可されるかどうかチェック
    //許可されない　→　パーミッションをリクエスト
    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        //PERMISSION_REQUEST_CODE　→　パーミッションリクエストコード
        requestPermissions(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
        //許可されている　→　getContentsInfo()呼び出し画像情報を取得
    } else {
        getContentsInfo()
    }

    // クリックリスナーの設定
    binding.btnNext.setOnClickListener {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED && imageUriList.size != 0) {
            showNextImage()
        }
    }
    binding.btnReturn.setOnClickListener {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED && imageUriList.size != 0) {
            showReturnImage()
        }
    }
    binding.btnPlayPause.setOnClickListener {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED && imageUriList.size != 0) {
            if (isPlaying) {
                stopSlideshow()
            } else {
                startSlideshow()
            }
        }
    }
}
    //画像の取得
    private fun getContentsInfo() {
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )
//カーソルを閉じる
        cursor?.use {
            while (cursor.moveToNext()) {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val id = cursor.getLong(idColumn)
                val contentUri: Uri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )
                imageUriList.add(contentUri)
            }

        }

        cursor?.close()

        if (imageUriList.isNotEmpty()) {
            displayCurrentImage()
        }
    }

//現在の画像を表示
    private fun displayCurrentImage() {
        binding.imageView.setImageURI(imageUriList[currentIndex])
    }
//次の画像表示
    private fun showNextImage() {
        currentIndex = (currentIndex + 1) % imageUriList.size
        displayCurrentImage()
    }
//前の画像表示
    private fun showReturnImage() {
        currentIndex = (currentIndex -1 + imageUriList.size) % imageUriList.size
    displayCurrentImage()
    }
//スライドショーの開始
    private fun startSlideshow() {
        binding.btnPlayPause.text = "停止"
        isPlaying = true
        binding.btnNext.isEnabled = false
        binding.btnReturn.isEnabled = false

        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                handler.post {
                    showNextImage()
                }
            }
        }, 2000,2000)
    }
//スライドショーの停止
    private fun stopSlideshow() {
        binding.btnPlayPause.text = "再生"
        isPlaying = false
        binding.btnNext.isEnabled = true
        binding.btnReturn.isEnabled = true

        timer?.cancel()
        timer = null
    }
//パーミッションリクエスト結果の処理(lesson5.8.3)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getContentsInfo()
        }
    }
}
