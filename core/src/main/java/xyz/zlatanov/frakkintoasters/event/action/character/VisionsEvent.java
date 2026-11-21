package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

// D'Anna Biers' "Visions": while Infiltrating, she looks at a random Loyalty Card of a human, then her
// Infiltration ends at the "Resurrection Ship".
public record VisionsEvent(int playerNumber, int targetPlayerNumber) implements CharacterActionEvent {
}
