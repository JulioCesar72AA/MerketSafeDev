package mx.softel.marketsafe.web_services_module.web_service;

import org.json.JSONException;
import org.json.JSONObject;

import mx.softel.marketsafe.wifi_db.UserModel;


public class LoginWebServiceUtils {

    public static JSONObject getLoginJson (UserModel user) {
        JSONObject loginJson = new JSONObject();

        try {

            loginJson.put("email", user.getEmail());
            loginJson.put("user_device_id_type", "SOFTEL_ID");
            loginJson.put("user_device_id", user.getCode());

        } catch (JSONException e) {
            e.printStackTrace();
            loginJson = null;
        }

        return loginJson;
    }
}
