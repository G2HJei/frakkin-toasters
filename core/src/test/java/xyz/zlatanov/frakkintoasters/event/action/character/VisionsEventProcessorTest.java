package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.LookAtLoyaltyCardEvent;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.DETECTOR_SABOTAGE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.NOT_CYLON;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class VisionsEventProcessorTest extends EventTestHarness<VisionsEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(DANNA_BIERS);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        moveTo(HUMAN_FLEET, DANNA_BIERS);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        player(2).loyaltyCards().addOnTop(NOT_CYLON);
    }

    @Test
    void looksAtALoyaltyCardThenEndsTheInfiltration() {
        ops.player(1).infiltrateGalactica(COMMAND);

        execute(new VisionsEvent(1, 2));

        assertFollowup(all(new LookAtLoyaltyCardEvent(1, 2), new EndInfiltrationEvent(1)));
    }

    @Test
    void onlyWhileInfiltrating() {
        assertInvalid(new VisionsEvent(1, 2));
    }

    @Test
    void notAtAPlayerWithoutLoyaltyCards() {
        ops.player(1).infiltrateGalactica(COMMAND);

        assertInvalid(new VisionsEvent(1, 3));
    }

    // Not while "Detector Sabotage" forbids looking at Loyalty Cards, as "Cylon Detector".
    @Test
    void notWhileDetectorSabotageIsInPlay() {
        ops.player(1).infiltrateGalactica(COMMAND);
        ops.keepCrisisCardInPlay(DETECTOR_SABOTAGE);

        assertInvalid(new VisionsEvent(1, 2));
    }
}
