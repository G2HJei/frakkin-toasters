package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.DrawQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class SetTheAgendaEventProcessor extends MutinyCardEventProcessor<SetTheAgendaEvent> {

    public static boolean canPlay(GameView game, PlayerView player) {
        return player.character() != game.president();
    }

    @Override
    public MutinyCard card() {
        return MutinyCard.SET_THE_AGENDA;
    }

    @Override
    protected boolean isValidPlay() {
        return canPlay(game, player);
    }

    @Override
    protected Followup resolve() {
        return all(new DrawQuorumCardEvent(), new DrawQuorumCardEvent(), new SetTheAgendaLookEvent(event.playerNumber()),
                thenDiscard());
    }
}
