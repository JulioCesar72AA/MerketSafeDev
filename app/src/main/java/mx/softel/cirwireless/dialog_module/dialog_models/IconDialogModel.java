package mx.softel.cirwireless.dialog_module.dialog_models;

import android.view.animation.Animation;

public class IconDialogModel extends BaseDialogModel{

    private int imageResource;
    private Animation animation;

    public IconDialogModel(int imageResource, String title, String message, Animation animation) {
        super(title, message);
        this.imageResource  = imageResource;
        this.animation      = animation;
    }

    // Getters -------------------------------------------------------------------------------------
    public int getImageResource() { return imageResource; }


    public Animation getAnimation () { return animation; }
    // ---------------------------------------------------------------------------------------------
}
