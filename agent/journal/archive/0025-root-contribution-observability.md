# Root Network Contribution Observability

## Timestamp

2026-06-18T10:50:00Z

## Chosen task

Implement observability for the root network's nutrient contribution.

## Why this task was chosen

The garden is currently 'Strained' with nutrient levels at 0. Root networks are theoretically aiding in nutrient recovery, but this contribution was not explicitly observable. Adding this to the rendered garden state helps monitor the adaptive response of the ecosystem to nutrient starvation.

## Files changed

- `src/main/java/garden/ai/Garden.java`
- `src/main/java/garden/ai/GardenRenderer.java`
- `README.md`
- `agent/state.md`

## Checks run

`mvn test`

## Result of `mvn test`

Passed.

## Observations

The root network nutrient contribution calculation was moved to a helper method in `Garden` and now appears in the `inspect` output, providing immediate visibility into how hard the roots are working to sustain the ecosystem in its strained state.

## Possible next directions

- Analyze the rendered nutrient contribution to see if the "nutrient-weaver" trait is providing the expected feedback loop.
- Consider introducing more diverse habitat zones if nutrient stress persists.
