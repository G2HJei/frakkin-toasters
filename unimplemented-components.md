# Unimplemented Components

A review of `core` on `master` at `1f5c88c` (2026-11-20): which game components have no events yet. Updated on
2026-11-21 for `skill-cards-plan.md`, which built the Skill Cards, the Action and Movement steps' offers and the
Communications move, and for `quorum-cards-plan.md`, which built the Quorum Cards and the President's title action.
Only the Kobol objective is in scope; rules that only apply with New Caprica, the Ionian Nebula or Earth are left out.

Card effects are short paraphrases of the images in `materials-sorted/`, not the printed text: read the card before
implementing one. Paths are relative to `core/src/main/java/xyz/zlatanov/frakkintoasters/`. "Unverified" marks what
the review inferred but didn't check against the rules or the physical cards.

## Overview

| Component | Built | Missing |
|---|---|---|
| Crisis Cards (136) | every card resolves | – |
| Destination Cards (21) | every card resolves | – |
| Location actions | every location has its action; Command, Armory, Weapons Control and Communications from anywhere through "Unorthodox Plan" | – |
| Skill Cards (51 types) | every type | – |
| Quorum Cards (23 types, 29 cards) | every card plays, the cards in play act | – |
| Mutiny Cards (22) | drawn, discarded, the Brig rules | none can be played |
| Super Crisis Cards (11) | drawn at the "Resurrection Ship"; the results of the 8 skill check cards | no way to play one; 3 cards have no effects; the Human Prisoner fail |
| "You Are a Cylon" Loyalty Cards (7) | revealed by an execution | the voluntary reveal (rule 6.1.1) and all 7 reveal powers |
| Final Five Loyalty Cards (5) | in the deck | no effect resolves (rule 5.4.1) |
| Personal Goal Loyalty Cards (8) | the end-game penalty | the reveal action (rule 5.3.1) |
| Motive Cards (13) | revealed at the end of the game | revealed during the game (rule 6.2.2.1) |
| Cylon Leaders | Infiltrating at the "Human Fleet", ending it on execution | "return to the Resurrection Ship" as an action (rule 6.2.3.5) |
| Character abilities (99) | 4, and 1 partly | 94 |
| Title Card actions (4) | the President's | the Admiral's and the CAG's |
| Pegasus destruction (rule 11.7.2.4) | – | missing |
| Repairing a location | `LocationRepaired`, through "Repair" | – |

None of the missing components is out of scope as a whole. Only some clauses are: D'Anna Biers' "Hub Destroyed", the
Detention notes on Felix Gaeta's "Coup" and Kat's "Stim Junkie", and rule 9.8.2.1 (Quorum Cards on New Caprica).

## Bugs reachable today

1. **Caprica.** `CapricaPlaySuperCrisisCardEvent` is a `LocationEvent` without a processor. A client can submit it for
   the Action step's `ActionEvent` decision: it fails with `FrakCallTheAdmiralException` instead of being rejected
   with `InvalidActionException`.
2. **Pegasus.** `AttackGalacticaResultEventProcessor` always offers `one(DamageGalacticaEvent, DamagePegasusEvent)`, and
   `PegasusOps.damage()` draws a token and damages its location. Nothing destroys Pegasus once its four locations are
   damaged (rule 11.7.2.4: everyone aboard goes to "Sickbay" and the board leaves the game), and nothing stops offering
   it. Unverified: a fifth Pegasus hit probably fails on the empty damage deck.
3. **Baltar's extra Loyalty Card.** `SleeperAgentPhaseEventProcessor.loyaltyCards` deals 2 cards to everyone in
   `CHARACTERS_WITH_EXTRA_CARDS`, Gaius Baltar included. Rule A3.8.5 gives Baltar the second card only at the start of
   the game; only Boomer gets 2 in the Sleeper Agent phase (rule 6.1.2.2).
4. **Boomer's "Sleeper Agent".** After her 2 cards in the Sleeper Agent phase she moves to the "Brig". The phase
   queues no `SendToBrigEvent` (the execution case in `FinishExecutionEventProcessor` does it).
5. **Latent: revealed Personal Goals.** `EndGameEventProcessor` charges every Personal Goal in
   `loyaltyCards().cards()`, revealed or not. It bites once Personal Goals can be revealed.

`quorum-cards-plan.md` fixed the Quorum Chamber's play, the Quorum deck (29 cards; the second "Authorization of Brutal
Force" is inferred, check the physical deck) and the Quorum hand limit (10 for every President).

## The Action and Movement steps

