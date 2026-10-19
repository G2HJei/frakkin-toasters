package xyz.zlatanov.frakkintoasters.event;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.*;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.*;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;

@NoArgsConstructor(access = PRIVATE)
public class FollowupUtils {


    public static Followup skillCheckFollowup(GameView game) {
        val followupEvents = new ArrayList<Followup>();
        followupEvents.add(single(new PlayFromDestinyDeckEvent()));
        followupEvents.add(addPlayerDecisions(game, List.of(PlayRestoreOrderEvent.class, PlayRecklessSkillCardEvent.class)));
        followupEvents.add(addPlayerDecisions(game, List.of(PlaySkillsEvent.class)));
        followupEvents.add(single(new ShuffleAndDivideCardsEvent()));
        followupEvents.add(single(new ResolveSkillCheckEvent()));
        return all(followupEvents.toArray(Followup[]::new));
    }

    public static Followup skillCheckAbilityFollowup(PlayerView player, SkillCard skillCard) {
        return switch (skillCard.type()) {
            case DOGFIGHT -> single(new PlayerDecisionEvent<>(player.number(), DogfightEvent.class));
            case FORCE_THEIR_HAND -> player.isHuman()
                    ? single(new PlayerDecisionEvent<>(player.number(), ForceTheirHandEvent.class))
                    : Followup.NONE;
            case QUICK_THINKING -> single(new PlayerDecisionEvent<>(player.number(), QuickThinkingEvent.class));
            case A_BETTER_MACHINE -> single(new ABetterMachineEvent());
            case BAIT -> single(new BaitEvent());
            case DRADIS_CONTACT -> single(new DradisContactEvent());
            case EXPLOIT_A_WEAKNESS -> single(new PlayerDecisionEvent<>(player.number(), ExploitAWeaknessEvent.class));
            case PERSONAL_VICES -> single(new PersonalVicesEvent());
            case VIOLENT_OUTBURSTS -> single(new ViolentOutburstsEvent());
            case PROTECT_THE_FLEET -> single(new PlayerDecisionEvent<>(player.number(), ProtectTheFleetEvent.class));
            case RED_TAPE -> single(new RedTapeEvent());
            case TRUST_INSTINCTS -> single(new TrustInstinctsEvent());
            case INSTALL_UPGRADES,
                 IRON_WILL,
                 ALL_HANDS_ON_DECK,
                 ESTABLISH_NETWORK -> Followup.NONE;
            default -> throw new FrakCallTheAdmiralException();
        };
    }

    private static Followup addPlayerDecisions(GameView game, List<Class<? extends PlayerEvent>> eventClasses) {
        val followups = new ArrayList<Followup>();
        var decidingPlayer = game.currentPlayerNumber();
        do {
            val playerEvents = new PlayerEvent[eventClasses.size()];
            decidingPlayer = decidingPlayer >= game.players().size() ? 1 : ++decidingPlayer;
            for (int i = 0; i < eventClasses.size(); i++) {
                val event = eventClasses.get(i);
                playerEvents[i] = new PlayerDecisionEvent<>(decidingPlayer, event);
            }
            followups.add(one(playerEvents));
        } while (decidingPlayer != game.currentPlayerNumber());
        return all(followups.toArray(Followup[]::new));
    }
}
