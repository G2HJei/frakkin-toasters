package xyz.zlatanov.frakkintoasters.projection;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.fuzz.AnswerFinder;
import xyz.zlatanov.frakkintoasters.projection.GameReport.MomentKind;
import xyz.zlatanov.frakkintoasters.projection.GameReport.PlayerReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GameReportTest {

    private static final int INPUT_CAP = 20_000;

    @Test
    void aGameThatGoesOnHasNoReport() {
        val engine = GameEngine.start(4, 7L);

        assertTrue(GameReport.of(engine.journal().entries()).isEmpty());
    }

    @Test
    void aFinishedGameIsToldWhole() {
        for (val seed : List.of(3L, 11L, 42L)) {
            val engine = played(4, seed);
            val game = engine.view();
            val report = GameReport.of(engine.journal().entries()).orElseThrow();

            assertEquals(game.outcome().orElseThrow(), report.winner(), "seed " + seed);
            assertEquals(game.winners(), report.winners());
            assertEquals(game.turn().number(), report.turns());
            assertEquals(game.distanceTraveled(), report.distance());
            assertEquals(4, report.players().size());
            for (val player : report.players()) {
                val view = game.player(player.number());
                assertEquals(view.character(), player.character());
                assertEquals(!view.isHuman(), player.cylon());
                assertEquals(view.turns(), player.turns());
                assertEquals(view.loyaltyCards().size() + view.loyaltyCards().revealedCards().size(),
                        player.loyaltyCards().size());
                assertEquals(player.revealedOnTurn() != null, story(report, MomentKind.CYLON_REVEALED).stream()
                        .anyMatch(moment -> moment.playerNumber() == player.number()));
            }
            // every Crisis Card drawn on someone's turn
            assertEquals(report.statistics().crisisCards(), report.players().stream()
                    .mapToInt(player -> player.statistics().crisisCards()).sum());
            assertEquals(report.statistics().jumps(), story(report, MomentKind.JUMPED).size());
            val ending = report.story().getLast();
            assertEquals(MomentKind.GAME_ENDED, ending.kind());
            assertEquals(report.winner(), ending.winner());
            // the resources at each turn's start, then at the end, as the game stands
            val resources = report.resources();
            assertEquals(1, resources.getFirst().turn());
            assertFalse(resources.getFirst().end());
            val last = resources.getLast();
            assertTrue(last.end());
            assertEquals(game.boards().galactica().morale(), last.morale());
            assertEquals(game.boards().galactica().population(), last.population());
            assertTrue(report.players().stream().mapToInt(player -> player.statistics().skillCardsGained()).sum() > 0);
        }
    }

    @Test
    void cardsPlayedIntoChecksCountForWhoPlayedThem() {
        val engine = played(5, 23L);
        val report = GameReport.of(engine.journal().entries()).orElseThrow();
        val checks = report.statistics().skillChecks().values().stream().mapToInt(Integer::intValue).sum();

        if (checks > 0) {
            assertTrue(report.players().stream().map(PlayerReport::statistics).anyMatch(statistics -> statistics.cardsIntoChecks() > 0));
        }
        for (val player : report.players()) {
            assertTrue(player.statistics().cardsIntoChecks() <= player.statistics().skillCardsGained());
        }
    }

    private static List<GameReport.Moment> story(GameReport report, MomentKind kind) {
        return report.story().stream().filter(moment -> moment.kind() == kind).toList();
    }

    private static GameEngine played(int players, long seed) {
        val engine = GameEngine.start(players, seed);
        val random = new Random(seed);
        for (int i = 0; i < INPUT_CAP && engine.view().outcome().isEmpty(); i++) {
            switch (engine.pendingInput().orElseThrow()) {
                case AwaitingDecision(var decision) -> engine.submit(decision.playerNumber(),
                        AnswerFinder.find(engine.view(), decision, random, finding -> {
                        }).orElseThrow());
                case AwaitingChoice(int player, var options) -> {
                    val list = new ArrayList<>(options);
                    engine.choose(player, list.get(random.nextInt(list.size())));
                }
            }
        }
        assertTrue(engine.view().outcome().isPresent(), "seed " + seed + " ends");
        return engine;
    }
}
