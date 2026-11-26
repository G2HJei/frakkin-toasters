# Projections

`projection/` holds what each `Viewer` (`PlayerViewer(n)` or `Observer`) may know. Projections are data, not text:
the frontend owns the wording.

- `GameProjection.of(view, pendingInput, viewer)` is the game. What lies open on the table is public. A hidden card is a
  count (a face down Skill Card doesn't show its color either, in a hand or in a skill check), and a field holding
  cards is `null` for a viewer who may not see them (`SkillHand`, `HiddenCards<T>`). `GameProjector`'s javadoc lists the rules: hands, Loyalty and other
  player cards, the President's Quorum hand (the Quorum Cards in play are public), looked-at Loyalty Cards and civilian
  ship cargo, face down discard piles (`Disclosure.FACEDOWN_DISCARDS`), cards played before a skill check, an open
  interrupt window's cards (the others count them in the hand), the pending input (backend decision 6) with its
  `Topic` (`GameProjection.of(view, pendingInput, pendingScope, viewer)`: the event that queued it, a `SecretEvent` only
  to its actor and its fields only when public, as the log shows the scope) and with its legal
  answers (`Decide.answers`, and `Choose.answers`: an `OptionAnswers` per option that asks the chooser a decision at
  once, e.g. the action of the Action step), what a player may do at any pause (`anytime`: an `AnytimeAnswers` per
  player the viewer acts for who can do something, his own, a sandbox's every player's; empty once the game has
  ended), and everything once the game has ended. The Mutineer card is faceup once held ("immediately reveal this card").
- `LiveUpdateProjection.of(journal, from, viewer)` is what happened: the scopes (`ScopeLog`: actor, event type, its
  fields when public, the facts, and `automatic` for the only answer the engine took, which only its actor sees: that
  it was his only one can tell what he holds) and the choices (`ChoiceLog`). `LiveUpdateProjector` has an exhaustive switch over every
  fact, so a new fact doesn't compile until it has a rule. A fact the viewer may not see becomes `Hidden` (a count;
  the log names the color of a draw by the Skill Card deck it came from). A shuffle never shows its order, a draw only a count. The scope of a `SecretEvent` shows only to its actor.
  Followups never show. It reads the whole journal, e.g. for who was President when a Quorum Card was drawn, and for
  the cards played before the active skill check: the `Shown` of `PlayedBeforeRevealed` names them by who played them
  (`revealed`, from the check's `CardsPlayedBefore` facts), so the log can say what the reveal showed.
- `GameReport.of(journal)` tells a finished game whole, for its results (empty while it goes on): the winner and
  winners, every player's character (and the ones he lost), all his Loyalty and Motive Cards, whether he ended a Cylon
  and the turn he revealed it, his turns and statistics (Skill Cards gained, played into checks with their printed
  strength, played for their text, Crisis Cards on his turns, abilities used, times in the "Brig"), the game's
  statistics (Crisis Cards, skill checks by result, jumps, ships lost by type, vipers and locations damaged, nukes), the
  resources at each turn's start and at the end, and the story (`Moment`s: jumps, Cylon reveals, characters leaving,
  title changes after the setup, Pegasus destroyed, the end). `GameReporter` applies the facts one by one to a game of
  its own, as a restore does, and tallies each with the game as it stood then. Everything is public once the game has
  ended, so it is one report for every viewer; `GameReportTest` plays seeded random games to their end and checks it.
- What a player learns is a fact (backend decision 8): a player who looks at cards he doesn't take records
  `PlayerFact.CardsSeen` (`PlayerOps.see`), which applies nothing, like `LoyaltyCardSeen` and `CivilianShipRevealed`.
- `Disclosure.CARD_COMPONENTS` marks every event component that holds a card public or private, and a test fails until
  a new one is marked. An event shows its fields only when all its cards are public.
- A civilian ship's id says nothing about its cargo: every shuffle of the civilian ship deck deals the ids anew
  (`AbstractOps.shuffle`), and a ship that goes back into the deck is forgotten by whoever looked at it
  (`CivilianShipForgotten`).
