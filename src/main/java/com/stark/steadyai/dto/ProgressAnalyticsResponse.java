package com.stark.steadyai.dto;

import java.util.List;

public class ProgressAnalyticsResponse {

    private int totalUrgeLogs;
    private double averageUrgeIntensity;
    private int highestUrgeIntensity;

    private int totalExposureTasks;
    private int completedExposureTasks;
    private int pendingExposureTasks;

    private int totalDelayAttempts;
    private int completedDelayAttempts;
    private int cancelledDelayAttempts;
    private double averageDelayMinutes;

    private List<TrendPointResponse> sevenDayUrgeTrend;
    private List<TriggerBreakdownResponse> triggerBreakdown;
    private List<IntensityDistributionResponse> intensityDistribution;
    private List<String> progressObservations;
    private String safetyNote;

    public ProgressAnalyticsResponse() {
    }

    // --- Urge metrics ---

    public int getTotalUrgeLogs() {
        return totalUrgeLogs;
    }

    public void setTotalUrgeLogs(int totalUrgeLogs) {
        this.totalUrgeLogs = totalUrgeLogs;
    }

    public double getAverageUrgeIntensity() {
        return averageUrgeIntensity;
    }

    public void setAverageUrgeIntensity(double averageUrgeIntensity) {
        this.averageUrgeIntensity = averageUrgeIntensity;
    }

    public int getHighestUrgeIntensity() {
        return highestUrgeIntensity;
    }

    public void setHighestUrgeIntensity(int highestUrgeIntensity) {
        this.highestUrgeIntensity = highestUrgeIntensity;
    }

    // --- Exposure metrics ---

    public int getTotalExposureTasks() {
        return totalExposureTasks;
    }

    public void setTotalExposureTasks(int totalExposureTasks) {
        this.totalExposureTasks = totalExposureTasks;
    }

    public int getCompletedExposureTasks() {
        return completedExposureTasks;
    }

    public void setCompletedExposureTasks(int completedExposureTasks) {
        this.completedExposureTasks = completedExposureTasks;
    }

    public int getPendingExposureTasks() {
        return pendingExposureTasks;
    }

    public void setPendingExposureTasks(int pendingExposureTasks) {
        this.pendingExposureTasks = pendingExposureTasks;
    }

    // --- Delay metrics ---

    public int getTotalDelayAttempts() {
        return totalDelayAttempts;
    }

    public void setTotalDelayAttempts(int totalDelayAttempts) {
        this.totalDelayAttempts = totalDelayAttempts;
    }

    public int getCompletedDelayAttempts() {
        return completedDelayAttempts;
    }

    public void setCompletedDelayAttempts(int completedDelayAttempts) {
        this.completedDelayAttempts = completedDelayAttempts;
    }

    public int getCancelledDelayAttempts() {
        return cancelledDelayAttempts;
    }

    public void setCancelledDelayAttempts(int cancelledDelayAttempts) {
        this.cancelledDelayAttempts = cancelledDelayAttempts;
    }

    public double getAverageDelayMinutes() {
        return averageDelayMinutes;
    }

    public void setAverageDelayMinutes(double averageDelayMinutes) {
        this.averageDelayMinutes = averageDelayMinutes;
    }

    // --- Trend and breakdown ---

    public List<TrendPointResponse> getSevenDayUrgeTrend() {
        return sevenDayUrgeTrend;
    }

    public void setSevenDayUrgeTrend(List<TrendPointResponse> sevenDayUrgeTrend) {
        this.sevenDayUrgeTrend = sevenDayUrgeTrend;
    }

    public List<TriggerBreakdownResponse> getTriggerBreakdown() {
        return triggerBreakdown;
    }

    public void setTriggerBreakdown(List<TriggerBreakdownResponse> triggerBreakdown) {
        this.triggerBreakdown = triggerBreakdown;
    }

    public List<IntensityDistributionResponse> getIntensityDistribution() {
        return intensityDistribution;
    }

    public void setIntensityDistribution(List<IntensityDistributionResponse> intensityDistribution) {
        this.intensityDistribution = intensityDistribution;
    }

    // --- Observations and safety ---

    public List<String> getProgressObservations() {
        return progressObservations;
    }

    public void setProgressObservations(List<String> progressObservations) {
        this.progressObservations = progressObservations;
    }

    public String getSafetyNote() {
        return safetyNote;
    }

    public void setSafetyNote(String safetyNote) {
        this.safetyNote = safetyNote;
    }
}
