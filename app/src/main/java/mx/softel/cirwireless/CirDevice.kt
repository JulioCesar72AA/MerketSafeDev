package mx.softel.cirwireless

import mx.softel.cirwireless.web_services_module.web_service.ScanPostResponse
import mx.softel.scanblelib.ble.BleDevice

class CirDevice (val bleDevice : BleDevice) {
    private var scanPostResponse: ScanPostResponse? = null

    public fun setScanPostResponse (scanPostResponse: ScanPostResponse) {
        this.scanPostResponse = scanPostResponse
    }

    public fun getScanPostResponse (): ScanPostResponse? = this.scanPostResponse
}