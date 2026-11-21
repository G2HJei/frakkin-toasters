package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;

// The title holder a Super Crisis Card names as chooser picks its top or bottom section, and that section resolves.
public record ChooseSuperCrisisSectionEvent(int playerNumber, SuperCrisisCard card, Section section) implements PlayerEvent {
}
