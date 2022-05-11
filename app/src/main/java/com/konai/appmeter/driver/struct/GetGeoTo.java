package com.konai.appmeter.driver.struct; /**
 * geoDistance
 * <p>
 * bearingP1toP2
 * <p>
 * geoMove
 * <p>
 * https://github.com/3chamchi/Location
 *
 * @author Republic of Korea
 * @author chamchi
 * @since 2019. 7. 25
 */

import com.konai.appmeter.driver.setting.Info;

import java.text.DecimalFormat;

public class GetGeoTo {


    final static int RADIUS = 6371;
    /**
     * 두 좌표 거리 구하기
     * @param latitude1 Start latitude
     * @param longitude1 Start longitude
     * @param latitude2 End latitude
     * @param longitude2 End longitude
     * @return Distance(m)
     */
    public static double geoDistance(double latitude1, double longitude1, double latitude2, double longitude2) {

        DecimalFormat df = new DecimalFormat("#.#####");

        if ((latitude1 == latitude2) && (longitude1 == longitude2)) {
            return 0;
        } else {
            double theta = longitude1 - longitude2;
            double distance = Math.sin(Math.toRadians(latitude1)) * Math.sin(Math.toRadians(latitude2)) +
                    Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2)) * Math.cos(Math.toRadians(theta));
            distance = Math.acos(distance);
            distance = Math.toDegrees(distance);
            distance = distance * 60 * 1.1515;
            distance = distance * 1.609344;

            //distance --> 0보다 클 경우
            if (0 < distance) {
                distance = Double.valueOf(df.format(distance));    //소숫점 다섯째 자리에서 반올림

                // distance --> 0일 경우
            } else {
                distance = 0;
            }

            distance = distance * 1000; //km --> m으로 변환

            return distance;
        }

    }

    /**
     * 두 좌표 방위각 구하기
     * @param latitude1 Start latitude
     * @param longitude1 Start longitude
     * @param latitude2 End latitude
     * @param longitude2 End longitude
     * @return bearing
     */
    public static short bearingP1toP2(double longitude1, double latitude1, double longitude2, double latitude2) {
        // 현재 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에 라디안 각도로 변환한다.
        double Cur_Lat_radian = latitude1 * (Math.PI / 180);
        double Cur_Lon_radian = longitude1 * (Math.PI / 180);


        // 목표 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에 라디안 각도로 변환한다.
        double Dest_Lat_radian = latitude2 * (Math.PI / 180);
        double Dest_Lon_radian = longitude2 * (Math.PI / 180);

        // radian distance
        double radian_distance = 0;
        radian_distance = Math.acos(Math.sin(Cur_Lat_radian) * Math.sin(Dest_Lat_radian)
                + Math.cos(Cur_Lat_radian) * Math.cos(Dest_Lat_radian) * Math.cos(Cur_Lon_radian - Dest_Lon_radian));

        // 목적지 이동 방향을 구한다.(현재 좌표에서 다음 좌표로 이동하기 위해서는 방향을 설정해야 한다. 라디안값이다.
        double radian_bearing = Math.acos((Math.sin(Dest_Lat_radian) - Math.sin(Cur_Lat_radian)
                * Math.cos(radian_distance)) / (Math.cos(Cur_Lat_radian) * Math.sin(radian_distance)));// acos의 인수로 주어지는 x는 360분법의 각도가 아닌 radian(호도)값이다.

        double true_bearing = 0;
        if (Math.sin(Dest_Lon_radian - Cur_Lon_radian) < 0) {
            true_bearing = radian_bearing * (180 / Math.PI);
            true_bearing = 360 - true_bearing;
        } else {
            true_bearing = radian_bearing * (180 / Math.PI);
        }

        return (short) true_bearing;
    }

    /**
     * 특정 좌표에서 방위각, 거리를 가지고 원하는 좌표 값 획득
     * @param latitude latitude
     * @param longitude longitude
     * @param direction_degree direction
     * @param length_degree length
     * @return double[2] location = {longitude, latitude}
     */
    public static double[] geoMove(double longitude, double latitude, double direction_degree, double length_degree) {
        double[] location = new double[2];
        double k_meter = length_degree / 1.609344 / 60 / 1.1515; // / RADIUS;


        double x = longitude + k_meter * Math.cos(direction_degree * Math.PI / 180);
        double y = latitude + k_meter * Math.sin(direction_degree * Math.PI / 180);

        location[0] = x;
        location[1] = y;

        return location;
    }

    public static double[] geoMoveKatec(double longitude, double latitude, double direction_degree, double length_degree) {
        double[] location = new double[2];
        double k_meter = length_degree; // / RADIUS;


        double x = longitude + k_meter * Math.cos(direction_degree * Math.PI / 180);
        double y = latitude + k_meter * Math.sin(direction_degree * Math.PI / 180);

        location[0] = x;
        location[1] = y;

        return location;
    }

    public static double[] geoMoveWgs84(double longitude, double latitude, double direction_degree, double length_degree) {
        double[] location = new double[2];
        double k_meter = length_degree; // / RADIUS;

        GeoPoint in_pt = new GeoPoint(longitude, latitude);
        GeoPoint tm_pt = GeoTrans.convert(GeoTrans.GEO, GeoTrans.KATEC, in_pt);

        double x = tm_pt.getX() + k_meter * Math.cos(direction_degree * Math.PI / 180);
        double y = tm_pt.getY() + k_meter * Math.sin(direction_degree * Math.PI / 180);

        in_pt.x = x;
        in_pt.y = y;
        tm_pt = GeoTrans.convert(GeoTrans.KATEC, GeoTrans.GEO, in_pt);

        location[0] = tm_pt.x;
        location[1] = tm_pt.y;

        return location;
    }

    public static double[] P1P2toP3CompWgs84(double longitude1, double latitude1,
                                          double longitude2, double latitude2, double longitude3, double latitude3, double dist)
    {
        short bearing1 = bearingP1toP2(longitude1, latitude1, longitude2, latitude2);
        short bearing2 = bearingP1toP2(longitude2, latitude2, longitude3, latitude3);
        short bearing3;
        int angle;
        double[] val = new double[2];

        if(Math.abs(bearing1 - bearing2) < 1)
        {
            angle = Math.abs(450 - bearing2) % 360;
            val = geoMoveWgs84(longitude2, latitude2, angle, dist);
//            return geoMoveWgs84(longitude2, latitude2, (bearing2 - bearing1) / 2 + bearing1, dist);

        }
        else
        {

            angle = Math.abs(450 - Math.abs((bearing1 - bearing2) / 2 + bearing2)) % 360;
            val = geoMoveWgs84(longitude2, latitude2, angle, dist);

//            return geoMoveWgs84(longitude2, latitude2, (bearing1 - bearing2) / 2 + bearing2, dist);

        }

        bearing3 = bearingP1toP2(longitude2, latitude2, val[0], val[1]);

        Info._displayLOG(Info.LOGDISPLAY, "bearing " + bearing1 + " " + bearing2 + " " + bearing3 + " " + (360 - bearing3), "");

        return val;

    }

}