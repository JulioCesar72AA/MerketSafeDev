package mx.softel.cirwireless.bootloader_db;

class Queries extends Definition {

    /***********************************************************************************************
     *                                  CREATE TABLE QUERIES                                       *
     ***********************************************************************************************/
    static final String CREATE_USER_TABLE =
            "CREATE TABLE " + TABLE_NAME_USER + " ("
                    + TABLE_FIELD_USER_ID + " INTEGER AUTO_INCREMENT, "
                    + TABLE_FIELD_MAC_WIFI_SMARTPHONE + " TEXT, "
                    + TABLE_FIELD_USER_EMAIL + " TEXT, "
                    + TABLE_FIELD_USER_ACCESS_DATE + " TEXT, "
                    + TABLE_FIELD_SESSION_CODE + " TEXT, "
            + "PRIMARY KEY (" + TABLE_FIELD_MAC_WIFI_SMARTPHONE + "))";

    /***********************************************************************************************
     *                                  INSERT TABLE QUERIES                                       *
     ***********************************************************************************************/
    static final String INSERT_MAC_SMARTPHONE =
            "INSERT INTO " + TABLE_NAME_USER + " (" +
                    TABLE_FIELD_USER_ID + ", " +
                    TABLE_FIELD_MAC_WIFI_SMARTPHONE + ") " +
            "VALUES (" +
                    "null, " +
                    "'%s'" +
                    ")";


    static final String INSERT_USER_EMAIL = "" +
            "INSERT INTO " + TABLE_NAME_USER + " (" +
                    TABLE_FIELD_USER_ID + ", " +
                    TABLE_FIELD_USER_EMAIL + ") " +
            "VALUES (" +
                    "null, " +
                    "'%s'"   +
                    ")";


    static final String INSERT_ACCESS_DATE = "" +
            "INSERT INTO " + TABLE_NAME_USER + " (" +
                    TABLE_FIELD_USER_ID + ", " +
                    TABLE_FIELD_USER_ACCESS_DATE + ") " +
            "VALUES (" +
                    "null, " +
                    "'%s'"   +
                    ")";


    static final String INSERT_USER = "" +
            "INSERT INTO " + TABLE_NAME_USER + " (" +
                TABLE_FIELD_USER_ID + ", " +
                TABLE_FIELD_USER_EMAIL + ", " +
                TABLE_FIELD_USER_ACCESS_DATE + ", " +
                TABLE_FIELD_MAC_WIFI_SMARTPHONE + ", " +
                TABLE_FIELD_SESSION_CODE + ") " +
            "VALUES (" +
                "null, " +
                "'%s', "   +
                "'%s', "   +
                "'%s', "   +
                "'%s'"   +
            ")";


    /***********************************************************************************************
     *                                  UPDATE TABLE QUERIES                                       *
     ***********************************************************************************************/
    static final String UPDATE_USER_ACCESS_DATE = "" +
            "UPDATE " + TABLE_NAME_USER + " "+
            "SET " + TABLE_FIELD_USER_ACCESS_DATE + "= '%s' " +
            "WHERE " + TABLE_FIELD_USER_EMAIL + "= '%s'";



    /***********************************************************************************************
     *                                  SELECT TABLE QUERIES                                       *
     ***********************************************************************************************/
    static final String SQL_SELECT_WIFI_MAC_SMARTPHONE = "SELECT " +
                TABLE_FIELD_MAC_WIFI_SMARTPHONE + " " +
            "FROM " +
                TABLE_NAME_USER;


    static final String SQL_SELECT_USER_EMAIL = "SELECT " +
                TABLE_FIELD_USER_EMAIL + " " +
            "FROM " +
                TABLE_NAME_USER;


    static final String SQL_SELECT_ACCESS_DATE = "SELECT " +
                TABLE_FIELD_USER_ACCESS_DATE + " " +
            "FROM " +
                TABLE_NAME_USER;


    static final String SQL_SELECT_USER_DATA = "SELECT " +
                TABLE_FIELD_USER_EMAIL          + ", " +
                TABLE_FIELD_USER_ACCESS_DATE    + ", " +
                TABLE_FIELD_MAC_WIFI_SMARTPHONE    + ", " +
                TABLE_FIELD_SESSION_CODE + " " +
            "FROM " +
                TABLE_NAME_USER;


    /***********************************************************************************************
     *                                  DELETE TABLE QUERIES                                       *
     ***********************************************************************************************/
    static final String DELETE_USER_TABLE = "DELETE FROM " + TABLE_NAME_USER;

    /***********************************************************************************************
     *                                    DROP TABLE QUERIES                                       *
     ***********************************************************************************************/
    static final String DROP_USER_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME_USER;
}
