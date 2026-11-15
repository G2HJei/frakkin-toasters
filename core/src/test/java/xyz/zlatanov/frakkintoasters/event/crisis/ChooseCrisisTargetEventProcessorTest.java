package xyz.zlatanov.frakkintoasters.event.crisis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;
import xyz.zlatanov.frakkintoasters.event.player.LookAtLoyaltyCardEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.*;

class ChooseCrisisTargetEventProcessorTest extends EventTestHarness<ChooseCrisisTargetEvent> {

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

    /* "The Admiral chooses another player to gain 1 miracle token." (rules 4.3.1 to 4.3.3) */

    @Test
    void shouldGiveTheChosenPlayerAMiracleToken() {
        player(CAG).exhaustMiracleToken();

        ops.playCrisisCard(AN_AMBITIOUS_OPERATION);
        execute(new ChooseCrisisTargetEvent(ADMIRAL, AN_AMBITIOUS_OPERATION, CAG));

        assertTrue(player(CAG).hasMiracleToken());
    }

    @Test
    void shouldNotGiveAPlayerWhoHasOneAnother() {
        player(CAG).exhaustMiracleToken();

        ops.playCrisisCard(AN_AMBITIOUS_OPERATION);
        assertInvalid(new ChooseCrisisTargetEvent(ADMIRAL, AN_AMBITIOUS_OPERATION, PRESIDENT));
    }

    /* "The current player draws 1 Mutiny Card. Then, he chooses a player to draw 1 Mutiny Card." */

    @Test
    void shouldLetThePlayerDrawAMutinyCard() {
        ops.playCrisisCard(SECRET_MEETINGS);
        execute(new ChooseCrisisTargetEvent(CURRENT, SECRET_MEETINGS, CAG));

        assertFollowup(new DrawMutinyCardEvent(CAG));
    }

    @Test
    void shouldOnlyLetTheChooserChoose() {
        ops.playCrisisCard(SECRET_MEETINGS);
        assertInvalid(new ChooseCrisisTargetEvent(CAG, SECRET_MEETINGS, CURRENT));
    }

    /* "The Admiral chooses another player to send to "Sickbay." The Admiral may then look at 1 of that character's Loyalty
    Cards at random." */

    @Test
    void shouldSendTheTargetToSickbayAndLetTheAdmiralLook() {
        ops.playCrisisCard(INTERROGATION);
        execute(new ChooseCrisisTargetEvent(ADMIRAL, INTERROGATION, CAG));

        assertFollowup(all(single(new SendToSickbayEvent(CAG)),
                one(new LookAtLoyaltyCardEvent(ADMIRAL, CAG), new NoOpEvent(ADMIRAL))));
    }

    /* "You must choose another player to receive the President title or the current player is executed." */

    @Test
    void shouldGiveThePresidentTitleToTheChosenPlayer() {
        ops.playCrisisCard(THE_CIRCLE);
        execute(new ChooseCrisisTargetEvent(PRESIDENT, THE_CIRCLE, CURRENT));

        assertFollowup(new MakePresidentEvent(FELIX_GAETA));
    }

    @Test
    void shouldExecuteTheCurrentPlayerWhenNobodyIsChosen() {
        ops.playCrisisCard(THE_CIRCLE);
        execute(new ChooseCrisisTargetEvent(PRESIDENT, THE_CIRCLE, null));

        assertFollowup(new ExecuteEvent(CURRENT));
    }

    /* "The President may choose to give the President title to the Admiral, or move to the "Brig" location." */

    @Test
    void shouldGiveTheTitleToTheAdmiral() {
        ops.playCrisisCard(REQUESTED_RESIGNATION);
        execute(new ChooseCrisisTargetEvent(PRESIDENT, REQUESTED_RESIGNATION, ADMIRAL));

        assertFollowup(new MakePresidentEvent(WILLIAM_ADAMA));
    }

    @Test
    void shouldMoveThePresidentToTheBrig() {
        ops.playCrisisCard(REQUESTED_RESIGNATION);
        execute(new ChooseCrisisTargetEvent(PRESIDENT, REQUESTED_RESIGNATION, null));

        assertFollowup(new SendToBrigEvent(PRESIDENT));
    }

    // The President is asked about "The Circle", so she can't answer as if it were "Requested Resignation", which she
    // chooses for too.
    @Test
    void shouldOnlyAnswerForTheCardBeingResolved() {
        ops.playCrisisCard(THE_CIRCLE);

        assertInvalid(new ChooseCrisisTargetEvent(PRESIDENT, REQUESTED_RESIGNATION, null));
    }

    // The card being resolved is the one played last, e.g. after "Fulfiller of Prophecy" returned to the Crisis step.
    @Test
    void shouldNotAnswerForAnEarlierCardOfTheTurn() {
        ops.playCrisisCard(THE_CIRCLE);
        ops.playCrisisCard(REQUESTED_RESIGNATION);

        assertInvalid(new ChooseCrisisTargetEvent(PRESIDENT, THE_CIRCLE, null));
    }

    @Test
    void shouldNotAnswerWithoutACardBeingResolved() {
        assertInvalid(new ChooseCrisisTargetEvent(PRESIDENT, THE_CIRCLE, null));
    }

    @Test
    void shouldNotTakeACardThatHasNoTargetToChoose() {
        ops.playCrisisCard(FOOD_SHORTAGE);
        assertInvalid(new ChooseCrisisTargetEvent(PRESIDENT, FOOD_SHORTAGE, CURRENT));
    }
}
