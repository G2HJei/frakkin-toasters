package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.QUICK_FIX;

public class QuickFixEventProcessor extends EventProcessor<QuickFixEvent> {

    public static boolean canUse(GameView game, PlayerView player) {
        val skillCheck = game.activeSkillCheck();
        return player.hasAbility(QUICK_FIX)
                && player.number() == game.turn().currentPlayerNumber() // on her turn
                && CharacterAbilities.canAffectSkillCheck(game)
                && skillCheck.totalStrength() == null
                && !skillCheck.dividedCards().isEmpty();
    }

    /**
     * @return the choice of a revealed card to discard or nothing, for the player who can use "Quick Fix"
     */
    public static Followup offer(GameView game) {
        val cally = game.currentPlayer();
        if (!canUse(game, cally)) {
            return NONE;
        }
        val options = new ArrayList<Event>();
        game.activeSkillCheck().dividedCards().stream()
                .distinct()
                .map(card -> new QuickFixEvent(cally.number(), card))
                .forEach(options::add);
        options.add(new NoOpEvent(cally.number()));
        return one(options.toArray(Event[]::new));
    }

    @Override
    protected boolean isValid() {
        return canUse(game, player) && game.activeSkillCheck().dividedCards().contains(event.card());
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(QUICK_FIX);
        ops.skillCheck().discard(event.card());
        return NONE;
    }
}
