# Triage slice 3

Slice 3 — Teams and activities: five PMD findings in `TeamSpecs`, `ActivitySpecs`, and `ActivitySecurityService`.

| Decision | Choice | Project-specific reason |
|---|---|---|
| D1 — EI_EXPOSE_REP / EI_EXPOSE_REP2 | Turn off for Project Pulse application packages | JPA association helpers mutate shared collections, Spring injects shared collaborators, and transport/template objects have no defensive-isolation contract. Accept the documented aliasing risk instead of introducing copies throughout the model. |
| D2 — UncommentedEmptyConstructor / UnnecessaryConstructor | Enforce | Document necessary JPA/Jackson/report constructors; remove only redundant sole public constructors, preserving Java's implicit public no-argument constructor. |
| D3 — ControlStatementBraces | Enforce | Protect null-denial checks, association helpers, and reminder-loop early exits against accidental expansion of unbraced bodies. |
| D4 — GuardLogStatement | Enforce | SLF4J placeholders defer formatting, but entity getters, principal-name lookup, and collection-size arguments still execute before the logger checks its level. |

Scope: fix two locale-dependent searches and two instantiable utility classes; suppress the single method-reference style suggestion with its reason. Show the Turkish-locale search bug with tests committed before the fix. Remove every resolved baseline entry, and leave unrelated findings for their assigned slices.

Detailed rationale: `assignment/project-decisions.md`. Evidence and the completed PR description: `assignment/pull-request.md`.
