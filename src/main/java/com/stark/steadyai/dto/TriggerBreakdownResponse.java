package com.stark.steadyai.dto;

public class TriggerBreakdownResponse {

    private String trigger;
    private int count;

    public TriggerBreakdownResponse() {
    }

    public TriggerBreakdownResponse(String trigger, int count) {
        this.trigger = trigger;
        this.count = count;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
