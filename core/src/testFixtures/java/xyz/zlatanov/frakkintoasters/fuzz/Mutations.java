package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.answer.LegalAnswers;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Checks that the legal answers are complete around an answer: the answer with one component set to each value of its
 * wide domain. An input the game takes that the answers don't cover is a {@code HINT_MISSING} finding. Only
 * {@code prepare} runs, so the game stays as it was.
 */
final class Mutations {

    private Mutations() {
    }

    static void check(GameView game, PlayerDecisionEvent<?> decision, LegalAnswers answers, Event answer, Random random,
                      Consumer<Finding> findings) {
        val components = answer.getClass().getRecordComponents();
        val mutable = new ArrayList<Integer>();
        for (int i = 0; i < components.length; i++) {
            // the engine sets the player and the constraints of a submitted answer itself
            if (!components[i].getName().equals("playerNumber") && components[i].getType() != EventConstraint.class) {
                mutable.add(i);
            }
        }
        if (mutable.isEmpty()) {
            return;
        }
        val index = mutable.get(random.nextInt(mutable.size()));
        val asked = new Asked(game, decision, random, findings);
        for (val value : Domains.wide(asked, components[index])) {
            val mutated = Records.with(answer, index, value);
            if (mutated == null) {
                continue;
            }
            val legal = AnswerFinder.legal(game, decision, mutated, findings);
            if (legal.isPresent() && !answers.covers(legal.get())) {
                findings.accept(missing(legal.get(), components[index].getName()));
                return;
            }
        }
    }

    /**
     * @return the finding for an input the game takes and the legal answers don't cover
     */
    static Finding missing(Event taken, String component) {
        val name = taken.getClass().getSimpleName() + (component == null ? "" : "." + component);
        return Finding.of(Finding.Kind.HINT_MISSING, "missing " + name,
                        "The legal answers miss a `" + taken.getClass().getSimpleName() + "` the game takes"
                                + (component == null ? "" : " (`" + component + "`)"))
                .withEvent(taken);
    }
}
