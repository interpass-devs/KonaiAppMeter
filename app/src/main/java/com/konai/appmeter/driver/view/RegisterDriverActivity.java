package com.konai.appmeter.driver.view;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.konai.appmeter.driver.DB.SQLiteControl;
import com.konai.appmeter.driver.DB.SQLiteHelper;
import com.konai.appmeter.driver.Dialog.Dlg_Num_Type;
import com.konai.appmeter.driver.R;
import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.setting.setting;

//import android.support.annotation.NonNull;


public class RegisterDriverActivity extends Activity {

    private EditText driverName;  //todo: 2022-04-29
    Button registerBtn, editBtn;
    Context context;
    public static SQLiteHelper helper;
    public static SQLiteControl sqlite;
    private String driver_num, identi_num;
    private TextView driverIdentiNum, driverLicenseNum; //todo: 2022-04-29
    private Dlg_Num_Type numTypeDialog;  //todo: 2022-04-29

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);

        //배경 테두리 없애기
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //액티비티 다이얼로그 화면사이즈 조절
        Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = (int)(display.getWidth() * 0.9);
        int height = (int)(display.getHeight() * 0.8);
        getWindow().getAttributes().width = width;
        getWindow().getAttributes().height = height;

        //해상도에 맞게 래이아웃 설정
        if (true){
            if (setting.gOrient == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){

                setContentView(R.layout.activity_register_driver);  //세로

            }else {

                setContentView(R.layout.activity_register_driver_h); //가로
            }
        }

        context = this;

        driverName = findViewById(R.id.et_driver_name);
        driverLicenseNum = findViewById(R.id.et_driver_license_num);
        driverIdentiNum = findViewById(R.id.et_driver_identi_num);
