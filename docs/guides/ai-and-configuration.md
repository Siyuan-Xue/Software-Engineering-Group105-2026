# AI And Configuration

AI features are optional. Without `QWEN_API_KEY`, the UI disables direct Qwen calls and persisted match analysis falls back to rule-based scoring.

## Environment Variables

| Variable | Required | Purpose |
|---|---|---|
| `QWEN_API_KEY` | For AI calls | DashScope API key. |
| `QWEN_MODEL` | No | Vision/multimodal resume model. |
| `QWEN_TEXT_MODEL` | No | Text model for matching, ranking, and drafting. |

## Consent

Every user-facing AI action is gated by an in-app disclaimer. Users must explicitly click the agree button before the browser sends the request.

Server endpoints also enforce that decision: AI requests must include `aiConsent=true` or `X-AI-Consent: accepted`. Accepted requests write an `AI_REQUEST` audit entry with the operator, target, feature name, and consent marker; prompts and full model outputs are not stored in the audit log to reduce privacy exposure.

## Fallback

`MatchingService` persists `match_scores.json` with:

- rule-based score
- AI/fallback score
- final score
- skill coverage
- missing required count
- workload remaining hours
- explanation

When Qwen is unavailable, AI score equals the rule score and the explanation states that fallback was used.
