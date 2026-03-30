package xyz.zlatanov.frakkintoasters.action;

import xyz.zlatanov.frakkintoasters.Game;

import java.util.List;

public interface Action {
    List<Action> apply(Game game);
}
