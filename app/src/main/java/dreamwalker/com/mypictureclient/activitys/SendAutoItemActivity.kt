package dreamwalker.com.mypictureclient.activitys

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dreamwalker.com.mypictureclient.R

class SendAutoItemActivity : AppCompatActivity() {

    private val PERMISSION_CODE = 1000


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
            Log.e(this@SendAutoItemActivity::class.java.name, "관한 허용되어있음")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //권한 허가
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
                            {
                                ActivityCompat.finishAffinity(this@SendAutoItemActivity)
                                System.exit(0)
                            }
                        }.setIcon(R.mipmap.ic_launcher)

                    dialogBuilder.create().show()
                }
            }
        }
    }
}
