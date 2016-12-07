package com.example.user.creditkeyboard.customview;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

/**
 * Created by feilong.guo on 16/12/7.
 */

public class WithdrawAmountTextWatcher implements TextWatcher {
    private static final String TAG = "WithdrawAmountTextWatch";
    private EditText editText;
    private String beforTC;
    private String changeTC;
    private String afterTC;

    private String currentInputDesc;
    /**
     * 要保留的小数位数
     */
    private int keepLength;

    public WithdrawAmountTextWatcher(EditText editText, int keepLength) {
        this.keepLength = keepLength;
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        beforTC = s.toString();
        Log.e(TAG, "beforeTextChanged: --->beforTC--->" + beforTC);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        changeTC = s.toString();
        Log.e(TAG, "onTextChanged: --->changeTC--->" + changeTC + "---count--->" + count);


        if (changeTC.length() >= 1) {
            currentInputDesc = changeTC.charAt(changeTC.length() - 1) + "";
            if (TextUtils.isEmpty(beforTC)) {
                if (currentInputDesc.equals(".")) {
                    editText.setText("");
                }
            }

            if (beforTC.equals("0")) {
                if (!currentInputDesc.equals(".")) {
                    editText.setText(beforTC);
                    editText.setSelection(beforTC.length() - 1);
                }
            }

            if (!TextUtils.isEmpty(changeTC)) {
                String lastChar = String.valueOf(changeTC.charAt(changeTC.length() - 1));
                String firstChar = String.valueOf(changeTC.charAt(0));
                if (changeTC.contains(".") && !lastChar.equals(".") && !firstChar.equals(".")) {
                    String[] split = changeTC.split("\\.");
                    String s1 = split[0];
                    String s2 = split[1];
                    String s3 = "";
                    if (s2.length() > keepLength) {
                        s3 = s2.substring(0, keepLength);
                        editText.setText(s1 + "." + s3);
                        editText.setSelection(changeTC.length());
                    }
                }
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        afterTC = s.toString();
        Log.e(TAG, "afterTextChanged: --->afterTC--->" + afterTC);
    }
}
