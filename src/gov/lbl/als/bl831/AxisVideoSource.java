package gov.lbl.als.bl831;

import java.awt.Image;
import java.awt.event.ActionListener;

import willibert.NetCamLib.Camera;
import willibert.NetCamLib.CameraException;

/**
 * A video source that uses the Axis video server as its source.
 */
public class AxisVideoSource implements VideoSource {

    private final Camera mCamera;

    public AxisVideoSource(Config config) {
        mCamera = new Camera();

        try {
            mCamera.setChannel(config.getCamera());
        } catch (CameraException e) {
            e.printStackTrace();
        }
        mCamera.setIPAddress(config.getHostname(), config.getPort());

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

}