`Actions` (`event/action/`) builds what the Action step, the Movement step and the grants offer (see "Actions and
Skill Cards" in `CLAUDE.md`): the action of the player's location, the "Action:" of a Skill Card he holds, a pilot's
viper, and in the Movement step a move or a "Movement:" card, in the "Brig" too. Revealed Cylons get only their
location's action. What is still missing plugs in there:

- the "Action:" abilities of character sheets, the Admiral's and the CAG's Title Cards, Mutiny Cards and Loyalty
  Cards, and the Cylon reveal (rule 6.1.1);
- a Cylon Leader's sheet actions instead of his location's (rule 6.2.1.2), and his return to the "Resurrection Ship"
  (rule 6.2.3.5);
- the "Movement:" abilities of character sheets.

The engine accepts any event for which `decision.action().isInstance(event)` holds, so a new `ActionEvent` is accepted
wherever the Action step offers its decision: its processor must reject what its card doesn't allow (revealed Cylons,
rule 6.1.2.3), and `Actions.canTakeAction` must know when it can be taken, so the decision always has an answer.

`GameOps.actingPlayer` is called by `GrantEvent` ("Executive Order", "State of Emergency", the "Hangar Deck"'s extra
action).

## Skill Cards

`skill-cards-plan.md` built every "Action:" and "Movement:" card and "Investigative Committee", and
`quorum-cards-plan.md` built "Popular Influence" once Quorum Cards could be played.

The plan's open questions were settled the way it proposed. These are choices, not rulings: the cards and the
rulebook don't settle them.

- A card is offered and accepted only when its effect can happen, e.g. "Build Nuke" below 3 nukes, "Repair" with
  something damaged.
- "Consolidate Power" may draw Treachery, as "Consult the Hybrid" does.
- A launch from the "Reserves" is the first of "Combat Veteran"'s 3 activations. "Launch Reserves" activates each
  viper once.
- "Unorthodox Plan" works from anywhere.
- "Negotiation" stops whole launches and activations, not placements.
- "Preventative Policy" reduces the next loss of the resource, whatever causes it: civilian ships, damage tokens and
  the FTL Control population loss included.
- An "Executive Order" target may be in the "Brig" (two actions) or in a viper.

## Quorum Cards

`quorum-cards-plan.md` built every card, the cards in play and their uses, and the President's title action (see
"Quorum Cards" in `CLAUDE.md`). Its open questions were settled the way it proposed. These are choices, not rulings:

- "Eulogy" counts any execution earlier in the game that cost morale.
- "Destroy 3 raiders" destroys all of them when fewer are on the board.
- "Resources for Galactica" repairs a Galactica or a Pegasus location.
- "Consult the Oracle" takes the card decks but Loyalty and Motive, and not an empty one.
- "Presidential Pardon" is discarded like the others, and its destination follows `MoveFromBrigEvent`.
- "Accept Prophecy" has no holder and stays in play when the presidency changes.
- Role holders are human players, Infiltrating Cylon Leaders included but not as Vice President. "Probation" may go to
  the President himself.
- "Encourage Mutiny" and "Presidential Order" don't target a character in the "Brig".
- The Arbitrator is asked before "Political Prowess". The Chief of Staff is asked before any card goes into the check,
  the Destiny cards included.
- The Mission Specialist's two other cards go to the bottom in the order drawn.
- "Popular Influence" played by someone who isn't President: "you" means its player, and "Resignation" can only be
  discarded.

Still open: the Cylon reveal (rule 6.1.1) and an Infiltrating Cylon Leader's return (rule 6.2.3.5) will discard the
cards given to the player through `TitleOps.discardQuorumCardsGivenTo`, as an execution does.

## Mutiny Cards

None can be played. A play event, e.g. `PlayMutinyCardEvent(playerNumber, MutinyCard, ...)`, is an `ActionEvent` for
a human or an Infiltrating Cylon Leader (rules 5.2.3.4 and 5.2.3.5), also in the "Brig" (rule 5.2.3.2). Each "can't
play if" becomes a static `canPlay` on its processor. Several cards leave the game: the remove fact above.

