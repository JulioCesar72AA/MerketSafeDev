package mx.softel.cir_wireless_mx.dataBaseRoom

import android.content.Context
import android.os.AsyncTask
import mx.softel.cir_wireless_mx.Entity.TagsFirmwareList
import mx.softel.cir_wireless_mx.dataBaseRoom.Dao.*


class UserRepository(context: Context) {

    var db_firmwareList: TagFirmwareListDao = TagFirmwareListDatabase.getInstance(context)?.firmwareListDao()!!

    fun getFirmwareList(): TagsFirmwareList {
        return db_firmwareList.getFirmwareList()
    }

    fun getFirmwareListWithId(setId: String): TagsFirmwareList {
        return db_firmwareList.findByIdFirmwareList(setId)
    }

    // Insert new user
    fun insertFirmwareList(temAlarmsFaults: TagsFirmwareList) {
        insertAsyncTaskFirmwareList(db_firmwareList).execute(temAlarmsFaults)
    }

    // update user
    fun updateFirmwareList(temHomeTemplates: TagsFirmwareList) {
        db_firmwareList.updateFirmwareList(temHomeTemplates)
    }

    // Delete user
    fun deleteFirmware(firmwareList: TagsFirmwareList) {
        //for (fw in firmwareList) {
            db_firmwareList.deleteFirmwareList(firmwareList)
        //}
    }
    private class insertAsyncTaskFirmwareList internal constructor(private val tagFirmwareListDao: TagFirmwareListDao ) :
        AsyncTask<TagsFirmwareList, Void, Void>() {

        override fun doInBackground(vararg params: TagsFirmwareList): Void? {
            tagFirmwareListDao.insertFirmwareList(params[0])
            return null
        }
    }
}
