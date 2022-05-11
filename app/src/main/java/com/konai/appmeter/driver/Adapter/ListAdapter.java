package com.konai.appmeter.driver.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.konai.appmeter.driver.R;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {
    public Context context;
    LayoutInflater inflater = null;
    private ArrayList<ListItem> m_oData = null;
    private int nListCnt = 0;
    public int pos;
    int mtcnt;
    int finalCnt = 0;

    public interface OnitemClickListener{
        void onItemClick(View v, int pos);
    }

    private ListAdapter.OnitemClickListener mListener = null;

    public void setmListener(ListAdapter.OnitemClickListener mListener){
        this.mListener = mListener;
    }



    public ListAdapter(Context mcontext, ArrayList<ListItem> _oData) {
        context = mcontext;
        m_oData = _oData;
        nListCnt = m_oData.size();
    }



    @Override
    public int getCount()
    {
        return nListCnt;
//        return recordSize;
    }

    @Override
    public Object getItem(int position) {return null;}

    @Override
    public long getItemId(int position) {return 0;}

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView == null)
        {
            //final Context context = parent.getContext();
            context = parent.getContext();
            pos = position;
            if(inflater == null)
            {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }


            convertView = inflater.inflate(R.layout.listview_todayrecord, parent, false);
        }



        TextView payDivision = (TextView) convertView.findViewById(R.id.pay_div);   //결제방법
        TextView pay_date = (TextView) convertView.findViewById(R.id.pay_date);
        TextView tv_dtlstarttime = (TextView) convertView.findViewById(R.id.tv_dtlstarttime); //출발시간
        TextView tv_dtlendtime = (TextView) convertView.findViewById(R.id.tv_dtlendtime);     //도착시간
        TextView tv_dtlpayment = (TextView) convertView.findViewById(R.id.tv_dtlpayment);     //요금
        TextView distance = (TextView) convertView.findViewById(R.id.distance);

        //Log.d("ttttttt", m_oData.get(position).drvPayDivision+", "+m_oData.get(position).drvDivision);

        if (m_oData.get(position).drvPayDivision == 0){
            payDivision.setText("현금결제");
        }else {
            payDivision.setText("카드결제");
        }

        //pay_date.setText(m_oData.get(position).eDate.substring(0,11));  //todo: 결제날짜 다시 설정 -> elapes ???
        //결제날짜

        if (m_oData.get(position).eDate.length() == 19){
            pay_date.setText(m_oData.get(position).eDate.substring(0,10));
        }else {
            pay_date.setText(m_oData.get(position).eDate);
        }

        //시작일
        tv_dtlstarttime.setText(m_oData.get(position).sDate);

        //종료일
        if (m_oData.get(position).eDate.length() == 19){
            tv_dtlendtime.setText(m_oData.get(position).eDate.substring(11,19));
        } else {
            tv_dtlendtime.setText(m_oData.get(position).eDate);
        }
        //결제
        tv_dtlpayment.setText((m_oData.get(position).drvPay + m_oData.get(position).addPay) + " 원");

        //거리
        distance.setText(String.format("%.2f", m_oData.get(position).distance / 1000.0) + "km");
        Log.d("distance_check", m_oData.get(position).distance+"!!");


        /*convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onItemClick(v, pos);
                }
            }
        });
*/

        getRecordCnt();

        return convertView;
    }




    //todo: 20211123
    // 결제취소 시
    // 총 요금에서 (-) 있는 요금 제외하고 카운팅하기..
    public int return_cnt(){
        if (m_oData.size() > 0){

            mtcnt = m_oData.size();
            int mFare = 0;
            String finalFare = "";

            for (int i=0; i < m_oData.size(); i++){
//                recordSize = i+1;
                Log.d("m_record_size", mtcnt+"");
                mFare = (m_oData.get(i).drvPay) + (m_oData.get(i).addPay);
                Log.d("mFare", mFare+"");
                finalFare = mFare+"";

                if (finalFare.contains("-")){
//                    recordSize--;  // 1- 카드결제취소 (취소만 빼기)
                    mtcnt = mtcnt-2;  // 1- 카드결제, 1- 카드결제취소 (둘다빼기)

                }else {
//                    Log.d("m_record_size_#", recordSize+"");
                }
            }
            Log.d("mFare","---------------------------------------");
            Log.d("m_record_size_final", mtcnt+"");

//            SharedPreferences pref = context.getSharedPreferences("tfare", MODE_PRIVATE);  //생성자를 통해 ListAdapter 의 context 를 가져옴
//            SharedPreferences.Editor editor = pref.edit();
//            editor.putInt("tcnt_", mtcnt);
//            editor.putInt("distE", mtddistanceE);
//            editor.putInt("distB", mtddistanceB);
//            editor.putInt("tfare", mtfare);
//            editor.commit();
        }
        return mtcnt;
    }
    //todo: end




    //todo: 20211123
    // 마지막 데이터 결제취소 시 -
    // 마지막 데이터와 같은 drvCode()를 확인하여 둘다 count 제외시키기.
    public int getRecordCnt(){
        if (m_oData.size() > 0){

            mtcnt = m_oData.size();

            for (int i=0; i < m_oData.size(); i++){
                String drvCode = m_oData.get(i).drvCode;

                Log.d("check_drvCode", drvCode);
                Log.d("check_drvCode","----------------------------------");
            }
        }


        return 0;
    }
    //todo: end


}
