package mx.softel.marketsafe.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.scan_devices_prototype.view.*
import mx.softel.marketsafe.CirDevice
import mx.softel.marketsafe.R
import mx.softel.scanblelib.ble.BleDevice
import mx.softel.scanblelib.extensions.inflate

class ScanRecyclerAdapter (private val context: Context, private val devicesList: List<CirDevice>,
                           private val onScanClick: OnScanClickListener) : RecyclerView.Adapter <ScanRecyclerAdapter.ScanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder
            = ScanViewHolder(parent.inflate(R.layout.scan_devices_prototype), onScanClick)


    override fun getItemCount(): Int = devicesList.size


    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        val cirDevice = devicesList[position]
        holder.bind(cirDevice)
    }

    /************************************************************************************************/
    /**     VIEW HOLDER                                                                             */
    /************************************************************************************************/
    class ScanViewHolder(itemView: View, private val onScanClickListener: OnScanClickListener) : RecyclerView.ViewHolder(itemView) {

        fun bind(device: CirDevice) = with(itemView) {
            tvScanMac.text      = device.bleDevice.getMac()
            tvDeviceModel.text  = device.bleDevice.getDeviceModelName()

            if (device.getScanPostResponse() != null) {

                tvDeviceModel.text          = "${device.getScanPostResponse()?.assetType} ${device.getScanPostResponse()?.assetModel}"
                tvDeviceSerial.text         = "${context.getText(R.string.serial_number_solkos)} ${device.getScanPostResponse()?.serialNumber}"
                tvDeviceSolkosModel.text    = "${context.getText(R.string.model_solkos)} ${device.getScanPostResponse()?.assetModel}"
                tvDeviceType.text           = "${context.getText(R.string.type_solkos)} ${device.getScanPostResponse()?.assetType}"

                var transmiting = context.getString(R.string.transmitting)

                if (!device.getScanPostResponse()!!.isTransmitting)
                    transmiting = context.getString(R.string.no_transmission)

                tvDeviceTransmition.text    = "${context.getText(R.string.transmition_solkos)} $transmiting"

                itemView.setOnClickListener { onScanClickListener.onCirSelected(device, true) }

            } else {
                this.setBackgroundColor(resources.getColor(R.color.colorBackShadow))
                tvDeviceSerial.visibility       = View.GONE
                tvDeviceSolkosModel.visibility  = View.GONE
                tvDeviceType.visibility         = View.GONE
                tvDeviceTransmition.visibility  = View.GONE
                itemView.setOnClickListener { onScanClickListener.onCirSelected(device, false) }
            }
        }

//        override fun onClick(v: View?) {
//            onScanClick. onScanClickListener(adapterPosition, v?.tvDeviceSerial!!.isVisible)
//        }

    }


    /************************************************************************************************/
    /**     INTERFACES                                                                              */
    /************************************************************************************************/
    interface OnScanClickListener {
//        fun onScanClickListener(position: Int, allowedDevice: Boolean)
        fun onCirSelected(cirSelected: CirDevice?, allowedDevice: Boolean)
    }

}