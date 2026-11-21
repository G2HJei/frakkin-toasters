package xyz.zlatanov.frakkintoasters.event.officials;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class MakeAdmiralEventProcessor extends EventProcessor<MakeAdmiralEvent> {

    @Override
    protected boolean isValid() {
        return canBecomeAdmiral(game, event.character());
    }

    @Override
    public Followup process() {
        ops.titles().assignAdmiral(event.character());
        return Followup.NONE;
    }

    // Another human player's character, not a Cylon Leader (rule 6.2.3.3), and not in the "Brig", where the Admiral
    // loses the title (rule 4.4.2.3).
    public static boolean canBecomeAdmiral(GameView game, Character character) {
        return character != game.admiral() && canHoldMilitaryTitle(game, character);
    }

    static boolean canHoldMilitaryTitle(GameView game, Character character) {
        return character != null
                && character.type() != CYLON_LEADER
                && game.players().stream().anyMatch(p -> p.character() == character && p.isHuman())
                && !game.boards().galactica().charactersIn(BRIG).contains(character);
    }
}
