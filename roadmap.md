# Roadmap

Where the project stands and what is left, in order: first what a full, playable online game with the Kobol objective
still needs (milestones A to F), then the long term (G onwards). Written 2026-11-21 against `master` (`4606f76`); the
open questions were answered on 2026-11-22. Updated 2026-11-23 against `88e3d61`: milestones A to C are done.
Updated 2026-11-25 against `cba4390`: D is done, E and F are next.

This is a roadmap, not a work order: each milestone gets its own `<topic>-plan.md` (context, decisions, phases that
end green) before it is built, and the file is deleted once the plan is executed: what it decided lives on in
`CLAUDE.md` and its `agents/` topic files, and in its milestone's "Where it differs from the plan" here.
`CLAUDE.md`'s "Status" section says which milestones are done; fix it per milestone, not at the end.

## Where we are

Done, and covered by tests:

- **Rules engine** (`core`): every component of the Kobol game (all Crisis, Super Crisis, Destination, Skill, Quorum,
  Mutiny, Loyalty and Motive Cards, every location and Title Card action, Pegasus, 99 character abilities), the turn
  loop, the jump and the end game. Fact-sourced journal, restore, rollback, undo, seeded randomness, per-viewer
  projections. The engine takes an input's only answer itself (`takeOnlyAnswers`).
- **Completable** (A): `./gradlew :core:fuzz` plays seeded random games to an ending; 10,000 seeds per player count
  leave no finding but a valid stall.
- **Legal answers** (B): `LegalAnswers` lists the answers of every decision, the asked viewer's `input` carries them,
  and the fuzzer answers from them.
- **Backend**: Google and Discord logins, lobby (host, join, observe, leave, start with 3 to 7), a live web socket per
  game with the state token, Postgres journal, sandbox with undo, export and import, regression journals (the fuzzer's
  findings among them).
- **Frontend** (C): the table (board image with the components on it, turn panel, log, players' mats, hover reveals),
  played through the engine's answers in the answers panel, on the board and on the mats, with every pause said in
  words, a log by step, deltas and animations, and a reference drawer; the lobby and room pages, the sandbox tools,
  e2e specs for a sandbox turn, a five-user lobby and a whole game played through the answers.

- **Sessions** (D): presence, seats that change hands by a vote or the host, host tools, chat, notifications,
  last-seen lines, results with a report, a read-only link and a rematch, problem reports, the dev login per tab.

What stands between this and "a group of friends can play a whole game online" is no longer the engine, the inputs or
the session, but the replays and a place to run it:

1. **A finished game can't be stepped through** (E): the read-only link shows its end and its log, no slider yet.
2. **Nothing runs it for real players** (F). The CI deploys master to the VPS, but there is no staging, no HTTPS, no
   backups, no frozen journal format, no privacy notice.

## Principles for the milestones

- **Legal moves come from the engine.** The browser holds no rules (`CLAUDE.md`, "Architecture"). Every UX improvement
  that needs "what
  can this player do now" asks `core` (`LegalAnswers`), and the same list drives the random-playout fuzzer.
- **Every input sends one of the engine's answers** as it was listed (`input/answers.ts`), and the engine judges it
  again. The generic `EventBuilder` under "Advanced" was removed on 2026-11-24; only the sandbox's setup still builds
  an event from the schema (`sandbox/SetupForm.tsx`).
- **Each milestone ends green** (`./gradlew build`, e2e where it touches the table) and updates `CLAUDE.md` and the
  `agents/` topic files.

---

## Part 1: to a full playable online game

### A. Prove the game is completable: done 2026-11-22

`./gradlew :core:fuzz` (the `fuzz/` test fixtures, "Random playouts" in `agents/testing.md`) plays seeded games of 3 to
7
players with random legal answers until they end. It restores them on the way and compares the twins, probes the checks
with hostile inputs, and rewrites `fuzzer-report.md`: the findings, the endings, the rounds to finish and the wins by
team. A finding exports as a regression journal with the input that failed (`FuzzJournalExportTest`), beside a core
test of its fix. The fix loop took four batches.

*Exit met:* 10,000 seeds per player count (last run 2026-11-22, `78a0421`) leave one finding, `NO_END`: 332 of 50,000
games, nearly all with 3 players, stall with every human in the "Brig", where nobody draws a Crisis Card. That is a
valid game, not an engine error.

Where it differs from the plan:

- The fuzzer runs by hand, not nightly: the nightly run comes with F's CI.
- Random play says nothing about balance: the Cylons win 49,654 of 50,000 games, in 5 rounds (median). The readout
  only shows that the endings are reachable: every one but "no character left to choose" happened, the humans' win at
  Kobol in 14 games.
- Decided on the way: "Political Prowess", "Change of Plans" and "Declare Emergency" are still asked of every player,
  so nobody learns who holds one, and a player without the card is offered only the pass.

### B. Legal-move hints: done 2026-11-22

`LegalAnswers` (`engine/answer/`, "Legal answers" in `agents/engine.md`) lists the answers of every decision for the
player it
asks: concrete events, and where the set is too large, shaped answers (an example and a `Pick` of cards or ships, with
`min`, `max` and whether the order counts). The asked viewer's `input` carries them (`Decide.answers`, and
`Choose.answers` for each option that asks a decision at once), and `anytime` the `AnytimeEvent`s a player may submit
at any pause, e.g. a Motive Card. The fuzzer answers from them and checks them: over 10,000 seeds per player count no
decision is without an answer, no option leads to a dead end, and the game takes no input they don't cover.

