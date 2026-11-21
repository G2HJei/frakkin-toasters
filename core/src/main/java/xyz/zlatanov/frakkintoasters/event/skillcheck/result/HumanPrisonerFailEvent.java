package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.event.Event;

// Human Prisoner (Super Crisis Card), fail: the Cylon who played the card picks a human, takes his whole Skill hand and
// sends him to "Sickbay".
public record HumanPrisonerFailEvent() implements Event {
}
