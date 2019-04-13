package dreamwalker.com.mypictureclient

import android.content.Intent

class SharedData {

    val allModeSenderIntent = Intent()
    val allModeProgressServiceIntent = Intent()
    var isConnected : Boolean = false
    val selectedModeSenderIntent = Intent()
    val selectedModeProgressServiceIntent = Intent()
    var allModeFileCount : Int = 0
    var allModeTotalFileCount : Int = 0

    var selectedModeFileCount : Int = 0
    var selectedModeTotalFileCount : Int = 0

    companion object{
        val instant = SharedData()
    }
}


