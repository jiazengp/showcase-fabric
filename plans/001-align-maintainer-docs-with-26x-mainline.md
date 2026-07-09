# Plan 001: Align maintainer docs with the 26.x / Next.js mainline reality and ignore local plans

> **Executor instructions**: Follow this plan step by step. Run every
> verification command and confirm the expected result before moving to the
> next step. If anything in the "STOP conditions" section occurs, stop and
> report — do not improvise. When done, update the status row for this plan
> in `plans/README.md` — unless a reviewer dispatched you and told you they
> maintain the index.
>
> **Drift check (run first)**: `git diff --stat 6bb2aa5..HEAD -- AGENTS.md docs/CLAUDE.md`
> If any in-scope file changed since this plan was written, compare the
> "Current state" excerpts against the live code before proceeding; on a
> mismatch, treat it as a STOP condition.

## Status

- **Priority**: P2
- **Effort**: S
- **Risk**: LOW
- **Depends on**: none
- **Category**: docs
- **Planned at**: commit `6bb2aa5`, 2026-07-09

## Why this matters

`AGENTS.md` is the maintainer-facing entrypoint for future human and agent work,
but it currently describes a repo that no longer exists: it says the docs site
uses Nuxt, still frames validation around `runShowcaseE2ETest`, and says each
version has Yarn mappings. Those statements now conflict with the live repo,
which uses a Next.js/Fumadocs docs app and a 26.x+ official-naming mainline.
Separately, the local `plans/` directory is advisor output and should not be
committed accidentally. Leaving both issues in place increases the chance that
future maintenance work is planned against the wrong architecture or that local
planning artifacts leak into version control.

## Current state

- Relevant files and their roles:
  - `AGENTS.md` — maintainer guidance for the root mod repo; this is the file currently drifting from reality.
  - `docs/CLAUDE.md` — maintainer guidance for the docs app; use this as the factual source for docs stack and docs commands.
  - `docs/package.json` — the executable source of truth for docs scripts and framework entrypoints.
  - `build.gradle` — the executable source of truth for the mod build system and 26.x+ version targeting.
  - `.gitignore` — the root ignore list; this is where the local `plans/` directory should be excluded from accidental commits.

- Current excerpts:
  - `AGENTS.md:11-12`
    - `./gradlew runShowcaseE2ETest`
    - `./gradlew runShowcaseE2ETest -PmcVer=26.2`
  - `AGENTS.md:29-31`
    - `cd docs && pnpm install && pnpm dev`
    - `Documentation uses Nuxt.js with multi-language support`
  - `AGENTS.md:128-139`
    - `Tests run in real Minecraft client environment with runShowcaseE2ETest`
    - `Each version has isolated dependencies: Fabric API, Yarn mappings, Loader versions`
  - `docs/CLAUDE.md:7`
    - `This is a Next.js-based documentation site using Fumadocs...`
  - `docs/package.json:5-13`
    - `"build": "next build"`
    - `"dev": "next dev --turbo"`
    - `"lint": "eslint"`
    - `"type-check": "tsc --noEmit"`
  - `build.gradle:1-25`
    - plugin `net.fabricmc.fabric-loom`
    - version gating comment `supported mainline (26.x+)`
    - no `mappings` dependency declaration exists in the build setup
  - `.gitignore:1-33`
    - contains ignores for `.gradle/`, `build/`, `run/`, local crash logs, `/CLAUDE.md`, and `.claude`
    - does not currently ignore `plans/`

- Repo conventions to follow:
  - Commit messages follow short conventional prefixes. Examples from `git log --oneline -10`:
    - `docs: optimize v2.4.1 changelog`
    - `build: release v2.4.1+1.21.11`
    - `fix: resolve memory leaks and thread management issues`
  - Existing maintainer docs are concise, sectioned Markdown files with flat bullet lists. Match the style already used in `AGENTS.md` and `docs/CLAUDE.md`.

## Commands you will need

| Purpose | Command | Expected on success |
|---------|---------|---------------------|
| Root build sanity | `./gradlew.bat build` | exit 0 |
| Docs lint | `cmd /c "cd docs && pnpm lint"` | exit 0 |
| Docs typecheck | `cmd /c "cd docs && pnpm type-check"` | exit 0 |
| Stale-text scan | `rg -n "Nuxt.js|Yarn mappings|runShowcaseE2ETest" AGENTS.md docs/CLAUDE.md` | no matches in `AGENTS.md`; matches in `docs/CLAUDE.md` only if they are intentionally factual |
| Ignore rule check | `rg -n "^plans/?$|^/plans/?$" .gitignore` | at least one match |

Notes:
- The advisor verified `./gradlew.bat build` exits 0 on this commit.
- The docs commands are defined in `docs/package.json`. In the advisor environment, both docs commands timed out after ~124s rather than returning a definitive pass/fail, so rerun them in execution and treat repeated timeout as a STOP condition instead of guessing.

## Scope

**In scope** (the only files you should modify):
- `AGENTS.md`
- `.gitignore`
- `plans/README.md`

**Out of scope** (do NOT touch, even though they look related):
- `docs/CLAUDE.md` — this file is already aligned with the actual docs stack and should be treated as a source of truth for this plan.
- `README.md` — not part of the selected finding.
- Any workflow, Gradle, or Java source file.
- Any historical changelog content under `docs/content/docs/changelog.mdx`.

