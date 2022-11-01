package mx.softel.cirwireless

import android.util.Log

private const val TAG = "UserPermissions"

class UserPermissions (val permissions: String) : java.io.Serializable {
    var isLinker    = false
    var isAdmin     = false
    var isCommander = false
    var isViewer    = false

    init {
        val permissionsArray = permissions.split(",")
        permissionsArray.forEach {
            when (it) {
                "admin" -> isAdmin          = true
                "linker" -> isLinker        = true
                "commander" -> isCommander  = true
                "viewer" -> isViewer        = true
            }
        }
    }
}