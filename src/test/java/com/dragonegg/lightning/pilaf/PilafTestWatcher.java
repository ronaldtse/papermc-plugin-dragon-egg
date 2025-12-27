package com.dragonegg.lightning.pilaf;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;

import java.util.Optional;
import java.util.function.Function;

/**
 * JUnit Extension for DragonEggLightning Integration Test Framework (DITF)
 *
 * Provides logging and monitoring for DITF tests
 */
public class PilafTestWatcher implements TestWatcher, BeforeTestExecutionCallback, AfterTestExecutionCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        String displayName = context.getDisplayName();
        System.out.println("🚀 Starting DITF test: " + displayName);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        String displayName = context.getDisplayName();
        Optional<Throwable> exception = context.getExecutionException();

        if (exception.isPresent()) {
            System.out.println("❌ DITF test failed: " + displayName);
            System.out.println("Error: " + exception.get().getMessage());
        } else {
            System.out.println("✅ DITF test passed: " + displayName);
        }
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        String displayName = context.getDisplayName();
        String disabledReason = reason.orElse("No reason provided");
        System.out.println("⏭️ DITF test disabled: " + displayName + " - " + disabledReason);
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        String displayName = context.getDisplayName();
        System.out.println("✅ DITF test successful: " + displayName);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        String displayName = context.getDisplayName();
        System.out.println("❌ DITF test failed: " + displayName);
        System.out.println("Cause: " + cause.getMessage());
        cause.printStackTrace();
    }
}
