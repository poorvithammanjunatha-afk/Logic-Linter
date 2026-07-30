package com.logiclinter.logiclinter_backend.controller;

import com.logiclinter.logiclinter_backend.model.AnalysisRequest;
import com.logiclinter.logiclinter_backend.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AnalysisController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeCode(@RequestBody AnalysisRequest request) {
        String result = geminiService.analyzeCode(request.getLanguage(), request.getCode());
        return ResponseEntity.ok(result);
    }
}