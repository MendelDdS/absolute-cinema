package com.cinema.catalog;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

@AnalyzeClasses(packages = "com.cinema.catalog", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domainDependsOnlyOnJdk = ArchRuleDefinition.classes()
            .that().resideInAPackage("com.cinema.catalog.domain..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("java..", "com.cinema.catalog.domain..");

    @ArchTest
    static final ArchRule applicationDependsOnlyOnAllowedPackages = ArchRuleDefinition.classes()
            .that().resideInAPackage("com.cinema.catalog.application..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("java..",
                    "com.cinema.catalog.application..",
                    "com.cinema.catalog.domain..",
                    "org.springframework.stereotype..",
                    "org.springframework.transaction..");

    @ArchTest
    static final ArchRule inboundAdaptersDoNotUseOutboundAdapters = ArchRuleDefinition.noClasses()
            .that().resideInAnyPackage("com.cinema.catalog.adapter.in..")
            .should().dependOnClassesThat().resideInAnyPackage("com.cinema.catalog.adapter.out..")
            .allowEmptyShould(true);
}
