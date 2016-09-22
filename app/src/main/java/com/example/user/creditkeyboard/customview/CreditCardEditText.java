package com.example.user.creditkeyboard.customview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.example.user.creditkeyboard.R;

import java.util.Calendar;

/**
 * Created by user on 16/9/22.
 */

public class CreditCardEditText extends EditText implements View.OnFocusChangeListener, TextWatcher {
    private static final String BACK_SLASH = "/";
    private static final String ZERO = "0";
    private static final String ONE = "1";
    private static final String TWO = "2";
    private static final int MAX_YEAR = 15;

    private String creditViewText;
    private String beforeTC;
    private String currentInputDesc;
    private Calendar calendar;
    private int currentYear;
    private int upYearLimit;
    private String currentY;
    private String upYearLY;
    private String recordFirstYearNum;
    private int yearMinValue;
    private int yearMaxValue;


    private Drawable mClearDrawable;
    private boolean hasFoucs;

    public CreditCardEditText(Context context) {
        this(context, null);
    }

    public CreditCardEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public CreditCardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCreditLimitData();
        init();
    }

    /**
     * 初始化信用卡有效期相关限制
     */
    private void initCreditLimitData() {
        calendar = Calendar.getInstance();
        // 计算信用卡年限逻辑
        currentYear = calendar.get(Calendar.YEAR);
        upYearLimit = currentYear + MAX_YEAR;
        currentY = currentYear + "";
        upYearLY = upYearLimit + "";

        String yearMin = currentY.charAt(currentY.length() - 2) + "" + currentY.charAt(currentY.length() - 1);
        yearMinValue = Integer.parseInt(yearMin);
        String yearMax = upYearLY.charAt(upYearLY.length() - 2) + "" + upYearLY.charAt(upYearLY.length() - 1);
        yearMaxValue = Integer.parseInt(yearMax);
    }

    private void init() {
        mClearDrawable = getCompoundDrawables()[2];
        if (mClearDrawable == null) {
            mClearDrawable = getResources().getDrawable(R.mipmap.icon_delete);
        }

        mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(), mClearDrawable.getIntrinsicHeight());
        setClearIconVisible(false);
        setOnFocusChangeListener(this);
        addTextChangedListener(this);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawables()[2] != null) {
                boolean touchable = event.getX() > (getWidth() - getTotalPaddingRight()) && (event.getX() < ((getWidth() - getPaddingRight())));
                if (touchable) {
                    this.setText("");
                }
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        this.hasFoucs = hasFocus;
        if (hasFocus) {
            setClearIconVisible(getText().length() > 0);
        } else {
            setClearIconVisible(false);
        }
    }

    protected void setClearIconVisible(boolean visible) {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        if (hasFoucs) {
            setClearIconVisible(s.length() > 0);
        }
        // TODO: 16/9/22 删除的时候如果最后一位是反斜杠 / 则一下子删除两个字符
        // 输入的时候进入逻辑判断
        if (count != 0) {
            String temp = s.toString();
            if (temp.length() >= 1) {
                currentInputDesc = temp.charAt(temp.length() - 1) + "";
            }

            if (ZERO.equals(beforeTC)) {
                if (ZERO.equals(currentInputDesc)) {
                    // 第一位是零,输入的数字也是零,则设置为0
                    setText(ZERO);
                    setSelection(ZERO.length());
                } else {
                    // 第一位是零,输入的数字不是零,补全斜杠
                    String aTemp = s.toString() + BACK_SLASH;
                    setText(aTemp);
                    setSelection(aTemp.length());
                }
            }

            if (TextUtils.isEmpty(beforeTC)) {
                if (!currentInputDesc.equals(ZERO) && !currentInputDesc.equals(ONE)) {
                    // 直接输入数字
                    String aTemp = ZERO + s.toString() + BACK_SLASH;
                    setText(aTemp);
                    setSelection(aTemp.length());
                }
            }

            if (ONE.equals(beforeTC)) {
                // 之前等于1
                if (currentInputDesc.equals(ZERO) || currentInputDesc.equals(ONE) || currentInputDesc.equals(TWO)) {
                    // 并且输入的第二位数字满足 0,1,2月份要求
                    String aTemp = ONE + currentInputDesc + BACK_SLASH;
                    setText(aTemp);
                    setSelection(aTemp.length());
                } else {
                    setText(ONE);
                    setSelection(ONE.length());
                }
            }
            creditViewText = getText().toString();
            Log.e("TAG", "--->>>onTextChanged-->>creditViewText-->>" + creditViewText);

            // 输入的文字对年份第一位进行逻辑判断
            if (beforeTC.endsWith(BACK_SLASH)) {
                try {
                    // 计算年份第一位的上下限
                    int currentCY = Integer.parseInt(currentY.charAt(currentY.length() - 2) + "");
                    int upCY = Integer.parseInt(upYearLY.charAt(upYearLY.length() - 2) + "");
                    // 当前输入的数字
                    int currentInputNum = Integer.parseInt(currentInputDesc);
                    if (!(currentInputNum >= currentCY && currentInputNum <= upCY)) {
                        setText(beforeTC);
                        setSelection(creditViewText.length());
                    } else {
                        recordFirstYearNum = currentInputDesc;
                    }
                } catch (Exception e) {
                    Log.e("TAG", e.toString());
                }
            }

            if (creditViewText.length() == 5) {
                // 输入的文字对于年份的第二位进行逻辑判断
                Log.e("TAG", "--->>>onTextChanged-->>creditViewText-->>" + creditViewText + "-->>beforeTC-->>" + beforeTC + "-->>s.toString()-->>" + s.toString());
                String secondYearNum = creditViewText.charAt(creditViewText.length() - 1) + "";
                Log.e("TAG", "设置第二位年份数字记录上一个recordFirstYearNum-->" + recordFirstYearNum + "secondYearNum-->>" + secondYearNum);
                String tempLast2Y = recordFirstYearNum + secondYearNum;
                int last2YearValue = Integer.parseInt(tempLast2Y);
                // 输入的年份第二位数字>=最小值 小于等于最大值
                if (!(last2YearValue >= yearMinValue && last2YearValue <= yearMaxValue)) {
                    setText(beforeTC);
                    setSelection(creditViewText.length());
                }
            }

            if (creditViewText.length() > 5) {
                // 控制输出
                setText(beforeTC);
                setSelection(creditViewText.length());
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        beforeTC = s.toString();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }


}
