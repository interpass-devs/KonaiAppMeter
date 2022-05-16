package com.konai.appmeter.driver.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.konai.appmeter.driver.R;
import com.konai.appmeter.driver.setting.setting;

import java.util.ArrayList;

public class Dlg_Num_Type extends Dialog {

    int index = 0;
    ArrayList<String> list = new ArrayList<>();
    private String val = "";
    private FrameLayout framework;
    private TextView dlgTitle, tvNumType;
    private Button okBtn, cancelBtn;
    private int mlength = 0;
    private int width, height;
    private View.OnClickListener context
                                ,okListener
                                ,cancelListener;
    private String numType;
    private RadioButton btn_0
                        , btn_1
                        , btn_2
                        , btn_3
                        , btn_4
                        , btn_5
                        , btn_6
                        , btn_7
                        , btn_8
                        , btn_9
                        , btn_back
                        , btn_clear;

    //constructor
    public Dlg_Num_Type(@NonNull Context context
                            , String numType
                            , View.OnClickListener okListener
                            , View.OnClickListener cancelListener
                            , int x , int y){
        super(context);
        this.numType = numType;
        this.okListener = okListener;
        this.cancelListener = cancelListener;
        this.width = x;
        this.height = y;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if (setting.gOrient == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            setContentView(R.layout.dlg_inptcreceipt_info_v);
        }else {
            setContentView(R.layout.dlg_inptcreceipt_info_h);
        }

        //액티비티 다이얼로그 화면사이즈 조절
        if (setting.gOrient == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //Build.VERSION.SDK_INT <= 25){
        {
            width = (int)(width * 0.9);
            height = (int)(height * 0.9);
        }else {
            width = (int)(width * 0.9);
            height = (int)(height * 0.8);
        }

        getWindow().getAttributes().width = width;
        getWindow().getAttributes().height = height;

        dlgTitle = (TextView) findViewById(R.id.dlgTitle);
        framework = (FrameLayout) findViewById(R.id.framework);
        tvNumType = (TextView) findViewById(R.id.tv_numtype);
        okBtn = (Button) findViewById(R.id.btn_ok);
        cancelBtn = (Button) findViewById(R.id.btn_cancel);
        btn_0 = (RadioButton) findViewById(R.id.btn_0);
        btn_1 = (RadioButton) findViewById(R.id.btn_1);
        btn_2 = (RadioButton) findViewById(R.id.btn_2);
        btn_3 = (RadioButton) findViewById(R.id.btn_3);
        btn_4 = (RadioButton) findViewById(R.id.btn_4);
        btn_5 = (RadioButton) findViewById(R.id.btn_5);
        btn_6 = (RadioButton) findViewById(R.id.btn_6);
        btn_7 = (RadioButton) findViewById(R.id.btn_7);
        btn_8 = (RadioButton) findViewById(R.id.btn_8);
        btn_9 = (RadioButton) findViewById(R.id.btn_9);
        btn_back = (RadioButton) findViewById(R.id.btn_back);
        btn_clear = (RadioButton) findViewById(R.id.btn_clear);

        if (numType.equals("identiNum")){
            dlgTitle.setText("사원번호 입력");
            mlength = 4;
        }else if (numType.equals("licenseNum")){
            dlgTitle.setText("운전자 자격번호 입력");
            mlength = 10;
        }else if (numType.equals("password")){
            dlgTitle.setText("비밀번호 입력");
            mlength = 4;
        }


        framework.setVisibility(View.GONE);
        tvNumType.setVisibility(View.VISIBLE);
        okBtn.setOnClickListener(okListener);
        cancelBtn.setOnClickListener(cancelListener);
        btn_0.setOnClickListener(keypadListener);
        btn_1.setOnClickListener(keypadListener);
        btn_2.setOnClickListener(keypadListener);
        btn_3.setOnClickListener(keypadListener);
        btn_4.setOnClickListener(keypadListener);
        btn_5.setOnClickListener(keypadListener);
        btn_6.setOnClickListener(keypadListener);
        btn_7.setOnClickListener(keypadListener);
        btn_8.setOnClickListener(keypadListener);
        btn_9.setOnClickListener(keypadListener);
        btn_back.setOnClickListener(keypadListener);
        btn_clear.setOnClickListener(keypadListener);
    }//onCreate..

    private void add_num(String val)
    {
        if(list.size() >= mlength)
            return;

        list.add(val);
    }

    private View.OnClickListener keypadListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            for (int i=0; i<1; i++){
                index = i;
                switch (v.getId()){
                    case R.id.btn_0:
                        add_num("0");
                        break;
                    case R.id.btn_1:
                        add_num("1");
                        break;
                    case R.id.btn_2:
                        add_num("2");
                        break;
                    case R.id.btn_3:
                        add_num("3");
                        break;
                    case R.id.btn_4:
                        add_num("4");
                        break;
                    case R.id.btn_5:
                        add_num("5");
                        break;
                    case R.id.btn_6:
                        add_num("6");
                        break;
                    case R.id.btn_7:
                        add_num("7");
                        break;
                    case R.id.btn_8:
                        add_num("8");
                        break;
                    case R.id.btn_9:
                        add_num("9");
                        break;
                    case R.id.btn_back:
                        try{
                            //마지막값 지우기
                            int lastIndex = list.size() - 1;
                            if (lastIndex <= 0) //20220303
                            {
                                Log.d("keypad_last_index", lastIndex+",  값: "+list.get(lastIndex));
                                list.removeAll(list);
                                tvNumType.setText("");
                            }else {
                                list.remove(lastIndex);
                            }
                        }catch (Exception e){}
                        break;
                    case R.id.btn_clear: //모두 삭제버튼
//                        list.clear();
                        if (list.size() != 0){
                            list.removeAll(list);
//                            Log.d("keypad_", list.size()+"개,  "+list.toString());
                        }
                        break;
                }//switch
//                Log.d("keypad_list", list.toString()+",   사이즈: "+list.size());
            }//for

//            Log.d("keypad_list_final", list.toString()+",   사이즈: "+list.size());

            tvNumType.setText(list.toString());

            try{
//                String calVal ="";
                int nmanulafare = 0;

                if (list.size() == 0){
                    Log.d("0000000","0000000");
                    tvNumType.setText("");
                    val ="";
                }else {
                    val = TextUtils.join("",list);  //instead of String.join
                    tvNumType.setText(val);
                }

                if (val.equals(null) || val.equals("") || Integer.parseInt(val) < 0){
                    nmanulafare = 0;
                }else {}
            }catch (Exception e){}

        }//onclick
    };//keypadListener



    public String returnNumTypeVal(){
        Log.d("keypad_val", val);
        return val;
    }





}//Dlg_Basic....
