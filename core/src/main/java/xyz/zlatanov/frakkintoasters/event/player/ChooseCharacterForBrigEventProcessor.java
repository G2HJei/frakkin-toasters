package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.player.SendToBrigEventProcessor.canBeSentToBrig;

public class ChooseCharacterForBrigEventProcessor extends EventProcessor<ChooseCharacterForBrigEvent> {

    /**
     * @return the chooser's decision, {@link Followup#NONE} when there is nobody he could choose
     */
    public static Followup offer(GameView game, int chooser) {
        return game.players().stream().anyMatch(player -> canBeChosen(game, player.number()))
                ? single(new PlayerDecisionEvent<>(chooser, ChooseCharacterForBrigEvent.class))
                : NONE;
    }

    // "he cannot choose a character that is already there. He also cannot choose a revealed Cylon player, or "Helo"
    // before he is on the board" (rule 9.2.3)
    public static boolean canBeChosen(GameView game, int playerNumber) {
        return canBeSentToBrig(game, playerNumber) && game.locate(game.player(playerNumber).character()).isPresent();
    }

    @Override
    protected boolean isValid() {
        return isPlayer(event.targetPlayerNumber()) && canBeChosen(game, event.targetPlayerNumber());
    }

    @Override
    public Followup process() {
        return single(new SendToBrigEvent(event.targetPlayerNumber()));
    }
}
