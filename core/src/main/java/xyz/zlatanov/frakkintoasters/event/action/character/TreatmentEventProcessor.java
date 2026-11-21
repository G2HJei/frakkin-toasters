package xyz.zlatanov.frakkintoasters.event.action.character;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEventProcessor;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class TreatmentEventProcessor extends CharacterActionEventProcessor<TreatmentEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.TREATMENT;
    }

    static final int CARDS = 2;

    public static boolean canUse(GameView game, PlayerView player) {
        return game.players().stream().anyMatch(other -> isTarget(player, other) && canDrawFrom(game, other));
    }

    @Override
    protected boolean isValidUse() {
        if (!isPlayer(event.targetPlayerNumber()) || event.colors() == null || event.colors().size() != CARDS
                || event.colors().stream().anyMatch(Objects::isNull)) {
            return false;
        }
        val target = game.player(event.targetPlayerNumber());
        return isTarget(player, target) && canDraw(game, target, event.colors());
    }

    // He gives 2 cards of his hand once he drew, the drawn ones included. Each pair is an option, so he can't give them
    // to anyone else.
    @Override
    protected Followup resolve() {
        ops.player(event.playerNumber()).drawSkillCards(event.colors());
        val hand = player.skillCards().cards();
        val options = new ArrayList<Event>();
        for (int i = 0; i < hand.size(); i++) {
            for (int j = i + 1; j < hand.size(); j++) {
                val cards = new ArrayList<>(List.of(hand.get(i), hand.get(j)));
                cards.sort(BY_TYPE_AND_VALUE);
                options.add(new TreatmentGiveEvent(event.playerNumber(), event.targetPlayerNumber(), cards));
            }
        }
        return one(options.toArray(Event[]::new));
    }

    private static final Comparator<SkillCard> BY_TYPE_AND_VALUE = Comparator
            .comparing((SkillCard card) -> card.type().ordinal())
            .thenComparingInt(SkillCard::value);

    // "Choose a human player": another one, whose skill set the cards come from.
    private static boolean isTarget(PlayerView player, PlayerView other) {
        return other.number() != player.number() && other.isHuman() && other.character() != null;
    }

    private static boolean canDrawFrom(GameView game, PlayerView target) {
        val colors = Arrays.stream(SkillCardColor.values()).toList();
        for (val first : colors) {
            for (val second : colors) {
                if (canDraw(game, target, List.of(first, second))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean canDraw(GameView game, PlayerView target, List<SkillCardColor> colors) {
        return DrawSkillCardsEventProcessor.fitsSkillSet(target.character(), colors)
                && colors.stream().distinct()
                .allMatch(color -> Collections.frequency(colors, color) <= game.decks().skillCardsLeft(color));
    }
}
