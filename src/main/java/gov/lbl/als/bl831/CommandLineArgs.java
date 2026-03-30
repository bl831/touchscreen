package gov.lbl.als.bl831;

import java.awt.RenderingHints;

import java.util.Properties;

import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
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

    @Option(names = {"-V", "--version"}, versionHelp = true, description = "display version info")
    boolean versionInfoRequested;

    @Option(names = {"--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @Option(names = {"-v", "--video"},
            description = "video source URI. (axis://host/axis-cgi/mjpg/video.cgi?camera=1 or v4l2://video0/MJPG/640x480/30fps)")
    private String videoUri = "";

    @Option(names = {"-t", "--touch"},
            description = "touch server URI that translates touch commands to DCSS. (e.g. touch://hostname:14000)")
    private String touchUri = "";

    @Option(names = {"-c", "--config"},
            description = "path to button configuration file.")
    private String configFile = "";

    @Option(names = {"-l", "--list-v4l2"}, description = "list available V4L2 video capture URIs.")
    private boolean listV4l2 = false;

    @Option(names = {"-s", "--simulate"}, description = "use simulated video source and do not connect to touch server.")
    private boolean simulate = false;

    @Option(names = {"-e", "--emulate"}, description = "emulate old-style touch coordinates for output.")
    private boolean emulate = false;

    @Option(names = {"-w", "--window"},
            description = "window size. (e.g. '1920x1080' or 'full'). Default: full.")
    private String window = "full";

    @Option(names = {"-i", "--interpolation"},
            description = "image interpolation. (can be 'nearest', 'bilinear', or 'bicubic').",
            converter = InterpolationConverter.class
    )
    private Object interpolation = null;

    public String getVideoUri() {
        return videoUri;
    }

    public String getTouchUri() {
        return touchUri;
    }

    public String getConfigFile() {
        return configFile;
    }

    public boolean getListV4l2() {
        return listV4l2;
    }

    public boolean getSimulate() {
        return simulate;
    }

    public boolean getEmulate() {
        return emulate;
    }

    public String getWindow() {
        return window;
    }

    public Object getInterpolation() {
        return interpolation;
    }

    /**
     * Applies defaults from a config file properties. Only sets values that
     * were not explicitly provided on the command line.
     *
     * @param props
     *        the properties loaded from the config file.
     * @param parseResult
     *        the picocli parse result used to check which options were
     *        explicitly matched.
     */
    void applyConfigFile(Properties props, ParseResult parseResult) {
        if (!parseResult.hasMatchedOption("--video")
                && props.containsKey("cli.video")) {
            videoUri = props.getProperty("cli.video");
        }
        if (!parseResult.hasMatchedOption("--touch")
                && props.containsKey("cli.touch")) {
            touchUri = props.getProperty("cli.touch");
        }
        if (!parseResult.hasMatchedOption("--emulate")
                && props.containsKey("cli.emulate")) {
            emulate = Boolean.parseBoolean(props.getProperty("cli.emulate"));
        }
        if (!parseResult.hasMatchedOption("--window")
                && props.containsKey("cli.window")) {
            window = props.getProperty("cli.window");
        }
        if (!parseResult.hasMatchedOption("--interpolation")
                && props.containsKey("cli.interpolation")) {
            try {
                interpolation = new InterpolationConverter()
                        .convert(props.getProperty("cli.interpolation"));
            } catch (Exception e) {
                System.err.printf("Invalid interpolation in config file: %s%n",
                        props.getProperty("cli.interpolation"));
            }
        }
    }
}
