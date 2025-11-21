package gov.lbl.als.bl831.video;

import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.swing.SwingUtilities;

import org.bytedeco.ffmpeg.avutil.AVFrame;
import org.bytedeco.ffmpeg.swscale.SwsContext;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.FrameGrabber.ImageMode;

import gov.lbl.als.bl831.VideoSource;
import io.tetrah.camerainfo.v4l2.V4L2CameraInfo;
import io.tetrah.camerainfo.v4l2.ioctl.V4L2Ioctl;

import static org.bytedeco.ffmpeg.global.avutil.*;
import static org.bytedeco.ffmpeg.global.swscale.*;

/**
 * Video source implementation using ByteDeco (JavaCV) for capturing video frames
 * from a device using FFmpeg.
 */
public class FFmpegVideoSource implements VideoSource {

    private final Java2DFrameConverter converter = new Java2DFrameConverter();

    private  ActionListener listener = null;

    private VideoCaptureThread              mCaptureThread;
    
    /**
     * Thread class responsible for capturing video frames from the device.
     */
    private class VideoCaptureThread extends Thread {
        private volatile boolean running = false;
        private SwsContext context = null;
        private String device;
        private String fourccPixelFormat;
        private int width;
        private int height;
        private int fps;

        private Image image;

        private ByteBuffer dstBufferCache = null;

        /**
         * Constructor for VideoCaptureThread.
         * 
         * @param device The video device to capture from
         * @param fourccPixelFormat The FOURCC pixel format string
         * @param width The width of the video frames
         * @param height The height of the video frames
         * @param fps The frames per second rate
         */
        public VideoCaptureThread(String device, String fourccPixelFormat, int width, int height, int fps) {
            this.device = device;
            this.fourccPixelFormat = fourccPixelFormat;
            this.width = width;
            this.height = height;
            this.fps = fps;
        }   

