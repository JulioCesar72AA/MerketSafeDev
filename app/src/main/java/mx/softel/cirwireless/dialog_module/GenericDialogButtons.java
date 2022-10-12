package mx.softel.cirwireless.dialog_module;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Objects;

import mx.softel.cirwireless.R;
import mx.softel.cirwireless.dialog_module.dialog_interfaces.DialogInteractor;
import mx.softel.cirwireless.dialog_module.dialog_models.BaseDialogModel;
import mx.softel.cirwireless.log_in_module.ui_login.log_in_dialog.DialogButtonsModel;


public class GenericDialogButtons extends Dialog {
    private static final String TAG = "GenericDialogButtons";

    private Context ctx;
    private DialogInteractor dialogInteractor;
    private BaseDialogModel baseDialogModel;


    public GenericDialogButtons(@NonNull Context context, BaseDialogModel baseDialogMode, DialogInteractor dialogInteractor) {
        super(context);
        this.ctx                = context;
        this.baseDialogModel    = baseDialogMode;
        this.dialogInteractor   = dialogInteractor;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(baseDialogModel.getIdLayoutDialogResource());
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getElements();
    }


    private void getElements () {

        // Se pueden agregar mas dialogos, tienes que contener botones
        switch (baseDialogModel.getIdLayoutDialogResource()) {
            case R.layout.login_error_dialog :
                ConstraintLayout clLoginDialogContainer = (ConstraintLayout) findViewById(R.id.cl_login_error_container);
                TextView tvErrorTitle                   = (TextView) findViewById(R.id.tv_login_error_title);
                TextView tvErrorMessage                 = (TextView) findViewById(R.id.tv_login_error_message);
                Button btnErrorAccept                   = (Button) findViewById(R.id.btn_login_error_accept);
                DialogButtonsModel dialogButtonModel    = (DialogButtonsModel) baseDialogModel;
                tvErrorTitle.setText(dialogButtonModel.getTitle());
                tvErrorMessage.setText(dialogButtonModel.getMessage());
                btnErrorAccept.setText(dialogButtonModel.getNegativeBtnText());

                btnErrorAccept.setOnClickListener(view -> this.dialogInteractor.negativeClick(this));

                setAnimation(ctx, clLoginDialogContainer, R.anim.scale);
                break;
        }
    }


    public static void setAnimation (Context ctx, View mView, int mAnimation) {
        Animation mSetAnimation = AnimationUtils.loadAnimation(ctx, mAnimation);
        mSetAnimation.setFillAfter(true);
        mView.startAnimation(mSetAnimation);
    }
}
