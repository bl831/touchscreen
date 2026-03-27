package gov.lbl.als.bl831;

/**
 * Holds configuration information.
 */
public class Config {

    private final String  mTouchHostname;
    private final int     mTouchPort;
    private final boolean mEmulate;
    private final Object  mInterpolationHint;

    public Config(String touchHostname, int touchPort, boolean emulate,
                  Object interpolationHint) {
        mTouchHostname = touchHostname;
        mTouchPort = touchPort;
        mEmulate = emulate;
        mInterpolationHint = interpolationHint;
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
     * @return <code>true</code> if old style touch coordinates should be sent.
     */
    public boolean isEmulate() {
        return mEmulate;
    }

    public Object getInterpolationHint() {
        return mInterpolationHint;
    }

}
