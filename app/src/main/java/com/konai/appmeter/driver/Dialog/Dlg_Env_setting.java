package com.konai.appmeter.driver.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.konai.appmeter.driver.R;
import com.konai.appmeter.driver.setting.setting;

public class Dlg_Env_setting extends Dialog {

    com.konai.appmeter.driver.setting.setting setting = new setting();

    private RelativeLayout ble_layout;

    private RadioButton modem_normal
                    , modem_woorinet
                    , modem_am
                    , ble
                    , serial_inabi
                    , serial_artview
                    , serial_atlan
                    , ori_horizontal
                    , ori_vertical
                    , gubun_personal
                    , gubun_corporate;

    private String bleValue, oriValue, gubunValue, modemValue;

    private View.OnClickListener okListener;
    private View.OnClickListener cancelListener;

    public Dlg_Env_setting(@NonNull Context context
                                , View.OnClickListener okListener
                                , View.OnClickListener cancelListener) {
        super(context);
        this.okListener = okListener;
        this.cancelListener = cancelListener;
    }

    public Dlg_Env_setting(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.dlg_env_setting_layout);

        if (Build.VERSION.SDK_INT <= 25){
            setContentView(R.layout.dlg_env_setting_layout_land);
        }else {
            setContentView(R.layout.dlg_env_setting_layout);
        }

        modem_normal = (RadioButton)findViewById(R.id.modem_normal);
        modem_woorinet = (RadioButton)findViewById(R.id.modem_woorinet);
        modem_am = (RadioButton)findViewById(R.id.modem_am);
        ble_layout = (RelativeLayout)findViewById(R.id.ble_layout);
        ble = (RadioButton) findViewById(R.id.ble);
        serial_inabi = (RadioButton)findViewById(R.id.serial_inabi);
        serial_artview = (RadioButton)findViewById(R.id.serial_artview);
        serial_atlan = (RadioButton)findViewById(R.id.serial_atlan);
        ori_horizontal = (RadioButton)findViewById(R.id.ori_horizontal);
        ori_vertical = (RadioButton)findViewById(R.id.ori_vertical);
        gubun_personal = (RadioButton)findViewById(R.id.gubun_personal);
        gubun_corporate = (RadioButton)findViewById(R.id.gubun_corporate);

        Button okBtn = (Button)findViewById(R.id.ok_btn);
        Button cancelBtn = (Button)findViewById(R.id.cancel_btn);

        okBtn.setOnClickListener(okListener);  //??????
        cancelBtn.setOnClickListener(cancelListener);  //??????

