package mx.softel.cirwireless.log_in_module.web_service;

import org.json.JSONException;
import org.json.JSONObject;

import mx.softel.cirwireless.bootloader_db.UserModel;
import mx.softel.cirwireless.log_in_module.web_service.web_service_enums.ApplicationIdentifiers;


public class LoginWebServiceUtils {

    public static JSONObject getLoginJson (UserModel user, ApplicationIdentifiers identifier) {
        JSONObject loginJson = new JSONObject();

        try {

            loginJson.put("email", user.getEmail());
            loginJson.put("app", identifier.toString());
            loginJson.put("code", user.getCode());

        } catch (JSONException e) {
            e.printStackTrace();
            loginJson = null;
        }

        return loginJson;
    }
}
