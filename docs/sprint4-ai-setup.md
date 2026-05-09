# Sprint 4 AI Setup

The Sprint 4 admin and applicant analytics features load their runtime settings from `src/main/resources/llm.properties`.

## Keys

- `ai.enabled`: turns the live AI integration on or off
- `ai.baseUrl`: base URL for the OpenAI-compatible endpoint
- `ai.apiKey`: API key used for the request
- `ai.model`: model name passed to the endpoint
- `ai.timeoutSeconds`: request timeout in seconds

## Local Development

For local testing without external calls, set `ai.enabled=false`. The app will keep the baseline analytics logic so the admin and applicant flows still work end to end.
