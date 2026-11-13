package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.Event;

// Makes the active skill check, unless "Political Prowess" already passed or failed it.
public record MakeSkillCheckEvent() implements Event {
}
