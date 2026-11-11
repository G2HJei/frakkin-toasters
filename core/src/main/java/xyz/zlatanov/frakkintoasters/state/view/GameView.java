package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import java.util.List;
import java.util.Optional;

public interface GameView {

    ObjectiveCard objective();

    List<? extends PlayerView> players();

    PlayerView player(int playerNumber);

    int playerNumber(Character character);

    TurnView turn();

    PlayerView currentPlayer();

    /**
     * @see TurnView#actingPlayerNumber()
     */
    PlayerView actingPlayer();

    BoardsHolderView boards();

    DecksHolderView decks();

    SkillCheckView activeSkillCheck();

    CylonShipsView cylonShips();

    int nukes();

    Character president();

    DeckView<QuorumCard> presidentHand();

    Character admiral();

    Character cag();

    /**
     * @return the location of the character, empty while it is not on the board (e.g. a "Stranded" Karl "Helo" Agathon)
     */
    Optional<Location> locate(Character character);

    Optional<Team> outcome();
}
