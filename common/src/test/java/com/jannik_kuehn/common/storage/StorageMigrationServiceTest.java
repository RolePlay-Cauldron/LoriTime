package com.jannik_kuehn.common.storage;

import com.github.roleplaycauldron.spellbook.core.logger.LoggerFactory;
import com.jannik_kuehn.common.LoriTimePlugin;
import com.jannik_kuehn.common.config.Configuration;
import com.jannik_kuehn.common.exception.StorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
class StorageMigrationServiceTest {

    private static final String TABLE_PREFIX = "loritime";

    private static final String CUSTOM_TABLE_PREFIX = "custom_prefix";

    @TempDir
    private File dataFolder;

    @Test
    void importsFlatFilesToSqliteAndDoesNotRepeatImport() throws IOException, StorageException, SQLException {
        final File legacyDataFolder = new File(dataFolder, "data");
        Files.createDirectories(legacyDataFolder.toPath());
        Files.writeString(new File(legacyDataFolder, "names.yml").toPath(), """
                Lorias_: 44174cf6-e76c-4994-899c-3387284ecd62
                Kenhir: ead2874d-f195-4f46-879f-1ff9dd545a02
                """);
        Files.writeString(new File(legacyDataFolder, "time.yml").toPath(), """
                ead2874d-f195-4f46-879f-1ff9dd545a02: 3302
                44174cf6-e76c-4994-899c-3387284ecd62: 46194
                """);
        final Configuration config = config();
        final LoriTimePlugin plugin = pluginWithConfig(config);

        final StorageMigrationService service = new StorageMigrationService(plugin, dataFolder);
        service.migrateIfNecessary();
        service.migrateIfNecessary();

        verify(config, never()).setValue("storageMethod", "sqlite");
        verify(config).setValue("storageMigration.legacyFlatFileImport", false);
        assertEquals(expectedMigrationResult(), migrationResult(legacyDataFolder), "legacy flat files should be imported once");
    }

    @Test
    void configuredSqlStorageDoesNotImportLegacyFlatFilesIntoSqlite() throws IOException {
        final File legacyDataFolder = new File(dataFolder, "data");
        Files.createDirectories(legacyDataFolder.toPath());
        final File namesFile = new File(legacyDataFolder, "names.yml");
        final File timeFile = new File(legacyDataFolder, "time.yml");
        Files.writeString(namesFile.toPath(), "Lorias_: 44174cf6-e76c-4994-899c-3387284ecd62\n");
        Files.writeString(timeFile.toPath(), "44174cf6-e76c-4994-899c-3387284ecd62: 46194\n");
        final Configuration config = sqlConfig();
        final LoriTimePlugin plugin = pluginWithConfig(config);

        final StorageMigrationService service = new StorageMigrationService(plugin, dataFolder);

        assertThrows(StorageException.class, service::migrateIfNecessary);
        verify(config, never()).setValue("storageMethod", "sqlite");
        assertTrue(namesFile.exists(), "Expected SQL-selected migration to leave flat names file in place");
        assertTrue(timeFile.exists(), "Expected SQL-selected migration to leave flat time file in place");
        assertFalse(new File(dataFolder, "loritime.db").exists(), "Expected SQL-selected migration not to create SQLite");
    }

    private LoriTimePlugin pluginWithConfig(final Configuration config) {
        final LoriTimePlugin plugin = mock(LoriTimePlugin.class);
        when(plugin.getLoggerFactory()).thenReturn(new LoggerFactory(Logger.getLogger("test")));
        when(plugin.getConfig()).thenReturn(config);
        return plugin;
    }

    private Configuration config() {
        final Configuration config = mock(Configuration.class);
        when(config.getString("storageMethod", "sqlite")).thenReturn("sqlite");
        when(config.getString("storageMethod")).thenReturn("sqlite");
        when(config.getString("data.tablePrefix", TABLE_PREFIX)).thenReturn(CUSTOM_TABLE_PREFIX);
        when(config.getBoolean("storageMigration.legacyFlatFileImport", false)).thenReturn(true, false);
        return config;
    }

