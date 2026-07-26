---
name: repository-hygiene
description: Audits the repository for portable configurations, cross-platform line endings, missing gitignores, absolute machine paths, .editorconfig compliance, and secrets.
---

# Repository Hygiene Skill

## Execution Steps

1. Run `git status` and check for untracked scratch files, temporary logs, or binary artifacts.
2. Search codebase for hardcoded absolute paths (e.g. `C:/...`, `D:/...`, `/home/user/`).
3. Audit `.editorconfig` compliance:
   - Ensure LF (`\n`) line endings are used across code, markdown, and shell scripts.
   - Ensure CRLF is restricted strictly to Windows `.bat` / `.cmd` scripts.
   - Ensure 4-space indentation for Java, XML, YAML, SQL and 2-space indentation for JS, JSX, TS, TSX, JSON, CSS, HTML.
   - Verify final newlines exist on all files.
4. Audit `.gitignore`: Ensure shared IDE configs (`.idea`, `.vscode`) and AI agent resources (`.agents`) are NOT improperly ignored.
5. Verify secrets audit: Search for JWT keys, AWS tokens, database passwords in tracked files.

## Verification Checklist

- [ ] `.editorconfig` rules strictly satisfied across repository.
- [ ] Zero absolute machine paths committed.
- [ ] Zero secrets or env files committed.
- [ ] Cross-platform build scripts (`.sh` and `.bat`) operate consistently.
