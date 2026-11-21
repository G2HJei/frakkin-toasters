package xyz.zlatanov.frakkintoasters.event.action.character;

import java.util.ArrayList;
import java.util.List;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardPlays;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class AbusePowerEventProcessor extends CharacterActionEventProcessor<AbusePowerEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.ABUSE_POWER;
    }

    static final int CARDS = 4;

    // Only a human plays Mutiny Cards (rules 5.2.3.4 and 5.2.3.5).
    public static boolean canUse(GameView game, PlayerView player) {
        return player.isHuman() && mutinyCardsLeft(game) > 0;
    }

    @Override
    protected boolean isValidUse() {
        return canUse(game, player);
    }

    // He chooses among the cards he can play now. With none, all go to the bottom.
    @Override
    protected Followup resolve() {
        val drawn = ops.player(event.playerNumber()).drawMutinyCardsToChoose(Math.min(CARDS, mutinyCardsLeft(game)));
        ops.player(event.playerNumber()).see(DeckId.MUTINY, drawn);
        val playable = drawn.stream().filter(card -> MutinyCardPlays.canPlay(game, player, card)).toList();
        if (playable.isEmpty()) {
            ops.player(event.playerNumber()).putMutinyCardsAtBottom(drawn);
            return NONE;
        }
        return one(playable.stream()
                .map(chosen -> new AbusePowerChoiceEvent(event.playerNumber(), chosen, others(drawn, chosen)))
                .toArray(Event[]::new));
    }

    private static List<MutinyCard> others(List<MutinyCard> drawn, MutinyCard chosen) {
        val others = new ArrayList<>(drawn);
        others.remove(chosen);
        return others;
    }

    private static int mutinyCardsLeft(GameView game) {
        val deck = game.decks().deck(DeckId.MUTINY);
        return deck.size() + deck.discardSize();
    }
}
