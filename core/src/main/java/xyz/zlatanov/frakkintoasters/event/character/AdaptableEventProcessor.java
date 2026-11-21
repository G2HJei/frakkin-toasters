package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Arrays;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2_OF_ANY_COLOR;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.ADAPTABLE;

public class AdaptableEventProcessor extends EventProcessor<AdaptableEvent> {

    // A card of some color is left to draw (rule 8.2.2.5).
    public static boolean canUse(GameView game, PlayerView player) {
        return player.hasAbility(ADAPTABLE)
                && Arrays.stream(SkillCardColor.values()).anyMatch(color -> game.decks().skillCardsLeft(color) > 0);
    }

    /**
     * @return whether a player has "Adaptable", so a Quorum Card's play asks after it ({@link OfferAdaptableEvent})
     */
    public static boolean mayBeOffered(GameView game) {
        return game.players().stream().anyMatch(player -> player.hasAbility(ADAPTABLE));
    }

    public static Followup offer(GameView game) {
        return game.players().stream()
                .filter(player -> canUse(game, player))
                .findFirst()
                .map(tory -> one(new AdaptableEvent(tory.number()), new NoOpEvent(tory.number())))
                .orElse(NONE);
    }

    @Override
    protected boolean isValid() {
        return canUse(game, player);
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(ADAPTABLE);
        return single(new PlayerDecisionEvent<>(player.number(), DrawSkillCardsEvent.class, DRAW_EXACTLY_2_OF_ANY_COLOR));
    }
}
