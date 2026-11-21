package xyz.zlatanov.frakkintoasters.event.action.quorum;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.result.DiscardFromDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.*;

public class ConsultTheOracleEventProcessor extends QuorumCardEventProcessor<ConsultTheOracleEvent> {

    // The card decks, without the Loyalty and Motive decks (rule A2.3.13.1). The damage tokens and the civilian ships
    // are tokens.
    private static final Set<DeckId> DECKS = EnumSet.of(POLITICS, LEADERSHIP, TACTICS, PILOTING, ENGINEERING,
            TREACHERY, DESTINY, QUORUM, CRISIS, SUPER_CRISIS, MUTINY, DESTINATION);

    public static boolean canPlay(GameView game) {
        return Arrays.stream(DeckId.values()).anyMatch(deck -> canLookAt(game, deck));
    }

    @Override
    public QuorumCard card() {
        return QuorumCard.CONSULT_THE_ORACLE;
    }

    @Override
    protected boolean isValidPlay() {
        return canLookAt(game, event.deck());
    }

    @Override
    protected Followup resolve() {
        val playerOps = ops.player(player.number());
        playerOps.see(event.deck(), List.of(game.decks().deck(event.deck()).bottom()));
        val destiny = game.decks().destiny().cards();
        if (destiny.isEmpty()) {
            ops.titles().discardPlayedQuorumCard(card());
            return NONE;
        }
        playerOps.see(DESTINY, destiny);
        // The discard takes 2 cards, or what is left, and shuffles the Destiny deck.
        return all(new PlayerDecisionEvent<>(player.number(), DiscardFromDestinyDeckEvent.class), thenDiscard());
    }

    private static boolean canLookAt(GameView game, DeckId deck) {
        return deck != null && DECKS.contains(deck) && !game.decks().deck(deck).isEmpty();
    }
}
