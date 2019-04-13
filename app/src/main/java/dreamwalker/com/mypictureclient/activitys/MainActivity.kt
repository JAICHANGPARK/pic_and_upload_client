package dreamwalker.com.mypictureclient.activitys

import android.content.Context
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import androidx.core.content.getSystemService
import dreamwalker.com.mypictureclient.R
import dreamwalker.com.mypictureclient.SharedData
import dreamwalker.com.mypictureclient.app_const.Constants
import java.net.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mPartialAp = arrayOfNulls<String>(3)
    private val mTimeout = 100
    private var mIpAddress: String? = null
    internal var mSharedData = SharedData.instant

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mIpAddress ?: setIP()
        mSharedData.isConnected = false
        ProcessTask(1, 50).start()
        ProcessTask(50, 100).start()
        ProcessTask(100, 150).start()
        ProcessTask(150, 200).start()
        ProcessTask(200, 256).start()


    }

    private inner class ProcessTask(var start: Int, var end: Int) : Thread() {

        override fun run() {

            try {
                var hostname: String? = null
                var connectSocket: Socket? = null
                var socketAddress: SocketAddress
                for (i in start until end) {

                    if (mSharedData.isConnected) {
                        break
                    }
                    try {
                        hostname = mPartialAp[0] + "." + mPartialAp[1] + "." + mPartialAp[2] + "."
                        +i
                        socketAddress = InetSocketAddress(hostname, Constants.CONNECT_PORT)
                        Log.e(Constants.TAG, hostname + " : 서버 연결 시도 ...")
                        try {
                            connectSocket = Socket()
                            connectSocket.connect(socketAddress, mTimeout)
                        } catch (e: SocketTimeoutException) {
                            continue
                        }

                        Log.e(Constants.TAG, hostname + " : 서버 응답 확인 !!")

                    } catch (e: ConnectException) {
                        continue
                    }

                }
            } catch (e: Exception) {
            }
        }
    }

    private fun setIP() {
        val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wm.connectionInfo
        val ip = wifiInfo.ipAddress
        Log.e(this@MainActivity::class.java.name, ip.toString())

        mIpAddress = Formatter.formatIpAddress(ip)
        val st = StringTokenizer(mIpAddress, ".")

        for(i in mPartialAp.indices){
            mPartialAp[i] = st.nextToken()
        }

        Log.e(Constants.TAG, mIpAddress) // phone ip
        Log.e(Constants.TAG, mPartialAp[0] + "." + mPartialAp[1] + "." + mPartialAp[2]) // ap ip


    }


}




