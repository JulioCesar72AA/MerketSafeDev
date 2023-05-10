package mx.softel.marketsafe.wifi_db;

public class UserModel {

    private String email;
    private String accessDate;
    private String macWifi;
    private String code;


    public UserModel(String email, String accessDate, String macWifi, String code) {
        this.email      = email;
        this.accessDate = accessDate;
        this.macWifi    = macWifi;
        this.code       = code;
    }


    // Getters -------------------------------------------------------------------------------------
    public String getEmail() { return email; }


    public String getAccessDate() { return accessDate; }


    public String getMacWifi() { return macWifi; }


    public String getCode() { return code; }
    // ---------------------------------------------------------------------------------------------


    // Setters -------------------------------------------------------------------------------------
    public void setAccessDate(String accessDate) { this.accessDate = accessDate; }
    // ---------------------------------------------------------------------------------------------


    public boolean isAValidUser () { return (getEmail() != null && getAccessDate() != null && getMacWifi() != null); }
}
