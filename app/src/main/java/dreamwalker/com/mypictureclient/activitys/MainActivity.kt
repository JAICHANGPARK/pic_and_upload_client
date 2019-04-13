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
import kotlinx.android.synthetic.main.activity_main.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mPartialAp = arrayOfNulls<String>(3)
    private val mTimeout = 100
    private var mIpAddress: String? = null

    private var mServerIP: String = ""
    private val mPCs = ArrayList<String>()
    private var mSyncDate: String? = null
    internal var mSharedData = SharedData.instant

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mIpAddress ?: setIp()
        mSharedData.isConnected = false
        ProcessTask(1, 50).start()
        ProcessTask(50, 100).start()
        ProcessTask(100, 150).start()
        ProcessTask(150, 200).start()
        ProcessTask(200, 256).start()


    }

    private inner class ProcessTask(var start: Int, var end: Int) : Thread() {

        override fun run() {
            var serverSocket: ServerSocket? = null
            try {
                var hostname: String? = null
                var connectSocket: Socket? = null

                var serverDos: DataOutputStream? = null
                var receiveSocket: Socket? = null
                var socketAddress: SocketAddress
                for (i in start until end) {

                    if (mSharedData.isConnected) break

                    try {
                        hostname = mPartialAp[0] + "." + mPartialAp[1] + "." + mPartialAp[2] + "." + i
                        Log.e(Constants.TAG, hostname)
                        socketAddress = InetSocketAddress(hostname, Constants.CONNECT_PORT)
                        Log.e(Constants.TAG, hostname + " : 서버 연결 시도 ...")
                        try {
                            connectSocket = Socket()
                            connectSocket?.connect(socketAddress, mTimeout)
                        } catch (e: SocketTimeoutException) {
                            continue
                        }

                        Log.e(Constants.TAG, hostname + " : 서버 응답 확인 !!")

                    } catch (e: ConnectException) {
                        continue
                    }

                    serverDos = DataOutputStream(connectSocket?.getOutputStream())
                    serverDos.writeUTF(mIpAddress)
                    serverDos.flush()

                    connectSocket?.close()

                    serverSocket = ServerSocket(Constants.FILE_SEND_PORT)
                    receiveSocket = serverSocket.accept()
                    Log.e(Constants.TAG, "서버 요청 확인")
                    mSharedData.isConnected = true

                    val dis: DataInputStream = DataInputStream(receiveSocket.getInputStream())
                    mServerIP = hostname
                    if (!mPCs.contains(mServerIP)) {
                        mPCs.add(mServerIP)
                    }
                    Log.e(Constants.TAG, "서버 주소 설정 완료")
                    mSyncDate = dis.readUTF()
                    Log.e(Constants.TAG, "동기화 날짜 수신 완료 ")
                    dis.close()
                    receiveSocket.close()
                    serverSocket.close()

                    if(mSharedData.isConnected){
                        connect_state.text ="PC를 선택해 주세요"
                    }

                }
            } catch (e: Exception) {
            }
        }
    }

    private fun setIp() {
        val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val wifiInfo = wm?.connectionInfo
        val ip = wifiInfo.ipAddress
        mIpAddress = Formatter.formatIpAddress(ip)
        val st = StringTokenizer(mIpAddress, ".")

        for (i in mPartialAp.indices) {
            mPartialAp[i] = st.nextToken()
        }

        Log.d(Constants.TAG, mIpAddress) // phone ip
        Log.d(Constants.TAG, mPartialAp[0] + "." + mPartialAp[1] + "." + mPartialAp[2]) // ap ip
    }


}




