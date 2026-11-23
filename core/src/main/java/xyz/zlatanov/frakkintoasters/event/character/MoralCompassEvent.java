package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MORAL_COMPASS;

// Karl "Helo" Agathon's "Moral Compass" (once per game): after a player chooses a section of a Crisis Card, he changes
// the choice to the other section, which resolves instead.
@Source(ability = MORAL_COMPASS)
public record MoralCompassEvent(int playerNumber, CrisisCard card, Section section) implements PlayerEvent {
}
