package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.Title;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The card or ability an event exists for, where its fields don't name it, so the table can say why a player is asked:
 * "Forward Thinker" beside "Lee Adama may activate his location". Exactly one element is set ({@link EventSources}).
 * On a constant of an enum that an event holds, it is the source of that event, e.g. what granted a {@code GrantEvent}.
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, FIELD })
public @interface Source {

    CharacterAbility[] ability() default {};

    SkillCardType[] skillCard() default {};

    CrisisCard[] crisisCard() default {};

    Location[] location() default {};

    Title[] title() default {};
}
