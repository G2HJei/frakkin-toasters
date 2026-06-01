package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.LIONS_HEAD_NEBULA;

class HumanFleetLookAtTopDestinationCardEventProcessorTest extends EventTestHarness<HumanFleetLookAtTopDestinationCardEvent> {

    @Test
    void shouldDrawCardAndFollowUpWithPlacementChoiceAndSkillDraw() {
        nextCard(destinationDeck, LIONS_HEAD_NEBULA);
        val initialSize = destinationDeck.size();

        executeAndAssertFollowup(new HumanFleetLookAtTopDestinationCardEvent(1),
                all(
                        one(
                                new PlaceDestinationCardOnTopEvent(1, LIONS_HEAD_NEBULA),
                                new PlaceDestinationCardOnBottomEvent(1, LIONS_HEAD_NEBULA)),
                        single(
                                new PlayerDecisionEvent<>(1, DrawSkillCardsEvent.class, DRAW_EXACTLY_2))
                ));

        assertEquals(initialSize - 1, destinationDeck.size());
    }
}
