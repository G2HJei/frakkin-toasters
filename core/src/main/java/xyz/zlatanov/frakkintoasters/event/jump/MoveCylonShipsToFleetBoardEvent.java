package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.event.Event;

// Move the Cylon ships in space areas of the main game board to the corresponding space areas of the Cylon Fleet board.
// The basestar damage tokens go back into the pile of unused tokens, randomly mixed (rule 12.1.1). Civilian ships and
// centurions on the Boarding Party track stay.
public record MoveCylonShipsToFleetBoardEvent() implements Event {
}
