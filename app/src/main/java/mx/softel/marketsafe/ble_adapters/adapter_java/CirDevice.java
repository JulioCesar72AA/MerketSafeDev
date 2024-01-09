package mx.softel.marketsafe.ble_adapters.adapter_java;

import static android.os.Build.VERSION_CODES.R;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class CirDevice extends RecyclerView.ViewHolder {

    private ConstraintLayout prototypeContainer;
    private TextView tvCirMac;
    private TextView tvCirDescription;
    private TextView tvCirRssi;

    public CirDevice(@NonNull View itemView) {
        super(itemView);
       // getElements();
    }

//    private void getElements () {
//        prototypeContainer  = itemView.findViewById(R.id.cl_cir_prototype);
//        tvCirMac            = itemView.findViewById(R.id.tv_cir_mac);
//        tvCirDescription    = itemView.findViewById(R.id.tv_cir_description);
//        tvCirRssi           = itemView.findViewById(R.id.tv_cir_rssi_value);
//    }

    // Setters ---------------
    public void setTvCirMac(String mac) { tvCirMac.setText(mac);}

    public void setTvCirDescription(String cirDescription) { tvCirDescription.setText(cirDescription); }

    public void setTvCirRssi (String cirRssi) { tvCirRssi.setText(cirRssi); }
    // -----------------------

    // Getters ---------------
    public String getCirMac () { return this.tvCirMac.getText().toString(); }

    public ConstraintLayout getPrototypeContainer () { return this.prototypeContainer; }

    public String getCirRssi () { return this.tvCirRssi.getText().toString(); }
    // -----------------------
}
