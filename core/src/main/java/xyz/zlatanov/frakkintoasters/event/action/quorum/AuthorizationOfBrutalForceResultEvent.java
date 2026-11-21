package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Event;

// What the die decides after "Authorization of Brutal Force": 1 population lost on 2 or less. Then the card is
// discarded.
public record AuthorizationOfBrutalForceResultEvent() implements Event {
}
