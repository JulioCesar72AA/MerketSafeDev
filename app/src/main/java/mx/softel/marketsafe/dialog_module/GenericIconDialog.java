package mx.softel.marketsafe.dialog_module;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import mx.softel.marketsafe.R;
import mx.softel.marketsafe.dialog_module.dialog_models.IconDialogModel;

public class GenericIconDialog extends DialogFragment {

    private ImageView ivDialogIcon;

    private TextView tvDialogTitle;
    private TextView tvDialogMessage;

    private IconDialogModel model;


    public GenericIconDialog (IconDialogModel model) { this.model = model; }


    // Lifecycle -----------------------------------------------------------------------------------
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.generic_icon_dialog, container);
        getElements(view);
        setElements();
        setAnimation();
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // this.getDialog().getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_white);
    }


    @Override
    public void onResume() {
        super.onResume();
        initializeProperties();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }
    // ---------------------------------------------------------------------------------------------



    private void getElements (View view) {
        ivDialogIcon    = view.findViewById(R.id.iv_generic_icon_dialog_icon);
        tvDialogTitle   = view.findViewById(R.id.tv_generic_icon_dialog_title);
        tvDialogMessage = view.findViewById(R.id.tv_generic_icon_dialog_message);
    }


    public TextView getTvDialogTitle () { return tvDialogTitle; }


    public TextView getTvDialogMessage () { return tvDialogMessage; }


    public ImageView getIvDialogIcon () { return ivDialogIcon; }


    private void setElements() {
        ivDialogIcon.setImageResource(model.getImageResource());
        tvDialogTitle.setText(model.getTitle());
        tvDialogMessage.setText(model.getMessage());
    }


    private void setAnimation () {
        Animation animation = model.getAnimation();
        if (animation != null) {
            animation.setFillAfter(true);
            ivDialogIcon.startAnimation(animation);
        }
    }


    private void initializeProperties () {
        Dialog dialog = getDialog();

        if (dialog != null && dialog.getWindow() != null) {
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
