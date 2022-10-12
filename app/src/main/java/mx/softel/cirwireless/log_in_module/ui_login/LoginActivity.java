package mx.softel.cirwireless.log_in_module.ui_login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import mx.softel.cirwireless.R;
import mx.softel.cirwireless.bootloader_db.BootloaderDatabase;
import mx.softel.cirwireless.bootloader_db.UserModel;
import mx.softel.cirwireless.dialog_module.GenericDialogButtons;
import mx.softel.cirwireless.dialog_module.dialog_interfaces.DialogInteractor;
import mx.softel.cirwireless.dialog_module.dialog_models.BaseDialogModel;
import mx.softel.cirwireless.log_in_module.ui_login.log_in_dialog.DialogButtonsModel;
import mx.softel.cirwireless.log_in_module.web_service.LoginWebService;
import mx.softel.cirwireless.log_in_module.web_service.LoginWebServiceUtils;
import mx.softel.cirwireless.log_in_module.web_service.URLModel;
import mx.softel.cirwireless.log_in_module.web_service.web_service_enums.ApplicationIdentifiers;
import mx.softel.cirwireless.log_in_module.web_service.web_service_enums.LoginAvailableUrl;
import mx.softel.cirwireless.log_in_module.web_service.web_service_enums.WebServiceStatusExecutionCodes;
import mx.softel.cirwireless.log_in_module.web_service.web_service_enums.WebServiceStatusInstallationCodes;
import mx.softel.cirwireless.utils.ElapsedDate;
import mx.softel.cirwireless.utils.Utils;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private static final int MAX_EXPIRATION_DAYS = 90; // Days (3 Months)

    private boolean wereUserInfoInDb;

    private EditText etUserEmail;
    private Button btnAccept;
    private LinearLayout llCourtain;

    private BootloaderDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        getElements();
        setListeners();
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkLocalUserData();
    }


    private void getElements () {
        db          = new BootloaderDatabase(this);
        etUserEmail = findViewById(R.id.et_email);
        btnAccept   = findViewById(R.id.btn_login);
        llCourtain  = findViewById(R.id.ll_courtain);
    }


    private void setListeners () {
        btnAccept.setOnClickListener(view -> {
            String userEmailStr = etUserEmail.getText().toString();

            if (!userEmailStr.matches("") && Utils.isEmailValid(userEmailStr)) {

                String macAddress       = Utils.getWifiMacAddress(LoginActivity.this);
                String currentDate      = Utils.getTimeandDate();
                UserModel user          = new UserModel(userEmailStr, currentDate, macAddress, Utils.getUUID());

                if (Utils.isNetworkAvailable(LoginActivity.this)) {

                    JSONObject jsonUserData = LoginWebServiceUtils.getLoginJson(user, ApplicationIdentifiers.SFTLAPPS2100005);
                    checkCloudUserData(new URLModel(LoginAvailableUrl.INSTALLATION_URL), jsonUserData, user);

                } else {

                    Utils.showToastShort(LoginActivity.this, getString(R.string.network_connection_neccessary));
                }

            } else
                Utils.showToastShort(LoginActivity.this, getString(R.string.invalid_info));

        });
    }


    // Revisamos si existe un usuario de manera local
    private void checkLocalUserData () {
        db.getUser(data -> {
            if (data != null) {
                UserModel user = (UserModel) data;

                if (user.isAValidUser()) {
                    try {
                        wereUserInfoInDb = true;
                        ElapsedDate elapsedDate = Utils.getDatesDifference(
                                Utils.getDefaultDateFormat().parse(user.getAccessDate()),
                                Utils.getDefaultDateFormat().parse(Utils.getTimeandDate())
                        );

                        // Log.e(TAG, "elapsedDate:DAYS: " + elapsedDate.getElapsedDays());
                        // Log.e(TAG, "elapsedDate:HOURS: " + elapsedDate.getElapsedHours());
                        // Log.e(TAG, "elapsedDate:MINUTES: " + elapsedDate.getElapsedMinutes());
                        // Log.e(TAG, "elapsedDate:SECONDS: " + elapsedDate.getElapsedSeconds());
                        // Log.e(TAG, "SAVED DATE: " + user.getAccessDate());

                        if (Utils.isNetworkAvailable(LoginActivity.this)) {

                            // Revisamos que siga activo el usuario en Cloud
                            JSONObject jsonUserData = LoginWebServiceUtils.getLoginJson(user, ApplicationIdentifiers.SFTLAPPS2100005);
                            checkCloudUserData(new URLModel(LoginAvailableUrl.EXECUTION_URL), jsonUserData, user);

                        } else if (elapsedDate.getElapsedDays() < MAX_EXPIRATION_DAYS) {
                            // TODO: Colocar a donde ir
//                            startActivity(new Intent(LoginActivity.this, ScanningActivity.class));
//                            finish();

                        } else {

                            uiHideCourtain();
                            Utils.showToastShort(LoginActivity.this, getString(R.string.login_is_neccesary));
                        }


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else {

                    db.deleteUserTable();
                    uiHideCourtain();
                    Utils.showToastShort(LoginActivity.this, getString(R.string.no_data_found));
                }

            } else {

                db.deleteUserTable();
                uiHideCourtain();
                Utils.showToastShort(LoginActivity.this, getString(R.string.no_data_found));
            }
        });
    }


    // Validamos el usuario en la nube
    private void checkCloudUserData (URLModel urlModel, JSONObject jsonData, UserModel user) {
        Utils.showToastShort(LoginActivity.this, getString(R.string.checking_info));
        uiShowCourtain();

        new LoginWebService(urlModel, jsonData, new LoginWebService.IServerConnectable() {
            @Override
            public void serverOk(JSONObject response, URLModel urlModel) {
                try {


                    int statusCode = response.getInt("status");

                    switch (urlModel.getUrl()) {
                        case INSTALLATION_URL :
                            if (statusCode == WebServiceStatusInstallationCodes.USER_ALLOWED.ordinal()) {
                                db.deleteUserTable();

                                db.insertUser(user, (data, wasWritten) -> {
                                    if (wasWritten) {
                                        Utils.showToastShort(LoginActivity.this, getString(R.string.access_granted));
                                        goToMainActivity();
                                    }
                                });

                            } else if (statusCode == WebServiceStatusInstallationCodes.USER_ALREADY_LOGGED_IN.ordinal() ||
                                    statusCode == WebServiceStatusInstallationCodes.USER_NOT_ALLOWED.ordinal()) {
                                db.deleteUserTable();

                                BaseDialogModel baseDialogModel = new DialogButtonsModel(R.layout.login_error_dialog, -1,
                                        getString(R.string.access_ungranted),
                                        getString(R.string.contact_administrator) + "\nCode: " + statusCode,
                                        "",
                                        getString(R.string.accept),
                                        View.VISIBLE,
                                        View.VISIBLE);

                                GenericDialogButtons dialog = new GenericDialogButtons(
                                        LoginActivity.this,
                                        baseDialogModel,
                                        new DialogInteractor() {
                                            @Override
                                            public void positiveClick(GenericDialogButtons dialog) { }

                                            @Override
                                            public void negativeClick(GenericDialogButtons dialog) { dialog.dismiss(); }
                                        });

                                uiHideCourtain();
                                etUserEmail.setText("");
                                dialog.show();

                            } else {

                                uiHideCourtain();
                                Utils.showToastShort(LoginActivity.this, getString(R.string.something_went_wrong));
                            }

                            break;

                        case EXECUTION_URL :
                            if  (statusCode == WebServiceStatusExecutionCodes.USER_ALLOWED.ordinal()) {

                                db.updateAccessDate(Utils.getTimeandDate(), user, (data, wasWritten) -> {
                                    if (wasWritten) {
                                        Utils.showToastShort(LoginActivity.this, getString(R.string.access_granted));
                                        goToMainActivity();
                                    }
                                });

                            } else if (statusCode == WebServiceStatusExecutionCodes.UNKNOWN_USER.ordinal() ||
                                    statusCode == WebServiceStatusExecutionCodes.CODES_NOT_MATCH.ordinal() ||
                                    statusCode == WebServiceStatusExecutionCodes.USER_NOT_ALLOWED.ordinal()) {

                                BaseDialogModel baseDialogModel = new DialogButtonsModel(R.layout.login_error_dialog, -1,
                                        getString(R.string.access_ungranted),
                                        getString(R.string.contact_administrator) + "\nCode: " + statusCode,
                                        "",
                                        getString(R.string.accept),
                                        View.VISIBLE,
                                        View.VISIBLE);

                                GenericDialogButtons dialog = new GenericDialogButtons(
                                        LoginActivity.this,
                                        baseDialogModel,
                                        new DialogInteractor() {
                                            @Override
                                            public void positiveClick(GenericDialogButtons dialog) { }

                                            @Override
                                            public void negativeClick(GenericDialogButtons dialog) { dialog.dismiss(); }
                                        });

                                uiHideCourtain();
                                etUserEmail.setText("");
                                dialog.show();

                            } else {

                                uiHideCourtain();
                                Utils.showToastShort(LoginActivity.this, getString(R.string.something_went_wrong));
                            }

                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void serverError() {
                uiHideCourtain();
                Utils.showToastShort(LoginActivity.this, getString(R.string.service_not_available));
            }
        }).execute();
    }


    private void goToMainActivity () {
//        startActivity(new Intent(LoginActivity.this, ScanningActivity.class));
//        finish();
    }


    private void uiShowCourtain () {
        llCourtain.setVisibility(View.VISIBLE);
        btnAccept.setVisibility(View.INVISIBLE);
    }


    private void uiHideCourtain () {
        llCourtain.setVisibility(View.GONE);
        btnAccept.setVisibility(View.VISIBLE);
    }
}