Where it differs from the plan: there is no enumeration per kind of decision. One search tries the values of each
answer record's components (`AnswerClasses`, `Domains`, `Shapes`, `AnswerSearch`), skips a record when the check the
game asks before it offers one says no (`canPlay`, `canAct`, ...), and lets the processor's own `prepare` judge every
candidate, so no rule is written twice.

### C. A table that is pleasant to play: done 2026-11-22

What the table has now (`agents/frontend.md` and `agents/frontend-table.md`):

- **C1, inputs**: the answers panel (a choice's options in order, the answers grouped by kind, a draw as a card per
  color, a picker for a shape, an answer made of several parts picked part by part, "Back" and "Clear", the builder
  under "Advanced"), the board and the mats answering (targets that light up, "Act" chips, a viper's activations, card
  plays from the mat, a popover next to the click), sorted hands, and the characters to choose grouped by pool.
- **C2, turn flow**: a red edge on the asked viewer's board, a sentence for every pause from the input's `topic`, led
  by the card or ability that asks (`@Source`), the log by step with a flash on the board, deltas at the resources,
  the tracks and the nukes, and small animations (ships, the die, cards, damage).
- **C3, reference**: a drawer (the R key) with every card by deck and a search, the character sheets, the rulebook's
  sections and cheat sheets (the turn, a skill check, the Attack Table from the engine's thresholds, the Crisis Card
  icons, loyalty), and a link to the article to read beside each pause. The rulebook PDFs ship with the app.

*Exit met:* `playthrough.spec.ts` plays a seeded four-player sandbox from setup to an ending through the answers
alone, and "Advanced" never opens (seed 7: a Cylon win in 254 pauses).

Where it differs from the plan:

- The log's filter to what changed for the viewer ("Mine") was built and then removed.
- During an interrupt window the pause says only that someone may play a card or use an ability: naming the window
  or who has answered would tell who holds a card.
- Ordering is by numbered clicks, not drag and drop. The animations always play (no setting yet).

### D. Sessions that survive real people: done 2026-11-25

What a session has now (`agents/backend.md` "lobby/", "chat/", "problem/"; `agents/frontend-table.md`):

- **Presence**: in a lobby whoever's room page polls, in a running game whoever has a socket; the client pings every 20
  s and the server closes a connection silent for a minute. A dot per user in the room, on the mats and in the seats
  panel, "away" and "vacant seat" chips, and a line under the pause when the game waits for such a seat.
- **Seats**: a seat belongs to the game and a user holds it (`seat_holder` keeps who and why he left). A player leaves
  his seat and watches on; anyone not banned takes a vacant one, a former holder only his own, and sees what the seat
  sees. A vote of more than half of the other seated players hands a seat over, the host's passes at once. The users
  whose seat changed reconnect at once (`4001`) with their new viewer. No clock: the engine never passes for anyone.
- **Host tools**: remove or ban a user (before or after the start), cancel a lobby (`end_reason = 'CANCELLED'`),
  abandon a game (also by a vote of more than half of the players: `FINISHED`, `ABANDONED`, no input, nothing revealed).
- **Notifications**: "your move" as a browser notification while the tab is hidden (asked for by "Notify me", never on
  load), the tab's title flashing until it shows, and the favicon.
- **Chat**: one public chat for everyone at the table, players and observers, over the game socket, stored in
  `chat_message`, 500 characters, 5 messages per 10 s; the log panel switches to it and back, with a count of what came
  while it was closed.
- **Last seen**: a red line in the log above what came after the viewer's last decision, and in the chat under what he
  had seen when he opened it.
- **Game end**: `GameReport` in `core` (winners, every player's cards, statistics per player and per game, the
  resources per turn, the story), sent as `report`; a results dialog over the table with the winners by every user who
  held their seat and a resources chart; the table's URL as a read-only link for anyone logged in; the host's rematch
  with the same seats.
