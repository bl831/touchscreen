package gov.lbl.als.bl831;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Parses a touch server URI, validating that a port is present.
 * <p>
 * The "touch" scheme is parsed using the standard {@link URI} class
 * while preserving all URI components.
 * <p>
 * Port is required.
 */
public class TouchUriParser {

    /**
     * Parses and validates a touch URI.
     *
     * @param touchUri
     *        the touch URI string, e.g. "touch://hostname:14000"
     * @return the parsed URI.
     * @throws IllegalArgumentException
     *         if the URI is null, empty, missing a port, or not a valid
     *         touch URI.
     */
    public static URI parse(String touchUri) throws IllegalArgumentException {
        if (touchUri == null || !touchUri.startsWith("touch://")) {
            throw new IllegalArgumentException("URI must start with 'touch://'");
        }

        try {
            URI uri = new URI(touchUri);
            if (uri.getHost() == null || uri.getHost().isEmpty()) {
                throw new IllegalArgumentException(
                        "Missing hostname in touch URI: " + touchUri);
            }
            if (uri.getPort() == -1) {
                throw new IllegalArgumentException(
                        "Missing port in touch URI: " + touchUri);
            }
            return uri;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    "Invalid URI format: " + e.getMessage());
        }
    }
}
