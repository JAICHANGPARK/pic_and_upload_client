package dreamwalker.com.mypictureclient.activitys

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dreamwalker.com.mypictureclient.R
import dreamwalker.com.mypictureclient.SharedData
import dreamwalker.com.mypictureclient.app_const.Constants
import dreamwalker.com.mypictureclient.services.SendAutoItemService

class SendAutoItemActivity : AppCompatActivity() {

    private val PERMISSION_CODE = 1000

    private var mSharedData = SharedData.instance

    private lateinit var mProgressIntent: Intent
    private lateinit var mLastDate: String
    private lateinit var mServerIP: String
    private var mIsNewFile = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_auto_item)
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), PERMISSION_CODE
            )
        } else {
            mSharedData.isConnected = false
            getFileInfo()
            Log.e(this@SendAutoItemActivity::class.java.name, "관한 허용되어있음")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //권한 허가
                    getFileInfo()
                } else {
                    val dialogBuilder = AlertDialog.Builder(this@SendAutoItemActivity)
                        .setTitle("Permission Error")
                        .setMessage(
                            "사진 정보를 읽기위해 권한에 동의해야 합니다. \n" +
                                    "애플리케이션을 종료합니다."
                        )
                        .setPositiveButton(
                            android.R.string.ok
                        ) { dialog, which ->

                            ActivityCompat.finishAffinity(this@SendAutoItemActivity)
                            System.exit(0)

                        }.setIcon(R.mipmap.ic_launcher)

                    dialogBuilder.create().show()
                }
            }
        }
    }

    private fun getFileInfo() {
        mServerIP = intent.getStringExtra(Constants.IP)
        mLastDate = intent.getStringExtra(Constants.DATE)
        getImageInfo()
        getVideoInfo()
    }

    private fun getImageInfo() {
        val images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val imagesProjection = arrayOf(MediaStore.Images.Media.DATE_TAKEN)
        Log.e(Constants.TAG, images.toString())
        Log.e(Constants.TAG, imagesProjection.toString())
        val imageCursor = contentResolver.query(
            images,
            imagesProjection,
            null,
            null,
            MediaStore.Images.Media.DATE_TAKEN + " desc"
        )
        val imageDateIndex = imageCursor.getColumnIndex(
            MediaStore.Images.Media.DATE_TAKEN
        )



        if (imageCursor != null && imageCursor.count > 0) {
            imageCursor.moveToFirst()
            val imageData = imageCursor.getString(imageDateIndex)
            Log.e(Constants.TAG, imageData)
            if (isValidDate(mLastDate, imageData)) {
                mIsNewFile = true
            }
        }
    }

    private fun getVideoInfo() {
        val videos = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val videosProjection = arrayOf(MediaStore.Video.Media.DATE_TAKEN)
        // 동영상 파일 불러오기
        val videoCursor =
            contentResolver.query(videos, videosProjection, null, null, MediaStore.Video.Media.DATE_TAKEN + " desc ")
        val videoDateIndex = videoCursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN)
        // 최신 동영상으로 이동
        if (videoCursor != null && videoCursor.count > 0) {
            videoCursor.moveToFirst()

            val videoDate = videoCursor.getString(videoDateIndex)
            // 최종 동기화 날짜보다 이전 날짜일 경우 중지
            if (isValidDate(mLastDate, videoDate)) {
                mIsNewFile = true
            }
        }
    }

    private fun isValidDate(lastDate: String, date: String): Boolean {
        return lastDate.compareTo(date) <= 0
    }

    private fun startSendService(){
        mSharedData.allModeSenderIntent.putExtra(Constants.LAST_DATE, mLastDate)
        mSharedData.allModeSenderIntent.putExtra(Constants.LAST_DATE, mLastDate)
        mSharedData.allModeSenderIntent.putExtra(Constants.SERVER_IP, mServerIP)
        mSharedData.allModeSenderIntent.setClass(this, SendAutoItemService::class.java)
        startService(mSharedData.allModeSenderIntent)
    }
}
