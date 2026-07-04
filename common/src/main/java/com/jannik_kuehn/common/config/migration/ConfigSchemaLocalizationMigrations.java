package com.jannik_kuehn.common.config.migration;

import com.jannik_kuehn.common.config.StructuredConfigurationDocument;

import java.util.Locale;

/**
 * Localization-specific config schema migrations.
 */
final class ConfigSchemaLocalizationMigrations {
    /**
     * Localization path for irreversible transfer warnings.
     */
    private static final String TRANSFER_WARNING_PATH = "messages.message.command.loritimeadmin.transfer.warning";

    private ConfigSchemaLocalizationMigrations() {
    }

    /* default */ static ConfigMigration transferWarning() {
        return ConfigMigration.from(1)
                .operation(ConfigSchemaLocalizationMigrations::addTransferWarningMessage)
                .build();
    }

    private static void addTransferWarningMessage(final StructuredConfigurationDocument document) {
        if (document.contains(TRANSFER_WARNING_PATH)) {
            return;
        }

        if (localeTag(document).startsWith("de")) {
            document.set(TRANSFER_WARNING_PATH, "<#FF3232>WARNUNG: Dieser Transfer schreibt gespeicherte "
                    + "Verlaufsdaten um und kann von LoriTime nicht rueckgaengig gemacht werden. Erstelle und pruefe "
                    + "ein Backup, bevor du /lta confirm ausfuehrst.");
            return;
        }

        document.set(TRANSFER_WARNING_PATH, "<#FF3232>WARNING: This transfer rewrites stored history and cannot "
                + "be reverted by LoriTime. Create and verify a backup before running /lta confirm.");
    }

    private static String localeTag(final StructuredConfigurationDocument document) {
        final Object locale = document.get("locale");
        return locale instanceof final String value ? value.toLowerCase(Locale.ROOT) : "";
    }
}
