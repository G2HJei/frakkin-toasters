package xyz.zlatanov.frakkintoasters.state.view;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.RemovedComponents;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.LocationsArea;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCardInPlay;
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

    /**
     * @return whether "Bomb on Colonial One" failed and stays in play: nobody may move to Colonial One for the rest of the
     * game (rule 9.4), and its locations have no action
     */
    default boolean colonialOneDestroyed() {
        return crisisCardsInPlay().contains(SkillCheckType.BOMB_ON_COLONIAL_1);
    }

    /**
     * @return whether the location is on a destroyed Colonial One or a destroyed Pegasus: nobody moves or starts there,
     * and it has no action
     */
    default boolean isDestroyed(Location location) {
        return colonialOneDestroyed() && LocationsArea.COLONIAL_ONE.locations().contains(location)
                || boards().pegasus().destroyed() && LocationsArea.PEGASUS.locations().contains(location);
    }

    Character president();

    DeckView<QuorumCard> presidentHand();

    /**
     * @return the Quorum Cards that stay in play: the roles with their holders, and "Accept Prophecy"
     */
    List<QuorumCardInPlay> quorumCardsInPlay();

    default boolean inPlay(QuorumCard card) {
        return quorumCardsInPlay().stream().anyMatch(inPlay -> inPlay.card() == card);
    }

    /**
     * @return the player the card in play was given to, empty when it isn't in play or has no holder
     */
    default Optional<Integer> quorumCardHolder(QuorumCard card) {
        return quorumCardsInPlay().stream()
                .filter(inPlay -> inPlay.card() == card)
                .map(QuorumCardInPlay::holderPlayerNumber)
                .filter(java.util.Objects::nonNull)
                .findFirst();
    }

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
