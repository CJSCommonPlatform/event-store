package uk.gov.justice.services.healthcheck.database;

import static java.util.Collections.unmodifiableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class DatabaseTableLister {

    private final String SQL = "SELECT tablename " +
            "FROM " +
            "pg_catalog.pg_tables " +
            "WHERE " +
            "   schemaname != 'pg_catalog' " +
            "AND schemaname != 'information_schema' " +
            "AND tablename != 'databasechangelog' " +
            "AND tablename != 'databasechangeloglock';";

    public List<String> listTables(final DataSource dataSource) throws SQLException {

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(SQL);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            final List<String> tableNames = new ArrayList<>();
            while (resultSet.next()) {
                final String tableName = resultSet.getString(1);
                tableNames.add(tableName);
            }

            return unmodifiableList(tableNames);
        }
    }
}
