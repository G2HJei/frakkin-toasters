package xyz.zlatanov.frakkintoasters.event.crisis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.*;

// The players a title holder can pick on a Crisis Card, so that he is only asked when he has an answer to give.
class ChooseCrisisTargetLegalTargetsTest extends TestHarness {

    private static final int CURRENT   = 1;
    private static final int PRESIDENT = 2;
    private static final int ADMIRAL   = 3;
    private static final int CAG       = 4;

    @BeforeEach
    void setUp() {
        setUpGame(4);
        player(CURRENT).character(FELIX_GAETA);
        player(PRESIDENT).character(LAURA_ROSLIN);
        player(ADMIRAL).character(WILLIAM_ADAMA);
        player(CAG).character(LEE_APOLLO_ADAMA);
        moveTo(COMMAND, FELIX_GAETA);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(HANGAR_DECK, LEE_APOLLO_ADAMA);
        president(LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);
        cag(LEE_APOLLO_ADAMA);
    }

    // "The Admiral chooses another player to gain 1 miracle token." (rules 4.3.1 to 4.3.3)
    @Test
    void shouldOnlyOfferPlayersWithoutAMiracleToken() {
        player(CAG).exhaustMiracleToken();
        player(CURRENT).exhaustMiracleToken();

        assertEquals(List.of(CURRENT, CAG), legalTargets(AN_AMBITIOUS_OPERATION));
    }

    @Test
    void shouldNotOfferTheAdmiralHimself() {
        player(ADMIRAL).exhaustMiracleToken();

        assertEquals(List.of(), legalTargets(AN_AMBITIOUS_OPERATION));
    }

    @Test
    void shouldNotOfferARevealedCylonAMiracleToken() {
        revealCylon();
        player(CURRENT).exhaustMiracleToken();

        assertEquals(List.of(), legalTargets(AN_AMBITIOUS_OPERATION));
    }

    // "another player to send to "Sickbay"" (rule 9.3.2)
    @Test
    void shouldNotInterrogateSomeoneWhoIsInSickbayAlready() {
        moveTo(SICKBAY, LEE_APOLLO_ADAMA);

        assertEquals(List.of(CURRENT, PRESIDENT), legalTargets(INTERROGATION));
    }

    // "or the current player is executed": a revealed Cylon ignores it (rule 6.1.2.1).
    @Test
    void shouldOfferTheExecutionOfAHumanCurrentPlayer() {
        assertEquals(Arrays.asList(CURRENT, ADMIRAL, CAG, null), legalTargets(THE_CIRCLE));
    }

    @Test
    void shouldNotOfferTheExecutionOfARevealedCylon() {
        revealCylon();

        assertEquals(List.of(ADMIRAL, CAG), legalTargets(THE_CIRCLE));
        assertFalse(legalTargets(THE_CIRCLE).stream().anyMatch(Objects::isNull));
    }

    @Test
    void shouldOnlyOfferTheBrigWhenThePresidentIsTheAdmiral() {
        admiral(LAURA_ROSLIN);

        assertEquals(Arrays.asList((Integer) null), legalTargets(REQUESTED_RESIGNATION));
    }

    @Test
    void shouldOfferTheAdmiralTheTitleOrTheBrig() {
        assertEquals(Arrays.asList(ADMIRAL, null), legalTargets(REQUESTED_RESIGNATION));
    }

    private List<Integer> legalTargets(xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard card) {
        return ChooseCrisisTargetEventProcessor.legalTargets(game, card);
    }
}
