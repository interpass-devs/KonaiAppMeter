package com.konai.appmeter.driver.Dialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.konai.appmeter.driver.R;


public class Dlg_Basic extends Dialog {

    private TextView msgText;
    private String msg;
    private Button okBtn, cancelBtn;
    private View.OnClickListener okListener;
    private View.OnClickListener cancelListener;


    public Dlg_Basic(@NonNull Context context
                            , String message
                            , View.OnClickListener okListener
                            , View.OnClickListener cancelListener) {
        super(context);
        this.msg = message;
        this.okListener = okListener;
        this.cancelListener = cancelListener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dlg_basic);

        msgText = findViewById(R.id.msg);
        okBtn = findViewById(R.id.okay_btn);
        cancelBtn = findViewById(R.id.cancel_btn);

        msgText.setText(msg);
        okBtn.setOnClickListener(okListener);
        cancelBtn.setOnClickListener(cancelListener);


    }//onCreate





}//Dlg_Basic
