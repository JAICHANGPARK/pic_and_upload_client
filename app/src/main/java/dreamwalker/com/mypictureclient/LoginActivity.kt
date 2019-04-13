package dreamwalker.com.mypictureclient

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Process
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class LoginActivity : AppCompatActivity() {
    private lateinit var mWifiManager: WifiManager
    private lateinit var mWifiTextHandler : WifiStateHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mWifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mWifiManager.isWifiEnabled = true
        mWifiTextHandler = WifiStateHandler()



    }

    private inner class WifiStateThread : Thread() {
        override fun run() {
            val start = System.currentTimeMillis()
            try {
                mWifiTextHandler.sendEmptyMessage(0)
                Thread.sleep(1500)
                while (true) {
                    if (mWifiManager.connectionInfo.rssi > -70) {
                        mWifiTextHandler.sendEmptyMessage(1)
                        break
                    }
                    else {
                        //연결 시도
                        mWifiTextHandler.sendEmptyMessage(0)
                    }
                    if (System.currentTimeMillis() - start >= 10000) {
                        break
                        //연결 실패
                        mWifiTextHandler.sendEmptyMessage(2)
                    }
                    Thread.sleep(100)
                }
            } catch (e: Exception) {
                print(e)
            }
        }
    }

    private inner class WifiStateHandler : Handler() {
        override fun handleMessage(msg: Message?) {
            if (msg?.what == 0) {
                if (wifi_state_info.text.isEmpty() || wifi_state_info.text.length >= 15) {
                    wifi_state_info.text = "Wi-Fi 연결 시도 중"
                } else {
                    wifi_state_info.append(".")
                }
            } else if (msg?.what == 1) {
                wifi_state_info.text = "Wi-Fi 연결 완료"
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                wifi_state_info.text ="와이파이 연결 실패"
                val dialogBuilder  = AlertDialog.Builder(this@LoginActivity)
                    .setTitle("Wi-Fi Error")
                    .setMessage("연결할 수 있는 Wi-Fi가 없습니다.")
                    .setPositiveButton(android.R.string.ok){
                        dialog, which -> Process.killProcess(Process.myPid())
                    }
                    .setIcon(R.mipmap.ic_launcher)

                val dig = dialogBuilder.create()
                dig.show()

            }
        }
    }
}
