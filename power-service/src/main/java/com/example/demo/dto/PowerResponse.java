package com.example.demo.dto;

public class PowerResponse {
    private Integer requiredWattage;
    private String recommendation;

    public PowerResponse(Integer requiredWattage, String recommendation) {
        this.requiredWattage = requiredWattage;
        this.recommendation = recommendation;
    }

    public Integer getRequiredWattage() {
        return requiredWattage;
    }

    public void setRequiredWattage(Integer requiredWattage) {
        this.requiredWattage = requiredWattage;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}