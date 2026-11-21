package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.Arrays;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.BLIND_DEVOTION;

public class BlindDevotionEventProcessor extends EventProcessor<BlindDevotionEvent> {

    // The moment, once the cards are in and before they are revealed, is where the offer is queued: just before the
    // shuffle.
    public static boolean canUse(GameView game, PlayerView player) {
        val skillCheck = game.activeSkillCheck();
        return CharacterAbilities.canUseMiracle(game, player, BLIND_DEVOTION)
                && CharacterAbilities.canAffectSkillCheck(game)
                && skillCheck.result() == null
                && skillCheck.totalStrength() == null
                && skillCheck.zeroStrengthColor() == null;
    }

    /**
     * @return the choice of a skill type or nothing, for the player who can use "Blind Devotion". He names it without
     * seeing the cards, so every type is offered
     */
    public static Followup offer(GameView game) {
        return game.players().stream()
                .filter(player -> canUse(game, player))
                .findFirst()
                .map(chief -> {
                    val options = new ArrayList<Event>();
                    Arrays.stream(SkillCardColor.values())
                            .map(color -> new BlindDevotionEvent(chief.number(), color))
                            .forEach(options::add);
                    options.add(new NoOpEvent(chief.number()));
                    return one(options.toArray(Event[]::new));
                })
                .orElse(NONE);
    }

    @Override
    protected boolean isValid() {
        return canUse(game, player) && event.color() != null;
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(BLIND_DEVOTION);
        ops.skillCheck().zeroStrength(event.color());
        return NONE;
    }
}
