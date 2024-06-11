package org.example.credentials;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Exit;
import org.example.exceptions.NotFoundError;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

// Движок для загрузки данных из JSON
public class JsonCredentials extends Credentials {
    private static final Logger LOGGER = Logger.getLogger("");

    public JsonCredentials(String profile) throws NotFoundError {
        File file = new File(profile, "logins.json");

        // Проверки на существование целевого файла
        if (!file.exists())
            throw new NotFoundError();

        super(file.getPath());
    }

    // Получение данных
    @Override
    public Iterator<Tuple4<String, String, String, Integer>> iterator() {
        try {
            LOGGER.info("Reading password database in JSON format");
            // Десерелизация
            ObjectMapper mapper = new ObjectMapper();
            Map data = mapper.readValue(new File(this.db), Map.class);

            // Проверка на правильный формат
            if (!data.containsKey("logins")) {
                LOGGER.exiting("Unrecognized format in {}", this.db);
                throw new RuntimeException(new Exit(Exit.BAD_SECRETS));
            }

            // Возвращение итератора со значениями
            @SuppressWarnings("unchecked")
            Iterable<Map<String, Object>> logins = (Iterable<Map<String, Object>>) data.get("logins");
            return new Iterator<>() {
                private final Iterator<Map<String, Object>> loginIterator = logins.iterator();

                @Override
                public boolean hasNext() {
                    return loginIterator.hasNext();
                }

                @Override
                public Tuple4<String, String, String, Integer> next() {
                    Map<String, Object> login = loginIterator.next();
                    try {
                        return new Tuple4<>(
                                (String) login.get("hostname"),
                                (String) login.get("encryptedUsername"),
                                (String) login.get("encryptedPassword"),
                                ((Number) login.get("encType")).intValue()
                        );
                    } catch (Exception e) {
                        LOGGER.info(STR."Skipped record \{login} due to missing fields");
                        return next();
                    }
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void done() {
        // Не требует закрытия
    }
}