| Card | Benefit | Cost | Can't play when | Reuse, or what's missing |
|---|---|---|---|---|
| Armed Resistance | Look at the top Crisis Card, keep it on top or put it on the bottom | The Admiral goes to "Sickbay" | – | `SendToSickbayEvent`, the look-and-place of `HumanFleetLookAtTopCrisisCardEventProcessor` as a shared followup |
| Assume Command | Become Admiral | Discard 5 Skill Cards | fewer than 5 cards, already Admiral, in the "Brig" | `PlayerDecisionEvent<DiscardSkillCardsEvent>(discardExactly(5))`, `MakeAdmiralEvent` |
| Bait and Switch | Draw 2 Skill Cards of any colors | 2 Treachery shuffled into the Destiny deck | – | `DRAW_EXACTLY_2_OF_ANY_COLOR`, `ShuffleTreacheryIntoDestinyDeckEvent` |
| Betrayal of Trust | Look at the top Destination Card, keep it or put it on the bottom | Draw 2 Treachery | – | `PlaceDestinationCardOnTop/AtBottomEvent`, `DrawSkillCardsOfColorEvent` |
| Blackmail | Take 3 random Skill Cards from the President | – | you are President, or he holds 2 or fewer | New: moving Skill Cards between players (no op does) |
| Clipped Wings | Repair all damaged vipers | Vipers in space go back to the Reserves; draw 2 Treachery | no viper in space | `ReturnVipersToReservesEvent`, `ShipOps.repair` for all, `DrawSkillCardsOfColorEvent` |
| Controversial Speech | Roll: on a high result +1 morale and the card leaves the game | Otherwise every player draws 1 Treachery | – | `rollDie`, `GainResourceEvent`, `DrawSkillCardsOfColorEvent` per player |
| Feed the People | +1 food | Jump track -2; the card leaves the game | – | `DecreaseJumpPreparationEvent` x2, `GainResourceEvent` |
| Impeachment | Become President | Discard 5 Skill Cards | fewer than 5 cards, already President | `discardExactly(5)`, `MakePresidentEvent` |
| Make a Deal | Move a character from the "Brig" to a Galactica location | A chosen player draws a Mutiny Card | nobody in the "Brig" | `MoveFromBrigEvent`, `DrawMutinyCardEvent` for a human or an Infiltrating Leader |
| Necessary Risk | +1 food | A basestar and 3 raiders in an area of your choice; the card leaves the game | – | `PlaceShipsEvent` always places in front of Galactica: needs an area. `GalacticaOps.placeBasestar/placeRaider(area)` exist |
| Panic | Activate an unmanned viper if possible | A civilian ship behind Galactica | – | `PlaceShipsEvent`, `one(PlayerDecisionEvent<ActivateViperEvent>, NoOpEvent)` over `canActivateUnmanned` |
| Peaceful Resistance | Roll: on a low result the Admiral goes to the "Brig" | You go to "Sickbay" (stay put in the "Brig", rule A2.3.3.1) | – | `SendToSickbayEvent`, `rollDie`, `SendToBrigEvent` |
| Ruined Reputation | A chosen human draws 2 Skill Cards of any colors | Then roll: on a low result he goes to the "Brig" | – | `DrawSkillCardsEvent` decision, `rollDie`, `SendToBrigEvent` |
| Scavenging for Parts | A civilian ship in space goes back into the civilian ship deck | Damage Galactica | – | `DamageGalacticaEvent`, a new event over `ShipOps.escort()` |
| Selfish Act | Draw 2 Skill Cards | Discard it, then draw another Mutiny Card | – | `DrawSkillCardsEvent`, discard first (it changes the "Brig" count), `DrawMutinyCardEvent` |
| Send a Message | Attack a centurion aboard, +2 to the roll | Damage Galactica | – | `DamageGalacticaEvent`, `ArmoryResultEvent` with a modifier (the Armory itself is a location action) |
| Set the Agenda | The President draws 2 Quorum Cards | You look at his Quorum hand and put 2 on the bottom of the deck | you are President | `DrawQuorumCardEvent` x2, a new event with `CardsSeen` and the cards to the bottom |
| The Strong Survive | Jump track +1 | Draw a civilian ship and destroy it; the card leaves the game | – | `IncreaseJumpPreparationEvent`, `DrawAndDestroyCivilianShipsEvent(1)` |
| Unauthorized Usage | Launch a nuke (at a space area, errata A2.3.3.2) | The card leaves the game and every nuke token is discarded | the Admiral has no nukes | The nuke launch is missing everywhere; `GameOps.discardNuke` per token |
| Violent Protest | Draw 2 Politics | The President goes to "Sickbay" (stays in the "Brig", rule A2.3.3.3) | – | `DrawSkillCardsOfColorEvent`, `SendToSickbayEvent` |
| Weapons Armed | A raptor in the Reserves becomes an assault raptor | Each basestar launches 2 raiders | no raptor in the Reserves | `DestroyRaptorEvent`. Missing: more assault raptors, `LaunchRaidersEvent` with a count. Unverified: `LaunchRaidersEvent` with no basestar places a ship on the Cylon Fleet board, probably wrong here |

Related: `DiscardSkillCardsEventProcessor` has a todo that the one Mutiny Card per turn from discarding Treachery
(rule 10.2.1) isn't tracked.

## Super Crisis Cards

`SuperCrisisCard` is a bare enum: unlike `CrisisCard` it has no `SkillCheckType`, chooser or activation. A player
draws one with the "Resurrection Ship" action (`PlayerOps.drawSuperCrisisCard`), and "Footage Transmitted" would give
one. Nothing plays one: `PlaySuperCrisisCardEvent` and `CapricaPlaySuperCrisisCardEvent` (`//todo follow up with
chosen card's specific event`) have no processor, and `CapricaDrawAndResolveCrisisCardsEventProcessor` only covers the
"draw 2 Crisis Cards" option. A Super Crisis Card is immune to the character abilities that affect Crisis Cards or
skill checks (rule 6.1.2.5).

