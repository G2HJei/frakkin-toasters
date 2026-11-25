# Engine and legal answers

## Engine

`GameEngine` runs a game. `submit(player, event)` answers the pending `PlayerDecisionEvent` with a concrete event, and
`choose(player, option)` picks an option of the pending `OneOf`. Both run the game forward until a player has to act
again, so `status(player)` is always `Waiting(PendingInput)` or `Finished(winner)`. It holds no game rules: processors
decide.
`GameEngine.start(playerCount, randomness)` begins a game with `StartGameEvent` (see [Turn Loop](turn-loop.md)). A package-private
constructor takes any first event, and `GameEngineTest` uses the test-only `ScriptEvent` for it.

- `Agenda` is a stack of `(followup, parentScope)` frames, so followups run depth first. Pushing flattens a followup:
  `None` adds nothing and `AllOf` pushes its children in reverse, so every frame is a `Single` or a `OneOf`.
- The run loop takes the event on top, gets a new processor from `ProcessorRegistry`, `prepare`s it, opens its scope,
  runs it, puts the results of `WinChecks` and `AbilityTriggers` ("Memento") before the followup, closes the scope with
  them all and pushes them. It stops at a
  `PlayerDecisionEvent` or a `OneOf`, and clears the agenda once the game has an outcome. Only that empties the
  agenda: running out of events before the game ends throws `FrakCallTheAdmiralException`.
- The chooser of a `OneOf` is the player of the `PlayerEvent`s in its options, otherwise the current player. That
  works because `PlayerEvent.playerNumber()` is always the acting player: an event that names a target, such as
  `SendToBrigEvent`, is a plain `Event`. Options that name different players are a bug.
- Input check: the right player, `decision.action().isInstance(event)` (so `ActionEvent` takes any location action),
  and a submitted `PlayerEvent` names the submitter.
- Rejected input throws `InvalidActionException` and changes nothing. A queued event that fails validation and a
  decision with constraints its event can't carry throw `FrakCallTheAdmiralException`. Every concrete event class has to
  have a processor, and `ProcessorRegistry.create` asserts it, also without asserts (`ProcessorRegistryTest`).
- Rollback: anything thrown after an input was accepted rolls the call back, and the engine rethrows it as a
  `FrakCallTheAdmiralException`. `advance` notes the journal's size, and on failure `rebuild`s the engine from the
  entries before it. The same input stays pending. `view()` and `journal()` return new objects
  afterwards, so callers ask again.
- `GameEngine.restore(journal)` rebuilds a game from a copy of the journal: `GameReplayer` applies the facts and
  `AgendaReplayer` folds the scope entries through the `Agenda` moves of the run loop (`ScopeOpened` takes the top
  event, `ScopeClosed` pushes its followup, `ChoiceMade` selects the option, and the scope with a `GameEnded` fact
  clears the agenda when it closes). The first event's `ScopeOpened` finds an empty agenda. A journal that doesn't match
  those moves, or ends neither at a pause nor with the end of the game, throws `IllegalArgumentException`.