        View.OnClickListener onClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch (v.getId()){

                    case R.id.ble:   //????????????
                        ble.setChecked(true);
                        serial_inabi.setChecked(false);
                        serial_artview.setChecked(false);
                        serial_atlan.setChecked(false);
                        ble_layout.setBackgroundResource(R.drawable.edit_backgroud_radius_clicked);
                        serial_inabi.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        serial_artview.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        serial_atlan.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        bleValue = "true";
                        break;
                    case R.id.serial_inabi:  //????????? ????????????
                        ble.setChecked(false);
                        serial_inabi.setChecked(true);
                        serial_artview.setChecked(false);
                        serial_atlan.setChecked(false);
                        serial_inabi.setBackgroundResource(R.drawable.edit_backgroud_radius_clicked);
                        ble_layout.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        serial_artview.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        serial_atlan.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        bleValue = "1";
                        break;
                    case R.id.serial_artview:  //????????? ?????????
                        ble.setChecked(false);
                        serial_inabi.setChecked(false);
                        serial_artview.setChecked(true);
                        serial_atlan.setChecked(false);
                        serial_artview.setBackgroundResource(R.drawable.edit_backgroud_radius_clicked);
                        ble_layout.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        serial_inabi.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        serial_atlan.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        bleValue = "2";
                        break;
                    case R.id.serial_atlan:  //????????? ?????????
//                        Toast.makeText(getContext(), "????????? ??????", Toast.LENGTH_SHORT).show();
                        ble.setChecked(false);
                        serial_inabi.setChecked(false);
                        serial_artview.setChecked(false);
                        serial_atlan.setChecked(true);
                        serial_atlan.setBackgroundResource(R.drawable.edit_backgroud_radius_clicked);
                        ble_layout.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        serial_inabi.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        serial_artview.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        bleValue = "3";
                    case R.id.ori_horizontal:  //??????
                        ori_horizontal.setChecked(true);
                        ori_vertical.setChecked(false);
                        ori_horizontal.setBackgroundResource(R.drawable.edit_backgroud_radius_clicked);
                        ori_vertical.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        oriValue = "1";
                        break;
                    case R.id.ori_vertical:   //??????
                        ori_vertical.setChecked(true);
                        ori_horizontal.setChecked(false);
                        ori_vertical.setBackgroundResource(R.drawable.edit_backgroud_radius_clicked);
                        ori_horizontal.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        oriValue = "2";
                        break;
                    case R.id.gubun_personal:  //??????
                        gubun_personal.setChecked(true);
                        gubun_corporate.setChecked(false);
                        gubun_personal.setBackgroundResource(R.drawable.edit_backgroud_radius_clicked);
                        gubun_corporate.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        gubunValue ="1";
                        break;
                    case R.id.gubun_corporate:  //??????
                        gubun_corporate.setChecked(true);
                        gubun_personal.setChecked(false);
                        gubun_corporate.setBackgroundResource(R.drawable.edit_backgroud_radius_clicked);
                        gubun_personal.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        gubunValue ="2";
                    case R.id.modem_normal:   //??????- ??????
                        modem_normal.setChecked(true);
                        modem_woorinet.setChecked(false);
                        modem_am.setChecked(false);
                        modem_normal.setBackgroundResource(R.drawable.edit_backgroud_radius_clicked);
                        modem_woorinet.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        modem_am.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        modemValue = "1";
                        break;
                    case R.id.modem_woorinet:  //??????- ?????????
                        modem_normal.setChecked(false);
                        modem_woorinet.setChecked(true);
                        modem_am.setChecked(false);
                        modem_woorinet.setBackgroundResource(R.drawable.edit_backgroud_radius_clicked);
                        modem_normal.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        modem_am.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        modemValue = "2";
                        break;
                    case R.id.modem_am:        //??????- ?????????
                        modem_normal.setChecked(false);
                        modem_woorinet.setChecked(false);
                        modem_am.setChecked(true);
                        modem_am.setBackgroundResource(R.drawable.edit_backgroud_radius_clicked);
                        modem_woorinet.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        modem_normal.setBackgroundResource(R.drawable.edit_backgroud_radius);
                        modemValue = "3";
                        break;

                }//switch..
            }//onclick..
        };

        modem_normal.setOnClickListener(onClickListener);
        modem_woorinet.setOnClickListener(onClickListener);
        modem_am.setOnClickListener(onClickListener);
        ble.setOnClickListener(onClickListener);
        serial_inabi.setOnClickListener(onClickListener);
        serial_artview.setOnClickListener(onClickListener);
        serial_atlan.setOnClickListener(onClickListener);
        ori_horizontal.setOnClickListener(onClickListener);
        ori_vertical.setOnClickListener(onClickListener);
        gubun_personal.setOnClickListener(onClickListener);
        gubun_corporate.setOnClickListener(onClickListener);
    }//onCreate..



    public String return_blueValue(){
        Log.d("final_bleValue", bleValue);
        return bleValue;
    }

    public String return_oriValue(){
        Log.d("final_orivalue", oriValue);
        return oriValue;
    }

    public String return_gubunValue(){
        Log.d("final_gubunValue", gubunValue);
        return gubunValue;
    }

    //todo: 20220124
//    public String return_modemValue(){
//        Log.d("final_modemValue", modemValue);
//        return modemValue;
//    }
    //todo: end
}


