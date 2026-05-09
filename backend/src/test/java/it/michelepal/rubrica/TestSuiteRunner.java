package it.michelepal.rubrica;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.engine.discovery.DiscoverySelectors;

import it.michelepal.rubrica.repository.AppUserRepositoryTest;
import it.michelepal.rubrica.repository.ContactRepositoryTest;
import it.michelepal.rubrica.repository.TagRepositoryTest;
import it.michelepal.rubrica.service.ContactServiceTest;
import it.michelepal.rubrica.service.InputNormalizerTest;
import it.michelepal.rubrica.service.TagServiceTest;

/**
 * Test Suite Runner - Esegue tutti i test JUnit della suite Rubrica
 */
public class TestSuiteRunner {

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║           RUBRICA TEST SUITE EXECUTION                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(
                // Service Tests
                DiscoverySelectors.selectClass(InputNormalizerTest.class),
                DiscoverySelectors.selectClass(TagServiceTest.class),
                DiscoverySelectors.selectClass(ContactServiceTest.class),
                // Repository Tests
                DiscoverySelectors.selectClass(AppUserRepositoryTest.class),
                DiscoverySelectors.selectClass(TagRepositoryTest.class),
                DiscoverySelectors.selectClass(ContactRepositoryTest.class)
            )
            .build();

        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();
        printResults(summary);
    }

    private static void printResults(TestExecutionSummary summary) {
        long testsFound = summary.getTestsFoundCount();
        long testsStarted = summary.getTestsStartedCount();
        long testsSuccessful = summary.getTestsSucceededCount();
        long testsFailed = summary.getTestsFailedCount();
        long testsSkipped = summary.getTestsSkippedCount();
        long testsAborted = summary.getTestsAbortedCount();

        System.out.println("┌────────────────────────────────────────────────────────────┐");
        System.out.println("│ TEST RESULTS SUMMARY                                       │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.printf("│ Tests Found:      %-45d │\n", testsFound);
        System.out.printf("│ Tests Started:    %-45d │\n", testsStarted);
        System.out.printf("│ Tests Successful: %-45d │\n", testsSuccessful);
        System.out.printf("│ Tests Failed:     %-45d │\n", testsFailed);
        System.out.printf("│ Tests Skipped:    %-45d │\n", testsSkipped);
        System.out.printf("│ Tests Aborted:    %-45d │\n", testsAborted);
        System.out.println("├────────────────────────────────────────────────────────────┤");

        if (testsFailed == 0 && testsAborted == 0) {
            System.out.println("│ ✓ ALL TESTS PASSED SUCCESSFULLY                         │");
            System.out.println("└────────────────────────────────────────────────────────────┘\n");
            System.exit(0);
        } else {
            System.out.println("│ ✗ SOME TESTS FAILED                                        │");
            System.out.println("└────────────────────────────────────────────────────────────┘\n");

            if (!summary.getFailures().isEmpty()) {
                System.out.println("FAILURES:\n");
                summary.getFailures().forEach(failure -> {
                    System.out.println("  ✗ " + failure.getTestIdentifier().getDisplayName());
                    System.out.println("    Reason: " + failure.getException().getMessage());
                    System.out.println();
                });
            }

            System.exit(1);
        }
    }
}
