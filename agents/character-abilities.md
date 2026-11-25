# Character abilities

`CharacterAbility` lists the three abilities of each sheet (`CharacterAbility.of(character)`), with its `AbilityKind`
(`ACTION`, `MOVEMENT`, `TRIGGER`, `PASSIVE`, `NEGATIVE`) and the miracle tokens a use spends (`isMiracle()`: 1, 3 for
"Broadcast", rule 4.3.1.1). Every ability is built; below, grouped by where it
hooks in. An ability that answers something is offered only to a player who can use it, so a game without its
character pauses where it did.

- `PlayerView.hasAbility(ability)`: the character prints it and it is in effect. A revealed Cylon's are ignored (rule
  6.1.2.2), a Cylon Leader's always apply (rule 6.2.1.2). E.g. "Reckless" (`handLimit()`) and "Starts on the Bench".
- `CharacterAbilities` (`event/character/`): `canUseMiracle` (the ability, its tokens, and no "The Farm" in play for
  a human), `canUseOncePerTurn`, and `canAffectSkillCheck` (false for a Super Crisis Card's check, rule 6.1.2.5).
- `PlayerOps.useAbility(ability)` records `PlayerFact.AbilityUsed` (public, for the log and the turn's
  `usedAbilities()`) and a `MiracleTokenLost` per token a miracle spends.
- Miracle tokens are a count (`PlayerView.miracleTokens()`, `maxMiracleTokens()`: 1, 3 for Gaius Baltar (alt.)).
  `PlayerOps.gainMiracleToken` gains one only when `canGainMiracleToken()` (below the maximum, not a revealed Cylon,
  rules 4.3.1 to 4.3.3), and `loseMiracleTokens()` discards them all.
- Setup and skill draws: "Disreputable" (`SelectCharacterEventProcessor` draws the Mutiny Card, at setup and for a
  new character), "Coward" (`CreateLoyaltyDeckEventProcessor`), "Sleeper Agent" (`SleeperAgentEvent`), "Industrious",
  "Family Commitments", "Grieving" and "Starts on the Bench" (`ReceiveSkillsStepEventProcessor`, no decision when he
  draws none), "Reckless", "Stranded" and "Nothing But Trouble" (its skill set).
- The "Action:" abilities follow the Mutiny Cards' pattern: a `CharacterActionEvent` (an `ActionEvent`) per ability in
  `event/action/character/`, named after it, with its choices on it. `CharacterActionEventProcessor`'s final
  `isValid()` asks `CharacterActions.canUse` (the ability in effect, its miracle tokens, and the processor's static
  `canUse`), then `isValidUse()`; its final `process()` records the use, then `resolve()`. `CharacterActions` has an
  exhaustive switch over `CharacterAbility` (`canUse`, `eventClass`), and `canUseAny` feeds
  `Actions.canTakeOtherAction`. A Cylon Leader gets them beside his location's action (rule 6.2.1.2), a revealed
  Cylon doesn't (rule 6.1.2.2). `CharacterActionEventsTest` checks one event per `ACTION` ability.
- A choice that depends on what the ability reveals comes after it as concrete options naming the player
  (`SkilledPoliticianChoiceEvent`, `AbusePowerChoiceEvent`, `QuarantineChoiceEvent`, `TreatmentGiveEvent`) or a
  decision (`ReorderTopQuorumCardsEvent`). "Skilled Politician" holds the chosen Quorum Card (`PlayerPile.QUORUM`) and
  plays it through a decision on its event, as "Popular Influence" does; "Abuse Power" holds the chosen Mutiny Card to
  play it, which sends nobody to the "Brig" (`PlayerOps.drawMutinyCardsToChoose`, `holdMutinyCard`).
- Actions at another location ("Organized", "Resolute", `RemoteActions`): `GameFact.RemoteActionAllowed(player,
  location)` lets that player take the location's action from wherever he is, through the location's own decision
  (`TurnView.remoteActionLocation`, which `LocationActionProcessor` honours: only that location then), and
  `EndRemoteActionEvent` ends it. `RemoteActions.locations` lists those he could act at: undamaged, with an action, not
  the "Brig".
- "Blind Jump" queues `JumpingTheFleetEvent(true)`: the Admiral draws 1 Destination Card and the fleet goes there
  (`DrawDestinationCardsEvent(blindJump)`). "Expert Pilot" records its use when Starbuck starts her turn piloting;
  the Action step then adds `GrantEvent(ACTION, EXPERT_PILOT)`. "Meticulous" turns Aaron Doral's return action into a
  choice of `MeticulousEvent`s (the "Resurrection Ship", or another Cylon location and `GrantEvent(ACTION,
  METICULOUS)`).
- The "Movement:" abilities ("Intimate", "Aggressive Tactics", "Glimpse the Face of God") are
  `CharacterMovementEvent`s (`MovementAbilityEvent`s) with `CharacterMovementEventProcessor` and `CharacterMovements`,
  as the actions have; `Actions.offerMovement` asks `CharacterMovements.canUseAny`, Cylon Leaders included (rule
  6.1.3.2). "Aggressive Tactics" grants two actions (`GrantEvent(ACTION, AGGRESSIVE_TACTICS)`), and "Glimpse the Face
  of God" draws from the Destiny deck into the hand (`PlayerOps.drawFromDestinyDeck`), then asks for
  `GlimpseTheFaceOfGodPlaceEvent` (`PlayerOps.placeOnDestinyDeck`, the first card on top).
- The die-roll abilities are answers in the interrupt windows (`RollAbilities`, `UseRollAbilityEvent`, see [Interrupt
  Cards](interrupts.md)): "ECO Officer" (Helo on his turn, once a turn) and "FTL Operator" (Gaeta, once on each "FTL Control" roll)
  reroll in a window after the roll; "Longshot" (Anders on his turn, a miracle) chooses the result and "Hotshot" (Kat
  in her own Action step) discards a card for its strength +2, in a window before it. "Longshot" sends a "Strategic
  Planning" played with it back to the hand; "Hotshot"'s face takes the cards; neither face can be rerolled.
- Moving (`CharacterMoves`): "Clouded" refuses a destination with another character for every move Leoben chooses
  (`MoveEvent`, the "Human Fleet" infiltration, `LeaveBrigEvent`, the return to the "Resurrection Ship"); "Heretic" makes D'Anna pay a Super Crisis Card to
  leave the "Resurrection Ship" (`HereticDiscardEvent` options after the move, `PlayerOps.discardSuperCrisisCard`), and
  `Actions.canMove` is false while she has none.
- The ends of steps: `EndOfMovementStepEvent` offers "Politically Adroit" (`PoliticallyAdroitEvent`: a card to another
  human there, then a `DRAW_EXACTLY_2` decision) and applies "Kleptomania" (a forced discard of 2, or
  `GameFact.SentToBrigAtEndOfTurn`, which the Discard step turns into a `KleptomaniaBrigEvent` after the discards).
  `EndOfActionStepEvent` applies "Stim Junkie": `StartTurnEvent` notes Kat's start (`GameFact.StartLocationNoted`,
  `TurnView.startLocation()`), and she goes to "Sickbay" when she ends her Action step there.
