package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.GLIMPSE_THE_FACE_OF_GOD;

// The second half of "Glimpse the Face of God": 2 Skill Cards from his hand go on top of the Destiny deck, the first one
// on top. Only he knows them.
@Source(ability = GLIMPSE_THE_FACE_OF_GOD)
public record GlimpseTheFaceOfGodPlaceEvent(int playerNumber, List<SkillCard> cards) implements PlayerEvent {
}
