package xyz.zlatanov.frakkintoasters.event.endgame;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.EnumSet;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ADMIRALS_QUARTERS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.Location.FTL_CONTROL;
import static xyz.zlatanov.frakkintoasters.state.card.MotiveCard.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.ENGINEERING;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TACTICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;
import static xyz.zlatanov.frakkintoasters.state.track.JumpPreparation.MINUS_1;
import static xyz.zlatanov.frakkintoasters.state.track.JumpPreparation.MINUS_3;

/**
 * When a Cylon Leader can reveal a Motive Card (rule 6.2.2.1), as printed on the cards. He may reveal one any time its
 * requirement is met, in the middle of resolving something too ({@code RevealMotiveCardEvent}), and at the end of the
 * game, after the Personal Goals (rule 16.3.1). The cards that say "if the game is over" wait for the end.
 */
@NoArgsConstructor(access = PRIVATE)
public final class MotiveCards {

    private static final Set<MotiveCard> ONLY_AT_THE_END = EnumSet.of(A_FALSE_SENSE_OF_SECURITY, FIGHT_WITH_HONOR,
            HARVEST_THEIR_RESOURCES, SAVOR_THEIR_DEMISE, SUBJECTS_TO_STUDY, IMPROVE_EFFICIENCY, KEEP_THEM_DOCILE,
            LEARN_TO_CHERISH, PRESSURE_THEIR_LEADERS);

    public static boolean canReveal(GameView game, PlayerView player, MotiveCard card) {
        return !ONLY_AT_THE_END.contains(card) && canRevealAtTheEnd(game, player, card);
    }

    public static boolean canRevealAtTheEnd(GameView game, PlayerView player, MotiveCard card) {
        val galactica = game.boards().galactica();
        val distance = game.distanceTraveled();
        return switch (card) {
            case A_FALSE_SENSE_OF_SECURITY -> galactica.morale() >= 3;
            case A_JUSTIFIED_RESPONSE -> {
                // the blue spaces of the Jump Preparation track are the ones where the FTL jump costs population
                val onBlueSpace = galactica.jumpPreparation() == MINUS_3 || galactica.jumpPreparation() == MINUS_1;
                val noCylonShips = galactica.shipsInSpace(Raider.class).isEmpty()
                        && galactica.shipsInSpace(HeavyRaider.class).isEmpty()
                        && galactica.shipsInSpace(Basestar.class).isEmpty();
                yield onBlueSpace && noCylonShips;
            }
            case FIGHT_WITH_HONOR -> countOf(player, TREACHERY) >= 3;
            case HARVEST_THEIR_RESOURCES -> galactica.food() >= 2;
            case NO_UNNECESSARY_FORCE -> distance >= 5 && galactica.boardingPartyTrack().isEmpty();
            case SAVOR_THEIR_DEMISE -> distance >= 7;
            case SUBJECTS_TO_STUDY -> galactica.population() >= 4;
            case END_THE_CHASE -> galactica.isDamaged(FTL_CONTROL) || galactica.isDamaged(ADMIRALS_QUARTERS);
            case IMPROVE_EFFICIENCY -> countOf(player, POLITICS) >= 1 && countOf(player, TACTICS) >= 1
                    && countOf(player, ENGINEERING) >= 1;
            case KEEP_THEM_DOCILE -> galactica.food() <= 4;
            case LEARN_TO_CHERISH -> galactica.population() <= 6;
            case PRESSURE_THEIR_LEADERS -> galactica.morale() <= 5;
            case MAKE_AN_ALLY -> !player.mutinyCards().cards().isEmpty() && game.players().stream()
                    .filter(other -> other.number() != player.number())
                    .anyMatch(other -> game.locate(other.character()).filter(BRIG::equals).isPresent());
        };
    }

    private static long countOf(PlayerView player, xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor color) {
        return player.skillCards().cards().stream().filter(card -> card.type().color() == color).count();
    }
}
