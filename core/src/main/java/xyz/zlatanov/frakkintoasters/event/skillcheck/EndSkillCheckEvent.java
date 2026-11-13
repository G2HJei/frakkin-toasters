package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.Event;

// Discards the Skill Cards of the active skill check and ends it, after everything its result queued.
public record EndSkillCheckEvent() implements Event {
}
