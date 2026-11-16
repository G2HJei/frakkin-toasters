package xyz.zlatanov.frakkintoasters.event.jump;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.IncreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DrawAndDestroyCivilianShipsEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class ResolveDestinationCardEventProcessorTest extends EventTestHarness<ResolveDestinationCardEvent> {

    @BeforeEach
    void anAdmiral() {
        player(2).character(WILLIAM_ADAMA);
        admiral(WILLIAM_ADAMA);
    }

    // "The Admiral places the chosen card faceup next to the Objective Card and follows any instructions on the card."
    @Test
    void theCardCountsAndItsEffectsComeBeforeTheObjectiveAndTheReset() {
        execute(new ResolveDestinationCardEvent(BARREN_PLANET));

        assertEquals(2, game.distanceTraveled());
        assertFollowup(all(
                single(lose(FUEL, 2)),
                single(new ObjectiveInstructionsEvent(0)),
                single(new ResetJumpPreparationEvent()),
                NONE));
    }

    @Test
    void theLionsHeadNebulaAdvancesTheTrackAfterTheReset() {
        execute(new ResolveDestinationCardEvent(LIONS_HEAD_NEBULA));

        assertFollowup(all(
                single(lose(FUEL, 4)),
                single(new ObjectiveInstructionsEvent(0)),
                single(new ResetJumpPreparationEvent()),
                all(new IncreaseJumpPreparationEvent(), new IncreaseJumpPreparationEvent())));
    }

    // "Draw 1 civilian ship and destroy it. Then discard this card and draw a new Destination Card to resolve."
    @Test
    void aMisjumpDrawsAnotherCardAndHasNoDistance() {
        destinationDeck.nextCard(GAS_GIANT);

        execute(new ResolveDestinationCardEvent(MISJUMP));

        assertEquals(0, game.distanceTraveled());
        assertEquals(MISJUMP, game.decks().destination().lastDiscarded());
        assertFollowup(all(single(new DrawAndDestroyCivilianShipsEvent(1)),
                single(new ResolveDestinationCardEvent(GAS_GIANT))));
    }
}
