package com.cinema.booking;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

@AnalyzeClasses(packages = "com.cinema.booking", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domainDependsOnlyOnJdk = ArchRuleDefinition.classes()
            .that().resideInAPackage("com.cinema.booking.domain..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("java..", "com.cinema.booking.domain..");

    @ArchTest
    static final ArchRule applicationDependsOnlyOnAllowedPackages = ArchRuleDefinition.classes()
            .that().resideInAPackage("com.cinema.booking.application..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("java..",
                    "com.cinema.booking.application..",
                    "com.cinema.booking.domain..",
                    "org.springframework.stereotype..",
                    "org.springframework.transaction..");

    @ArchTest
    static final ArchRule inboundAdaptersDoNotUseOutboundAdapters = ArchRuleDefinition.noClasses()
            .that().resideInAnyPackage("com.cinema.booking.adapter.in..")
            .should().dependOnClassesThat().resideInAnyPackage("com.cinema.booking.adapter.out..");
}
