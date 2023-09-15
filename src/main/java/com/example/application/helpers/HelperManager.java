package com.example.application.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HelperManager {
    private static HelperManager instance;

    private final Helper[] helpersList;
    private Helper currentHelper;
    private final List<Map<String, String>> messageHistory;
    private int totalTokens;

    private HelperManager() {
        helpersList = Helper.values();
        currentHelper = helpersList[0];
        messageHistory = new ArrayList<>();
        messageHistory.add(0, Map.of("role", "system", "content", currentHelper.systemMessage));
        totalTokens = 0;
    }

    public static HelperManager getInstance() {
        if (instance == null) {
            instance = new HelperManager();
        }
        return instance;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public Helper[] getHelpersList() {
        return helpersList;
    }

    public void setHelper(Helper helper, String customSystemMessage) {
        currentHelper = helper;

        // Replace the old system message with the new helper's system message
        String newSystemMessage = customSystemMessage != null ? customSystemMessage : helper.systemMessage;
        messageHistory.remove(0);
        messageHistory.add(0, Map.of("role", "system", "content", newSystemMessage));
    }

    public void resetMessageHistory() {
        // Keep only the system message, but reset everything else
        Map<String, String> systemMessage = messageHistory.remove(0);
        messageHistory.clear();
        messageHistory.add(systemMessage);
        totalTokens = 0;
    }

    public String getResponse(String message, String key, String apiVersion,
                                             int tokens, double temperature) {
        String API_ENDPOINT = "https://api.openai.com/v1/chat/completions";
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();
            MediaType mediaType = MediaType.parse("application/json");

            // Add the user's message to the history
            Map<String, String> userMessage = Map.of("role", "user", "content", message);
            messageHistory.add(userMessage);

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("max_tokens", tokens);
            requestData.put("temperature", temperature);
            requestData.put("n", 1);
            requestData.put("model", apiVersion);
            requestData.put("messages", messageHistory);

            RequestBody requestBody = RequestBody.create(mediaType, HelperManager.getInstance().mapToJson(requestData));
            Request request = new Request.Builder()
                    .url(API_ENDPOINT)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + key)
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String responseBody = response.body().string();

                // Parsing the JSON
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(responseBody);

                // Navigate to the 'content' field
                String content = rootNode
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();

                // Total number of tokens used in the query
                totalTokens += rootNode
                        .path("usage")
                        .path("total_tokens")
                        .asInt();

                // After getting the response from the API, add it to the history
                Map<String, String> assistantMessage = Map.of("role", "assistant", "content", content);
                messageHistory.add(assistantMessage);

                System.out.println(responseBody);
                return content;

            } else {
                System.err.println("Request failed with code: " + response.code());
                System.err.println(response.body().string());
                return currentHelper.name+" didn't want to talk because the request failed with code: " + response.code();
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return currentHelper.name+" didn't want to talk because there was an error processing the JSON: " + e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return currentHelper.name+" didn't want to talk because there was an IO exception: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return currentHelper.name+" didn't want to talk because an exception occurred: " + e.getMessage();
        }
    }

    private String mapToJson(Map<String, Object> map) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
