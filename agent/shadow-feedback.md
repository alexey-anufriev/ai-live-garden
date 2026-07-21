# Autonomous Experiment Verdict

This verdict evaluates the safe code committed by the previous autonomous run. Shadow evaluation is evidence for the next iteration, not a merge gate. The next agent must inspect the current implementation and explicitly choose to keep, revise, or revert it.

- Classification: `wrong-direction`
- Acceptance: `experiment`
- PM direction: `D`
- Metric: `population.BEETLE`
- Goal: `increase`
- Required delta: 1
- Observed delta: -162
- Baseline average: 170
- Candidate average: 8
- Safety passed: true
- Target passed: false

## Implemented Hypothesis

Enhanced beetle-recovery trait threshold from 10 to 200.

## Harness Conclusion

The code was safe but moved the metric in the wrong direction. Correct or revert this committed mechanism in the next run before adding another mechanism for the same objective.

## Required Next Decision

Set `causalReach.previousFeedbackDecision` to `reuse`, `revise`, or `abandon` and explain the decision with current-state evidence. Because this code is already on main, inspect and change the implementation directly; there is no rejected branch to recover.


## Harness Finalization

The accepted source and measured verdict were preserved, but the garden tick and generated-memory transaction were rolled back because: accepted-finalization=success; AUTO_MEMORY_OUTCOME=failure,SYNC_JOURNAL_OUTCOME=skipped,REQUIRED_MEMORY_OUTCOME=skipped,JOURNAL_FORMAT_OUTCOME=skipped,SUMMARY_FORMAT_OUTCOME=skipped,SUMMARY_APPEND_ONLY_OUTCOME=skipped,ARCHIVE_JOURNAL_OUTCOME=skipped,ARCHIVE_SUMMARIES_OUTCOME=skipped,AGENT_WORKTREE_OUTCOME=skipped,RECORD_VERDICT_OUTCOME=skipped,AGENT_WORKTREE_SEVERITY=missing.
