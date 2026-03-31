package gov.lbl.als.bl831;

/**
 * Holds per-button configuration overrides loaded from a config file.
 */
public class ButtonConfig {

    private final String  mIconPath;
    private final String  mPressedIconPath;
    private final String  mCommand;
    private final boolean mHidden;

    public ButtonConfig(String iconPath, String pressedIconPath,
                        String command, boolean hidden) {
        mIconPath = iconPath;
        mPressedIconPath = pressedIconPath;
        mCommand = command;
        mHidden = hidden;
    }

    /**
     * @return path to the icon image, or null if not overridden.
     */
    public String getIconPath() {
        return mIconPath;
    }

    /**
     * @return path to the pressed-state icon, or null if not overridden.
     */
    public String getPressedIconPath() {
        return mPressedIconPath;
    }

    /**
     * @return custom command string, or null if not overridden.
     */
    public String getCommand() {
        return mCommand;
    }

    /**
     * @return <code>true</code> if this button should be hidden.
     */
    public boolean isHidden() {
        return mHidden;
    }
}
