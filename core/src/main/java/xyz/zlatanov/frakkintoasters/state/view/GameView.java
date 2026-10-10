package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import java.util.List;

public interface GameView {
    ObjectiveCard objective();

    List<PlayerView> players();

    PlayerView player(int playerNumber);

    int currentPlayer();

    GameStep step();

    //BoardsHolderView boards();

    //  DecksHolderView decks();

    //SkillCheckHolderView activeSkillCheck();

    //  CylonShipsView cylonShips();

    int nukes();

    Character president();

    //  DeckView<QuorumCard> presidentHand();

    Character admiral();

    Character cag();

    Location locate(Character character);
}