| Card | Kind | Effect | Mapped? |
|---|---|---|---|
| Bomb on Colonial One | Skill check | Fail: -2 morale, everyone on Colonial One to "Sickbay", the card stays in play and Colonial One is closed | The result, yes. Nothing enforces the closure: `MoveEventProcessor` ignores `crisisCardsInPlay`, and a character starting there needs another start |
| Cylon Intruders | Skill check, partial | Centurions, and on a fail Galactica damage | Yes |
| Fire All Missiles | Skill check, consequence | Fail: 2 civilian ships destroyed; consequence: Galactica damaged twice | Yes |
| Fleet Mobilization | Skill check | Basestars activate and raiders launch, more on a fail | Yes |
| Footage Transmitted | Skill check, partial | Treachery cards, and on a fail Super Crisis Cards for the Cylons | Yes |
| Human Prisoner | Skill check | Fail: the Cylon who played it takes a chosen human's hand, and that human goes to "Sickbay" | No: `HumanPrisonerFailEvent` has no processor, nothing records who played the card, no op moves Skill Cards between players |
| Inbound Nukes | Skill check | Fail: -1 fuel, food and population | Yes |
| The Farm | Skill check, partial | Food and population losses; on a fail it stays in play and the humans lose their miracle abilities | The result, yes. The miracle ban has nothing to block yet |
| Fighting Blind | CAG chooses | Centurions on the boarding track, or the CAG is executed | No: `PlaceCenturionsEvent`, `ExecuteEvent` |
| Psychological Warfare | President chooses | Morale and Treachery for every player, or the Destiny deck replaced by Treachery cards | No: a new event rebuilds the Destiny deck |
| Demand Peace Manifesto | Admiral chooses | Morale and Galactica damage, or the President and the Admiral discard their hands | No: `LoseResourceEvent`, `DamageGalacticaEvent`, `DiscardAllSkillCardsEvent` |

`ChooseCrisisSectionEvent` and `CrisisCardFollowups.choice`/`followup` are keyed on `CrisisCard` and need generalising
for the three choice cards. `KeepCrisisCardInPlayEvent` is keyed on `SkillCheckType`, so it already works for these.

Unverified: the "Resurrection Ship" action draws a card each time, with no limit on how many a player holds.

## Loyalty Cards

### The Cylon reveal

A hidden Cylon only reveals through `ExecuteEventProcessor`. The voluntary reveal (rule 6.1.1) is an action: discard
down to 3 Skill Cards (there is no "down to n" constraint yet), all Mutiny Cards, the miracle token and the in-play
Quorum Cards; lose the titles; go to the "Resurrection Ship" (a piloted viper goes back to the Reserves); draw a Super
Crisis Card (not after an execution); hand the other Loyalty Cards to a human (not from 7 distance on); pass on the
Mutineer card; end the turn. `ExecuteEventProcessor.cylon()` already has several of these steps to share. In the
"Brig" the reveal power can't be used (rule 6.1).

| Card | Reveal power | Reuse |
|---|---|---|
| Can place a centurion | May put a centurion at the start of the boarding track | `PlaceCenturionsEvent(1)` in a `one(..., NoOpEvent)` |
| Damage Galactica | Draw up to 5 Galactica damage tokens and resolve 2 | `GalacticaOps.drawDamage`/`resolveDamage`/`returnDamage`; `ChooseGalacticaDamageEvent` picks 1 of 2, needs 2 of up to 5. Unverified: where the others go |
| Decrease jump track | May move the jump track back 2 | `DecreaseJumpPreparationEvent` x2 |
| Draw Treachery cards | Each human trades a random Skill Card for a Treachery card, then the Cylon draws 2 Treachery | `eachHuman`, `DiscardRandomSkillCardsEvent`, `treachery(p, n)` |
| Reduce morale | May lose 1 morale | `LoseResourceEvent` |
| Send to Brig | May send a character on Galactica to the "Brig" | `PlayerDecisionEvent<SendToBrigEvent>` with an "on Galactica" filter |
| Send to Sickbay | A character on Galactica discards 5 Skill Cards and goes to "Sickbay" | `discard(p, 5)`, `SendToSickbayEvent`, the `chooseCharacterForSickbay` pattern |

### Final Five

Neither trigger exists:
- **A look.** `LookAtLoyaltyCardEventProcessor` has no Final Five hook, and `PlayerOps.lookAtLoyaltyCard` picks the
  card internally and returns nothing. The holder then resolves the card and reshuffles his Loyalty Cards face down,
  which needs a fact (rule 5.4.1.1).
- **An execution.** `ExecuteEventProcessor` has `// todo resolve a Final Five card revealed by the execution (rule
  5.4.1.2)`, and discards the card with the others where the rule removes it from the game. Revealed Cylons are
  immune.

