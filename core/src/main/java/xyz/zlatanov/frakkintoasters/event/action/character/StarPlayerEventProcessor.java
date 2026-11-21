package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.player.TreacheryDiscards;
import java.util.Collections;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class StarPlayerEventProcessor extends CharacterActionEventProcessor<StarPlayerEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.STAR_PLAYER;
    }

    public static boolean canUse(GameView game, PlayerView player) {
        return !player.skillCards().cards().isEmpty();
    }

    @Override
    protected boolean isValidUse() {
        return event.discards() != null && !event.discards().isEmpty() && holdsSkillCards(event.discards())
                && event.color() != null && inSkillSet(player, event.color());
    }

    // The discarded cards go to their discard piles first, so a deck that runs out takes them back.
    @Override
    protected Followup resolve() {
        ops.player(event.playerNumber()).discard(event.discards().toArray(SkillCard[]::new));
        val cards = Math.min(event.discards().size(), game.decks().skillCardsLeft(event.color()));
        ops.player(event.playerNumber()).drawSkillCards(Collections.nCopies(cards, event.color()));
        return TreacheryDiscards.mutinyCardFor(game, ops, event.playerNumber(), event.discards());
    }

    private static boolean inSkillSet(PlayerView player, SkillCardColor color) {
        return player.character().skillSet().stream().anyMatch(option -> option.colors().contains(color));
    }
}
