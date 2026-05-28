package com.stark.steadyai.dto;

import java.util.List;

public class ProgressReportResponse {

    private String generatedAt;
    private String startDate;
    private String endDate;
    private String userDisplayName;

    // Metrics
    private int totalUrgeLogs;
    private double averageUrgeIntensity;
    private String mostCommonTrigger;

    private int completedExposureTasks;
    private int pendingExposureTasks;

    private int completedDelayAttempts;
    private double averageDelayMinutes;

    // Sections
    private String weeklySummary;
    private List<String> keyObservations;
    private List<String> suggestedNextSteps;
    private String safetyNote;

    public ProgressReportResponse() {}

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

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

    public String getMostCommonTrigger() {
        return mostCommonTrigger;
    }

    public void setMostCommonTrigger(String mostCommonTrigger) {
        this.mostCommonTrigger = mostCommonTrigger;
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

    public int getCompletedDelayAttempts() {
        return completedDelayAttempts;
    }

    public void setCompletedDelayAttempts(int completedDelayAttempts) {
        this.completedDelayAttempts = completedDelayAttempts;
    }

    public double getAverageDelayMinutes() {
        return averageDelayMinutes;
    }

    public void setAverageDelayMinutes(double averageDelayMinutes) {
        this.averageDelayMinutes = averageDelayMinutes;
    }

    public String getWeeklySummary() {
        return weeklySummary;
    }

    public void setWeeklySummary(String weeklySummary) {
        this.weeklySummary = weeklySummary;
    }

    public List<String> getKeyObservations() {
        return keyObservations;
    }

    public void setKeyObservations(List<String> keyObservations) {
        this.keyObservations = keyObservations;
    }

    public List<String> getSuggestedNextSteps() {
        return suggestedNextSteps;
    }

    public void setSuggestedNextSteps(List<String> suggestedNextSteps) {
        this.suggestedNextSteps = suggestedNextSteps;
    }

    public String getSafetyNote() {
        return safetyNote;
    }

    public void setSafetyNote(String safetyNote) {
        this.safetyNote = safetyNote;
    }
}
