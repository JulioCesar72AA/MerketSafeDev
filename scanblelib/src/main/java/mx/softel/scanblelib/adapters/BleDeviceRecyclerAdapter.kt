package mx.softel.scanblelib.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.ble_scan_list_prototype.view.*
import mx.softel.scanblelib.R
import mx.softel.scanblelib.ble.BleDevice
import mx.softel.scanblelib.extensions.inflate

class BleDeviceRecyclerAdapter(private val devicesList: List<BleDevice>)
    : RecyclerView.Adapter<BleDeviceRecyclerAdapter.DeviceScanViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceScanViewHolder
            = DeviceScanViewHolder(parent.inflate(R.layout.ble_scan_list_prototype))


    override fun getItemCount(): Int = devicesList.size


    override fun onBindViewHolder(holder: DeviceScanViewHolder, position: Int)
            = holder.bind(devicesList[position])

    /************************************************************************************************/
    /**     VIEW HOLDER                                                                             */
    /************************************************************************************************/
    class DeviceScanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(device: BleDevice) = with(itemView) {
            tvScanName.text = device.name ?: "NULL"
            tvScanMac.text  = device.bleMacAddress
            tvScanRSSI.text = device.rssi.toString()
        }

    }

}