- **Report a problem**: under "Reference", a drawer with 1000 characters, stored with the game and its journal sequence.
- **Dev simulation**: the dev login per tab (`X-Dev-User`, `?as=<name>`, "Another user in this tab"), beside incognito
  windows and other browsers.

*Exit met:* `SessionExitTest` plays a seeded four-player game through `GameSessions`, restored from the rows every 40
inputs, hands the second seat to an observer by a vote mid-game and finishes with the engine's winner, the report and
the seat's history; `sessions.spec.ts` plays presence, chat, a hand-over and an abandon in four browsers.

Where it differs from the plan:

- No clock: the exit line once said "after the clock ran out"; a vote hands the seat over instead.
- The room is polled (every 3 s) for presence, seats and votes; the socket carries the game, the chat, the report and
  the pings. The client's heartbeat came forward from F.
- Standard games stay unseeded: the room shows a game's seed, which would tell the decks.
- An abandoned game reveals nothing, and its results name only who held each seat.
- One chat, not a players' and an observers' one (decided on 2026-11-25): observers write at the table too. A sandbox
  has no chat and no last-seen lines.

### E. Replays, review and "learn from the game"

- **Replay viewer**: any finished game, from the journal, for any viewer's projection, step through the scopes with a
  slider (the sandbox and `LiveUpdateProjection` already do the hard part). All-reveal mode once the game has ended.
- **Export** a game's journal for a bug report, with the format version, from the table (exists for sandboxes only).
- **"Report a bug at this point"**: a button that attaches the journal position and the viewer's last rejection.

*Exit:* a bug report from a player can be turned into a `RegressionJournals` test by pasting it.

### F. Ship it (ops and compliance)

- **CI**: GitHub Actions (`.github/workflows/ci.yml`): `./gradlew build` (Testcontainers on the hosted runner's
  Docker, the `FormatLockTest` with it), the e2e job against the built jar with the `dev` profile, and on master the
  deploy of that jar to the VPS (`deploy/deploy.sh`). Still missing: the nightly fuzzer from A.
- **Environments**: a staging and a production, self-hosted on a single small VPS (open question 6), `TOKEN_SECRET`
  set, Google and Discord client ids, HTTPS and the allowed origins, a reverse proxy that supports web sockets with
  idle timeouts above the heartbeat. A single node is
  enough (the `GameSessions` lock is per node); document that scaling out needs sticky routing by game id or moving
  the lock into Postgres (advisory locks).
- **Heartbeats and limits**: a web socket ping so proxies don't close idle tables; per-user rate limits on the lobby and
  the sandbox endpoints; a maximum of open lobbies, sandboxes and games per user; sandbox cleanup (nothing in the code
  expires a sandbox).
- **Data and backups**: Postgres backups with a restore test; a **journal-format migration story**: `FORMAT_VERSION`
  refuses to open old games today (`UNAVAILABLE`), which is acceptable in development and not for real games. The
  format is frozen at launch, as late as possible: after that only additions (new facts and events), and no game is
  ever refused (open question 3).
- **Observability**: structured logs with the game id, a metric for rejected inputs and rollbacks (a rollback is a bug:
  alert on it), the `FrakCallTheAdmiralException` stack with the journal position kept.
- **Privacy and legal**: an app that stores a Google or Discord identity (name, a provider subject id) and the
  game history is processing personal data. It needs a privacy notice, an account deletion that keeps other players'
  finished games intact (replace the name by "deleted player" in projections), and a retention rule for sandboxes and
  journals. If this is hosted for people in the EU, this is GDPR territory: the final wording and the lawful basis
  need review by whoever acts as the DPO or legal counsel, not by this document. The game, its artwork and text belong
  to their rights holders; the card and board images and the rulebook in `materials-sorted/` are used as they are,
  because the project is for private play (open question 2).
- **Moderation**: anyone can sign up for free (open question 6), so at minimum: report a user, and ban from a lobby (D's
  host tools).

*Exit:* a public staging environment where an invited group plays a full game and a rollback or error pages the
author.

### Milestone order and dependencies

```text
A fuzzer ──► B legal hints ──► C table UX ──► D sessions ──► E replays
  done          done             done           done
                                                 └── E builds on D's results screen and replay link
F (CI, ops, compliance) runs alongside D; its staging is the gate for inviting players.
```

A to D are done, in that order: A made the engine sound, B gave A its answers, C its inputs and D its sessions. F was
to start alongside C and started after D (the heartbeat came with D), with the CI and the deploy to the VPS. The rule for
"playable" is the exit line of D plus F's staging.

---

## Part 2: long term

High level only. Each is a separate project; order is by how much of the rest it needs.

### G. More of the base game's surroundings

