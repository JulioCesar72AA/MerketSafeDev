package mx.softel.cirwirelesslib.utils

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
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
                         characteristic : BluetoothGattCharacteristic,
                         mode           : Int,
                         mac            : ByteArray)
        = service.writeToCharacteristic(CommandUtils.setDeviceWifiModeCmd(mode, mac), characteristic)


    fun setInternalWifiCmd(service          : BleService,
                           characteristic   : BluetoothGattCharacteristic,
                           ssid             : String,
                           password         : String,
                           flag             : Int,
                           mac              : ByteArray)
            = service.writeToCharacteristic(CommandUtils.setInternalNameAPCmd(ssid, password, flag, mac), characteristic)


    fun getInternalWifiCmd(service          : BleService,
                           characteristic   : BluetoothGattCharacteristic,
                           mac              : ByteArray)
            = service.writeToCharacteristic(CommandUtils.getInternalNameAPCmd(mac), characteristic)


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
                             password       : String,
                             mac            : ByteArray)
            = service.writeToCharacteristic(CommandUtils.configureAccessPointCmd(ssid, password, mac), characteristic)


    /**
     * ## sendIpAtCmd
     * Ejecuta el comando AT que verifica la IP asignada al dispositivo
     * por el Access Point, si no nos ha asignado, responde "0.0.0.0"
     */
    fun sendIpAtCmd(service         : BleService,
                    characteristic  : BluetoothGattCharacteristic,
                    mac             : ByteArray)
            = service.writeToCharacteristic(CommandUtils.checkIpAddressCmd(mac), characteristic)

    /**
     * ## sendApConnectionCmd
     * Ejecuta el comando AT para verificar cual es el access point al que
     * se encuentra conectado el dispositivo
     */
    fun sendApConnectionCmd(service         : BleService,
                            characteristic  : BluetoothGattCharacteristic,
                            mac             : ByteArray)
            = service.writeToCharacteristic(CommandUtils.checkApConnectionCmd(mac), characteristic)


    /**
     * ## sendPing
     * Ejecuta el comando de PING al dominio establecido, puede responder
     * el tiempo de ejecución o un error por TIMEOUT
     *
     * @param domain Dominio al cual se desea hacer ping (ejemplo: www.gogle.com)
     */
    fun sendPing(service        : BleService,
                 characteristic : BluetoothGattCharacteristic,
                 domain         : String,
                 mac            : ByteArray)
            = service.writeToCharacteristic(CommandUtils.pingApCmd(domain, mac), characteristic)


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
                         characteristic : BluetoothGattCharacteristic,
                         mac            : ByteArray)
            = service.writeToCharacteristic(CommandUtils.closeSocketCmd(mac), characteristic)


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
                        port            : String,
                        mac             : ByteArray)
            = service.writeToCharacteristic(CommandUtils.openSocketCmd(server, port, mac), characteristic)


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
                       enable           : Int,
                       mac              : ByteArray)
            = service.writeToCharacteristic(CommandUtils.setAutoConnCmd(enable, mac), characteristic)


    fun resetWifiCmd(service        : BleService,
                     characteristic : BluetoothGattCharacteristic,
                     mac            : ByteArray)
        = service.writeToCharacteristic(CommandUtils.resetWifiCmd(mac), characteristic)


    fun getWirelessFirmwareCmd(service          : BleService,
                               characteristic   : BluetoothGattCharacteristic,
                               mac              : ByteArray)
            = service.writeToCharacteristic(CommandUtils.getWirelessFirmwareCmd(mac), characteristic)


    fun initCmd(service         : BleService,
                characteristic  : BluetoothGattCharacteristic,
                mac             : ByteArray)
            = service.writeToCharacteristic(CommandUtils.initialCmd(mac), characteristic)


    fun terminateCmd(service        : BleService,
                     characteristic : BluetoothGattCharacteristic,
                     mac            : ByteArray)
            = service.writeToCharacteristic(CommandUtils.terminateCmd(mac), characteristic)


    fun checkCipStatusCmd(service       : BleService,
                          characteristic: BluetoothGattCharacteristic,
                          mac           : ByteArray)
            = service.writeToCharacteristic(CommandUtils.checkCipStatusCmd(mac), characteristic)

    fun openLock (service           : BleService,
                  characteristic    : BluetoothGattCharacteristic,
                  mac               : ByteArray)
            = service.writeToCharacteristic(CommandUtils.openLockCmd(mac), characteristic)

    fun closeLock (service          : BleService,
                   characteristic   : BluetoothGattCharacteristic,
                   mac              : ByteArray)
            = service.writeToCharacteristic(CommandUtils.closeLockCmd(mac), characteristic)

    fun reloadFridge (service          : BleService,
                      characteristic: BluetoothGattCharacteristic,
                      mac : ByteArray)
            = service.writeToCharacteristic(CommandUtils.reloadFridgeCmd(mac), characteristic)

    fun updateDate (service         : BleService,
                    characteristic  : BluetoothGattCharacteristic,
                    mac             : ByteArray)
            = service.writeToCharacteristic(CommandUtils.setDate(mac), characteristic)

    fun readDate (service           : BleService,
                  characteristic    : BluetoothGattCharacteristic,
                  mac               : ByteArray)
            = service.writeToCharacteristic(CommandUtils.readDate(mac), characteristic)

    fun setStaticIp (service: BleService,
                     characteristic: BluetoothGattCharacteristic,
                     mac: ByteArray)
            = service.writeToCharacteristic(CommandUtils.setStaticIpCmd(mac), characteristic)

    fun setDyanmicIp (service: BleService,
                      characteristic: BluetoothGattCharacteristic,
                      mac: ByteArray)
            = service.writeToCharacteristic(CommandUtils.setDynamicIpCmd(mac), characteristic)

    fun setStaticIpValues (ipAddress: String,
                           maskAddress: String,
                           gateway: String,
                           service: BleService,
                           characteristic: BluetoothGattCharacteristic,
                           mac: ByteArray)
            = service.writeToCharacteristic(CommandUtils.setStaticIpValuesCmd(ipAddress, gateway, maskAddress, mac), characteristic)

    fun setRepositoryUrl (urlRepository: String,
                          port: String, service: BleService,
                          characteristic: BluetoothGattCharacteristic,
                          mac: ByteArray)
            = service.writeToCharacteristic(CommandUtils.setRepositoryUrl(urlRepository, port, mac), characteristic)

    fun setFirmwarePath (path: String, imagePrefix: String,
                         service: BleService,
                         characteristic: BluetoothGattCharacteristic,
                         mac: ByteArray)
            = service.writeToCharacteristic(CommandUtils.setImagePath(path, imagePrefix, mac), characteristic)

    // AT+OTAUPDATE=1,"0.2.0-3"
    fun setFirmwareVersion (firmwareVersion: String,
                            service: BleService,
                            characteristic: BluetoothGattCharacteristic,
                            mac: ByteArray)
            = service.writeToCharacteristic(CommandUtils.setImageVersion(firmwareVersion, mac), characteristic)

    fun getFirmwareWiFiModule (service: BleService,
                               characteristic: BluetoothGattCharacteristic,
                               mac: ByteArray)
            = service.writeToCharacteristic(CommandUtils.getFirmware(mac), characteristic)

    /*
    0x19
     */
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
        // Log.e("CIR_COMMANDS", "RESPONSE_MAC_LIST: ${response[3]} = ${0x2B.toByte()}")
        // Log.e("CIR_COMMANDS", "RESPONSE: ${response.toHex()}")

        if (response[3].toInt() != 0) {
            val list = ArrayList<String>()
            val byteElement = ByteArray(6)
            var macString: String

            for (i in 0..2) {
                // Iteramos por cada elemento de las MAC del arreglo
                for (j in 0..2) {
                    byteElement[j] = response[(2 * i) + (j + 1)]
                }
                // Casteamos el ByteArray en un String para el array final
                macString = byteElement.toHex().replace(" ", ":")
                list.add(macString)
            }
            return list
        }

        return null;
    }

}