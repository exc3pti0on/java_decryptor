package org.example.browser;

import org.example.Exit;
import org.example.utils.NSSLibLoader;

import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.example.utils.FirefoxProfileManager.askPassword;

public class NSSProxy {
    private final Logger LOG;

    final boolean nonFatalDecryption;

    public NSSProxy (boolean nonFatalDecryption, Logger LOG) {
        NSSLibLoader.loadLib( );
        this.nonFatalDecryption = nonFatalDecryption;
        this.LOG = LOG;
    }

    public void initialize (String profile) {
        // The sql: prefix ensures compatibility with both
        // Berkley DB (cert8) and Sqlite (cert9) dbs
        String profilePath = STR."sql:\{profile}";
        System.out.println(STR."Initializing NSS with profile '\{profilePath}'");
        int errStatus = init(profilePath);
        System.out.println(STR."Initializing NSS returned \{errStatus}");

        if (errStatus != 0) {
            handleError(Exit.FAIL_INIT_NSS, "Couldn't initialize NSS, maybe '" + profile + "' is not a valid profile?");
        }
    }

    public void shutdown ( ) {
        int errStatus = shutdownNSS( );

        if (errStatus != 0) {
            handleError(Exit.FAIL_SHUTDOWN_NSS, "Couldn't shutdown current NSS profile");
        }
    }

    public void authenticate (String profile, boolean interactive) {
        System.out.println("Retrieving internal key slot");
        // Получаем ключ загрузки
        PK11SlotInfo keyslot = getInternalKeySlot( );

        System.out.println(STR."Internal key slot \{keyslot}");
        if (keyslot == null) {
            handleError(Exit.FAIL_NSS_KEYSLOT, "Failed to retrieve internal KeySlot");
        }

        try {
            if (needLogin(keyslot) != 0 /* Проверяем, нужна ли авторизация */) {
                // Запрашиваем пароль
                String password = askPassword(profile, interactive);

                System.out.println(STR."Authenticating with password '\{password}'");
                // Проверяем пароль
                int errStatus = checkUserPassword(keyslot, password);

                if (errStatus != 0 /* Проверяем, успешный ли пароль*/) {
                    throw new RuntimeException( );
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Функция для обработки ошибок нативной библиотеки
    public void handleError (int exitcode, Object... logerror) {
        if (logerror.length > 0) {
            LOG.warning(String.format(String.join(" ", Arrays.stream(logerror).map(Objects::toString).collect(Collectors.joining( )))));
        } else {
            LOG.info("Error during a call to NSS library, trying to obtain error info");
        }

        int code = getError( );
        String name = errorToName(code);
        name = name == null ? "NULL" : name;
        // Сериализуем ошибку
        String text = errorToString(code, 0);

        LOG.warning(STR."\{name}: \{text}");

        throw new Exit(exitcode);
    }

    public String decrypt (byte[] data64) {
        // Десерелизуем пароль
        byte[] data = new String(Base64.getUrlDecoder( ).decode(data64)).getBytes( );

        // Подготавливаем дешифровку
        SECItem inp = new SECItem(0, data, data.length);
        SECItem out = new SECItem(0, null, 0);

        // Декодируем пароль
        int errStatus = decrypt(inp, out, null);
        LOG.info(STR."Decryption of data returned status \{errStatus}");
        try {
            if (errStatus != 0 /* Проверяем на успешность */) { // -1 means password failed, other status are unknown
                String errorMsg = "Username/Password decryption failed. Credentials damaged or cert/key file mismatch.";
                if (nonFatalDecryption) {
                    throw new IllegalArgumentException(errorMsg);
                } else {
                    handleError(Exit.DECRYPTION_FAILED, errorMsg);
                }
            }
            // Если всё успешно, возвращаем пароль
            return out.decodeData( );
        } finally {
            // Освобождаем память
            zFreeItem(out, 0);
        }
    }

    public native int init (String arg);

    public native int shutdownNSS ( );

    public native void zFreeItem (SECItem arg1, int arg2);

    public native int getError ( );

    public native String errorToName (int arg);

    public native String errorToString (int arg1, long arg2);

    public native PK11SlotInfo getInternalKeySlot ( );

    public native void freeSlot (PK11SlotInfo arg);

    public native int needLogin (PK11SlotInfo arg);

    public native int checkUserPassword (PK11SlotInfo arg1, String arg2);

    public native int decrypt (SECItem arg1, SECItem arg2, Object arg3);
}

