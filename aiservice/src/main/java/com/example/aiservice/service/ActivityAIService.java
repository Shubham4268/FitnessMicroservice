package com.example.aiservice.service;

import com.example.aiservice.Repository.RecommendationRepository;
import com.example.aiservice.model.Activity;
import com.example.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {

    private final GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity){
        String prompt = createPromptForActivity(activity);
        String AIResponse = geminiService.getAnswer(prompt);

//        log.info("RESPONSE FROM AI: {}", AIResponse);
        return processAiResponse(activity,AIResponse);
    }

    private Recommendation processAiResponse(Activity activity, String aiResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(aiResponse);

            JsonNode textNode = rootNode.path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text");

            String jsonContent = textNode.asString()
                    .replaceAll("```json\\n","")
                    .replaceAll("\\n```","")
                    .trim();

//            log.info("Parsed response from AI: ", jsonContent );

            JsonNode analysisJson = mapper.readTree(jsonContent);
            JsonNode analysisNode = analysisJson.path("analysis");
            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSections(fullAnalysis, analysisNode, "overall" , "OverAll: ");
            addAnalysisSections(fullAnalysis, analysisNode, "pace" , "Pace: ");
            addAnalysisSections(fullAnalysis, analysisNode, "heartRate" , "Heart Rate: ");
            addAnalysisSections(fullAnalysis, analysisNode, "caloriesBurned" , "Calories: ");

            List<String> improvements = extractImprovements(analysisJson.path("improvements"));
            List<String> suggestions = extractSuggestions(analysisJson.path("suggestions"));
            List<String> safety = extractSafetyGuidelines(analysisJson.path("safety"));

            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getType())
                    .recommendation(fullAnalysis.toString().trim())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safety(safety)
                    .createdAt(LocalDateTime.now())
                    .build();
        }
        catch (Exception e){
            e.printStackTrace();
            return generateDefaultRecommendation(activity);
        }
    }

    private Recommendation generateDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .activityType(activity.getType())
                .recommendation(
                        "Keep maintaining consistency in your activity. Regular effort matters more than intensity."
                )
                .improvements(List.of(
                        "Increase consistency if activity frequency is low",
                        "Ensure proper warm-up and cool-down",
                        "Track progress weekly"
                ))
                .suggestions(List.of(
                        "Stay hydrated",
                        "Maintain proper posture",
                        "Allow adequate recovery time"
                ))
                .safety(List.of(
                        "Avoid overtraining",
                        "Stop immediately if you feel pain or dizziness"
                ))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<String> extractSafetyGuidelines(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();
        if (safetyNode.isArray()){
            safetyNode.forEach(item->{
                safety.add(item.asString());
            });

        }
        return safety.isEmpty() ?
                Collections.singletonList("Follow general safety protocols") :
                safety;
    }

    private List<String> extractSuggestions(JsonNode suggestionNode) {
        List<String> suggestions = new ArrayList<>();
        if (suggestionNode.isArray()){
            suggestionNode.forEach(suggestion->{
                String workout = suggestion.path("workout").asString();
                String description = suggestion.path("description").asString();
                suggestions.add(String.format("%s: %s", workout, description));
            });
    }
        return suggestions.isEmpty() ?
                Collections.singletonList("No specific Suggestions provided") :
                suggestions;
    }

    private List<String> extractImprovements(JsonNode improvementNode) {
        List<String> improvements = new ArrayList<>();
        if (improvementNode.isArray()){
            improvementNode.forEach(improvement->{
                String area = improvement.path("area").asString();
                String detail = improvement.path("recommendation").asString();
                improvements.add(String.format("%s: %s", area, detail));
            });

        }
        return improvements.isEmpty() ?
                Collections.singletonList("No specific improvements provided") :
                improvements;
    }

    private void addAnalysisSections(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if (!analysisNode.path(key).isMissingNode()){
            fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asString())
                    .append("\n\n");
        }
    }


    private String createPromptForActivity(Activity activity) {
        return String.format("""
        Analyze this fitness activity and provide detailed recommendations in the following EXACT JSON format:
        {
          "analysis": {
            "overall": "Overall analysis here",
            "pace": "Pace analysis here",
            "heartRate": "Heart rate analysis here",
            "caloriesBurned": "Calories analysis here"
          },
          "improvements": [
            {
              "area": "Area name",
              "recommendation": "Detailed recommendation"
            }
          ],
          "suggestions": [
            {
              "workout": "Workout name",
              "description": "Detailed workout description"
            }
          ],
          "safety": [
            "Safety point 1",
            "Safety point 2"
          ]
        }

        Analyze this activity:
        Activity Type: %s
        Duration: %d minutes
        Calories Burned: %d
        Additional Metrics: %s
        
        Provide detailed analysis focusing on performance, improvements, next workout suggestions, and safety guidelines.
        Ensure the response follows the EXACT JSON format shown above.
        """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }

    private final RecommendationRepository recommendationRepository;
    public void getAllRecommendations(){
        List<Recommendation> recommendations = new ArrayList<>();
        recommendations = recommendationRepository.findAll();
//        log.info("List of all the recommendations {}", recommendations);
    }



}