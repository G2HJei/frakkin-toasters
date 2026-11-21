package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus.Finished;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.BuildNukeEvent;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeOpened;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.CardsSeen;
import xyz.zlatanov.frakkintoasters.event.AttackCylonShipEvent;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;
import xyz.zlatanov.frakkintoasters.event.action.GrantedViperActivationEvent;
import xyz.zlatanov.frakkintoasters.event.action.ViperActivationRoundEvent;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy;
import xyz.zlatanov.frakkintoasters.event.action.MovementAbilityEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.CombatVeteranEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.ConsolidatePowerEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.CriticalSituationEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.ExecutiveOrderEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.LaunchScoutEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.MaximumFirepowerEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.RaptorSpecialistEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.RepairEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.DamageLocationEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.MoveCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.deck.LookAtTopCardEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.PreventivePolicyEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.StateOfEmergencyEvent;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.HangarDeckEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnBottomEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.event.turn.ActionStepEvent;
import xyz.zlatanov.frakkintoasters.event.turn.MovementStepEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.ViperActivation.attack;
import static xyz.zlatanov.frakkintoasters.event.ViperActivation.launch;
import static xyz.zlatanov.frakkintoasters.event.ViperActivation.move;
import static xyz.zlatanov.frakkintoasters.event.action.skillcard.RaptorSpecialistEvent.Choice.BUILD_ASSAULT_RAPTOR;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.CRISIS;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.MOVE;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.PILOTING;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

/**
 * Skill Cards played for their "Action:" or "Movement:" through {@code submit} and {@code choose} alone, restoring the
 * game at every pause. The cards are dealt with {@link GiveSkillCardEvent}, which a seeded game can't choose.
 */
class SkillCardActionsTest {

    // Only the end of the game empties the agenda, so the script ends it.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private static final SkillCard CONSOLIDATE_POWER_1 = new SkillCard(1, CONSOLIDATE_POWER);
    private static final SkillCard BUILD_NUKE_6        = new SkillCard(6, BUILD_NUKE);
    private static final SkillCard CRITICAL_SITUATION_3 = new SkillCard(3, CRITICAL_SITUATION);
    private static final SkillCard PREVENTIVE_POLICY_3  = new SkillCard(3, PREVENTIVE_POLICY);
    private static final SkillCard EXECUTIVE_ORDER_1    = new SkillCard(1, EXECUTIVE_ORDER);
    private static final SkillCard STATE_OF_EMERGENCY_6 = new SkillCard(6, STATE_OF_EMERGENCY);
    private static final SkillCard COMBAT_VETERAN_3     = new SkillCard(3, COMBAT_VETERAN);
    private static final SkillCard MAXIMUM_FIREPOWER_3  = new SkillCard(3, MAXIMUM_FIREPOWER);
    private static final SkillCard RAPTOR_SPECIALIST_3  = new SkillCard(3, RAPTOR_SPECIALIST);
    private static final SkillCard LAUNCH_SCOUT_1       = new SkillCard(1, LAUNCH_SCOUT);
    private static final SkillCard REPAIR_1             = new SkillCard(1, REPAIR);

    // The choices of the target of an "Executive Order", player 2.
    private static final Followup MOVE_THEN_ACT = all(new GrantEvent(2, MOVE, GrantedBy.EXECUTIVE_ORDER),
            new GrantEvent(2, ACTION, GrantedBy.EXECUTIVE_ORDER));
    private static final Followup ACT_TWICE     = all(new GrantEvent(2, ACTION, GrantedBy.EXECUTIVE_ORDER),
            new GrantEvent(2, ACTION, GrantedBy.EXECUTIVE_ORDER));

    private GameEngine engine;

