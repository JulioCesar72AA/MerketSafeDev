package mx.softel.scanblelib.sqlite

object Catalog {

    private val TAG = Catalog::class.java.simpleName

    // BASE DE DATOS
    const val DATABASE_NAME                 = "beacons_db"
    const val DATABASE_VERSION              = 2

    // TABLAS
    const val TABLE_BEACONS_NAME            = "beacon"

    // COLUMNAS DE LA TABLA BEACONS
    const val BEACON_ID                     = "id_beacon"
    const val BEACON_TYPE                   = "tipo_beacon"
    const val BEACON_DESCRIPTION            = "descripcion"
    const val BEACON_ENCRYPTION             = "encriptado"      // Indica si la información está encriptada
    const val BEACON_COLLECT_ALLOW          = "recolectar"      // Indica si el dispositivo puede ser recolectado

    // VALORES DE BEACONS CONOCIDOS
    const val BEACON_TYPE_0000              = "0x0000"
    const val BEACON_TYPE_0001              = "0x0001"
    const val BEACON_TYPE_0002              = "0x0002"
    const val BEACON_TYPE_0003              = "0x0003"
    const val BEACON_TYPE_0004              = "0x0004"
    const val BEACON_TYPE_0005              = "0x0005"
    const val BEACON_TYPE_0006              = "0x0006"
    const val BEACON_TYPE_0007              = "0x0007"
    const val BEACON_TYPE_0008              = "0x0008"
    const val BEACON_TYPE_0009              = "0x0009"
    const val BEACON_TYPE_000A              = "0x000a"
    const val BEACON_TYPE_000B              = "0x000b"
    const val BEACON_TYPE_000C              = "0x000c"
    const val BEACON_TYPE_000D              = "0x000d"
    const val BEACON_TYPE_000E              = "0x000e"
    const val BEACON_TYPE_000F              = "0x000f"
    const val BEACON_TYPE_0013              = "0x0013"
    const val BEACON_TYPE_0014              = "0x0014"


    const val BEACON_DESCRIPTION_0000       = "BootLoader"
    const val BEACON_DESCRIPTION_0001       = "Cir 2017"
    const val BEACON_DESCRIPTION_0002       = "Cir Nama"
    const val BEACON_DESCRIPTION_0003       = "DataLogger"
    const val BEACON_DESCRIPTION_0004       = "MSC 4G LTE"
    const val BEACON_DESCRIPTION_0005       = "Cir 2017 1.2.6, 1.2.8 y 1.2.9"
    const val BEACON_DESCRIPTION_0006       = "Cir 2017 1.3.5"
    const val BEACON_DESCRIPTION_0007       = "Cir 2017"
    const val BEACON_DESCRIPTION_0008       = "Cir 2017"
    const val BEACON_DESCRIPTION_0009       = "Cir 2017 1.3.6"
    const val BEACON_DESCRIPTION_000A       = "Cir 2017 1.3.9"
    const val BEACON_DESCRIPTION_000B       = "Cir Wireless"
    const val BEACON_DESCRIPTION_000C       = "Cir Wireless Blocked"
    const val BEACON_DESCRIPTION_000D       = "CIR RS"
    const val BEACON_DESCRIPTION_000E       = "CIR RSB"
    const val BEACON_DESCRIPTION_000F       = "Sanitizador"
    const val BEACON_DESCRIPTION_0013       = "Cir RS"
    const val BEACON_DESCRIPTION_0014       = "Cir RS"

    // VALORES DEFAULT
    const val NULL_STRING                   = "null"
    const val DEVICE_IS_ENCRYPTED           = 1
    const val DEVICE_IS_NOT_ENCRYPTED       = 0
    const val DEVICE_COULD_BE_COLLECTED     = 1
    const val DEVICE_COULD_NOT_BE_COLLECTED = 0


    // QWERIES A SQLITE

    // Creación de la tabla de BEACONS
    const val DATABASE_CREATE_TABLE_BEACONS =
        """
            CREATE TABLE $TABLE_BEACONS_NAME (
                $BEACON_ID INTEGER AUTO_INCREMENT,
                $BEACON_TYPE TEXT,
                $BEACON_DESCRIPTION TEXT,
                $BEACON_ENCRYPTION NUMERIC,
                $BEACON_COLLECT_ALLOW NUMERIC,
                PRIMARY KEY($BEACON_ID)
            )
        """

