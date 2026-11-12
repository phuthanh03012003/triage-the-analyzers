# Project decisions — slice 3

## D1 — turn off EI_EXPOSE_REP and EI_EXPOSE_REP2 within this application

Selected after the student delegated the choice in response to the proposal. Applied in the analyzer configuration.

Project Pulse uses mutable objects as its application model rather than exposing an immutable Java library API:

- JPA entities share associations and managed collections. `TeamService.transferTeam` removes/adds a team through `Section.getTeams()`, while `Section.addInstructor` updates the inverse association through `Instructor.getSections()`. Returning copies there would silently break those updates.
- Controllers/services retain the actual Spring-injected collaborators; copying a collaborator would defeat dependency injection and transaction proxies.
- The remaining findings concern mutable response/report collections, YAML template data, and `MyUserPrincipal` wrapping the application's user entity. These types carry the application model; none promises defensive isolation. The response converters construct transport data and Jackson serializes it to JSON, so a browser never receives the server's Java object reference.

This is an accepted mutability tradeoff, not a claim that every warning is false. For example, `PeerEvaluation.setRatings` calculates a total once, so later mutation through an alias can make that total stale. A future encapsulation change should design aggregate mutation methods and test the callers together; copying every getter/setter to silence this rule is not that design.

`backend/config/spotbugs-exclude.xml` entry (scoped to application classes, leaving every other SpotBugs rule enabled):

```xml
<!-- Project Pulse deliberately shares mutable JPA associations, Spring collaborators,
     and application transport/template objects. TeamService.transferTeam updates
     associations through getters; defensive copies there would discard updates.
     Transport objects have no immutable-Java-API contract. Aliasing remains an
     accepted design risk (e.g. PeerEvaluation ratings/totalScore consistency),
     to address through aggregate APIs rather than blanket defensive copies. -->
<Match>
    <Package name="~team\.projectpulse\..*"/>
    <Bug pattern="EI_EXPOSE_REP,EI_EXPOSE_REP2"/>
</Match>
```

Remove exactly the 219 matching SpotBugs baseline entries. Keep the other 18 entries.

## D2 — enforce both constructor rules

Remove the ten redundant public no-argument constructors that are the class's only constructor. Java supplies the same public no-argument constructor; JPA's requirement remains satisfied. Keep necessary no-argument constructors beside other overloads, with an internal comment explaining JPA, Jackson, or report-building use. Keep both rules enabled and remove their baseline rule tokens.

## D3 — enforce ControlStatementBraces

Add braces around all thirty reported bodies without changing conditions or statements. This makes the null-denial checks, association updates, and reminder-loop early exits harder to break when a second statement is added. Keep the rule enabled and remove its baseline tokens.

## D4 — enforce GuardLogStatement

Guard the four reported logging calls at their actual levels. SLF4J placeholders defer formatting but Java still evaluates arguments such as `authentication.getName()` and entity getters first. Guards avoid that evaluation when logging is disabled. Keep the rule enabled and remove its baseline tokens.
