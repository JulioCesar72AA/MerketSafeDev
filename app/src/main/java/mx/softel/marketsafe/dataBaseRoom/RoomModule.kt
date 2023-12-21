package mx.softel.cir_wireless_mx.dataBaseRoom

import android.content.Context
import android.os.AsyncTask
import mx.softel.cir_wireless_mx.Entity.TagsFirmwareList
import mx.softel.cir_wireless_mx.dataBaseRoom.Dao.*


class SupportedFirmwaresRepository(context: Context) {

    var db_firmwareList     : TagFirmwareListDao        = SupportedFirmwareDao.getInstance(context)?.firmwareListDao()!!
    var dbSupportedFirmware : SupportedFirmwareDao      = SupportedFirmwareDao.getInstance(context)!!

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

    // Delete firmware table
    fun dropFirmwareTableList () {
        dbSupportedFirmware.clearAllData()
    }

    private class insertAsyncTaskFirmwareList internal constructor(private val tagFirmwareListDao: TagFirmwareListDao ) :
        AsyncTask<TagsFirmwareList, Void, Void>() {

        override fun doInBackground(vararg params: TagsFirmwareList): Void? {
            tagFirmwareListDao.insertFirmwareList(params[0])
            return null
        }
    }
}
