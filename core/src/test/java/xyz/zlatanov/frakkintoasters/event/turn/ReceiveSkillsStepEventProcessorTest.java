package xyz.zlatanov.frakkintoasters.event.turn;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.*;
import static xyz.zlatanov.frakkintoasters.state.GameStep.RECEIVE_SKILLS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class ReceiveSkillsStepEventProcessorTest extends EventTestHarness<ReceiveSkillsStepEvent> {

    @Test
    void aHumanDrawsTheWholeSkillSet() {
        current(KARA_STARBUCK_THRACE, HANGAR_DECK);

        execute(new ReceiveSkillsStepEvent());

        assertEquals(RECEIVE_SKILLS, game.turn().step());
        assertDraws(DRAW_EXACTLY_5);
    }

    // "2 Politics, 1 Leadership, 1 Engineering"
    @Test
    void theCountComesFromTheSkillSet() {
        current(GAIUS_BALTAR, RESEARCH_LAB);

        execute(new ReceiveSkillsStepEvent());

        assertDraws(DRAW_EXACTLY_4);
    }

    // "Characters in "Sickbay", "Medical center" or on the "Resurrection Ship" draw only one card." (rule 8.2.2)
    @Test
    void aCharacterInSickbayDrawsOneCard() {
        current(KARA_STARBUCK_THRACE, SICKBAY);

        execute(new ReceiveSkillsStepEvent());

        assertDraws(DRAW_EXACTLY_1);
    }

    @Test
    void aRevealedCylonOnTheResurrectionShipDrawsOneCard() {
        current(KARA_STARBUCK_THRACE, RESURRECTION_SHIP);
        revealCylon();

        execute(new ReceiveSkillsStepEvent());

        assertDraws(DRAW_EXACTLY_1);
    }

    // "Revealed Cylon players only draw two Skill Cards on their turns." (rule 8.2.2.3)
    @Test
    void aRevealedCylonDrawsTwoCards() {
        current(KARA_STARBUCK_THRACE, CAPRICA);
        revealCylon();

        execute(new ReceiveSkillsStepEvent());

        assertDraws(DRAW_EXACTLY_2);
    }

    @Test
    void aCylonLeaderDrawsTheSkillSet() {
        current(CAVIL, CYLON_FLEET);

        execute(new ReceiveSkillsStepEvent());

        assertDraws(DRAW_EXACTLY_2);
    }

    // "An Infiltrating Cylon Leader may draw one extra Skill Card (from within his Skill set)" (rule 8.2.2.4)
    @Test
    void anInfiltratingCylonLeaderDrawsOneMore() {
        current(SHARON_ATHENA_AGATHON, HANGAR_DECK);
        player(1).infiltrateGalactica();

        execute(new ReceiveSkillsStepEvent());

        assertDraws(DRAW_EXACTLY_3);
    }

    @Test
    void strandedHeloDrawsToo() {
        player(1).character(KARL_HELO_AGATHON);

        execute(new ReceiveSkillsStepEvent());

        assertDraws(DRAW_EXACTLY_5);
    }

    private void current(Character character, Location location) {
        player(1).character(character);
        moveTo(location, character);
    }

    private void assertDraws(EventConstraint count) {
        assertFollowup(new PlayerDecisionEvent<>(1, DrawSkillCardsEvent.class, count));
    }
}
