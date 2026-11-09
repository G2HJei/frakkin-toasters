package xyz.zlatanov.frakkintoasters.event.officials;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class MakePresidentEventProcessor extends EventProcessor<MakePresidentEvent> {

    @Override
    protected boolean isValid() {
        return canBecomePresident(game, event.character());
    }

    @Override
    public Followup process() {
        ops.titles().assignPresident(event.character());
        return Followup.NONE;
    }

    // Another player's character gets the title ("Prisoner Revolt").
    // Revealed Cylons hold no titles, and neither do Cylon Leaders, even while Infiltrating (rule 6.2.3.3).
    public static boolean canBecomePresident(GameView game, Character character) {
        return character != null
                && character != game.president()
                && character.type() != CYLON_LEADER
                && game.players().stream().anyMatch(p -> p.character() == character && p.isHuman());
    }
}
