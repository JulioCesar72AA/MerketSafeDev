package mx.softel.marketsafe.dialog_module.dialog_interfaces;

import mx.softel.marketsafe.dialog_module.GenericDialogButtons;

public interface DialogInteractor {
    void positiveClick(GenericDialogButtons dialog);

    void negativeClick(GenericDialogButtons dialog);
}