package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.experimental.Accessors;

@Accessors(fluent = true)
public record Raider(int id) implements Ship {
}
