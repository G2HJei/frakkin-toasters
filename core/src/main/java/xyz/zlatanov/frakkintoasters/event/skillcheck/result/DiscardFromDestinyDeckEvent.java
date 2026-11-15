package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

// "Search the Destiny deck and choose 2 cards to discard. Then reshuffle the Destiny deck." ("Mysterious Message")
public record DiscardFromDestinyDeckEvent(int playerNumber, List<SkillCard> cards) implements PlayerEvent {
}
