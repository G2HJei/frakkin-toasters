package xyz.zlatanov.frakkintoasters.state.view;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.RemovedComponents;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;
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

    /**
     * @return whether an execution has cost the fleet morale in this game, for "Eulogy"
     */
    boolean moraleLostToExecution();

    /**
     * The skill checks of the Crisis Cards that are kept in play, e.g. "Detector Sabotage" and "Legendary Discovery".
     */
    List<SkillCheckType> crisisCardsInPlay();

    Character president();

    DeckView<QuorumCard> presidentHand();

    Character admiral();

    Character cag();

    /**
     * @return the location of the character, empty while it is not on the board (e.g. a "Stranded" Karl "Helo" Agathon)
     */
    Optional<Location> locate(Character character);

    Optional<Team> outcome();

    /**
     * The players who won, empty while the game goes on. A Cylon Leader wins by his Motive Cards, so it can be a player
     * of either team (rule 16.3.2).
     */
    List<Integer> winners();

    /**
     * The Destination Cards next to the Objective Card, in the order the fleet reached them.
     */
    List<DestinationCard> destinations();

    /**
     * The distance the fleet has traveled (rule 12.3).
     */
    default int distanceTraveled() {
        // "Legendary Discovery" is placed next to the Kobol Objective card and counts as 1 distance
        val legendaryDiscoveries = crisisCardsInPlay().stream().filter(SkillCheckType.LEGENDARY_DISCOVERY::equals).count();
        return destinations().stream().mapToInt(DestinationCard::distance).sum() + (int) legendaryDiscoveries;
    }

    /**
     * The components out of the game, e.g. destroyed ships and executed characters.
     */
    RemovedComponents removedComponents();
}
