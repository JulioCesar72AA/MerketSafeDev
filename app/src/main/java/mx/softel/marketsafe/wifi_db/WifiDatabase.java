package mx.softel.marketsafe.wifi_db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

import mx.softel.marketsafe.web_services_module.SolkosServerResponse;


public class WifiDatabase extends SQLiteOpenHelper {

    private static final String TAG = "BootloaderDatabase";

    private Context mContext;

    private static final String DATABASE_NAME = "nfc_db";

    private static int DATA_BASE_VERSION = 3;
    private static int STATIC_MEMORY_SECTION = 1;

    public WifiDatabase(@NonNull Context mContext) {
        super(mContext, DATABASE_NAME, null, DATA_BASE_VERSION);
        this.mContext = mContext;
    }

    @Override
    public void onCreate(SQLiteDatabase db) { db.execSQL(Queries.CREATE_USER_TABLE);}


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        Log.e(TAG, "onUpgrade: ");
        db.execSQL(Queries.DROP_USER_TABLE);
        onCreate(db);
    }


    /***********************************************************************************************
     *                                        INSERT QUERIES                                       *
     ***********************************************************************************************/
    public boolean insertMacSmartphone (String macSmartphone) {
        SQLiteDatabase mDatabase = this.getWritableDatabase();

        try {
            String insertQuery = String.format(Queries.INSERT_CELLPHONE_ID,
                    macSmartphone);

            // Log.e(TAG, "insertTemplate: " + mInsertQuery);

            mDatabase.execSQL(insertQuery);

        } catch (Exception e) {
            e.printStackTrace();
        }

        mDatabase.close();
        return true;
    }


    public void insertUser (UserModel user, SolkosServerResponse userAccessInfo, IDBWritable idbWritable) {
        SQLiteDatabase mDatabase = this.getWritableDatabase();
        boolean wasSaved = false;

        try {
            String insertQuery = String.format(Queries.INSERT_USER_DATA,
                    user.getCode(),
                    userAccessInfo.getToken(),
                    userAccessInfo.getOrganizationId(),
                    userAccessInfo.getOrganizationName(),
                    userAccessInfo.getOrganizationType(),
                    userAccessInfo.getUserId(),
                    user.getEmail(),
                    userAccessInfo.getName(),
                    userAccessInfo.getPermissionsStr(),
                    userAccessInfo.getTokenCreation()
                    );

            // Log.e(TAG, "insertTemplate: " + insertQuery);

            mDatabase.execSQL(insertQuery);
            wasSaved = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        idbWritable.writableResult(user, wasSaved);
        mDatabase.close();
    }


    public void insertUserEmail (String email, IDBWritable idbWritable) {
        SQLiteDatabase mDatabase = this.getWritableDatabase();
        boolean wasSaved = false;

        try {
            String insertQuery = String.format(Queries.INSERT_USER_EMAIL,
                    email);

            // Log.e(TAG, "insertTemplate: " + mInsertQuery);

            mDatabase.execSQL(insertQuery);
            wasSaved = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        idbWritable.writableResult(email, wasSaved);
        mDatabase.close();
    }


    public void insertAccessDate (String date, IDBWritable idbWritable) {
        SQLiteDatabase mDatabase = this.getWritableDatabase();
        boolean wasSaved = false;

        try {
            String insertQuery = String.format(Queries.INSERT_ACCESS_DATE,
                    date);

            // Log.e(TAG, "insertTemplate: " + mInsertQuery);
            mDatabase.execSQL(insertQuery);
            wasSaved = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        idbWritable.writableResult(date, wasSaved);
        mDatabase.close();
    }


    /***********************************************************************************************
     *                                        SELECT QUERIES                                       *
     ***********************************************************************************************/
     public void getUserToken (IDBReadable idbReadable) {
        String token = null;
        SQLiteDatabase database = this.getWritableDatabase();

        try {
            String selectQuery = Queries.SQL_SELECT_USER_TOKEN;
            Cursor cursor = database.rawQuery(selectQuery, null);

            if (cursor != null) {
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    token = cursor.getString(cursor.getColumnIndex(Queries.TABLE_FIELD_TOKEN));
                }

                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            token = null;
        }

        idbReadable.readableResult(token);
        database.close();
    }


    public void getUserPermissionsAndToken(IDBReadable idbReadable) {
        String token        = null;
        String permissions  = null;

        SQLiteDatabase database = this.getWritableDatabase();

        try {
            String selectQuery = Queries.SQL_SELECT_USER_PERMISSIONS_AND_TOKEN;
            Cursor cursor = database.rawQuery(selectQuery, null);

            if (cursor != null) {
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    token       = cursor.getString(cursor.getColumnIndex(Queries.TABLE_FIELD_TOKEN));
                    permissions = cursor.getString(cursor.getColumnIndex(Queries.TABLE_FIELD_USER_PERMISSIONS));
                }

                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            token = null;
            permissions = null;
        }

        String [] values = new String [] {token, permissions};

        idbReadable.readableResult(values);
        database.close();
    }


    public String getMacWifiSmartphone () {
        String macWifiSmartphone = null;
        SQLiteDatabase database = this.getWritableDatabase();

        try {
            String selectQuery = Queries.SQL_SELECT_WIFI_MAC_SMARTPHONE;
            Cursor cursor = database.rawQuery(selectQuery, null);

            if (cursor != null) {
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    macWifiSmartphone = cursor.getString(cursor.getColumnIndex(Queries.TABLE_FIELD_CELLPHONE_ID));
                }

                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            macWifiSmartphone = null;
        }

        database.close();
        return macWifiSmartphone;
    }


    public void getUserEmail (IDBReadable idbReadable) {
        String userEmail = null;
        SQLiteDatabase database = this.getWritableDatabase();

        try {
            String selectQuery = Queries.SQL_SELECT_USER_EMAIL;
            Cursor cursor = database.rawQuery(selectQuery, null);

            if (cursor != null) {
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    userEmail = cursor.getString(cursor.getColumnIndex(Queries.TABLE_FIELD_USER_EMAIL));
                }

                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            userEmail = null;
        }

        idbReadable.readableResult(userEmail);
        database.close();
    }


    public void getAccessDate (IDBReadable idbReadable) {
        String accessDate = null;
        SQLiteDatabase database = this.getWritableDatabase();

        try {
            String selectQuery = Queries.SQL_SELECT_ACCESS_DATE;
            Cursor cursor = database.rawQuery(selectQuery, null);

            if (cursor != null) {
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    accessDate = cursor.getString(cursor.getColumnIndex(Queries.TABLE_FIELD_TOKEN_CREATION));
                }

                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            accessDate = null;
        }

        idbReadable.readableResult(accessDate);
        database.close();
    }


    public void getUser (IDBReadable idbReadable) {
        UserModel userModel = null;
        SQLiteDatabase database = this.getWritableDatabase();

        try {

            String selectQuery = Queries.SQL_SELECT_USER_DATA;
            Cursor cursor = database.rawQuery(selectQuery, null);

            if (cursor != null) {

                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();

                    userModel = new UserModel(
                            cursor.getString(cursor.getColumnIndex(Queries.TABLE_FIELD_USER_EMAIL)),
                            cursor.getString(cursor.getColumnIndex(Queries.TABLE_FIELD_TOKEN_CREATION)),
                            cursor.getString(cursor.getColumnIndex(Queries.TABLE_FIELD_CELLPHONE_ID)),
                            cursor.getString(cursor.getColumnIndex(Queries.TABLE_FIELD_TOKEN))
                    );
                }

                cursor.close();
            }

        } catch (Exception e) {
            Log.e(TAG, "ERROR USER: " + e.getMessage());
            userModel = null;
        }

        idbReadable.readableResult(userModel);
        database.close();
    }


    /***********************************************************************************************
     *                                        UPDATE QUERIES                                       *
     ***********************************************************************************************/
    public void updateAccessDate (String date, UserModel userModel, IDBWritable idbWritable) {
        SQLiteDatabase mDatabase = this.getWritableDatabase();
        boolean wasUpdated = false;

        try {
            String updateQuery = String.format(Queries.UPDATE_USER_ACCESS_DATE,
                    date,
                    userModel.getEmail());

            // Log.e(TAG, "insertQuery: " + updateQuery);
            mDatabase.execSQL(updateQuery);
            wasUpdated = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        idbWritable.writableResult(date, wasUpdated);
        mDatabase.close();
    }


    /***********************************************************************************************
     *                                        DELETE QUERIES                                       *
     ***********************************************************************************************/
    public boolean deleteUserTable () {
        SQLiteDatabase mDatabase = this.getWritableDatabase();

        try {
            String deleteQuery = Queries.DELETE_USER_TABLE;

            // Log.e(TAG, "insertTemplate: " + mInsertQuery);

            mDatabase.execSQL(deleteQuery);

        } catch (Exception e) {
            e.printStackTrace();
        }

        mDatabase.close();
        return true;
    }


    /***********************************************************************************************
     *                                             UTILS                                           *
     ***********************************************************************************************/
    public boolean isMacSmartphoneSaved () {
        return (getMacWifiSmartphone() != null);
    }



    /***********************************************************************************************
     *                                             INTERFACE                                       *
     ***********************************************************************************************/
    public interface IDBWritable {
        void writableResult(Object data, boolean wasWritten);
    }


    public interface IDBReadable {
        void readableResult(Object data);
    }
}
