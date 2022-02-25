package com.konai.appmeter.driver.VO;

public class RecordVO {

    private String drvCode;
    private int drvDivision;
    private int drvPay;
    private int drvPayDivision;
    private int addPay;
    private String coordsX;
    private String coordsY;
    private String sDate;
    private String eDate;
    private String distance;

    public void setsDate(String sDate) {
        this.sDate = sDate;
    }

    public void seteDate(String eDate) {
        this.eDate = eDate;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDrvCode() {
        return drvCode;
    }

    public void setDrvCode(String drvCode) {
        this.drvCode = drvCode;
    }

    public int getDrvDivision() {
        return drvDivision;
    }

    public void setDrvDivision(int drvDivision) {
        this.drvDivision = drvDivision;
    }

    public int getDrvPay() {
        return drvPay;
    }

    public void setDrvPay(int drvPay) {
        this.drvPay = drvPay;
    }

    public int getDrvPayDivision() {
        return drvPayDivision;
    }

    public void setDrvPayDivision(int drvPayDivision) {
        this.drvPayDivision = drvPayDivision;
    }

    public int getAddPay() {
        return addPay;
    }

    public void setAddPay(int addPay) {
        this.addPay = addPay;
    }

    public String getCoordsX() {
        return coordsX;
    }

    public void setCoordsX(String coordsX) {
        this.coordsX = coordsX;
    }

    public String getCoordsY() {
        return coordsY;
    }

    public void setCoordsY(String coordsY) {
        this.coordsY = coordsY;
    }

    public String getsDate() {
        return sDate;
    }

    public void setSdate(String sDate) {
        this.sDate = sDate;
    }

    public String geteDate() {
        return eDate;
    }

    public void setEdate(String eDate) {
        this.eDate = eDate;
    }


    @Override
    public String toString() {
        return "RecordVO{" +
                "drvCode='" + drvCode + '\'' +
                ", drvDivision=" + drvDivision +
                ", drvPay=" + drvPay +
                ", drvPayDivision=" + drvPayDivision +
                ", addPay=" + addPay +
                ", coordsX='" + coordsX + '\'' +
                ", coordsY='" + coordsY + '\'' +
                ", sDate='" + sDate + '\'' +
                ", eDate='" + eDate + '\'' +
                ", distance='" + distance + '\'' +
                '}';
    }
}
