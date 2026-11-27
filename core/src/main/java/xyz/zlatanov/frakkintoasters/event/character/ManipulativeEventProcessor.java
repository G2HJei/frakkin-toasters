package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextPresidentEvent;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.Title;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEventProcessor.canBecomeAdmiral;
import static xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEventProcessor.canBecomePresident;
import static xyz.zlatanov.frakkintoasters.state.Title.ADMIRAL;
import static xyz.zlatanov.frakkintoasters.state.Title.PRESIDENT;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MANIPULATIVE;

public class ManipulativeEventProcessor extends EventProcessor<ManipulativeEvent> {

    // On her own turn, a title she can take: not one she holds, and not the Admiral's from the "Brig".
    public static boolean canUse(GameView game, PlayerView player, Title title) {
        return player.number() == game.turn().currentPlayerNumber()
                && CharacterAbilities.canUseMiracle(game, player, MANIPULATIVE)
                && switch (title) {
            case ADMIRAL -> canBecomeAdmiral(game, player.character());
            case PRESIDENT -> canBecomePresident(game, player.character());
            case CAG -> false;
        };
    }

    public static Followup offer(GameView game) {
        val current = game.currentPlayer();
        val options = new ArrayList<Event>();
        for (val title : new Title[]{ADMIRAL, PRESIDENT}) {
            if (canUse(game, current, title)) {
                options.add(new ManipulativeEvent(current.number(), title));
            }
        }
        if (options.isEmpty()) {
            return NONE;
        }
        options.add(new NoOpEvent(current.number()));
        return one(options.toArray(Event[]::new));
    }

    /**
     * At the end of the turn, the titles taken with "Manipulative" go back to their previous holders. One who can't hold
     * it any more, e.g. executed or in the "Brig", is passed over: the title goes to the next in line.
     */
    public static Followup giveTitlesBack(GameView game, GameOps ops) {
        val current = game.currentPlayer();
        val successions = new ArrayList<Event>();
        game.turn().borrowedTitles().forEach((title, previousHolder) -> {
            val holder = title == ADMIRAL ? game.admiral() : game.president();
            if (holder != current.character()) {
                return; // she lost it meanwhile, e.g. in the "Brig": it went on as titles do
            }
            if (title == ADMIRAL) {
                if (canBecomeAdmiral(game, previousHolder)) {
                    ops.titles().assignAdmiral(previousHolder);
                } else {
                    successions.add(new DetermineNextAdmiralEvent(current.number()));
                }
            } else if (canBecomePresident(game, previousHolder)) {
                ops.titles().assignPresident(previousHolder);
            } else {
                successions.add(new DetermineNextPresidentEvent(current.number()));
            }
        });
        return all(successions.toArray(Event[]::new));
    }

    @Override
    protected boolean isValid() {
        return event.title() != null && canUse(game, player, event.title());
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(MANIPULATIVE);
        ops.titles().borrow(event.title(), player.character());
        return NONE;
    }
}
