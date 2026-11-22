package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

// Asks a player whether he plays "Political Prowess", "Change of Plans" or "Declare Emergency" into the skill check, as
// the check stands when he is asked, so a card another player played first is taken into account. Every player is
// asked, so the table doesn't learn who holds the card.
public record OfferPlayOrPassEvent(int playerNumber, SkillCardType card) implements PlayerEvent {
}
