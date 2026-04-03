package xyz.zlatanov.frakkintoasters.action;

import xyz.zlatanov.frakkintoasters.Game;

public record PlayerDecisionAction(int player, Class<? extends Action> action) implements Action {
    @Override
    public void apply(Game game) {

    }
}
