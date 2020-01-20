package mx.softel.scanblelib.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.HashMap

class BeaconsDatabaseHelper(ctx: Context,
                            factory: SQLiteDatabase.CursorFactory?)
    : SQLiteOpenHelper(ctx, Catalog.DATABASE_NAME, factory, Catalog.DATABASE_VERSION) {

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(db: SQLiteDatabase?) {
        db?.apply {
            execSQL(Catalog.DATABASE_CREATE_TABLE_BEACONS)
            execSQL(Catalog.DATABASE_INSERT_DEFAULT_BEACONS)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(Catalog.DATABASE_DROP_TABLE_MACS_CONECTION_TABLE)
        onCreate(db)
    }


    /************************************************************************************************/
    /**     GETTERS                                                                                 */
    /************************************************************************************************/
    fun getBeaconTypes(): HashMap<String, String> {
        val beaconTypes = HashMap<String, String>()
        val db = writableDatabase
        val cursor = db.rawQuery(Catalog.DATABASE_SELECT_BEACON_TYPES, null)

        if (cursor != null && cursor.count > 0 && cursor.moveToFirst()) {
            do {
                beaconTypes[cursor.getString(cursor.getColumnIndex(Catalog.BEACON_TYPE))] =
                cursor.getInt(cursor.getColumnIndex(Catalog.BEACON_ENCRYPTION)).toString() +
                "|" + cursor.getString(cursor.getColumnIndex(Catalog.BEACON_TYPE))
            } while (cursor.moveToNext())
            cursor.close()
        }
        return beaconTypes
    }


    /************************************************************************************************/
    /**     CONPANION OBJECT                                                                         */
    /************************************************************************************************/
    companion object {
        private val TAG = BeaconsDatabaseHelper::class.java.simpleName
    }

}