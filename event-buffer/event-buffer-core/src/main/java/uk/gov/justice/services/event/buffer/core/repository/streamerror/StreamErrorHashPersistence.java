package uk.gov.justice.services.event.buffer.core.repository.streamerror;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.transaction.Transactional.TxType.REQUIRED;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

public class StreamErrorHashPersistence {

    private static final String FIND_BY_HASH_SQL = """
            SELECT
                exception_classname,
                cause_classname,
                java_classname,
                java_method,
                java_line_number
            FROM stream_error_hash
            WHERE hash = ?
            """;

    private static final String FIND_ALL_SQL = """
            SELECT
                hash,
                exception_classname,
                cause_classname,
                java_classname,
                java_method,
                java_line_number
            FROM stream_error_hash
            """;

    private static final String INSERT_HASH_SQL = """
            INSERT INTO stream_error_hash (
                hash,
                exception_classname,
                cause_classname,
                java_classname,
                java_method,
                java_line_number
            )
            VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING
            """;

    private static final String DELETE_ORPHANED_STREAM_ERROR_HASH_SQL = """
            DELETE FROM stream_error_hash
            WHERE NOT EXISTS (select 1
                              from stream_error
                              where stream_error.hash = stream_error_hash.hash);
    """;

    @Transactional(REQUIRED)
    public int upsert(final StreamErrorHash streamErrorHash, final Connection connection) throws SQLException {

        try(final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_HASH_SQL)) {
            preparedStatement.setString(1, streamErrorHash.hash());
            preparedStatement.setString(2, streamErrorHash.exceptionClassName());
            preparedStatement.setString(3, streamErrorHash.causeClassName().orElse(null));
            preparedStatement.setString(4, streamErrorHash.javaClassName());
            preparedStatement.setString(5, streamErrorHash.javaMethod());
            preparedStatement.setInt(6, streamErrorHash.javaLineNumber());

            return preparedStatement.executeUpdate();
        }
    }

    @Transactional(REQUIRED)
    public Optional<StreamErrorHash> findByHash(final String hash, final Connection connection) throws SQLException {

        try(final PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_HASH_SQL)) {
            preparedStatement.setString(1, hash);
            try(final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final String exceptionClassname = resultSet.getString("exception_classname");
                    final String causeClassname = resultSet.getString("cause_classname");
                    final String javaClassname = resultSet.getString("java_classname");
                    final String javaMethod = resultSet.getString("java_method");
                    final int javaLineNumber = resultSet.getInt("java_line_number");

                    final StreamErrorHash streamErrorHash = new StreamErrorHash(
                            hash,
                            exceptionClassname,
                            Optional.ofNullable(causeClassname),
                            javaClassname,
                            javaMethod,
                            javaLineNumber
                    );

                    return of(streamErrorHash);
                }
            }

            return empty();
        }
    }

    @Transactional(REQUIRED)
    public List<StreamErrorHash> findAll(final Connection connection) throws SQLException {

        try(final PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {

            final List<StreamErrorHash> streamErrorHashes = new ArrayList<>();
            try(final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final String hash = resultSet.getString("hash");
                    final String exceptionClassname = resultSet.getString("exception_classname");
                    final String causeClassname = resultSet.getString("cause_classname");
                    final String javaClassname = resultSet.getString("java_classname");
                    final String javaMethod = resultSet.getString("java_method");
                    final int javaLineNumber = resultSet.getInt("java_line_number");

                    final StreamErrorHash streamErrorHash = new StreamErrorHash(
                            hash,
                            exceptionClassname,
                            Optional.ofNullable(causeClassname),
                            javaClassname,
                            javaMethod,
                            javaLineNumber
                    );

                    streamErrorHashes.add(streamErrorHash);
                }
            }

            return streamErrorHashes;
        }
    }

    @Transactional(REQUIRED)
    public int deleteOrphanedHashes(final Connection connection) throws SQLException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_ORPHANED_STREAM_ERROR_HASH_SQL)){
            return preparedStatement.executeUpdate();
        }
    }
}
