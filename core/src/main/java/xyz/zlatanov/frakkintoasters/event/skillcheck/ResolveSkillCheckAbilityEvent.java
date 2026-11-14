package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

// The skill check ability the current player resolves next (rule 10.4.2).
public record ResolveSkillCheckAbilityEvent(SkillCardType type) implements Event {
}
