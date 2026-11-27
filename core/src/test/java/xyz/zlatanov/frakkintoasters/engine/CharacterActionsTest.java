package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus.Finished;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.character.AbusePowerChoiceEvent;
import xyz.zlatanov.frakkintoasters.event.action.character.AbusePowerEvent;
import xyz.zlatanov.frakkintoasters.event.action.character.OrganizedEvent;
import xyz.zlatanov.frakkintoasters.event.action.character.OrganizedLocationEvent;
import xyz.zlatanov.frakkintoasters.event.action.character.SkilledPoliticianChoiceEvent;
import xyz.zlatanov.frakkintoasters.event.action.character.SkilledPoliticianEvent;
import xyz.zlatanov.frakkintoasters.event.action.mutiny.FeedThePeopleEvent;
import xyz.zlatanov.frakkintoasters.event.action.quorum.EnactProductionQuotasEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.location.colonialone.PresidentsOfficeEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.CommandEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.ResearchLabEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.turn.ActionStepEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.view.GalacticaBoardView;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.*;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TACTICS;

/**
 * Player 1 has the ability, player 2 is President and Admiral.
 */
class CharacterActionsTest {

    // Only the end of the game empties the agenda, so the script ends it.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private GameEngine engine;

    // Three locations from "Communications", each through its own decision. While one is allowed, his own location's
    // action isn't.
    @Test
    void organizedTakesTheActionsOfThreeLocations() {
        start(all(setUp(LOUIS_HOSHI, LAURA_ROSLIN), single(new ActionStepEvent()), CYLONS_WIN));
        val skillCards = game().player(1).skillCards().size();

        act(1);
        play(engine -> engine.submit(1, new OrganizedEvent(1)));
        choose(1, single(new OrganizedLocationEvent(1, RESEARCH_LAB, List.of())));
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, LocationEvent.class)));
        assertThrows(InvalidActionException.class, () -> engine.submit(1, new PresidentsOfficeEvent(1)));
        play(engine -> engine.submit(1, new ResearchLabEvent(1, TACTICS)));
        choose(1, single(new OrganizedLocationEvent(1, PRESIDENTS_OFFICE, List.of(RESEARCH_LAB))));
        play(engine -> engine.submit(1, new PresidentsOfficeEvent(1)));
        choose(1, single(new OrganizedLocationEvent(1, COMMAND, List.of(RESEARCH_LAB, PRESIDENTS_OFFICE))));
        play(engine -> engine.submit(1, new CommandEvent(1, false)));
        choose(1, single(new NoOpEvent(1)));
        choose(1, single(new NoOpEvent(1)));

        assertEquals(skillCards + 3, game().player(1).skillCards().size());
        assertEquals(COMMUNICATIONS, game().locate(LOUIS_HOSHI).orElseThrow());
        assertEquals(0, game().player(1).miracleTokens());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // She isn't President, and the card she resolves is hers to play.
    @Test
    void skilledPoliticianResolvesOneOfFourQuorumCards() {
        start(all(setUp(LAURA_ROSLIN, WILLIAM_ADAMA),
                single(new StackDeckEvent(DeckId.QUORUM, List.of(ENACT_PRODUCTION_QUOTAS, EULOGY, EULOGY, ARREST_ORDER))),
                single(new ActionStepEvent()), CYLONS_WIN));
        val food = galactica().food();

        act(1);
        play(engine -> engine.submit(1, new SkilledPoliticianEvent(1)));
        choose(1, single(new SkilledPoliticianChoiceEvent(1, ENACT_PRODUCTION_QUOTAS, List.of(EULOGY, EULOGY, ARREST_ORDER))));
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, EnactProductionQuotasEvent.class)));
        play(engine -> engine.submit(1, new EnactProductionQuotasEvent(1)));

        assertEquals(food + 1, galactica().food());
        val deck = game().decks().quorum().cards();
        assertEquals(List.of(EULOGY, EULOGY, ARREST_ORDER), deck.subList(deck.size() - 3, deck.size()));
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // "Blackmail" isn't offered: the President holds no Skill Card. Holding a second Mutiny Card to play it doesn't send
    // him to the "Brig".
    @Test
    void abusePowerPlaysOneOfFourMutinyCards() {
        start(all(setUp(TOM_ZAREK_ALT, LAURA_ROSLIN),
                single(new StackDeckEvent(DeckId.MUTINY, List.of(FEED_THE_PEOPLE, BLACKMAIL, PANIC, SELFISH_ACT))),
                single(new ActionStepEvent()), CYLONS_WIN));
        // his starting card ("Disreputable") goes through his own "Necessary Steps"
        val necessarySteps = (AwaitingChoice) engine.pendingInput().orElseThrow();
        choose(1, necessarySteps.options().iterator().next());
        val food = galactica().food();
        val disreputable = game().player(1).mutinyCards().cards(); // he starts with a Mutiny Card

        act(1);
        play(engine -> engine.submit(1, new AbusePowerEvent(1)));
        val choice = (AwaitingChoice) engine.pendingInput().orElseThrow();
        assertEquals(3, choice.options().size());
        choose(1, single(new AbusePowerChoiceEvent(1, FEED_THE_PEOPLE, List.of(BLACKMAIL, PANIC, SELFISH_ACT))));
        play(engine -> engine.submit(1, new FeedThePeopleEvent(1)));

        assertEquals(food + 1, galactica().food());
        assertEquals(WEAPONS_CONTROL, game().locate(TOM_ZAREK_ALT).orElseThrow());
        assertEquals(disreputable, game().player(1).mutinyCards().cards());
        assertTrue(game().removedComponents().contains(FEED_THE_PEOPLE));
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // The second character is of another type than the first, as the selection requires, and holds both titles.
    private static Followup setUp(Character first, Character second) {
        return all(
                new SelectCharacterEvent(1, first),
                new SelectCharacterEvent(2, second),
                new SelectCharacterEvent(3, KARA_STARBUCK_THRACE),
                new MakePresidentEvent(second),
                new MakeAdmiralEvent(second));
    }

    private void start(Followup script) {
        engine = new GameEngine(3, Randomness.seeded(42), new ScriptEvent(script));
        assertRestoresTheSameGame();
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

    private void act(int player) {
        choose(player, single(new PlayerDecisionEvent<>(player, ActionEvent.class)));
    }

    private void choose(int player, Followup option) {
        val choice = (AwaitingChoice) engine.pendingInput().orElseThrow();
        assertTrue(choice.options().contains(option), choice.options().toString());
        play(engine -> engine.choose(player, option));
    }

    private Game game() {
        return (Game) engine.view();
    }

    private GalacticaBoardView galactica() {
        return engine.view().boards().galactica();
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