- Seeds: `GameEngine.start(playerCount, seed)` and `restore(journal, seed)` make a seeded engine, which reseeds its
  `Randomness` (`reseed`) before each input from the seed mixed with the journal's size. So the same seed and inputs
  give the same game, also after a restore. A game started with a `Randomness`, or restored without a seed, gets fresh
  randomness after a restore. `pendingInput()` is the pending input in full, a secret one included, and
  `pendingScope()` the `ScopeOpened` of the event that queued it (the agenda frame's parent, `Journal.scope`).
- Sandbox: `inject(SandboxEvent)` runs a setup event at once, ahead of the pending input, through the same run loop
  (win checks included). `JournalEntry.EventInjected(event)` before its scope tells `AgendaReplayer` to push it, with
  the root as its parent. `submit` never takes a `SandboxEvent`. `undo()` goes back to the pause before the last input:
  the engine knows where each input began (live from `advance`, after a restore from
  `AgendaReplayer.replayWithInputs`: the scope that answered a decision, each `ChoiceMade`, each `EventInjected`), and
  rebuilds from that prefix. A choice that led to a decision is undone on its own; before the first pause there is
  nothing to undo.
- Anytime input: an `AnytimeEvent` (a `PlayerEvent`, e.g. `RevealMotiveCardEvent`) is something a player may do at any
  pause without being asked. `submit` takes one from its own player at any pause, a choice or a secret decision too,
  and runs it ahead of the pending input as `inject` does (`runAhead`, recorded as `EventInjected`), so restore and undo
  treat it as one input. It answers nothing: the pending input stays as it was. A finished game, an event that names
  another player or fails `isValid()` is rejected and changes nothing. `LegalAnswers.anytime(game, n)` lists the ones a
  player may submit now, which the projection carries (see [Projections](projections.md)).
- Only answers: an engine after `takeOnlyAnswers()` (the backend's, `frakkin.take-only-answers`) answers a decision
  whose `LegalAnswers.only()` is present itself, as the run loop reaches it: one concrete answer, or one shape that
  takes every element of its pick (by position, so two equal cards are two ways and he is asked). A search that reported
  a crash or an unsupported class asks. `JournalEntry.OnlyAnswerTaken` comes before the answer's scope, which is logged
  under the decision's player; `AgendaReplayer` counts it as no input, so undo goes back past it. A choice is always
  asked: `one(...)` collapses a single option, so a choice of one is built to ask (`OfferPlayOrPassEvent`, so nobody
  learns that he holds no card). It applies from the next input on: the pause it stands at stays, so a restore
  changes no journal.
- `event/sandbox/` holds the `SandboxEvent` marker and the setup events, plain `Event`s with typed fields (one per kind
  of card): put a Crisis, Destination, Skill or Destiny card on top, give a Skill Card, a Mutiny Card, a Super Crisis
  Card or a Loyalty Card (`GiveLoyaltyCardToPlayerEvent`: from the Loyalty or the "You Are Not a Cylon" deck, or a "You
  Are a Cylon" card nobody has), give the President a Quorum
  Card, move a character, place a ship,
  change a resource, set the jump preparation track, damage a location, assign a title. Their processors use the
  rules' ops where one fits, and `SandboxOps` (`GameOps.sandbox()`) for the rest. `DeckFact.CardsPulled` takes a
  stacked or given card out of its draw pile, so no copy appears.

## Legal answers

`LegalAnswers.of(game, decision)` (`engine/answer/`) lists the answers a pending decision takes, for the player it
asks. It adds no rule: every candidate comes from the game as it is and its processor's `prepare` judges it, as it
judges a client's input.

- `LegalAnswers(events, shapes)`: an event is an answer `submit` takes as it is, with the decision's constraints on it.
  A `ShapedAnswer(example, pick)` has a part to pick: the example is legal, and the `Pick` (`SkillCardPick`,
  `QuorumCardPick`, `ShipPick`) names the component, the elements to pick from by position, `min` and `max`, and
  whether the order counts. A move's discard is a pick of one card. `covers(event)` says whether an event is one of
  them (a list compares as a multiset), `forOption(game, option)` gives the answers of the decision an option of a
  choice asks first (`decisionOf`), `answerTypes(action)` the classes that may answer, and `anytime(game, n)` the
  `AnytimeEvent`s a player may submit now, as the answers of a decision on `AnytimeEvent` itself.
- `AnswerClasses`: the records that may answer an action (not a sandbox event, and an anytime event only for the action
  `AnytimeEvent`), each skipped when the
  check the game asks before it offers one says no (`SkillCardPlays.canPlay`, `QuorumCardPlays`, `MutinyCardPlays`,
  `CharacterActions`, `CharacterMovements`, `LocationEvent.canAct`, `canActIgnoringDamage` with
  `ANYWHERE_EVEN_IF_DAMAGED`).
- `Domains`: the values of a scalar component, by type and name: the asked player for `playerNumber`, every player
  for another one, the ships of the kind the name says (`civilianShipId`, `raiderId`, `basestarId`, `cylonShipId`,
  `viperId`/`fighterId`, `centurionId`; any ship for `shipId`), the cards of the pile an enum comes from (his hand,
  the President's hand while he is President, the turn's Crisis Cards), every constant of any other enum, the
  activations of every fighter in space, and null for every reference. A skill check's cards count once they are
  divided, face up.
- `Shapes`: where a list's elements come from (the hand, the President's hand, the top Quorum Cards for
  "Influential", the ships of one place and kind), or the few list values to try as they are (the color selections of
  a draw, up to the constraint's count; enum lists of up to 2; the tiebreak of the cards before a check). The search
  judges a witness per size (the first and the last elements), so a source puts its elements in the order that makes
  the first ones legal: the strongest first for "Selfish" (`minStrength` 20), a Reckless card last before a check.
  Size 0 is a concrete answer, each run of legal sizes a shape, and a shape that another holds is dropped. A record
  with a list and no source is reported, and `LegalAnswersTest` lists the two that only a choice offers.
- A candidate whose check throws is left out, not a crash of the table; `LegalAnswers.Listener` gets it, and the
  classes the search has no values for. `LegalAnswersTest` checks a decision of each kind (every answer is taken, a
  known one is there and an illegal one is not), `LegalAnswersEngineTest` the scripted turns and an interrupt window.
