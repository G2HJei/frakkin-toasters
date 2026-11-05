package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

public interface PlayerEvent extends Event {

    // The acting player: usually the current player, but not always, e.g. the target of an "Executive Order" card, or
    // any player taking part in a skill check. Events that always involve the current player do not need to be
    // PlayerEvents, use GameView.currentPlayer() instead. See also GameView.actingPlayer().

    int playerNumber();

    default PlayerView player(GameView game) {
        return game.player(playerNumber());
    }
}
