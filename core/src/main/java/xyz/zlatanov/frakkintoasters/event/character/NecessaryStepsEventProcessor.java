package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.colonialone.PressRoomEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.List;
import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.NECESSARY_STEPS;

public class NecessaryStepsEventProcessor extends EventProcessor<NecessaryStepsEvent> {

    /**
     * Draws for the player through "Necessary Steps" when someone has it: Zarek draws the top 2 Mutiny Cards and sees
     * them.
     *
     * @return Zarek's choice of the card to give, empty when nobody has the ability (the player draws as usual)
     */
    public static Optional<Followup> drawFor(GameView game, GameOps ops, int drawerPlayerNumber, boolean staysPut) {
        val deck = game.decks().deck(DeckId.MUTINY);
        val left = deck.size() + deck.discardSize();
        val zarek = game.players().stream().filter(player -> player.hasAbility(NECESSARY_STEPS)).findFirst();
        if (zarek.isEmpty() || left < 1) {
            return Optional.empty();
        }
        int zarekNumber = zarek.get().number();
        val drawn = ops.player(zarekNumber).drawMutinyCardsToChoose(Math.min(2, left));
        ops.player(zarekNumber).see(DeckId.MUTINY, drawn);
        return Optional.of(one(drawn.stream()
                .map(given -> new NecessaryStepsEvent(zarekNumber, drawerPlayerNumber, given, other(drawn, given), staysPut))
                .toArray(Event[]::new)));
    }

    private static MutinyCard other(List<MutinyCard> drawn, MutinyCard given) {
        return drawn.size() < 2 ? null : drawn.get(drawn.indexOf(given) == 0 ? 1 : 0);
    }

    @Override
    protected boolean isValid() {
        return player.hasAbility(NECESSARY_STEPS) && isPlayer(event.drawerPlayerNumber()) && event.given() != null;
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(NECESSARY_STEPS);
        ops.player(event.drawerPlayerNumber()).holdMutinyCard(event.given());
        if (event.bottom() != null) {
            ops.player(player.number()).putMutinyCardsAtBottom(List.of(event.bottom()));
        }
        val drawer = game.player(event.drawerPlayerNumber());
        return event.staysPut()
                ? PressRoomEventProcessor.keepOneMutinyCard(game, drawer.number())
                : DrawMutinyCardEventProcessor.afterDrawing(game, drawer);
    }
}
