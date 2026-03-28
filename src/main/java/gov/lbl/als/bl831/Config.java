package gov.lbl.als.bl831;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import picocli.CommandLine.ParseResult;

/**
 * Holds configuration information.
 */
public class Config {

    private final String  mVideoUri;
    private final String  mTouchHostname;
    private final int     mTouchPort;
    private final boolean mSimulate;
    private final boolean mEmulate;
    private final Object  mInterpolationHint;

    public Config(String videoUri, String touchHostname, int touchPort,
                  boolean simulate, boolean emulate, Object interpolationHint) {
        mVideoUri = videoUri;
        mTouchHostname = touchHostname;
        mTouchPort = touchPort;
        mSimulate = simulate;
        mEmulate = emulate;
        mInterpolationHint = interpolationHint;
    }

    /**
     * Builds a Config from command line arguments and an optional config file.
     * Config file values are applied first, then CLI args override.
     *
     * @param cla
     *        the parsed command line arguments.
     * @param parseResult
     *        the picocli parse result used to detect explicitly set options.
     * @return the configuration.
     */
    public static Config fromCommandLine(CommandLineArgs cla,
                                         ParseResult parseResult) {
        String configFile = cla.getConfigFile();
        if (!configFile.isEmpty()) {
            Properties props = loadConfigFile(configFile);
            if (props != null) {
                cla.applyConfigFile(props, parseResult);
            }
        }

        String touchHostname = null;
        int touchPort = 0;

        String touchUri = cla.getTouchUri();
        if (touchUri != null && !touchUri.isEmpty()) {
            URI uri = TouchUriParser.parse(touchUri);
            touchHostname = uri.getHost();
            touchPort = uri.getPort();
        }

        return new Config(cla.getVideoUri(), touchHostname, touchPort,
                cla.getSimulate(), cla.getEmulate(), cla.getInterpolation());
    }

    /**
     * Loads a properties file from the given path.
     *
     * @param path
     *        the path to the config file.
     * @return the loaded properties, or null if the file could not be read.
     */
    private static Properties loadConfigFile(String path) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
            return props;
        } catch (IOException e) {
            System.err.printf("Unable to load config file '%s': %s%n",
                    path, e.getMessage());
            return null;
        }
    }

    /**
     * @return video source URI
     */
    public String getVideoUri() {
        return mVideoUri;
    }

    /**
     * @return touch server hostname
     */
    public String getTouchHostname() {
        return mTouchHostname;
    }

    /**
     * @return touch server port
     */
    public int getTouchPort() {
        return mTouchPort;
    }

    /**
     * @return <code>true</code> if running in simulate mode.
     */
    public boolean isSimulate() {
        return mSimulate;
    }

    /**
     * @return <code>true</code> if old style touch coordinates should be sent.
     */
    public boolean isEmulate() {
        return mEmulate;
    }

    public Object getInterpolationHint() {
        return mInterpolationHint;
    }

}
