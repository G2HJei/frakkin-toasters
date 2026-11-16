package xyz.zlatanov.frakkintoasters.state.card;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.Team;

import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.Team.HUMANS;

/**
 * The Motive Cards of the Cylon Leaders, with the allegiance printed on each (rule 6.2.2). The requirements are in
 * {@code MotiveCards}.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum MotiveCard {
    SUBJECTS_TO_STUDY(CYLONS),
    SAVOR_THEIR_DEMISE(CYLONS),
    NO_UNNECESSARY_FORCE(CYLONS),
    HARVEST_THEIR_RESOURCES(CYLONS),
    FIGHT_WITH_HONOR(CYLONS),
    A_JUSTIFIED_RESPONSE(CYLONS),
    A_FALSE_SENSE_OF_SECURITY(CYLONS),
    PRESSURE_THEIR_LEADERS(HUMANS),
    MAKE_AN_ALLY(HUMANS),
    LEARN_TO_CHERISH(HUMANS),
    KEEP_THEM_DOCILE(HUMANS),
    IMPROVE_EFFICIENCY(HUMANS),
    END_THE_CHASE(HUMANS);

    private final Team allegiance;
}
