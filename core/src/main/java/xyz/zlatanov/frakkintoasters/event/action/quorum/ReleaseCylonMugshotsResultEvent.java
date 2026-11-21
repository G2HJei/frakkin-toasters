package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Event;

// What the die decides after "Release Cylon Mugshots": 1 morale lost on 3 or less. Then the card is discarded.
public record ReleaseCylonMugshotsResultEvent() implements Event {
}
