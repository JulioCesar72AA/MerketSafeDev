package mx.softel.cir_wireless_mx.dataBaseRoom

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mx.softel.cir_wireless_mx.Entity.TagsFirmwareList
import mx.softel.cir_wireless_mx.dataBaseRoom.Dao.*

@Database(entities = [TagsFirmwareList::class], version = 2)
abstract class TagFirmwareListDatabase : RoomDatabase() {
    abstract fun firmwareListDao(): TagFirmwareListDao

    companion object {
        private var INSTANCE: TagFirmwareListDatabase? = null

        fun getInstance(context: Context): TagFirmwareListDatabase? {
            if (INSTANCE == null) {
                synchronized(TagFirmwareListDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        TagFirmwareListDatabase::class.java, "firmware_list.db").allowMainThreadQueries()
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
    private fun checkCorrectFirmware(fware: String, fwDb: TagsFirmwareList): Boolean {
        if (fwDb.id == fwDb.fw) {
            return true
        }
        return false
    }
}