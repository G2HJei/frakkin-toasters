package xyz.zlatanov.frakkintoasters.event;

import java.util.List;

public record MoveCylonShipsToMainBoard(List<Integer> shipIds) implements Event {
}
