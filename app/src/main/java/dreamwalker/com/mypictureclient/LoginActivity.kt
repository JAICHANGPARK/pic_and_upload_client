package dreamwalker.com.mypictureclient

import android.content.Context
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.getSystemService

class LoginActivity : AppCompatActivity() {
    private lateinit var mWifiManager: WifiManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mWifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mWifiManager.isWifiEnabled = true


    }
    private inner class WifiStateThread : Thread(){
        override fun run() {
            val start = System.currentTimeMillis()
            try {
                Thread.sleep(1500)
                while (true){
                    if(mWifiManager.connectionInfo.rssi > -70) break
                    else{
                        //연결 시도

                    }

                }
            }
        }
    }
}
