package com.konai.appmeter.driver.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.konai.appmeter.driver.DB.SQLiteControl;
import com.konai.appmeter.driver.DB.SQLiteHelper;
import com.konai.appmeter.driver.MemberCertActivity;
import com.konai.appmeter.driver.RegisterDriverActivity;
import com.konai.appmeter.driver.R;
import com.konai.appmeter.driver.setting.Info;

import java.util.ArrayList;

public class Dlg_Select_Driver extends Dialog {

    SQLiteHelper helper;
    SQLiteControl sqlite;

    String[] splt;

    Context context;
    ImageView registerDriver;

    ArrayList<Driver_Item> driverItem = new ArrayList<>();
    RecyclerView recycler;
    RecyclerDriverAdapter driverAdapter;

    public Dlg_Select_Driver(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dlg_select_driver);
        context = getContext();



        //get driver member
        helper = new SQLiteHelper(context);
        sqlite = new SQLiteControl(helper);

        //me: 선택할 운전자 리스트..
        // DB open 또는 create
        RecyclerDriverSet();

        //me: 운전자 등록하기 화면으로 이동
        registerDriver = findViewById(R.id.register_driver);
        registerDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, RegisterDriverActivity.class);
                i.putExtra("clickEditBtn", "registerBtn");
                context.startActivity(i);
            }
        });
    }//onCreate..

    public void RecyclerDriverSet(){

        if(null != driverAdapter){
            driverAdapter.items.clear();
            driverAdapter.notifyDataSetChanged();
        }
        String driverMemberList[] = sqlite.selectMemberList();

        Info.G_full_driver_num.clear();
        for (int i=0; i<driverMemberList.length; i++){
            splt = driverMemberList[i].split("#");
            driverItem.add(new Driver_Item(splt[0], splt[2]));  //리사이클러뷰 데이터 splt[0]: 이름/ splt[1]: 운전자 자격증번호
            Info.G_full_driver_num.add(splt[1]);
        }
        driverAdapter = new RecyclerDriverAdapter(context, driverItem);
        recycler = findViewById(R.id.recycler);
        recycler.setAdapter(driverAdapter);
        //driverAdapter 아이템 클릭 리스너..
        driverAdapter.setmListener(new RecyclerDriverAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int post , String type) {

                //Log.d("check_pos", post+"");
                ArrayList<String> dd =  Info.G_full_driver_num;

                if(type.equals("e")){   //edit버튼
                    Intent i = new Intent(context, RegisterDriverActivity.class);
                    i.putExtra("clickEditBtn", "editBtn");
                    i.putExtra("name", driverItem.get(post).driver_name);
//                    i.putExtra("license_num", (driverItem.get(post).driver_num));   // 4자리만 나옴..
                    i.putExtra("license_num", Info.G_full_driver_num.get(post));
                    i.putExtra("identi_num", driverItem.get(post).driver_num);
                    context.startActivity(i);
                }else if (type.equals("d")){ //delete 버튼
                    sqlite.deleteMember(Info.G_full_driver_num.get(post));
                    driverAdapter.items.remove(post);
                    driverAdapter.notifyDataSetChanged();
                }else {   //iteview 클릭
                    dismiss();
                    Info.G_driver_name = driverItem.get(post).driver_name;
                    Info.G_driver_num = driverItem.get(post).driver_num;
                    // 수정
                    Info.G_license_num = Info.G_full_driver_num.get(post);
                }
            }
        });

    }
}//Dlg_Select_Driver_Adapter..

/* 다이얼로그 안에 리사이클러뷰 어댑터 생성.. */
class RecyclerDriverAdapter extends RecyclerView.Adapter{

    Context context;
    ArrayList<Driver_Item> items;
    String dbName;
    String dbLicenseName;
    SQLiteHelper helper;
    SQLiteControl sqlite;

    public RecyclerDriverAdapter(Context context, ArrayList<Driver_Item> driver_items) {
        this.context = context;
        this.items = driver_items;
    }

    public interface OnItemClickListener{
        void onItemClick(View v, int post, String type);
    }

    private OnItemClickListener mListener = null;

    public void setmListener(OnItemClickListener mListener){
        this.mListener = mListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder( ViewGroup parent, int i) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.recycler_select_driver, parent, false);

        VH holder = new VH(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder( RecyclerView.ViewHolder holder, int position) {
        VH vh = (VH) holder;

        vh.tvDriverName.setText( items.get(position).driver_name);
        vh.tvDriverNum.setText( items.get(position).driver_num);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class VH extends RecyclerView.ViewHolder{
        TextView tvDriverName;
        TextView tvDriverNum;
        Button editBtn;
        Button deleteBtn;

        public VH( View itemView) {
            super(itemView);

            Log.d("dfasdf","Asdfasdf");

            helper = new SQLiteHelper(context);
            sqlite = new SQLiteControl(helper);

            tvDriverName = itemView.findViewById(R.id.tv_driver_name);
            tvDriverNum = itemView.findViewById(R.id.tv_driver_num);
            editBtn = itemView.findViewById(R.id.edit_btn);
            deleteBtn = itemView.findViewById(R.id.delete_btn);

            //삭제버튼
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION){
                        if (mListener != null){
                            mListener.onItemClick(v, pos, "d");
                        }
                    }
                }
            });

            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION){
                        if (mListener != null){
                            mListener.onItemClick(v, pos, "e");
                        }
                    }
                }
            });


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION){
                        if (mListener != null){
                            mListener.onItemClick(v, pos, "i");
                        }
                    }
                }
            });
        }
    }//VH..
}