package com.example.user.creditkeyboard.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.example.user.creditkeyboard.R;
import com.example.user.creditkeyboard.customview.CreditCardEditText;
import com.example.user.creditkeyboard.customview.keyboard.creditkeyboard.CreditKeyboardBinder;

import java.io.File;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final String BACK_SLASH = "/";
    private static final String ZERO = "0";
    private static final String ONE = "1";
    private static final String TWO = "2";
    private static final int MAX_YEAR = 15;

    private CreditKeyboardBinder creditKeyboardBinder;
    private CreditCardEditText editText;
    private EditText creditSystemNum;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        calendar = Calendar.getInstance();


        editText = (CreditCardEditText) findViewById(R.id.et_test);
        creditSystemNum = (EditText) findViewById(R.id.et_system_num);
        creditKeyboardBinder = new CreditKeyboardBinder(this);
        creditKeyboardBinder.registerEditText(editText);

        // 计算信用卡年限逻辑
        creditCardLogic(creditSystemNum);
        currentYear = calendar.get(Calendar.YEAR);
        upYearLimit = currentYear + MAX_YEAR;

        currentY = currentYear + "";
        upYearLY = upYearLimit + "";

        String yearMin = currentY.charAt(currentY.length() - 2) + "" + currentY.charAt(currentY.length() - 1);
        yearMinValue = Integer.parseInt(yearMin);

        String yearMax = upYearLY.charAt(upYearLY.length() - 2) + "" + upYearLY.charAt(upYearLY.length() - 1);
        yearMaxValue = Integer.parseInt(yearMax);

    }

    private void creditCardLogic(final EditText creditSystemNum) {
        creditSystemNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                beforeTC = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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
                            creditSystemNum.setText(ZERO);
                            creditSystemNum.setSelection(ZERO.length());
                        } else {
                            // 第一位是零,输入的数字不是零,补全斜杠
                            String aTemp = s.toString() + BACK_SLASH;
                            creditSystemNum.setText(aTemp);
                            creditSystemNum.setSelection(aTemp.length());
                        }
                    }

                    if (TextUtils.isEmpty(beforeTC)) {
                        if (!currentInputDesc.equals(ZERO) && !currentInputDesc.equals(ONE)) {
                            // 直接输入数字
                            String aTemp = ZERO + s.toString() + BACK_SLASH;
                            creditSystemNum.setText(aTemp);
                            creditSystemNum.setSelection(aTemp.length());
                        }
                    }

                    if (ONE.equals(beforeTC)) {
                        // 之前等于1
                        if (currentInputDesc.equals(ZERO) || currentInputDesc.equals(ONE) || currentInputDesc.equals(TWO)) {
                            // 并且输入的第二位数字满足 0,1,2月份要求
                            String aTemp = ONE + currentInputDesc + BACK_SLASH;
                            creditSystemNum.setText(aTemp);
                            creditSystemNum.setSelection(aTemp.length());
                        } else {
                            creditSystemNum.setText(ONE);
                            creditSystemNum.setSelection(ONE.length());
                        }
                    }
                    creditViewText = creditSystemNum.getText().toString();
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
                                creditSystemNum.setText(beforeTC);
                                creditSystemNum.setSelection(creditViewText.length());
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
                            creditSystemNum.setText(beforeTC);
                            creditSystemNum.setSelection(creditViewText.length());
                        }
                    }

                    if (creditViewText.length() > 5) {
                        // 控制输出
                        creditSystemNum.setText(beforeTC);
                        creditSystemNum.setSelection(creditViewText.length());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        creditKeyboardBinder.unregisterEditText(editText);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (creditKeyboardBinder.intercepteBackKeyDown()) {
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean numPattern(String textDesc) {
        String pattern = "^[0-9]*$";
        Pattern r = Pattern.compile(pattern);
        // 现在创建 matcher 对象
        Matcher m = r.matcher(textDesc);
        return m.find();
    }

}
