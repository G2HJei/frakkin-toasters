package xyz.zlatanov.frakkintoasters.event.location.cylon;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnBottomEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnTopEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HUMAN_FLEET;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEOBEN_CONOY;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.DETENTE;

class HumanFleetLookAtTopCrisisCardEventProcessorTest extends EventTestHarness<HumanFleetLookAtTopCrisisCardEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(LEOBEN_CONOY);
        moveTo(HUMAN_FLEET, LEOBEN_CONOY);
    }
    @Test
    void shouldDrawCardAndFollowUpWithPlacementChoiceAndSkillDraw() {
        crisisDeck.nextCard(DETENTE);
        val initialSize = crisisDeck.size();

        execute(new HumanFleetLookAtTopCrisisCardEvent(1));

        assertEquals(initialSize - 1, crisisDeck.size());
        assertFollowup(
                all(
                        Followup.one(
                                new PlaceCrisisCardOnTopEvent(1, DETENTE),
                                new PlaceCrisisCardOnBottomEvent(1, DETENTE)),
                        single(
                                new PlayerDecisionEvent<>(1, DrawSkillCardsEvent.class, DRAW_EXACTLY_2))));
    }
}
