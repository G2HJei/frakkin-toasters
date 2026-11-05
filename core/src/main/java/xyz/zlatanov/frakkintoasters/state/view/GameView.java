package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import java.util.List;

public interface GameView {

    ObjectiveCard objective();

    List<? extends PlayerView> players();

    PlayerView player(int playerNumber);

    int currentPlayerNumber();

    PlayerView currentPlayer();

    /**
     * The player who is taking an action or moving right now. It is the current player, except when someone else acts
     * during that player's turn (e.g. the target of an "Executive Order" card). It does not change who the "current
     * player" is for game effects, e.g. skill checks.
     */
    int actingPlayerNumber();

    PlayerView actingPlayer();

    GameStep step();

    int turn();

    BoardsHolderView boards();

    SkillCheckView activeSkillCheck();

    CylonShipsView cylonShips();

    int nukes();

    Character president();

    Character admiral();

    Character cag();

    Location locate(Character character);
}
