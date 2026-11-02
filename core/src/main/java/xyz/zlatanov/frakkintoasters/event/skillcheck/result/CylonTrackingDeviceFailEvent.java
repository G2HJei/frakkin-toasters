package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.event.Event;

// Cylon Tracking Device (Galactica crisis)
// FAIL: Destroy 1 raptor and place a basestar in front of Galactica and 2 civilian ships behind it.
// Covers placing the ships. The raptor is destroyed by a separate DestroyRaptorEvent.
public record CylonTrackingDeviceFailEvent() implements Event {
}
