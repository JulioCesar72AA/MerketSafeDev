package mx.softel.scanblelib.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.ble_scan_list_prototype.view.*
import mx.softel.scanblelib.R
import mx.softel.scanblelib.ble.BleDevice
import mx.softel.scanblelib.extensions.inflate

class BleDeviceRecyclerAdapter(private val devicesList: List<BleDevice>,
                               private val onScanClickListener: OnScanClickListener)
    : RecyclerView.Adapter<BleDeviceRecyclerAdapter.DeviceScanViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceScanViewHolder
            = DeviceScanViewHolder(parent.inflate(R.layout.ble_scan_list_prototype), onScanClickListener)


    override fun getItemCount(): Int = devicesList.size


    override fun onBindViewHolder(holder: DeviceScanViewHolder, position: Int)
            = holder.bind(devicesList[position])

    /************************************************************************************************/
    /**     VIEW HOLDER                                                                             */
    /************************************************************************************************/
    class DeviceScanViewHolder(itemView: View, onScanClickListener: OnScanClickListener)
        : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var onScanClick: OnScanClickListener

        init {
            itemView.setOnClickListener(this@DeviceScanViewHolder)
            onScanClick = onScanClickListener
        }

        fun bind(device: BleDevice) = with(itemView) {
            tvScanName.text = device.getName()
            tvScanMac.text  = device.getMac()
            tvScanRSSI.text = device.getRssi().toString()
        }

        override fun onClick(v: View?) {
            onScanClick.onScanClickListener(adapterPosition)
        }

    }


    /************************************************************************************************/
    /**     INTERFACES                                                                              */
    /************************************************************************************************/
    interface OnScanClickListener {
        fun onScanClickListener(position: Int)
    }


}