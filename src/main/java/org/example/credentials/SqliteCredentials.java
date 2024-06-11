package org.example.credentials;

import org.example.exceptions.NotFoundError;

import java.io.File;
import java.sql.*;
import java.util.Iterator;

// Движок для загрузки данных из SQLite
public class SqliteCredentials extends Credentials {
    private Connection conn;
    private PreparedStatement stmt;

    public SqliteCredentials (String profile) throws NotFoundError {
        super(STR."\{profile}/signons.sqlite");

        String db = STR."\{profile}/sings.sqlite";

        // Проверки на существование целевого файла
        if (!new File(db).exists( ))
            throw new NotFoundError( );

        try {
            // Инициализация подключения и подготовка данных
            conn = DriverManager.getConnection(STR."jdbc:sqlite:\{db}");
            stmt = conn.prepareStatement(
                    "SELECT hostname, encryptedUsername, encryptedPassword, encType FROM moz_logins"
            );
        } catch (SQLException e) {
            e.printStackTrace( );
        }
    }

    // Загрузка данных
    @Override
    public Iterator<Tuple4<String, String, String, Integer>> iterator ( ) {
        System.out.println("Reading password database in SQLite format");
        return new Iterator<>( ) {
            private ResultSet rs;
            {
                try {
                    rs = stmt.executeQuery( );
                } catch (SQLException e) {
                    e.printStackTrace( );
                }
            }

            @Override
            public boolean hasNext ( ) {
                try {
                    return rs.next( );
                } catch (SQLException e) {
                    e.printStackTrace( );
                    return false;
                }
            }

            @Override
            public Tuple4<String, String, String, Integer> next ( ) {
                try {
                    String hostname = rs.getString("hostname");
                    String encryptedUsername = rs.getString("encryptedUsername");
                    String encryptedPassword = rs.getString("encryptedPassword");
                    int encType = rs.getInt("encType");
                    return new Tuple4<>(hostname, encryptedUsername, encryptedPassword, encType);
                } catch (SQLException e) {
                    e.printStackTrace( );
                    return null;
                }
            }
        };
    }

    // Закрытие базы
    @Override
    public void done ( ) {
        try {
            stmt.close( );
            conn.close( );
        } catch (SQLException e) {
            e.printStackTrace( );
        }
    }
}

