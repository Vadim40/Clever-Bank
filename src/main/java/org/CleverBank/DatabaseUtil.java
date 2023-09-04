package org.CleverBank;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

/**
 * Класс `DatabaseUtil` предоставляет метод для создания и настройки источника данных (DataSource)
 * для подключения к базе данных PostgreSQL.
 */
public class DatabaseUtil {

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/bank";
    private static final String JDBC_USER = "postgres";
    private static final String JDBC_PASSWORD = "ghjuhn";

    /**
     * Получает и настраивает источник данных (DataSource) для подключения к базе данных PostgreSQL.
     *
     * @return Источник данных (DataSource) для PostgreSQL.
     */
    public static DataSource getDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(JDBC_URL);
        dataSource.setUser(JDBC_USER);
        dataSource.setPassword(JDBC_PASSWORD);
        return dataSource;
    }
}
