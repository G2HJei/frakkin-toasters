package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.event.Event;

// Quorum in Uproar (Daybreak crisis)
// FAIL: The President discards 2 random Quorum Cards and 2 random Skill Cards.
// Covers the Quorum Cards. The Skill Cards are discarded by a separate DiscardRandomSkillCardsEvent.
public record QuorumInUproarFailEvent() implements Event {
}
