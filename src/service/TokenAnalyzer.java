package service;

/**
 * Token, Complexity, & Cost Analyzer Service
 * 
 * Purpose:
 * This class hosts core business logic algorithms responsible for analyzing prompt strings.
 * It simulates a light LLM tokenizer (heuristically estimating tokens by prompt length),
 * checks for redundant words using string frequency maps, detects weak prompts (lacking context), 
 * and classifies prompt complexity and cost brackets.
 * 
 * Heuristic Rules:
 * - 1 Token ≈ 4 Characters in English text.
 * - Complexity & Cost Brackets: Low (<= 50 tokens), Medium (<= 150 tokens), High (> 150 tokens).
 * - Weak Prompts: Prompts containing less than 10 tokens or less than 3 total words.
 */

import java.util.*;
import java.util.stream.Collectors;

public class TokenAnalyzer {

    /**
     * Estimates the token count of a given prompt using standard character heuristics.
     * In English, 1 token roughly corresponds to 4 characters.
     * 
     * @param prompt The user's input prompt.
     * @return The estimated token count.
     */
    public int estimateTokens(String prompt) {
        if (prompt == null || prompt.isBlank()) return 0;
        int tokens = prompt.trim().length() / 4;
        System.out.println("[TokenAnalyzer] Prompt length: " + prompt.length());
        System.out.println("[TokenAnalyzer] Estimated tokens: " + tokens);
        return tokens;
    }

    /**
     * Categorizes prompt complexity level based on token volume boundaries.
     * 
     * @param tokens The prompt's token count.
     * @return Classification String (LOW, MEDIUM, HIGH).
     */
    public String getComplexity(int tokens) {
        if (tokens <= 50) return "LOW";
        else if (tokens <= 150) return "MEDIUM";
        else return "HIGH";
    }

    /**
     * Categorizes prompt cost category based on token volume boundaries.
     * 
     * @param tokens The prompt's token count.
     * @return Classification String (Low, Medium, High).
     */
    public String getCostCategory(int tokens) {
        if (tokens <= 50) return "Low";
        else if (tokens <= 150) return "Medium";
        else return "High";
    }

    /**
     * Dynamically generates structural optimization recommendations for a prompt.
     * Analyses the string length and checks for redundant duplicate words.
     * 
     * @param prompt The raw user prompt text.
     * @param tokens The estimated token count.
     * @return Practical optimization suggestions.
     */
    public String getSuggestion(String prompt, int tokens) {
        StringBuilder suggestion = new StringBuilder();

        // 1. Expensive prompt check
        if (tokens > 300) {
            suggestion.append("Prompt too expensive. Break it into smaller tasks. ");
        }

        // 2. Vague prompt check
        if (tokens < 10) {
            suggestion.append("Prompt too vague. Add more context and details. ");
        }

        // 3. Duplicate redundancy check (frequency analysis)
        String[] words = prompt.toLowerCase().split("\\s+");
        Map<String, Integer> freq = new HashMap<>();
        for (String w : words) {
            freq.merge(w, 1, Integer::sum);
        }
        
        // Find words longer than 3 characters repeated more than 2 times
        List<String> repeated = freq.entrySet().stream()
            .filter(e -> e.getValue() > 2 && e.getKey().length() > 3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        if (!repeated.isEmpty()) {
            suggestion.append("Remove redundant words: ").append(repeated).append(". ");
        }

        if (suggestion.isEmpty()) {
            suggestion.append("Prompt looks good.");
        }

        return suggestion.toString();
    }

    /**
     * Heuristically determines if a prompt is too weak or lacks context.
     * 
     * @param prompt The raw user prompt text.
     * @param tokens The estimated token count.
     * @return True if prompt is categorized as weak, false otherwise.
     */
    public boolean isWeakPrompt(String prompt, int tokens) {
        return tokens < 10 || prompt.split("\\s+").length < 3;
    }
}
