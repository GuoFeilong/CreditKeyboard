package com.example.user.creditkeyboard.customview;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 身份证的的输入框监听
 * <p>
 * Created by feilong.guo on 16/12/7.
 */

public class AuthIDCardTextWatcher implements TextWatcher {
    private static final String TAG = "AuthIDCardTextWatcher";
    private EditText editText;
    private String beforTC;
    private String changeTC;
    private String afterTC;

    private String currentInputDesc;

    public AuthIDCardTextWatcher(EditText editText) {
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

        // 二代身份证的规则,只能输入18数字或者 17 位数字 + 字母X
        if (changeTC.length() >= 1 && changeTC.length() < 18) {
            currentInputDesc = changeTC.charAt(changeTC.length() - 1) + "";
            if (!isNum(currentInputDesc)) {
                editText.setText(beforTC);
            }
        }
        if (changeTC.length() == 18) {
            currentInputDesc = changeTC.charAt(changeTC.length() - 1) + "";
            if (!isNum(currentInputDesc) && !currentInputDesc.equals("x") && !currentInputDesc.equals("X")) {
                editText.setText(beforTC);
            }
            editText.setSelection(editText.getText().length() );
        }

    }

    @Override
    public void afterTextChanged(Editable s) {
        afterTC = s.toString();
        Log.e(TAG, "afterTextChanged: --->afterTC--->" + afterTC);
    }

    /**
     * 判断是否是数字
     *
     * @param sourth 字符串
     * @return 结果
     */
    private boolean isNum(String sourth) {
        String pattern = "[0-9]";
        Pattern compile = Pattern.compile(pattern);
        Matcher matcher = compile.matcher(sourth);
        return matcher.find();
    }
}
