package com.logiclinter.logiclinter_backend.model;

public class AnalysisRequest {
    private String language;
    private String code;

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}