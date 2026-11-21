package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import java.util.List;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Assume Command (Mutiny Card)
// Action: discard 5 Skill Cards to become Admiral. Discard it. Not with fewer than 5 Skill Cards, as Admiral or in the
// "Brig".
public record AssumeCommandEvent(int playerNumber, List<SkillCard> discards) implements MutinyCardEvent {
}
