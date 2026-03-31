package gov.lbl.als.bl831;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class V4L2UriParser {
    
    // Regex pattern for v4l2 URI with optional pixel format
    private static final String URI_PATTERN = 
        "^v4l2://([^/]+)(/([A-Z0-9]{4}))?/([0-9]+x[0-9]+)/([0-9]+(\\.[0-9]+)?fps)$";
    
    private static final Pattern pattern = Pattern.compile(URI_PATTERN);
    
    public static class V4L2UriComponents {
        private final String device;
        private final String pixelFormat;
        private final int width;
        private final int height;
        private final double fps;
        
        public V4L2UriComponents(String device, String pixelFormat, int width, int height, double fps) {
            this.device = device;
            this.pixelFormat = pixelFormat;
            this.width = width;
            this.height = height;
            this.fps = fps;
        }

        // Getters for private members
        public String getDevice() {
            return device;
        }
        
        public String getPixelFormat() {
            return pixelFormat;
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public double getFps() {
            return fps;
        }
            
        /**
         * Returns the URI string representation of this V4L2UriComponents object
         * @return URI string in the format v4l2://<device>/<format>/<resolution>/<fps>
         *         or v4l2://<device>/<resolution>/<fps> if format is not specified
         */
        public String toUriString() {
            // Format FPS to ensure it has the proper "fps" suffix
            String fpsString = String.format("%.1ffps", fps);
            // Handle special case where fps is a whole number
            if (pixelFormat != null && !pixelFormat.isEmpty()) {
                return String.format("v4l2://%s/%s/%dx%d/%s", device, pixelFormat, width, height, fpsString);
            } else {
                return String.format("v4l2://%s/%dx%d/%s", device, width, height, fpsString);
            }
        }        

        @Override
        public String toString() {
            if (pixelFormat != null && !pixelFormat.isEmpty()) {
                return String.format("Device: %s, Format: %s, Resolution: %dx%d, FPS: %s", 
                                   device, pixelFormat, width, height, fps);
            } else {
                return String.format("Device: %s, Format: N/A, Resolution: %dx%d, FPS: %s", 
                                   device, width, height, fps);
            }
        }
    }
    
    public static V4L2UriComponents parseUri(String uri) throws IllegalArgumentException {
        if (uri == null || uri.trim().isEmpty()) {
            throw new IllegalArgumentException("URI cannot be null or empty");
        }
        
        Matcher matcher = pattern.matcher(uri.trim());
        
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                "Invalid URI format. Expected format: v4l2://<device>[/format]/<resolution>/<fps>\n" +
                "Where:\n" +
                "- Device: any valid device name (e.g., video0, video1)\n" +
                "- Format (optional): 4-character pixel format (e.g., MJPG, YUYV)\n" +
                "- Resolution: integer x integer (e.g., 1920x1080)\n" +
                "- FPS: integer or decimal number followed by 'fps' (e.g., 30fps, 30.0fps)");
        }
        
        String device = matcher.group(1);
        String pixelFormat = matcher.group(3); // This will be null if format is not provided
        String resolution = matcher.group(4);
        String fpsString = matcher.group(5);
        
        // Parse width and height from resolution string
        String[] dimensions = resolution.split("x");
        int width = Integer.parseInt(dimensions[0]);
        int height = Integer.parseInt(dimensions[1]);
      
        // Convert FPS to Double
        double fps = Double.parseDouble(fpsString.replace("fps", ""));
        
        return new V4L2UriComponents(device, pixelFormat, width, height, fps);
    }
    
    public static void main(String[] args) {
        // Test cases - including cases with and without pixel format
        String[] testUris = {
            "v4l2://video0/MJPG/1920x1080/30.0fps",
            "v4l2://video1/YUYV/1280x720/10.0fps",
            "v4l2://usb0/MJPG/640x480/15.0fps",
            "v4l2://video2/YUYV/160x120/30.0fps",
            "v4l2://video3/MJPG/1920x1080/30fps",
            "v4l2://video4/YUYV/1280x720/10fps",
            // Test cases without pixel format
            "v4l2://video5/1920x1080/30.0fps",
            "v4l2://video6/1280x720/10.0fps"
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