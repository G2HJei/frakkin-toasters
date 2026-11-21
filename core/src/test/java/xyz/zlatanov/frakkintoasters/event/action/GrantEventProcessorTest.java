package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.ActingPlayerChanged;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BUILD_NUKE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.PREVENTIVE_POLICY;

class GrantEventProcessorTest extends EventTestHarness<GrantEvent> {

    // The choice is made as the game stands when the grant runs: the pilot the Hangar Deck just launched.
    @Test
    void aGrantedActionIsOfferedWhenItRuns() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.player(1).pilot(viperAt(GALACTICA_SPACE_4_OCLOCK).id());
        player(1).gainSkillCards(new SkillCard(6, BUILD_NUKE));

        execute(new GrantEvent(1, ACTION, GrantedBy.HANGAR_DECK));

        assertFollowup(one(new PlayerDecisionEvent<>(1, ActivateViperEvent.class),
                new PlayerDecisionEvent<>(1, ActionEvent.class),
                new NoOpEvent(1)));
        assertEquals(List.of(), actingPlayerChanges()); // he was the acting player already
    }

    @Test
    void anotherPlayerBecomesTheActingPlayer() {
        roslinAtThePresidentsOffice();

        execute(new GrantEvent(2, ACTION, GrantedBy.EXECUTIVE_ORDER));

        assertFollowup(one(new PlayerDecisionEvent<>(2, ActionEvent.class), new NoOpEvent(2)));
        assertEquals(2, game.turn().actingPlayerNumber());
        assertEquals(List.of(new ActingPlayerChanged(2)), actingPlayerChanges());
    }

    // A "Movement:" ability can't be used when another effect lets the player move (rule 8.3.5).
    @Test
    void aGrantedMoveIsAPlainMove() {
        roslinAtThePresidentsOffice();
        player(2).gainSkillCards(new SkillCard(3, PREVENTIVE_POLICY));

        execute(new GrantEvent(2, MOVE, GrantedBy.EXECUTIVE_ORDER));

        assertFollowup(one(new PlayerDecisionEvent<>(2, MoveEvent.class), new NoOpEvent(2)));
    }

    @Test
    void aMoveOrAnAction() {
        roslinAtThePresidentsOffice();

        execute(new GrantEvent(2, MOVE_OR_ACTION, GrantedBy.STATE_OF_EMERGENCY));

        assertFollowup(one(new PlayerDecisionEvent<>(2, MoveEvent.class),
                new PlayerDecisionEvent<>(2, ActionEvent.class),
                new NoOpEvent(2)));
    }

    // A character in the "Brig" can't move, so the grant offers him only his action.
    @Test
    void noMoveFromTheBrig() {
        player(2).character(LAURA_ROSLIN);
        moveTo(BRIG, LAURA_ROSLIN);

        execute(new GrantEvent(2, MOVE_OR_ACTION, GrantedBy.STATE_OF_EMERGENCY));

        assertFollowup(one(new PlayerDecisionEvent<>(2, ActionEvent.class), new NoOpEvent(2)));
    }

    // A Cylon player may not benefit from an "Executive Order": a target who revealed himself loses his second action
    // (A2.3.8.4).
    @Test
    void anExecutiveOrderGivesARevealedCylonNothing() {
        player(1).character(WILLIAM_ADAMA);
        moveTo(COMMAND, WILLIAM_ADAMA);
        revealCylon();

        execute(new GrantEvent(1, ACTION, GrantedBy.EXECUTIVE_ORDER));

        assertEquals(List.of(), actingPlayerChanges());
    }

    // An "Executive Order" stops when the current player is executed or revealed (A2.3.8.6).
    @Test
    void anExecutiveOrderStopsWhenTheTurnEndedEarly() {
        roslinAtThePresidentsOffice();
        ops.endTurnEarly();

        execute(new GrantEvent(2, ACTION, GrantedBy.EXECUTIVE_ORDER));
    }

    // A "State of Emergency" resolves completely (A2.3.8.6).
    @Test
    void aStateOfEmergencyGoesOnWhenTheTurnEndedEarly() {
        roslinAtThePresidentsOffice();
        ops.endTurnEarly();

        execute(new GrantEvent(2, MOVE_OR_ACTION, GrantedBy.STATE_OF_EMERGENCY));

        assertFollowup(one(new PlayerDecisionEvent<>(2, MoveEvent.class),
                new PlayerDecisionEvent<>(2, ActionEvent.class),
                new NoOpEvent(2)));
    }

    // A "State of Emergency" lets Cylon players move or act too (A2.3.8.6).
    @Test
    void aStateOfEmergencyIncludesARevealedCylon() {
        player(1).character(WILLIAM_ADAMA);
        moveTo(RESURRECTION_SHIP, WILLIAM_ADAMA);
        revealCylon();

        execute(new GrantEvent(1, MOVE_OR_ACTION, GrantedBy.STATE_OF_EMERGENCY));

        assertFollowup(one(new PlayerDecisionEvent<>(1, MoveEvent.class),
                new PlayerDecisionEvent<>(1, ActionEvent.class),
                new NoOpEvent(1)));
    }

    @Test
    void theGrantNamesAPlayer() {
        assertInvalid(new GrantEvent(9, ACTION, GrantedBy.HANGAR_DECK));
    }

    private void roslinAtThePresidentsOffice() {
        player(2).character(LAURA_ROSLIN);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
    }

    private List<ActingPlayerChanged> actingPlayerChanges() {
        return journal.facts().stream()
                .filter(ActingPlayerChanged.class::isInstance)
                .map(ActingPlayerChanged.class::cast)
                .toList();
    }

    // Expert Pilot's second action belongs to her Action step, which is over.
    @Test
    void expertPilotsSecondActionEndsWithTheTurn() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.endTurnEarly();

        execute(new GrantEvent(1, ACTION, GrantEvent.GrantedBy.EXPERT_PILOT));
    }

    // "Aggressive Tactics": the actions of his own turn, which ended.
    @Test
    void anAggressiveTacticsGrantDoesNothingOnceTheTurnEndedEarly() {
        player(1).character(CAVIL);
        moveTo(CYLON_FLEET, CAVIL);
        ops.endTurnEarly();

        execute(new GrantEvent(1, ACTION, GrantedBy.AGGRESSIVE_TACTICS));
    }
}
