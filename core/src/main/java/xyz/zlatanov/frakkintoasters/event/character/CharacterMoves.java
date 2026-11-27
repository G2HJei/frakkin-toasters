package xyz.zlatanov.frakkintoasters.event.character;

import lombok.NoArgsConstructor;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESURRECTION_SHIP;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.CLOUDED;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.HERETIC;

@NoArgsConstructor(access = PRIVATE)
public final class CharacterMoves {

    /**
     * Leoben Conoy's "Clouded": no move of his own choosing ends where another character stands. A card or another
     * player may still put him there.
     */
    public static boolean cloudedBlocks(GameView game, PlayerView player, Location destination) {
        return player.hasAbility(CLOUDED) && game.players().stream()
                .filter(other -> other.number() != player.number() && other.character() != null)
                .anyMatch(other -> game.locate(other.character()).equals(Optional.of(destination)));
    }

    /**
     * D'Anna Biers' "Heretic": she leaves the "Resurrection Ship" only by discarding a Super Crisis Card.
     */
    public static boolean hereticCost(GameView game, PlayerView player) {
        return player.hasAbility(HERETIC) && game.locate(player.character()).equals(Optional.of(RESURRECTION_SHIP));
    }

    public static boolean hereticHeld(GameView game, PlayerView player) {
        return hereticCost(game, player) && player.superCrisisCards().isEmpty();
    }
}
