package xyz.zlatanov.frakkintoasters.event.endgame;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Arrays;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.Team.HUMANS;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

/**
 * Ends the game for a team, and works out which players win with it. Every ending goes through here: the Cylon Leaders'
 * results depend on the state of the game when it ended (rule 6.2.2.1), so they reveal their Motive Cards first.
 */
@NoArgsConstructor(access = PRIVATE)
public final class EndGame {

    public static void end(GameView game, GameOps ops, Team winner) {
        revealMotiveCards(game, ops);
        ops.endGame(winner, winners(game, winner));
    }

    // A Cylon Leader reveals every card he can: a card of the other allegiance still counts as revealed, so it never
    // costs him the win (rule 16.3.2).
    private static void revealMotiveCards(GameView game, GameOps ops) {
        for (val player : game.players()) {
            if (!isCylonLeader(player)) {
                continue;
            }
            // "Reveal this card if the game is over" waits for the Personal Goals, which the caller recorded first.
            for (val card : List.copyOf(player.motiveCards().cards())) {
                if (MotiveCards.canRevealAtTheEnd(game, player, card)) {
                    ops.player(player.number()).revealMotiveCard(card);
                }
            }
        }
    }

    /**
     * @return the players who win with the team: the humans with the humans, the players with a "You Are a Cylon" card,
     * revealed or not, with the Cylons, and a Cylon Leader along with either, by his Motive Cards (rule 16.3.2)
     */
    private static List<Integer> winners(GameView game, Team winner) {
        // A player whose character was executed and who has none left to choose has lost.
        return game.players().stream()
                .filter(player -> player.character() != null)
                .filter(player -> isCylonLeader(player) ? leaderWins(player, winner) : sideOf(player) == winner)
                .map(PlayerView::number)
                .toList();
    }

    private static boolean leaderWins(PlayerView leader, Team winner) {
        val unrevealed = leader.motiveCards().cards().size();
        val revealedForTheWinner = leader.motiveCards().revealedCards().stream()
                .filter(card -> card.allegiance() == winner)
                .count();
        return unrevealed <= 1 && revealedForTheWinner >= 2;
    }

    private static Team sideOf(PlayerView player) {
        val hasCylonCard = Arrays.stream(LoyaltyCard.values()).filter(LoyaltyCard::isCylon).anyMatch(card ->
                player.loyaltyCards().cards().contains(card) || player.loyaltyCards().revealedCards().contains(card));
        return hasCylonCard ? CYLONS : HUMANS;
    }

    private static boolean isCylonLeader(PlayerView player) {
        return player.character() != null && player.character().type() == CYLON_LEADER;
    }
}
