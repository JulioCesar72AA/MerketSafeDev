package mx.softel.cirwireless.web_services_module.ui_login.log_in_dialog;


import mx.softel.cirwireless.dialog_module.dialog_models.BaseDialogModel;

public class DialogButtonsModel extends BaseDialogModel {

    private String positiveBtnText;
    private String negativeBtnText;

    private int btnPositiveVisibility;
    private int btnNegativeVisibility;

    public DialogButtonsModel(int idLayoutDialogResource, int idImgResource,
                              String title, String message,
                              String positiveBtnText, String negativeBtnText,
                              int btnPositiveVisibility, int btnNegativeVisibility) {
        super(idLayoutDialogResource, idImgResource, title, message);
        this.positiveBtnText        = positiveBtnText;
        this.negativeBtnText        = negativeBtnText;
        this.btnPositiveVisibility  = btnPositiveVisibility;
        this.btnNegativeVisibility  = btnNegativeVisibility;
    }


    public String getNegativeBtnText() { return negativeBtnText; }

    public String getPositiveBtnText() { return positiveBtnText; }

    public int getBtnNegativeVisibility() { return btnNegativeVisibility; }

    public int getBtnPositiveVisibility() { return btnPositiveVisibility; }
}
