# Learnings

This folder is the **single source of truth** for meaningful changes in this repository. It is an append-only, immutable log written for future engineers (human and AI). Read it before planning non-trivial work; write to it after non-trivial work.

## When to add an entry

Add an entry for any change with lasting consequences: new features, refactors, bug fixes with non-obvious root causes, dependency or infra changes, spec-kit implementations, or anything where the "why" would be hard to reconstruct from the diff alone.

Skip purely cosmetic edits (typos, formatting, comment-only tweaks).

## File naming

```
learnings/YYYY-MM-DD-short-kebab-slug.md
```

Append `-HHMM` only when multiple entries land on the same date:

```
learnings/2026-05-18-1430-auth-refactor.md
learnings/2026-05-18-1715-rate-limit-fix.md
```

## Required sections

Every entry must contain these four sections, in this order:

```markdown
# <Title>

## What changed
- Files touched and a short summary of the edits.

## Why it mattered
- Decision rationale, constraints, alternatives considered.

## Gotchas / pitfalls
- Non-obvious things discovered during the work that would help the next engineer.

## Verification
- How the change was validated: tests run, manual checks, exact commands used.
```

## Rules

- **Immutable history.** Never rewrite past entries to reflect later changes. If a prior decision is reversed, add a new dated entry that references the old one (e.g., "Supersedes `2026-05-18-auth-refactor.md`").
- **One entry per logical change**, not one per commit. A multi-commit refactor gets a single entry.
- **Link to spec-kit artifacts.** If the change came from a spec-kit flow (`/specify` → `/plan` → `/tasks` → `/implement`), link back to the relevant files under `specs/` and note any deviations from the plan plus the reason.
- **Concrete over abstract.** Cite file paths, function names, commit SHAs, and command output. Avoid vague summaries.

## Template

Copy this skeleton to start a new entry:

```markdown
# <one-line title>

## What changed
-

## Why it mattered
-

## Gotchas / pitfalls
-

## Verification
-
```

## Entries

- `2026-11-19-home-cover.md` — sticky accessibility cover across Home, version 1.1.5.
- `2026-11-21-voice-results.md` — leave Alexa results uncovered, version 1.1.6.
- `2026-11-22-hubble-nasa-wallpaper.md` — Hubble and NASA home backdrop, version 1.1.7.
- `2026-11-21-scenic-dream.md` — bundled scenic daydream, version 1.1.8.
- `2026-11-21-scenic-kenburns.md` — Ken Burns matrix drift on the scenic dream, version 1.1.9.
- `2026-11-21-scenic-fifty.md` — scenic slide length 60 seconds, version 1.1.10.
- `2026-11-22-ubuntu-font.md` — Ubuntu on photo captions and the rest of the UI, version 1.1.11.
