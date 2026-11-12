<!-- Replace ISSUE_NUMBER after publishing assignment/issue.md in this fork.
     PR title: Triage slice 3: teams and activities
     Base repository: phuthanh03012003/triage-the-analyzers; base branch: main.
     Head branch: triage-slice-3. Leave the PR open; do not merge. -->

Closes #ISSUE_NUMBER
Slice: 3 — Teams and activities
Traces to: none: config

## Project decisions

| | Rule | Choice | Reason | Where recorded |
|---|---|---|---|---|
| D1 | EI_EXPOSE_REP, EI_EXPOSE_REP2 | Turn off in application packages | `TeamService.transferTeam` and inverse-association helpers mutate collections returned by entity getters; Spring collaborators must retain their injected identity. Transport/template objects make no defensive-isolation promise. Accept aliasing risk, including ratings/total-score consistency; blanket copies are not an aggregate design. | `backend/config/spotbugs-exclude.xml`; rationale in `assignment/project-decisions.md`; 8eec00e |
| D2 | UncommentedEmptyConstructor, UnnecessaryConstructor | Enforce | Document 20 necessary constructors; remove 10 redundant sole public constructors. Java still supplies their public no-argument constructor, including for JPA. | Constructors in `backend/src/main/java`; both rules remain enabled; 8eec00e |
| D3 | ControlStatementBraces | Enforce | Add braces at all 30 findings: null-denial checks, association updates, and reminder early exits should remain correct when another statement is added. | Reported control statements in `backend/src/main/java`; rule remains enabled; 8eec00e |
| D4 | GuardLogStatement | Enforce | Guard all four reported calls so their arguments are not evaluated when their log level is disabled. `{}` placeholders alone only defer formatting. | `AuthController`, `ReminderService`, `WeeklyReminderScheduler`, `DocumentTemplateRegistry`; rule remains enabled; 8eec00e |

## My slice

Line numbers below refer to the original code on `main`, matching the unfiltered PMD report.

| File:line | Rule | Verdict | Recorded as | Reason |
|---|---|---|---|---|
| `team/TeamSpecs.java:5` | InstantiableUtilityClass | Real — API design | Private constructor in 04824f8; `final` in 8eec00e | This class exposes static specification factories only; it is neither a JPA entity nor a Spring bean requiring construction. |
| `team/TeamSpecs.java:9` | UseLocaleWithCaseConversions | Real — search bug | `Locale.ROOT` in 04824f8 | The search pattern must not change from `%ivan%` to `%ıvan%` when the server uses Turkish. |
| `team/TeamSpecs.java:14` | UseLocaleWithCaseConversions | Real — search bug | `Locale.ROOT` in 04824f8 | Exact section-name search must not turn `SENIOR I` into `senıor ı` under a Turkish default locale. |
| `activity/ActivitySpecs.java:5` | InstantiableUtilityClass | Real — API design | Private constructor in 04824f8; `final` in 8eec00e | Static query factories need no instances; construction is not part of a framework contract here. |
| `activity/ActivitySecurityService.java:80` | LambdaCanBeMethodReference | Not worth it | Reasoned `NOPMD` on the projection in 04824f8 | The existing section-ID projection is clear and correct. Method-reference syntax adds no protection to the scoped authorization check. |

All paths above are relative to `backend/src/main/java/team/projectpulse/`.

## The bug I showed

`backend/src/test/java/team/projectpulse/team/TeamSpecsTest.java` executes each actual JPA specification under English and Turkish defaults and checks the string supplied to the Criteria API. The tests restore the default locale in `finally` and share a resource lock. This checks predicate construction; database collation behavior is not claimed.

- **Red: ac5e979**, committed before the fix. `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home ./mvnw --batch-mode -ntp -Dtest=TeamSpecsTest test` (from `backend/`) reported **2 tests, 2 failures, 0 errors**. Each expected argument appeared only once, in English; Turkish supplied a dotless-i argument.
- **Green: 04824f8**, using `Locale.ROOT`. The two regression tests passed. A run including `ActivitySecurityServiceTest` and `TeamSecurityServiceTest` reported **12 tests, 0 failures, 0 errors**.
- The wrapper was made executable in 8eec00e; when checking out the earlier commits, use `sh ./mvnw` or set its executable bit.

## The baselines

| | Before | After |
|---|---|---|
| `spotbugs-baseline.xml` — Match entries / findings | 237 | 18 |
| `pmd-baseline.properties` — represented findings | 130 | 51 |
| PMD class/rule pairs physically stored | 101 | 40 |
| PMD class rows physically stored | 68 | 32 |

PMD stores one token per class/rule pair, so several findings can share a token. Counts of actual findings come from unfiltered reports generated in a temporary worktree with empty baselines. SpotBugs lost exactly D1's 219 entries. PMD lost D2's 40, D3's 30, D4's four, and the slice's five. Both sets of remaining baseline entries are subsets of their originals; neither baseline gained an entry.

## What the agent did

The agent inspected source and unfiltered reports, proposed the D1 tradeoff, and applied it after the student delegated the choice. It wrote the regression tests before the fix, implemented the slice and D2–D4 changes, shrank the baselines, and drafted the issue/PR text.

The agent avoided blanket defensive copies, which would discard intentional association updates, and retained necessary overloaded-class no-argument constructors rather than deleting them all. It kept the correct authorization lambda with a reasoned suppression. The analyzer found that the new private-constructor utility classes should also be final; the agent corrected them. No student rejection or manual rewrite of generated code is claimed; the student still needs to review and understand this work.

## How it was verified

- **Verified:** initial analyzer gate passed; both locale regressions failed before the fix and passed afterward; 12 focused search/authorization tests passed; unfiltered reports confirmed 18 remaining SpotBugs and 51 remaining PMD findings; baseline-subset checks and `git diff --check` passed.
- **Full backend gate:** Java 21 `./mvnw --batch-mode -ntp verify` passed with **441 tests, zero failures, zero errors, zero skipped**, followed by successful SpotBugs and PMD checks. Docker Desktop supplied the MySQL integration-test container. The first attempt was blocked by the stopped Docker daemon; it passed after Docker started.
- **Frontend report-only checks (local Node 25.6.1):** `npm ci` passed; ESLint reported 130 problems (128 errors, two warnings); `vue-tsc` reported 18 errors; Prettier reported six files. No frontend source was changed.
- **Not verified:** GitHub PR checks and repository Issues/Actions settings until the issue/PR are published; interactive browser behavior; database-collation equivalence for every Unicode character.
