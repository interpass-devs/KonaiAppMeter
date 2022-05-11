package com.konai.appmeter.driver;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomKeyboardView extends KeyboardView {

    CustomOnKeyboardActionListener keyListener, keyListener2;

    Keyboard kb = null, kb2 = null;

    Context context;

    boolean caps = false;

    private String kor;

    ArrayList<String> codeList = new ArrayList<>();


    public CustomKeyboardView(Context context, AttributeSet attrs) {

        super(context, attrs);

        this.context = context;

        kb = new Keyboard(context, R.xml.custom_keyboard);

        kb2 = new Keyboard(context, R.xml.custom_keyboard);

    }



    public void setActionListenerActivity(Activity act) {


        kor= "1";

        this.clearFocus();

        keyListener = new CustomOnKeyboardActionListener(act);

        this.setOnKeyboardActionListener(keyListener);

        this.setKeyboard(kb);

    }


//    public void setActionListenerAcitivty2(Activity act) {
//
//
//
//        kor="2";
//
//        this.clearFocus();
//
//        keyListener2 = new CustomOnKeyboardActionListener(act);
//
//        this.setOnKeyboardActionListener(keyListener2);
//
//        this.setKeyboard(kb2);
//
//    }



    @Override

    public boolean isInEditMode() {

        return true;

    };



    private class CustomOnKeyboardActionListener implements OnKeyboardActionListener {

        Activity owner;

        public CustomOnKeyboardActionListener(Activity activity) {

            owner = activity;

        }



        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override

        public void onKey(int primaryCode, int[] keyCodes) {

            Log.d("checkCode","primaryCode: "+primaryCode+",  keyCode: "+keyCodes);

            long eventTime = System.currentTimeMillis();

            if (primaryCode == -5) {

                if (kor.equals("2")) {

                    setActionListenerActivity(owner);

                }
//                else if (kor.equals("1")) {
//
//                    setActionListenerAcitivty2(owner);
//
//                }

            } else {

                KeyEvent event = new KeyEvent(eventTime, eventTime,

                        KeyEvent.ACTION_DOWN, primaryCode, 0, 0, 0, 0,

                        KeyEvent.FLAG_SOFT_KEYBOARD |KeyEvent.FLAG_KEEP_TOUCH_MODE);

                Log.e("Custom", "KEYCODE:"+primaryCode+"=> "+ keyCodes);  //KEYCOD:35=> [I@774e7ab


                owner.dispatchKeyEvent(event);

            }


        }


        @Override

        public void onPress(int primaryCode) {

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







}