| Card | Effect when another player looks at it | Reuse |
|---|---|---|
| Cylon ships activate | The Cylon ships activate by the card's icons | `ActivateRaidersEvent`, `ActivateCylonBasestarsEvent`, `ActivateHeavyRaidersAndCenturionsEvent` |
| Damage Galactica twice | Galactica is damaged twice | `DamageGalacticaEvent` x2 |
| They are executed | The player who looked is executed | `ExecuteEvent`, guarded by `canBeExecuted` |
| You are executed | The holder is executed and the others discard 2 random Skill Cards | `ExecuteEvent`, `DiscardRandomSkillCardsEvent` |
| You are sent to Brig | The player who looked goes to the "Brig" | `SendToBrigEvent` |

On an execution, each card has its own effect for the other players (they discard Skill Cards); read the cards.

### Personal Goals

The reveal is an action, allowed only while the card's condition holds (rule 5.3.1). It records
`LoyaltyCardRevealed`, and at 6 distance or less adds a "You Are Not a Cylon" card to the Loyalty deck and draws a
Loyalty Card (the path of `FinishExecutionEvent`). The end-game loss is in `LoyaltyCard.personalGoalResource()`.

| Card | Condition | Read from | End-game loss |
|---|---|---|---|
| Acquire Power | The player holds 2 or more titles | `president()`, `admiral()`, `cag()` | food |
| Devastation | The Admiral has no nukes left | `nukes()` | morale |
| Political Intrigue | The President is in the "Brig" | `locate(president())` | food |
| Sacrifice | 6 or more vipers damaged or destroyed | the damaged ships, `removedComponents()` | fuel |
| Selfish | Discard Skill Cards worth 20 strength | a new discard | fuel |
| Self Destruction | The player is in the "Brig" or "Sickbay" | `locate` | morale |
| Stand and Fight | 10 or more raiders on the main board | the Galactica board | population |
| Use Caution | A destination of distance 1 has been reached | `destinations()` | population |

## Motive Cards and Cylon Leaders

- `MotiveCards` holds the requirements, and `EndGame.end` reveals the cards at the end of the game. Revealing one
  during the game, even in the middle of resolving something (rule 6.2.2.1), isn't built; it fits the interrupt
  windows.
- An Infiltrating Cylon Leader can't go back to the "Resurrection Ship" as an action (rule 6.2.3.5: discard the given
  Quorum Cards and the Mutiny Cards, and down to 3 Skill Cards in the "Brig"). Infiltration only ends on execution.
- The Cylon Leaders' character abilities are all missing (see Characters).
- Unverified: D'Anna Biers' "Press Room" setup option (`PlaceInSetupLocationEventProcessor`) puts her on Colonial One
  without Infiltrating. No rule found for it.

## Title Cards

| Title | Action | State |
|---|---|---|
| President | Draw a Quorum Card | Built: `PresidentTitleActionEvent` |
| Admiral | Launch a nuke at a space area (rule 11.6.1: a roll damages or destroys a basestar, on 7 also 3 raiders, on 8 every ship there) | No event; only `DiscardNukeTokenEvent` |
| CAG | Activate an unmanned viper, then give the CAG title to another human | No event |
| CAG | Once per turn, while piloting, activate an unmanned viper and take another action | No event |

`MakeAdmiralEvent` and `MakeCagEvent` check who can take the title (`canBecomeAdmiral`, `canBecomeCag`), and
`SendToBrigEventProcessor` passes the Admiral's and the CAG's titles on. `PlayerOps.moveTo` and `LeaveBrigEventProcessor`
still have todos for the Admiral and CAG in the "Brig" (rule 4.4).

## Characters

99 abilities, three per sheet: 4 built, 1 partly, 94 missing. Kinds: Action, Movement, Miracle (the "once per game"
ability, spends the miracle token, rule 4.3), Miracle action, Passive or triggered, Negative. A Cylon Leader's
abilities are always in effect (rule 6.2.1.2); a revealed Cylon's are ignored (rule 6.1.2.2).

