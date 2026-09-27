package xyz.zlatanov.frakkintoasters.operation;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class GameOperations {
    private ShipOperations ship;
}
