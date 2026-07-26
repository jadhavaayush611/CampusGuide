# Bug Remediation Prompt Template

```markdown
## Task
Diagnose and resolve the issue: [Issue Description / Error Log].

## Instructions
1. Inspect the full un-truncated stack trace or error log before forming a hypothesis.
2. Locate the root cause in code rather than masking symptoms or applying quiet try/catch blocks.
3. Add a reproducing unit or integration test case.
4. Implement the fix ensuring backward compatibility with existing API contracts.
5. Execute `mvn clean verify` and verify that all test suites pass.
```
