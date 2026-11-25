# Skill checks

## Skill Check Sequence

Each step of `skill-check.drawio` is an event, and each step's processor queues the next one, so a step decides from
the state at that moment what follows.

- A location action or a Crisis Card (`ResolveCrisisCardEventProcessor`, `ChooseCrisisSectionEventProcessor`) calls
  `ops.skillCheck().start(type)` and returns
  `FollowupUtils.beforeSkillCheckFollowups(game)`: "Mysterious Intuition" for a Crisis Card's check when Boomer can
  use it, a "Political Prowess" offer per player for a location check, then `MakeSkillCheckEvent`. That skips the
  check when one of them decided it, and otherwise asks for the
  cards played before the check, from the player to the left of the current player (`eachPlayerFromTheLeft`).
- "Political Prowess", "Change of Plans" and "Declare Emergency" are offered to every player in turn
  (`FollowupUtils.playOrPassDecisions`), each through an `OfferPlayOrPassEvent(n, card)` that builds his choice when it
  runs, so a card another player played first counts: the play or the pass while he holds the card and the check allows
  it (`OfferPlayOrPassEventProcessor.canPlay`), otherwise the pass alone. That one option is a `OneOf` built directly, so
  he is still asked and the table doesn't learn who holds the card.
- `RevealCardsBeforeSkillCheckEvent` (and a tiebreak), `ResolveCardsBeforeSkillCheckEvent` (Support the People, the
  Chief of Staff, "Fast Learner", the Destiny cards, the cards played into the check, then "Blind Devotion"),
  `ShuffleAndDivideCardsEvent` ("Quick Fix" and "Human Delusion" once the cards are revealed, the abilities unless
  "Restore Order" was played, the "consequence" when a card with the ability icon was played in, a Reckless check's
  Treachery cards), `TotalSkillCheckStrengthEvent` (Declare Emergency), `DetermineSkillCheckResultEvent`, then the
  result, `OfferCommandAuthorityEvent` (queued only when someone has "Command Authority" and its token), and
  `EndSkillCheckEvent` last. The character abilities in a check are under [Character Abilities](character-abilities.md).
- A card put into the check goes face down onto its pile of cards, even an empty one ("Guts and Initiative" skips the
  Destiny cards), until `ShuffleAndDivideCardsEvent` records `SkillCheckFact.CardsDivided`
  (`SkillCheckView.cardsDivided()`). A card added after that goes face up onto its pile, e.g. "Force Their Hand".
- The current player resolves the abilities one at a time, in his order (rule 10.4.2).
  `DetermineSkillCheckAbilitiesOrderEvent` offers the ones left as a `OneOf` of `ResolveSkillCheckAbilityEvent`s and
  queues itself after the pick, so an ability whose card was removed first doesn't resolve. Only the cards from the
  shuffle count, and Trust Instincts sends the cards it adds through the same loop. `ResolveSkillCheckAbilityEvent`
  records `AbilityResolved`, once per check. Iron Will, Install Upgrades, All Hands on Deck and Establish Network act
  through that record.
- "Investigative Committee" is played before the check. `SkillCheckView.investigated()` is true once the revealed
  cards played before it hold one (they stay with the check until it ends). `PlayCardsIntoSkillCheckEventProcessor`
  then records `SkillCheckFact.CardsShown(playerNumber, cards)` after the cards go into the pile like any other, so
  everyone learns who played what (`SkillCheckProjection.shownCards`, the turn panel's "Face up" rows). The Destiny
  cards are dealt face down before anyone plays.
- `EndSkillCheckEvent` runs after everything the result queued: it discards the cards of the check, the ones played
  before it too, and records `SkillCheckEnded`, which clears `GameView.activeSkillCheck()`.
- `DetermineSkillCheckResultEventProcessor.difficulty(game)` is the difficulty so far: Declare Emergency, Jury Rigged,
  4 less at the "Airlock" on a character in the "Brig", and what the Quorum Cards in play changed
  (`SkillCheckView.difficultyAdjustment()`, `SkillCheckFact.DifficultyAdjusted`).
- The Quorum Cards in play act in a check (see [Quorum Cards](quorum-and-mutiny-cards.md#quorum-cards)): "Accept Prophecy" and the Arbitrator
  (`UseArbitratorEvent`) as the "Admiral's Quarters" is activated, the Chief of Staff (`UseChiefOfStaffEvent`,
  `SkillCheckView.politicsCountPositive()`) before any card goes in, and "Probation" (`UseProbationEvent`, a
  `CardsLookedAt` only the President sees) after its holder put cards in, which are then the top of the pile.
- The game board skill checks are the "Admiral's Quarters", the "Brig" and the "Airlock" (rule 10.3.3). The Daybreak
  "Administration" has no skill check: it draws a Mutiny Card, and `ReplaceMutinousPresidentEvent` then lets the
  player choose a new President if the President holds one. With "Accept Prophecy" in play the President is first
  asked whether to discard it and keep the title (`DefendPresidencyEvent`), and while a Vice President isn't President
  he takes the title without a choice. `SkillCheckView.targetPlayerNumber()` is the character the location check is
  about, the player himself at the "Brig".

## Skill Check Results

`DetermineSkillCheckResultEventProcessor` queues a `ResolveSkillCheckResultEvent` after the Change of Plans decisions.
Its processor returns `NONE` when Change of Plans replaced the pass result or Iron Will prevented the fail result.
Otherwise it asks `SkillCheckResultFollowups` (in `event/skillcheck`) for the pass, partial pass or fail followup of
the active `SkillCheckType`, as printed on the card. The helper resolves the players (current player, President,
Admiral, the character chosen by a location action via `SkillCheckView.targetPlayerNumber()`) and board state at that
moment. Its pass and fail switches list every type, so a new `SkillCheckType` doesn't compile until it's mapped.

- An effect shared by 2 or more skill checks uses a generic event: an existing one when it fits (e.g.
  `DamageGalacticaEvent`, `SendToSickbayEvent`), otherwise one in its area package (e.g. `LoseResourceEvent`).
- An effect unique to one skill check uses a specialized event in `event/skillcheck/result`, named after the skill
  check and result (e.g. `CrashLandingFailEvent`), with the card text in its comment.
- A revealed Cylon ignores the penalties (rule 6.1.2.1). The current player is one when he resolves a Crisis Card at
  "Caprica", so his penalties go through `onCurrentPlayer` and `sendTo`, and "each player" ones through `eachHuman`. A
  result event that targets him checks it too, e.g. `DefendingAPrisonerFailResultEventProcessor` queues no execution.
