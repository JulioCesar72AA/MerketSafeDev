package mx.softel.cirwireless.wifi_db;

class Queries extends Definition {

    /***********************************************************************************************
     *                                  CREATE TABLE QUERIES                                       *
     ***********************************************************************************************/
    static final String CREATE_USER_TABLE =
            "CREATE TABLE " + TABLE_NAME_USER + " ("
                    + TABLE_FIELD_USER_ID + " INTEGER, "
                    + TABLE_FIELD_CELLPHONE_ID + " TEXT, "
                    + TABLE_FIELD_USER_EMAIL + " TEXT, "
                    + TABLE_FIELD_TOKEN_CREATION + " TEXT, "
                    + TABLE_FIELD_TOKEN + " TEXT, "
                    + TABLE_FIELD_ORGANIZATION_ID + " INTEGER, "
                    + TABLE_FIELD_ORGANIZATION_NAME + " TEXT, "
                    + TABLE_FIELD_ORGANIZATION_TYPE + " TEXT, "
                    + TABLE_FIELD_USER_NAME + " TEXT, "
                    + TABLE_FIELD_USER_PERMISSIONS + " TEXT, "
                    + "PRIMARY KEY (" + TABLE_FIELD_CELLPHONE_ID + "))";

    /***********************************************************************************************
     *                                  INSERT TABLE QUERIES                                       *
     ***********************************************************************************************/
    static final String INSERT_CELLPHONE_ID =
            "INSERT INTO " + TABLE_NAME_USER + " (" +
                    TABLE_FIELD_USER_ID + ", " +
                    TABLE_FIELD_CELLPHONE_ID + ") " +
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
            TABLE_FIELD_TOKEN_CREATION + ") " +
            "VALUES (" +
                    "null, " +
                    "'%s'"   +
                    ")";


    static final String INSERT_USER_DATA = "" +
            "INSERT INTO " + TABLE_NAME_USER + " (" +
                TABLE_FIELD_CELLPHONE_ID + ", " +
                TABLE_FIELD_TOKEN + ", " +
                TABLE_FIELD_ORGANIZATION_ID + ", " +
                TABLE_FIELD_ORGANIZATION_NAME + ", " +
                TABLE_FIELD_ORGANIZATION_TYPE + ", " +
                TABLE_FIELD_USER_ID + ", " +
                TABLE_FIELD_USER_EMAIL + ", " +
                TABLE_FIELD_USER_NAME + ", " +
                TABLE_FIELD_USER_PERMISSIONS + ", " +
                TABLE_FIELD_TOKEN_CREATION +
                ") " +
            "VALUES (" +
                "'%s', " +
                "'%s', " +
                "%d, " +
                "'%s', " +
                "'%s', " +
                "%d, " +
                "'%s', "   +
                "'%s', "   +
                "'%s', "   +
                "'%s' "   +
            ")";


    /***********************************************************************************************
     *                                  UPDATE TABLE QUERIES                                       *
     ***********************************************************************************************/
    static final String UPDATE_USER_ACCESS_DATE = "" +
            "UPDATE " + TABLE_NAME_USER + " "+
            "SET " + TABLE_FIELD_TOKEN_CREATION + "= '%s' " +
            "WHERE " + TABLE_FIELD_USER_EMAIL + "= '%s'";



    /***********************************************************************************************
     *                                  SELECT TABLE QUERIES                                       *
     ***********************************************************************************************/
    static final String SQL_SELECT_WIFI_MAC_SMARTPHONE = "SELECT " +
                TABLE_FIELD_CELLPHONE_ID + " " +
            "FROM " +
                TABLE_NAME_USER;


    static final String SQL_SELECT_USER_TOKEN = "SELECT " +
                TABLE_FIELD_TOKEN + " " +
            "FROM " +
                TABLE_NAME_USER;


    static final String SQL_SELECT_USER_PERMISSIONS_AND_TOKEN = "SELECT " +
                TABLE_FIELD_TOKEN + ", " +
                TABLE_FIELD_USER_PERMISSIONS + " " +
            "FROM " +
                TABLE_NAME_USER;



    static final String SQL_SELECT_USER_EMAIL = "SELECT " +
                TABLE_FIELD_USER_EMAIL + " " +
            "FROM " +
                TABLE_NAME_USER;


    static final String SQL_SELECT_ACCESS_DATE = "SELECT " +
            TABLE_FIELD_TOKEN_CREATION + " " +
            "FROM " +
                TABLE_NAME_USER;


    static final String SQL_SELECT_USER_DATA = "SELECT " +
                TABLE_FIELD_USER_EMAIL          + ", " +
            TABLE_FIELD_TOKEN_CREATION + ", " +
            TABLE_FIELD_CELLPHONE_ID + ", " +
            TABLE_FIELD_TOKEN + " " +
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
