package mx.softel.cirwirelesslib.utils

import android.bluetooth.BluetoothGattCharacteristic
import mx.softel.bleservicelib.BleService
import mx.softel.cirwirelesslib.extensions.toHex


object CirCommands {

    /**
     * ## sendRefreshApCmd
     * Ejecuta el comando de actualización de AccesPoints en el dispositivo
     */
    fun sendRefreshApCmd(service        : BleService,
                         characteristic : BluetoothGattCharacteristic)
            = service.writeToCharacteristic(CommandUtils.refreshAccessPointsCmd(), characteristic)


    /**
     * ## getMacListCmd
     * Ejecuta el comando para pedir los AccessPoints que el dispositivo almacenó (6)
     */
    fun getMacListCmd(service           : BleService,
                      characteristic    : BluetoothGattCharacteristic)
            = service.writeToCharacteristic(CommandUtils.getAccessPointsCmd(), characteristic)


    fun setDeviceModeCmd(service        : BleService,
                         characteristic : BluetoothGattCharacteristic, mode: Int)
            = service.writeToCharacteristic(CommandUtils.setDeviceWifiModeCmd(mode), characteristic)


    fun setInternalWifiCmd(service          : BleService,
                           characteristic   : BluetoothGattCharacteristic,
                           ssid             : String,
                           password         : String,
                           flag             : Int)
            = service.writeToCharacteristic(CommandUtils.setInternalNameAPCmd(ssid, password, flag), characteristic)


    fun getInternalWifiCmd(service          : BleService,
                           characteristic   : BluetoothGattCharacteristic)
            = service.writeToCharacteristic(CommandUtils.getInternalNameAPCmd(), characteristic)


    /**
     * ## setConfigureWifiCmd
     * Ejecuta el comando para configurar el access point seleccionado, por medio
     * de comando AT
     *
     * @param ssid Nombre del Access Point
     * @param password La contraseña para acceder al Access Point
     */
    fun sendConfigureWifiCmd(service        : BleService,
                             characteristic : BluetoothGattCharacteristic,
                             ssid           : String,
                             password       : String)
            = service.writeToCharacteristic(CommandUtils.configureAccessPointCmd(ssid, password), characteristic)


    /**
     * ## sendIpAtCmd
     * Ejecuta el comando AT que verifica la IP asignada al dispositivo
     * por el Access Point, si no nos ha asignado, responde "0.0.0.0"
     */
    fun sendIpAtCmd(service         : BleService,
                    characteristic  : BluetoothGattCharacteristic)
            = service.writeToCharacteristic(CommandUtils.checkIpAddressCmd(), characteristic)

    /**
     * ## sendApConnectionCmd
     * Ejecuta el comando AT para verificar cual es el access point al que
     * se encuentra conectado el dispositivo
     */
    fun sendApConnectionCmd(service         : BleService,
                            characteristic  : BluetoothGattCharacteristic)
            = service.writeToCharacteristic(CommandUtils.checkApConnectionCmd(), characteristic)


    /**
     * ## sendPing
     * Ejecuta el comando de PING al dominio establecido, puede responder
     * el tiempo de ejecución o un error por TIMEOUT
     *
     * @param domain Dominio al cual se desea hacer ping (ejemplo: www.gogle.com)
     */
    fun sendPing(service        : BleService,
                 characteristic : BluetoothGattCharacteristic,
                 domain         : String)
            = service.writeToCharacteristic(CommandUtils.pingApCmd(domain), characteristic)


    /**
     * ## readAtResponseCmd
     * Ejecuta el comando para la lectura de la respuesta del comando AT enviado previamente
     */
    fun readAtResponseCmd(service       : BleService,
                          characteristic: BluetoothGattCharacteristic)
            = service.writeToCharacteristic(CommandUtils.readAtCmd(), characteristic)


    /**
     * ## closeAtSocketCmd
     * Ejecuta el comando para cerrar el socket con el servidor
     */
    fun closeAtSocketCmd(service        : BleService,
                         characteristic : BluetoothGattCharacteristic)
            = service.writeToCharacteristic(CommandUtils.closeSocketCmd(), characteristic)


