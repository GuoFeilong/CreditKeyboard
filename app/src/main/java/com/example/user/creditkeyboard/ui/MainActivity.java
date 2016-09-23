package com.example.user.creditkeyboard.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.example.user.creditkeyboard.R;
import com.example.user.creditkeyboard.customview.ClearEditText;
import com.example.user.creditkeyboard.customview.CreditCardTextWatcher;
import com.example.user.creditkeyboard.customview.keyboard.creditkeyboard.CreditKeyboardBinder;

public class MainActivity extends AppCompatActivity {
    private CreditKeyboardBinder creditKeyboardBinder;
    private ClearEditText editText;
    private ClearEditText creditSystemNum;
    private ClearEditText thridClearET;

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


        editText = (ClearEditText) findViewById(R.id.et_test);
        creditSystemNum = (ClearEditText) findViewById(R.id.et_system_num);
        thridClearET = (ClearEditText) findViewById(R.id.et_third);
        creditKeyboardBinder = new CreditKeyboardBinder(this);
        creditKeyboardBinder.registerEditText(creditSystemNum);


        creditSystemNum.addTextChangedListener(new CreditCardTextWatcher(creditSystemNum));
        editText.addTextChangedListener(new CreditCardTextWatcher(editText));
        thridClearET.addTextChangedListener(new CreditCardTextWatcher(thridClearET));

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


}