- The Crisis Card abilities, none of which touches a Super Crisis Card (`SkillCheckType.onCrisisCard()`):
  - Drawing: "Religious Visions" (the Crisis step draws 2, `ReligiousVisionsEvent`, nothing to choose between two
    copies), "Delusional Intuition" (`DelusionalIntuitionEvent`, a `DRAW_EXACTLY_1_OF_ANY_COLOR` decision, also after the Gas
    Cloud's draw), "Secret
    Destiny" (`SecretDestinyEvent`: `GameOps.discardRevealedCrisisCard`, then a new card is drawn and revealed), all
    through `CrisisCardReveals`. "Inspirational Leader" counts 1 strength cards positive on a Crisis Card's check
    while William Adama is the current and the acting player (`TotalSkillCheckStrengthEventProcessor`).
  - Choosing: "Amoral", "Choose a Different Path" and "Deceitful" in `CrisisCardFollowups` (see [Crisis Cards](crisis-cards.md#crisis-cards)),
    "Moral Compass" after a section choice (`MoralCompassEvent`, only on another player's choice).
  - "Mysterious Intuition" (`MysteriousIntuitionEvent`, a pass, a fail or nothing) before a Crisis Card's check;
    "Modifications" (`ModificationsEvent`, one per `CylonActivation`) instead of the card's icon at the start of an
    Activate Cylon Ships step, also one without a Crisis Card (`offerWithoutCard`); "Recon" (`ReconEvent`, then `LookAtTopCardEvent`) last in the Discard step.
  - Where the alternative is to go on, the option to go on is the plain event that follows (e.g.
    `one(SecretDestinyEvent, ResolveCrisisCardEvent)`): it names nobody, so the chooser is the ability's player.
- The skill check abilities, none of which affects a Super Crisis Card's check (`canAffectSkillCheck`),
  each with an `offer(game)` that is `NONE` when nobody can use it (see [Skill Check Sequence](skill-checks.md#skill-check-sequence) for where):
  - Cards into a check: `PlayCardsIntoSkillCheckEventProcessor.maxCards` and `canContribute` hold the limits, "Vanity",
    "Calculating" and "Misguided" included, and "Impulsive" (never exactly 1 outside the "Brig"). Only a player who may
    put a card in is asked (`eachPlayerFromTheLeft` with a filter). "Logic-Bound" then asks Simon O'Neill which of his
    cards goes face up (`LogicBoundEvent`, a `CardsShown`).
  - "Fast Learner" (`FastLearnerEvent`: the top 3 of a deck into Dee's hand, then `FastLearnerIntoCheckEvent` or not),
    "Blind Devotion" (`BlindDevotionEvent`, `SkillCheckFact.StrengthZeroed`: the color counts 0 in
    `SkillCheck.cardValue`), "Quick Fix" (`QuickFixEvent`: Cally discards a revealed card on her turn), "Human Delusion"
    (a decision on `HumanDelusionEvent`: cards from her hand face up into their piles, `SkillCheckOps.addRevealedCards`,
    for the strength only, their abilities don't resolve) and "Command Authority" (`CommandAuthorityEvent`:
    `SkillCheckOps.takeRevealedCard` for each revealed card).
  - Location checks: "Insubordinate" (-3 when Starbuck is chosen) and "Cylon Hatred" (`CylonHatredEvent`, -3) at the
    "Admiral's Quarters", "Friends in Low Places" (`FriendsInLowPlacesEvent`, ±2) at the "Brig", through
    `SkillCheckFact.DifficultyAdjusted`, and "Intolerant" (`IntolerantEvent` beside the `SendToBrigEvent` of an
    "Admiral's Quarters" pass of 10 or more, `SkillCheckResultFollowups`).
- The other triggers, queued only in games with their character, each offered only when it can be used:
  - Right after any event that caused it: an op notes the trigger (`PlayerFact.AbilityTriggered`,
    `TurnView.triggeredAbilities()`), and the engine's `AbilityTriggers.check` (after `WinChecks`) queues its offer,
    which clears it (`AbilityTriggerCleared`). It queues one offer at a time: the check after that offer queues the
    next trigger, so no offer is queued twice. `AbstractOps.loseResource` notes "Memento" on a population loss
    (`OfferMementoEvent` offers `MementoEvent`: 3 Piloting cards drawn, one discarded) and "Emotionally Fragile" when
    morale ends at 2 or less (`EmotionallyFragileEvent` queues `ExecuteEvent(n, withoutMoraleLoss = true)`).
  - "For Love": the discards a player is made to do go through `ForcedDiscards` (`DiscardKind`: chosen, random, the
    whole hand), which in a game with Sharon "Athena" Agathon queues a `ForcedDiscardEvent` that asks her
    (`ForLoveEvent`) before the discard happens. `DeceitfulDiscards`, the reveal powers and the Final Five discards use
    it; a cost a player chooses, the hand limit and a Super Crisis Card don't.
  - Where they happen: "Escort" (`EscortEventProcessor.offer` in `DestroyCivilianShipEvent`, unless `escortOffered`),
    "Necessary Steps" (`NecessaryStepsEventProcessor.drawFor` in `DrawMutinyCardEvent` and the "Press Room";
    `DrawMutinyCardEventProcessor.afterDrawing` then applies the "Brig" rules), "Dutiful" (`OfferDutifulEvent` after
    "Command", "Communications" and "Weapons Control", wherever he activated them from, a remote action again from
    elsewhere, never inside "Organized"; `DutifulAgainEvent` offers the action once the discard is done, which may
    have sent him to the "Brig"), "Alert Viper Pilot" (after an unmanned launch from the "Reserves", and when
    "Launch Reserves" places its vipers, before it activates them, `GrantEvent(ACTION, ALERT_VIPER_PILOT)`), "Forward Thinker"
    (`OfferForwardThinkerEvent` after an "Executive Order", a decision on any `LocationEvent`), "Maintenance Engineer"
    (after "Repair", `GrantEvent(ACTION, MAINTENANCE_ENGINEER)`), "Raptor Pilot" (`RaptorPilotEvent` instead of "Launch
    Scout"'s look), "Adaptable" (`OfferAdaptableEvent` after any Quorum Card), "No One Gets Left Behind" (in the jump,
    after the Cylon ships move), "Manipulative" (first in `StartTurnEvent`, `GameFact.TitleBorrowed`, given back by
    `EndTurnEvent`) and "Efficient" (Dee's "Communications" looks at every civilian ship).
- The penalties (`CharacterPenalties`), each adding nothing to a game without its character: "Lies and Secrets" (Cavil
  discards his hand after any "You Are a Cylon" reveal, by action or execution), "Moral Dilemma" (2 cards after each
  Mutiny Card Lee Adama gets, in `DrawMutinyCardEventProcessor.afterDrawing` and the "Press Room"), "Alcoholic" (first
  in `StartTurnEvent`), "Forced to Eject" (`destroyedInsteadOfDamaged`, where a fighter in space is damaged) and
  "Headstrong" (`ForcedDiscards` makes Apollo's chosen discards random, and so does his hand limit).
- The bans (`CharacterBans`): "Emotionally Attached", "Bent on Revenge" and "Convicted Criminal" in
  `LocationEvent.canActIgnoringDamage`, "Specialized" in `SkillCardPlays.canPlay` and `SkillCardEventProcessor`,
  "Spiritual Crisis" in `RevealCylonEventProcessor.canReveal`. "Nothing But Trouble"'s note bans nothing that exists:
  no Treachery Card has an action, their text resolves in skill checks (`CharacterBansTest` pins it).
- The costs (`ActionCosts`): "Terminal Illness" (2 cards before any location), "Conflicted Loyalties" (1 for the
  "Cylon Fleet") and "Reluctant" (1 more for a Skill Card's "Action:"). The player pays first: the processor records
  `GameFact.ActionCostPaid(n)` and queues the discard and the same event again, which then acts and settles it
  (`ActionCostPaid(null)`), or does nothing when the discard took away what it needed. The location cost is a
  `discardExactly(n)` decision, "Reluctant"'s a choice of the other cards, so the card played stays in the hand. An
  action that can't be paid isn't offered.