- **Game options at the lobby**: choose which expansions' components are in the decks (base only, Pegasus, Exodus
  and Daybreak added one by one; today every component of the in-scope set is in), with the rules consequences (the
  Crisis deck's 141 composition, which characters are picked from, Pegasus in play or not). A variant list: the
  optional rules the rulebook gives (e.g. no Cylon Leaders, the 3-player handling).
- **Teams, ratings and history**: a player's finished games, a character and role statistics page, optional ratings.
  Only with E's results and replays.
- **Spectators with delay**: observers see a delayed stream so they can't leak to players; today an observer is sent
  updates at once (check, then add the delay in `ConnectionPool`).
- **Tournaments and private groups**: invites, groups, scheduled games.
- **Sound and music**: an optional soundscape (the table is fully silent now), per-event sounds, a muted default.
- **Localization**: the labels are already one place (`src/labels/`) and never carry a card's printed text; that makes
  other languages cheap for the UI and hard for the cards (needs the printed text first).

### H. The other objectives

All of this is out of scope today (`CLAUDE.md` "Scope"). Each objective is a bundle of rules, components and
setup; the engine has the seams (`ObjectiveCard`, setup by objective, the `Location` enum, boards by `BoardsHolder`).
Start by listing what each one touches, as the Kobol work did.

1. **New Caprica**: the New Caprica board and its locations (Detention, Resistance HQ, ...), its crisis cards,
   occupation forces. New board, new locations, new damage and loss rules; the largest change to `Location` and the
   board layout.
2. **Ionian Nebula**: trauma tokens, allies, crossroads cards, the alternate basestar damage tokens. New per-player
   state (trauma), new decks, an "ally" component, new decision types; touches the projections.
3. **Earth**: the Demetrius and Rebel Basestar boards, mission cards, the basestar allegiance marker, the Hub Destroyed
   overlay. Two more boards, a new win path, new boards in `BoardSvg`.

Before any of these: make the objective a first-class setup parameter (`StartGameEvent`, the Crisis/Destination deck
builders and the winner check read it), and move the Kobol-specific rules behind it, with a regression pass that the
Kobol game is byte-for-byte unchanged (the journals are the check). The deleted rules the code notes as "out of
scope" would come back with their rulebook sections (the blue, green and orange backgrounds).

### I. Content and rules beyond the combined rulebook

- **Daybreak, Exodus and Pegasus variants and optional rules** the combined rulebook marks, if any are not in.
- **Custom content**: a scenario/variant file format (a Crisis deck, a character set, a house rule) so a group can
  play variants without code. This forces the cards out of enums and into data; a big refactor that only pays off if
  it is wanted.
- **Challenge scenarios and a tutorial**: a guided first game (a fixed seed, a scripted setup, tips at each step),
  the single biggest lever for new players, built on the sandbox's setup events and the hints of B.

### J. Platform

- **Native/PWA wrapper** with push notifications, installable, offline reading of finished replays.
- **Public API for other clients** (the wire schema is already published at `GET /api/schema`).
- **Horizontal scale**: Postgres advisory locks per game instead of the in-process lock, a socket fan-out through
  Postgres `LISTEN/NOTIFY` or Redis, so more than one node serves a game.
- **Analytics and balance**: from E's data, which characters, cards and objectives decide games.

---

## Open questions (answered 2026-11-22)

1. **Rules reminders on the table** (C1/C3). A link to the rulebook page with quoted article to read there. Built in C
   (`labels/rules.ts`, the reference drawer).
2. **Redistributing the artwork and the rulebook** (C3, F). `materials-sorted/` holds card images and the rules PDF
   that are not ours. Fine for private play as is the intent of this entire project. The PDFs ship with the app since C.
3. **Format migration** (F). Freeze `FORMAT_VERSION` at launch (add-only, new facts only). Freeze the format as late as
   possible and never refuse a game after that.
4. **Timeouts and defaults** (D). Default only when it is the only option that is visible to all players. For example
   auto-drawing skill cards when the skill set of the character is fixed. Built ahead of D as the engine's only
   answers (`takeOnlyAnswers`); D has no clock.
5. **Chat and hidden information** (D). Only public chat will be available.
6. **Hosting and who pays** (F). Self-hosted, a single small VPS, free to sign up.
7. **Bots in a ranked/open game** (G). Bots are out of the scope for this project.

## Not in this roadmap

- Any rule change to Kobol play beyond what the fuzzer finds; the choices in `choices.md` stand until a playtest
  contradicts one.
- Private channels between hidden Cylons (the rules ban out-of-table communication; see open question 5).
- Bots and AI seats (open question 7). The fuzzer's random player only tests the engine.
- Real-money features, ads, or a marketplace.
- Rewriting the engine's architecture (events, processors, ops, journal): it is the reason the rest is cheap.
