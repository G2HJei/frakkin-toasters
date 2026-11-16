package xyz.zlatanov.frakkintoasters.event.loyalty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.ASTEROID_FIELD;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.DESOLATE_MOON;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.ICY_MOON;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class FinishSleeperAgentPhaseEventProcessorTest extends EventTestHarness<FinishSleeperAgentPhaseEvent> {

    @BeforeEach
    void players() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
    }

    // "When a Cylon player receives Loyalty Cards during the Sleeper Agent phase, he looks at all of his facedown Loyalty
    // Cards and gives them to one human player of his choice." (rule 5.5.1)
    @Test
    void aRevealedCylonGivesTheCardsToAHuman() {
        player(1).loyaltyCards().addOnTop(CYLON_DAMAGE_GALACTICA, NOT_CYLON).reveal(CYLON_DAMAGE_GALACTICA);

        execute(new FinishSleeperAgentPhaseEvent());

        assertFollowup(one(new GiveLoyaltyCardsEvent(1, 2), new GiveLoyaltyCardsEvent(1, 3)));
    }

    // "The human player he gives it to must immediately reveal the Mutineer card as if it had been dealt to him." (5.5.2.2)
    @Test
    void aRevealedCylonPassesTheMutineerOn() {
        player(1).loyaltyCards().addOnTop(CYLON_DAMAGE_GALACTICA, MUTINEER).reveal(CYLON_DAMAGE_GALACTICA);

        execute(new FinishSleeperAgentPhaseEvent());

        assertFollowup(one(new PassMutineerCardEvent(1, 2, true), new PassMutineerCardEvent(1, 3, true)));
    }

    @Test
    void aHumanHandsNothingOn() {
        player(1).loyaltyCards().addOnTop(NOT_CYLON);

        execute(new FinishSleeperAgentPhaseEvent());
    }

    // "If the Mutineer card was included in the Loyalty deck and was not revealed, the current player chooses a human
    // player to draw an additional card from the Loyalty deck." (rule 5.5.2.3)
    @Test
    void theCurrentPlayerChoosesWhoDrawsAnAdditionalCardWhenNobodyGotTheMutineer() {
        loyaltyDeck.addOnTop(MUTINEER);
        ops.changeCurrentPlayer(2);

        execute(new FinishSleeperAgentPhaseEvent());

        assertFollowup(one(
                new ChooseExtraLoyaltyCardEvent(2, 2),
                new ChooseExtraLoyaltyCardEvent(2, 3),
                new ChooseExtraLoyaltyCardEvent(2, 1)));
    }

    // "If Galactica has traveled seven or more distance no excess loyalty cards may be handed off." (rule 6.1.1)
    @Test
    void noHandOffFromSevenDistanceOn() {
        ops.galactica().reachDestination(ASTEROID_FIELD);
        ops.galactica().reachDestination(DESOLATE_MOON);
        ops.galactica().reachDestination(ICY_MOON);
        player(1).loyaltyCards().addOnTop(CYLON_DAMAGE_GALACTICA, NOT_CYLON).reveal(CYLON_DAMAGE_GALACTICA);

        execute(new FinishSleeperAgentPhaseEvent());
    }

    @Test
    void theMutineerIsPassedOnNoMatterTheDistance() {
        ops.galactica().reachDestination(ASTEROID_FIELD);
        ops.galactica().reachDestination(DESOLATE_MOON);
        ops.galactica().reachDestination(ICY_MOON);
        player(1).loyaltyCards().addOnTop(CYLON_DAMAGE_GALACTICA, MUTINEER, NOT_CYLON).reveal(CYLON_DAMAGE_GALACTICA);

        execute(new FinishSleeperAgentPhaseEvent());

        assertFollowup(all(one(new PassMutineerCardEvent(1, 2, true), new PassMutineerCardEvent(1, 3, true))));
    }
}
