package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

// The player the card names as chooser picks its top or bottom section, and that section resolves (rule 8.5.2).
public record ChooseCrisisSectionEvent(int playerNumber, CrisisCard card, Section section) implements PlayerEvent {

    public enum Section {
        TOP,
        BOTTOM
    }
}
