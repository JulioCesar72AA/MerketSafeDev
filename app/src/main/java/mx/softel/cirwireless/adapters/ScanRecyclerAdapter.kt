package mx.softel.cirwireless.adapters

import android.content.Context
import android.opengl.Visibility
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.scan_devices_prototype.view.*
import mx.softel.cirwireless.CirDevice
import mx.softel.cirwireless.R
import mx.softel.scanblelib.ble.BleDevice
import mx.softel.scanblelib.extensions.inflate

class ScanRecyclerAdapter (private val context: Context, private val devicesList: List<CirDevice>,
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

        fun bind(device: CirDevice) = with(itemView) {
            tvScanMac.text      = device.bleDevice.getMac()
            tvDeviceModel.text  = device.bleDevice.getDeviceModelName()

            if (device.getScanPostResponse() != null) {
                tvDeviceSerial.text         = "${context.getText(R.string.serial_number_solkos)} ${device.getScanPostResponse()?.serialNumber}"
                tvDeviceSolkosModel.text    = "${context.getText(R.string.model_solkos)} ${device.getScanPostResponse()?.assetModel}"
                tvDeviceType.text           = "${context.getText(R.string.type_solkos)} ${device.getScanPostResponse()?.assetType}"

                var transmiting = context.getString(R.string.transmitting)

                if (!device.getScanPostResponse()!!.isTransmitting)
                    transmiting = context.getString(R.string.no_transmission)

                tvDeviceTransmition.text    = "${context.getText(R.string.transmition_solkos)} $transmiting"

            } else {
                this.setBackgroundColor(resources.getColor(R.color.colorBackShadow))
                tvDeviceSerial.visibility       = View.GONE
                tvDeviceSerial.visibility       = View.GONE
                tvDeviceSolkosModel.visibility  = View.GONE
                tvDeviceType.visibility         = View.GONE
                tvDeviceTransmition.visibility  = View.GONE
            }

        }

        override fun onClick(v: View?) {
            if (v?.tvDeviceSerial!!.isVisible)
                onScanClick.onScanClickListener(adapterPosition)

            else
                onScanClick.onScanClickListener(-1)
        }

    }


    /************************************************************************************************/
    /**     INTERFACES                                                                              */
    /************************************************************************************************/
    interface OnScanClickListener {
        fun onScanClickListener(position: Int)
    }

}