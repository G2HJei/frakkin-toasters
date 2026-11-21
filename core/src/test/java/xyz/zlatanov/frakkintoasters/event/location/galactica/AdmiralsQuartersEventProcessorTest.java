package xyz.zlatanov.frakkintoasters.event.location.galactica;

import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import xyz.zlatanov.frakkintoasters.event.character.CylonHatredEvent;
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
        player(1).character(ELLEN_TIGH);
        moveTo(Location.ADMIRALS_QUARTERS, ELLEN_TIGH);
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
        president(ELLEN_TIGH);
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

    // "Insubordinate": the check is 3 easier when Kara Thrace is chosen.
    @Test
    void insubordinateMakesTheCheckEasierAgainstStarbuck() {
        player(2).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);

        execute(new AdmiralsQuartersEvent(1, 2));

        assertEquals(-3, game.activeSkillCheck().difficultyAdjustment());
        assertSkillCheckTriggered(ADMIRALS_QUARTERS);
    }

    // "Cylon Hatred": Saul Tigh is asked as the location is activated.
    @Test
    void cylonHatredIsOffered() {
        player(2).character(SAUL_TIGH);
        moveTo(COMMAND, SAUL_TIGH);

        execute(new AdmiralsQuartersEvent(1, 3));

        assertFollowup(allOf(one(new CylonHatredEvent(2), new NoOpEvent(2)), FollowupUtils.beforeSkillCheckFollowups(game)));
    }
}
