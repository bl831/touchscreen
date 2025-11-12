package gov.lbl.als.bl831;

import java.awt.RenderingHints;

import gov.lbl.als.bl831.V4L2UriParser.V4L2UriComponents;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import picocli.CommandLine.TypeConversionException;

@Command(
    resourceBundle = "app",
    name = "${bundle:app.name}",
    version = "${bundle:app.version}",
    description = "${bundle:app.description}")
public class CommandLineArgs {

    static class InterpolationConverter implements ITypeConverter<Object> {

        public Object convert(String interpolation) throws Exception {
            Object interpolationHint = null;
            if ("nearest".equalsIgnoreCase(interpolation)) {
                interpolationHint = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
            } else if ("bilinear".equalsIgnoreCase(interpolation)) {
                interpolationHint = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
            } else if ("bicubic".equalsIgnoreCase(interpolation)) {
                interpolationHint = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
            } else {
                throw new TypeConversionException( "'" + interpolation + "'. Allowed values are: 'nearest', 'bilinear', 'bicubic'." );
            }
            return interpolationHint;
        }
    }

    static class VideoCaptureUriConverter implements ITypeConverter<String> {

        @Override
        public String convert(String value) throws Exception {
            try {
                V4L2UriComponents components = V4L2UriParser.parseUri(value);
                return components.toUriString();
            } catch (IllegalArgumentException ex) {
                throw new TypeConversionException ("'" + value + "'. " + ex.getMessage() + "\nUse the --video-capture-list-uris option to see choices.");
            }            
        }
    }

    @Option(names = {"-V", "--version"}, versionHelp = true, description = "display version info")
    boolean versionInfoRequested;

    @Option(names = {"--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;       
    
    @Option(names = {"-h", "--hostname"}, description = "the Axis server name. (defaults to axis)")
    private String hostname = "axis";

    @Option(names = {"-p", "--port"}, description = "the Axis server port. (defaults to 80)")
    private int port = 80;

    @Option(names = {"-c", "--camera"}, description = "the camera stream number to display. (defaults to 4)")
    private int camera = 4;

    @Option(names = {"-d", "--dcss-hostname"}, description = "the DCSS touch server name. (defaults to dcss)")
    private String dcsshostname = "dcss";

    @Option(names = {"-r", "--dcss-port"}, description = "the DCSS touch server port. (defaults to 14000)")
    private int dcssport = 14000;

    @Option(names = {"-v", "--video-capture"},
            description = "the uri that specifies the video properties to capture.",
            converter = VideoCaptureUriConverter.class
    )
    private String videoCaptureUri = "none";

    @Option(names = {"-l", "--video-capture-list-uris"}, description = "causes the internal video capture system to be used.")
    private boolean videoCaptureListUris = false;

    @Option(names = {"-e", "--emulate"}, description = "emulate old-style touch coordinates for output.")
    private boolean emulate = false;

    @Option(names = {"-f", "--frames-per-second"}, description = "capture device frame rate. (can be 15.0, 24.0, or 30.0).")
    private float framesPerSecond = 24.0f;

    @Option(names = {"-i", "--interpolation"},
            description = "image interpolation. (can be 'nearest', 'bilinear', or 'bicubic').",
            converter = InterpolationConverter.class
    )
    private Object interpolation = null;

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public int getCamera() {
        return camera;
    }

    public String getDcsshostname() {
        return dcsshostname;
    }

    public int getDcssport() {
        return dcssport;
    }

    public String getVideoCaptureUri() {
        return videoCaptureUri;
    }

    public boolean getVideoCaptureListUris() {
        return videoCaptureListUris;
    }

    public boolean getEmulate() {
        return emulate;
    }

    public float getFramesPerSecond() {
        return framesPerSecond;
    }

    public Object getInterpolation() {
        return interpolation;
    }
}