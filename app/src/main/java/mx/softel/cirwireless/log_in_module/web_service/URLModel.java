package mx.softel.cirwireless.log_in_module.web_service;


import mx.softel.cirwireless.log_in_module.web_service.web_service_enums.AvailableUrl;

public class URLModel {
    private static final String LOGIN_URL_SOLKOS_BASE   = "https://cir-wifi-interface-b7agk5thba-uc.a.run.app";
    private static final String LOGIN_URL_SOLKOS_LOGIN  = "https://cir-wifi-interface-b7agk5thba-uc.a.run.app/login";
    private static final String SCAN_URL_SOLKOS_FRIDGES = "https://cir-wifi-interface-b7agk5thba-uc.a.run.app/assets/scan";

    private String urlStr;
    private AvailableUrl url;

    public URLModel (AvailableUrl url) {
        this.url    =  url;
        switch (url) {
            case LOGIN_URL:
                this.urlStr = LOGIN_URL_SOLKOS_LOGIN;
                break;

            case SCAN_ALLOWED_FRIDGES:
                this.urlStr = SCAN_URL_SOLKOS_FRIDGES;
                break;

            default: break;
        }
    }


    // Getters ---------------------------------------------
    public String getUrlStr() { return urlStr; }


    public AvailableUrl getUrl() { return url; }
    // -----------------------------------------------------
}