| Character | Ability | Kind | Effect | State |
|---|---|---|---|---|
| Aaron Doral | Industrious | Passive | Infiltrating, draws 2 extra Skill Cards instead of 1 | Missing: `ReceiveSkillsStepEventProcessor` gives every Infiltrating Leader +1 |
| | Meticulous | Miracle | When an action ends his Infiltration, he may go to any Cylon location and take another action | Missing, and the return action itself is missing |
| | Vanity | Negative | Can't put cards into a skill check in another player's Action step | Missing |
| Anastasia "Dee" Dualla | Efficient | Passive | At "Communications", any number of civilian ships | Missing: fixed at 2 |
| | Fast Learner | Miracle | Before a check, the top 3 cards of a Skill deck go into the check or her hand | Missing |
| | Emotionally Fragile | Negative | Executed when morale drops to 2 or less, without the morale loss | Missing |
| Brendan "Hot Dog" Costanza | Memento | Trigger | Once per turn after population drops: draw 3 Piloting, keep 2 | Missing |
| | Escort | Miracle | Destroy an undamaged viper instead of a civilian ship, which goes back into the deck | Missing |
| | Forced to Eject | Negative | His piloted viper is destroyed instead of damaged | Missing |
| Callandra "Cally" Tyrol | Quick Fix | Trigger | On her turn, discard 1 revealed skill check card before the total | Missing |
| | Discharge of a Firearm | Miracle action | Execute another character at her location | Missing |
| | Impulsive | Negative | Puts 0 or at least 2 cards into a check, never 1 | Missing |
| "Caprica" Six | Intimate | Movement | Take a random Skill Card from a player, who draws one from a deck she picks | Missing |
| | Human Delusion | Miracle | After the reveal, put any cards from her hand into the check | Missing |
| | Conflicted Loyalties | Negative | Discards a Skill Card to use the "Cylon Fleet" | Missing |
| Cavil | Primacy | Action | Place (or remove) a basestar, then optionally raiders and a civilian ship | Missing |
| | Aggressive Tactics | Miracle movement | Take two actions | Missing |
| | Lies and Secrets | Negative | Discards his hand when a "You Are a Cylon" card is revealed | Missing |
| "Chief" Galen Tyrol | Maintenance Engineer | Trigger | Another action after playing "Repair", once per turn | Missing, needs "Repair" |
| | Blind Devotion | Miracle | Name a skill type; its cards count 0 in a check | Missing |
| | Reckless | Negative | Hand limit 8 | Built: `Player.handLimit()` |
| D'Anna Biers | Visions | Action | Infiltrating, look at a human's random Loyalty Card, then go back to the "Resurrection Ship" | Missing |
| | Don't Trust Anyone | Miracle action | Draw 2 Super Crisis Cards | Missing; `PlayerOps.drawSuperCrisisCard` exists |
| | Heretic | Negative | Discards a Super Crisis Card to leave the "Resurrection Ship" | Missing ("Hub Destroyed" clause out of scope) |
| Ellen Tigh | Politically Adroit | Trigger | Ending her move with another human: give him a Skill Card, draw 2 | Missing |
| | Manipulative | Miracle | Holds the Admiral or President title for her turn | Missing |
| | Nothing But Trouble | Negative | Treachery in her skill set; humans can't use Treachery text | Built as data (`"1xTr"`); the text ban has nothing to block yet |
| Felix Gaeta | FTL Operator | Trigger | May reroll the "FTL Control" die | Missing |
| | Coup | Miracle action | Take the Admiral title, from the "Brig" via "Command" | Missing |
| | Misguided | Negative | At most 3 cards into a check | Missing |
| Gaius Baltar | Delusional Intuition | Trigger | After drawing a Crisis Card, draw a Skill Card of any type | Missing |
| | Cylon Detector | Miracle action | Look at all of a player's Loyalty Cards | Missing |
| | Coward | Negative | Starts with 2 Loyalty Cards | Built, but he gets 2 in the Sleeper Agent phase too (Bugs, 3) |
| Gaius Baltar (alt.) | Cult Leader | Action | Give or take a miracle token at his location | Missing; `hasMiracleToken` is a boolean |
| | Broadcast | Miracle action | Spend 3 miracle tokens: +2 to a resource | Missing; needs a token count (rule 4.3.1.1) |
| | Spiritual Crisis | Negative | Can only reveal as a Cylon in the "Brig" | Missing; no voluntary reveal yet |
| Helena Cain | Intolerant | Trigger | A strong "Admiral's Quarters" pass may execute instead of the "Brig" | Missing |
| | Blind Jump | Miracle action | Destroy 2 civilian ships and jump, even from the red zone | Missing (todo in `GalacticaOps`) |
| | Bent on Revenge | Negative | Can't use "FTL Control" or the "Engine Room" | Missing (`LocationEvent.canAct`) |
| Karl "Helo" Agathon | ECO Officer | Trigger | Once on his turn, reroll a die | Missing |
| | Moral Compass | Miracle | Change a choice made on a Crisis Card | Missing |
| | Stranded | Negative | Off the board until his 2nd turn | Built |
| Karl "Helo" Agathon (alt.) | Raptor Pilot | Trigger | Better "Launch Scout" | Missing, needs "Launch Scout" |
| | No One Gets Left Behind | Miracle | At a jump, 1 fuel for 2 population | Missing |
| | Family Commitments | Negative | One card fewer when not on Galactica | Missing (todo in `ReceiveSkillsStepEventProcessor`) |
| Kara "Starbuck" Thrace | Expert Pilot | Passive | Starting her turn in a viper, two actions | Missing |
| | Secret Destiny | Miracle | Discard a revealed Crisis Card and draw another | Missing |
| | Insubordinate | Negative | "Admiral's Quarters" is easier against her | Missing (`difficulty`) |
| Laura Roslin | Religious Visions | Passive | Draws 2 Crisis Cards, resolves 1 | Missing (`CrisisStepEventProcessor`) |
| | Skilled Politician | Miracle action | Draw 4 Quorum Cards and resolve 1, also when not President | Missing; the drawn card can be held like "Popular Influence"'s (`PlayerPile.QUORUM`) |
| | Terminal Illness | Negative | Discards 2 Skill Cards to use a location | Missing |
| Lee Adama | Forward Thinker | Trigger | After his "Executive Order", use his own location | Missing, needs "Executive Order" |
| | Choose a Different Path | Miracle | On a Crisis choice, take a "discard 5 Skill Cards" option instead | Missing |
| | Moral Dilemma | Negative | Discards 2 Skill Cards when he draws a Mutiny Card | Missing (`DrawMutinyCardEventProcessor`) |
| Lee "Apollo" Adama | Alert Viper Pilot | Trigger | When a viper launches from the Reserves, pilot it and act | Missing (his setup launch is built) |
| | CAG | Miracle action | Activate up to 6 unmanned vipers | Missing; reuse `ActivateViperEvent` |
| | Headstrong | Negative | His forced discards are random | Missing; `DiscardRandomSkillCardsEvent` exists |
| Leoben Conoy | Glimpse the Face of God | Movement | Draw 2 Destiny cards, put 2 from his hand on the Destiny deck | Missing |
| | Cryptic Message | Miracle action | Swap Skill hands with a player | Missing |
| | Clouded | Negative | Can't move into an occupied location | Missing (`MoveEventProcessor`) |
| Louanne "Kat" Katraine | Hotshot | Trigger | Discard a card for its strength +2 instead of a viper roll | Missing |
| | Sacrifice | Miracle action | Piloting, go to "Sickbay" to destroy ships in her area | Missing |
| | Stim Junkie | Negative | To "Sickbay" if she didn't move on her turn | Missing |
| Louis Hoshi | Dutiful | Trigger | Once per turn, discard to use "Command", "Communications" or "Weapons Control" again | Missing |
| | Organized | Miracle action | Use 3 different locations from anywhere | Missing |
| | Reluctant | Negative | Discards a card to use a Skill Card action | Missing; nothing to block yet |
| Romo Lampkin | Deceitful | Passive | One card fewer on Crisis discards | Missing |
| | Attorney | Miracle action | Free a prisoner and take his hand | Missing |
| | Kleptomania | Negative | Ending a move with another player: discard 2 or go to the "Brig" | Missing |
| Samuel T. Anders | Star Player | Action | Trade any cards for as many from one skill type | Missing |
| | Longshot | Miracle | Choose a die result on his turn | Missing |
| | Starts on the Bench | Negative | Skips his first Receive Skills step | Missing |
| Saul Tigh | Cylon Hatred | Trigger | Lower an "Admiral's Quarters" difficulty by 3 | Missing |
| | Declare Martial Law | Miracle action | The Admiral becomes President | Missing; `CrisisCardFollowups.admiralBecomesPresident` |
| | Alcoholic | Negative | Discards his only Skill Card at the start of any turn | Missing |
| Sharon "Athena" Agathon | For Love | Trigger | Once per turn, draw a Treachery card so another player discards one fewer | Missing (she starts Infiltrating: built) |
| | Resolute | Miracle action | Use any undamaged location | Missing |
| | Grieving | Negative | No Skill Cards in a hazardous location | Missing (todo in `ReceiveSkillsStepEventProcessor`) |
| Sharon "Boomer" Valerii | Recon | Trigger | At the end of her turn, look at the top Crisis Card | Missing |
| | Mysterious Intuition | Miracle | Choose a Crisis skill check's result | Missing |
| | Sleeper Agent | Negative | 2 Loyalty Cards in the Sleeper Agent phase, then the "Brig" | Partly: no "Brig" (Bugs, 4) |
| Sherman "Doc" Cottle | Treatment | Action | Draw 2 from another human's skill set, give him 2 | Missing |
| | Quarantine | Miracle action | Look at the civilian ships and swap one | Missing |
| | Specialized | Negative | Can't use Engineering card actions | Missing; nothing to block yet |
| Simon O'Neill | Calculating | Passive | Puts more cards into checks | Missing (`PlayCardsIntoSkillCheckEventProcessor`) |
| | Modifications | Miracle | Pick the Cylon ships that activate | Missing |
| | Logic-Bound | Negative | Plays one card face up into each check | Missing |
| Tom Zarek | Friends in Low Places | Trigger | Change the "Brig" difficulty by 2 | Missing (the "Administration" half is moot: no check) |
| | Unconventional Tactics | Miracle action | Trade 1 population for another resource | Missing |
| | Convicted Criminal | Negative | Can't use occupied locations but the "Brig" | Missing |
| Tom Zarek (alt.) | Necessary Steps | Trigger | Picks which Mutiny Card a player draws, from 2 | Missing |
| | Abuse Power | Miracle action | Draw 4 Mutiny Cards and play 1, no "Brig" | Missing, needs Mutiny play |
| | Disreputable | Negative | Starts with a Mutiny Card | Missing |
| Tory Foster | Adaptable | Trigger | After a Quorum Card action, draw 2 Skill Cards | Missing; it hooks on `QuorumCardEventProcessor` |
| | Influential | Miracle action | Reorder the top Quorum Cards | Missing |
| | Amoral | Negative | Takes the first option of a "current player chooses" Crisis | Missing (`CrisisCardFollowups.choice`) |
| William Adama | Inspirational Leader | Passive | Strength 1 cards count positive on his Crisis checks | Missing (`countsAsPositive`) |
| | Command Authority | Miracle | Takes the check's cards into his hand | Missing (`EndSkillCheckEventProcessor`) |
| | Emotionally Attached | Negative | Can't use the "Admiral's Quarters" | Missing |

