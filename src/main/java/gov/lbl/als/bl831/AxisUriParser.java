package gov.lbl.als.bl831;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Parses an Axis camera URI into its components.
 * <p>
 * Expected format: axis://[user:pass@]hostname[:port]/path[?query]
 * <p>
 * The scheme "axis" is mapped to "http" for the underlying connection.
 * Port defaults to 80 if not specified.
 */
public class AxisUriParser {

    public static class AxisUriComponents {
        private final String hostname;
        private final int port;
        private final int camera;
        private final String resolution;
        private final int fps;
        private final String username;
        private final String password;

        public AxisUriComponents(String hostname, int port, int camera,
                                  String resolution, int fps,
                                  String username, String password) {
            this.hostname = hostname;
            this.port = port;
            this.camera = camera;
            this.resolution = resolution;
            this.fps = fps;
            this.username = username;
            this.password = password;
        }

        public String getHostname() {
            return hostname;
        }

        public int getPort() {
            return port;
        }

        public int getCamera() {
            return camera;
        }

        public String getResolution() {
            return resolution;
        }

        public int getFps() {
            return fps;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        @Override
        public String toString() {
            return String.format("Host: %s, Port: %d, Camera: %d, Resolution: %s, FPS: %d",
                    hostname, port, camera,
                    resolution != null ? resolution : "N/A",
                    fps);
        }
    }

    /**
     * Parses an Axis URI string into its components.
     *
     * @param uriString
     *        the URI to parse, e.g. "axis://hostname:80/axis-cgi/mjpg/video.cgi?camera=1"
     * @return the parsed components
     * @throws IllegalArgumentException
     *         if the URI is null, empty, or not a valid Axis URI
     */
    public static AxisUriComponents parseUri(String uriString) throws IllegalArgumentException {
        if (uriString == null || uriString.trim().isEmpty()) {
            throw new IllegalArgumentException("URI cannot be null or empty");
        }

        if (!uriString.startsWith("axis://")) {
            throw new IllegalArgumentException("URI must start with 'axis://'");
        }

        //
        // Replace axis:// with http:// so java.net.URI can parse it.
        //
        String httpUri = "http://" + uriString.substring("axis://".length());

        URI uri;
        try {
            uri = new URI(httpUri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI format: " + e.getMessage());
        }

        String hostname = uri.getHost();
        if (hostname == null || hostname.isEmpty()) {
            throw new IllegalArgumentException("Hostname is required");
        }

        int port = uri.getPort();
        if (port == -1) {
            port = 80;
        }

        //
        // Parse username and password from user info.
        //
        String username = null;
        String password = null;
        String userInfo = uri.getUserInfo();
        if (userInfo != null && !userInfo.isEmpty()) {
            int colonIndex = userInfo.indexOf(':');
            if (colonIndex >= 0) {
                username = userInfo.substring(0, colonIndex);
                password = userInfo.substring(colonIndex + 1);
            } else {
                username = userInfo;
            }
        }

        //
        // Parse query parameters for camera, resolution, and fps.
        //
        int camera = 1;
        String resolution = null;
        int fps = -1;

        String query = uri.getQuery();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    if ("camera".equalsIgnoreCase(keyValue[0])) {
                        try {
                            camera = Integer.parseInt(keyValue[1]);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                    "Invalid camera number: " + keyValue[1]);
                        }
                    } else if ("resolution".equalsIgnoreCase(keyValue[0])) {
                        resolution = keyValue[1];
                    } else if ("fps".equalsIgnoreCase(keyValue[0])) {
                        try {
                            fps = Integer.parseInt(keyValue[1]);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                    "Invalid fps value: " + keyValue[1]);
                        }
                    }
                }
            }
        }

        return new AxisUriComponents(hostname, port, camera, resolution, fps,
                username, password);
    }
}