    /**
     * ## openAtSocketCmd
     * Ejecuta el comando para abrir el socket con el servidor
     *
     * @param server Servidor con el que se desea comunicar
     * @param port Puerto de acceso al servidor
     */
    fun openAtSocketCmd(service         : BleService,
                        characteristic  : BluetoothGattCharacteristic,
                        server          : String,
                        port            : String)
            = service.writeToCharacteristic(CommandUtils.openSocketCmd(server, port), characteristic)


    /**
     * ## sensSsidCmd
     * Ejecuta el comando para configurar el nombre del access point al que
     * deseamos conectarnos
     *
     * @param ssid Nombre del Acces Point
     */
    fun sendSsidCmd(service         : BleService,
                    characteristic  : BluetoothGattCharacteristic,
                    ssid            : String)
            = service.writeToCharacteristic(CommandUtils.setSsidCmd(ssid), characteristic)


    /**
     * ## sendPasswordCmd
     * Ejecuta el comando para configurar el password del access point al que
     * deseamos conectarnos
     *
     * @param password Contraseña del Access Point seleccionado
     */
    fun sendPasswordCmd(service         : BleService,
                        characteristic  : BluetoothGattCharacteristic,
                        password        : String)
            = service.writeToCharacteristic(CommandUtils.setPasswordCmd(password), characteristic)


    /**
     * ## sendStatusWifiCmd
     * Envía el comando para verificar el estado de la tarea Wifi
     */
    fun sendStatusWifiCmd(service       : BleService,
                          characteristic: BluetoothGattCharacteristic)
            = service.writeToCharacteristic(CommandUtils.getWifiStatusCmd(), characteristic)


    fun setAutoConnCmd(service          : BleService,
                       characteristic   : BluetoothGattCharacteristic,
                       enable           : Int)
            = service.writeToCharacteristic(CommandUtils.setAutoConnCmd(enable), characteristic)


    fun resetWifiCmd(service        : BleService,
                     characteristic : BluetoothGattCharacteristic)
            = service.writeToCharacteristic(CommandUtils.resetWifiCmd(), characteristic)


    fun getWirelessFirmwareCmd(service          : BleService,
                               characteristic   : BluetoothGattCharacteristic)
            = service.writeToCharacteristic(CommandUtils.getWirelessFirmwareCmd(), characteristic)


    fun initCmd(service         : BleService,
                characteristic  : BluetoothGattCharacteristic)
            = service.writeToCharacteristic(CommandUtils.initialCmd(), characteristic)


    fun terminateCmd(service        : BleService,
                     characteristic : BluetoothGattCharacteristic)
            = service.writeToCharacteristic(CommandUtils.terminateCmd(), characteristic)


    fun checkCipStatusCmd(service       : BleService,
                          characteristic: BluetoothGattCharacteristic)
            = service.writeToCharacteristic(CommandUtils.checkCipStatusCmd(), characteristic)



    /**
     * ## fromResponseGetMacList
     * A partir de la respuesta obtenida por [getMacListCmd] parsea la respuesta
     * para convertir el arreglo de bytes en una lista de Strings conteniendo las
     * direcciones MAC leídas por el dispositivo
     *
     * @param response Arreglo de Bytes que responde el dispositivo
     * @return Lista de MAC's visibles para el dispositivo
     */
    fun fromResponseGetMacList(response: ByteArray): ArrayList<String>? {
        // Validamos que el tamaño de la respuesta sea correcto para parsear los datos
        if (response[3] != 0x2B.toByte()) {
            return null
        }

        val list = ArrayList<String>()
        val byteElement = ByteArray(6)
        var macString: String

        for (i in 0..5) {
            // Iteramos por cada elemento de las MAC del arreglo
            for (j in 0..5) {
                byteElement[j] = response[(6 * i) + (j + 5)]
            }
            // Casteamos el ByteArray en un String para el array final
            macString = byteElement.toHex().replace(" ", ":")
            list.add(macString)
        }
        return list
    }

}