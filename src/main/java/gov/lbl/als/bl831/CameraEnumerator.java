package gov.lbl.als.bl831;

import io.tetrah.camerainfo.v4l2.V4L2CameraCapabilities;
import io.tetrah.camerainfo.v4l2.V4L2CameraInfo;
import io.tetrah.camerainfo.v4l2.V4L2FrameInterval;
import io.tetrah.camerainfo.v4l2.V4L2FrameSize;
import io.tetrah.camerainfo.v4l2.V4L2PixelFormat;
import io.tetrah.camerainfo.v4l2.ioctl.V4L2Ioctl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Utility class to enumerate and list all available webcams on the local machine
 * using the Sarxos webcam-capture library.
 */
public class CameraEnumerator {

    public Collection<String> getVideoDevices() throws IOException {
        return new V4L2CameraInfo().getVideoDevices();
    }

    public Collection<String> getV4L2Uris(String device) throws IOException {

        Collection<String> uris = new ArrayList<>();
            
        // Get camera capabilities
        V4L2CameraInfo ci = new V4L2CameraInfo();
        V4L2CameraCapabilities capabilities = ci.getCapabilities(device);
        int caps = capabilities.getCapabilities();
        boolean isCapture = (caps & V4L2Ioctl.V4L2_CAP_VIDEO_CAPTURE) != 0;
        boolean isMPlane = (caps & V4L2Ioctl.V4L2_CAP_VIDEO_CAPTURE_MPLANE) != 0;
        if (!isCapture && !isMPlane) {
            return uris;
        }
        
        // Get pixel formats
        try {
            Collection<V4L2PixelFormat> pixelFormats = ci.getPixelFormats(device, V4L2Ioctl.V4L2_BUF_TYPE_VIDEO_CAPTURE);
            for (V4L2PixelFormat pixelFormat : pixelFormats) {

                Collection<V4L2FrameSize> frameSizes = ci.getFrameSizes(device, pixelFormat.getFourcc());
                for (V4L2FrameSize frameSize : frameSizes) {
                    int width = frameSize.getWidth();
                    int height = frameSize.getHeight();
                    
                    // Get FPS for this pixel format and frame size
                    Collection<V4L2FrameInterval> fivs = ci.getFrameIntervals(device, pixelFormat.getFourcc(), width, height);
                    
                    String shortDevice = removePathPrefix(device);
                    for (V4L2FrameInterval fiv : fivs) {
                        uris.add(new V4L2UriParser.V4L2UriComponents(shortDevice, pixelFormat.getFourcc(), width, height, fiv.getFps()).toUriString());
                    }
                }
            }
        } catch (IOException e) {
            //Do nothing
        }
        return uris;
    }

    private static String removePathPrefix(String path) {
        int lastSlashIndex = path.lastIndexOf('/');
        return path.substring(lastSlashIndex + 1);
    }
}