package xyz.zlatanov.frakkintoasters.event.turn;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.MovementAbilityEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.GameStep.MOVEMENT;
import static xyz.zlatanov.frakkintoasters.state.GameStep.SETUP;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ADMINISTRATION;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.Location.CAPRICA;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAPRICA_SIX;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARL_HELO_AGATHON;
import static xyz.zlatanov.frakkintoasters.state.character.Character.ROMO_LAMPKIN;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BUILD_NUKE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.PREVENTIVE_POLICY;

class MovementStepEventProcessorTest extends EventTestHarness<MovementStepEvent> {

    private static final SkillCard PREVENTIVE_POLICY_3 = new SkillCard(3, PREVENTIVE_POLICY);

    // "the current player may move his character token to another location if he wishes" (rule 8.3)
    @Test
    void theCurrentPlayerMayMove() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);

        execute(new MovementStepEvent());

        assertEquals(MOVEMENT, game.turn().step());
        assertFollowup(one(new PlayerDecisionEvent<>(1, MoveEvent.class), new NoOpEvent(1)));
    }

    // "Characters in the "Brig" may not move" (rule 9.2)
    @Test
    void aCharacterInTheBrigCantMove() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(BRIG, KARA_STARBUCK_THRACE);

        execute(new MovementStepEvent());

        assertEquals(MOVEMENT, game.turn().step());
    }

    // A "Movement:" ability is used instead of moving (rule 8.3.5).
    @Test
    void aMovementCardIsOfferedBesideTheMove() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(PREVENTIVE_POLICY_3);

        execute(new MovementStepEvent());

        assertFollowup(one(new PlayerDecisionEvent<>(1, MoveEvent.class),
                new PlayerDecisionEvent<>(1, MovementAbilityEvent.class),
                new NoOpEvent(1)));
    }

    // Movement abilities may be used in the "Brig" (rule 10.4.1).
    @Test
    void aCharacterInTheBrigMayUseAMovementCard() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(BRIG, KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(PREVENTIVE_POLICY_3);

        execute(new MovementStepEvent());

        assertFollowup(one(new PlayerDecisionEvent<>(1, MovementAbilityEvent.class), new NoOpEvent(1)));
    }

    // "Action:" cards aren't offered in the Movement step.
    @Test
    void anActionCardIsNotAMovement() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(BRIG, KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(new SkillCard(6, BUILD_NUKE));

        execute(new MovementStepEvent());
    }

    @Test
    void aRevealedCylonCannotUseAMovementCard() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(BRIG, KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(PREVENTIVE_POLICY_3);
        revealCylon();

        execute(new MovementStepEvent());
    }

    @Test
    void strandedHeloCantMove() {
        player(1).character(KARL_HELO_AGATHON);

        execute(new MovementStepEvent());

        assertEquals(MOVEMENT, game.turn().step());
    }

    // The turn ended early, e.g. the current player was executed (rule 7.1), so the step doesn't happen.
    @Test
    void shouldNotHappenAfterTheTurnEndedEarly() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.endTurnEarly();

        execute(new MovementStepEvent());

        assertEquals(SETUP, game.turn().step());
    }

    // A "Movement:" ability of a Cylon Leader's sheet, also when he isn't Infiltrating (rule 6.1.3.2).
    @Test
    void aSheetMovementAbilityIsOfferedBesideTheMove() {
        player(1).character(CAPRICA_SIX);
        moveTo(CAPRICA, CAPRICA_SIX);

        execute(new MovementStepEvent());

        assertFollowup(one(new PlayerDecisionEvent<>(1, MoveEvent.class),
                new PlayerDecisionEvent<>(1, MovementAbilityEvent.class),
                new NoOpEvent(1)));
    }

    // "Kleptomania" acts once the step is over.
    @Test
    void theStepEndsForAnAbilityThatActsThen() {
        player(1).character(ROMO_LAMPKIN);
        moveTo(ADMINISTRATION, ROMO_LAMPKIN);

        execute(new MovementStepEvent());

        assertFollowup(all(one(new PlayerDecisionEvent<>(1, MoveEvent.class), new NoOpEvent(1)),
                single(new EndOfMovementStepEvent())));
    }
}
