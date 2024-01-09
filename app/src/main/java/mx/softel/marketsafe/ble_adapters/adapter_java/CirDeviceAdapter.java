package mx.softel.marketsafe.ble_adapters.adapter_java;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mx.softel.marketsafe.R;
import mx.softel.marketsafe.adapters.ScanRecyclerAdapter;
import mx.softel.scanblelib.ble.BleDevice;

public class CirDeviceAdapter extends RecyclerView.Adapter <CirDevice> {

    private static final String TAG = "CirDeviceAdapter";
    private ArrayList <BleDevice> cirsFound;
    private ICirAdapter cirsFoundInterface;

    public CirDeviceAdapter(ArrayList<BleDevice> cirsFound, ICirAdapter cirsFoundInterface) {
        this.cirsFound          = cirsFound;
        this.cirsFoundInterface = cirsFoundInterface;
    }

    @NonNull
    @Override
    public CirDevice onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_devices_prototype, parent, false);
        return new CirDevice(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CirDevice holder, int position) {
        final BleDevice device = cirsFound.get(position);
        //Log.e(TAG, "BeaconBytes: " + Utils.getHexValue(device.getBeaconDevice().getBytes()));
        //Log.e(TAG, "Beacon type: " + device.getBeaconModel().getBeaconType());
        //Log.e(TAG, "Beacon: " + device.getMacDevice());

        if( device.getDeviceModelName() != null ) holder.setTvCirDescription( device.getName() );

        else holder.setTvCirDescription("NA");

        holder.setTvCirMac(device.getMac());
        holder.setTvCirRssi(String.valueOf(device.getRssi()));
        holder.getPrototypeContainer().setOnClickListener(view -> cirsFoundInterface.onCirSelected(device));
    }

    @Override
    public int getItemCount() {
        return (cirsFound != null) ? cirsFound.size() : 0;
    }

    public interface ICirAdapter {
        void onCirSelected (BleDevice cirSelected);
    }
}
