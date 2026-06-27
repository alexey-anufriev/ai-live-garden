# Garden Chronicle Prompt

Write exactly one story handoff JSON file at `.story-run.json`. Do not edit files under `story/` directly.

Use the previous/current garden snapshots, diff, and recent events as factual grounding. Write a quiet fairy-tale, naturalist-myth, or living-chronicle chapter, not a technical report.

Required JSON shape:

```json
{
  "volumeTitle": "Title for the volume, required only when the context says this starts a new volume",
  "chapterTitle": "Title based on the actual garden events",
  "chapterBody": "300-700 words of narrative prose"
}
```

Rules:

1. Create only `.story-run.json`.
2. Do not modify source code, workflows, `AGENTS.md`, `GEMINI.md`, `data/garden-state.txt`, `story/`, or `story/last-narrated-garden-state.txt`.
3. Do not include raw statistics, implementation jargon, Markdown headings, or unsupported major events in `chapterBody`.
4. Keep existing volume titles unchanged. If this continues an existing volume, set `volumeTitle` to an empty string.
