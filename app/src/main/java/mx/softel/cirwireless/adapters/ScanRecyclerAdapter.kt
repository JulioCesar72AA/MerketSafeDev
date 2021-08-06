package mx.softel.cirwireless.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.scan_devices_prototype.view.*
import mx.softel.cirwireless.R
import mx.softel.scanblelib.ble.BleDevice
import mx.softel.scanblelib.extensions.inflate

class ScanRecyclerAdapter (private val devicesList: List<BleDevice>,
                           private val onScanClickListener: OnScanClickListener)
    : RecyclerView.Adapter<ScanRecyclerAdapter.ScanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder
            = ScanViewHolder(parent.inflate(R.layout.scan_devices_prototype), onScanClickListener)


    override fun getItemCount(): Int = devicesList.size


    override fun onBindViewHolder(holder: ScanViewHolder, position: Int)
            = holder.bind(devicesList[position])

    /************************************************************************************************/
    /**     VIEW HOLDER                                                                             */
    /************************************************************************************************/
    class ScanViewHolder(itemView: View, onScanClickListener: OnScanClickListener)
        : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var onScanClick: OnScanClickListener

        init {
            itemView.setOnClickListener(this@ScanViewHolder)
            onScanClick = onScanClickListener
        }

        fun bind(device: BleDevice) = with(itemView) {
            tvScanMac.text = device.getMac()
            tvDeviceModel.text = device.getDeviceModelName()
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