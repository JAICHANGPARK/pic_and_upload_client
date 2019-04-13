package dreamwalker.com.mypictureclient.activitys

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
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
    internal var mSharedData = SharedData.instance


    private var mDialog: AlertDialog? = null
    internal var mSearchDialogHandler: SearchDialogHandler? = null
    private var mSelectedModeIndex: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mIpAddress ?: setIp()
        mSharedData.isConnected = false
        mSearchDialogHandler = SearchDialogHandler()

        ProcessTask(1, 50).start()
        ProcessTask(50, 100).start()
        ProcessTask(100, 150).start()
        ProcessTask(150, 200).start()
        ProcessTask(200, 256).start()
        mSearchDialogHandler?.sendEmptyMessage(0)

    }

    override fun onResume() {
        super.onResume()
        mIpAddress?: setIp()
        if (!mSharedData.isConnected) {
            mPCs.remove(mServerIP)
        }
        setUi()
        SharedData.instance.allModeFileCount = 0
        SharedData.instance.allModeTotalFileCount = 0
        SharedData.instance.selectedModeFileCount = 0
        SharedData.instance.selectedModeTotalFileCount = 0
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
                    mSearchDialogHandler?.sendEmptyMessage(1)
                    if (mSharedData.isConnected) {
                        connect_state.text = "PC를 선택해 주세요"
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            synchronized(this) {
                mSharedData.threadCount = mSharedData.threadCount + 1
                if (mSharedData.threadCount == 5) {
                    mSearchDialogHandler?.sendEmptyMessage(1)
                    mSharedData.threadCount = 0
                }
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

    private fun setUi() {
        connect_state.text = "PC와 연결이 필요합니다."
        select_pc.setOnClickListener { selectPc() }

        select_picture.setOnClickListener { selectMode() }
        select_picture.setTextColor(Color.GRAY)
        select_picture.isClickable = false

        search_pc.setOnClickListener {
            mSharedData.isConnected = false
            mSharedData.threadCount = 0
            ProcessTask(1, 50).start()
            ProcessTask(50, 100).start()
            ProcessTask(100, 150).start()
            ProcessTask(150, 200).start()
            ProcessTask(200, 256).start()
            mSearchDialogHandler?.sendEmptyMessage(0)
        }

        @Suppress("DEPRECATION")
        mDialog = ProgressDialog(this@MainActivity, R.style.CustomDialog)
        mDialog?.setMessage("Server PC 검색중")
        mDialog?.setCancelable(false)
    }
    private fun selectPc() {
        val pcList = mPCs.toTypedArray<CharSequence>()
        val alt_bld = AlertDialog.Builder(this)
        alt_bld.setTitle("PC를 선택해주세요.")
        alt_bld.setSingleChoiceItems(
            pcList, -1
        ) { dialog, item ->
            connect_state.text = "연결 완료!!"
            select_picture.isClickable = true
            select_picture.setTextColor(Color.BLACK)
            mServerIP = pcList[item].toString()
            dialog.dismiss()
        }
        val alert = alt_bld.create()
        alert.show()
    }

    private fun selectMode() {
        val modes = arrayOf<CharSequence>("자동", "선택")
        val alt_bld = AlertDialog.Builder(this)
        alt_bld.setTitle("전송할 방법을 선택해주세요.")
        alt_bld.setSingleChoiceItems(modes, 0,
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, item: Int) {
                    mSelectedModeIndex = item
                }
            }).setPositiveButton("확인",
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    if (mSelectedModeIndex == 0) {
                        val intent = Intent()
                        intent.putExtra(Constants.IP, mServerIP)
                        intent.putExtra(Constants.DATE, mSyncDate)
                        intent.setClass(applicationContext, SendAutoItemActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent()
                        intent.putExtra(Constants.IP, mServerIP)
//                        intent.setClass(applicationContext, SendSelectedItemActivity::class.java)
//                        startActivity(intent)
                    }
                }
            })
        val alert = alt_bld.create()
        alert.show()
    }

    internal inner class SearchDialogHandler : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == 0) {
                mDialog?.show()
            } else if (msg.what == 1) {
                mDialog?.let {
                    if (it.isShowing ?: true) {
                        it.dismiss()
                    }
                }
            }
        }

    }
}







