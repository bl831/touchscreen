package gov.lbl.als.bl831.video;

import java.awt.Image;
import java.awt.event.ActionListener;

import gov.lbl.als.bl831.CommandLineArgs;
import gov.lbl.als.bl831.VideoSource;
import willibert.NetCamLib.Camera;
import willibert.NetCamLib.CameraException;

/**
 * A video source that uses the Axis video server as its source.
 */
public class AxisVideoSource implements VideoSource {

    private final Camera mCamera;

    public AxisVideoSource(CommandLineArgs cla) {
        mCamera = new Camera();

        try {
            mCamera.setChannel(cla.getCamera());
        } catch (CameraException e) {
            e.printStackTrace();
        }
        mCamera.setIPAddress(cla.getHostname(), cla.getPort());

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
