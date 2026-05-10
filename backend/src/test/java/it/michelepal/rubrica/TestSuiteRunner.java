package it.michelepal.rubrica;

import it.michelepal.rubrica.controller.RestApiIntegrationTest;
import it.michelepal.rubrica.repository.AppUserRepositoryTest;
import it.michelepal.rubrica.repository.ContactRepositoryTest;
import it.michelepal.rubrica.repository.TagRepositoryTest;
import it.michelepal.rubrica.service.ContactServiceTest;
import it.michelepal.rubrica.service.InputNormalizerTest;
import it.michelepal.rubrica.service.TagServiceTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * JUnit 5 test suite per eseguire tutti i test della rubrica.
 */
@Suite
@DisplayName("Rubrica Test Suite")
@SelectClasses({
    InputNormalizerTest.class,
    TagServiceTest.class,
    ContactServiceTest.class,
    AppUserRepositoryTest.class,
    TagRepositoryTest.class,
    ContactRepositoryTest.class,
    RestApiIntegrationTest.class
})
public class TestSuiteRunner {

    @BeforeAll
    static void suiteStarted() {
        System.out.println();
        System.out.println("===========================================================");
        System.out.println("RUBRICA TEST SUITE STARTED");
        System.out.println("===========================================================");
        System.out.println("Esecuzione di 7 classi di test:");
        System.out.println("  - InputNormalizerTest");
        System.out.println("  - TagServiceTest");
        System.out.println("  - ContactServiceTest");
        System.out.println("  - AppUserRepositoryTest");
        System.out.println("  - TagRepositoryTest");
        System.out.println("  - ContactRepositoryTest");
        System.out.println("  - RestApiIntegrationTest");
        System.out.println();
    }

    @AfterAll
    static void suiteFinished() {
        System.out.println();
        System.out.println("===========================================================");
        System.out.println("RUBRICA TEST SUITE COMPLETED");
        System.out.println("===========================================================");
        System.out.println();
    }
}
