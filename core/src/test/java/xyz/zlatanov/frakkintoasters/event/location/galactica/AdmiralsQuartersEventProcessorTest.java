package xyz.zlatanov.frakkintoasters.event.location.galactica;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.UseArbitratorEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class AdmiralsQuartersEventProcessorTest extends EventTestHarness<AdmiralsQuartersEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(WILLIAM_ADAMA);
        moveTo(Location.ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        player(3).character(GAIUS_BALTAR);
        moveTo(RESEARCH_LAB, GAIUS_BALTAR);
    }

    @Test
    void shouldActivateAdmiralsQuartersSkillCheck() {
        execute(new AdmiralsQuartersEvent(1, 3));
        assertSkillCheckTriggered(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldRememberTheChosenCharacter() {
        execute(new AdmiralsQuartersEvent(1, 3));
        assertSkillCheckTriggered(ADMIRALS_QUARTERS);
        assertEquals(3, game.activeSkillCheck().targetPlayerNumber());
    }

    // "Accept Prophecy" makes the check against the President harder, then leaves play.
    @Test
    void acceptProphecyRaisesTheDifficultyAgainstThePresident() {
        president(GAIUS_BALTAR);
        ops.titles().putInPlay(QuorumCard.ACCEPT_PROPHECY, null);

        execute(new AdmiralsQuartersEvent(1, 3));

        assertEquals(2, game.activeSkillCheck().difficultyAdjustment());
        assertEquals(List.of(), game.quorumCardsInPlay());
        assertSkillCheckTriggered(ADMIRALS_QUARTERS);
    }

    @Test
    void acceptProphecyStaysAgainstAnotherCharacter() {
        president(WILLIAM_ADAMA);
        ops.titles().putInPlay(QuorumCard.ACCEPT_PROPHECY, null);

        execute(new AdmiralsQuartersEvent(1, 3));

        assertEquals(0, game.activeSkillCheck().difficultyAdjustment());
        assertTrue(game.inPlay(QuorumCard.ACCEPT_PROPHECY));
        assertSkillCheckTriggered(ADMIRALS_QUARTERS);
    }

    // The Arbitrator is asked as the location is activated, before the check.
    @Test
    void theArbitratorMayChangeTheDifficulty() {
        ops.titles().putInPlay(QuorumCard.ASSIGN_ARBITRATOR, 3);

        execute(new AdmiralsQuartersEvent(1, 3));

        assertFollowup(FollowupUtils.allOf(
                one(new UseArbitratorEvent(3, 3), new UseArbitratorEvent(3, -3), new NoOpEvent(3)),
                FollowupUtils.beforeSkillCheckFollowups(game)));
    }

    @Test
    void shouldNotChooseACharacterAlreadyInTheBrig() {
        moveTo(BRIG, GAIUS_BALTAR);
        assertInvalid(new AdmiralsQuartersEvent(1, 3));
    }

    @Test
    void shouldNotChooseACylon() {
        player(2).character(CAVIL);
        assertInvalid(new AdmiralsQuartersEvent(1, 2));
    }

    @Test
    void shouldNotChooseSomeoneWhoIsNotPlaying() {
        assertInvalid(new AdmiralsQuartersEvent(1, 4));
    }
}
