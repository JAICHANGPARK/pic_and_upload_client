package dreamwalker.com.mypictureclient

import android.content.Intent
import dreamwalker.com.mypictureclient.model.SelectedImageData
import java.util.ArrayList

class SharedData {

    val allModeSenderIntent = Intent()
    val allModeProgressServiceIntent = Intent()
    var isConnected: Boolean = false
    var threadCount: Int = 0

    val selectedModeSenderIntent = Intent()
    val selectedModeProgressServiceIntent = Intent()
    var allModeFileCount: Int = 0
    var allModeTotalFileCount: Int = 0

    var selectedModeFileCount: Int = 0
    var selectedModeTotalFileCount: Int = 0

    var selectedImageList = ArrayList<SelectedImageData>()

    companion object {
        val instance = SharedData()
    }
}


