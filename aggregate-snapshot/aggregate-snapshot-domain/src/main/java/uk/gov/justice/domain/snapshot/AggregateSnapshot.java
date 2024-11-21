package uk.gov.justice.domain.snapshot;

import static java.lang.String.format;
import static org.apache.commons.lang3.SerializationUtils.serialize;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.core.aggregate.exception.AggregateChangeDetectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class AggregateSnapshot<T extends Aggregate> implements Serializable {

    private static final long serialVersionUID = -6621681121087097921L;

    private final UUID streamId;
    private final Long positionInStream;
    private final String type;
    private final byte[] aggregateByteRepresentation;
    private ZonedDateTime createdAt;

    @SuppressWarnings("unchecked")
    public AggregateSnapshot(final UUID streamId, final Long versionId, final T aggregate) {
        this(streamId, versionId, (Class<T>) aggregate.getClass(), serialize(aggregate));
    }

    public AggregateSnapshot(final UUID streamId, final Long versionId, final Class<T> type, final byte[] aggregateByteRepresentation) {
        this(streamId, versionId, type.getName(), aggregateByteRepresentation);
    }

    public AggregateSnapshot(final UUID streamId, final Long versionId, final String type, final byte[] aggregateByteRepresentation) {
        this.streamId = streamId;
        this.positionInStream = versionId;
        this.type = type;
        this.aggregateByteRepresentation = aggregateByteRepresentation;
    }

    /**
     * This one is used when constructing from ResultSet coming from the DB. This is the only place where we need to populate the createdAt from the resultSet
     * The other constructors do not need the createdAt param as they are used to store the snapshot in the DB in which case the date is generated right before the insert meaning it does not need to be set on the pojo
     * @param streamId
     * @param versionId
     * @param type
     * @param aggregateByteRepresentation
     * @param createdAt this is the date coming from the DB.
     */
    public AggregateSnapshot(final UUID streamId, final Long versionId, final String type, final byte[] aggregateByteRepresentation, final ZonedDateTime createdAt) {
        this(streamId, versionId, type, aggregateByteRepresentation);
        this.createdAt = createdAt;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public Long getPositionInStream() {
        return positionInStream;
    }

    public String getType() {
        return type;
    }

    public byte[] getAggregateByteRepresentation() {
        return aggregateByteRepresentation;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    @SuppressWarnings("unchecked")
    public T getAggregate(final ObjectInputStreamStrategy streamStrategy) throws AggregateChangeDetectedException {
        try (final ObjectInputStream objectInputStream = streamStrategy.objectInputStreamOf(new ByteArrayInputStream(aggregateByteRepresentation))) {
            return (T) Class.forName(getType()).cast(objectInputStream.readObject());
        } catch (SerializationException | ClassNotFoundException | IOException e) {
            throw new AggregateChangeDetectedException(format("Failed to deserialise Aggregate into %s. Cause: %s", type, e.getLocalizedMessage()), positionInStream, createdAt);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AggregateSnapshot that = (AggregateSnapshot) o;

        return new EqualsBuilder()
                .append(streamId, that.streamId)
                .append(positionInStream, that.positionInStream)
                .append(type, that.type)
                .append(aggregateByteRepresentation, that.aggregateByteRepresentation)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(streamId)
                .append(positionInStream)
                .append(type)
                .append(aggregateByteRepresentation)
                .toHashCode();
    }

}
