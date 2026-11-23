package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.COMMAND_AUTHORITY;

// Just before a skill check ends, William Adama may use "Command Authority", if he still can after its result. Queued
// only in games where someone has the ability.
@Source(ability = COMMAND_AUTHORITY)
public record OfferCommandAuthorityEvent() implements Event {
}
