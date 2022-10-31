package mx.softel.cirwireless

import mx.softel.cirwireless.log_in_module.web_service.ScanPostResponse
import mx.softel.scanblelib.ble.BleDevice

data class CirDevice (val bleDevice : BleDevice,
                      val scanPostResponse: ScanPostResponse)