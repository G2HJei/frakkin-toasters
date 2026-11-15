package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

// After the die roll of a section of a Crisis Card, e.g. "Roll a die. On a 4 or less, -1 fuel." The effect is in
// CrisisCardFollowups, with the rest of the card.
public record ResolveCrisisRollEvent(CrisisCard card, Section section) implements Event {
}
