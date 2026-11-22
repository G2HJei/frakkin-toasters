# Choices

Where the cards and the rulebook are silent or unclear, the build makes a choice. These are choices, not rulings: a
different answer changes only the code named. Only the Kobol objective is in scope.

## Skill and Quorum Cards

- A card is offered and accepted only when its effect can happen, e.g. "Build Nuke" below 3 nukes, "Repair" with
  something damaged.
- "Consolidate Power" may draw Treachery, as "Consult the Hybrid" does.
- A launch from the "Reserves" is the first of "Combat Veteran"'s 3 activations. "Launch Reserves" activates each
  viper once.
- "Unorthodox Plan" works from anywhere. "Negotiation" stops whole launches and activations, not placements.
- "Preventative Policy" reduces the next loss of the resource, whatever causes it.
- An "Executive Order" target may be in the "Brig" (two actions) or in a viper.
- "Eulogy" counts any execution earlier in the game that cost morale. "Destroy 3 raiders" destroys all of them when
  fewer are on the board. "Resources for Galactica" repairs a Galactica or a Pegasus location.
- "Consult the Oracle" takes the card decks but Loyalty and Motive, and not an empty one.
- "Presidential Pardon" is discarded like the others. "Accept Prophecy" has no holder and stays in play when the
  presidency changes.
- Role holders are human players, Infiltrating Cylon Leaders included but not as Vice President. "Probation" may go to
  the President himself. "Encourage Mutiny" and "Presidential Order" don't target a character in the "Brig".
- The Arbitrator is asked before "Political Prowess", the Chief of Staff before any card goes into the check.
- The Mission Specialist's two other cards go to the bottom in the order drawn.
- "Popular Influence" played by someone who isn't President: "you" means its player, and "Resignation" can only be
  discarded.

## Titles and Mutiny Cards

- The nuke follows rule 11.6.1 (a space area; the token is discarded), not the Admiral Title Card's older text. A 7
  with no basestar in the area still destroys up to 3 raiders.
- The CAG's first action needs an unmanned viper to activate: the card says "activate, then give".
- "Weapons Armed" prints no fate; it is discarded like the others. It needs an assault raptor left to build, as the
  components limit. "Unauthorized Usage" discards the nukes (errata A2.3.3.2), so nothing limits "Build Nuke"
  afterwards, and the card's player picks the basestar the nuke hits.
- "Make a Deal" moves the prisoner to any Galactica location, "Sickbay" included: the card says any location on
  Galactica. "Presidential Pardon" and "Attorney" keep to locations that aren't hazardous.
- The Mutiny Card for discarding Treachery is once a turn per player.

## Cylons and Loyalty Cards

- The reveal does rule 6.1.1's step 1 (the discards) before the card's power, because "Draw Treachery Cards" makes its
  player discard down to 3 first; steps 2 to 7 follow the power. The titles pass before the Loyalty Cards and the
  Mutineer card, in the rule's order, on an execution too.
- "Damage Galactica" draws as many tokens as are left, up to 5. "Send to Brig" and "Send to Sickbay" target characters
  on a Galactica location.
- A Cylon Leader who starts on a location of the humans starts Infiltrating, D'Anna Biers at the "Press Room" too, and
  draws 3 starting Skill Cards as Athena does (A3.9 gives the reason: she starts Infiltrating).
- Motive Cards are revealed at a pause: a requirement met only between two pauses can't be caught. Four cards can be
  revealed during the game; the nine that only count once the game is over wait for the end.
- Several Final Five cards seen at once resolve in the order the current player picks. "You Are Executed" leaves the
  game before the execution, so that execution doesn't resolve it again. A look at one's own cards triggers nothing.
- A hidden Cylon may reveal a Personal Goal, a revealed one can't. "Selfish" takes at least 20 strength. "Sacrifice"
  counts the damaged vipers and the destroyed vipers and assault raptors, the Mark VIIs that start damaged included.
- "Each revealed Cylon" on a card includes a Cylon Leader who isn't Infiltrating (rule 6.2.1): "Footage Transmitted"
  and "Psychological Warfare". On "Psychological Warfare"'s first section a Cylon player ignores the discard, the
  negative half (rule 6.1.2.1), and still draws the 2 Treachery. "Footage Transmitted" draws no Super Crisis Card when
  none is left.

## Ships, boards and setup

