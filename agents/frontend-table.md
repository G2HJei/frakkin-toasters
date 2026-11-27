# Frontend: the table

The game page (`TablePage`) and what it draws. The rest of the frontend is in [frontend.md](frontend.md).

## The table

- Layout: no bar over it, the board in the middle as high as the screen (`.table-main` is a size container, the
  board's width follows its height), and a column on each side that scrolls on its own. Under 1100px the board goes
  across the width and the panels under it. Where the board leaves 1180px beside it (`useWide` in `TablePage`: the
  screen's width less the board's, which no media query can say), the log gets a column of its own left of the turn
  and the chat shows under the turn, without the switch (`.table-main.wide`); its columns stop growing at 560px (log),
  520px (turn) and 440px (mats), and the rest goes into margins outside them. The styles are the "table" section of
  `styles.css`: the board's palette, cut corners (`.cf`, `--chamfer`) and the printed-label look.
- Left column: one panel for the game, the turn and the decision, the "At any time" panel while there is something to
  do, then the log. The panel's top is `TurnPanel`: its tab names the game, with the connection beside it while it isn't
  open; one row with the turn's number ("T4") and the current character in a banner (70%), red while he acts, otherwise
  amber (40%) with a red one of only the acting character's name beside it (30%; the titles and the heading's
  `aria-label` say which is which), and the steps in the last 30% (only the step the turn is at is lit, named where the
  steps have 16rem, a container query); the skill check under the turn's Crisis Card. Its bottom is the decision
  (`InputPanel`) and its prompts. The two are separate boxes drawn as one (`.turn-input` is `display: contents`, each box
  cut at its own corners), so each sizes itself in the column, and the whole panel's edge is red while the viewer is
  asked. The decision takes the height its answers need while the log keeps 15vh, and scrolls
  inside its body past that (under its heading and pause line, down to 12rem); the log takes the rest and scrolls inside
  its body. The left column never scrolls as a whole, and the board's "your move" glow is clipped so the page never
  does. No panel has a title.
- Right column: the sandbox's tools in a sandbox, or the seats of a standard game (`table/session/SessionPanel`, open
  while a seat is vacant or a vote is open: each seat's character and user, "Take this seat" for whoever holds none,
  votes to hand a seat over or to abandon with their tally, the host's "Hand over", "Ban" and "Abandon the game", which
  pass at once, "Leave my seat", "Leave the table"), above the players' mats (`PlayersPanel`, in one column, under the
  board as many as fit; the viewer's own first, then the others in turn order from his): title badges with the
  insignia cut out of the card (`table/players/icons/`, the card as their preview), the nukes beside the Admiral, and
  right of the portrait a strip with a cell per card of the sheet's skill set in its colors (split along the diagonal for
  "either"), for everyone but a revealed Cylon, who draws 2 of any colors. Another player's hand is one Skill Card back
  with how many (`SkillCardBack`), like the face down cards of a skill check. When the viewer looked at one of a
  player's Loyalty Cards, that player's Loyalty pile lies in a halo in the green blue of what only the viewer knows
  (`--known`; `knowingPlayer`: the viewer, the asked player in a sandbox), and hovering it lists "Loyalty Cards looked
  at": every look, whether he still holds the card or not, never which of his face down cards it was. A sandbox keeps
  each player's looks out of sight until hovered (`Learned`).
