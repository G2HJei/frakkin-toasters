package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.event.Event;

// "If the previous step causes the total distance ... to equal or exceed the given distances for the first time, then
// follow the instructions on the Objective Card." (rule 12.1.1, step 6). With the Kobol Objective that is the Sleeper
// Agent phase at 4 distance. At 8 the humans are one jump from the end, which JumpingTheFleetEvent checks.
//
// @param distanceBefore the distance traveled before the Destination Card of this jump
public record ObjectiveInstructionsEvent(int distanceBefore) implements Event {
}