- The Skill decks follow the strengths Appendix A2.3.8 lists (each color 35 cards, Treachery 26, 201 in all,
  `AllCardsProviderTest`). The set symbols tell the Exodus 0 cards (three copies, "Red Tape" in Politics) from the
  Daybreak ones (two copies). Treachery's twelve 0 cards and eight 3 cards split evenly between their two cards
  ("Bait" and "Dradis Contact" 6 each, "A Better Machine" and "Personal Vices" 4 each), as players' card lists give
  them (https://forums.somethingawful.com/showthread.php?threadid=3765570).
- The Crisis deck has 141 cards (Appendix A2.3.9): the 136 different ones, 4 "Food Shortage" and 3 "Water Shortage"
  with the basestar and the jump icon. The official component lists (70 base, 20 Pegasus, 40 Exodus, 30 Daybreak, less
  the 19 Cylon attack cards) leave the 5 extra copies in the base game, and a sort of the base deck names them
  (https://ffg-forum-archive.entropicdreams.com/topic/6351-complete-card-list). No official list names the copies.
- The Quorum deck has 29 cards: 4 "Inspirational Speech", 2 each of "Authorization of Brutal Force", "Arrest Order" and
  "Food Rationing", one of the others. A4.6.1 names the two "Arrest Order", a play-by-forum deck list the rest
  (https://forum.quartertothree.com/t/battlestar-galactica-28-i-swear-this-has-happened-before/139020), and an FFG forum
  thread the second "Authorization of Brutal Force"
  (https://ffg-forum-archive.entropicdreams.com/topic/9774-possible-loophole-with-recycling-quorum-cards).
- The jump track has no constant for its "Auto Jump" space: moving past `MINUS_1` jumps the fleet at once and puts the
  token back at the start, which is what the space does (rule 12.1, `IncreaseJumpPreparationEventProcessor`).
- The Pursuit track's spaces are in the order the board prints them (`board.layout.json`, measured from the image):
  start, one civilian ship, an empty space, two civilian ships, "Auto Attack".
- Every effect that damages Galactica offers Pegasus while it is in play (rule 11.7.2), not only attacks.
- Damaged vipers are picked one at a time by the player taking an action, the current player otherwise; an assault
  raptor counts as a viper and is destroyed.
- The Loyalty deck has the 18 plain "You Are Not a Cylon" cards of the Appendix's component list.
- Raiders activate one space area at a time in the current player's order; basestars and heavy raiders clockwise,
  where the order can't change the outcome.
- "Psychological Warfare" rebuilds the Destiny deck from as many Treachery cards as are left, up to 6.

## Character abilities

- An ability that answers something is offered only to a player who can use it, so a game without its character
  pauses where it did. The negative abilities don't bind a Super Crisis Card's check either.
- Die rolls: one reroll a window; "Longshot" fixes the result (a "Strategic Planning" played with it goes back to the
  hand); "Hotshot"'s face takes the cards but can't be rerolled.
- "Resolute" takes any undamaged location with an action, Cylon locations included; "Organized" 3 different ones, no
  Cylon location, never the "Brig". "Treatment" gives its 2 cards as fixed pairs, of colors in the target's skill set.
- "Blind Jump": the Admiral draws 1 Destination Card and the Mission Specialist keeps his card.
- "Moral Dilemma"'s 2 cards come after what the Mutiny Card brings, the "Brig" and the Mutiny Card discards
  (`DrawMutinyCardEventProcessor.afterDrawing`, the "Press Room"): a "Bait" among them draws another Mutiny Card, which
  then finds them done.
- "Human Delusion"'s cards count for the strength, but their abilities don't resolve. "Logic-Bound" isn't optional.
  "Vanity", "Impulsive", "Misguided" and "Headstrong" don't bind a Super Crisis Card's check (rule 6.1.2.5).
- "Moral Compass" and "Choose a Different Path" act on the choice of a section, not on the targets a card asks for.
  "Inspirational Leader" is for the player who drew the Crisis Card in his Crisis step; "Delusional Intuition" follows
  every Crisis Card Baltar draws, the Gas Cloud's too, once for the draw.
- "Deceitful" reduces Romo Lampkin's first discard of a card's effects, "Choose a Different Path"'s discard included
  (it is the card's result); a whole hand becomes "keep 1".
- "For Love" doesn't reduce costs a player chooses, the hand limit, an execution's hand, the reveal's discard down to
  3 or a Super Crisis Card; it does reduce "Kleptomania". "Escort" doesn't act on a nuke's 8, but does on Kat's
  "Sacrifice". "Headstrong" applies at the hand limit too.
- "Resolute" can't take the "Hangar Deck" from the "Brig", nor Infiltrate again (errata A2.1.1.1). "Visions" waits while
  "Detector Sabotage" is in play, and from the "Brig" ends with the discard down to 3. "Primacy" places its basestar from
  the Cylon Fleet board when the supply is out (rule 11.4.2), and may take one off the main board only then.
- "Dutiful" follows an activation from anywhere, but not inside "Organized", which never takes a location twice.
  "Modifications" is asked in every Activate Cylon Ships step, one without a Crisis Card too. "Alert Viper Pilot" is
  offered when "Launch Reserves" places its vipers, before it activates them. "Clouded" forbids the return to an
  occupied "Resurrection Ship". "Efficient" always shows every civilian ship.
- "Kleptomania"'s "Brig" comes at the end of the turn, after the discards. "Manipulative" gives the title back to the
  next in line when its previous holder can't hold it.
- "Nothing But Trouble"'s note bans nothing: no Treachery Card has an action.
- A cost ("Terminal Illness", "Conflicted Loyalties", "Reluctant") is paid before the action, which then runs; the log
  shows the event twice.


## Out of scope

New Caprica, the Ionian Nebula and Earth as a whole (see `CLAUDE.md`). Some clauses of in-scope cards are too: D'Anna
Biers' "Hub Destroyed", the Detention notes on Felix Gaeta's "Coup" and Kat's "Stim Junkie", and rule 9.8.2.1.
