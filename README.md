## 这次带来一个小小的信用卡有效期规则的Editext,额外赠送内置数字键盘的开发 ##


----------
首先来看下需求:
1）	月份数字：
λ	数字输入0:后一位数字可输入1-9，输入1则展示01/(注意斜杠展示在界面)，点击0界面没有反应

λ	数字输入1:后一位数字可输入0、1、2，输入0则展示10/(注意斜杠展示在界面)，点击3-9界面没有反应

λ	数字输入2:则展示02/(注意斜杠展示在界面),

λ	数字输入3-9，展示逻辑同2 


2）	年份数字：
λ	数字输入1、2、3，点击其他数字，界面无反应
λ	当输入数字1时：
继续输入数字6（当前年份为16年，2017年时即可输入7），则校验月份是否>=10,如果是，可以输入，如果不是，则不可输入
可继续输入数字7/8/9,0-5不可输入
λ	当输入数字2时：可继续输入数字0-9
λ	当输入数字3时：可继续输入数字0、1；也就是年份最大数字为31年（当前年份+15年，2017年时即可输入32）
不可以输入4-9和0，输入界面无反应


----------
这是DEMO完成时候的预览
![这里写图片描述](http://img.blog.csdn.net/20160923190429837)
用到项目里面也是和ok的啊,
![这里写图片描述](http://img.blog.csdn.net/20160923190627418)

**所以我们要写一个自定义的带清除的的Editext但是,还要加入额外的判断逻辑,加入清除按钮简单就一笔带过**


----------

```
    private void init() {
        mClearDrawable = getCompoundDrawables()[2];
        if (mClearDrawable == null) {
            mClearDrawable = getResources().getDrawable(R.mipmap.city_keyword_select_closeicon);
        }

        mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(), mClearDrawable.getIntrinsicHeight());
        setClearIconVisible(false);
        setOnFocusChangeListener(this);
        addTextChangedListener(this);
        // 禁止粘贴
        setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        setCustomSelectionActionModeCallback(this);
    }

```
自定义的Editext在构造方法中,获取系统的drawable数组,并且难道我们要设置的图片

```
 protected void setClearIconVisible(boolean visible) {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

```

调动这个设置drawable区域的API,把自定义的drawable显示

```
  @Override
    public void afterTextChanged(Editable s) {
        setClearIconVisible(s.toString().length() > 0);
    }
```

Editext的回调,判断显示icon的时机,


----------
由于我们添加的删除图标是一个drawable,所以我们只能重写onTouch模拟点击事件,进行清空editext文本内容的操作


```
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
```

判断手指抬起的时候,在UP中,用手指抬起的坐标和view的宽度-图片的宽度的坐标做个对比,如果大于这个值,说明在图片的点击区域内,那么我们清空,文本内容,其实就是给文本设置 空字符串就行了!


----------
**下面我们关键来看下那个监听里面应该如何对于上面的条件进行判断**

 - 自定义文本监听
 - 在文本监听的构造中获取,当前的时间限制
 - 解析当前的时间和限制时间,获取月份和年限的限制
 

```
//初始化的时候获取时间信息
    public CreditCardTextWatcher(EditText editText) {
        this.creditSystemNum = editText;

        Calendar calendar = Calendar.getInstance();
        // 计算信用卡年限逻辑
        int currentYear = calendar.get(Calendar.YEAR);
        int upYearLimit = currentYear + MAX_YEAR;

        currentY = currentYear + "";
        upYearLY = upYearLimit + "";

        String yearMin = currentY.charAt(currentY.length() - 2) + "" + currentY.charAt(currentY.length() - 1);
        yearMinValue = Integer.parseInt(yearMin);

        String yearMax = upYearLY.charAt(upYearLY.length() - 2) + "" + upYearLY.charAt(upYearLY.length() - 1);
        yearMaxValue = Integer.parseInt(yearMax);
    }

```
在监听里面逐条进行判断
```
// 记录变化之前的字符串
 @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        beforeTC = s.toString();
    }
```

```
 @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // 输入的时候进入逻辑判断,count等于0的时候说明在删除,不等于0的时候说明在键入字符
        if (count != 0) {
            String temp = s.toString();
            // 获取正在输入的字符是多少
            if (temp.length() >= 1) {
                currentInputDesc = temp.charAt(temp.length() - 1) + "";
            }
            // 判断之前的字符串是不是0,可以参考信用卡输入有效期逻辑,而且不能出现13月份,14月份等等不符合常理的
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
			// 如果之前是空
            if (TextUtils.isEmpty(beforeTC)) {
            //当前的数字不是0 也不是1,就直接补全加上斜杠
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
            String creditViewText = creditSystemNum.getText().toString();
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
                        creditSystemNum.setSelection(creditSystemNum.getText().length());
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
                    creditSystemNum.setSelection(creditSystemNum.getText().length());
                }
            }

            if (creditViewText.length() > 5) {
                // 控制输出
                creditSystemNum.setText(beforeTC);
                creditSystemNum.setSelection(creditSystemNum.getText().length());
            }
        } else {
            int selectionStart = creditSystemNum.getSelectionStart();
            int textLength = creditSystemNum.getText().length();
            // 判断光标有没有移动,移动的话不删除文字,
            if (selectionStart != textLength) {
                creditSystemNum.setText(beforeTC);
                creditSystemNum.setSelection(creditSystemNum.getText().length());
            } else {
                // 如果在最后,则删除
                Log.e("TAG", "--->>selectionStart=" + selectionStart + "--->>textLength" + textLength);
                creditSystemNum.setSelection(s.toString().length());
                if (s.length() == 2) {
                    // 当只剩下三个字符的时候一下删除两个字符
                    int index = creditSystemNum.getSelectionStart();
                    Editable editable = creditSystemNum.getText();
                    editable.delete(index - 1, index);
                }
            }
        }
    }
```

**注释已经很详细了,下面源代码连接送上,而且有菜单,里面有一个仿iPhone的自定义数字键盘,可以屏蔽掉系统键盘的哦,赶紧下载试试吧**

[下载地址:https://github.com/GuoFeilong/CreditKeyboard](https://github.com/GuoFeilong/CreditKeyboard)


----------
## 源码包含了自定义键盘,和信用卡自定义editext,希望各位多多star谢谢 ##

