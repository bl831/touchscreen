package gov.lbl.als.bl831.video;

import java.awt.Image;
import java.awt.event.ActionListener;

import gov.lbl.als.bl831.VideoSource;
import willibert.NetCamLib.Camera;
import willibert.NetCamLib.CameraException;

/**
 * A video source that uses the Axis video server as its source.
 */
public class AxisVideoSource implements VideoSource {

    private final Camera mCamera;

    public AxisVideoSource(String hostname, int port, int camera,
                            String resolution, int fps,
                            String username, String password) {
        mCamera = new Camera();

        try {
            mCamera.setChannel(camera);
        } catch (CameraException e) {
            e.printStackTrace();
        }
        mCamera.setIPAddress(hostname, port);

        if (resolution != null && !resolution.isEmpty()) {
            mCamera.setResolution(resolution);
        }
        if (fps > 0) {
            mCamera.setFramerate(fps);
        }

        if (username != null && !username.isEmpty()) {
            mCamera.enableAuthentication(true);
            mCamera.setAuthentication(username, password);
        }
    }

    @Override
    public Image getImage() {
        return mCamera.getImage();
    }

    @Override
    public void addActionListener(ActionListener listener) {
        mCamera.addActionListener(listener);
    }

    @Override
    public void start() {
        try {
            mCamera.start();
        } catch (CameraException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Noop
    }
}