//        driverIdentiNum.setEnabled(false); //20210901 tra..sh

        //운전자 등록 -> 운전자 자격증 다이얼로그
        driverLicenseNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
                numTypeDialog = new Dlg_Num_Type(context
                        , "licenseNum"
                        , new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //ok
                        String val = numTypeDialog.returnNumTypeVal();
                        if (val.length() == 9 || val.length() == 10){
                            driverLicenseNum.setText(val);  //운전자 자격증번호
                            numTypeDialog.dismiss();
                        }else {
                            Toast.makeText(context, "잘못 입력하셨습니다. \n운전자 자격증 9자리 또는 10자리를 입력해주세요.", Toast.LENGTH_SHORT).show();
                            driverLicenseNum.setText("");}
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //cancel
                        numTypeDialog.dismiss();
                    }
                }, dm.widthPixels, dm.heightPixels
                        );

                int width = dm.widthPixels;
                int height = dm.heightPixels;

                if (setting.gOrient == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
                    width = (int) (width * 0.9);
                }

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(numTypeDialog.getWindow().getAttributes());
                lp.width = width;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                Window window = numTypeDialog.getWindow();
                window.setAttributes(lp);

                //show dialog
                numTypeDialog.setCancelable(false);
                numTypeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                numTypeDialog.show();
            }
        });



        //운전자 등록 -> 사원번호 다이얼로그
        driverIdentiNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
                numTypeDialog = new Dlg_Num_Type(context
                        , "identiNum"
                        , new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //ok
                        String val = numTypeDialog.returnNumTypeVal();
                        if (val.length() == 4){
                            driverIdentiNum.setText(val);
                            numTypeDialog.dismiss();
                        }else {
                            Toast.makeText(context, "잘못 입력하셨습니다. \n사원번호 4자리를 입력해주세요.", Toast.LENGTH_SHORT).show();
                            driverIdentiNum.setText("");
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //cancel
                        numTypeDialog.dismiss();
                    }
                },  dm.widthPixels, dm.heightPixels);

                int width = dm.widthPixels;
                int height = dm.heightPixels;
                width = (int) (width * 0.9);

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(numTypeDialog.getWindow().getAttributes());
                lp.width = width;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                Window window = numTypeDialog.getWindow();
                window.setAttributes(lp);

                //show dialog
                numTypeDialog.setCancelable(false);
                numTypeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                numTypeDialog.show();
            }
        });
        //todo: end

        registerBtn = findViewById(R.id.register_btn);
        editBtn = findViewById(R.id.edit_btn);
        driverName.setTextColor(Color.parseColor("#ffffff"));
        driverLicenseNum.setTextColor(Color.parseColor("#ffffff"));
        driverIdentiNum.setTextColor(Color.parseColor("#ffffff"));

        if (Build.VERSION.SDK_INT <= 25){
            registerBtn.setTextSize(5 * setting.gTextDenst);
            editBtn.setTextSize(5 * setting.gTextDenst);
        }else {}

        driverName.setText(getIntent().getStringExtra("name"));
        driverName.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        driverName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT){

                }
                return false;
            }
        });

        if (getIntent().getStringExtra("license_num") != null){
            driverLicenseNum.setText(getIntent().getStringExtra("license_num"));
            driverIdentiNum.setText((getIntent().getStringExtra("identi_num")));
            driverIdentiNum.setTextColor(getResources().getColor(R.color.white));
        }


        driverLicenseNum.setImeOptions(EditorInfo.IME_ACTION_DONE);
        driverLicenseNum.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    //키패드에서 완료버튼 누른후 set 해주기..
                    if (driverLicenseNum.getText().toString().length() == 9){


                    }else if (driverLicenseNum.getText().toString().length() != 9){      //20210831
                        Toast.makeText(context, "자격번호는 9자리로 입력해주세요.", Toast.LENGTH_SHORT).show();
                    }

                    //키패드 내리기
                    final InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE) ;
                    manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        });


        //기존 값 (수정시)
        driver_num = getIntent().getStringExtra("license_num");
        identi_num = getIntent().getStringExtra("identi_num");


        if (getIntent().getStringExtra("clickEditBtn").equals("editBtn")){
            editBtn.setVisibility(View.VISIBLE);
            registerBtn.setVisibility(View.GONE);
        }else {
            editBtn.setVisibility(View.GONE);
            registerBtn.setVisibility(View.VISIBLE);
        }


        //등록버튼
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (driverName.getText().length() < 1){
                    Toast.makeText(context, "성함을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }else if (driverLicenseNum.getText().length() < 1){
                    Toast.makeText(context, "운전자격증 번호 9자리를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                else if (driverIdentiNum.getText().toString().length() != 4){         //todo: 2022-04-29
                    Toast.makeText(context, "사원번호 4자리를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    // 데이터 INSERT
                    if(driverLicenseNum.getText().toString().length() == 9){

                        long reuslt = Info.sqlite.insertMember(driverName.getText().toString(), driverLicenseNum.getText().toString(), driverIdentiNum.getText().toString() );
                        finish();
                        if(reuslt == -1 ){
                            Toast.makeText(context, "잘못된 정보가 입력되었습니다. 다시 등록 해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(context, "운전자 자격번호는 9자리로 입력해주세요.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });



        //수정완료 버튼
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (driverName.getText().length() < 1){
                    Toast.makeText(context, "성함을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }else if (driverLicenseNum.getText().length() < 1){
                    Toast.makeText(context, "운전자격증 번호 9자리를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
//                else if (driverIdentiNum.getText().toString().length() != 4){
//                    Toast.makeText(context, "사원번호 4자리를 입력해주세요.", Toast.LENGTH_SHORT).show();
//                }
                else {
                    // 데이터 INSERT
                    if(driverLicenseNum.getText().toString().length() == 9){

                        Info.sqlite.updateMember(driverName.getText().toString(), driverLicenseNum.getText().toString(),driver_num, driverIdentiNum.getText().toString() );
                        finish();

                    }else{
                        Toast.makeText(context, "운전자 자격번호는 9자리로 입력해주세요.", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

    }//onCreate..



    private void initializecontents(int nTP){
        if (nTP == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_register_driver);
        }else {
            setContentView(R.layout.activity_register_driver_h);
        }
    }

}//RegisterDriverActivity
