package mx.softel.marketsafe.dialog_module.dialog_models;

public class BaseDialogModel {

    private int     idLayoutDialogResource;
    private int     idImgResource;

    private String title;
    private String message;


    public BaseDialogModel(int idLayoutDialogResource, int idImgResource, String title, String message) {
        this.idLayoutDialogResource = idLayoutDialogResource;
        this.idImgResource          = idImgResource;
        this.title                  = title;
        this.message                = message;
    }


    public BaseDialogModel(String title, String message) {
        this.title      = title;
        this.message    = message;
    }


    // Getters -------------------------------------------------------------------------------------
    public int getIdLayoutDialogResource() { return idLayoutDialogResource; }

    public int getIdImgResource() { return idImgResource; }

    public String getTitle() { return title; }

    public String getMessage() { return message; }
    // ---------------------------------------------------------------------------------------------
}