package com.example.user.creditkeyboard.customview.keyboard.creditkeyboard;

import android.app.Activity;

import com.example.user.creditkeyboard.R;
import com.example.user.creditkeyboard.customview.keyboard.KeyboardBinder;

/**
 * Created by user on 16/9/22.
 */

public class CreditKeyboardBinder extends KeyboardBinder {

    public CreditKeyboardBinder(Activity activity) {
        super(activity);
    }

    @Override
    protected int getKeyboardLayoutResId() {
        return R.layout.payment_number_keyboard;
    }

    @Override
    protected int getKeyboardResId() {
        return R.xml.payment_keyboard_crdit;
    }

    public boolean intercepteBackKeyDown() {
        if (isShow) {
            dismiss();
            return true;
        }
        return false;
    }
}
