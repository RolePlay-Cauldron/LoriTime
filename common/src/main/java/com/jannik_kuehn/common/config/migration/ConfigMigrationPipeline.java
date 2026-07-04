package com.jannik_kuehn.common.config.migration;

import com.jannik_kuehn.common.config.StructuredConfigurationDocument;

/**
 * Applies ordered migrations for a configuration schema.
 */
public final class ConfigMigrationPipeline {
    /**
     * Schema metadata used by this pipeline.
     */
    private final ConfigSchema schema;

    /**
     * Creates a migration pipeline.
     *
     * @param schema schema metadata
     */
    public ConfigMigrationPipeline(final ConfigSchema schema) {
        this.schema = schema;
    }

    /**
     * Migrates a document to the latest schema version.
     *
     * @param document document to mutate
     * @return migration result
     */
    public ConfigMigrationResult migrate(final StructuredConfigurationDocument document) {
        int currentVersion = detectVersion(document);
        boolean changed = false;

        for (final ConfigMigration migration : schema.migrations()) {
            if (migration.getFromVersion() >= currentVersion && migration.getFromVersion() < schema.latestVersion()) {
                migration.apply(document);
                currentVersion = migration.getToVersion();
                changed = true;
            }
        }

        if (!Integer.valueOf(schema.latestVersion()).equals(document.get(schema.versionPath()))) {
            document.set(schema.versionPath(), schema.latestVersion());
            changed = true;
        }

        return new ConfigMigrationResult(document, changed);
    }

    private int detectVersion(final StructuredConfigurationDocument document) {
        final Object value = document.get(schema.versionPath());
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String string) {
            try {
                return Integer.parseInt(string);
            } catch (final NumberFormatException ignored) {
                return schema.legacyBaselineVersion();
            }
        }
        return schema.legacyBaselineVersion();
    }
}
