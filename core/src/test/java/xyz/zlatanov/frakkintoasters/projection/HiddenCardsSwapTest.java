package xyz.zlatanov.frakkintoasters.projection;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.ScriptedTurns;
import xyz.zlatanov.frakkintoasters.engine.journal.GameReplayer;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// At every pause of a seeded game, a viewer's projection doesn't change when another player's hidden cards are swapped
// for others of the same colors.
class HiddenCardsSwapTest {

    @Test
    void aViewerDoesntSeeTheOtherPlayersHiddenCards() {
        val engine = ScriptedTurns.start();
        var swaps = 0;
        swaps += assertSwapsAreInvisible(engine);
        for (val input : ScriptedTurns.inputs()) {
            input.apply(engine.view()).applyTo(engine);
            swaps += assertSwapsAreInvisible(engine);
        }
        assertTrue(swaps > 50, "the swaps changed cards " + swaps + " times");
    }

    private int assertSwapsAreInvisible(GameEngine engine) {
        var swaps = 0;
        for (int owner = 1; owner <= 3; owner++) {
            val original = GameReplayer.replay(engine.journal());
            val swapped = GameReplayer.replay(engine.journal());
            swaps += swapHiddenCards(swapped, owner);
            for (val viewer : viewersOtherThan(owner)) {
                assertEquals(GameProjection.of(original, engine.pendingInput(), viewer),
                        GameProjection.of(swapped, engine.pendingInput(), viewer),
                        "player " + owner + "'s cards as seen by " + viewer);
            }
        }
        return swaps;
    }

    private static List<Viewer> viewersOtherThan(int owner) {
        val viewers = new ArrayList<Viewer>();
        for (int player = 1; player <= 3; player++) {
            if (player != owner) {
                viewers.add(Viewer.player(player));
            }
        }
        viewers.add(Viewer.observer());
        return viewers;
    }

    private static int swapHiddenCards(Game game, int owner) {
        var swaps = 0;
        val hand = game.player(owner).skillCards();
        for (val card : hand.cards()) {
            val other = otherCardOfTheColor(game, card);
            hand.remove(card);
            hand.addOnTop(other);
            swaps++;
        }
        val loyalty = game.player(owner).loyaltyCards();
        for (val card : loyalty.cards()) {
            if (card == LoyaltyCard.MUTINEER) {
                continue; // faceup once someone holds it
            }
            loyalty.remove(card);
            loyalty.addOnTop(card == LoyaltyCard.NOT_CYLON ? LoyaltyCard.CYLON_CAN_PLACE_CENTURION : LoyaltyCard.NOT_CYLON);
            swaps++;
        }
        return swaps;
    }

    private static SkillCard otherCardOfTheColor(Game game, SkillCard card) {
        return game.decks().<SkillCard>deck(DeckId.of(card.type().color())).cards().stream()
                .filter(other -> !other.equals(card))
                .findFirst()
                .orElseThrow();
    }
}