## Git workflow

- Branch: `advisor/001-align-maintainer-docs`
- Commit as one logical unit
- Message style: conventional prefix, e.g. `docs: align maintainer guidance with 26.x mainline`
- Do NOT push or open a PR unless the operator instructed it.

## Steps

### Step 1: Align the docs-stack description in `AGENTS.md`

Update the `Documentation` section so it matches the live docs application:

- Replace the incorrect Nuxt-based wording with Next.js/Fumadocs wording that is consistent with `docs/CLAUDE.md:7` and `docs/package.json:5-13`.
- Keep the command list concise; do not add speculative commands that are not defined in `docs/package.json`.
- Preserve the current section structure and bullet style.

**Verify**: `rg -n "Nuxt.js" AGENTS.md` → no output

### Step 2: Remove stale validation and dependency-model claims from `AGENTS.md`

Update the build/testing/configuration guidance so it no longer implies:

- `runShowcaseE2ETest` is the canonical verification path for the current branch
- Yarn mappings are still part of the active version-targeting model

Target the specific stale statements at:

- `AGENTS.md:11-12`
- `AGENTS.md:128-139`

Replace them with statements that are true for the current branch:

- 26.x+ mainline targeting
- official naming / no active Yarn mappings entry in the main build configuration
- current validation commands that actually exist in the repo

Do not invent stronger guarantees than the repo can currently prove. If a test command is optional, say so plainly instead of implying it is always available.

**Verify**: `rg -n "Yarn mappings|runShowcaseE2ETest" AGENTS.md` → no output

### Step 3: Re-read the file for internal consistency and update the plan index

Read the final `AGENTS.md` top to bottom and check that:

- the docs stack, verification guidance, and version-targeting language all point at the same current reality
- no remaining bullets contradict `docs/CLAUDE.md`, `docs/package.json`, or `build.gradle`

Then update the `plans/README.md` row for plan `001` from `TODO` to `DONE`.

**Verify**: `git diff -- AGENTS.md plans/README.md` → diff only shows the intended doc/index edits

### Step 4: Ignore the local `plans/` directory

Add an ignore rule for `plans/` in the root `.gitignore`.

- Match the existing root `.gitignore` style: one path per line, grouped by purpose, no extra commentary unless the surrounding section already uses it.
- Do not broaden the ignore to unrelated files such as `advisor-plans/` unless that directory also exists and the operator explicitly asks for it.
- Preserve existing ignore entries such as `/CLAUDE.md` and `.claude`.

**Verify**: `rg -n "^plans/?$|^/plans/?$" .gitignore` → one match for the new ignore rule

## Test plan

- No new code tests are required; this is a maintainer-doc accuracy plan.
- Validation is by command and text checks:
  - Run `./gradlew.bat build`
  - Run `cmd /c "cd docs && pnpm lint"`
  - Run `cmd /c "cd docs && pnpm type-check"`
  - Run `rg -n "Nuxt.js|Yarn mappings|runShowcaseE2ETest" AGENTS.md docs/CLAUDE.md`
  - Run `rg -n "^plans/?$|^/plans/?$" .gitignore`
- Use `docs/CLAUDE.md` and `docs/package.json` as the structure/content pattern for describing the docs app.

## Done criteria

Machine-checkable. ALL must hold:

- [ ] `./gradlew.bat build` exits 0
- [ ] `cmd /c "cd docs && pnpm lint"` exits 0
- [ ] `cmd /c "cd docs && pnpm type-check"` exits 0
- [ ] `rg -n "Nuxt.js|Yarn mappings|runShowcaseE2ETest" AGENTS.md` returns no matches
- [ ] `rg -n "^plans/?$|^/plans/?$" .gitignore` returns a match
- [ ] No files outside `AGENTS.md`, `.gitignore`, and `plans/README.md` are modified
- [ ] `plans/README.md` status row for `001` is updated

## STOP conditions

Stop and report back (do not improvise) if:

- The code at the locations in "Current state" no longer matches the excerpts above.
- `docs/CLAUDE.md` or `docs/package.json` have changed such that they no longer clearly establish the docs stack or commands.
- Either docs validation command times out twice or fails for reasons unrelated to the `AGENTS.md` edits.
- Fixing the drift appears to require editing workflow, Gradle, Java, or docs content files outside the in-scope list.
- `AGENTS.md` or the selected plan files are still uncommitted and the executor is running in an isolated worktree that cannot see them. In that case, stop and ask the operator to commit or otherwise make those files available before execution.

## Maintenance notes

- Reviewers should check that `AGENTS.md` stops making promises the repo cannot prove, especially around verification commands and versioning architecture.
- Reviewers should also check that `.gitignore` now protects the local `plans/` directory without masking unrelated tracked files.
- If the branch later adds `26.3+` support, revisit only the version-targeting language that names the baseline; keep the stack description grounded in executable files (`docs/package.json`, `build.gradle`) rather than assumptions.
- Follow-up findings about CI behavior, `fabric.mod.json` compatibility bounds, and `build.properties` churn were intentionally left out of this plan because the user selected only the maintainer-doc drift item.
