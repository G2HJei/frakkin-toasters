package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.action.Actions;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_DOWN_TO_3;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.PANIC;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class ReturnToResurrectionShipEventProcessorTest extends EventTestHarness<ReturnToResurrectionShipEvent> {

    @BeforeEach
    void setUp() {
        setUpGame(4);
        player(1).character(HELENA_CAIN);
        player(4).character(SHARON_ATHENA_AGATHON);
        ops.player(4).infiltrateGalactica(HANGAR_DECK);
    }

    // "End your Infiltration and move to the "Resurrection Ship"", keeping the miracle token (rule 4.3.2).
    @Test
    void shouldEndTheInfiltrationAtTheResurrectionShip() {
        assertTrue(Actions.canTakeOtherAction(game, player(4)));

        execute(new ReturnToResurrectionShipEvent(4));

        assertFalse(player(4).isInfiltrating());
        assertEquals(RESURRECTION_SHIP, locate(SHARON_ATHENA_AGATHON));
        assertTrue(player(4).hasMiracleToken());
    }

    // He "must discard any Quorum Cards he has been given (without effect) and any Mutiny cards he holds".
    @Test
    void shouldDiscardTheQuorumCardsGivenToHimAndHisMutinyCards() {
        ops.titles().putInPlay(QuorumCard.ASSIGN_MISSION_SPECIALIST, 4);
        player(4).mutinyCards().addOnTop(PANIC);

        execute(new ReturnToResurrectionShipEvent(4));

        assertEquals(List.of(), game.quorumCardsInPlay());
        assertEquals(List.of(QuorumCard.ASSIGN_MISSION_SPECIALIST), quorumDeck.discardedCards());
        assertTrue(player(4).mutinyCards().isEmpty());
        assertEquals(List.of(PANIC), mutinyDeck.discardedCards());
    }

    // "If you were in the "Brig", you must then discard down to 3 cards."
    @Test
    void shouldDiscardDownToThreeSkillCardsFromTheBrig() {
        moveTo(BRIG, SHARON_ATHENA_AGATHON);
        player(4).gainSkillCards(new SkillCard(1, EVASIVE_MANOEUVRES), new SkillCard(2, LAUNCH_SCOUT),
                new SkillCard(3, REPAIR), new SkillCard(4, REPAIR));

        execute(new ReturnToResurrectionShipEvent(4));

        assertEquals(RESURRECTION_SHIP, locate(SHARON_ATHENA_AGATHON));
        assertFollowup(new PlayerDecisionEvent<>(4, DiscardSkillCardsEvent.class, DISCARD_DOWN_TO_3));
    }

    @Test
    void shouldNotDiscardFromTheBrigWithThreeSkillCardsOrFewer() {
        moveTo(BRIG, SHARON_ATHENA_AGATHON);
        player(4).gainSkillCards(new SkillCard(1, EVASIVE_MANOEUVRES), new SkillCard(2, LAUNCH_SCOUT),
                new SkillCard(3, REPAIR));

        execute(new ReturnToResurrectionShipEvent(4));

        assertEquals(3, player(4).skillCards().size());
    }

    // Outside the "Brig" he keeps his hand.
    @Test
    void shouldKeepTheSkillCardsOutsideTheBrig() {
        player(4).gainSkillCards(new SkillCard(1, EVASIVE_MANOEUVRES), new SkillCard(2, LAUNCH_SCOUT),
                new SkillCard(3, REPAIR), new SkillCard(4, REPAIR));

        execute(new ReturnToResurrectionShipEvent(4));

        assertEquals(4, player(4).skillCards().size());
    }

    @Test
    void shouldRejectACylonLeaderWhoIsNotInfiltrating() {
        ops.player(4).endInfiltration();

        assertFalse(ReturnToResurrectionShipEventProcessor.canReturn(game, player(4)));
        assertInvalid(new ReturnToResurrectionShipEvent(4));
    }

    @Test
    void shouldRejectAHuman() {
        moveTo(COMMAND, HELENA_CAIN);

        assertInvalid(new ReturnToResurrectionShipEvent(1));
    }

    // Meticulous: Aaron Doral may go to another Cylon location instead, once per game.
    @Test
    void aaronDoralMayGoToAnotherCylonLocation() {
        player(2).character(AARON_DORAL);
        ops.player(2).infiltrateGalactica(COMMAND);

        execute(new ReturnToResurrectionShipEvent(2));

        assertFollowup(xyz.zlatanov.frakkintoasters.event.Followup.one(
                new xyz.zlatanov.frakkintoasters.event.action.character.MeticulousEvent(2, RESURRECTION_SHIP),
                new xyz.zlatanov.frakkintoasters.event.action.character.MeticulousEvent(2, CAPRICA),
                new xyz.zlatanov.frakkintoasters.event.action.character.MeticulousEvent(2, CYLON_FLEET),
                new xyz.zlatanov.frakkintoasters.event.action.character.MeticulousEvent(2, HUMAN_FLEET),
                new xyz.zlatanov.frakkintoasters.event.action.character.MeticulousEvent(2, BASESTAR_BRIDGE)));
    }

    // Leoben Conoy's "Clouded": the return is a move he chooses, so not while another character is there.
    @Test
    void shouldNotReturnToAnOccupiedResurrectionShipWhenClouded() {
        player(2).character(LEOBEN_CONOY);
        ops.player(2).infiltrateGalactica(COMMAND);
        moveTo(RESURRECTION_SHIP, HELENA_CAIN);

        assertInvalid(new ReturnToResurrectionShipEvent(2));
    }
}
