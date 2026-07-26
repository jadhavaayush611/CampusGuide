# Repository Recovery & Environment Setup Workflow

Procedure for restoring lost development assets, configurations, and environment setups.

1. **Audit Workspace**: Inspect existing directories (`.idea`, `.vscode`, `.agents`, `scripts`, `templates`).
2. **Restore .gitignore**: Ensure shared project assets are not ignored while personal state remains hidden.
3. **Restore IDE Configs**: Generate portable IntelliJ (`.idea`) and VS Code (`.vscode`) configurations.
4. **Restore AI Development Assets**: Restore skills, prompts, instructions, and workflows under `.agents/`.
5. **Restore Shared Scripts & Templates**: Populate `scripts/` utility scripts and `templates/` boilerplate.
6. **Execute Full Build Verification**: Validate repository state using `scripts/build.bat` or `scripts/build.sh`.
