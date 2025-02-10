package uk.gov.justice.services.event.sourcing.subscription.error;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.DigestUtils;

public class HashFromStringGenerator {

    private static final String SHA_512 = "SHA-512";

    public String createHashFrom(final String rawString) {
        final byte[] rawStringBytes = rawString.getBytes(UTF_8);

        try {
            final MessageDigest messageDigest = MessageDigest.getInstance(SHA_512);
            messageDigest.reset();
            messageDigest.update(rawStringBytes);
            final byte[] digest = messageDigest.digest();

            return DigestUtils.sha512_224Hex(digest);
        } catch (final NoSuchAlgorithmException e) {
            throw new ExceptionHashingException(SHA_512 + " algorithm not found", e);
        }
    }
}