    @Test
    void consolidatePowerDrawsTwoCardsOfAnyColor() {
        start(all(setUp(WILLIAM_ADAMA), single(new GiveSkillCardEvent(1, CONSOLIDATE_POWER_1)), single(new ActionStepEvent()),
                CYLONS_WIN));

        act(1);
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, ActionEvent.class)));
        play(engine -> engine.submit(1, new ConsolidatePowerEvent(1, CONSOLIDATE_POWER_1)));

        // outside William Adama's skill set
        play(engine -> engine.submit(1, new DrawSkillCardsEvent(1, Map.of(PILOTING, 1, TREACHERY, 1))));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(Set.of(PILOTING, TREACHERY), colors(engine.view().player(1).skillCards().cards()));
        assertEquals(List.of(CONSOLIDATE_POWER), engine.view().turn().playedSkillCards());
    }

    // A character in "Sickbay" has no location action, but may play a card.
    @Test
    void buildNukeFromSickbay() {
        start(all(setUp(WILLIAM_ADAMA), all(new GiveSkillCardEvent(1, BUILD_NUKE_6), new SendToSickbayEvent(1)),
                single(new ActionStepEvent()), CYLONS_WIN));

        assertEquals(SICKBAY, engine.view().locate(WILLIAM_ADAMA).orElseThrow());
        act(1);
        play(engine -> engine.submit(1, new BuildNukeEvent(1, BUILD_NUKE_6)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(3, engine.view().nukes());
        assertEquals(List.of(), engine.view().player(1).skillCards().cards());
    }

    // "Launch yourself in a viper. You may then take 1 more action": the launched pilot plays a card instead of
    // activating his viper (rule 11.2.1).
    @Test
    void theHangarDecksExtraActionMayBeACard() {
        start(all(setUp(KARA_STARBUCK_THRACE), single(new GiveSkillCardEvent(1, BUILD_NUKE_6)), single(new ActionStepEvent()),
                CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new HangarDeckEvent(1)));
        play(engine -> engine.submit(1, new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_4_OCLOCK, KARA_STARBUCK_THRACE, null)));

        assertWaitingFor(new AwaitingChoice(1, Set.of(
                single(new PlayerDecisionEvent<>(1, ActivateViperEvent.class)),
                single(new PlayerDecisionEvent<>(1, ActionEvent.class)),
                single(new NoOpEvent(1)))));
        act(1);
        play(engine -> engine.submit(1, new BuildNukeEvent(1, BUILD_NUKE_6)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(3, engine.view().nukes());
        assertTrue(engine.view().boards().galactica().pilotedFighter(KARA_STARBUCK_THRACE).isPresent());
    }

    // A "Movement:" card instead of moving: the player takes an action in his Movement step.
    @Test
    void criticalSituationTakesAnActionInsteadOfMoving() {
        start(all(setUp(WILLIAM_ADAMA),
                all(new GiveSkillCardEvent(1, CRITICAL_SITUATION_3), new GiveSkillCardEvent(1, BUILD_NUKE_6)),
                single(new MovementStepEvent()), CYLONS_WIN));

        assertWaitingFor(new AwaitingChoice(1, Set.of(
                single(new PlayerDecisionEvent<>(1, MoveEvent.class)),
                single(new PlayerDecisionEvent<>(1, MovementAbilityEvent.class)),
                single(new NoOpEvent(1)))));
        choose(1, single(new PlayerDecisionEvent<>(1, MovementAbilityEvent.class)));
        play(engine -> engine.submit(1, new CriticalSituationEvent(1, CRITICAL_SITUATION_3)));
        act(1);
        play(engine -> engine.submit(1, new BuildNukeEvent(1, BUILD_NUKE_6)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(3, engine.view().nukes());
        assertEquals(List.of(CRITICAL_SITUATION, BUILD_NUKE), engine.view().turn().playedSkillCards());
    }

    @Test
    void preventivePolicyReducesTheNextLoss() {
        start(all(setUp(WILLIAM_ADAMA), single(new GiveSkillCardEvent(1, PREVENTIVE_POLICY_3)),
                single(new MovementStepEvent()), single(new LoseResourceEvent(FOOD, 2)), CYLONS_WIN));

        choose(1, single(new PlayerDecisionEvent<>(1, MovementAbilityEvent.class)));
        play(engine -> engine.submit(1, new PreventivePolicyEvent(1, PREVENTIVE_POLICY_3, FOOD)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(7, engine.view().boards().galactica().food());
    }

    // The target acts twice: the grants make him the acting player, and the current player acts again at the end.
    @Test
    void executiveOrderLetsTheTargetActTwice() {
        start(all(setUp(WILLIAM_ADAMA),
                all(new GiveSkillCardEvent(1, EXECUTIVE_ORDER_1), new GiveSkillCardEvent(2, BUILD_NUKE_6),
                        new GiveSkillCardEvent(2, CONSOLIDATE_POWER_1)),
                single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new ExecutiveOrderEvent(1, EXECUTIVE_ORDER_1, 2)));
        assertWaitingFor(new AwaitingChoice(2, Set.of(MOVE_THEN_ACT, ACT_TWICE)));

        choose(2, ACT_TWICE);
        assertEquals(2, engine.view().turn().actingPlayerNumber());
        act(2);
        play(engine -> engine.submit(2, new BuildNukeEvent(2, BUILD_NUKE_6)));
        act(2);
        play(engine -> engine.submit(2, new ConsolidatePowerEvent(2, CONSOLIDATE_POWER_1)));
        play(engine -> engine.submit(2, new DrawSkillCardsEvent(2, Map.of(PILOTING, 2))));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(1, engine.view().turn().actingPlayerNumber());
        assertEquals(3, engine.view().nukes());
        assertEquals(List.of(EXECUTIVE_ORDER, BUILD_NUKE, CONSOLIDATE_POWER), engine.view().turn().playedSkillCards());
    }

    @Test
    void executiveOrderLetsTheTargetMoveThenAct() {
        start(all(setUp(WILLIAM_ADAMA),
                all(new GiveSkillCardEvent(1, EXECUTIVE_ORDER_1), new GiveSkillCardEvent(2, BUILD_NUKE_6)),
                single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new ExecutiveOrderEvent(1, EXECUTIVE_ORDER_1, 2)));
        choose(2, MOVE_THEN_ACT);
        choose(2, single(new PlayerDecisionEvent<>(2, MoveEvent.class)));
        play(engine -> engine.submit(2, new MoveEvent(2, ADMINISTRATION, null)));
        act(2);
        play(engine -> engine.submit(2, new BuildNukeEvent(2, BUILD_NUKE_6)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(ADMINISTRATION, engine.view().locate(LAURA_ROSLIN).orElseThrow());
        assertEquals(3, engine.view().nukes());
    }

    // The target of an "Executive Order" plays a "State of Emergency": each grant makes its player the acting player,
    // so the target acts again afterwards, and the current player once both are over.
    @Test
    void grantsNest() {
        start(all(setUp(WILLIAM_ADAMA),
                all(new GiveSkillCardEvent(1, EXECUTIVE_ORDER_1), new GiveSkillCardEvent(2, STATE_OF_EMERGENCY_6)),
                single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new ExecutiveOrderEvent(1, EXECUTIVE_ORDER_1, 2)));
        choose(2, ACT_TWICE);
        act(2);
        play(engine -> engine.submit(2, new StateOfEmergencyEvent(2, STATE_OF_EMERGENCY_6)));
        for (val player : List.of(2, 3, 1)) { // clockwise from the player of the card
            assertEquals(player, engine.view().turn().actingPlayerNumber());
            pass(player);
        }
        assertEquals(2, engine.view().turn().actingPlayerNumber()); // his second action
        pass(2);

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(1, engine.view().turn().actingPlayerNumber());
        assertEquals(7, engine.view().boards().galactica().food());
    }

    // The rounds follow the viper the launch took, and offer its legal activations as options.
    @Test
    void combatVeteranLaunchesAViperAndMovesIt() {
        start(all(setUp(WILLIAM_ADAMA), single(new GiveSkillCardEvent(1, COMBAT_VETERAN_3)), single(new ActionStepEvent()),
                CYLONS_WIN));
        int launched = engine.view().boards().galactica().reserves().stream()
                .filter(Viper.class::isInstance).findFirst().orElseThrow().id();

        act(1);
        play(engine -> engine.submit(1, new CombatVeteranEvent(1, COMBAT_VETERAN_3, launch(VIPER, GALACTICA_SPACE_4_OCLOCK))));
        assertWaitingFor(new AwaitingChoice(1, Set.of(
                all(new GrantedViperActivationEvent(1, move(launched, GALACTICA_SPACE_2_OCLOCK)),
                        new ViperActivationRoundEvent(1, launched, 1, false)),
                all(new GrantedViperActivationEvent(1, move(launched, GALACTICA_SPACE_6_OCLOCK)),
                        new ViperActivationRoundEvent(1, launched, 1, false)),
                single(new NoOpEvent(1)))));
        choose(1, all(new GrantedViperActivationEvent(1, move(launched, GALACTICA_SPACE_6_OCLOCK)),
                new ViperActivationRoundEvent(1, launched, 1, false)));
        choose(1, single(new GrantedViperActivationEvent(1, move(launched, GALACTICA_SPACE_8_OCLOCK))));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(GALACTICA_SPACE_8_OCLOCK, engine.view().boards().galactica().shipsInSpace().entrySet().stream()
                .filter(entry -> entry.getKey().id() == launched).findFirst().orElseThrow().getValue());
    }

    // The launched pilot attacks with his action, and may stop after any attack.
    @Test
    void maximumFirepowerAttacksUntilThePlayerStops() {
        start(all(setUp(KARA_STARBUCK_THRACE),
                all(new GiveSkillCardEvent(1, MAXIMUM_FIREPOWER_3), new PlaceRaiderEvent(GALACTICA_SPACE_4_OCLOCK),
                        new PlaceRaiderEvent(GALACTICA_SPACE_4_OCLOCK)),
                single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new HangarDeckEvent(1)));
        play(engine -> engine.submit(1, new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_4_OCLOCK, KARA_STARBUCK_THRACE, null)));
        act(1);
        play(engine -> engine.submit(1, new MaximumFirepowerEvent(1, MAXIMUM_FIREPOWER_3)));
        val viper = engine.view().boards().galactica().pilotedFighter(KARA_STARBUCK_THRACE).orElseThrow().id();
        val raider = engine.view().boards().galactica().shipsIn(GALACTICA_SPACE_4_OCLOCK, Raider.class).getFirst().id();
        choose(1, all(new GrantedViperActivationEvent(1, attack(viper, raider)), new ViperActivationRoundEvent(1, viper, 3, true)));
        if (engine.pendingInput().isPresent()) {
            pass(1); // a raider is left to attack: the player stops
        }

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(1, engine.journal().entries().stream()
                .filter(entry -> entry instanceof ScopeOpened opened && opened.event() instanceof AttackCylonShipEvent)
                .count());
    }

    @Test
    void raptorSpecialistBuildsAnAssaultRaptor() {
        start(all(setUp(WILLIAM_ADAMA), single(new GiveSkillCardEvent(1, RAPTOR_SPECIALIST_3)), single(new ActionStepEvent()),
                CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new RaptorSpecialistEvent(1, RAPTOR_SPECIALIST_3, BUILD_ASSAULT_RAPTOR)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(2, engine.view().boards().galactica().reserves().stream().filter(AssaultRaptor.class::isInstance).count());
        assertEquals(2, engine.view().boards().galactica().supply().size());
    }

    // The die decides: on 3 or more the player looks at a card and puts it back, otherwise a raptor is destroyed.
    @Test
    void launchScoutLooksAtACardOrLosesARaptor() {
        start(all(setUp(WILLIAM_ADAMA), single(new GiveSkillCardEvent(1, LAUNCH_SCOUT_1)), single(new ActionStepEvent()),
                CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new LaunchScoutEvent(1, LAUNCH_SCOUT_1)));

        if (engine.view().turn().dieRoll().result() >= 3) {
            choose(1, single(new LookAtTopCardEvent(1, CRISIS)));
            val top = engine.journal().facts().stream()
                    .filter(CardsSeen.class::isInstance).map(CardsSeen.class::cast).toList().getLast();
            choose(1, single(new PlaceCrisisCardOnBottomEvent(1, (CrisisCard) top.cards().getFirst())));
            assertEquals(top.cards().getFirst(), ((Game) engine.view()).decks().crisis().cards().getLast());
        } else {
            assertEquals(3, engine.view().boards().galactica().reserves().stream().filter(Raptor.class::isInstance).count());
        }
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // A damaged location has no action (rule 11.7.2.2), but its character may repair it.
    @Test
    void repairALocationFromInside() {
        start(all(setUp(WILLIAM_ADAMA),
                all(new GiveSkillCardEvent(1, REPAIR_1), new DamageLocationEvent(COMMAND), new MoveCharacterEvent(WILLIAM_ADAMA, COMMAND)),
                single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new RepairEvent(1, REPAIR_1, List.of())));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertFalse(engine.view().boards().galactica().isDamaged(COMMAND));
    }

    // The engine accepts a card only where a decision of its timing is asked.
    @Test
    void aCardAnswersOnlyADecisionOfItsTiming() {
        start(all(setUp(WILLIAM_ADAMA), all(new GiveSkillCardEvent(1, CONSOLIDATE_POWER_1), new GiveSkillCardEvent(1, BUILD_NUKE_6)),
                single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new ConsolidatePowerEvent(1, CONSOLIDATE_POWER_1)));

        assertThrows(InvalidActionException.class, () -> engine.submit(1, new BuildNukeEvent(1, BUILD_NUKE_6)));
    }

    /* the game */

    // Player 1 is the current player, with an empty hand. Each of the three plays a character of another type, as the
    // selection allows (rule A3.7.2).
    private static Followup setUp(Character first) {
        return all(
                new SelectCharacterEvent(1, first),
                new SelectCharacterEvent(2, LAURA_ROSLIN),
                new SelectCharacterEvent(3, first == WILLIAM_ADAMA ? KARA_STARBUCK_THRACE : WILLIAM_ADAMA));
    }

    private void start(Followup script) {
        engine = new GameEngine(3, Randomness.seeded(42), new ScriptEvent(script));
        assertRestoresTheSameGame();
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

    // The player chooses to take an action.
    private void act(int player) {
        choose(player, single(new PlayerDecisionEvent<>(player, ActionEvent.class)));
    }

    private void pass(int player) {
        choose(player, single(new NoOpEvent(player)));
    }

    private void choose(int player, Followup option) {
        val choice = (AwaitingChoice) engine.pendingInput().orElseThrow();
        assertTrue(choice.options().contains(option), choice.options().toString());
        play(engine -> engine.choose(player, option));
    }

    /* assertions */

    private void assertWaitingFor(PendingInput expected) {
        assertEquals(EngineStatus.waiting(expected), engine.status(expected.player()));
    }

    private void assertRestoresTheSameGame() {
        val restored = GameEngine.restore(engine.journal());

        assertEquals(snapshot(engine), snapshot(restored));
        assertEquals(statuses(engine), statuses(restored));
    }

    private static Set<SkillCardColor> colors(List<SkillCard> cards) {
        return cards.stream().map(card -> card.type().color()).collect(toSet());
    }

    // The view is the game itself.
    private static GameSnapshot snapshot(GameEngine engine) {
        return GameSnapshot.of((Game) engine.view());
    }

    private static List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, 3).mapToObj(engine::status).toList();
    }
}
