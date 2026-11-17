package xyz.zlatanov.frakkintoasters.engine.journal;

import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.Die;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.BeforeRaidersActivate;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.ship.*;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.GameStep.CRISIS;
import static xyz.zlatanov.frakkintoasters.state.GameStep.RECEIVE_SKILLS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.STRATEGIC_PLANNING;

class GameReplayerTest {

    private final Journal journal = new Journal(); // ScriptedScenario.play(journal) records into it, each test gets a new one

    @Test
    void replayedGameEqualsTheLiveGame() {
        val live = ScriptedScenario.play(journal);
        val replayed = GameReplayer.replay(journal);
        assertEquals(GameSnapshot.of(live), GameSnapshot.of(replayed));
    }

    @Test
    void replayDoesNotDependOnTheRandomnessOfTheReplayingGame() {
        val live = ScriptedScenario.play(journal);

        val first = GameReplayer.replay(journal, Randomness.seeded(1));
        val second = GameReplayer.replay(journal, Randomness.seeded(2));

        assertEquals(GameSnapshot.of(live), GameSnapshot.of(first));
        assertEquals(GameSnapshot.of(live), GameSnapshot.of(second));
    }

    @Test
    void replayIgnoresScopesAndChoices() {
        val live = ScriptedScenario.play(journal);
        val entries = journal.entries();

        assertTrue(entries.stream().anyMatch(JournalEntry.ScopeOpened.class::isInstance));
        assertTrue(entries.stream().anyMatch(JournalEntry.ScopeClosed.class::isInstance));
        assertTrue(entries.stream().anyMatch(JournalEntry.ChoiceMade.class::isInstance));
        assertEquals(GameSnapshot.of(live), GameSnapshot.of(GameReplayer.replay(journal)));
    }

    @Test
    void aJournalHasToStartWithGameStarted() {
        assertThrows(IllegalArgumentException.class, () -> GameReplayer.replay(new Journal()));

        journal.record(new xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.TurnStarted(1));

        assertThrows(IllegalArgumentException.class, () -> GameReplayer.replay(journal));
    }

    @Test
    void theSnapshotSeesTheDifferencesThatReplayHasToGetRight() {
        val live = ScriptedScenario.play(journal);
        val snapshot = GameSnapshot.of(live);

        val replayed = GameReplayer.replay(journal);
        replayed.boards().galactica().decreaseFuel(1);
        assertNotEquals(snapshot, GameSnapshot.of(replayed));

        val replayed2 = GameReplayer.replay(journal);
        val basestar = replayed2.boards().galactica().shipsInSpace(Basestar.class).getFirst();
        basestar.clearDamage(); // a Basestar equals by id only, the snapshot has to look at its damage
        assertNotEquals(snapshot, GameSnapshot.of(replayed2));

        val replayed3 = GameReplayer.replay(journal);
        replayed3.boards().galactica().shipsInSpace(HumanFighter.class).forEach(f -> f.pilot(FELIX_GAETA));
        assertNotEquals(snapshot, GameSnapshot.of(replayed3));
    }

    @Test
    void theScenarioRecordsEveryKindOfFact() {
        val live = ScriptedScenario.play(journal);
        val recorded = journal.facts().stream().map(Object::getClass).collect(java.util.stream.Collectors.toSet());

        val missing = java.util.Arrays.stream(Fact.class.getPermittedSubclasses())
                .flatMap(area -> java.util.Arrays.stream(area.getPermittedSubclasses()))
                .filter(type -> !recorded.contains(type))
                .map(Class::getSimpleName)
                .sorted()
                .toList();

        assertEquals(java.util.List.of(), missing);
    }
}
