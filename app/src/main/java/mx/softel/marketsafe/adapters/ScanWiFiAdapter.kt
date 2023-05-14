package mx.softel.marketsafe.adapters

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.scan_devices_prototype.view.*
import kotlinx.android.synthetic.main.scan_wifi_prototype.view.*
import mx.softel.marketsafe.CirDevice
import mx.softel.marketsafe.R
import mx.softel.scanblelib.extensions.inflate

private const val TAG = "ScanWiFiAdapter"

class ScanWiFiAdapter(private val context: Context, private val wiFiList: ArrayList<String>,
                      private val onScanWiFiClickListener: OnScanClickListener)
    : RecyclerView.Adapter<ScanWiFiAdapter.ScanWiFiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanWiFiAdapter.ScanWiFiViewHolder
            = ScanWiFiAdapter.ScanWiFiViewHolder(parent.inflate(R.layout.scan_wifi_prototype), onScanWiFiClickListener)

    override fun onBindViewHolder(holder: ScanWiFiViewHolder, position: Int) = holder.bind(wiFiList[position])

    override fun getItemCount (): Int = wiFiList.size


    /************************************************************************************************/
    /**     VIEW HOLDER                                                                             */
    /************************************************************************************************/
    class ScanWiFiViewHolder(itemView: View, onScanClickListener: OnScanClickListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var onScanWiFiClick: OnScanClickListener

        init {
            itemView.setOnClickListener(this@ScanWiFiViewHolder)
            onScanWiFiClick = onScanClickListener
        }

        fun bind(wiFiName: String) = with(itemView) {
            tvWiFiName.text = wiFiName
        }

        override fun onClick(v: View?) {
            Log.e(TAG, "adapterPosition: $adapterPosition")
            onScanWiFiClick.onScanClickListener(adapterPosition)
        }
    }


    /************************************************************************************************/
    /**     INTERFACES                                                                              */
    /************************************************************************************************/
    interface OnScanClickListener {
        fun onScanClickListener(position: Int)
    }
}