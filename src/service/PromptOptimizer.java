package service;

/**
 * AI Prompt Optimization Coordinator
 * 
 * Purpose:
 * This service coordinates prompt rewriting. It wraps raw developer prompts in strict 
 * system instructions (instructing the model to return *only* the clean optimized prompt 
 * and omit explanations, conversational text, or preambles) and dispatches it to the 
 * Gemini API client.
 */

import api.GeminiClient;

public class PromptOptimizer {

    // Instantiate the direct Google Gemini REST client
    private GeminiClient geminiClient = new GeminiClient();

    /**
     * Re-writes weak or wordy prompts for maximum clarity and token efficiency using the Gemini API.
     * 
     * @param originalPrompt The raw prompt text inputted by the developer.
     * @return The polished, AI-optimized rewrite of the prompt.
     */
    public String optimizePrompt(String originalPrompt) {
        System.out.println("[PromptOptimizer] Optimizing prompt...");
        System.out.println("[PromptOptimizer] Original: " + originalPrompt);

        // Define a strict optimization system instruction context to enforce precise output formats.
        String instruction = "Improve the following prompt for an AI system. " +
            "Return ONLY the optimized prompt, nothing else, no explanation, no preamble.\n\n" +
            "Prompt: " + originalPrompt;

        // Call the Gemini API and extract response text
        String optimized = geminiClient.generateContent(instruction);
        System.out.println("[PromptOptimizer] Optimized: " + optimized);
        return optimized;
    }
}
