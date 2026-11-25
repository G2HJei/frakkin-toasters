# Quorum and Mutiny Cards

## Quorum Cards

Every Quorum Card can be played. They follow the Skill Cards' pattern, with the card's choices
on its event.

- A play is a `QuorumCardEvent` (an `ActionEvent`), one record per card in `action/quorum/`. The record is the card: it
  has `playerNumber` and the card's choices, no card component, and its processor's `card()` names it.
  `QuorumCardEventProcessor`'s final `isValid()` checks that the player is human and holds the card (his own held card
  first, otherwise the President's hand), then `isValidPlay()`. Its final `process()` takes the card out of where it
  was, then `resolve()`.
- `QuorumCardPlays.canPlay` is an exhaustive switch over `QuorumCard` that asks each processor's static `canPlay`: a
  card is offered only when its effect can happen. `canPlayAny` covers the President's hand, and `eventClass(card)`
  gives the event of a card, for a decision that takes only that card.
- Who plays: the President, as his action (rule 8.4) from his hand, also from the "Brig" and as a pilot; the President
  at the Quorum Chamber, which draws a card and then offers `one(DrawQuorumCardEvent, decision<QuorumCardEvent>,
  NoOpEvent)`; and the player of "Popular Influence" (see [Actions and Skill Cards](actions.md)).
- `PresidentTitleActionEvent` (`action/title/`) is the President Title Card's action: draw a Quorum Card. The
  Admiral's and the CAG's are beside it (see [Combat](combat.md)).
- A played card is in no pile while it resolves and goes where its text says, usually last: the discard pile
  (`DiscardPlayedQuorumCardEvent`, face up), out of the game (`DeckFact.CardsRemovedFromGame`, into
  `GameView.removedComponents()`), or in play. A card whose fate depends on a roll settles it in its result event.
- The deck holds 29 cards (`DecksHolder`): the second "Authorization of Brutal Force" is inferred, check the physical
  deck. The hand limit is `TitleOps.QUORUM_HAND_LIMIT` (10) for every President. `DecksHolderView.quorumCardsLeft()`
  counts the draw and discard piles; nothing is drawn when both are empty.
- In play (`GameView.quorumCardsInPlay()`, `QuorumCardPutInPlay`, `QuorumCardLeftPlay`, public): the four "Assign"
  cards and "Probation" with their holder, "Accept Prophecy" without one. A card used up leaves play
  (`TitleOps.discardFromPlay`), and the cards given to a player go on execution and at the end of an Infiltration
  (`TitleOps.discardQuorumCardsGivenTo`).
- Where they act: the Arbitrator and "Accept Prophecy" at the "Admiral's Quarters", the Chief of Staff and "Probation"
  in skill checks (see [Skill Check Sequence](skill-checks.md#skill-check-sequence)), the Mission Specialist at the jump and the Vice President and "Accept
  Prophecy" at the "Administration" (see [Jump and Kobol](jump-and-endgame.md#jump-and-kobol) and [Skill Check Sequence](skill-checks.md#skill-check-sequence)).
- The sandbox gives the President a card with `GiveQuorumCardToPresidentEvent`. The wire schema names each Quorum
  event's `quorumCard`, by which the answers and the log name the card played. The mats show the cards in play face up
  in front of their holder.

## Mutiny Cards

- To draw a Mutiny Card, queue a `DrawMutinyCardEvent`. Its processor sends the player to the "Brig" on the second card
  (the Mutineer on the third, `PlayerView.isMutineer()`), and asks for a `Discard1MutinyCardEvent` per card over the
  limit (rules 5.2.3.2 and 5.2.3.3). In the "Brig" and off the board (a Stranded Helo) the player only discards. Only
  the Press Room calls `PlayerOps.drawMutinyCard()`, because its target doesn't move. Discarding a "Bait" or a "Dradis
  Contact" by choice draws one once a turn per player (`TurnLimit.MUTINY_CARD_FROM_TREACHERY`, rule A2.3.8.1): every
  discard of cards the player picked asks `TreacheryDiscards.mutinyCardFor` (`DiscardSkillCardsEvent`, moving between
  ships, the "Engine Room", "Star Player", "Selfish", "Hotshot"), not a random one or the whole hand. A new character
  who keeps the Mutineer card after an execution draws one too (`FinishExecutionEvent`, rule 5.2.1).
- Every Mutiny Card is played as an action (rule 5.2.3.1), following the Quorum Cards' pattern: a
  `MutinyCardEvent` (an `ActionEvent`), one record per card in `action/mutiny/` with the card's choices on it, and a
  processor extending `MutinyCardEventProcessor`, whose final `isValid()` checks a human (an Infiltrating Cylon Leader
  counts, rules 5.2.3.4 and 5.2.3.5) holding the card, also in the "Brig", then `isValidPlay()`. Its final `process()`
  takes the card out of the hand (`PlayerOps.playMutinyCard`), then `resolve()` ends with its fate:
  `DiscardPlayedMutinyCardEvent` (face up) or `RemoveMutinyCardFromGameEvent`. `MutinyCardPlays.canPlay` is an
  exhaustive switch over `MutinyCard` asking each processor's static `canPlay` (the card's "can't play" line), and
  `MutinyCardEventsTest` checks one event per card.
- What they share: `PlayerOps.giveSkillCards` and `takeRandomSkillCards` ("Blackmail"), `PlaceShipsEvent`'s
  `cylonArea` ("Necessary Risk"), `LaunchRaidersEvent.fromEachBasestar(n)` (unlike the icon, nothing goes to the Cylon
  Fleet board without a basestar, rule 9.6), `ActivateUnmannedViperEvent` (a decision's answer: an unmanned viper even
  for a pilot, "Panic"), `ShuffleCivilianShipIntoDeckEvent`, `LookAtTopCardEvent` (also the "Human Fleet"'s look),
  `NukeResultEvent` ("Unauthorized Usage" discards every nuke, errata A2.3.3.2) and `TitleOps.putQuorumCardsAtBottom`
  ("Set the Agenda", after a `CardsSeen` of the President's hand). The sandbox deals one with
  `GiveMutinyCardToPlayerEvent`.
