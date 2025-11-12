package gov.lbl.als.bl831;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class V4L2UriParser {
    
    // Regex pattern for v4l2 URI with specific format requirements
    private static final String URI_PATTERN = 
        "^v4l2://([^/]+)/([A-Z0-9]{4})/([0-9]+x[0-9]+)/([0-9]+(\\.[0-9]+)?fps)$";
    
    private static final Pattern pattern = Pattern.compile(URI_PATTERN);
    
    public static class V4L2UriComponents {
        public final String device;
        public final String pixelFormat;
        public final int width;
        public final int height;
        public final double fps;
        
        public V4L2UriComponents(String device, String pixelFormat, int width, int height, double fps) {
            this.device = device;
            this.pixelFormat = pixelFormat;
            this.width = width;
            this.height = height;
            this.fps = fps;
        }

        /**
         * Returns the URI string representation of this V4L2UriComponents object
         * @return URI string in the format v4l2://<device>/<format>/<resolution>/<fps>
         */
        public String toUriString() {
            // Format FPS to ensure it has the proper "fps" suffix
            String fpsString = String.format("%.1ffps", fps);
            // Handle special case where fps is a whole number
            return String.format("v4l2://%s/%s/%dx%d/%s", device, pixelFormat, width, height, fpsString);
        }        

        @Override
        public String toString() {
            return String.format("Device: %s, Format: %s, Resolution: %dx%d, FPS: %s", 
                               device, pixelFormat, width, height, fps);
        }
    }
    
    public static V4L2UriComponents parseUri(String uri) throws IllegalArgumentException {
        if (uri == null || uri.trim().isEmpty()) {
            throw new IllegalArgumentException("URI cannot be null or empty");
        }
        
        Matcher matcher = pattern.matcher(uri.trim());
        
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                "Invalid URI format. Expected format: v4l2://<device>/<format>/<resolution>/<fps>\n" +
                "Where:\n" +
                "- Device: any valid device name\n" +
                "- Format: 4-character pixel format (e.g., MJPG, YUYV)\n" +
                "- Resolution: integer x integer (e.g., 1920x1080)\n" +
                "- FPS: integer or decimal number followed by 'fps' (e.g., 30fps, 30.0fps)");
        }
        
        String device = matcher.group(1);
        String pixelFormat = matcher.group(2);
        String resolution = matcher.group(3);
        String fpsString = matcher.group(4);
        
        // Parse width and height from resolution string
        String[] dimensions = resolution.split("x");
        int width = Integer.parseInt(dimensions[0]);
        int height = Integer.parseInt(dimensions[1]);
      
        // Convert FPS to Double
        double fps = Double.parseDouble(fpsString.replace("fps", ""));
        
        return new V4L2UriComponents(device, pixelFormat, width, height, fps);
    }
    
    public static void main(String[] args) {
        // Test cases
        String[] testUris = {
            "v4l2://video0/MJPG/1920x1080/30.0fps",
            "v4l2://video1/YUYV/1280x720/10.0fps",
            "v4l2://usb0/MJPG/640x480/15.0fps",
            "v4l2://video2/YUYV/160x120/30.0fps",
            "v4l2://video3/MJPG/1920x1080/30fps",
            "v4l2://video4/YUYV/1280x720/10fps"
        };
        
        for (String uri : testUris) {
            try {
                V4L2UriComponents components = parseUri(uri);
                System.out.println("Parsed URI: " + uri);
                System.out.println("Components: " + components);
                System.out.println();
            } catch (IllegalArgumentException e) {
                System.out.println("Error parsing URI '" + uri + "': " + e.getMessage());
                System.out.println();
            }
        }
        
        // Test invalid cases
        String[] invalidUris = {
            "v4l2://video0/MJPG/1920x1080/30.0",       // missing fps suffix
            "v4l2://video0/MJPG/1920x1080/30fps",      // missing decimal point
            "v4l2://video0/MJP/1920x1080/30.0fps",     // format too short
            "v4l2://video0/MJPG/1920x1080/30.0fps1",   // extra characters
            "v4l2://video0/MJPG/1920x/30.0fps",        // invalid resolution
            "v4l2://video0/MJPG/1920x1080",            // missing fps
        };
        
        System.out.println("Testing invalid URIs:");
        for (String uri : invalidUris) {
            try {
                parseUri(uri);
                System.out.println("Unexpectedly parsed: " + uri);
            } catch (IllegalArgumentException e) {
                System.out.println("Correctly rejected '" + uri + "': " + e.getMessage());
            }
        }
    }
}