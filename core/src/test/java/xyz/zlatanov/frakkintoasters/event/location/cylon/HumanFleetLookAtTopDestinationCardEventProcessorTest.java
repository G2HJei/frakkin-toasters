package xyz.zlatanov.frakkintoasters.event.location.cylon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.deck.LookAtTopCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HUMAN_FLEET;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEOBEN_CONOY;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.DESTINATION;

class HumanFleetLookAtTopDestinationCardEventProcessorTest extends EventTestHarness<HumanFleetLookAtTopDestinationCardEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(LEOBEN_CONOY);
        moveTo(HUMAN_FLEET, LEOBEN_CONOY);
    }

    @Test
    void shouldLookAtTheTopCardThenDrawTwoSkillCards() {
        execute(new HumanFleetLookAtTopDestinationCardEvent(1));

        assertFollowup(all(new LookAtTopCardEvent(1, DESTINATION),
                new PlayerDecisionEvent<>(1, DrawSkillCardsEvent.class, DRAW_EXACTLY_2)));
    }
}
