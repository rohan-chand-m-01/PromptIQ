package service;

/**
 * Intelligent Model Recommendation Engine
 * 
 * Purpose:
 * This class coordinates the cost-effective routing rules of prompts.
 * It recommends the most appropriate LLM engine model variant based on complexity.
 * This prevents over-paying for a massive model on trivial queries and guarantees 
 * that highly complex tasks get routed to robust reasoning architectures.
 * 
 * Recommendation Heuristics:
 * - Simple Prompts (<= 50 tokens): Recommended for Gemini Flash (highly cost-efficient, ultra-fast).
 * - Moderate Prompts (<= 150 tokens): Recommended for Gemini Pro (general versatile performance).
 * - Complex Prompts (> 150 tokens): Recommended for Gemini Advanced (deep reasoning, highly complex tasks).
 */
public class ModelSelector {

    /**
     * Recommends a specific Gemini model based on estimated prompt tokens.
     * 
     * @param tokens The estimated token count of the prompt.
     * @return The recommended model name.
     */
    public String recommendModel(int tokens) {
        String model;
        if (tokens <= 50) {
            model = "Gemini Flash";
        } else if (tokens <= 150) {
            model = "Gemini Pro";
        } else {
            model = "Gemini Advanced";
        }
        System.out.println("[ModelSelector] Tokens: " + tokens + " → Recommended: " + model);
        return model;
    }
}
