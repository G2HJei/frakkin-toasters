package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.event.Event;

// Iron Will - color: LEADERSHIP, values: 0
// Skill Check: If total strength in this skill check is within 4 of the difficulty, do not trigger the fail effect. If
// total strength in this skill check is 0 or less, lose 1 morale.
public record IronWillEvent() implements Event {
}