        /**
         * Main execution method for the thread.
         * Starts the video capture process.
         */
        @Override
        public void run() {
            running = true;
            try {
                startCapture(this.device, this.fourccPixelFormat, this.width, this.height, this.fps);
            } catch (IOException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Stops the video capture thread.
         */
        public void stopRunning() {
            running = false;
        }

        /**
         * Gets the current image from the video capture thread.
         * 
         * @return The current BufferedImage, or null if no image is available
         */
        public Image getImage() {
            return image;
        }

        /**
         * Starts the video capture process with the specified parameters.
         * 
         * @param device The video device to capture from
         * @param fourccPixelFormat The FOURCC pixel format string
         * @param width The width of the video frames
         * @param height The height of the video frames
         * @param fps The frames per second rate
         * @throws IOException If an I/O error occurs during capture
         */
        private void startCapture(String device, String fourccPixelFormat, int width, int height, int fps) throws IOException {
            if (fourccPixelFormat == null) {
                fourccPixelFormat = askDriverForPixelFormat(device);
                System.out.printf("No pixel format specified, querying device '%s' returned pixel format '%s'%n", device, fourccPixelFormat);
            }
            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(device)) {
                
                // Special handling for MJPEG streams. ffmpeg has depricated processing of older MJPEG
                // streams, so we need to use a workaround. We set the image mode to RAW and then
                // tell the driver to capture in native MJPEG format without conversion.
                String avPixFormat = V4L2AVUtils.v4l2PixFmt2AvPixFmtString(fourccPixelFormat);
                if (avPixFormat == null) {
                    avPixFormat = V4L2AVUtils.v4l2PixFmt2AvCodecString(fourccPixelFormat);
                    grabber.setImageMode(ImageMode.RAW);
                }

                grabber.setOption("pixel_format", avPixFormat);
                grabber.setImageWidth(width);
                grabber.setImageHeight(height);
                grabber.setFrameRate(fps);
                grabber.setOption("fflags", "nobuffer");
                grabber.setOption("probesize", "32");
                grabber.start();
                while (!Thread.currentThread().isInterrupted() && running) {
                    Frame f = grabber.grabImage();
                    if (f == null)
                        break;
                        
                    // Special handling for MJPEG frames.
                    f = swsScaleMjpegToBgr24Frame(f);
                    BufferedImage img = converter.convert(f);
                    f.close();
                    if (img != null) {
                        this.image = img;
                        if (listener != null) {
                            SwingUtilities.invokeLater(() -> listener.actionPerformed(null));
                        }
                    }
                }
            }
        }

        private String askDriverForPixelFormat(String deivce) {
            String fourcc = null;
            try {
                fourcc = (new V4L2CameraInfo().getPixelFormats(device, V4L2Ioctl.V4L2_BUF_TYPE_VIDEO_CAPTURE))
                   .stream().findFirst().map(f -> f.getFourcc()).orElse(null);
            } catch (IOException e) {
            }
            return fourcc;
        }

        /**
         * Scales MJPEG frame to BGR24 format.
         * 
         * @param frame The input frame to convert
         * @return The converted frame in BGR24 format
         */
        private Frame swsScaleMjpegToBgr24Frame(Frame frame) {
            if (frame.opaque instanceof AVFrame) {
                AVFrame avf = (AVFrame) frame.opaque;
                int srcFmt = avf.format();
                // Normalize deprecated YUVJ* to corresponding YUV* while preserving full-range
                // via colorspace details
                switch (srcFmt) {
                    case AV_PIX_FMT_YUVJ420P:
                        srcFmt = AV_PIX_FMT_YUV420P;
                        break;
                    case AV_PIX_FMT_YUVJ422P:
                        srcFmt = AV_PIX_FMT_YUV422P;
                        break;
                    case AV_PIX_FMT_YUVJ444P:
                        srcFmt = AV_PIX_FMT_YUV444P;
                        break;
                    case AV_PIX_FMT_YUVJ440P:
                        srcFmt = AV_PIX_FMT_YUV440P;
                        break;
                    default:
                        break;
                }

                // Do the conversion if the format has changed, otherwise return the original
                // frame.
                if (avf.format() != srcFmt) {
                    Frame convertedFrame = swsSaleMjpegToBgr24Frame(avf, srcFmt);
                    frame.close();
                    frame = convertedFrame;
                }
            }
            return frame;
        }

        /**
         * Performs the actual scaling from MJPEG to BGR24 format.
         * 
         * @param src The source AVFrame
         * @param srcFmt The source pixel format
         * @return The scaled frame in BGR24 format
         */
        private Frame swsSaleMjpegToBgr24Frame(AVFrame src, int srcFmt) {
            int w = src.width();
            int h = src.height();

            int dstFmt = AV_PIX_FMT_BGR24;

            // 1) Get or create scaler context (check for null)
            context = sws_getCachedContext(
                    context,
                    w, h, srcFmt,
                    w, h, dstFmt,
                    SWS_BILINEAR,
                    null, null, (double[]) null);
            if (context == null) {
                throw new RuntimeException("sws_getCachedContext returned null");
            }

            // 2) Full-range + BT.601 coefficients; use correct IntPointer overloads
            // For SD content, ITU-601 (bt470bg) is typical for MJPEG; change to
            // SWS_CS_DEFAULT or SWS_CS_ITU709 if appropriate.
            int cs = SWS_CS_ITU601;
            IntPointer inv = sws_getCoefficients(cs);
            IntPointer tab = sws_getCoefficients(cs);

            int srcRange = 1; // full range for YUVJ input
            int dstRange = 1; // keep full range output (common for UI processing)
            int brightness = 0;
            int contrast = 1 << 16; // unity in swscale fixed-point
            int saturation = 1 << 16; // unity in swscale fixed-point

            // Set colorspace/range; ignore return code < 0 only if library too old, but
            // typically this succeeds
            sws_setColorspaceDetails(context, inv, srcRange, tab, dstRange, brightness, contrast, saturation);

            // 3) Allocate destination AVFrame and buffer properly
            AVFrame dst = av_frame_alloc();
            if (dst == null) {
                throw new RuntimeException("av_frame_alloc failed");
            }
            dst.format(dstFmt);
            dst.width(w);
            dst.height(h);

            // Compute buffer size and allocate a direct buffer
            int bufSize = av_image_get_buffer_size(dstFmt, w, h, 1);
            if (bufSize < 0) {
                av_frame_free(dst);
                throw new RuntimeException("av_image_get_buffer_size failed");
            }
            if (dstBufferCache == null || dstBufferCache.capacity() != bufSize) {
                dstBufferCache = ByteBuffer.allocateDirect(bufSize);
            } else {
                dstBufferCache.clear();
            }
            ByteBuffer dstBuf = dstBufferCache;

            // Fill dst->data/linesize to point into dstBuf
            // Use a BytePointer on the direct buffer; do not use Pointer.pointerToBuffer()
            // for FFmpeg image fill
            BytePointer dstPtr = new BytePointer(dstBuf);
            int ret = av_image_fill_arrays(
                    dst.data(), dst.linesize(),
                    dstPtr,
                    dstFmt, w, h, 1);
            if (ret < 0) {
                av_frame_free(dst);
                throw new RuntimeException("av_image_fill_arrays failed: " + ret);
            }

            // 4) Scale from YUV -> BGR24
            int scaled = sws_scale(context, src.data(), src.linesize(), 0, h, dst.data(), dst.linesize());
            if (scaled != h) {
                av_frame_free(dst);
                throw new RuntimeException("sws_scale returned " + scaled + " (expected " + h + ")");
            }

            // 5) Wrap into a JavaCV Frame (BGR24, 3 channels)
            Frame out = new Frame(w, h, Frame.DEPTH_UBYTE, 3);
            out.imageStride = dst.linesize(0);
            out.createIndexer();

            ByteBuffer outBuf = (ByteBuffer) out.image[0];
            dstBuf.position(0).limit(bufSize);
            outBuf.position(0).limit(bufSize);
            outBuf.put(dstBuf);
            outBuf.flip();

            // 6) Cleanup native AVFrame structure (not the Java ByteBuffer)
            // av_frame_free frees only the AVFrame struct here (no av_malloc buffer was
            // tied to data[0]).
            av_frame_free(dst);

            return out;
        }

    }

    /**
     * Constructor for ByteDecoVideoSource.
     * 
     * @param device The video device to capture from
     * @param fourccPixelFormat The FOURCC pixel format string
     * @param width The width of the video frames
     * @param height The height of the video frames
     * @param fps The frames per second rate
     */
    public FFmpegVideoSource(String device, String fourccPixelFormat, int width, int height, int fps) {
        super();
        mCaptureThread = new VideoCaptureThread(device, fourccPixelFormat,  width, height, fps);
    }

    /**
     * Starts the video capture thread.
     * 
     * @throws IOException If an I/O error occurs during capture
     */
    @Override
    public void start() throws IOException {
        mCaptureThread.start();
    }

    /**
     * Stops the video capture thread.
     * 
     * @throws IOException If an I/O error occurs during capture
     */
    @Override
    public void stop() throws IOException {
        mCaptureThread.stopRunning();
    }

    /**
     * Gets the current image from the video capture.
     * 
     * @return The current BufferedImage, or null if no image is available
     */
    @Override
    public Image getImage() {
        return mCaptureThread.getImage();
    }

    /**
     * Adds an action listener to be notified when new frames are available.
     * 
     * @param listener The ActionListener to add
     */
    @Override
    public void addActionListener(ActionListener listener) {
        this.listener = listener;
    }
}
