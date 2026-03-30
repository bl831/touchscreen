package gov.lbl.als.bl831.video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Offline video source that loops a bundled WebM sample video.
 * Extracts the classpath resource to a temp file and plays it
 * via {@link FFmpegVideoSource} with looping enabled.
 */
public class SampleVideoSource extends FFmpegVideoSource {

    private static final String RESOURCE_PATH = "/sample-video.webm";

    public SampleVideoSource() {
        super(extractVideo(), true);
    }

    /**
     * Extracts the bundled video resource to a temporary file.
     * FFmpegFrameGrabber requires a file path, so the classpath resource
     * must be written to disk first.
     */
    private static File extractVideo() {
        InputStream res = SampleVideoSource.class
                .getResourceAsStream(RESOURCE_PATH);
        if (res == null) {
            throw new RuntimeException(
                    "Bundled video resource not found: " + RESOURCE_PATH);
        }
        try {
            File tmp = File.createTempFile("sample-video-", ".webm");
            tmp.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(tmp)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = res.read(buf)) != -1) {
                    out.write(buf, 0, n);
                }
            }
            res.close();
            return tmp;
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to extract video resource", e);
        }
    }
}
