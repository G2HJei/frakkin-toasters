package xyz.zlatanov.frakkintoasters.state.skill;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class SkillCheckHolder {

    private final SkillCheck          type;
    private final List<SkillCardType> cards = new ArrayList<>();
}
