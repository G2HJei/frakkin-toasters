package xyz.zlatanov.frakkintoasters.action;

import xyz.zlatanov.frakkintoasters.Game;

import java.util.List;


public class NoopAction implements Action {

    @Override
    public List<Action> apply(Game game) {
        return List.of();
    }
}
