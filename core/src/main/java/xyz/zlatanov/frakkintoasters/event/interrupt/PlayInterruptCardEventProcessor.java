package xyz.zlatanov.frakkintoasters.event.interrupt;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.List;
import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CALCULATIONS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.MAJOR_VICTORY;

public class PlayInterruptCardEventProcessor extends EventProcessor<PlayInterruptCardEvent> {

    @Override
    protected boolean isValid() {
        val window = game.turn().interruptWindow();
        if (window.isEmpty()) {
            return false;
        }
        if (event.card() == null) {
            return event.calculationsModifier() == null;
        }
        return holdsSkillCards(List.of(event.card()))
                && canPlay(game, player, event.card().type(), window.get())
                && isModifierOfTheCard();
    }

    @Override
    public Followup process() {
        if (event.card() != null) {
            ops.interrupts().play(new PlayedInterruptCard(event.playerNumber(), event.card(), event.calculationsModifier()));
        }
        return Followup.NONE;
    }

    public static boolean holdsCardFor(GameView game, PlayerView player, InterruptWindow window) {
        return player.skillCards().cards().stream().anyMatch(card -> canPlay(game, player, card.type(), window));
    }

    public static boolean canPlay(GameView game, PlayerView player, SkillCardType type, InterruptWindow window) {
        if (!player.isHuman() || !window.cards().contains(type)) {
            return false; // revealed Cylons can't use the abilities of Skill Cards (rule 6.1.2.3)
        }
        return switch (type) {
            // "Only 1 "Calculations" card may be used per die roll", which a reroll doesn't change.
            case CALCULATIONS -> !game.turn().dieRoll().cards().contains(CALCULATIONS);
            // A reroll needs a die: not after "Hotshot" set the face.
            case EVASIVE_MANOEUVRES -> RollAbilities.canReroll(game.turn().dieRoll());
            // "The first 4 raiders activating in your space area": the player pilots a viper where raiders are.
            case RUN_INTERFERENCE -> pilotedFighterArea(game, player)
                    .filter(area -> !game.boards().galactica().shipsIn(area, Raider.class).isEmpty())
                    .isPresent();
            // "after you destroy": the player who destroyed it is the one acting. "Limit of 1 a turn."
            case MAJOR_VICTORY -> player.number() == game.turn().actingPlayerNumber()
                    && !game.turn().resolvedInterruptCards().contains(MAJOR_VICTORY);
            default -> true;
        };
    }

    public static Optional<Location> pilotedFighterArea(GameView game, PlayerView player) {
        val galactica = game.boards().galactica();
        return Optional.ofNullable(player.character())
                .flatMap(galactica::pilotedFighter)
                .map(galactica::locate);
    }

    private boolean isModifierOfTheCard() {
        if (event.card().type() != CALCULATIONS) {
            return event.calculationsModifier() == null;
        }
        return event.calculationsModifier() != null && Math.abs(event.calculationsModifier()) == 1; // "add or subtract 1"
    }
}
