package xyz.zlatanov.frakkintoasters.event.turn;

import xyz.zlatanov.frakkintoasters.event.Event;

// Each player but the first draws their starting Skill Cards (rule A3.9). The first player draws in their first turn.
public record ReceiveStartingSkillsEvent() implements Event {
}
