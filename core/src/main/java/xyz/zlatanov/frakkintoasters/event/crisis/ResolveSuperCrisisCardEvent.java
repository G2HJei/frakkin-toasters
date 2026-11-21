package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;

// The Super Crisis Card a revealed Cylon played at "Caprica" resolves: its skill check, or its chooser's pick of a
// section. The player is the Cylon who played it.
public record ResolveSuperCrisisCardEvent(int playerNumber, SuperCrisisCard card) implements PlayerEvent {
}
