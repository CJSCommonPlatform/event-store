package uk.gov.justice.services.eventstore.management.validation.process;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.error;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.success;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class EventLinkageChecker {

    public List<VerificationResult> verifyEventNumbersAreLinkedCorrectly(
            final String tableName,
            final DataSource dataSource) {

        final String query = format("SELECT event_number, previous_event_number FROM %s ORDER BY event_number", tableName);

        final List<VerificationResult> errors = new ArrayList<>();

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(query);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            int lastEventNumber = 0;
            int count = 0;
            while(resultSet.next()) {
                final int eventNumber = resultSet.getInt("event_number");
                final int previousEventNumber = resultSet.getInt("previous_event_number");

                if (previousEventNumber != lastEventNumber) {

                    final String message =
                            "Events incorrectly linked in %s table. " +
                                    "Event with event number %d " +
                                    "is linked to previous event number %d " +
                                    "whereas it should be %d";

                    errors.add(error(format(message, tableName, eventNumber, previousEventNumber, lastEventNumber)));
                }

                lastEventNumber = eventNumber;
                count++;
            }
            
            if (errors.isEmpty()) {
                
                final String message = format(
                        "All %d events in the %s table are correctly linked",
                        count,
                        tableName);

                return singletonList(success(message));
            }

            return errors;

        } catch (final SQLException e) {
            throw new CatchupVerificationException(format("Failed to get event numbers from %s table", tableName), e);
        }
    }
}
