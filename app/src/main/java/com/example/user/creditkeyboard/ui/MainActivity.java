package com.example.user.creditkeyboard.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.creditkeyboard.R;
import com.example.user.creditkeyboard.customview.ClearEditText;
import com.example.user.creditkeyboard.customview.CreditCardTextWatcher;
import com.example.user.creditkeyboard.customview.WithdrawAmountTextWatcher;
import com.example.user.creditkeyboard.customview.keyboard.creditkeyboard.CreditKeyboardBinder;

public class MainActivity extends AppCompatActivity {
    private CreditKeyboardBinder creditKeyboardBinder;
    private CreditKeyboardBinder creditKeyboardBinder2;
    private ClearEditText editText;
    private ClearEditText creditSystemNum;
    private ClearEditText thridClearET;

    private EditText editText4;

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

        TextView textView = (TextView) findViewById(R.id.tv_test);
        int lineCount = textView.getLineCount();


        editText = (ClearEditText) findViewById(R.id.et_test);
        creditSystemNum = (ClearEditText) findViewById(R.id.et_system_num);
        thridClearET = (ClearEditText) findViewById(R.id.et_third);
        editText4 = (EditText) findViewById(R.id.et_forth);
        creditKeyboardBinder = new CreditKeyboardBinder(this);
        creditKeyboardBinder.registerEditText(creditSystemNum);
        creditKeyboardBinder2 = new CreditKeyboardBinder(this);
        creditKeyboardBinder2.registerEditText(editText);

        creditSystemNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String testYear = "2016";
                String sub = testYear.substring(0, 2);
                Log.e("TAG", "-->>焦点--hasFocus--sub--->>" + sub + hasFocus + "--->>value=" + creditSystemNum.getText().toString());

            }
        });
        //禁止光标
//        creditSystemNum.setCursorVisible(false);


        creditSystemNum.addTextChangedListener(new CreditCardTextWatcher(creditSystemNum, new CreditCardTextWatcher.OnCreditCardExpireDataFinishedListener() {
            @Override
            public void dataFinished(CreditCardTextWatcher.ExpireEntity expireEntity) {

            }
        }));
        editText.addTextChangedListener(new CreditCardTextWatcher(editText, new CreditCardTextWatcher.OnCreditCardExpireDataFinishedListener() {
            @Override
            public void dataFinished(CreditCardTextWatcher.ExpireEntity expireEntity) {
                Toast.makeText(MainActivity.this, "--->>有效期回调:expireEntity" + expireEntity.toString(), Toast.LENGTH_SHORT).show();
            }
        }));
        thridClearET.addTextChangedListener(new CreditCardTextWatcher(thridClearET, new CreditCardTextWatcher.OnCreditCardExpireDataFinishedListener() {
            @Override
            public void dataFinished(CreditCardTextWatcher.ExpireEntity expireEntity) {

            }
        }));

        editText4.addTextChangedListener(new WithdrawAmountTextWatcher(editText4));
    }

    @Override
    protected void onStop() {
        super.onStop();
        creditKeyboardBinder.unregisterEditText(creditSystemNum);
        creditKeyboardBinder2.unregisterEditText(editText);
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


}
