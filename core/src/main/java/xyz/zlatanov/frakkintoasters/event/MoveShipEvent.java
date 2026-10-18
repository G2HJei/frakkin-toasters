package xyz.zlatanov.frakkintoasters.event;

public record MoveShipEvent(int shipId,
                            xyz.zlatanov.frakkintoasters.state.board.Location galacticaLocation) implements Event {
}
