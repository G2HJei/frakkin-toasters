package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterMovementEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsOfColorEvent;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Arrays;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.INTIMATE;

public class IntimateEventProcessor extends CharacterMovementEventProcessor<IntimateEvent> {

    // There is always another player, and a deck to name as long as one has cards left (rule 8.2.2.5).
    public static boolean canUse(GameView game, PlayerView player) {
        return game.players().size() > 1
                && Arrays.stream(SkillCardColor.values()).anyMatch(color -> game.decks().skillCardsLeft(color) > 0);
    }

    @Override
    public CharacterAbility ability() {
        return INTIMATE;
    }

    @Override
    protected boolean isValidUse() {
        val target = event.targetPlayerNumber();
        return isPlayer(target)
                && target != player.number()
                && event.deck() != null
                && game.decks().skillCardsLeft(event.deck()) > 0;
    }

    @Override
    protected Followup resolve() {
        ops.player(player.number()).takeRandomSkillCards(event.targetPlayerNumber(), 1);
        return single(new DrawSkillCardsOfColorEvent(event.targetPlayerNumber(), event.deck(), 1));
    }
}
