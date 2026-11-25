# Events, followups and validation

## Events and Processors

Events are plain records with no logic. `Event` is an empty marker interface. `PlayerEvent` adds `playerNumber()`.
`ActionEvent extends PlayerEvent` marks an action a player takes in the Action step. `LocationEvent extends ActionEvent`
marks the action of a location and names it with `location()`. Its static `canAct` says whether a player can take the
action of a location at all (see "Validation").

A `PlayerEvent` is for events whose player can differ from the current player (an "Executive Order" target, any player
in a
skill check, targets of an effect). An event that always involves the current player is a plain `Event` and reads
`GameView.currentPlayer()`. `GameView.actingPlayer()` is the player taking an action or moving right now: the current
player, unless another one acts during that turn (set it through `GameOps.actingPlayer(n)` and `resetActingPlayer()`).
It never changes who the "current player" is for game effects such as skill checks.

Each `XxxEvent` has an `XxxEventProcessor extends EventProcessor<XxxEvent>` in the same package. Keep that naming:
`ProcessorRegistry` and `EventTestHarness` find the processor by it. A processor runs one event, in two calls:

1. `prepare(GameView, event)` sets the context (`game`, `event`, and helper views `galacticaBoard`, `pegasusBoard`,
   `cylonFleetBoard`, plus `player` when the event is a `PlayerEvent`), calls `init()` (optional setup, marked for
   removal), then `isValid()`, and throws `InvalidActionException` if it returns false. It returns the processor.
2. `run(GameOps)` sets `ops` and calls `process()`, which mutates state through `ops` and returns a `Followup`. It
   throws `FrakCallTheAdmiralException` unless `prepare` accepted the event, and on a second call.

`ops` is only set by `run`, so `init()` and `isValid()` can't change state, and the caller can validate before it opens
the event's journal scope. The caller builds the ops, so a processor never sees the concrete `Game` or the journal.
Processors read through `GameView` and write only through
game ops. Game ops do mechanics, and processors make decisions (targets, rules branching, followups).

Every state change leaves a fact in the journal, see [Journal and Replay](journal.md).

The placeholder event (`event/placeholder/`: `PlayerDecisionEvent`) has no processor. It stands for a point where a
player has to choose. `PlayerDecisionEvent<T>(playerNumber,
Class<T> action, List<EventConstraint>)` means "this player picks a concrete `T` event".

`@Source` (`event/`) names the card or ability an event exists for, where its fields don't: every event of a character
ability (`event/character/`, `event/action/character/`) and every `Offer...Event` has one, which `EventSourcesTest`
checks. On an enum constant it names what the constant stands for, e.g. each `GrantEvent.GrantedBy`. `EventSources.of`
reads it, and the table names it beside the event (see [Frontend](frontend.md)), so a player sees why he is asked.

`FollowupUtils` builds shared followup sequences, e.g. `beforeSkillCheckFollowups(game)`, and holds the helpers that
`SkillCheckResultFollowups` and `CrisisCardFollowups` share, e.g. `lose`, `gain` and `discard`. `playersFrom(game, n)`
lists the players clockwise from player `n`, which is the order of the player numbers.

## Followups

Followups chain events using a recursive sealed interface with four variants:

- `Followup.None` - no further events (singleton `Followup.NONE`)
- `Followup.Single(Event)` - wraps a single event (leaf node)
- `Followup.AllOf(List<Followup>)` - execute all children in order (sequence)
- `Followup.OneOf(Set<Followup>)` - the player picks exactly one (choice). Backed by a `LinkedHashSet`, so order is kept
  and duplicates collapse

Factory methods: `single(event)`, `all(...)`, `one(...)`. They collapse trivial cases: no arguments gives `NONE`, one
argument gives that argument. `all(...)` and `one(...)` take either plain `Event` varargs (wrapped in `Single`) or
nested `Followup` varargs, so you can write "choose between a single event or a sequence of events":

```java
return one(
        single(new NoOpEvent(playerNumber)),
        all(new DecreaseJumpPreparationEvent(), new DecreaseJumpPreparationEvent()));
```

## Event Constraints

`EventConstraint` is a plain enum of extra rules that can be pinned onto an event (e.g. `DRAW_EXACTLY_2`,
`DRAW_EXACTLY_2_OF_ANY_COLOR`, `DISCARD_EXACTLY_4`, `DISCARD_DOWN_TO_HAND_LIMIT`, `DISCARD_DOWN_TO_3`,
`DISCARD_DOWN_TO_1`, `MUST_LAUNCH_IN_VIPER`, `DESTINATION_GALACTICA`), and `drawExactly(n)`, `discardExactly(n)` and
`discardDownTo(1 or 3)` give
the counted ones. A processor attaches them to a
`PlayerDecisionEvent`, e.g. `new PlayerDecisionEvent<>(n, DrawSkillCardsEvent.class, DRAW_EXACTLY_2)`. When the
decision is answered, the engine copies them onto the submitted event with `ConstrainedEvent.withConstraints`,
replacing whatever the client sent, so a client can't drop one.

