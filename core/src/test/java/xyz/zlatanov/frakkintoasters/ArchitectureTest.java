package xyz.zlatanov.frakkintoasters;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import xyz.zlatanov.frakkintoasters.state.Mutates;

import static com.tngtech.archunit.core.domain.JavaCall.Predicates.target;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Keeps the journal complete: replay only works if every state change is a recorded fact. Ops record facts, and only
 * the fact applier changes components. Tests are not analyzed, they set up state directly.
 */
@AnalyzeClasses(packages = "xyz.zlatanov.frakkintoasters", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule onlyTheFactApplierAndTheStateItselfCallMutators =
            noClasses().that().resideOutsideOfPackages("..journal..", "..state..")
                    .should().callMethodWhere(target(annotatedWith(Mutates.class)))
                    .because("state changes have to be recorded as facts: ops record a fact, FactApplier changes the state");

    @ArchTest
    static final ArchRule eventProcessorsDoNotCallMutators =
            noClasses().that().resideInAPackage("..event..")
                    .should().callMethodWhere(target(annotatedWith(Mutates.class)))
                    .because("processors read through GameView and write only through game ops");

    @ArchTest
    static final ArchRule opsDoNotCallMutators =
            noClasses().that().resideInAPackage("..operation..")
                    .should().callMethodWhere(target(annotatedWith(Mutates.class)))
                    .because("ops compute a fact and record it, instead of changing the state themselves");

    @ArchTest
    static final ArchRule stateDoesNotDependOnOpsEventsOrTheEngine =
            noClasses().that().resideInAPackage("..state..")
                    .should().dependOnClassesThat().resideInAnyPackage("..operation..", "..event..", "..engine..")
                    .because("state is the bottom layer");

    @ArchTest
    static final ArchRule eventsDoNotDependOnTheEngine =
            noClasses().that().resideInAPackage("..event..")
                    .should().dependOnClassesThat().resideInAPackage("..engine..")
                    .because("processors read through GameView and write only through game ops, they never see the journal");
}
