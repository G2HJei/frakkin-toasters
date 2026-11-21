# Unimplemented components plan

Status (2026-11-21): every phase is done, 16c (audit fixes), 16d (verification fixes) and 17 (docs) included. `unimplemented-components.md` is
now the record of what was built, the choices made and what is still open. Where the build differs from the plan:

- Phase 1: the per-turn limit is `GameFact.TurnLimitUsed(TurnLimit)`, read through `TurnView.usedTurnLimits()`. The
  nuke stays discarded (`NukeDiscarded`), as rule 11.6.1 says, although the Title Card says "removed from the game".
  A 7 with no basestar in the area still destroys up to 3 raiders. `NukeBasestarEvent` carries the roll as `result`,
  so the launcher's pick can't change it. `Actions.canTakeCardAction` is renamed `canTakeOtherAction` (decision 2).
- Phase 2: `TurnLimitUsed` names the player (the Treachery Mutiny Card is once a turn per player), read through
  `TurnView.hasUsed(n, limit)`. "Unauthorized Usage" discards the nukes (errata A2.3.3.2), so nothing limits "Build
  Nuke". The shared look is the existing `LookAtTopCardEvent`, which the "Human Fleet" now queues too. New:
  `ActivateUnmannedViperEvent` (a decision's answer, "Panic"), `ShuffleCivilianShipIntoDeckEvent`, the sandbox's
  `GiveMutinyCardToPlayerEvent`, and the wire schema's `mutinyCard`, which the `EventBuilder` uses as it uses
  `quorumCard`. `LaunchRaidersEvent` is `(raidersPerBasestar, icon)`: only the icon places a basestar on the Cylon
  Fleet board. "Set the Agenda" asks through `SetTheAgendaLookEvent` and `SetTheAgendaChoiceEvent`.
- Phase 3: each power is its own `CylonPower...Event` naming the revealer (`CylonRevealPowers.power`), so the "may"
  ones are `one(power, NoOpEvent)` options. "Damage Galactica" draws as many tokens as there are, up to 5, and offers
  every pair (`ChooseGalacticaDamageTokensEvent`). "Send to Brig" and "Send to Sickbay" target characters on a Galactica
  location only. `FinishCylonRevealEvent` passes the titles before the Loyalty Cards and the Mutineer card, in rule
  6.1.1's order, for the execution too. The execution's choice is `RevealCylonOnExecutionEvent`. The engine test stacks
  the Loyalty deck (phase 7 added the sandbox's `GiveLoyaltyCardToPlayerEvent`).
- Phase 4: the cards read showed -2 morale on "Bomb on Colonial One"'s fail, as the code had. The "Resurrection
  Ship" card prints no limit, so nothing changed there. A new character whose start is on a destroyed Colonial One
  starts in "Sickbay" (rule 7.4; phase 4 let him pick a Galactica location, phase 16c fixed it), and a
  destroyed Colonial One's locations have no action (`GameView.isDestroyed`). `CapricaPlaySuperCrisisCardEvent` moved to
  `event/location/cylon/`. "Psychological Warfare" builds the Destiny deck from as many Treachery cards as are left, up
  to 6 (`ReplaceDestinyDeckEvent`). New: the sandbox's `GiveSuperCrisisCardToPlayerEvent`.
- Phase 5: `ReturnToResurrectionShipEvent` is in `event/player/`, beside the execution. The end of an Infiltration is
  `PlayerOps.endInfiltration()` (the fact, the move, the given Quorum Cards and the Mutiny Cards), which the execution
  calls too; the return then asks for the "Brig" discard. The setup start is
  `PlaceInSetupLocationEventProcessor.startAt`, for every Cylon Leader who starts on a location of the humans.
- Phase 6: `RevealMotiveCardEvent` is in `event/loyalty/`. The cards read: four can be revealed during the game ("A
  Justified Response", "End the Chase", "Make an Ally", "No Unnecessary Force"), the nine others say "if the game is
  over" (`MotiveCards.canReveal`). `GameEngine.submit` runs an `AnytimeEvent` through the same `runAhead` as `inject`;
  `GameSessions.submit` sends one past the pending-decision check, with the player of the connection, the one the event
  names in a sandbox. The schema lists them in `anytimeEvents`, and the frontend's `AnytimePanel` offers them under the
  input panel, the `EventBuilder` narrowing the card to the player's own (`choices`). The sessions test covers the
  backend path; no socket-level test was added, the socket only passes `submit` on.
- Phase 7: `ResolveFinalFiveCardEvent(holder, card, looker)` is a plain event and `ResolveFinalFiveCardsEvent` offers
  the next of several, so the current player orders them (`FinalFiveCards.seen`). `LookAtAllLoyaltyCardsEvent` is the
  look at all of a player's cards, for "Cylon Detector". "You Are Executed" takes the card out of the game before the
  execution, so that execution doesn't resolve it again. Each card an execution reveals makes every other human who
  holds Skill Cards discard 2 (a decision, not random). New: the sandbox's `GiveLoyaltyCardToPlayerEvent`.
- Phase 8: bug 5 wasn't one: a revealed card leaves `loyaltyCards().cards()`, so the end game already charged only the
  unrevealed Personal Goals; a test pins it, and an execution now discards the revealed ones too (`Deck.remove` takes a
  revealed card). A hidden Cylon may reveal a Personal Goal, a revealed one can't. "Selfish" takes at least 20
  strength. "Sacrifice" counts the damaged vipers box (the Mark VIIs that start there too) and the destroyed vipers and
  assault raptors. `LoyaltyDeckOps.addNotACylon` adds nothing once the "You Are Not a Cylon" deck is empty, for the
  execution too. Engine test: `PersonalGoalTest`.
- Phase 9: rule 11.7.2 lets the current player draw a Pegasus token "each time Galactica would be damaged", not only
  after an attack, so every Galactica hit (the attacks, "Test the Limits", the Crisis, Super Crisis, Destination and
  Final Five cards, the consequences) queues `DamageBattlestarEvent`, which decides when it runs: Pegasus is offered
  only while it is in play, so a hit queued before Pegasus was destroyed can't reach it. The fourth damaged location
  (also from the sandbox) queues `DestroyPegasusEvent`; a destroyed Pegasus is `GameView.isDestroyed`, like Colonial
  One, and `LeaveBrigEvent` now refuses both. The viper damage is one fighter at a time (`DamageChosenViperEvent`, one
  option per kind of fighter in each place), and assault raptors are candidates, destroyed when picked. The titles in
  the "Brig" fall back to the highest in line there, and the first to leave takes them (`BrigTitles`); the line of
  succession now skips revealed Cylons. The plain "You Are Not a Cylon" cards are 18 (Appendix component list). The
  todos in `ShipOps`, `PlayerOps.moveTo` and `SkillCheckOps.playFromDestinyDeck` were stale and are gone. Engine test:
  `PegasusDestructionTest`.
- Phase 10: `CharacterAbility` carries an `AbilityKind` (`ACTION`, `MOVEMENT`, `TRIGGER`, `PASSIVE`, `NEGATIVE`) and the
  miracle tokens a use spends (`isMiracle()`), not a "miracle" kind. Whether an ability is in effect is a state rule,
  `PlayerView.hasAbility`; `CharacterAbilities` (`event/character/`) adds `canUseMiracle`, `canUseOncePerTurn` and
  `canAffectSkillCheck`. `AbilityUsed` is a `PlayerFact`. `PlayerOps.loseMiracleToken` became `loseMiracleTokens`
  (all of them), and `gainMiracleToken` records nothing when the player can't gain one. "Sleeper Agent" is its own
  `SleeperAgentEvent`, which sends Boomer to the "Brig" only if she can still go there. "Reckless" doesn't apply to a
  revealed Cylon. A Receive Skills step that draws nothing ("Grieving", "Starts on the Bench") asks nothing. The
  projection's `miracleToken` is the count `miracleTokens`. Engine test: `CharacterSetupTest`.
- Phase 11: `CharacterActionEvent` and its processor are in `event/action/`, beside the Mutiny Cards', and the
  processor's `isValid()` asks `CharacterActions.canUse` (the processor's static `canUse` included). "Organized" and
  "Resolute" take location actions from anywhere through `GameFact.RemoteActionAllowed`/`RemoteActionEnded`
  (`TurnView.remoteActionLocation`, honoured by `LocationActionProcessor`: only that location while it is allowed), with
  the location's own decision; `RemoteActions.locations` lists the undamaged locations with an action, never the
  "Brig". "Resolute" takes any of them, Cylon locations included; "Organized" none of those, 3 different ones, one
  `OrganizedLocationEvent` choice at a time (`OrganizedNextEvent`). "Treatment"'s gift is a choice of
  `TreatmentGiveEvent` pairs, so the Doc can't give to anyone else, and its colors fit the target's skill set
  (`DrawSkillCardsEventProcessor.fitsSkillSet`). "Skilled Politician" holds the chosen card like "Popular Influence"
  and plays it through a decision on its event, discarding it if it can't be played; the others go to the bottom
  (`TitleOps.putDrawnQuorumCardsAtBottom`). "Abuse Power" offers only the cards he can play, and puts all 4 at the
  bottom when none is. "Blind Jump" is `JumpingTheFleetEvent(true)`: the Admiral draws 1 Destination Card and the fleet
  goes there, the Mission Specialist keeps his card, and no FTL roll costs population. "Expert Pilot" records its use
  at `StartTurnEvent`, and the Action step then adds `GrantEvent(ACTION, EXPERT_PILOT)`, nothing once the turn ended
  early. "Meticulous" makes the return action a choice of `MeticulousEvent`s (the "Resurrection Ship" is the plain
  return). "Visions" looks first, then ends the Infiltration (`EndInfiltrationEvent`, without the "Brig" discard of the
  return action). "Primacy" places a basestar while one is in the supply, and only then may take one off; the raiders
  and the civilian ship come together. "Coup" from the "Brig" moves to "Command" and takes the title at once. Engine
  test: `CharacterActionsTest`.
- Phase 12: the "Movement:" abilities are `CharacterMovementEvent`s with `CharacterMovements`, as the actions have.
  "Aggressive Tactics" gives two `GrantEvent(ACTION, AGGRESSIVE_TACTICS)` in the Movement step, and his Action step
  still follows. "Clouded" binds every move Leoben chooses: `MoveEvent`, the "Human Fleet" infiltration and
  `LeaveBrigEvent`. "Heretic" is paid after the move (`HereticDiscardEvent` options), and `Actions.canMove` is false
  while D'Anna holds no Super Crisis Card. The ends of the Movement and Action steps are `EndOfMovementStepEvent` and
  `EndOfActionStepEvent`, queued only when the current player has an ability that acts then. "Kleptomania"'s "Brig"
  is `GameFact.SentToBrigAtEndOfTurn`, which the Discard step turns into a `KleptomaniaBrigEvent` after the discards
  (a Treachery discard may have sent him there first). "Stim Junkie" compares with `GameFact.StartLocationNoted`,
  recorded at the start of Kat's turn only. Engine test: `CharacterMovementTest`, in 4-player games.
- Phase 13: a window asks a player for a card when he holds one for it, then for an ability when he has one
  (`UseRollAbilityEvent`, a secret decision; `RollAbilities.canUse`). The abilities resolve at the close after the
  cards (`RollAbilityResolved` records the use). "FTL Control" rolls with its own window after the roll,
  `AfterFtlControlRoll`, so "FTL Operator" can tell its roll (once per roll: `DieRollView.abilities()`). Choices: one
  reroll a window, so a reroll ability after "Evasive Maneuvers" or another reroll is withdrawn and asked again on the
  new roll. "Longshot" is fixed (`DieRollReplaced(value, true)`): a "Strategic Planning" played with it goes back to
  its hand, and no window opens after it. "Hotshot"'s face (strength +2) takes the cards and the clamp but can't be
  rerolled, by an ability or "Evasive Maneuvers": there is no die. "Hotshot" needs Kat as current and acting player in
  the Action step, any roll then. "ECO Officer" and "Longshot" take any roll of their turn. Super Crisis immunity
  doesn't apply: no Super Crisis Card rolls a die. Engine test: `RollAbilitiesTest`.
- Phase 14: the abilities are offered only to a player who can use them, so games without those characters keep
  their journals. Where the alternative is to go on, the option to go on is the plain event that follows
  (`one(SecretDestinyEvent, ResolveCrisisCardEvent)`, `one(MoralCompassEvent, ResolveCrisisSectionEvent)`, the card's
  activation beside the `ModificationsEvent`s): it names nobody, so the engine asks the ability's player. Choices:
  "Delusional Intuition" and "Inspirational Leader" are for the player who drew the card, the current player in his
  Crisis step (not Starbuck's redraw, nor a Cylon at "Caprica": revealed, his abilities are ignored), and "Inspirational
  Leader" needs William Adama as the acting player too. "Moral Compass" and "Choose a Different Path" act on the choice
  of a section ("X chooses"), not on the targets a card asks for; Helo isn't asked about his own choice, and nothing
  changes a different path. "Mysterious Intuition" is asked before anything else of a Crisis Card's check, and gives a
  pass or a fail, as "Political Prowess" does. "Recon" comes last in the Discard step. "Deceitful" reduces Romo
  Lampkin's first discard of a card's effects (a builder per card, `DeceitfulDiscards`), counted from what he holds
  when it happens; a whole hand becomes "keep 1" (`DISCARD_DOWN_TO_1`). `ScriptedTurns` and `TurnLoopTest` stack more
  Crisis Cards, since Roslin draws 2. Engine test: `CrisisAbilitiesTest`.
- Phase 15: the abilities sit at their moments of the check, each `offer(game)` `NONE` without a player who can use
  it: "Fast Learner" before the Destiny cards, "Blind Devotion" after the last player put cards in, "Quick Fix" then
  "Human Delusion" right after the reveal, "Command Authority" through `OfferCommandAuthorityEvent`, queued before
  `EndSkillCheckEvent` only when someone has the ability and its token, so it asks whoever can still use it after the
  result. A player who may put no card in ("Vanity") isn't asked. Choices: "Human Delusion"'s cards count for the
  strength but their abilities don't resolve (they come after the abilities are found); "Fast Learner" takes any
  deck, Treachery included, and fewer than 3 when the deck and its discard pile have fewer; "Blind Devotion" offers
  every type, the Chief can't see the cards; "Logic-Bound" isn't optional and skips an investigated check, where every
  card shows anyway; the negative abilities don't bind a Super Crisis check either, being abilities. The "before the
  reveal" moments are kept by where the offers are queued, not by the piles: `SkillCheckOps.playCards` puts a card
  straight onto its face-up pile when the face-down one is empty (with "Guts and Initiative" before anyone played),
  which this phase leaves as it was (phase 16 fixed it). Engine test: `SkillCheckAbilitiesTest`.
- Phase 16, triggers: an ability that answers something wherever it happens is noted by the op that causes it and
  offered by the engine right after that event, as `WinChecks` are (`AbilityTriggers`, `PlayerFact.AbilityTriggered`
  and `AbilityTriggerCleared`, `TurnView.triggeredAbilities()`): "Memento" after any population loss
  (`AbstractOps.loseResource`), through `OfferMementoEvent`, when 3 Piloting cards are left. The other triggers sit
  where they happen, queued only in games with their character: "For Love" through `ForcedDiscards` and
  `ForcedDiscardEvent` (the Crisis Card and location check discards of `DeceitfulDiscards`, the reveal powers "Draw
  Treachery" and "Send to Sickbay", the Final Five discards; not the costs a player chooses, the hand limit,
  "Kleptomania" or a Super Crisis Card), with `DiscardKind` replacing `DeceitfulEvent.Discard`; "Escort" on
  `DestroyCivilianShipEvent` (raider attacks, "Main Batteries", "Civilian Self Defense"; not a nuke's 8 nor Kat's
  "Sacrifice", which destroy through the ops), offering the first undamaged viper of each kind in the "Reserves" and
  every viper in space; "Necessary Steps" in `DrawMutinyCardEvent` and the "Press Room", Zarek's own starting card
  ("Disreputable") included; "Dutiful" after "Command", "Communications" and "Weapons Control" (`OfferDutifulEvent`),
  only while Hoshi is still at the location and can act there; "Alert Viper Pilot" after an unmanned launch from the
  "Reserves" and after "Launch Reserves"' activations (`OfferAlertViperPilotEvent`); "Forward Thinker" after "Executive
  Order"'s reset (`OfferForwardThinkerEvent`, a decision on any `LocationEvent`); "Maintenance Engineer" after "Repair"
  on his own turn (`GrantEvent(ACTION, MAINTENANCE_ENGINEER)`); "Raptor Pilot" instead of "Launch Scout"'s look
  (`RaptorPilotEvent`, the two cards put back one after the other); "Adaptable" after any Quorum Card
  (`OfferAdaptableEvent`); "No One Gets Left Behind" after the Cylon ships move in a jump; "Manipulative" first at
  `StartTurnEvent`, given back at `EndTurnEvent` (`GameFact.TitleBorrowed`, `TurnView.borrowedTitles()`), to the next in
  line when the previous holder can't hold it, and not at all when she lost it meanwhile; "Efficient" in the
  "Communications" action, every civilian ship by id. `RepairEventProcessorTest` and
  `CommunicationsEventProcessorTest` now use characters without those abilities. Engine test: `CharacterTriggersTest`.
- Phase 16, penalties and bans: `CharacterPenalties` ("Lies and Secrets" after any reveal, by action or execution;
  "Moral Dilemma" after every Mutiny Card Lee Adama gets; "Alcoholic" first in `StartTurnEvent`; "Forced to Eject"
  where a fighter in space is damaged; "Headstrong" in `ForcedDiscards`, and at the hand limit too: it is a forced
  discard), `CharacterBans` (in `LocationEvent.canActIgnoringDamage`, so "Unorthodox Plan" and the remote actions obey
  them; "Specialized" in `SkillCardPlays` and `SkillCardEventProcessor`; "Spiritual Crisis" in `canReveal`), and
  `ActionCosts`. A cost is paid before the action: the processor records `GameFact.ActionCostPaid(n)` and queues the
  discard and the same event again, which acts and records `ActionCostPaid(null)`, or does nothing when the discard
  took what it needed (e.g. a Treachery Card's Mutiny Card sent the player to the "Brig"); the log shows the event
  twice. `LocationActionProcessor` and `SkillCardEventProcessor` own `process()`, the locations' effects moved to
  `act()`. "Reluctant" offers the other cards as concrete discards, so Hoshi can't discard the card he plays; costs
  are choices, so neither "For Love" nor "Headstrong" touches them. "Emotionally Fragile" is an `AbilityTriggers`
  trigger noted by `AbstractOps.loseResource` (morale at 2 or less), and `ExecuteEvent` got `withoutMoraleLoss`.
  "Nothing But Trouble"'s note bans nothing that exists: no Treachery Card has an action, their text resolves in skill
  checks for the current player, and `CharacterBansTest` pins it. The skill check's face-down pile now has an explicit
  `SkillCheckFact.CardsDivided`: before it every card goes face down, even onto an empty pile ("Guts and
  Initiative"). Tests that put William Adama at the "Admiral's Quarters", Laura Roslin at a location, Helena Cain at the
  "Engine Room" or Lee "Apollo" Adama as a discarding CAG now use other characters. Engine test:
  `CharacterPenaltiesTest`.
- Phase 16c: see its section. The tiebreak is a choice, not a decision. "Mysterious Message" offers its discards the
  same way as "Consult the Oracle". D'Anna Biers starting Infiltrating at the "Press Room" draws 3 starting Skill Cards,
  which follows A3.9's reason for Athena ("starts the game Infiltrating"). Engine test: `SevenPlayerSetupTest`.

Builds every row of the overview in `unimplemented-components.md` that still has something missing, one row at a
time, and the bugs that review lists:

| Row | Missing today | Phase |
|---|---|---|
| Title Card actions (4) | the Admiral's and the CAG's | 1 |
| Mutiny Cards (22) | none can be played | 2 |
| "You Are a Cylon" Loyalty Cards (7) | the voluntary reveal and the 7 reveal powers | 3 |
| Super Crisis Cards (11) | no way to play one, 3 cards without effects, the Human Prisoner fail | 4 |
| Cylon Leaders | the return to the "Resurrection Ship" as an action | 5 |
| Motive Cards (13) | revealing one during the game | 6 |
| Final Five Loyalty Cards (5) | no effect resolves | 7 |
| Personal Goal Loyalty Cards (8) | the reveal action | 8 |
| Pegasus destruction | all of it | 9 |
| Character abilities (99) | 94 | 10 to 16 |

The order isn't the table's: a row comes after the rows it builds on. "Unauthorized Usage" launches a nuke, so the
title actions come first. The reveal hands out Super Crisis Cards and ends a Cylon's chance to play Mutiny Cards. The
character abilities use nearly everything before them (Mutiny Cards, Super Crisis Cards, the reveal, Loyalty looks,
the Leader's return).

The plan is done when:

- every card and ability in the table can happen in a game that `GameEngine.start` began, through `submit` and
  `choose`, with `restore` giving the same game at every pause;
- the overview of `unimplemented-components.md` has "–" in every "Missing" cell, and its bugs are fixed;
- `./gradlew build` is green after every phase: `core`, `backend` (Testcontainers, Docker has to run) and `frontend`.

The effects below are paraphrases of the images in `materials-sorted/` (MutinyCard, SuperCrisisCard, LoyaltyCard,
MotiveCard, Title, Character/card), read for this plan, and of the rulebook (`bsg-rules/`, CR2-0 and its Appendix):
rules 4.3, 4.4, 5.2 to 5.5, 6.1, 6.2, 7.1, 9.4, 11.6.1 and 11.7.2.4. A phase reads its cards again before it builds
them. Where the cards and the rules are silent, the plan makes a choice (see "Open questions").

## Decisions

1. **One event per card or ability, with its choices on it.** The Skill and Quorum Cards work this way: a play is one
   submit, and its processor checks the whole play. A choice that depends on what the card reveals comes after it, as
   a decision or a `one(...)` of concrete events that name the player who chooses. Each "can't play if" is a static
   `canPlay` on the processor, collected in an exhaustive switch (`MutinyCardPlays`, `CharacterAbilities`), so a new
   card or ability doesn't compile until it is mapped, and a decision to play one always has an answer.
2. **Everything that is "an action" plugs into `Actions`.** The Admiral's and the CAG's title actions, Mutiny Cards,
   the Cylon reveal, a Personal Goal's reveal, the Leader's return and the character sheets' "Action:" abilities are
   `ActionEvent`s. `Actions.canTakeCardAction` (renamed `canTakeOtherAction`: not only cards any more) asks each of
   them, so the Action step, the grants and a pilot's second decision offer them. A Cylon Leader may use his sheet's
   actions instead of his location's (rule 6.2.1.2). Revealed Cylons get only their location's action, as today.
3. **Anytime input.** A Motive Card may be revealed "any time its requirements are currently being met", even in the
   middle of resolving something (rule 6.2.2.1). `AnytimeEvent extends PlayerEvent` marks such an event.
   `GameEngine.submit` takes one at any pause without answering the pending input: it runs ahead of it, like a sandbox
   `inject`, and the journal records it as `EventInjected`, so `AgendaReplayer`, `restore` and `undo` work unchanged.
   The backend's `submit` passes it on as it is. The frontend gets an "Anytime" control (phase 6).
4. **Abilities in effect.** `CharacterAbility` lists the 99 abilities with their character and kind.
   `CharacterAbilities.has(game, player, ability)` is true when the player's character prints it and he isn't a
   revealed Cylon, whose abilities are ignored (rule 6.1.2.2), while a Cylon Leader's are always in effect (rule
   6.2.1.2). A miracle ability also needs a miracle token, and no "The Farm" in play for a human. Every use records a
   public `AbilityUsed(playerNumber, ability)`, which `TurnView.usedAbilities()` keeps until `TurnStarted`, for the
   "once per turn" abilities and the log. A miracle use records `MiracleTokenLost` too.
5. **Miracle tokens are a count** (rule 4.3.1.1): at most 1, at most 3 for Gaius Baltar (alt.), none for a revealed
   Cylon (4.3.2). `PlayerView.miracleTokens()`, and `hasMiracleToken()` stays as "more than 0".
6. **Per-turn limits** are facts kept by the turn and cleared by `TurnStarted`, as `playedSkillCards()` is: the CAG's
   pilot action, the Mutiny Card drawn for discarding Treachery (rule 10.2.1), and the abilities (decision 4).
7. **The reveal's discards, then the power, then the other reveal steps.** "Draw Treachery Cards" says its player
   draws "after first discarding down to 3", so step 1 of rule 6.1.1 (the discards) comes before the card's power, and
   steps 2 to 7 after it, while the Cylon is still where he revealed.
8. **Skill Cards change hands** through `PlayerOps.giveSkillCards(recipient, cards)`: `CardsLost` and `CardsGained`.
   Both players see the cards, the others a count with the colors. "Blackmail", "Human Prisoner", "Intimate",
   "Treatment", "Cryptic Message", "Attorney" and "Politically Adroit" use it.
9. **Super Crisis Cards are immune** to the character abilities that affect Crisis Cards or skill checks (rule
   6.1.2.5). `SkillCheckType.isSuperCrisis()` tells, and those abilities check it.
10. **Each phase finishes its own plumbing.** A phase that adds events or facts bumps `JournalCodec.FORMAT_VERSION`
    once and rewrites the format lock (`-DupdateFormatLock=true`), gives new int components a `WireSchema` role, marks
    new card components in `Disclosure.CARD_COMPONENTS`, adds a `LiveUpdateProjector` rule per new fact, runs
    `./gradlew :backend:exportSchema` and `npm run gen`, and adds labels where the humanized fallback reads badly. It
    fixes the `CLAUDE.md` lines it makes stale and its rows in `unimplemented-components.md`. Its tests: a processor test
    per event, `assertInvalid` for each "can't", and an engine test that plays it through `submit` and `choose`,
    restoring at every pause (`MutinyCardActionsTest`, `CylonRevealTest`, ...).

## Phase 1 - Title Card actions

The Admiral's and the CAG's actions (`event/action/title/`, beside `PresidentTitleActionEvent`).

- **The nuke** (rule 11.6.1, which targets a space area; the Title Card's older text says a basestar).
  `LaunchNukeEvent(playerNumber, area)`: the Admiral, human, with a nuke, at a space area. The nuke is discarded
  (`GameOps.discardNuke`), then `rollDie(new NukeResultEvent(playerNumber, area))`:
  - 1-2: a basestar there is damaged twice; 3-6: destroyed; 7: a basestar and 3 raiders destroyed; 8: every ship in
    the area is destroyed, raiders, heavy raiders, basestars, vipers (a pilot goes to "Sickbay"), raptors and civilian
    ships.
  - With several basestars there, the Admiral picks one (`NukeBasestarEvent(playerNumber, basestarId, roll)` options).
    Nothing of the right type there: no effect, the nuke is still spent.
  - A destroyed basestar opens the "Major Victory" window, as an attack's does.
  - The roll is the Admiral's, so his abilities and the interrupt windows apply.
- **The CAG's first action**: `PassCagTitleEvent(playerNumber, activation, newCagPlayerNumber)`: activate an unmanned
  viper (`ViperActivation`, checked like `ProtectTheFleetEvent`), then give the CAG title to another human who can take
  it (`MakeCagEventProcessor.canBecomeCag`). Offered when an unmanned viper can be activated and someone can take the
  title.
- **The CAG's second action**: `CagPilotActionEvent(playerNumber, activation)`: once per turn, while piloting, activate
  an unmanned viper, then `Actions.offerAction` again (decision 6: `GameFact.TurnLimitUsed(CAG_PILOT_ACTION)`).
- `Actions.canTakeOtherAction` asks `TitleActions.canAct(game, player)` for the three title actions.
- Unauthorized Usage (phase 2) reuses `NukeResultEvent`.

## Phase 2 - Mutiny Cards

- `MutinyCardEvent extends ActionEvent` (`event/action/`), one record per card in `event/action/mutiny/`, named after
  the card, the card's choices on it. `MutinyCardEventProcessor<T>`: its final `isValid()` checks that the player is
  human (an Infiltrating Cylon Leader counts, rules 5.2.3.4 and 5.2.3.5) and holds the card, also in the "Brig" (rule
  5.2.3.2), then `isValidPlay()`. Its final `process()` takes the card out of his hand and calls `resolve()`, which ends
  with the card's fate: the Mutiny discard pile, or out of the game (`DeckFact.CardsRemovedFromGame(MUTINY, card)`).
- `MutinyCardPlays.canPlay(game, player, card)` (exhaustive switch over `MutinyCard`, asking each processor's static
  `canPlay`) and `canPlayAny`. `MutinyCardEventsTest` checks one event per card.
- Building blocks:
  - `PlayerOps.giveSkillCards` and `takeRandomSkillCards` (decision 8).
  - Placing a basestar and raiders in a chosen space area (`PlaceShipsEvent` gets an area, `null` for in front of
    Galactica), and `LaunchRaidersEvent` with a count of raiders per basestar.
  - ~~Nukes removed from the game~~: errata A2.3.3.2 discards them, so "Build Nuke" is unchanged.
  - The one Mutiny Card per turn for discarding Treachery (rule 10.2.1, the todo in
    `DiscardSkillCardsEventProcessor`), as a per-turn limit (decision 6).
  - A shared "look at the top card, keep it on top or put it at the bottom" followup for Crisis and Destination
    cards, from `HumanFleetLookAtTopCrisisCardEventProcessor` and the Destination ones.

| Card | Effect | Can't play when | Fate | Event |
|---|---|---|---|---|
| Armed Resistance | The Admiral goes to "Sickbay", then look at the top Crisis Card: top or bottom | – | discard | `ArmedResistanceEvent` |
| Assume Command | Discard 5 Skill Cards, become Admiral | fewer than 5 cards, already Admiral, in the "Brig" | discard | `AssumeCommandEvent(discards)` |
| Bait and Switch | Draw 2 Skill Cards of any colors, then 2 Treachery shuffled into the Destiny deck | – | discard | `BaitAndSwitchEvent` |
| Betrayal of Trust | Draw 2 Treachery, then look at the top Destination Card: top or bottom | – | discard | `BetrayalOfTrustEvent` |
| Blackmail | Take 3 random Skill Cards from the President | you are President, he holds 2 or fewer | discard | `BlackmailEvent` |
| Clipped Wings | Every viper in space back to the "Reserves", every damaged viper repaired, then draw 2 Treachery | no viper in space | discard | `ClippedWingsEvent` |
| Controversial Speech | Roll: 6+ +1 morale and the card leaves the game; otherwise discard it and every player draws 1 Treachery | – | by the roll | `ControversialSpeechEvent` |
| Feed the People | +1 food, jump track -2 | – | out of the game | `FeedThePeopleEvent` |
| Impeachment | Discard 5 Skill Cards, become President | fewer than 5 cards, already President | discard | `ImpeachmentEvent(discards)` |
| Make a Deal | Move a character from the "Brig" to a Galactica location; discard the card, then a chosen player draws a Mutiny Card | nobody in the "Brig" | discard | `MakeADealEvent(target, destination, drawer)` |
| Necessary Risk | +1 food, then a basestar and 3 raiders in a chosen space area, as many as there are | – | out of the game | `NecessaryRiskEvent(area)` |
| Panic | A civilian ship behind Galactica, then activate an unmanned viper if possible | – | discard | `PanicEvent` |
| Peaceful Resistance | You go to "Sickbay" (in the "Brig" you stay, A2.3.3.1), then roll: 4 or less, the Admiral goes to the "Brig" | – | discard | `PeacefulResistanceEvent` |
| Ruined Reputation | A chosen human draws 2 Skill Cards of any colors, then roll: 4 or less, he goes to the "Brig" | – | discard | `RuinedReputationEvent(target)` |
| Scavenging for Parts | Damage Galactica, then a civilian ship in space goes back into the civilian ship deck, if any | – | discard | `ScavengingForPartsEvent(civilianShipId)` |
| Selfish Act | Draw 2 Skill Cards, discard the card, then draw a Mutiny Card | – | discard | `SelfishActEvent` |
| Send a Message | Damage Galactica, then attack a centurion on the Boarding Party track with +2, if any | – | discard | `SendAMessageEvent` |
| Set the Agenda | The President draws 2 Quorum Cards; you look at his hand and put 2 of them at the bottom of the deck | you are President | discard | `SetTheAgendaEvent` |
| The Strong Survive | Draw and destroy a civilian ship, then jump track +1 | – | out of the game | `TheStrongSurviveEvent` |
| Unauthorized Usage | Launch a nuke at a space area (errata A2.3.3.2), then every nuke token and the card leave the game | the Admiral has no nukes | out of the game | `UnauthorizedUsageEvent(area)` |
| Violent Protest | Draw 2 Politics, the President goes to "Sickbay" (stays in the "Brig", A2.3.3.3) | – | discard | `ViolentProtestEvent` |
| Weapons Armed | A raptor from the "Reserves" is destroyed and an assault raptor takes its place, then each basestar launches 2 raiders | no raptor in the "Reserves", no assault raptor left in the supply | discard | `WeaponsArmedEvent` |

## Phase 3 - The Cylon reveal

- `RevealCylonEvent(playerNumber, card)` (`event/loyalty/`), an `ActionEvent`: a human who isn't a Cylon Leader and
  holds the "You Are a Cylon" card. It records `LoyaltyCardRevealed`, then queues step 1 (the discards), the card's
  power (none in the "Brig", rule 6.1), then `FinishCylonRevealEvent(playerNumber, superCrisisCard = true)` for steps
  2 to 7 (decision 7).
- `FinishCylonRevealEvent` follows rule 6.1.1, and `ExecuteEventProcessor.cylon()` queues it without the Super Crisis
  Card:
  1. Discard down to 3 Skill Cards (a decision with a new `discardDownTo(3)` constraint, only when he holds more), all
     Mutiny Cards, the miracle tokens, the Quorum Cards given to him (`TitleOps.discardQuorumCardsGivenTo`).
  2. Lose the titles (`GiveTitlesToNextInLineEvent`).
  3. To the "Resurrection Ship"; a piloted viper goes back to the "Reserves".
  4. Draw a Super Crisis Card (not after an execution).
  5. Hand the other Loyalty Cards to a human (not from 7 distance on), and 6. the Mutineer card (always).
  7. The turn ends if it is his (`endTurnEarly`), and the current player token moves on.
- The execution reveals the "You Are a Cylon" card the player chooses when he holds two different ones (the todo in
  `ExecuteEventProcessor`).
- The 7 powers, each a `one(effect, NoOpEvent(revealer))` where the card says "may" (all but "Draw Treachery
  cards"), with concrete options that name the revealer:

| Card | Power |
|---|---|
| Can place a centurion | May place a centurion at the start of the Boarding Party track |
| Damage Galactica | May draw up to 5 Galactica damage tokens, resolve 2 of them, the others go back |
| Decrease jump track | May move the jump track back 2 |
| Draw Treachery cards | Must: each human discards a random Skill Card and draws a Treachery card, then the Cylon draws 2 Treachery |
| Reduce morale | May lose 1 morale |
| Send to Brig | May send a character on Galactica to the "Brig" |
| Send to Sickbay | May choose a character on Galactica, who discards 5 Skill Cards and goes to "Sickbay" |

- `Actions.canTakeOtherAction` offers the reveal. Gaius Baltar (alt.)'s "Spiritual Crisis" will limit it in phase 16.

## Phase 4 - Super Crisis Cards

- `SuperCrisisCard` gets what is printed, as `CrisisCard` has: its `SkillCheckType` (`null` for the three choice
  cards) and its `CrisisChooser`.
- `CapricaPlaySuperCrisisCardEvent` gets its processor (bug 1): the player holds the card. It moves the card out of his
  hand and queues `ResolveSuperCrisisCardEvent(playerNumber, card)`, which starts the skill check, or offers the
  chooser `ChooseSuperCrisisSectionEvent(chooser, card, section)` with `SuperCrisisCardFollowups`. No Activate Cylon
  Ships step (the "Caprica" card). Delete the placeholder `PlaySuperCrisisCardEvent`; `ProcessorRegistryTest` uses
  another event without a processor.
- The card goes to the Super Crisis discard pile once resolved, unless kept in play ("Bomb on Colonial One", "The Farm"
  on a fail, through `KeepCrisisCardInPlayEvent`).
- The skill check records who played the card (`SkillCheckView.playedBy()`), for "Human Prisoner": on a fail the Cylon
  takes the Skill Cards of a human he chooses, who goes to "Sickbay" (decision 8).
- The three choice cards:
  - "Fighting Blind" (the CAG): 2 centurions at the start of the Boarding Party track, or the CAG is executed.
  - "Psychological Warfare" (the President): -1 morale and each player discards 2 Skill Cards, then draws 2
    Treachery; or each revealed Cylon draws 2 Treachery, then the Destiny deck is discarded and rebuilt from 6
    Treachery cards (a new event).
  - "Demand Peace Manifesto" (the Admiral): -1 morale and Galactica damaged twice, or the President and the Admiral
    discard their Skill Card hands.
- Check the 8 mapped results against the cards: the reader saw -1 morale on "Bomb on Colonial One"'s fail, the code
  has -2.
- "Bomb on Colonial One" in play closes Colonial One (rule 9.4): its characters go to "Sickbay", nobody moves there,
  and a new character whose setup is there starts in another start location. "The Farm" in play blocks the humans'
  miracle abilities (decision 4, used from phase 10).
- `SkillCheckType.isSuperCrisis()` (decision 9).
- The "Resurrection Ship"'s action: read the location card and fix it if it lets a player draw without limit.

## Phase 5 - Cylon Leaders

- `ReturnToResurrectionShipEvent(playerNumber)`, an `ActionEvent` for an Infiltrating Cylon Leader (rule 6.2.3.5, the
  Infiltration card): discard the Quorum Cards given to him and his Mutiny Cards, in the "Brig" discard down to 3
  Skill Cards (`discardDownTo(3)`), `InfiltrationEnded`, to the "Resurrection Ship". He keeps his miracle token (4.3.2).
- The execution's Leader path ends the Infiltration the same way.
- D'Anna Biers' "Press Room" start: a Cylon Leader who starts on a human location starts Infiltrating, as Sharon
  "Athena" Agathon does (the "Unverified" note in the review).

## Phase 6 - Motive Cards during the game

- `AnytimeEvent` and the engine (decision 3), with `GameEngineTest` cases: during a decision, during a choice, a
  secret input, restore and undo after it, rejected when invalid (nothing changes).
- `RevealMotiveCardEvent(playerNumber, card)`: a Cylon Leader reveals a Motive Card whose requirement holds now.
  `MotiveCards.canReveal(game, player, card)` for the game in progress; the cards that say "if the game is over" can't be
  revealed before the end. `EndGame.end` still reveals at the end what it can.
- Frontend: the input panel lists the `AnytimeEvent`s of the schema the player can build (a Cylon Leader with Motive
  Cards in hand), outside the pending input. `LiveGameTest` or `SandboxLiveTest` covers one through the socket.

## Phase 7 - Final Five

- Every look at another player's Loyalty Card goes through `PlayerOps.lookAtLoyaltyCard`, which returns the card seen.
  A Final Five card queues `ResolveFinalFiveCardEvent(holderPlayerNumber, card, lookerPlayerNumber)` (rule 5.4.1.1):
  a public `FinalFiveCardShown` fact, then its effect. The holder shuffles it back among his Loyalty Cards. With
  several, the current player orders them.
- The looks: `LookAtLoyaltyCardEvent`, "Release Cylon Mugshots", and from phase 11 "Cylon Detector" (all of a player's
  cards) and "Visions".
- An execution (rule 5.4.1.2, the todo in `ExecuteEventProcessor`): the Final Five cards resolve, then leave the game
  (`CardsRemovedFromGame(LOYALTY)`), the other cards are discarded. Revealed Cylons ignore them.
- On an execution, each of the 5 cards makes every player discard 2 Skill Cards. When another player looks at it:

| Card | When another player looks at it |
|---|---|
| Cylon ships activate | The Cylon ships activate by the card's icons |
| Damage Galactica twice | Galactica is damaged twice |
| They are executed | The player who looked is executed |
| You are executed | The holder is executed, the others discard 2 random Skill Cards (no reshuffle: he is executed) |
| You are sent to Brig | The player who looked goes to the "Brig" |

## Phase 8 - Personal Goals

- `RevealPersonalGoalEvent(playerNumber, card, discards)`, an `ActionEvent`, only while the card's condition holds
  (rule 5.3.1). `discards` is for "Selfish" only (Skill Cards worth 20 strength), empty otherwise. It records
  `LoyaltyCardRevealed`, and at 6 distance or less adds a "You Are Not a Cylon" card to the Loyalty deck, shuffles it
  and draws (`FinishExecutionEvent`'s path, shared).
- `EndGameEventProcessor` charges only the Personal Goals that aren't revealed (bug 5).

| Card | Condition |
|---|---|
| Acquire Power | He holds 2 or more titles |
| Devastation | The Admiral has no nukes left |
| Political Intrigue | The President is in the "Brig" |
| Sacrifice | 6 or more vipers damaged or destroyed |
| Selfish | Discard Skill Cards worth 20 strength |
| Self Destruction | He is in the "Brig" or "Sickbay" |
| Stand and Fight | 10 or more raiders on the main board |
| Use Caution | A destination of distance 1 has been reached |

## Phase 9 - Pegasus destruction and loose ends

- Rule 11.7.2.4: when all four Pegasus locations are damaged, `DestroyPegasusEvent` moves everyone on Pegasus to
  "Sickbay" and records `PegasusDestroyed` (`BoardsHolderView.pegasusDestroyed()`). Then nobody moves to or starts on
  Pegasus (Helena Cain starts at "Command"), its locations have no action, a hit is a Galactica hit
  (`AttackGalacticaResultEventProcessor`, bug 2), and the board shows it destroyed.
- The other todos a full game reaches: the player taking an action chooses which vipers are damaged
  (`DamageVipersEventProcessor`, rule 11.3.2.2), all humans in the "Brig" and the first to leave becomes Admiral and
  CAG (`LeaveBrigEventProcessor`), the stale todo in `PlayerOps.moveTo`, the Cylon ships moving to the Cylon Fleet
  board on a jump (`ShipOps`), and the count of plain "You Are Not a Cylon" cards (`LoyaltyDeckOps`, rule A3.5).

## Phases 10 to 16 - Character abilities

Every ability of the 33 sheets, by where it plugs in. A revealed Cylon's are ignored, a Cylon Leader's always apply,
Super Crisis Cards are immune (decisions 4 and 9).

### Phase 10 - The plumbing, setup and skill draws

- `CharacterAbility`, `CharacterAbilities`, `AbilityUsed`, `TurnView.usedAbilities()` (decision 4), miracle tokens as
  a count (decision 5).
- Setup: Disreputable (Tom Zarek (alt.) starts with a Mutiny Card), Coward (Gaius Baltar's second Loyalty Card only at
  the start, bug 3), Sleeper Agent (Boomer: 2 cards in the Sleeper Agent phase, then the "Brig", bug 4).
- Receive Skills: Industrious, Family Commitments, Grieving, Starts on the Bench, and the todo in
  `ReceiveSkillsStepEventProcessor`.

### Phase 11 - Action abilities

`CharacterActionEvent extends ActionEvent`, one record per ability in `event/action/character/`, with
`CharacterAbilities.canUse` in `Actions.canTakeOtherAction`; a Cylon Leader takes them instead of his location's
action (rule 6.2.1.2).

| Character | Ability | Effect |
|---|---|---|
| Cavil | Primacy | A basestar in front of Galactica, or remove one when all are in play; then may place 3 raiders in front and a civilian ship behind |
| D'Anna Biers | Visions | Infiltrating: look at a random Loyalty Card of a human, then end the Infiltration at the "Resurrection Ship" |
| D'Anna Biers | Don't Trust Anyone | Miracle: draw 2 Super Crisis Cards |
| Gaius Baltar (alt.) | Cult Leader | Give a miracle token to a player at his location, or take his |
| Gaius Baltar (alt.) | Broadcast | Spend 3 miracle tokens: +2 to one resource |
| Samuel T. Anders | Star Player | Discard any number of Skill Cards, draw as many of one type of his skill set |
| Sherman "Doc" Cottle | Treatment | Choose a human: draw 2 from his skill set, give him 2 Skill Cards |
| Callandra "Cally" Tyrol | Discharge of a Firearm | Miracle: execute another character at her location |
| Felix Gaeta | Coup | Miracle: take the Admiral title, from the "Brig" via "Command" |
| Gaius Baltar | Cylon Detector | Miracle: look at all of a player's Loyalty Cards |
| Helena Cain | Blind Jump | Miracle: at 6 distance or less, destroy 2 civilian ships and jump, even from the red zone; the Admiral draws 1 Destination Card |
| Laura Roslin | Skilled Politician | Miracle: draw 4 Quorum Cards, resolve 1, the rest to the bottom, President or not |
| Lee "Apollo" Adama | CAG | Miracle: activate up to 6 unmanned vipers |
| Leoben Conoy | Cryptic Message | Miracle: swap Skill hands with a player |
| Louanne "Kat" Katraine | Sacrifice | Miracle, piloting: go to "Sickbay" to destroy 5 raiders, 2 heavy raiders, 1 basestar or 1 civilian ship in her area |
| Louis Hoshi | Organized | Miracle, not in the "Brig": activate 3 different undamaged non-Cylon locations from anywhere |
| Romo Lampkin | Attorney | Miracle: move a character from the "Brig" to a non-hazardous Galactica location, and take his player's Skill Cards |
| Saul Tigh | Declare Martial Law | Miracle: the Admiral becomes President |
| Sharon "Athena" Agathon | Resolute | Miracle: activate any undamaged location |
| Sherman "Doc" Cottle | Quarantine | Miracle: look at every civilian ship in space, replace one with a new one, shuffle it back |
| Tom Zarek | Unconventional Tactics | Miracle: -1 population, +1 of another resource |
| Tom Zarek (alt.) | Abuse Power | Miracle: draw 4 Mutiny Cards, play 1, the rest to the bottom; no "Necessary Steps", no "Brig" |
| Tory Foster | Influential | Miracle: look at the top 5 Quorum Cards and reorder them, President or not |
| Kara "Starbuck" Thrace | Expert Pilot | Starting her turn in a viper, 2 actions in her Action step |
| Aaron Doral | Meticulous | Miracle, when he ends his Infiltration with an action: any Cylon location instead, and another action |

### Phase 12 - Movement and position

- Movement abilities (`MovementAbilityEvent`): Intimate (Caprica Six), Aggressive Tactics (Cavil, miracle: 2
  actions), Glimpse the Face of God (Leoben Conoy).
- Moving: Clouded (no voluntary move to an occupied location), Heretic (D'Anna discards a Super Crisis Card to leave
  the "Resurrection Ship").
- The end of the Movement step: Politically Adroit (Ellen Tigh), Kleptomania (Romo Lampkin). The end of the Action
  step: Stim Junkie (Kat).

### Phase 13 - Die rolls

The interrupt windows take abilities as well as cards: a window asks each player who holds a card for it or has an
ability for it, and the answer is a card or an ability (`UseRollAbilityEvent`).

- ECO Officer (Helo: once per turn, on his turn, reroll), FTL Operator (Felix Gaeta: reroll "FTL Control"), Longshot
  (Anders: miracle, choose the result on his turn, unmodifiable), Hotshot (Kat: in her Action step, a discarded card's
  strength +2 instead of the roll).

### Phase 14 - Crisis Cards

- Religious Visions (Roslin draws 2 Crisis Cards, resolves 1), Delusional Intuition (Baltar draws a Skill Card of any
  type after drawing a Crisis Card), Inspirational Leader (William Adama: strength 1 counts positive on his Crisis
  checks), Recon (Boomer, end of turn: top Crisis Card to top or bottom).
- Choices: Amoral (Tory Foster takes the first option of "current player chooses"), Choose a Different Path (Lee Adama,
  miracle: "the current player discards 5 Skill Cards" instead), Moral Compass (Helo, miracle: change a choice made on a
  Crisis Card).
- Secret Destiny (Starbuck, miracle: discard the revealed Crisis Card, draw another), Mysterious Intuition (Boomer,
  miracle: choose a Crisis check's result), Modifications (Simon O'Neill, miracle: choose the activation at the start of
  an Activate Cylon Ships step), Deceitful (Romo Lampkin: one card fewer on a Crisis discard, once per card).

### Phase 15 - Skill checks

- Into a check: Vanity (Doral: none in another player's Action step), Impulsive (Cally: 0 or 2+, not in the "Brig"),
  Misguided (Gaeta: at most 3), Calculating (Simon O'Neill: 2, Infiltrating 3, not in the "Brig"), Logic-Bound (Simon
  O'Neill: one face up).
- Around it: Fast Learner (Dee, miracle: the top 3 of a Skill deck into the check or her hand), Blind Devotion (Chief,
  miracle: a type counts 0), Quick Fix (Cally: discard 1 revealed card on her turn), Human Delusion (Caprica Six,
  miracle: add cards after the reveal), Command Authority (William Adama, miracle: take the check's cards).
- Location checks: Cylon Hatred (Saul Tigh: "Admiral's Quarters" -3), Insubordinate (Starbuck: -3 against her),
  Friends in Low Places (Tom Zarek: "Brig" ±2; the "Administration" has no check), Intolerant (Cain: a pass of 10+
  may execute instead).

### Phase 16 - Other triggers, penalties and bans

- Triggers: Efficient (Dee: any number of civilian ships at "Communications"), Dutiful (Hoshi), Maintenance Engineer
  (Chief, after "Repair"), Forward Thinker (Lee Adama, after "Executive Order"), Alert Viper Pilot (Apollo), Raptor
  Pilot (Helo (alt.), "Launch Scout"), Memento (Hot Dog), Escort (Hot Dog, miracle), For Love (Athena), Necessary
  Steps (Tom Zarek (alt.)), Adaptable (Tory Foster), No One Gets Left Behind (Helo (alt.), miracle), Manipulative
  (Ellen Tigh, miracle).
- Penalties: Emotionally Fragile (Dee), Forced to Eject (Hot Dog), Lies and Secrets (Cavil), Moral Dilemma (Lee
  Adama), Alcoholic (Saul Tigh), Headstrong (Apollo: forced discards are random).
- Bans and costs: Emotionally Attached (William Adama), Bent on Revenge (Cain), Convicted Criminal (Tom Zarek),
  Terminal Illness (Roslin), Conflicted Loyalties (Caprica Six), Reluctant (Hoshi), Specialized (Doc Cottle), Spiritual
  Crisis (Baltar (alt.)), and Nothing But Trouble's note (humans can't use Treachery text).

## Phase 16c - Audit fixes

A read-only audit of the rulebook against the code, after phase 16, found what a full Kobol game still trips on:

- A 7 player game stalled at character selection: every non-Leader was refused. The last seat to choose must take the
  Cylon Leader if nobody has (rule A3.7.2.2). Boomer and Athena aren't versions of one character (rule 6.2.4).
- The interrupt tiebreak asked the current player for cards he couldn't see. It is a choice of
  `TiebreakInterruptCardsEvent` options, which show them to him.
- "Consult the Oracle" and "Mysterious Message" asked the browser for Destiny cards it doesn't know: the discards are
  options (`DiscardFromDestinyDeckEventProcessor.options`), which only the player sees. "Dogfight" and "Quick
  Thinking" take a card of the check: the picker offers the check's face-up cards (`TableContext.checkCards`).
- A new character whose start is unavailable starts in "Sickbay" (rule 7.4).
- Every discard the player chooses draws the Treachery Mutiny Card (rule 10.2.1, A2.3.8.1, `TreacheryDiscards`), and a
  new character who keeps the Mutineer card draws one (rule 5.2.1).
- "Disabled Hangar" stops every launch from its basestar (rule 11.7.3).
- Out of ships, every ship of the Cylon space area moves (rule 11.4.1.2), and nothing when no ship of the type is on
  the Cylon Fleet board (the code threw).
- The raiders activate a space area at a time in the current player's order (rule 11.5, `ActivateRaiderAreasEvent`).
  Basestars and heavy raiders stay clockwise: their order can't change the outcome.
- Stale todos in `AdvancePursuitTrackEvent` and `GalacticaOps` are gone.

Format version 37.

## Phase 16d - Verification fixes

Six read-only agents checked every Mutiny, Title, Super Crisis, Loyalty and Motive Card and the 99 abilities against
the images, and researched the deck counts. What they confirmed:

- The decks: the Skill decks follow Appendix A2.3.8 (each color 35 cards, Treachery 26, `AllCardsProviderTest`), the
  Crisis deck has its 141 cards (3 more "Food Shortage", 2 more "Water Shortage" with the basestar and the jump icon),
  and the Quorum deck's 29 cards were already right. Sources are in `unimplemented-components.md`.
- "Make a Deal" takes any Galactica location, "Sickbay" included (`MoveFromBrigEventProcessor.isGalacticaDestination`).
- "Each revealed Cylon" includes a Cylon Leader who isn't Infiltrating (rule 6.2.1): "Footage Transmitted",
  "Psychological Warfare". Its first section lets a Cylon player keep his cards but draw the Treachery. "Footage
  Transmitted" draws no Super Crisis Card when none is left.
- "Resolute" (errata A2.1.1.1): no "Hangar Deck" from the "Brig", no second Infiltration.
- "For Love" reduces "Kleptomania" (`ForcedDiscards`). "Modifications" is asked in a step without a Crisis Card
  (`offerWithoutCard`). "Visions" waits out "Detector Sabotage" and ends from the "Brig" with the discard down to 3.
  "Primacy" places from the Cylon Fleet board when the supply is out. "Delusional Intuition" follows the Gas Cloud's
  draw. "Dutiful" follows an activation from anywhere, never inside "Organized". "Alert Viper Pilot" comes before
  "Launch Reserves" activates its vipers. "Clouded" forbids the return to an occupied "Resurrection Ship". Kat's
  "Sacrifice" destroys a civilian ship through `DestroyCivilianShipEvent`, so "Escort" can act.

No new journal types: the format version stays 37.

## Phase 17 - Docs

Each phase fixes the `CLAUDE.md` lines it makes stale. This phase adds the sections: "Mutiny Cards", "The Cylon
reveal" (with Final Five, Personal Goals, Motive Cards and anytime input), "Character abilities", the roadmap, the
test classes under "Testing Patterns", the new packages under "Key Packages". `unimplemented-components.md` becomes a
short record of what was built and the choices made.

Built as: "Mutiny Cards" split out of "End of Game and Titles", the reveal, the looks, the Final Five, the Personal
Goals, the Motive Cards and the Cylon Leaders gathered in "Cylons and Loyalty Cards", anytime input under "Engine".

## Open questions

Each has the choice the plan makes. A different answer changes only its phase.

1. **The reveal's discards before the power** (decision 7). Only "Draw Treachery Cards" orders them, and the other
   powers don't care, so all reveals do it the same way.
2. **The nuke follows rule 11.6.1**, a space area and the 8 that destroys everything, not the older Title Card text.
3. **"Weapons Armed" prints no fate.** It is discarded like the others.
4. ~~**Nuke tokens "removed from the game"** by "Unauthorized Usage".~~ Settled by errata A2.3.3.2: they are discarded.
5. **Motive Cards are revealed at a pause.** A requirement met only between two pauses can't be caught.
6. **The CAG's first action needs an unmanned viper to activate.** The card says "activate, then give".
7. **D'Anna Biers at the "Press Room" starts Infiltrating.** No rule says so; a Cylon Leader on a human location is
   Infiltrating everywhere else.

## Not in this plan

- New Caprica, Ionian Nebula and Earth rules: "Hub Destroyed", "Detention", allies, trauma.
- Legal-move hints for the frontend.
