package mx.softel.cir_wireless_mx.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Firmware_Table")
data class TagsFirmwareList (
    @PrimaryKey val id : String,
    val fw             : String?
)