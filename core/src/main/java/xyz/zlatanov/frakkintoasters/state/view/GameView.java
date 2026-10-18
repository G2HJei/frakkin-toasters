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
