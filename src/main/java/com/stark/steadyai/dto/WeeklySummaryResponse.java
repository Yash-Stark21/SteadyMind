package com.stark.steadyai.dto;

import java.time.LocalDate;

public class WeeklySummaryResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private int totalUrgeLogs;
    private double averageIntensity;
    private String mostCommonTrigger;
    private String highestRiskPeriod;
    private String progressObservations;
    private String recurringPatterns;
    private String suggestedNextSteps;
    private String safetyNote;

    public WeeklySummaryResponse() {
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getTotalUrgeLogs() {
        return totalUrgeLogs;
    }

    public void setTotalUrgeLogs(int totalUrgeLogs) {
        this.totalUrgeLogs = totalUrgeLogs;
    }

    public double getAverageIntensity() {
        return averageIntensity;
    }

    public void setAverageIntensity(double averageIntensity) {
        this.averageIntensity = averageIntensity;
    }

    public String getMostCommonTrigger() {
        return mostCommonTrigger;
    }

    public void setMostCommonTrigger(String mostCommonTrigger) {
        this.mostCommonTrigger = mostCommonTrigger;
    }

    public String getHighestRiskPeriod() {
        return highestRiskPeriod;
    }

    public void setHighestRiskPeriod(String highestRiskPeriod) {
        this.highestRiskPeriod = highestRiskPeriod;
    }

    public String getProgressObservations() {
        return progressObservations;
    }

    public void setProgressObservations(String progressObservations) {
        this.progressObservations = progressObservations;
    }

    public String getRecurringPatterns() {
        return recurringPatterns;
    }

    public void setRecurringPatterns(String recurringPatterns) {
        this.recurringPatterns = recurringPatterns;
    }

    public String getSuggestedNextSteps() {
        return suggestedNextSteps;
    }

    public void setSuggestedNextSteps(String suggestedNextSteps) {
        this.suggestedNextSteps = suggestedNextSteps;
    }

    public String getSafetyNote() {
        return safetyNote;
    }

    public void setSafetyNote(String safetyNote) {
        this.safetyNote = safetyNote;
    }
}
