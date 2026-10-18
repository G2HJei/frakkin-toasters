package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;

public class PersonalVicesEventProcessor extends EventProcessor<PersonalVicesEvent> {

    @Override
    public Followup process() {
        drawTreachery();
        drawMutiny();
        return Followup.NONE;
    }

    private void drawTreachery() {
        var drawingPlayer = game.currentPlayerNumber();
        do {
            ops.player(drawingPlayer).drawSkillCards(TREACHERY);
            drawingPlayer = drawingPlayer == game.players().size() ? 1 : drawingPlayer + 1;
        } while (drawingPlayer != game.currentPlayerNumber());
    }

    private void drawMutiny() {
        if (game.currentPlayer().isHuman()) {
            ops.player(game.currentPlayerNumber()).drawMutinyCard();
        }
    }
}
