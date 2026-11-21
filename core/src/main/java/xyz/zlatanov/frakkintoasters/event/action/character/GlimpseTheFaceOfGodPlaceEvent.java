package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

// The second half of "Glimpse the Face of God": 2 Skill Cards from his hand go on top of the Destiny deck, the first one
// on top. Only he knows them.
public record GlimpseTheFaceOfGodPlaceEvent(int playerNumber, List<SkillCard> cards) implements PlayerEvent {
}
