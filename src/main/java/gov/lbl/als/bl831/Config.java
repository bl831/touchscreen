package gov.lbl.als.bl831;

/**
 * Holds configuration information.
 */
public class Config {

    private final String  mDcssHostname;
    private final int     mDcssPort;
    private final String  mHostname;
    private final int     mPort;
    private final int     mCamera;
    private final boolean mVideoCapture;
    private final boolean mEmulate;
    private final Object  mInterpolationHint;
    private final float   mFramesPerSecondHint;

    public Config(String dcsshostname, String dcssport, String hostname, String port,
                  String camera, boolean videoCapture, boolean emulate,
                  Object interpolationHint, float framesPerSecondHint) {
        mDcssHostname = dcsshostname;
        mHostname = hostname;
        mVideoCapture = videoCapture;
        mEmulate = emulate;
        mDcssPort = Integer.parseInt(dcssport);
        mPort = Integer.parseInt(port);
        mCamera = Integer.parseInt(camera);
        mInterpolationHint = interpolationHint;
        mFramesPerSecondHint = framesPerSecondHint;
    }

    /**
     * @return dcss hostname
     */
    public String getDcssHostname() {
        return mDcssHostname;
    }

    /**
     * @return dcss port
     */
    public int getDcssPort() {
        return mDcssPort;
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return mHostname;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return mPort;
    }

    /**
     * @return the camera
     */
    public int getCamera() {
        return mCamera;
    }

    /**
     * @return the videoCapture
     */
    public boolean isVideoCapture() {
        return mVideoCapture;
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

    public float getFramesPerSecondHint() {
        return mFramesPerSecondHint;
    }

}