The 94 missing abilities, by where they plug in:

- **The Action step (25):** Primacy, Visions, Cult Leader, Star Player, Treatment, the 18 miracle actions, Expert
  Pilot, Meticulous.
- **The Movement step (3):** Intimate, Aggressive Tactics, Glimpse the Face of God.
- **During something else (33):** die rolls (ECO Officer, FTL Operator, Longshot, Hotshot: the interrupt windows of
  `FollowupUtils.rollDie`), Crisis Cards (Secret Destiny, Moral Compass, Choose a Different Path, Mysterious Intuition,
  Delusional Intuition, Modifications), skill checks (Quick Fix, Human Delusion, Blind Devotion, Fast Learner, Command
  Authority, Intolerant, Cylon Hatred, Friends in Low Places), after an action (Efficient, Dutiful, Maintenance
  Engineer, Forward Thinker, Alert Viper Pilot, Raptor Pilot), other events (Memento, Escort, For Love, Necessary
  Steps, Adaptable, No One Gets Left Behind), one's own turn (Politically Adroit, Manipulative, Recon).
- **Forced penalties (7):** Emotionally Fragile, Forced to Eject, Lies and Secrets, Moral Dilemma, Stim Junkie,
  Kleptomania, Alcoholic.
- **Rule changes (25):** Skill draws (Industrious, Family Commitments, Grieving, Starts on the Bench), cards into a
  check (Vanity, Impulsive, Misguided, Calculating, Logic-Bound), strength and difficulty (Inspirational Leader,
  Insubordinate), location bans and costs (Emotionally Attached, Bent on Revenge, Convicted Criminal, Terminal Illness,
  Conflicted Loyalties), moving (Clouded, Heretic), Crisis handling (Religious Visions, Amoral, Deceitful), discards
  (Headstrong), and bans on actions that don't exist yet (Reluctant, Specialized, Spiritual Crisis).
