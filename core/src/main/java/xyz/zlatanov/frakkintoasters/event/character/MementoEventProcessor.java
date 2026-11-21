package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Collections;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MEMENTO;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.PILOTING;

public class MementoEventProcessor extends EventProcessor<MementoEvent> {

    private static final int CARDS_DRAWN = 3;

    // The top 3 cards, so the Piloting deck and its discard pile have to hold 3.
    public static boolean canUse(GameView game, PlayerView player) {
        return CharacterAbilities.canUseOncePerTurn(game, player, MEMENTO)
                && game.decks().skillCardsLeft(PILOTING) >= CARDS_DRAWN;
    }

    @Override
    protected boolean isValid() {
        return canUse(game, player);
    }

    @Override
    public Followup process() {
        val playerOps = ops.player(player.number());
        playerOps.useAbility(MEMENTO);
        val drawn = playerOps.drawSkillCards(Collections.nCopies(CARDS_DRAWN, PILOTING));
        // he keeps 2 of the 3 he drew
        return one(drawn.stream()
                .map(card -> new DiscardSkillCardsEvent(player.number(), List.of(card)))
                .toArray(Event[]::new));
    }
}
