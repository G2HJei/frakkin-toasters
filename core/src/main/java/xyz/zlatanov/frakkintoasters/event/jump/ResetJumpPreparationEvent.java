package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.event.Event;

// "Move the fleet token to the start space of the Jump Preparation track." (rule 12.1.1, step 7)
public record ResetJumpPreparationEvent() implements Event {
}
