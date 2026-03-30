package gov.lbl.als.bl831;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
    private final String  mWindow;
    private final Object  mInterpolationHint;
    private final Map<VirtualButton, ButtonConfig> mButtonOverrides;
    private final Color   mBackground;
    private final Color   mBorderColor;
    private final boolean mBorderlessButtons;

    public Config(String videoUri, String touchHostname, int touchPort,
                  boolean simulate, boolean emulate, String window,
                  Object interpolationHint,
                  Map<VirtualButton, ButtonConfig> buttonOverrides,
                  Color background, Color borderColor,
                  boolean borderlessButtons) {
        mVideoUri = videoUri;
        mTouchHostname = touchHostname;
        mTouchPort = touchPort;
        mSimulate = simulate;
        mEmulate = emulate;
        mWindow = window;
        mInterpolationHint = interpolationHint;
        mButtonOverrides = buttonOverrides;
        mBackground = background;
        mBorderColor = borderColor;
        mBorderlessButtons = borderlessButtons;
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
        Map<VirtualButton, ButtonConfig> buttonOverrides = Collections.emptyMap();
        Color background = null;
        Color borderColor = null;
        boolean borderlessButtons = false;

        String configFile = cla.getConfigFile();
        if (!configFile.isEmpty()) {
            Properties props = loadConfigFile(configFile);
            if (props != null) {
                cla.applyConfigFile(props, parseResult);
                File configDir = new File(configFile).getAbsoluteFile()
                        .getParentFile();
                buttonOverrides = parseButtonOverrides(props, configDir);
                background = parseHexColor(
                        props.getProperty("ui.background"));
                borderColor = parseHexColor(
                        props.getProperty("ui.border-color"));
                borderlessButtons = Boolean.parseBoolean(
                        props.getProperty("ui.borderless-buttons"));
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
                cla.getSimulate(), cla.getEmulate(), cla.getWindow(),
                cla.getInterpolation(), buttonOverrides,
                background, borderColor, borderlessButtons);
    }

    /**
     * Parses button override properties from the config file. Looks for
     * keys matching button.&lt;Name&gt;.icon, button.&lt;Name&gt;.command,
     * and button.&lt;Name&gt;.hidden.
     *
     * @param props
     *        the config file properties.
     * @param configDir
     *        the directory containing the config file, used to resolve
     *        relative icon paths.
     * @return a map of button overrides.
     */
    private static Map<VirtualButton, ButtonConfig> parseButtonOverrides(
            Properties props, File configDir) {
        Map<VirtualButton, ButtonConfig> overrides = new HashMap<>();

        for (VirtualButton vb : VirtualButton.values()) {
            String prefix = "button." + vb.name() + ".";
            String iconValue = props.getProperty(prefix + "icon");
            String pressedValue = props.getProperty(prefix + "pressed");
            String command = props.getProperty(prefix + "command");
            String hiddenValue = props.getProperty(prefix + "hidden");

            if (iconValue == null && pressedValue == null
                    && command == null && hiddenValue == null) {
                continue;
            }

            String iconPath = resolveIconPath(iconValue, configDir);
            String pressedPath = resolveIconPath(pressedValue, configDir);

            boolean hidden = Boolean.parseBoolean(hiddenValue);

            overrides.put(vb, new ButtonConfig(iconPath, pressedPath,
                    command, hidden));
        }

        return overrides;
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
     * Resolves an icon path relative to the config file directory.
     *
     * @param value
     *        the icon path from the config file, or null.
     * @param configDir
     *        the directory containing the config file.
     * @return the resolved absolute path, or null if value is null.
     */
    private static String resolveIconPath(String value, File configDir) {
        if (value == null) {
            return null;
        }
        File iconFile = new File(value);
        if (!iconFile.isAbsolute()) {
            iconFile = new File(configDir, value);
        }
        return iconFile.getAbsolutePath();
    }

    /**
     * Parses a hex color string (e.g. "#00e5ff" or "00e5ff").
     *
     * @param hex
     *        the hex color string, or null.
     * @return the parsed Color, or null if hex is null or invalid.
     */
    private static Color parseHexColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return null;
        }
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        try {
            return new Color(Integer.parseInt(hex, 16));
        } catch (NumberFormatException e) {
            System.err.printf("Invalid color value: '%s'%n", hex);
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

    /**
     * @return the window size string (e.g. "1920x1080" or "full").
     */
    public String getWindow() {
        return mWindow;
    }

    /**
     * @return <code>true</code> if the window should be full screen.
     */
    public boolean isFullScreen() {
        return "full".equalsIgnoreCase(mWindow);
    }

    /**
     * @return the window width, or 0 if full screen.
     */
    public int getWindowWidth() {
        if (isFullScreen()) {
            return 0;
        }
        String[] parts = mWindow.toLowerCase().split("x");
        return Integer.parseInt(parts[0]);
    }

    /**
     * @return the window height, or 0 if full screen.
     */
    public int getWindowHeight() {
        if (isFullScreen()) {
            return 0;
        }
        String[] parts = mWindow.toLowerCase().split("x");
        return Integer.parseInt(parts[1]);
    }

    public Object getInterpolationHint() {
        return mInterpolationHint;
    }

    /**
     * @return button overrides from the config file, keyed by VirtualButton.
     */
    public Map<VirtualButton, ButtonConfig> getButtonOverrides() {
        return mButtonOverrides;
    }

    /**
     * @return the background color, or null if not configured.
     */
    public Color getBackground() {
        return mBackground;
    }

    /**
     * @return the border color for the frame and video, or null if not
     *         configured.
     */
    public Color getBorderColor() {
        return mBorderColor;
    }

    /**
     * @return <code>true</code> if buttons should have no Swing chrome.
     */
    public boolean isBorderlessButtons() {
        return mBorderlessButtons;
    }
}
