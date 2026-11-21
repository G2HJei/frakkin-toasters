package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.ResetActingPlayerEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESEARCH_LAB;
import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.FORWARD_THINKER;

class ForwardThinkerEventProcessorTest extends EventTestHarness<ForwardThinkerEvent> {

    // Once his "Executive Order" target is done, the action of his own location.
    @Test
    void leeAdamaActivatesHisLocation() {
        player(1).character(LEE_ADAMA);
        moveTo(RESEARCH_LAB, LEE_ADAMA);
        assertEquals(one(new ForwardThinkerEvent(1), new NoOpEvent(1)),
                ForwardThinkerEventProcessor.offer(game, player(1)));

        execute(new ForwardThinkerEvent(1));

        assertTrue(game.turn().hasUsed(1, FORWARD_THINKER));
        assertFollowup(new PlayerDecisionEvent<>(1, LocationEvent.class));
    }

    // On another player's turn he acts, then that player does again.
    @Test
    void outOfHisTurn() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(LEE_ADAMA);
        moveTo(RESEARCH_LAB, LEE_ADAMA);

        execute(new ForwardThinkerEvent(2));

        assertEquals(2, game.turn().actingPlayerNumber());
        assertFollowup(all(new PlayerDecisionEvent<>(2, LocationEvent.class), new ResetActingPlayerEvent()));
    }

    @Test
    void notWhereHeHasNoAction() {
        player(1).character(LEE_ADAMA);
        moveTo(SICKBAY, LEE_ADAMA);

        assertEquals(NONE, ForwardThinkerEventProcessor.offer(game, player(1)));
        assertInvalid(new ForwardThinkerEvent(1));
    }
}
