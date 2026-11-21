package xyz.zlatanov.frakkintoasters.event.action.quorum;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.DestroyRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyCenturionEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyHeavyRaiderEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.ship.Centurion;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

public class AuthorizationOfBrutalForceEventProcessor extends QuorumCardEventProcessor<AuthorizationOfBrutalForceEvent> {

    private static final int RAIDERS = 3;

    public static boolean canPlay(GameView game) {
        return !raiders(game).isEmpty() || !heavyRaiders(game).isEmpty() || !centurions(game).isEmpty();
    }

    @Override
    public QuorumCard card() {
        return QuorumCard.AUTHORIZATION_OF_BRUTAL_FORCE;
    }

    @Override
    protected boolean isValidPlay() {
        val ids = event.shipIds();
        if (ids == null || ids.isEmpty() || ids.stream().anyMatch(Objects::isNull) || new HashSet<>(ids).size() != ids.size()) {
            return false;
        }
        if (ids.size() == 1 && (heavyRaiders(game).contains(ids.getFirst()) || centurions(game).contains(ids.getFirst()))) {
            return true;
        }
        // 3 raiders, or every raider when fewer are left, from any area
        val raiders = raiders(game);
        return raiders.containsAll(ids) && ids.size() == Math.min(RAIDERS, raiders.size());
    }

    @Override
    protected Followup resolve() {
        val ids = event.shipIds();
        Event destroy;
        if (heavyRaiders(game).contains(ids.getFirst())) {
            destroy = new DestroyHeavyRaiderEvent(ids.getFirst());
        } else if (centurions(game).contains(ids.getFirst())) {
            destroy = new DestroyCenturionEvent(ids.getFirst());
        } else {
            destroy = new DestroyRaidersEvent(Set.copyOf(ids));
        }
        return all(single(destroy), rollDie(new AuthorizationOfBrutalForceResultEvent()));
    }

    private static Set<Integer> raiders(GameView game) {
        return game.boards().galactica().shipsInSpace(Raider.class).stream().map(Ship::id).collect(toSet());
    }

    private static Set<Integer> heavyRaiders(GameView game) {
        return game.boards().galactica().shipsInSpace(HeavyRaider.class).stream().map(Ship::id).collect(toSet());
    }

    private static Set<Integer> centurions(GameView game) {
        return game.boards().galactica().boardingPartyTrack().keySet().stream().map(Centurion::id).collect(toSet());
    }
}
