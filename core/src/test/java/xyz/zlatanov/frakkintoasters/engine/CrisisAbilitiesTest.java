package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.character.ChooseADifferentPathEvent;
import xyz.zlatanov.frakkintoasters.event.character.MoralCompassEvent;
import xyz.zlatanov.frakkintoasters.event.character.MysteriousIntuitionEvent;
import xyz.zlatanov.frakkintoasters.event.character.ReconEvent;
import xyz.zlatanov.frakkintoasters.event.character.ReligiousVisionsEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ResolveCrisisSectionEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextCagEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextPresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnBottomEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnTopEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayCardsBeforeSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.turn.CrisisStepEvent;
import xyz.zlatanov.frakkintoasters.event.turn.DiscardStepEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.view.DeckView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_EXACTLY_5;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.RIOTS_NO_JUMP;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.WATER_SABOTAGED;

/**
 * Player 1 is the current player. Each script ends the game after the step, as only the end of the game empties the
 * agenda.
 */
class CrisisAbilitiesTest {

    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private GameEngine engine;

    // "Moral Compass": Kat chooses the skill check of "Water Sabotaged", Karl "Helo" Agathon changes it to the bottom.
    @Test
    void heloChangesTheChoice() {
        start(setUp(List.of(LOUANNE_KAT_KATRAINE, LAURA_ROSLIN, KARL_HELO_AGATHON), WATER_SABOTAGED),
                single(new CrisisStepEvent()));
        val food = engine.view().boards().galactica().food();

        play(engine -> engine.choose(1, single(new ChooseCrisisSectionEvent(1, WATER_SABOTAGED, TOP))));
        assertWaitingFor(new AwaitingChoice(3, Set.of(single(new MoralCompassEvent(3, WATER_SABOTAGED, BOTTOM)),
                single(new ResolveCrisisSectionEvent(WATER_SABOTAGED, TOP)))));
        play(engine -> engine.choose(3, single(new MoralCompassEvent(3, WATER_SABOTAGED, BOTTOM))));

        // no skill check: the bottom costs 1 food
        assertNull(engine.view().activeSkillCheck());
        assertEquals(food - 1, engine.view().boards().galactica().food());
        assertEquals(0, engine.view().player(3).miracleTokens());
        assertEquals(EngineStatus.finished(Team.CYLONS), engine.status(1));
    }

    // "Mysterious Intuition": Sharon "Boomer" Valerii decides that the check of "Water Sabotaged" fails without making it.
    @Test
    void boomerChoosesTheResult() {
        start(setUp(List.of(SAUL_TIGH, LAURA_ROSLIN, SHARON_BOOMER_VALERII), WATER_SABOTAGED), single(new CrisisStepEvent()));
        val food = engine.view().boards().galactica().food();

        play(engine -> engine.choose(1, single(new ChooseCrisisSectionEvent(1, WATER_SABOTAGED, TOP))));
        assertWaitingFor(new AwaitingChoice(3, Set.of(single(new MysteriousIntuitionEvent(3, true)),
                single(new MysteriousIntuitionEvent(3, false)), single(new NoOpEvent(3)))));
        play(engine -> engine.choose(3, single(new MysteriousIntuitionEvent(3, false))));

        // Nobody played a card: the check ended with its fail result.
        assertNull(engine.view().activeSkillCheck());
        assertEquals(true, engine.view().boards().galactica().food() < food);
        assertEquals(0, engine.view().player(3).miracleTokens());
        assertEquals(EngineStatus.finished(Team.CYLONS), engine.status(1));
    }

    // "Religious Visions": Laura Roslin draws both cards, resolves "Riots" and puts "Water Sabotaged" at the bottom.
    @Test
    void roslinPicksTheCardToResolve() {
        start(setUp(List.of(LAURA_ROSLIN, SAUL_TIGH, LOUANNE_KAT_KATRAINE), RIOTS_NO_JUMP, WATER_SABOTAGED),
                single(new CrisisStepEvent()));

        assertWaitingFor(new AwaitingChoice(1, Set.of(
                single(new ReligiousVisionsEvent(1, RIOTS_NO_JUMP, WATER_SABOTAGED)),
                single(new ReligiousVisionsEvent(1, WATER_SABOTAGED, RIOTS_NO_JUMP)))));
        play(engine -> engine.choose(1, single(new ReligiousVisionsEvent(1, RIOTS_NO_JUMP, WATER_SABOTAGED))));

        assertEquals(List.of(RIOTS_NO_JUMP), engine.view().turn().crisisCards());
        assertEquals(WATER_SABOTAGED, crisisDeck().bottom());
        // the Admiral, Saul Tigh, chooses on "Riots"
        assertWaitingFor(new AwaitingChoice(2, Set.of(single(new ChooseCrisisSectionEvent(2, RIOTS_NO_JUMP, TOP)),
                single(new ChooseCrisisSectionEvent(2, RIOTS_NO_JUMP, BOTTOM)))));
    }

