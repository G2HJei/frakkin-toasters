package xyz.zlatanov.frakkintoasters.event.action.character;

import java.util.ArrayList;
import java.util.List;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class SkilledPoliticianEventProcessor extends CharacterActionEventProcessor<SkilledPoliticianEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.SKILLED_POLITICIAN;
    }

    static final int CARDS = 4;

    public static boolean canUse(GameView game, PlayerView player) {
        return game.decks().quorumCardsLeft() > 0;
    }

    @Override
    protected Followup resolve() {
        val drawn = ops.titles().drawQuorumCards(Math.min(CARDS, game.decks().quorumCardsLeft()));
        ops.player(event.playerNumber()).see(DeckId.QUORUM, drawn);
        // Copies of one card make the same option, which the choice collapses.
        return one(drawn.stream()
                .map(chosen -> new SkilledPoliticianChoiceEvent(event.playerNumber(), chosen, others(drawn, chosen)))
                .toArray(Event[]::new));
    }

    private static List<QuorumCard> others(List<QuorumCard> drawn, QuorumCard chosen) {
        val others = new ArrayList<>(drawn);
        others.remove(chosen);
        return others;
    }
}
