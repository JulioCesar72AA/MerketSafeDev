package mx.softel.cir_wireless_mx.dataBaseRoom.Dao

import androidx.room.*
import mx.softel.cir_wireless_mx.Entity.TagsFirmwareList

@Dao
interface TagFirmwareListDao {
    @Insert
    fun insertFirmwareList(vararg temporalFirmwareList: TagsFirmwareList)

    @Delete
    fun deleteFirmwareList(temporalFirmwareList: TagsFirmwareList)

    @Update
    fun updateFirmwareList(vararg temporalFirmwareList: TagsFirmwareList)

    @Query("SELECT * FROM firmware_table WHERE id = :id")
    fun findByIdFirmwareList(id: String): TagsFirmwareList

    @Query("SELECT * FROM firmware_table")
    fun getFirmwareList(): TagsFirmwareList
}