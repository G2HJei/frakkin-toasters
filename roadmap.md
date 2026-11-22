# Roadmap

Where the project stands and what is left, in order: first what a full, playable online game with the Kobol objective
still needs (milestones A to F), then the long term (G onwards). Written 2026-11-21 against `master` (`4606f76`); the
open questions were answered on 2026-11-22.

This is a roadmap, not a work order: each milestone gets its own `<topic>-plan.md` (context, decisions, phases that
end green) before it is built, as the backend, skill card and quorum card plans did. `CLAUDE.md`'s "Roadmap" section
still lists the finished steps; fix it per milestone, not at the end.

## Where we are

Done, and covered by tests:

- **Rules engine** (`core`): every component of the Kobol game (all Crisis, Super Crisis, Destination, Skill, Quorum,
  Mutiny, Loyalty and Motive Cards, every location and Title Card action, Pegasus, 99 character abilities), the turn
  loop, the jump and the end game. Fact-sourced journal, restore, rollback, undo, seeded randomness, per-viewer
  projections.
- **Backend**: Google and Discord logins, lobby (host, join, observe, leave, start with 3 to 7), a live web socket per
  game with the state token, Postgres journal, sandbox with undo, export and import, regression journals.
- **Frontend**: the table (board image with the components on it, turn panel, log, players' mats, hover reveals), the
  lobby and room pages, the sandbox tools, e2e specs for a sandbox turn and a five-user lobby.

What stands between this and "a group of friends can play a whole game online":

1. **Nobody has proved the engine survives a whole game.** Tests cover single rows (one engine test per feature) and a
   few scripted turns. There is no evidence that a random 4-player game reaches an ending without a deadlock ("a
   decision must always have a legal answer"), an exception, or a rollback.
2. **The input side is the clunky part.** A choice is a column of text buttons, and every decision is a form generated
   from the wire schema (`EventBuilder`: a "Which" dropdown of event types, then a field per record component). It is
   correct and complete, and it is what makes playing feel like filling in forms. `boardMoves.ts` covers moving and
   the "Act" button only. The engine publishes no legal answers, so the browser can only narrow by guessing.
3. **A real game is not a real session.** No turn clock or absence handling, no presence, no chat, no end-of-game
   screen beyond one line, no rematch, no deployment pipeline, no CI.

## Principles for the milestones

- **Legal moves come from the engine.** The browser holds no rules (`CLAUDE.md`). Every UX improvement that needs "what
  can this player do now" asks `core`, and the same list drives a random-playout fuzzer. Milestone B is the hinge.
- **The generic `EventBuilder` stays** as "Advanced" and as the sandbox's tool. Dedicated inputs are added per kind of
  decision, each building the same JSON the builder would (the `boardMoves.ts` pattern, with a test that says so).
- **Each milestone ends green** (`./gradlew build`, e2e where it touches the table) and updates `CLAUDE.md`.

---

## Part 1: to a full playable online game

### A. Prove the game is completable (core, small, first)

Everything else is built on the engine being sound, so find out now. Done 2026-11-22: `./gradlew :core:fuzz` played
10,000 seeds per player count with no finding left but a valid stall (every human in the "Brig"); each run writes
`fuzzer-report.md` anew.

- **Random-playout fuzzer** (test fixture, runs in `core`): start a seeded game with 3 to 7 players, answer every
  `Waiting` with a random legal answer, restore at random pauses and compare `GameSnapshot`s, until `Finished`. Needs
  legal answers, so it begins with B1's enumeration for the decisions whose set is small, and falls back to
  constructing candidates from the wire schema for the rest. Asserts: no `FrakCallTheAdmiralException`, no pause with
  no legal answer, the game ends within a turn cap, every ending is one of the rule's.
- Run it over thousands of seeds in a nightly Gradle task (not in `build`), keep failing seeds as regression journals
  in `backend/src/test/resources/journals/`, each with a core test next to its fix. A journal that a `FORMAT_VERSION`
  change makes unloadable is deleted, and its core test stays (decided 2026-11-22).
- Fix what it finds. Expect the findings to be deadlocks at rare card combinations and unreachable-`Finished` loops (a
  game where the resources never hit 0 and the jump track can't advance). Decided 2026-11-22: "Political Prowess",
  "Change of Plans" and "Declare Emergency" are still asked of every player, so nobody learns who holds one, and a
  player without the card is offered only the pass.
- A **game-length and balance readout** from the same runs: turns to finish, win rate by team. Random play is not
  meaningful balance, only a smoke test for broken distributions (e.g. humans never win).

*Exit:* 10,000 seeded random games per player count finish with no engine error.

### B. Legal-move hints (core + wire, the hinge)

The "Still open" item of the frontend plan, and the prerequisite for most of C.

- **B1. `LegalAnswers` in `core`**: for a pending `PlayerDecisionEvent<T>`, enumerate the concrete answers where the set
  is small and finite, and describe the shape of the answer where it is not. Group by kind of decision:
    - *enumerable*: moves (destinations and whether a discard is needed), location actions, viper activations (viper ×
      `ViperActivation`), Skill Card plays (`SkillCardPlays.canPlay`, `QuorumCardPlays.canPlay`, `MutinyCardPlays`,
      `CharacterActions.canUse`), targets of a choice (`ChooseCrisisTargetEventProcessor.legalTargets` is the pattern),
      ships to damage, interrupt-window cards.
    - *shaped*: "discard exactly 3 of these cards", "draw exactly 2 of these colors", "place cards in this order", with
      the candidate cards and the counts, not every combination.

  Processors already own the checks (`canPlay`, `canAct`, `legalTargets`); this collects them behind one entry point
  instead of adding a second rule set. A test per decision type asserts that every enumerated answer passes `isValid()`
  and that a known-illegal one is absent.
- **B2. Wire**: `input` in the projection carries the hints for the asked viewer only (secret inputs stay secret).
  `WireSchema` gains the new records, `npm run gen`, `WireSchemaTest` pins them, `FORMAT_VERSION` untouched (it is a
  projection, not the journal).
- **B3. Fuzzer upgrade**: A's fuzzer answers from B's enumeration, so it also checks that the enumeration is complete (a
  pending decision with an empty enumeration is a failure unless it is a shaped one).

*Exit:* every `PlayerDecisionEvent` the game can ask has hints or a documented shape; the table has them in `input`.

### C. A table that is pleasant to play (frontend, the biggest milestone)

Goal: a player can play a whole turn without opening the event builder. Split into phases that each end green.

**C1. Inputs on the board and the hand** (uses B)

- **Hand tray** under the board: your Skill Cards as large cards, the ones playable now lit, the others dimmed, with a
  click to play (the card's event built from hints, asking for its choices in a small dialog next to the card, not in
  the side panel). Same tray for Quorum, Mutiny and Loyalty cards.
- **Targets light up**: when the game asks for a player, location, viper, raider, basestar, civilian ship or card
  target, the legal ones glow on the board and click to answer (the "Act"/move model extended). Hover shows what the
  answer does when the label can say it.
- **Viper activation** on the board (still open in `CLAUDE.md`): click a fighter, a menu of its legal activations (move,
  attack, land), with the attack's target picked on the board.
- **Discard / draw / order pickers**: one widget for "pick exactly n of these cards" with a visible counter, one for
  "put these in order" (drag or numbered clicks), replacing the generic list fields.
- **Choices as cards, not strings**: where options name cards, locations or players, show them (`choiceArt` already
  does for art; extend to a grid layout). The labels stay names, never a card's printed text; a rules reminder is a
  link to the rulebook page, quoting the article to read there (open question 1).
- **Undo for the unrevealed**: a "take back" within the player's own current pending input (selection state), not an
  engine undo.

**C2. Turn flow and awareness**

- A **turn banner**: whose turn, which step, a step track you can read at a glance, "your move" shown on the board edge
  as well as the tab title and favicon (exists).
- **Pause explanation**: when the game waits for someone, say what for in a sentence ("Kat is deciding whether to play
  a card on the roll"), not only "Waiting for X". Interrupt windows are the worst case: say which window, how many have
  answered.
- **Log you can read**: group by step, collapse the mechanical facts, keep the story ("Adama played Strategic Planning,
  the roll became 6, 2 raiders destroyed"), filter to "what changed for me". Click an entry to flash what it touched on
  the board.
- **Resource and track deltas**: a floating "-1 morale" where the dial is, when it changes; the same for the jump
  track, distance and the nukes.
- **Animations, small and skippable**: ship moves, die roll, card draw to the hand, a damaged location flashing. A
  setting turns them off.

**C3. Reference without leaving the table**

- **Card browser / rules lookup**: any card name in the log or a prompt is hoverable; a searchable "all cards" drawer;
  the character sheet and the Cylon/human loyalty rules a click away; a rules PDF link per rulebook section, quoting
  the article (open question 1). The PDF in `materials-sorted/` ships with the app: the project is for private play
  (open question 2).
- **Cheat sheets**: the turn sequence, the Attack Table, the skill check order, the Crisis step icons.
- **Explain a rejection**: `InvalidActionException` messages are shown raw today; map the common ones to a sentence
  and a highlight of the part of the input at fault.

**C4. Layout, devices, accessibility**

- A phone / tablet layout: the board stays zoomable, the panels become bottom sheets, the hand tray the primary input.
  Today under 1100px the panels stack under the board; that is a fallback, not a design.
- Board zoom and pan, and a "fit to my turn" button (focus the locations the pending input involves).
- Keyboard play for the common inputs (move, act, pass, confirm, cycle options), focus order, `aria-live` for the
  asked-player change, readable contrast on the red/blue zone cues (a colorblind palette option).
- A settings page for: animations, density, card hover size, sounds, notification choices.

**C5. Polish pass with real players**

- Instrument the table minimally (client-side, no personal data: which inputs fall back to the builder, which
  rejections happen, how long a decision takes) so the next UX round starts from the clunky parts, not from guesses.
  Anything that records a user's play needs a privacy line in G's notice first.

*Exit:* a four-player game from setup to an ending without a single use of the generic builder; the same on a phone
for the one-handed inputs.

### D. Sessions that survive real people (backend + frontend)

- **Presence**: who is connected, shown on the players' mats and in the room; a disconnected asked player is marked.
- **Turn clock and absence** (per-game option, off by default for friends, on for open games): a soft reminder, then a
  vote or host action to hand the seat over (next item). The engine picks a *default answer* only when an input has a
  single answer and every player can see it, e.g. drawing the Skill Cards of a character whose skill set leaves no
  choice (open question 4); it never passes or discards for an absent player. Never silent: a default answer is a
  logged fact with its actor "default".
- **Take over / replace a seat**: a vacated seat (left, banned, abandoned) can be taken by an observer (bots are out of
  scope, open question 7); the replacement sees what the seat sees, no more. The hand and secrets belong to the seat,
  not the user.
- **Host tools**: kick before the start, cancel a lobby, abandon a running game (vote or host, ends it without a
  winner, `FINISHED` with a reason), and a delete of finished games the host owns.
- **Notifications**: "your move" by browser notification and, optionally, email or a Discord webhook; rate limited, off
  by default. A game that lasts 3 to 4 hours spread over days needs this.
- **Async play**: a long-lived game that can sit for days: the session cache already restores from the rows; check
  restart, idle eviction and the `StateTokens` on very old tabs. Also a "last seen / waiting since" in the lobby list.
- **Chat**: public only (open question 5): a game chat for players and a separate one for observers (the table is a
  social game: accusations and bluffing are most of it), and no private messages. Per-game, stored apart from the
  journal (it is not game state), with a rate limit and a mute. Cylon-team private chat is out (rules forbid
  out-of-band communication between hidden Cylons), see "Not in this roadmap".
- **Reconnect polish**: the socket reconnects with backoff already; show a banner, resync the log, keep a half-filled
  input across a reconnect.
- **Game end**: a results screen (winner, winners by name, every revealed loyalty, motive cards, the story of the game,
  turns played), a shareable read-only replay link, and a **rematch** that reopens the room with the same seats.

*Exit:* a game played over three days across restarts, with one player replaced mid-game after the clock ran out and
a default answer given by the engine, finishes with the right ending and the results screen.

### E. Replays, review and "learn from the game"

- **Replay viewer**: any finished game, from the journal, for any viewer's projection, step through the scopes with a
  slider (the sandbox and `LiveUpdateProjection` already do the hard part). All-reveal mode once the game has ended.
- **Export** a game's journal for a bug report, with the format version, from the table (exists for sandboxes only).
- **"Report a bug at this point"**: a button that attaches the journal position and the viewer's last rejection.

*Exit:* a bug report from a player can be turned into a `RegressionJournals` test by pasting it.

### F. Ship it (ops and compliance)

- **CI**: GitHub Actions: `./gradlew build` (Testcontainers needs Docker, available on hosted runners), the e2e job
  against the built jar with the `dev` profile, the nightly fuzzer from A, the `FormatLockTest`. There is no CI today.
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
- **Moderation**: anyone can sign up for free (open question 6), so at minimum: report a user, and ban from a lobby
  (D's host tools).

*Exit:* a public staging environment where an invited group plays a full game and a rollback or error pages the
author.

### Milestone order and dependencies

```text
A fuzzer ──► B legal hints ──► C table UX ──► D sessions ──► E replays
   │                │               │              │
   └── fixes engine └──── upgrades A └──── feeds F's analytics └── needs C's results screen
F (CI, ops, compliance) runs alongside from C on; its staging is the gate for inviting players.
```

A first, because it can change the engine and every later milestone assumes it is sound. B before C because C's
inputs are the legal-hints' consumers. D can start in parallel with C's later phases (it is mostly backend). The rule
for "playable" is the exit line of D plus F's staging.

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

1. **Rules reminders on the table** (C1/C3). A link to the rulebook page with quoted article to read there.
2. **Redistributing the artwork and the rulebook** (C3, F). `materials-sorted/` holds card images and the rules PDF
   that are not ours. Fine for private play as is the intent of this entire project.
3. **Format migration** (F). Freeze `FORMAT_VERSION` at launch (add-only, new facts only). Freeze the format as late as
   possible and never refuse a game after that.
4. **Timeouts and defaults** (D). Default only when it is the only option that is visible to all players. For example
   auto-drawing skill cards when the skill set of the character is fixed.
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
