package mx.softel.cirwireless.log_in_module.web_service;


import mx.softel.cirwireless.log_in_module.web_service.web_service_enums.LoginAvailableUrl;

public class URLModel {
    private static final String LOGIN_URL_INSTALLATION  = "https://auth-app-refri-iot-dot-refrigeracioniot.uc.r.appspot.com/appAuthInstall";
    private static final String LOGIN_URL_EXECUTION     = "https://auth-app-refri-iot-dot-refrigeracioniot.uc.r.appspot.com/appAuthExec";

    private String urlStr;
    private LoginAvailableUrl url;

    public URLModel (LoginAvailableUrl url) {
        this.url    =  url;
        this.urlStr = (url ==  LoginAvailableUrl.INSTALLATION_URL) ?  LOGIN_URL_INSTALLATION : LOGIN_URL_EXECUTION;
    }


    // Getters ---------------------------------------------
    public String getUrlStr() { return urlStr; }


    public LoginAvailableUrl getUrl() { return url; }
    // -----------------------------------------------------
}
