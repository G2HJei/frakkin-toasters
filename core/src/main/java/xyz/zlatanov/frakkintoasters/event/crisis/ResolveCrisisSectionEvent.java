package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

// The section a player chose resolves, when "Moral Compass" could have changed the choice and didn't.
public record ResolveCrisisSectionEvent(CrisisCard card, Section section) implements Event {
}
