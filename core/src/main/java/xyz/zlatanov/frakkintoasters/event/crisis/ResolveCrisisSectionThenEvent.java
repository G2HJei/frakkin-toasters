package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

// The second part of a section, built once the first is done, because the first may change who it names: a morale
// loss that executes "Emotionally Fragile" Dee, a "Bait" discarded into a Mutiny Card that sends someone to the
// "Brig". The effect is in CrisisCardFollowups, with the rest of the card.
public record ResolveCrisisSectionThenEvent(CrisisCard card, Section section) implements Event {
}
