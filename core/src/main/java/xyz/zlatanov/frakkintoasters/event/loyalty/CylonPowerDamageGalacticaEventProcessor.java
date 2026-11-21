package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.ship.ChooseGalacticaDamageTokensEvent;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.util.ArrayList;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class CylonPowerDamageGalacticaEventProcessor extends EventProcessor<CylonPowerDamageGalacticaEvent> {

    static final int TOKENS_DRAWN    = 5;
    static final int TOKENS_RESOLVED = 2;

    @Override
    protected boolean isValid() {
        return !game.decks().deck(DeckId.GALACTICA_DAMAGE).isEmpty();
    }

    @Override
    public Followup process() {
        // Drawing fewer than he may only takes choices away, so he draws as many as there are, up to 5.
        val left = game.decks().deck(DeckId.GALACTICA_DAMAGE).size();
        List<GalacticaDamage> tokens = ops.galactica().drawDamage(Math.min(TOKENS_DRAWN, left));
        if (tokens.size() <= TOKENS_RESOLVED) {
            return single(new ChooseGalacticaDamageTokensEvent(player.number(), tokens, List.of()));
        }
        // Every pair of tokens, in the order drawn. The others go back to the pile.
        val options = new ArrayList<Event>();
        for (int first = 0; first < tokens.size(); first++) {
            for (int second = first + 1; second < tokens.size(); second++) {
                val returned = new ArrayList<>(tokens);
                returned.remove(second);
                returned.remove(first);
                options.add(new ChooseGalacticaDamageTokensEvent(player.number(),
                        List.of(tokens.get(first), tokens.get(second)), List.copyOf(returned)));
            }
        }
        return one(options.toArray(Event[]::new));
    }
}