An event that supports a constraint implements `ConstrainedEvent` and has its own record component for it (e.g.
`DrawSkillCardsEvent.drawLimit`, `null` when there is none, or `LaunchViperEvent.mustLaunchInViper`). Its processor
checks the constraint in `isValid()` and throws `FrakCallTheAdmiralException` for any constraint it doesn't support.
`DrawSkillCardsEvent`, `DiscardSkillCardsEvent`, `DiscardQuorumCardsEvent`, `LeaveBrigEvent`, `LaunchViperEvent` and
the four `AnywhereLocationEvent`s (`ANYWHERE_EVEN_IF_DAMAGED`) implement it.

## Validation

`isValid()` is the only rule check a submitted event gets, so a processor checks everything the client sent: that the
players, ships and cards it names exist and are where they have to be, and the rules of the action. A queued event goes
through the same check, and failing it there is a bug. So a rule that only binds a player's choice can't go in
`isValid()` when the event is also queued directly. The processor that asks offers the legal options as a `one(...)`
instead, e.g. `FollowupUtils.chooseCharacterForSickbay` (rule 9.3.2).

- Location action processors extend `LocationActionProcessor`. Its final `isValid()` checks that the player is at
  `event.location()` and that `LocationEvent.canAct` allows the action (for an "Unorthodox Plan" only
  `canActIgnoringDamage`, see [Actions and Skill Cards](actions.md)), then calls `isValidAction()` for the details of the event,
  and its final `process()` calls `act()`. `LocationEvent.canAct` is an exhaustive switch over `Location`, so a new
  location doesn't compile until it's mapped. A damaged location has no action (rule 11.7.2.2), and a location with
  preconditions asks the static `canAct` of its processor, e.g. FTL Control out of the red zone. The character bans
  and costs come first in `canActIgnoringDamage` (`CharacterBans.canActivate`, `ActionCosts.canPayLocationCost`), so
  they bind every way of activating a location (see [Character Abilities](character-abilities.md)).
- A rule that another processor or a followup needs too is a public static check on its processor, e.g.
  `SendToBrigEventProcessor.canBeSentToBrig`, `MakePresidentEventProcessor.canBecomePresident` or
  `MakeAdmiralEventProcessor.canBecomeAdmiral` (and `MakeCagEventProcessor.canBecomeCag`: not in the "Brig").
- `EventProcessor.isPlayer(n)` checks a player number from the client, and `holdsSkillCards(cards)` the hand. It counts
  copies, because the decks hold equal cards.
- A decision must always have a legal answer, or the game waits forever. A processor queues one only when an answer
  exists, and an optional one goes in a `one(decision, NoOpEvent)`.
- A Skill Card color with no cards in its deck and discard pile can't be drawn (rule 8.2.2.5,
  `DecksHolderView.skillCardsLeft`). `DrawSkillCardsEvent` may draw fewer than its `DRAW_EXACTLY_n` only when no other
  card fits, and fixed-color draws take what is left.
- Clients may send null fields, so a check that would throw on one rejects it instead (the state's `List.copyOf`
  lists throw on `contains(null)` too).
- A followup is built before the events ahead of it run, so a queued event may find its character changed: an event
  that names him tolerates it, e.g. `SendToBrigEvent` leaves a character already in the "Brig" or a revealed Cylon
  where he is, `MakeAdmiralEvent` gives no title to one sent to the "Brig" meanwhile, and `AttackCylonShipResultEvent`
  misses once its fighter or target is gone (queued before the roll, whose cards and abilities can move them). A rule that binds a player's choice goes in the event he answers with, e.g.
  `ChooseCharacterForBrigEvent` (rule 9.2.3, `ChooseCharacterForBrigEventProcessor.offer`). An offer that depends on
  what the effects before it did is an event that builds it when it runs, e.g. `OfferPlayOrPassEvent`,
  `OfferUnmannedViperActivationEvent` (the CAG's activation after "Review Camera Footage" damaged the "Reserves",
  "Command"'s second), `OfferPilotLaunchEvent` ("Return to Duty"), `DutifulAgainEvent` and
  `ResolveCrisisSectionThenEvent` (the second part of a Crisis Card's
  section, from `CrisisCardFollowups.secondPart`).
