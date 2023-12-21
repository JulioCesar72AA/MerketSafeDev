package mx.softel.cir_wireless_mx.dataBaseRoom

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.softel.cir_wireless_mx.Entity.TagsFirmwareList
import mx.softel.cir_wireless_mx.dataBaseRoom.Dao.*

@Database(entities = [TagsFirmwareList::class], version = 2)
abstract class SupportedFirmwareDao : RoomDatabase() {
    abstract fun firmwareListDao(): TagFirmwareListDao

    companion object {
        private val DATABASE_NAME = "firmware_list.db"

        private var INSTANCE: SupportedFirmwareDao? = null

        private lateinit var context: Context

        fun getInstance(context: Context): SupportedFirmwareDao? {
            this.context = context

            if (INSTANCE == null) {
                synchronized(SupportedFirmwareDao::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        SupportedFirmwareDao::class.java, DATABASE_NAME).allowMainThreadQueries()
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

    fun clearAllData() {
        // Lanza la operación en un hilo de fondo
        CoroutineScope(Dispatchers.IO).launch {
            INSTANCE?.clearAllTables()
        }
    }

    fun recreateDatabase() {
        // Lanza la operación en un hilo de fondo
        CoroutineScope(Dispatchers.IO).launch {
            INSTANCE?.close()
            context.deleteDatabase(DATABASE_NAME)
            INSTANCE = null
        }
    }
}