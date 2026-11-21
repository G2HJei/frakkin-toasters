package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.board.PlaceCenturionsEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section;
import xyz.zlatanov.frakkintoasters.event.deck.ReplaceDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardAllSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamageBattlestarEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.*;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.event.player.ExecuteEventProcessor.canBeExecuted;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;

/**
 * The sections of the three Super Crisis Cards without a skill check, which a title holder chooses between. The skill
 * check results of the other eight are in {@code SkillCheckResultFollowups}.
 */
@RequiredArgsConstructor
public class SuperCrisisCardFollowups {

    // "Psychological Warfare" builds the new Destiny deck from this many Treachery cards.
    static final int PSYCHOLOGICAL_WARFARE_TREACHERY = 6;

    private final GameView game;

    public Followup choice(SuperCrisisCard card) {
        val chooser = chooser(card);
        return one(Stream.of(TOP, BOTTOM)
                .map(section -> new ChooseSuperCrisisSectionEvent(chooser, card, section))
                .toArray(Event[]::new));
    }

    public Followup followup(SuperCrisisCard card, Section section) {
        return switch (card) {
            // The CAG: 2 centurions on the Boarding Party track, or he is executed.
            case FIGHTING_BLIND -> section == TOP
                    ? single(new PlaceCenturionsEvent(2))
                    : execute(cag());
            // The President: -1 morale and each player trades 2 Skill Cards for 2 Treachery, or the Cylon players draw 2
            // Treachery and the Destiny deck is rebuilt from Treachery alone. A Cylon player ignores the discard, the
            // negative half (rule 6.1.2.1), and still draws. A Cylon Leader who isn't Infiltrating counts as a revealed
            // Cylon (rule 6.2.1).
            case PSYCHOLOGICAL_WARFARE -> section == TOP
                    ? allOf(single(lose(MORALE, 1)), each(p -> true, p -> game.player(p).isHuman()
                    ? all(discard(p, 2), treachery(p, 2))
                    : single(treachery(p, 2))))
                    : allOf(each(p -> !p.isHuman(), p -> single(treachery(p, 2))),
                    single(new ReplaceDestinyDeckEvent(TREACHERY, PSYCHOLOGICAL_WARFARE_TREACHERY)));
            // The Admiral: -1 morale and Galactica damaged twice, or the President and the Admiral discard their hands.
            case DEMAND_PEACE_MANIFESTO -> section == TOP
                    ? all(lose(MORALE, 1), new DamageBattlestarEvent(), new DamageBattlestarEvent())
                    : presidentAndAdmiralDiscardTheirHands();
            default -> throw new FrakCallTheAdmiralException(card + " has a skill check, not sections");
        };
    }

    private int chooser(SuperCrisisCard card) {
        return switch (card.chooser()) {
            case PRESIDENT -> game.playerNumber(game.president());
            case ADMIRAL -> admiral();
            case CAG -> cag();
            case CURRENT_PLAYER -> game.turn().currentPlayerNumber();
            case null -> throw new FrakCallTheAdmiralException(card + " has no chooser");
        };
    }

    private int admiral() {
        return game.playerNumber(game.admiral());
    }

    private int cag() {
        return game.playerNumber(game.cag());
    }

    private Followup execute(int playerNumber) {
        return canBeExecuted(game, playerNumber) ? single(new ExecuteEvent(playerNumber)) : NONE;
    }

    // One discard when the same player holds both titles.
    private Followup presidentAndAdmiralDiscardTheirHands() {
        val president = game.playerNumber(game.president());
        val admiral = admiral();
        return president == admiral
                ? single(new DiscardAllSkillCardsEvent(president))
                : all(new DiscardAllSkillCardsEvent(president), new DiscardAllSkillCardsEvent(admiral));
    }

    // Clockwise from the current player.
    private Followup each(Predicate<PlayerView> who, IntFunction<Followup> followup) {
        return all(playersFrom(game, game.turn().currentPlayerNumber()).stream()
                .filter(who)
                .map(player -> followup.apply(player.number()))
                .toArray(Followup[]::new));
    }
}
