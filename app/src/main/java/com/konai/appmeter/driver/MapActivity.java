package com.konai.appmeter.driver;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.konai.appmeter.driver.setting.Info;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PolylineOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.LocationButtonView;

import java.util.ArrayList;
import java.util.List;

//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;


public class MapActivity extends Activity implements OnMapReadyCallback{

    private MapView mapView;
    private LocationButtonView locationButtonView;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    private Button btnPoly;
    PolylineOverlay polyline;

    private String putParamX;
    private String putParamY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent listIntent = getIntent();

        putParamX = listIntent.getExtras().getString("coordsX");
        putParamY = listIntent.getExtras().getString("coordsY");

        btnPoly = findViewById(R.id.polybtn);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        naverMapBasicSettings();


        btnPoly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //polyline.setMap(mapView);
            }
        });

    }

    public void naverMapBasicSettings() {
        mapView.getMapAsync(this);
        //내위치 버튼
        locationButtonView = findViewById(R.id.locationbuttonview);
        // 내위치 찾기 위한 source
        //locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onMapReady(@NonNull final NaverMap naverMap) {
        //naverMap.getUiSettings().setLocationButtonEnabled(true);
        locationButtonView.setMap(naverMap);

        String[] coordsX = putParamX.split("\\|");
        String[] coordsY = putParamY.split("\\|");
        List<LatLng> coords = new ArrayList<>();
        List<Marker> markers = new ArrayList<>();

        Double dX;
        Double dY;
        int ncount = 0;

        for(int i=0; i<coordsX.length; i++)
        {

            if(coordsX[i].isEmpty())
                continue;

            dX = Double.parseDouble(coordsX[i]);
            dY = Double.parseDouble(coordsY[i]);

            if(dX == 0)
                continue;

//            Log.e(i + "번째", coordsX[i] + " / " + coordsY[i]);
            coords.add(new LatLng(dX, dY));
            //markers.add(new LatLng((dX, dY)));
            ncount++;
        }

        if(ncount > 1) //20201013
        {
            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(Double.parseDouble(coordsX[1]), Double.parseDouble(coordsY[1])));
            naverMap.moveCamera(cameraUpdate);

            naverMap.setLocationSource(locationSource);
            naverMap.setLocationTrackingMode(LocationTrackingMode.NoFollow);
            polyline = new PolylineOverlay();
            polyline.setColor(Color.RED);
            polyline.setWidth(10);
            polyline.setJoinType(PolylineOverlay.LineJoin.Round);
            polyline.setCoords(coords);
            polyline.setMap(naverMap);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

//		Log.i("TAg", "---------- screenWidthDp : " + newConfig.screenWidthDp + ", screenHeightDp : " + newConfig.screenHeightDp);

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(Info.m_Service != null)
            Info.m_Service._showhideLbsmsg(true);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(Info.m_Service != null)
            Info.m_Service._showhideLbsmsg(false);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


}
