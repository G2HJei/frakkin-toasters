package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

// Puts chosen cards on top of the Destiny deck, the first on top, so the next skill check draws them. It lives in the
// tests, so the processor registry test doesn't see it.
public record StackDestinyDeckEvent(List<SkillCard> topFirst) implements Event {
}
