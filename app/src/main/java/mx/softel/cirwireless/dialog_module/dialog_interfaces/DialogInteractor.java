package mx.softel.cirwireless.dialog_module.dialog_interfaces;

import mx.softel.bootloadermanual.dialog_module.GenericDialogButtons;

public interface DialogInteractor {
    void positiveClick(GenericDialogButtons dialog);

    void negativeClick(GenericDialogButtons dialog);
}