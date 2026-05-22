package com.stark.steadyai.dto;

public class TrendPointResponse {

    private String date;
    private int count;
    private double averageIntensity;

    public TrendPointResponse() {
    }

    public TrendPointResponse(String date, int count, double averageIntensity) {
        this.date = date;
        this.count = count;
        this.averageIntensity = averageIntensity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getAverageIntensity() {
        return averageIntensity;
    }

    public void setAverageIntensity(double averageIntensity) {
        this.averageIntensity = averageIntensity;
    }
}
