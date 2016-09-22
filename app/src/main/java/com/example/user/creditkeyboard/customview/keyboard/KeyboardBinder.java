package com.example.user.creditkeyboard.customview.keyboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.Editable;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;


import com.example.user.creditkeyboard.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class KeyboardBinder {
    /* 滚动布局动画时间，和键盘弹出时间相等 */
    private static final int ANIM_DUTION = 250;
    /* 删除按键key */
    private static final int DEL_KEY = 60001;
    private Activity activity;
    /* 自定义键盘载体 */
    private PopupWindow popupWindow;
    /* 当前Activity的contentview，实际上是contentview父容器 */
    private View mainContentView;
    /* 键盘高度 */
    private int keyboardHeight;
    /* 可复用用于计算坐标数组 */
    private int[] viewLocation;
    /* 当前Activity显示区域 */
    private Rect mainFrameRect;
    /* contentview为了不遮挡输入框而滑动的偏移，用于输入框隐藏后恢复contentview布局 */
    private int deltaY;
    /* 自定义输入框是否显示标志，因为需要做动画，并且系统回主动dismiss popupwindow，所以不用popupwindow.isShow判断 */
    protected boolean isShow;

    public KeyboardBinder(Activity activity) {
        this.activity = activity;

        viewLocation = new int[2];
        mainContentView = activity.findViewById(android.R.id.content);
        mainFrameRect = new Rect();
        mainContentView.getWindowVisibleDisplayFrame(mainFrameRect);
    }

    /**
     * 绑定EditText 注意：被绑定EditText的OnFocusChangeListener和OnTouchListener监听会被覆盖掉
     *
     * @param editText
     */
    public void registerEditText(EditText editText) {
        if (null == editText) {
            return;
        }

        // 监听焦点变化，用于显示/隐藏当前输入法窗口
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    show(view);
                } else {
                    dismiss();
                }
            }
        });

        // 去掉系统自带键盘
        editText.setOnTouchListener(new EditTextSimplyOnTouchListener());
    }

    /**
     * 去绑定 注意：去绑定EditText的OnFocusChangeListener和OnTouchListener监听会被置空
     *
     * @param editText
     */
    public void unregisterEditText(final EditText editText) {
        if (null == editText) {
            return;
        }

        // 去掉 自定义输入法窗口显示监听
        editText.setOnFocusChangeListener(null);
        editText.setOnTouchListener(null);
    }

    public boolean isShow() {
        return isShow;
    }

    /**
     * 隐藏自定义键盘
     */
    public void dismiss() {
        if (null != popupWindow) {
            popupWindow.dismiss();
        }
    }

    public void show(View view) {
        hideSoftInputMethod(view);

        if (null == popupWindow) {
            // 自定义键盘布局
            View contentView = buildContentView();
            View confirmView = contentView.findViewById(R.id.complete);
            confirmView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            KeyboardView keyboardView = (KeyboardView) contentView
                    .findViewById(R.id.keyboardview);
            Keyboard keyboard = new Keyboard(activity, getKeyboardResId());
            keyboardView.setKeyboard(keyboard);
            keyboardView.setPreviewEnabled(false);
            keyboardView.setOnKeyboardActionListener(new KeyboardActionListener());

            // 键盘载体PopupWindow
            popupWindow = new PopupWindow(activity);
            popupWindow.setAnimationStyle(R.style.PaymentKeyboardAnim);
            popupWindow.setBackgroundDrawable(new BitmapDrawable());
            popupWindow.setOutsideTouchable(true);
            popupWindow.setOnDismissListener(new KeyBoardViewDismissListener());
            popupWindow.setTouchInterceptor(new KeyBoardViewTouchInterceptor());
            popupWindow.setContentView(contentView);
            popupWindow.setBackgroundDrawable(new ColorDrawable(
                    Color.TRANSPARENT));
            popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

            keyboardHeight = getViewMaxHeigth(contentView);
        }

        if (!isShow) {
            popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            view.getLocationOnScreen(viewLocation);

            // 计算自定义键盘和待输入EditText是否覆盖，仅覆盖时会移动contentview
            int keyboardLocY = mainFrameRect.bottom - keyboardHeight;
            deltaY = keyboardLocY - viewLocation[1] - view.getMeasuredHeight();
            if (deltaY < 0) {
                doKeyboardShowAnim();
            } else {
                // 不执行动画
                isShow = true;
            }
        }
    }

    /**
     * 获取view最大高度
     *
     * @param view
     * @return
     */
    private static int getViewMaxHeigth(View view) {
        if (null == view) {
            return 0;
        }

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        return view.getMeasuredHeight();
    }

    /**
     * 运行 键盘弹出动画 TranslateAnimation完成连续效果， scrollBy移动实际布局,使得之后的点击事件生效
     */
    private void doKeyboardShowAnim() {
        // 开启动画
        TranslateAnimation anim = new TranslateAnimation(
                mainContentView.getScaleX(), mainContentView.getScaleX(),
                mainContentView.getScaleY(), mainContentView.getScaleY()
                + deltaY);
        anim.setDuration(ANIM_DUTION);
        anim.setFillAfter(true);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainContentView.clearAnimation();
                mainContentView.scrollBy(0, -deltaY);
                isShow = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mainContentView.startAnimation(anim);
    }

    /**
     * 运行 键盘消失动画
     */
    private void doKeyboardDismissAnim() {
        // 滚回原来位置
        if (deltaY >= 0) {
            isShow = false;
            return;
        }
        // 开启动画
        TranslateAnimation anim = new TranslateAnimation(
                mainContentView.getScaleX(), mainContentView.getScaleX(),
                mainContentView.getScaleY(), mainContentView.getScaleY()
                - deltaY);
        anim.setDuration(ANIM_DUTION);
        anim.setFillAfter(true);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainContentView.clearAnimation();
                mainContentView.scrollBy(0, deltaY);
                isShow = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mainContentView.startAnimation(anim);
    }

    /**
     * 隐藏系统输入法
     *
     * @param view
     */
    private void hideSoftInputMethod(View view) {
        if (view != null) {
            ((InputMethodManager) activity
                    .getSystemService(Activity.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 获取当前获得焦点的EditText
     *
     * @return
     */
    private EditText getCurrentFocusEditText() {
        View focusCurrent = activity.getWindow().getCurrentFocus();
        if (focusCurrent instanceof EditText) {
            EditText edittext = (EditText) focusCurrent;
            return edittext;
        }

        return null;
    }

    /**
     * 构建内容区域布局 可用于子类重载，自定义键盘容器布局
     *
     * @return
     */
    protected View buildContentView() {
        View contentView = LayoutInflater.from(activity).inflate(
                getKeyboardLayoutResId(), null);
        return contentView;
    }

    /**
     * 弹出键盘的布局
     *
     * @return
     */
    protected abstract int getKeyboardLayoutResId();

    /**
     * 构建Keyboard布局 可用于子类重载，自定义键盘布局
     *
     * @return
     */
    protected abstract int getKeyboardResId();

    /**
     * 自定义输入法弹窗消失回调
     */
    private class KeyBoardViewDismissListener implements
            PopupWindow.OnDismissListener {
        @Override
        public void onDismiss() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doKeyboardDismissAnim();
                }
            });
        }
    }

    /**
     * 自定义输入法Touch拦截 完成点击EditText不消失弹窗(默认点击popupwindow外部，弹窗消失)
     */
    private class KeyBoardViewTouchInterceptor implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            EditText editText = getCurrentFocusEditText();
            if (null == editText) {
                return false;
            }

            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                int[] location = new int[2];
                editText.getLocationOnScreen(location);
                if (event.getRawX() >= location[0]
                        && event.getRawX() <= (location[0] + editText
                        .getWidth())
                        && event.getRawY() >= location[1]
                        && event.getRawY() <= (location[1] + editText
                        .getHeight())) {
                    // 如果点击的是当今获取焦点的EditText，则消费事件，避免弹窗消失
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * 为了去掉系统自带键盘（不执行onTouchEvent），对Touch事件进行简化， 简化后的功能: 1. 获取焦点 2. 选择游标位置
     */
    private class EditTextSimplyOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                EditText edittext = (EditText) view;
                // 获取焦点
                if (edittext.isFocusable() && edittext.isFocusableInTouchMode()
                        && !edittext.isFocused()) {
                    edittext.requestFocus();
                }
                setKeyBoardCursorNew(edittext);
                show(view);
            }
            return false;
        }
    }

    /**
     * 自定义键盘按键监听
     */
    private class KeyboardActionListener implements KeyboardView.OnKeyboardActionListener {
        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            EditText editText = getCurrentFocusEditText();
            if (null == editText) {
                return;
            }

            Editable editable = editText.getText();
            if (null == editable) {
                return;
            }

            int start = editText.getSelectionStart();
            int end = editText.getSelectionEnd();
            if (-1 == start || -1 == end) {
                // 选中状态异常
                return;
            }

            if (start < end) {
                // 选中多个
                if (primaryCode == DEL_KEY) {
                    editable.delete(start, end);
                } else {
                    editable.replace(start, end,
                            Character.toString((char) primaryCode));
                }
            } else if (start == end) {
                // 未选中
                if (primaryCode == DEL_KEY) {
                    if (start > 0) {
                        editable.delete(start - 1, end);
                    }
                } else {
                    editable.insert(start,
                            Character.toString((char) primaryCode));
                }
            }
        }

        @Override
        public void onPress(int arg0) {
        }

        @Override
        public void onRelease(int primaryCode) {
        }

        @Override
        public void onText(CharSequence text) {
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeUp() {
        }
    }

    /**
     * EditText获取焦点后不显示输入法
     *
     * @param edit
     * @return
     */
    private boolean setKeyBoardCursorNew(EditText edit) {
        boolean flag = false;

        InputMethodManager imm = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();// isOpen若返回true，则表示输入法打开
        if (isOpen) {
            // ((InputMethodManager)
            // mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
            if (imm.hideSoftInputFromWindow(edit.getWindowToken(), 0))
                flag = true;
        }

        // act.getWindow().setSoftInputMode(
        // WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        String methodName = null;
        if (currentVersion >= 16) {
            // 4.2
            methodName = "setShowSoftInputOnFocus";
        } else if (currentVersion >= 14) {
            // 4.0
            methodName = "setSoftInputShownOnFocus";
        }

        if (methodName == null) {
            edit.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method setShowSoftInputOnFocus;
            try {
                setShowSoftInputOnFocus = cls.getMethod(methodName,
                        boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(edit, false);
            } catch (NoSuchMethodException e) {
                edit.setInputType(InputType.TYPE_NULL);
                e.printStackTrace();
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }
}