    // "Amoral": Tory Foster has to take the first option of "Water Sabotaged", its skill check, without being asked.
    @Test
    void toryFosterTakesTheFirstOption() {
        start(setUp(List.of(TORY_FOSTER, SAUL_TIGH, LOUANNE_KAT_KATRAINE), WATER_SABOTAGED), single(new CrisisStepEvent()));

        assertEquals(xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.WATER_SABOTAGED,
                engine.view().activeSkillCheck().type());
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(2, PlayCardsBeforeSkillCheckEvent.class)));
    }

    // "Choose a Different Path": Lee Adama makes the result "the current player discards 5 Skill Cards", himself here.
    @Test
    void leeAdamaChoosesADifferentPath() {
        start(setUp(List.of(LEE_ADAMA, SAUL_TIGH, LOUANNE_KAT_KATRAINE), WATER_SABOTAGED), single(new CrisisStepEvent()));

        assertWaitingFor(new AwaitingChoice(1, Set.of(single(new ChooseCrisisSectionEvent(1, WATER_SABOTAGED, TOP)),
                single(new ChooseCrisisSectionEvent(1, WATER_SABOTAGED, BOTTOM)),
                single(new ChooseADifferentPathEvent(1, WATER_SABOTAGED)))));
        play(engine -> engine.choose(1, single(new ChooseADifferentPathEvent(1, WATER_SABOTAGED))));

        assertNull(engine.view().activeSkillCheck());
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, DiscardSkillCardsEvent.class, DISCARD_EXACTLY_5)));
        play(engine -> engine.submit(1, new DiscardSkillCardsEvent(1, List.of()))); // he holds no cards
        assertEquals(0, engine.view().player(1).miracleTokens());
        assertEquals(EngineStatus.finished(Team.CYLONS), engine.status(1));
    }

    // "Recon": at the end of her turn Sharon "Boomer" Valerii looks at "Riots" and puts it at the bottom.
    @Test
    void boomerLooksAtTheTopCardAtTheEndOfHerTurn() {
        start(setUp(List.of(SHARON_BOOMER_VALERII, LAURA_ROSLIN, SAUL_TIGH), RIOTS_NO_JUMP, WATER_SABOTAGED),
                single(new DiscardStepEvent()));

        assertWaitingFor(new AwaitingChoice(1, Set.of(single(new ReconEvent(1)), single(new NoOpEvent(1)))));
        play(engine -> engine.choose(1, single(new ReconEvent(1))));
        assertWaitingFor(new AwaitingChoice(1, Set.of(single(new PlaceCrisisCardOnTopEvent(1, RIOTS_NO_JUMP)),
                single(new PlaceCrisisCardOnBottomEvent(1, RIOTS_NO_JUMP)))));
        play(engine -> engine.choose(1, single(new PlaceCrisisCardOnBottomEvent(1, RIOTS_NO_JUMP))));

        assertEquals(WATER_SABOTAGED, crisisDeck().cards().getFirst());
        assertEquals(RIOTS_NO_JUMP, crisisDeck().bottom());
        assertEquals(EngineStatus.finished(Team.CYLONS), engine.status(1));
    }

    // The characters by seat, the titles, and the Crisis Cards stacked from the top.
    private static Followup setUp(List<Character> characters, CrisisCard... topToBottom) {
        val events = new ArrayList<Followup>();
        for (int i = 0; i < characters.size(); i++) {
            events.add(single(new SelectCharacterEvent(i + 1, characters.get(i))));
        }
        events.add(all(new DetermineNextPresidentEvent(), new DetermineNextAdmiralEvent(), new DetermineNextCagEvent()));
        for (val card : List.of(topToBottom).reversed()) {
            events.add(single(new StackCrisisCardEvent(card)));
        }
        return all(events.toArray(Followup[]::new));
    }

    private void start(Followup setUp, Followup step) {
        engine = new GameEngine(3, Randomness.seeded(42), new ScriptEvent(all(setUp, step, CYLONS_WIN)));
        assertRestoresTheSameGame();
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

    private DeckView<CrisisCard> crisisDeck() {
        return engine.view().decks().deck(DeckId.CRISIS);
    }

    private void assertWaitingFor(PendingInput expected) {
        assertEquals(EngineStatus.waiting(expected), engine.status(expected.player()));
    }

    private void assertRestoresTheSameGame() {
        val restored = GameEngine.restore(engine.journal());

        assertEquals(snapshot(engine), snapshot(restored));
        assertEquals(statuses(engine), statuses(restored));
    }

    // The view is the game itself.
    private static GameSnapshot snapshot(GameEngine engine) {
        return GameSnapshot.of((Game) engine.view());
    }

    private static List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, 3).mapToObj(engine::status).toList();
    }
}
