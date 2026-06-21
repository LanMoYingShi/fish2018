package com.fongmi.android.tv.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BackupFilesTest {

    @Test
    public void isBackupName_acceptsCurrentAndLegacyBackupNames() {
        assertTrue(BackupFiles.isBackupName("WebHomeTV-5da881-2026-06-21.bk.gz"));
        assertTrue(BackupFiles.isBackupName("WebHomeTV-2026-06-21.bk.gz"));
        assertTrue(BackupFiles.isBackupName("tv-2026-06-21.bk.gz"));
    }

    @Test
    public void isBackupName_rejectsNonBackupNames() {
        assertFalse(BackupFiles.isBackupName("WebHomeTV-5da881-2026-06-21.bk"));
        assertFalse(BackupFiles.isBackupName("tv-2026-06-21.bk"));
        assertFalse(BackupFiles.isBackupName("tvdata-2026-06-21.bk.gz"));
        assertFalse(BackupFiles.isBackupName("backup-tv-2026-06-21.bk.gz"));
        assertFalse(BackupFiles.isBackupName(null));
    }

    @Test
    public void isCurrentDeviceBackupName_acceptsOnlyCurrentDeviceGeneratedBackups() {
        assertTrue(BackupFiles.isCurrentDeviceBackupName("WebHomeTV-5da881-2026-06-21.bk.gz", "5da881"));

        assertFalse(BackupFiles.isCurrentDeviceBackupName("WebHomeTV-other-2026-06-21.bk.gz", "5da881"));
        assertFalse(BackupFiles.isCurrentDeviceBackupName("tv-2026-06-21.bk.gz", "5da881"));
        assertFalse(BackupFiles.isCurrentDeviceBackupName("WebHomeTV-5da881-2026-06-21.bk", "5da881"));
        assertFalse(BackupFiles.isCurrentDeviceBackupName("WebHomeTV-5da881-2026-06-21.bk.gz", ""));
        assertFalse(BackupFiles.isCurrentDeviceBackupName(null, "5da881"));
    }
}
