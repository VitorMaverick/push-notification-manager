package br.edu.acad.ifma;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.belongToAnyOf;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packagesOf = PushNotificationManagerApp.class, importOptions = DoNotIncludeTests.class)
class TechnicalStructureTest {

    // prettier-ignore
    @ArchTest
    static final ArchRule respectsTechnicalArchitectureLayers = layeredArchitecture()
        .consideringAllDependencies()
        .layer("Config").definedBy("..config..")
        .layer("Web").definedBy("..web..")
        .optionalLayer("Service").definedBy("..service..")
        .layer("Security").definedBy("..security..")
        .optionalLayer("Persistence").definedBy("..repository..")
        .layer("Domain").definedBy("..domain..")

        .whereLayer("Config").mayNotBeAccessedByAnyLayer()
        .whereLayer("Web").mayOnlyBeAccessedByLayers("Config")
        .whereLayer("Service").mayOnlyBeAccessedByLayers("Web", "Config")
        .whereLayer("Security").mayOnlyBeAccessedByLayers("Config", "Service", "Web")
        .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Service", "Security", "Web", "Config")
        .whereLayer("Domain").mayOnlyBeAccessedByLayers("Persistence", "Service", "Security", "Web", "Config")

        .ignoreDependency(belongToAnyOf(PushNotificationManagerApp.class), alwaysTrue())
        .ignoreDependency(alwaysTrue(), belongToAnyOf(
            br.edu.acad.ifma.config.Constants.class,
            br.edu.acad.ifma.config.ApplicationProperties.class
        ))
        .ignoreDependency(resideInAPackage("..app.."), alwaysTrue())
        .ignoreDependency(resideInAPackage("..adapters.."), alwaysTrue())
        .ignoreDependency(alwaysTrue(), resideInAPackage("..app.."))
        .ignoreDependency(alwaysTrue(), resideInAPackage("..adapters.."));

    @ArchTest
    static final ArchRule domainMustNotDependOnAdapters = noClasses()
        .that()
        .resideInAPackage("..app.domain..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..adapters..");

    @ArchTest
    static final ArchRule portsMustNotDependOnAdapters = noClasses()
        .that()
        .resideInAPackage("..app.port..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..adapters..");
}
