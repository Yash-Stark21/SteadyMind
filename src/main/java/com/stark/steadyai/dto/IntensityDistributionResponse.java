package com.stark.steadyai.dto;

public class IntensityDistributionResponse {

    private String range;
    private int count;

    public IntensityDistributionResponse() {
    }

    public IntensityDistributionResponse(String range, int count) {
        this.range = range;
        this.count = count;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
