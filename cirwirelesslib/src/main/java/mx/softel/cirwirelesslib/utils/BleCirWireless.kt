package mx.softel.cirwirelesslib.utils

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.util.Log
import mx.softel.bleservicelib.BleService
import mx.softel.bleservicelib.enums.DisconnectionReason
import mx.softel.cirwirelesslib.constants.BleConstants
import mx.softel.cirwirelesslib.enums.StateMachine
import java.util.*

class BleCirWireless {

    private val TAG = BleCirWireless::class.java.simpleName

    private var uuidService             : UUID?                         = null
    private var uuidQuickCommandService : UUID?                         = null

    private var characteristicDeviceInfo: BluetoothGattCharacteristic?  = null
    private var characteristicNotify    : BluetoothGattCharacteristic?  = null
    private var characteristicWrite     : BluetoothGattCharacteristic?  = null
    private var quickCommandsCharacteristic : BluetoothGattCharacteristic? = null
    private var notificationDescriptor  : BluetoothGattDescriptor?      = null

    private var currentState    : StateMachine  = StateMachine.UNKNOWN
    private var firmware        : String        = ""
    private var correctFirmware : Boolean       = false
    private var isDescriptorOn  : Boolean       = false

    /************************************************************************************************/
    /**     GETTERS/SETTERS                                                                         */
    /************************************************************************************************/
    fun getCharacteristicDeviceInfo(): BluetoothGattCharacteristic? = characteristicDeviceInfo
    fun getCharacteristicWrite()     : BluetoothGattCharacteristic? = characteristicWrite
    fun getQuickCommandsCharacteristic() : BluetoothGattCharacteristic? = quickCommandsCharacteristic
    fun getCharacteristicNotify()    : BluetoothGattCharacteristic? = characteristicNotify
    fun getNotificationDescriptor()  : BluetoothGattDescriptor?     = notificationDescriptor
    fun getCurrentState()            : StateMachine                 = currentState
    fun setCurrentState(state: StateMachine) { currentState = state }

    /**
     * ## getCommCharacteristics
     * Obtiene las características del servicio
     *
     * @param service Servicio Bluetooth del dispositivo conectado
     */
    fun getCommCharacteristics(service: BluetoothGattService) {
        val uuid = service.uuid.toString()

        if (uuid == BleConstants.CIR_NAMA_SERVICE_UUID) {
            uuidService = service.uuid

            // Solicitamos e inicializamos las características de COMUNICACIÓN
            val characteristics = service.characteristics
            for (char in characteristics) {

                when (char.uuid.toString()) {
                    BleConstants.CIR_NAMA_NOTIFY_UUID -> {
                        characteristicNotify = char

                        // Obtenemos los descriptores de la notificación
                        for (desc in char.descriptors) {
                            if (desc.uuid.toString() == BleConstants.NOTIFICATION_DESCRIPTOR) {
                                notificationDescriptor = desc
                            }
                        }

                    }
                    BleConstants.CIR_NAMA_WRITE_UUID -> {
                        characteristicWrite = char
                    }
                }
            }
        }

        if (uuid == BleConstants.QUICK_COMMANDS_SERVICE) {
            uuidQuickCommandService = service.uuid
            val characteristics = service.characteristics

            for (char in characteristics) {
                // Log.e(TAG, "uuid: ${char.uuid.toString()}")
                when (char.uuid.toString()) {
                    BleConstants.QUICK_COMMANDS_CHARACTERISTIC -> {
                        quickCommandsCharacteristic = char
                    }
                }
            }
        }
    }

    /**
     * ## getInfoCharacteristics
     * Obtiene las características del servicio e inicializa los datos
     * del [BleConstants.DEVICE_INFO_SERVICE_UUID] para validar el firmware
     * de la tarjeta en cuestión
     *
     * @param service Servicio Bluetooth del dispositivo conectado
     */
    fun getInfoCharacteristics(service: BluetoothGattService) {
        val uuid = service.uuid.toString()

        if (uuid == BleConstants.DEVICE_INFO_SERVICE_UUID) {
            // Solicitamos e inicializamos las características de INFORMACIÓN
            val characteristics = service.characteristics
            for (char in characteristics) {
                if (char.uuid.toString() == BleConstants.DEVICE_INFO_UUID) {
                    characteristicDeviceInfo = char
                }
            }
        }
    }

    /**
     * ## initPoleCmd
     * Habilita la notificación en el dispositivo para permitir
     * la comunicación por poleo
     */
    private fun initPoleCmd(service: BleService,
                    characteristic: BluetoothGattCharacteristic,
                    descriptor: BluetoothGattDescriptor) {

        // Log.d(TAG, "initPoleCmd")
        service.apply{
            enableCharacteristicNotification(true, characteristic)
            writeToDescriptor(BleService.DISABLE_NOTIFICATION, descriptor)
        }
        currentState = StateMachine.POLING
    }

    /**
     * ## getFirmwareData
     * Realiza una lectura de [characteristicDeviceInfo] para posteriormente
     * obtener el Firmware en
     */
    fun getFirmwareData(service: BleService) {
        if (characteristicDeviceInfo != null) {
            service.bleGatt!!.readCharacteristic(characteristicDeviceInfo)
        }
    }


    fun extractFirmwareData(service: BleService,
                            characteristic: BluetoothGattCharacteristic,
                            descriptor: BluetoothGattDescriptor) {
        // Log.d(TAG, "extractFirmwareData")
        // Lectura de DEVICE_INFO_UUID
        if (characteristic.uuid.toString() == BleConstants.DEVICE_INFO_UUID) {
            val one = characteristic.value[1]
            val two = characteristic.value[2]
            val three = characteristic.value[3]
            firmware = "$one.$two.$three"
            println("$TAG FIRMWARE $firmware")
            correctFirmware = (firmware == BleConstants.FIRMWARE_350) ||
                    (firmware == BleConstants.FIRMWARE_351) ||
                    (firmware == BleConstants.FIRMWARE_352) ||
                    (firmware == BleConstants.FIRMWARE_353) ||
                    (firmware == BleConstants.FIRMWARE_354) ||
                    (firmware == BleConstants.FIRMWARE_355) ||
                    (firmware == BleConstants.FIRMWARE_357) ||
                    (firmware == BleConstants.FIRMWARE_363) ||
                    (firmware == BleConstants.FIRMWARE_382) ||
                    (firmware == BleConstants.FIRMWARE_387) ||
                    (firmware == BleConstants.FIRMWARE_388) ||
                    (firmware == BleConstants.FIRMWARE_401) ||
                    (firmware == BleConstants.FIRMWARE_402) ||
                    (firmware == BleConstants.FIRMWARE_410)


            if (!correctFirmware)
                service.disconnectBleDevice(DisconnectionReason.FIRMWARE_UNSUPPORTED.status)

            initPoleCmd(service, characteristicNotify!!, descriptor)
        }
    }


    fun descriptorFlag(service: BleService, descriptor: BluetoothGattDescriptor) {
        if (!isDescriptorOn) {
            service.writeToDescriptor(BleService.ENABLE_NOTIFICATION, descriptor)
            isDescriptorOn = true
        }
    }

}