    // Inserción del catálogo de valores de los BEACONS
    const val DATABASE_INSERT_DEFAULT_BEACONS =
        """
            INSERT INTO $TABLE_BEACONS_NAME ($BEACON_ID, $BEACON_TYPE, $BEACON_DESCRIPTION, $BEACON_ENCRYPTION, $BEACON_COLLECT_ALLOW)
            VALUES
            ($NULL_STRING, '$BEACON_TYPE_0000', '$BEACON_DESCRIPTION_0000', $DEVICE_IS_NOT_ENCRYPTED, $DEVICE_COULD_NOT_BE_COLLECTED ),
            ($NULL_STRING, '$BEACON_TYPE_0001', '$BEACON_DESCRIPTION_0001', $DEVICE_IS_ENCRYPTED, $DEVICE_COULD_BE_COLLECTED ),
            ($NULL_STRING, '$BEACON_TYPE_0002', '$BEACON_DESCRIPTION_0002', $DEVICE_IS_NOT_ENCRYPTED, $DEVICE_COULD_NOT_BE_COLLECTED ),
            ($NULL_STRING, '$BEACON_TYPE_0003', '$BEACON_DESCRIPTION_0003', $DEVICE_IS_ENCRYPTED, $DEVICE_COULD_BE_COLLECTED ),
            ($NULL_STRING, '$BEACON_TYPE_0004', '$BEACON_DESCRIPTION_0004', $DEVICE_IS_NOT_ENCRYPTED, $DEVICE_COULD_NOT_BE_COLLECTED ),
            ($NULL_STRING, '$BEACON_TYPE_0005', '$BEACON_DESCRIPTION_0005', $DEVICE_IS_NOT_ENCRYPTED, $DEVICE_COULD_BE_COLLECTED ),
            ($NULL_STRING, '$BEACON_TYPE_0006', '$BEACON_DESCRIPTION_0006', $DEVICE_IS_NOT_ENCRYPTED, $DEVICE_COULD_BE_COLLECTED ),
            ($NULL_STRING, '$BEACON_TYPE_0007', '$BEACON_DESCRIPTION_0007', $DEVICE_IS_NOT_ENCRYPTED, $DEVICE_COULD_BE_COLLECTED ),
            ($NULL_STRING, '$BEACON_TYPE_0008', '$BEACON_DESCRIPTION_0008', $DEVICE_IS_NOT_ENCRYPTED, $DEVICE_COULD_BE_COLLECTED ),
            ($NULL_STRING, '$BEACON_TYPE_0009', '$BEACON_DESCRIPTION_0009', $DEVICE_IS_ENCRYPTED, $DEVICE_COULD_BE_COLLECTED ),
            ($NULL_STRING, '$BEACON_TYPE_000A', '$BEACON_DESCRIPTION_000A', $DEVICE_IS_ENCRYPTED, $DEVICE_COULD_BE_COLLECTED ),
            ($NULL_STRING, '$BEACON_TYPE_000B', '$BEACON_DESCRIPTION_000B', $DEVICE_IS_ENCRYPTED, $DEVICE_COULD_BE_COLLECTED),
            ($NULL_STRING, '$BEACON_TYPE_000C', '$BEACON_DESCRIPTION_000C', $DEVICE_IS_ENCRYPTED, $DEVICE_COULD_BE_COLLECTED),
            ($NULL_STRING, '$BEACON_TYPE_000D', '$BEACON_DESCRIPTION_000D', $DEVICE_IS_ENCRYPTED, $DEVICE_COULD_BE_COLLECTED),
            ($NULL_STRING, '$BEACON_TYPE_000E', '$BEACON_DESCRIPTION_000E', $DEVICE_IS_ENCRYPTED, $DEVICE_COULD_BE_COLLECTED),
            ($NULL_STRING, '$BEACON_TYPE_000F', '$BEACON_DESCRIPTION_000F', $DEVICE_IS_ENCRYPTED, $DEVICE_COULD_BE_COLLECTED),
            ($NULL_STRING, '$BEACON_TYPE_0013', '$BEACON_DESCRIPTION_0013', $DEVICE_IS_ENCRYPTED, $DEVICE_COULD_BE_COLLECTED),
            ($NULL_STRING, '$BEACON_TYPE_0014', '$BEACON_DESCRIPTION_0014', $DEVICE_IS_ENCRYPTED, $DEVICE_COULD_BE_COLLECTED)
        """

    // Selección de beacons por tipo "recolectables"
    const val DATABASE_SELECT_BEACON_TYPES =
        """
            SELECT $BEACON_ID, $BEACON_TYPE, $BEACON_DESCRIPTION, $BEACON_ENCRYPTION
            FROM $TABLE_BEACONS_NAME
            WHERE $BEACON_COLLECT_ALLOW = $DEVICE_COULD_BE_COLLECTED
        """

    // Selección de toda la tabla BEACONS
    const val DATABASE_SELECT_ALL_BEACONS_TYPES = "SELECT * FROM $TABLE_BEACONS_NAME"

    // Actualización de la base de datos
    const val DATABASE_DROP_TABLE_MACS_CONECTION_TABLE = "DROP TABLE IF EXISTS $TABLE_BEACONS_NAME"


}