- **Setup (1):** Disreputable.

## Missing building blocks

What several of the components above need. `quorum-cards-plan.md` built a fact for a card that leaves the game
(`DeckFact.CardsRemovedFromGame`), Quorum Cards in play with a holder, destroying one heavy raider or a chosen
centurion, a destination choice for 3 cards, `MakeCagEvent` and the checks in `MakeAdmiralEvent`. Still missing:

- the other kinds of actions and movement powers in `Actions` (see "The Action and Movement steps");
- the voluntary Cylon reveal;
- playing Super Crisis and Mutiny Cards;
- launching a nuke (gaining one is `GameOps.gainNuke`);
- moving Skill Cards between players;
- `PlaceShipsEvent` in a chosen area, `LaunchRaidersEvent` with a count;
- several miracle tokens per player (rule 4.3.1.1);
- a once-per-turn limit for the Mutiny Card from Treachery (`TurnView.playedSkillCards()` does it for Skill Cards).

## Other todos

- `ExecuteEventProcessor`: which "You Are a Cylon" card is revealed when there are several (it takes the first).
- `DamageVipersEventProcessor`: the player taking an action chooses which vipers (rule 11.3.2.2).
- `IncreaseJumpPreparationEventProcessor`: an "Auto Jump" space instead of resetting by hand.
- `LoyaltyDeckOps`: check the count of plain "You Are Not a Cylon" cards.
- `SkillCheckOps.playFromDestinyDeck`: always 2?
- `ShipOps`: moving ships to the Cylon Fleet board on a jump.

## Suggested order

1. The other kinds of actions and movement powers, in `Actions`. Without them, the character sheets, the Admiral's
   and the CAG's Title Cards and the Mutiny Cards stay unreachable whatever events exist. The Skill and Quorum Cards
   and the President's title action are done.
2. The Cylon reveal: the reveal powers, the Super Crisis Card it hands out, the abilities it triggers.
3. Playing a Super Crisis Card at "Caprica", with the three unmapped cards and the Human Prisoner fail.
4. Mutiny Cards, with the building blocks above; the Admiral's and the CAG's Title Card actions and the nuke launch.
5. Final Five, Personal Goals, Motive Cards during the game, the Cylon Leaders' return action.
6. Character abilities, by hook: the actions, then the triggers (reusing the interrupt windows), the penalties and the
   rule changes.
7. Pegasus destruction and the smaller bugs (Baltar, Boomer, the end-game Personal Goals).
