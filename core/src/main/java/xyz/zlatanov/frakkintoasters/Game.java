package xyz.zlatanov.frakkintoasters;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public class Game {
    private final ObjectiveCard objective;
    private int food = 8;
    private int morale = 10;
    private int population = 12;

}
