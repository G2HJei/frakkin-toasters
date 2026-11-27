package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.FAST_LEARNER;

public class FastLearnerEventProcessor extends EventProcessor<FastLearnerEvent> {

    static final int CARDS = 3;

    public static boolean canUse(GameView game, PlayerView player) {
        val skillCheck = game.activeSkillCheck();
        return CharacterAbilities.canUseMiracle(game, player, FAST_LEARNER)
                && CharacterAbilities.canAffectSkillCheck(game)
                && skillCheck.result() == null
                && skillCheck.totalStrength() == null;
    }

    public static Followup offer(GameView game) {
        return game.players().stream()
                .filter(player -> canUse(game, player))
                .findFirst()
                .map(dee -> {
                    val options = new ArrayList<Event>();
                    Arrays.stream(SkillCardColor.values())
                            .filter(color -> game.decks().skillCardsLeft(color) > 0)
                            .map(color -> new FastLearnerEvent(dee.number(), color))
                            .forEach(options::add);
                    options.add(new NoOpEvent(dee.number()));
                    return one(options.toArray(Event[]::new));
                })
                .orElse(NONE);
    }

    @Override
    protected boolean isValid() {
        return canUse(game, player) && event.color() != null && game.decks().skillCardsLeft(event.color()) > 0;
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(FAST_LEARNER);
        // She looks at them in her hand, so only she knows them. As many as the deck and its discard pile have left.
        val count = Math.min(CARDS, game.decks().skillCardsLeft(event.color()));
        val cards = ops.player(player.number()).drawSkillCards(Collections.nCopies(count, event.color()));
        return one(new FastLearnerIntoCheckEvent(player.number(), cards), new NoOpEvent(player.number()));
    }
}
