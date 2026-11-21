package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import java.util.List;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Impeachment (Mutiny Card)
// Action: discard 5 Skill Cards to become President. Discard it. Not with fewer than 5 Skill Cards, nor as President.
public record ImpeachmentEvent(int playerNumber, List<SkillCard> discards) implements MutinyCardEvent {
}