    private Configuration sqlConfig() {
        final Configuration config = mock(Configuration.class);
        when(config.getString("storageMethod", "sqlite")).thenReturn("mysql");
        when(config.getString("storageMethod")).thenReturn("mysql");
        when(config.getString("data.tablePrefix", TABLE_PREFIX)).thenReturn(TABLE_PREFIX);
        when(config.getString("data.host")).thenReturn("localhost");
        when(config.getInt("data.port")).thenReturn(3306);
        when(config.getString("data.database")).thenReturn("minecraft");
        when(config.getString("data.user")).thenReturn("");
        when(config.getBoolean("storageMigration.legacyFlatFileImport", false)).thenReturn(false);
        return config;
    }

    private boolean hasLegacyStorageBackupDirectory() {
        final File backupFolder = new File(dataFolder, "backups");
        final File[] legacyBackups = backupFolder.listFiles((directory, name) -> name.startsWith("legacy-storage-"));
        return legacyBackups != null && legacyBackups.length > 0;
    }

    private MigrationResult migrationResult(final File legacyDataFolder) throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, "loritime.db"))) {
            return new MigrationResult(
                    new File(legacyDataFolder, "names.yml").exists(),
                    new File(legacyDataFolder, "time.yml").exists(),
                    new File(legacyDataFolder, "names.yml.migrated").exists(),
                    new File(legacyDataFolder, "time.yml.migrated").exists(),
                    hasLegacyStorageBackupDirectory(),
                    countRows(connection, TABLE_PREFIX + "_player"),
                    countRows(connection, TABLE_PREFIX + "_time"),
                    countRows(connection, TABLE_PREFIX + "_time_adjustment"),
                    sumAdjustment(connection, "Lorias_"),
                    sumAdjustment(connection, "Kenhir"),
                    singleString(connection, "SELECT `name` FROM `" + TABLE_PREFIX + "_player` WHERE `name` = 'Lorias_'"),
                    singleString(connection, "SELECT `name` FROM `" + TABLE_PREFIX + "_player` WHERE `name` = 'Kenhir'"),
                    singleString(connection, "SELECT `reason` FROM `" + TABLE_PREFIX + "_time_adjustment`"),
                    singleString(connection, "SELECT `scope_type` FROM `" + TABLE_PREFIX + "_time_adjustment`"),
                    singleString(connection, "SELECT w.`world` FROM `" + TABLE_PREFIX + "_time_adjustment` a "
                            + "JOIN `" + TABLE_PREFIX + "_world` w ON w.`id` = a.`world_id`"));
        }
    }

    private MigrationResult expectedMigrationResult() {
        return new MigrationResult(false, false, true, true, false, 2, 0, 2, 46_194, 3302,
                "Lorias_", "Kenhir", "LEGACY_IMPORT", "WORLD", "global");
    }

    private int countRows(final Connection connection, final String table) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM `" + table + "`")) {
            requireRow(resultSet, "count query should return a row");
            return resultSet.getInt(1);
        }
    }

    private String singleString(final Connection connection, final String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            requireRow(resultSet, "string query should return a row");
            return resultSet.getString(1);
        }
    }

    private int sumAdjustment(final Connection connection, final String playerName) throws SQLException {
        final String sql = "SELECT SUM(a.`amount_seconds`) "
                + "FROM `" + TABLE_PREFIX + "_time_adjustment` a "
                + "JOIN `" + TABLE_PREFIX + "_player` p ON p.`id` = a.`player_id` "
                + "WHERE p.`name` = '" + playerName + "'";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            requireRow(resultSet, "time sum query should return a row");
            return resultSet.getInt(1);
        }
    }

    private void requireRow(final ResultSet resultSet, final String message) throws SQLException {
        if (!resultSet.next()) {
            throw new AssertionError(message);
        }
    }

    private record MigrationResult(
            boolean namesFileExists,
            boolean timeFileExists,
            boolean migratedNamesFileExists,
            boolean migratedTimeFileExists,
            boolean legacyStorageBackupDirectoryExists,
            int players,
            int timeEntries,
            int adjustments,
            int loriasTime,
            int kenhirTime,
            String loriasName,
            String kenhirName,
            String reason,
            String scopeType,
            String world) {
    }
}
