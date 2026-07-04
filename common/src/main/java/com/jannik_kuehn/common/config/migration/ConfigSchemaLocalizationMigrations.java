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

    /**
     * Localization prefix for deleteHistory messages.
     */
    private static final String DELETE_HISTORY_PREFIX = "messages.message.command.loritimeadmin.deleteHistory.";

    private ConfigSchemaLocalizationMigrations() {
    }

    /* default */ static ConfigMigration transferWarning() {
        return ConfigMigration.from(1)
                .operation(ConfigSchemaLocalizationMigrations::addTransferWarningMessage)
                .build();
    }

    /* default */ static ConfigMigration deleteHistoryMessages() {
        return ConfigMigration.from(2)
                .operation(ConfigSchemaLocalizationMigrations::addDeleteHistoryMessages)
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

    private static void addDeleteHistoryMessages(final StructuredConfigurationDocument document) {
        final boolean german = localeTag(document).startsWith("de");
        putIfMissing(document, "usage", german
                ? "<#A4A4A4>Benutze <red>/loritimeadmin deleteHistory [player] server:<source> "
                + "[world:<source>] [time:<range>] <#A4A4A4>um das Loeschen von Verlaufsdaten vorzubereiten. "
                + "Ohne Spieler werden alle Spieler beruecksichtigt."
                : "<#A4A4A4>Use <red>/loritimeadmin deleteHistory [player] server:<source> "
                + "[world:<source>] [time:<range>] <#A4A4A4>to preview deleting scoped history. "
                + "Omit player to delete matching history for all players.");
        putIfMissing(document, "preview", german
                ? "<#A4A4A4>DeleteHistory-Vorschau fuer <#50CBAB>[player]<#A4A4A4>: Scope "
                + "<#50CBAB>[scope]<#A4A4A4>, Zeitraum <#50CBAB>[range]<#A4A4A4>, "
                + "<#50CBAB>[sessions] <#A4A4A4>Sitzungen, <#50CBAB>[adjustments] <#A4A4A4>Anpassungen, "
                + "<#50CBAB>[players] <#A4A4A4>Spieler. Nutze <red>/lta confirm <#A4A4A4>innerhalb "
                + "von 15 Sekunden zum Anwenden."
                : "<#A4A4A4>DeleteHistory preview for <#50CBAB>[player]<#A4A4A4>: scope "
                + "<#50CBAB>[scope]<#A4A4A4>, range <#50CBAB>[range]<#A4A4A4>, "
                + "<#50CBAB>[sessions] <#A4A4A4>sessions, <#50CBAB>[adjustments] <#A4A4A4>adjustments, "
                + "<#50CBAB>[players] <#A4A4A4>players. Use <red>/lta confirm <#A4A4A4>within "
                + "15 seconds to apply.");
        putIfMissing(document, "warning", german
                ? "<#FF3232>WARNUNG: Dieses Loeschen entfernt gespeicherte Verlaufsdaten und kann von LoriTime "
                + "nicht rueckgaengig gemacht werden. Erstelle und pruefe ein Backup, bevor du /lta confirm ausfuehrst."
                : "<#FF3232>WARNING: This delete removes stored history and cannot be reverted by LoriTime. "
                + "Create and verify a backup before running /lta confirm.");
        putIfMissing(document, "success", german
                ? "<#1AFA29>Verlaufsdaten geloescht: <#50CBAB>[sessions] <#1AFA29>Sitzungen, "
                + "<#50CBAB>[adjustments] <#1AFA29>Anpassungen, <#50CBAB>[players] <#1AFA29>Spieler."
                : "<#1AFA29>History deleted: <#50CBAB>[sessions] <#1AFA29>sessions, "
                + "<#50CBAB>[adjustments] <#1AFA29>adjustments, <#50CBAB>[players] <#1AFA29>players.");
        putIfMissing(document, "failure", german
                ? "<#FF3232>Loeschen der Verlaufsdaten fehlgeschlagen."
                : "<#FF3232>Storage history delete failed.");
    }

    private static void putIfMissing(final StructuredConfigurationDocument document,
                                     final String suffix,
                                     final String value) {
        final String path = DELETE_HISTORY_PREFIX + suffix;
        if (!document.contains(path)) {
            document.set(path, value);
        }
    }
}
