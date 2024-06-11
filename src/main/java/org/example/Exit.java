package org.example;

public class Exit extends RuntimeException {
    /**
     * Exception to allow a clean exit from any point in execution
     */
    public static final int CLEAN = 0;
    public static final int ERROR = 1;
    public static final int MISSING_PROFILEINI = 2;
    public static final int MISSING_SECRETS = 3;
    public static final int BAD_PROFILEINI = 4;
    public static final int LOCATION_NO_DIRECTORY = 5;
    public static final int BAD_SECRETS = 6;
    public static final int BAD_LOCALE = 7;

    public static final int FAIL_LOCATE_NSS = 10;
    public static final int FAIL_LOAD_NSS = 11;
    public static final int FAIL_INIT_NSS = 12;
    public static final int FAIL_NSS_KEYSLOT = 13;
    public static final int FAIL_SHUTDOWN_NSS = 14;
    public static final int BAD_PRIMARY_PASSWORD = 15;
    public static final int NEED_PRIMARY_PASSWORD = 16;
    public static final int DECRYPTION_FAILED = 17;

    public static final int PASSSTORE_NOT_INIT = 20;
    public static final int PASSSTORE_MISSING = 21;
    public static final int PASSSTORE_ERROR = 22;

    public static final int READ_GOT_EOF = 30;
    public static final int MISSING_CHOICE = 31;
    public static final int NO_SUCH_PROFILE = 32;

    public static final int UNKNOWN_ERROR = 100;
    public static final int KEYBOARD_INTERRUPT = 102;

    public int exitcode;

    public Exit (int exitcode) {
        this.exitcode = exitcode;
    }

    @Override
    public String toString ( ) {
        return STR."Premature program exit with exit code \{this.exitcode}";
    }
}

