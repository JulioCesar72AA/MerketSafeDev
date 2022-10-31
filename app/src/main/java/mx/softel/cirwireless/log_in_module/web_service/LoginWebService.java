package mx.softel.cirwireless.log_in_module.web_service;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class LoginWebService extends AsyncTask<Void, Void, Void> {

    private static final String TAG                     = "LoginWebService";

    private int                 responseCode;

    private URLModel            urlModel;
    private JSONObject          jsonData;
    private JSONObject          response;
    private IServerConnectable  iServerConnectable;

    public LoginWebService(URLModel urlModel, JSONObject jsonData, IServerConnectable iServerConnectable) {
        this.urlModel           = urlModel;
        this.jsonData           = jsonData;
        this.iServerConnectable = iServerConnectable;
    }


    @Override
    protected Void doInBackground(Void... voids) {

        try {
            Log.e(TAG, "url: " + urlModel.getUrlStr());

            URL url = new URL(urlModel.getUrlStr());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(jsonData.toString());
            os.flush();
            os.close();

            Log.e(TAG, "JSON DATA: " + jsonData.toString());
            responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {

                String line = "";
                StringBuilder responseStr = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                while ((line = br.readLine()) != null)
                    responseStr.append(line);

                response = new JSONObject(responseStr.toString());
                Log.e(TAG, "response: " + response);

            }

            else if (responseCode == HttpsURLConnection.HTTP_BAD_REQUEST) response = null;

            else if (responseCode == HttpsURLConnection.HTTP_NOT_FOUND) response = null;

            else if (responseCode == HttpsURLConnection.HTTP_CONFLICT) response = null;


            else if (responseCode == HttpsURLConnection.HTTP_INTERNAL_ERROR) response = null;

            Log.e(TAG, "RESPONSE CODE: " + conn.getResponseCode());
            Log.e(TAG, "RESPONSE_MESSAGE: " + conn.getResponseMessage());

            conn.disconnect();

        } catch (Exception e) {

            e.printStackTrace();
            response = null;

        }

        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (response != null)
            iServerConnectable.serverOk(response, urlModel, responseCode);

        else
            iServerConnectable.serverError(responseCode);
    }

    public interface IServerConnectable {
        void serverOk(JSONObject response, URLModel urlModel, int responseCode);

        void serverError(int responseCode);
    }
}
