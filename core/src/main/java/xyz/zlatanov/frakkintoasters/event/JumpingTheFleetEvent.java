package xyz.zlatanov.frakkintoasters.event;

// The fleet jumps. A blind jump (Helena Cain's "Blind Jump") lets the Admiral draw only 1 Destination Card.
public record JumpingTheFleetEvent(boolean blindJump) implements Event {

    public JumpingTheFleetEvent() {
        this(false);
    }
}
