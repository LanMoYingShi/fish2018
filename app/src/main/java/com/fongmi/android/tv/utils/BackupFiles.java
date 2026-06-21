package com.fongmi.android.tv.utils;

import java.io.File;

public final class BackupFiles {

    public static final String BACKUP_PREFIX = "WebHomeTV-";
    public static final String LEGACY_BACKUP_PREFIX = "tv-";
    private static final String BACKUP_SUFFIX = ".bk.gz";

    private BackupFiles() {
    }

    public static boolean isBackup(File file) {
        return file != null && isBackupName(file.getName());
    }

    public static boolean isBackupName(String name) {
        return name != null && name.endsWith(BACKUP_SUFFIX) && (name.startsWith(BACKUP_PREFIX) || name.startsWith(LEGACY_BACKUP_PREFIX));
    }

    public static boolean isCurrentDeviceBackup(File file, String ownerId) {
        return file != null && isCurrentDeviceBackupName(file.getName(), ownerId);
    }

    public static boolean isCurrentDeviceBackupName(String name, String ownerId) {
        return name != null && ownerId != null && !ownerId.isEmpty() && name.endsWith(BACKUP_SUFFIX) && name.startsWith(getCurrentDevicePrefix(ownerId));
    }

    public static String getCurrentDevicePrefix(String ownerId) {
        return BACKUP_PREFIX + ownerId + "-";
    }
}
