package gov.lbl.als.bl831;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Converts an Axis camera URI to an HTTP URI suitable for FFmpeg.
 * <p>
 * The "axis" scheme is replaced with "http" while preserving all other
 * URI components (authority, path, query, fragment).
 * <p>
 * Port defaults to 80 if not specified.
 */
public class AxisUriParser {

    /**
     * Converts an Axis URI to its HTTP equivalent.
     *
     * @param axisUri
     *        the Axis URI string, e.g. "axis://host/axis-cgi/mjpg/video.cgi?camera=1"
     * @return the equivalent HTTP URI.
     * @throws IllegalArgumentException
     *         if the URI is null, empty, or not a valid Axis URI.
     */
    public static URI toHttpUri(String axisUri) throws IllegalArgumentException {
        if (axisUri == null || !axisUri.startsWith("axis://")) {
            throw new IllegalArgumentException("URI must start with 'axis://'");
        }

        try {
            URI original = new URI(axisUri);
            return new URI("http",
                    original.getRawAuthority(),
                    original.getRawPath(),
                    original.getRawQuery(),
                    original.getRawFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI format: " + e.getMessage());
        }
    }
}
