package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.event.Event;

// Human Prisoner (super crisis)
// FAIL: The Cylon player who played this card chooses one human player and takes all of his Skill Cards. That human
// player's character is then sent to "Sickbay."
public record HumanPrisonerFailEvent() implements Event {
}
