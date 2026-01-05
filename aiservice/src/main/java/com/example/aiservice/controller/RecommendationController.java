package com.example.aiservice.controller;

import com.example.aiservice.model.Recommendation;
import com.example.aiservice.service.ActivityAIService;
import com.example.aiservice.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Scanner;

@Controller
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private ActivityAIService aiService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Recommendation>> getUserRecommendation(@PathVariable String userId){
        return ResponseEntity.ok(recommendationService.getUserRecommendations(userId));
    }

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<Recommendation> getActivityRecommendation(@PathVariable String activityId){
        return ResponseEntity.ok(recommendationService.getActivityRecommendations(activityId));
    }


}