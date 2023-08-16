package mx.softel.marketsafe.web_services_module.ui_login;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.text.ParseException;

import javax.net.ssl.HttpsURLConnection;

import mx.softel.marketsafe.R;
import mx.softel.marketsafe.activities.MainActivity;
import mx.softel.marketsafe.web_services_module.SolkosServerResponse;
import mx.softel.marketsafe.wifi_db.WifiDatabase;
import mx.softel.marketsafe.wifi_db.UserModel;
import mx.softel.marketsafe.dialog_module.GenericDialogButtons;
import mx.softel.marketsafe.dialog_module.dialog_interfaces.DialogInteractor;
import mx.softel.marketsafe.dialog_module.dialog_models.BaseDialogModel;
import mx.softel.marketsafe.web_services_module.ui_login.log_in_dialog.DialogButtonsModel;
import mx.softel.marketsafe.web_services_module.web_service.LoginWebService;
import mx.softel.marketsafe.web_services_module.web_service.LoginWebServiceUtils;
import mx.softel.marketsafe.web_services_module.web_service.URLModel;
import mx.softel.marketsafe.web_services_module.web_service.web_service_enums.AvailableUrl;
import mx.softel.marketsafe.web_services_module.web_service.web_service_enums.WebServiceStatusInstallationCodes;
import mx.softel.marketsafe.utils.ElapsedDate;
import mx.softel.marketsafe.utils.Utils;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private static final int MAX_EXPIRATION_DAYS = 90; // Days (3 Months)

    private boolean wereUserInfoInDb;

    private EditText etUserEmail;
    private Button btnAccept;
    private LinearLayout llCourtain;

    private WifiDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getElements();
        setListeners();
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkLocalUserData();
    }


    private void getElements () {
        db          = new WifiDatabase(this);
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
                String androidId        = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                UserModel user          = new UserModel(userEmailStr, currentDate, macAddress, androidId);

                if (Utils.isNetworkAvailable(LoginActivity.this)) {
                    JSONObject jsonUserData = LoginWebServiceUtils.getLoginJson(user);
                    checkCloudUserData(new URLModel(AvailableUrl.LOGIN_URL), jsonUserData, user);

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
                                Utils.getDefaultDateFormat().parse(user.getAccessDate().replace("T", " ")),
                                Utils.getDefaultDateFormat().parse(Utils.getTimeandDate())
                        );

                        // Log.e(TAG, "elapsedDate:DAYS: " + elapsedDate.getElapsedDays());
                        // Log.e(TAG, "elapsedDate:HOURS: " + elapsedDate.getElapsedHours());
                        // Log.e(TAG, "elapsedDate:MINUTES: " + elapsedDate.getElapsedMinutes());
                        // Log.e(TAG, "elapsedDate:SECONDS: " + elapsedDate.getElapsedSeconds());
                        // Log.e(TAG, "SAVED DATE: " + user.getAccessDate());
                        if (elapsedDate.getElapsedDays() < MAX_EXPIRATION_DAYS) {
                            etUserEmail.setText(user.getEmail());
                            // startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            // finish();

                        } else if (Utils.isNetworkAvailable(LoginActivity.this)) {

                            // Revisamos que siga activo el usuario en Cloud
                            JSONObject jsonUserData = LoginWebServiceUtils.getLoginJson(user);
                            checkCloudUserData(new URLModel(AvailableUrl.LOGIN_URL), jsonUserData, user);

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
            public void serverOk(JSONObject response, URLModel urlModel, int responseCode) {
                try {

                    SolkosServerResponse userSolkosResponse = new SolkosServerResponse(response);

                    switch (urlModel.getUrl()) {
                        case LOGIN_URL :
                            if (responseCode == HttpsURLConnection.HTTP_OK) {
                                db.deleteUserTable();

                                db.insertUser(user, userSolkosResponse, (data, wasWritten) -> {
                                    if (wasWritten) {
                                        Utils.showToastShort(LoginActivity.this, getString(R.string.access_granted));
                                        goToMainActivity();
                                    }
                                });

                            } else if (responseCode == WebServiceStatusInstallationCodes.USER_ALREADY_LOGGED_IN.ordinal() ||
                                    responseCode == WebServiceStatusInstallationCodes.USER_NOT_ALLOWED.ordinal()) {
                                db.deleteUserTable();

                                BaseDialogModel baseDialogModel = new DialogButtonsModel(R.layout.login_error_dialog, -1,
                                        getString(R.string.access_ungranted),
                                        getString(R.string.contact_administrator) + "\nCode: " + responseCode,
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

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void serverError(int responseCode) {
                uiHideCourtain();

                switch (responseCode) {
                    case HttpsURLConnection.HTTP_BAD_REQUEST :
                        Utils.showToastShort(getApplicationContext(), getString(R.string.request_error));
                        break;

                    case HttpsURLConnection.HTTP_NOT_FOUND :

                        Utils.showToastShort(getApplicationContext(), getString(R.string.user_not_found));
                        break;

                    case HttpsURLConnection.HTTP_CONFLICT :

                        Utils.showToastShort(getApplicationContext(), getString(R.string.already_session));
                        break;

                    case HttpsURLConnection.HTTP_INTERNAL_ERROR:

                        Utils.showToastShort(getApplicationContext(), getString(R.string.internal_server_error));
                        break;
                }
                Utils.showToastShort(LoginActivity.this, getString(R.string.service_not_available));
            }
        }).execute();
    }


    private void goToMainActivity () {
        startActivity(new Intent(this, MainActivity.class));
        finish();
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