- A player is named by his character everywhere (`playerName`), "Player n" before he has one; the user in the seat shows
  only on his mat (`userName`, from the room's seats), with a dot for whether he is connected, "away" when he isn't and
  "vacant seat" when nobody holds it (`seatState`, `SeatPresence`). The input panel says so under the pause when the
  game waits for such a seat.

## The log

`table/log/LogPanel.tsx`, the story in `table/log/story.ts`.

- Every turn stays open (named with its current player), grouped by step (`StepChanged`; a step without a line has no
  header). Each scope is a line: a player's line starts with his token and name, every line again, its words on the
  same row; an event about no player (no `playerNumber`) has neither.
- The choice of a character names the user in the seat ("User123 chooses William Adama", `namesUser`, "Player n" in a
  sandbox). Another player the words name has his portrait in front of his name and his sheet on hover, in the color of
  the words; the line's own player named again has no portrait (`withoutOwnPortrait`), but a title he holds keeps it
  ("makes the [Saul Tigh] Admiral President").
- The holder of a title is the one at the time of the line: the story follows the `TitleFact.*Assigned` facts
  (`TITLES_ASSIGNED`), and before the first it takes the table's titles.
- A choice is told by the event it picked, under the player who picked it (a tiebreak nobody was asked about under the
  current player, `KEPT_BY_CURRENT_PLAYER`), a pass by what it passed on ("doesn't move"). Left out: the passes on the
  cards every player is asked about, the scopes that only ask, an event about no player that changed nothing, the
  secret answers to an interrupt window (`SECRET_ANSWERS`). An event whose facts say everything (the steps, a skill
  check's mechanics, a roll, a result) is told by them, each under the player it is about. The reveal of the cards
  played before a skill check names them by who played them (the fact's `revealed`), and the close of an interrupt
  window tells each card and ability as its player's play ("plays Strategic Planning 4, changing the roll by +2",
  "plays ..., which goes back to the hand", "uses FTL Operator", also when it waits for the next roll), a card a
  tiebreak sends back on its own line.
- An only answer the server took has the answers' pass icon in white in front ("automatic" on hover); a tag names the
  card or ability a line comes from when its words don't. The images after the words leave out what the words show
  already and the token that leads the line. A ship the table no longer shows (a raider back in the supply) is named by
  the type a typed field of the log gave it (`raiderShipId`, ...).
- Facts stay hidden, but a sandbox shows them under each line with the "Facts" switch (floating over the log's top
  right, in the place of the chat's switch): the ones that change the table first (`isStoryFact`; a ship destroyed, damaged or launched counts), without
  the ones the words say already. What the fleet gains or loses is never only under a line: it gets a line of its own
  after it ("is executed", then "The fleet loses 1 morale"), as an interrupt card a tiebreak sends back does.
- A click on a line lights what it names on the board for a moment (`table/flash.ts`, `entryTargets`, `useFlash`). The
  log follows the newest entry until the user scrolls up (`useFollowBottom`: only a scroll up leaves the end, since a
  scroll event arrives a frame late); the arrow at its bottom right follows again, amber while new entries came below.

## The chat

The "Chat" switch floats over the top right of the log and of the chat, where a sandbox has its "Facts" switch
(`ChatToggle` in `TablePage`): it puts the chat in the log's place and reads "Logs" to go back, in the same look either
way (unlike "Facts", it is no switch that lights up), and it counts what came since the chat was last open. A wide
screen has no switch: the chat stays open under the turn, the log in its own column. `table/chat/ChatPanel.tsx` takes
the log's place and sizing (its box takes the focus only when the switch opened it): one chat for everyone at the
table; a message shows its writer's portrait (the character of the seat he held), his name, the character or
"observer", and the time. The box is one line high, without a button: Enter sends (500 characters at most),
Shift+Enter breaks the line; whoever only watches a
finished game reads it with a note. A refusal (too long, too fast) shows under it.

## Catching up

- Last seen (`table/lastSeen.ts`, per game and user in `localStorage`, guarded; `useTalk` in `TablePage`): the log's
  red line goes above the first entry after the viewer's last decision (it moves with the game that follows his next
  one; a click on it marks all seen; a first visit has seen all), the chat's under the last message he had seen when
  he opened it (all is seen while it is open). A sandbox has neither.
- "Your move" (`table/notify.tsx`): while the tab is hidden and the viewer becomes asked, a browser notification (once
  per move, if allowed: the turn panel's "Notify me" asks on a click, never on load) and the tab's title flashing until
  the tab shows; the favicon marks a move too. Not in a sandbox.

## Report a problem

"Report a problem" under the turn panel's "Reference" (`table/problem/ProblemDrawer.tsx`) opens a small drawer at the
bottom right: a box of 1000 characters at most, sent to `POST /api/games/{id}/problems`, then a thank-you.

## Results

`table/results/ResultsDialog.tsx` lies over the table once the game has ended (the `report` in `gameStore`) or was
abandoned, and opens again from "Results" beside the ending in the input panel (or in the seats panel): the ending and
its summary, the winners by every user who held their seat (`seatHistory`: "Gaius, then Dee"), a row per player (his
character and the ones before, the users, Cylon or human and when revealed, his Loyalty and Motive Cards, turns and
statistics), the game's statistics as tiles, the resources over the turns (`ResourcesChart`: four lines on one axis in
the dataviz palette's validated dark slots, a legend, the names at the line ends moved apart, a crosshair with every
value of the turn, the numbers "As a table") and the story. An abandoned game names only who held each seat. Its
actions: copy the link (the table's URL: anyone logged in watches a finished game), the host's "Rematch with the same
seats" and everyone's "Go to the rematch". The words are in `labels/results.ts`.

## Animations

What changed with the last game message plays once (`table/changes.ts`, `changesBetween` of `gameStore.previous` and
the game, keyed by the version): a floating delta at a resource, the distance and the Admiral's nukes; a track marker
fading out of its old space and in on the new one; a newly damaged location pulsing red; the "Critical damage" plate
flickering on; a resource's shield fading in with the "Preventive Policy" that raised it and out with the loss it took;
a ship gliding from its old place (FLIP on `data-ship`); the die tumbling; new cards sliding into a hand. A snapshot (a
reconnect, an undo: `expectSnapshot`) leaves no previous game, so it animates nothing.

## The board

`src/table/board/`: `board.jpg` is the surface, and `BoardSvg` lays the components on its printed slots, in the image's
pixels. `board.layout.json`, measured with the image, holds what is printed; `layout.ts` reads it (a place for every
`Location`, which a test checks, the title panels, the space areas' sectors (`SPACE_SECTORS`), the outlines of the
Galactica and basestar models traced from the image (`SHIP_OUTLINES`), the tracks, the resources, the deck rack, the
launch tubes) and adds the free starfield (`SLOTS`).

- Section names (`labels/board.ts`) aren't printed: the board draws each where the picture once printed it (`LABELS`),
  a chip like the mats' badges in greys, on the top edge of a box that has one.
- Standees stand on the printed names of their locations, reaching a little into the pictures. A damaged location gets
  a red "damaged" chip (like the Engine Room's "activated") at the bottom of its panel, under the name. From 4 damaged
  Galactica locations (`isCriticallyDamaged`) a red "Critical damage" plate lies on the hull above the "Armory", between
  the "Research Lab" and the "Brig" (`SLOTS.criticalDamage`), its ends in the hazard stripes of the printed frames;
  hovering it explains that 6 destroy Galactica.
- Ships fly in the middle of their space areas, the Cylon ships first, a ship as large in every area of its board, alone
  or piled (the bow's strip is 8 o'clock, the stern's 2 o'clock; the launch tubes stay free).
- Resources are a row of gauges where nothing is printed (`RESOURCES`, `resources.tsx`): its icon (`icons/`, flat
  one-colour SVGs) and a pip per unit (up to 15, centred on the jump track's line); the red zone's pips are a muted red
  that turns bright and glows while the amount is in it; hovering shows name and amount. While "Preventive Policy"
  makes its next loss this turn smaller (`TurnProjection.lossReductions`) a force field surrounds it, explained in the
  popover.
- Markers sit on the tracks. The Boarding Party track isn't printed: `boarding-party.png`, red only at its end, lies over
  the hull while a centurion is on it, and its centurions stand large and glow red.
- The decks lie in the rack, which prints no names but rings each Skill Card deck in its color, with their counts and
  top discard over them. Civilian ships lie at the end of the ship reserves, destinations tuck under the objective
  showing their distance, Crisis Cards kept in play lie by the jump track.
- Left out: the card being resolved, its skill check and the die (the turn panel shows them), the damage token piles
  (only the tokens in play show) and the nukes (the Admiral's mat).
- Hovering a location shows its card, a standee its character sheet with its player's mat beside it (`PlayerMatOf`, the
  preview's `extra`), and a pile of civilian ships what each costs when destroyed, to whoever knows it (a sandbox, the
  players who looked). A pile that holds a civilian ship the viewer looked at lies on a soft halo in `--known`.

## Board answers

The board and the mats answer the pending input (`src/input/boardAnswers.ts`, `AnsweringContext`). `offerOf(moves, picks)`
lights what the moves left name: the locations, ships (a ring) and standees of one kind of move at a time (the
only kind left, or the only one naming any; the Action step lights none), the "Act" chip of each location action (a
click picks its kind), and the asked player's cards that start a move (`Playable` on his mat; a sandbox shows the asked
player's own cards face up). A space area lights as the whole sector its printed lines cut out (`SpaceSector`): it
glows from its outer side and fades out towards the centre the areas converge on, cut off at the Galactica or basestar
model (a clip of the board minus `SHIP_OUTLINES`, so a click there reaches the location under it). A player target lights his standee and his mat. A click picks the target: the only move
left is sent, a shape opens its picker in the panel, and several stay in the panel and in `AnswerPopover` next to the
click (e.g. a viper's activations, a card's targets). Hovering a lit target (a location, the "Act" chip, a standee, a
ship, a `Playable` card) says at once what clicking does (`TargetHint`, `hintOf`: the move it sends or "n answers"),
as a popover over the preview of what is under the pointer (see [Hover](#hover)); a standee in a lit location shows the
location's, the innermost lit target its own. A group's row in the panel picks its kind, so the board lights its
targets.

## Hover

`src/table/reveal.tsx`: a pile shows what it holds in one fixed popover (`RevealLayer`), its cards as large as their
images (no preview of their own) in rows of up to 860px, scrolling when they don't fit the window. The preview of a
single image (`ArtPreview`) shows it as large as it is, scaled down only by a smaller window; a card back has none, so
it never covers its pile's popover. A lit target's hint is the first popover of the same layer (`.hover-pop`), the
preview a second one under it, placed together by the pointer; inside a lit target the board's images drop their
`<title>` tooltip (`InTarget`). Neither, nor the reference's grids, puts a name under an image (it stays the
image's `alt`); a pile keeps a label that says more, e.g. who played the card. A face-up discard pile shows to everyone.
A sandbox looks like a live table, every hand and deck face down, and shows on hover what the players can't see
(`hidden` sections: hands, Loyalty and other player cards, draw piles, face-down discards, a skill check's cards); the
cards played before a skill check lie face up there once revealed (`playedBeforeRevealed`).

## Reference

`table/reference/ReferenceDrawer.tsx`, opened by the turn panel's "Reference", the R key outside a field (which closes
it too) or a mat's portrait (its sheet): every card by deck with a search, the character sheets, the titles' lines of
succession (a column per title from the schema's `linesOfSuccession`, a portrait and a name per row), the rulebook's
sections, cheat sheets (the turn, a skill check, the Attack Table from the schema, the Crisis Card's icons, loyalty) and
what left the game (the executed characters' sheets, the destroyed ships with their cost, counted on the tab).
`labels/rules.ts` indexes the in-scope articles of the combined rulebook and its Appendix by page (the PDFs ship as Vite
assets) and gives each kind of input the article to read (`ruleFor`, the link beside the pause line). It names things
and points to the article, never repeats the rulebook's text; section names and cheat sheets are in our words.

## Art

`src/art/`: the images of `materials-sorted/`, a folder per enum and a file per constant (a Skill Card as `TYPE_value`,
a dial as `RESOURCE_value`; for what no enum names, `Title/`, `Token/`, `Die/` and `Back/SKILL`).

- `images.ts` loads the folders with `import.meta.glob` (not the full board scan at the root), so Vite copies them into
  the build, and gives an `ArtRef` per component (`cardArt`, `skillCardArt`, `characterArt` (the standee, the sheet as
  preview), `locationArt`, `shipArt`, `deckBackArt`, `titleArt`, `titleIconArt` (the insignia, also on the mats'
  badges), `tokenArt`, `dieArt`, `skillColorBackArt` (drawn: the Skill decks print no back), ...) or `null` where there
  is none: the UI then shows the name. `images.test.ts` checks that every constant has its image and every image names a
  constant.
- `refs.ts` finds what an event, a fact or an option names through the schema, for the log and the input.
- `Art.tsx`: `CardTile` (a face-up card is a `tile`, a face-down one a `tile back`, which the lobby e2e checks),
  `CardBack`, `ArtStrip`, `SvgArt`, and the hover layer (`ArtPreview`: a target's hint over the image's preview), which
  an image and a target take with them when they go away under the pointer (an image only the preview it showed itself:
  a standee's preview shows his mat, whose portrait is the same image, and StrictMode mounts it twice).
