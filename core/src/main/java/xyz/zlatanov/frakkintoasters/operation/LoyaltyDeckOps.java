package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsAddedOnTop;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.util.Arrays;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;

//todo maybe create SetupUpOps and move the code there
public class LoyaltyDeckOps extends AbstractOps {

    LoyaltyDeckOps(Game game, FactRecorder recorder) {
        super(game, recorder);
    }


    public LoyaltyDeckOps createNotACylon() {
        record(new CardsAddedOnTop(DeckId.LOYALTY_NOT_CYLON,
                //todo check correct vanilla non-cylon cards count
                List.of(NOT_CYLON, NOT_CYLON, NOT_CYLON, NOT_CYLON, NOT_CYLON, NOT_CYLON, NOT_CYLON, NOT_CYLON, NOT_CYLON, NOT_CYLON, NOT_CYLON,
                        NOT_CYLON_USE_CAUTION,
                        NOT_CYLON_STAND_AND_FIGHT,
                        NOT_CYLON_SELFISH,
                        NOT_CYLON_SELF_DESTRUCTION,
                        NOT_CYLON_SACRIFICE,
                        NOT_CYLON_POLITICAL_INTRIGUE,
                        NOT_CYLON_DEVASTATION,
                        NOT_CYLON_ACQUIRE_POWER,
                        FINAL_FIVE_YOU_ARE_SENT_TO_BRIG,
                        FINAL_FIVE_YOU_ARE_EXECUTED,
                        FINAL_FIVE_THEY_ARE_EXECUTED,
                        FINAL_FIVE_DAMAGE_GALACTICA_TWICE,
                        FINAL_FIVE_CYLON_SHIPS_ACTIVATE)));
        shuffle(DeckId.LOYALTY_NOT_CYLON);
        return this;
    }

    public LoyaltyDeckOps create(int notCylonDraws, int cylonDraws, boolean withMutineer) {
        List<LoyaltyCard> notACylonLoyalties = draw(DeckId.LOYALTY_NOT_CYLON, notCylonDraws);
        record(new CardsAddedOnTop(DeckId.LOYALTY, notACylonLoyalties));
        record(new CardsAddedOnTop(DeckId.LOYALTY, drawCylonLoyalties(cylonDraws)));
        if (withMutineer) {
            record(new CardsAddedOnTop(DeckId.LOYALTY, List.of(MUTINEER)));
        }
        shuffle(DeckId.LOYALTY);
        return this;
    }

    /**
     * The cylon loyalty cards are not a deck of the game: a random selection of them is dealt, and the chosen cards are
     * what gets recorded.
     */
    private List<LoyaltyCard> drawCylonLoyalties(int count) {
        val cylonLoyalties = Arrays.stream(values())
                .filter(LoyaltyCard::isCylon)
                .toList();
        return game.randomness().shuffled(cylonLoyalties).subList(0, count);
    }
}
