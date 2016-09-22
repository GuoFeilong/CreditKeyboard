package com.example.user.creditkeyboard.customview.keyboard.creditkeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

import com.example.user.creditkeyboard.R;

import java.util.List;

/**
 * Created by user on 16/9/22.
 */

public class CreditKeyboardView extends KeyboardView {
    /**
     * 删除按键的KEY
     */
    private static final int DEL_KEY = 60001;
    private Context mContext;
    private Keyboard.Key mDelKey;

    public CreditKeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CreditKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    public void setKeyboard(Keyboard keyboard) {
        super.setKeyboard(keyboard);
        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for (Keyboard.Key key : keys) {
            if (key.codes[0] == DEL_KEY) {
                mDelKey = key;
                break;
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDelKeyBackground(mDelKey, canvas);
        drawDelKeyIcon(mDelKey, canvas, mDelKey.pressed);
    }

    /**
     * 重新设置delete键的背景
     *
     * @param key
     * @param canvas
     */
    private void drawDelKeyBackground(Keyboard.Key key, Canvas canvas) {
        Drawable drawable = mContext.getResources().getDrawable(
                R.drawable.payment_bg_keyboard_del_key);
        int[] drawableState = key.getCurrentDrawableState();
        drawable.setState(drawableState);
        drawable.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
        drawable.draw(canvas);
    }

    /**
     * 绘制delelte键的icon
     *
     * @param key
     * @param canvas
     * @param pressed
     */
    private void drawDelKeyIcon(Keyboard.Key key, Canvas canvas, boolean pressed) {
        Drawable drawable;
        if (pressed) {
            drawable = mContext.getResources().getDrawable(
                    R.mipmap.payment_keyboard_del_pressed);
        } else {
            drawable = mContext.getResources().getDrawable(
                    R.mipmap.payment_keyboard_del_normal);
        }

        drawable.setBounds(key.x + (key.width - drawable.getIntrinsicWidth())
                        / 2, key.y + (key.height - drawable.getIntrinsicHeight()) / 2,
                key.x + (key.width + drawable.getIntrinsicWidth()) / 2, key.y
                        + (key.height + drawable.getIntrinsicHeight()) / 2);
        drawable.draw(canvas);
    }
}
