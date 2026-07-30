package com.logiclinter.logiclinter_backend.model;

public class AnalysisResponse {
    private String analysisJson;

    public AnalysisResponse(String analysisJson) {
        this.analysisJson = analysisJson;
    }

    public String getAnalysisJson() { return analysisJson; }
    public void setAnalysisJson(String analysisJson) { this.analysisJson = analysisJson; }
}