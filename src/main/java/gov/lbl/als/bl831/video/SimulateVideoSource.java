package gov.lbl.als.bl831.video;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;

import javax.swing.SwingUtilities;

import gov.lbl.als.bl831.Config;
import gov.lbl.als.bl831.VideoSource;

public class SimulateVideoSource implements VideoSource {
    
    private ActionListener                  mListener;
    private final int                       mWidth;
    private final int                       mHeight;
    private Image                           mCurrentImage;
    private final Toolkit                   mToolkit     = Toolkit.getDefaultToolkit();
    private final DirectColorModel          mColorModel  = new DirectColorModel(32,
                                                                 0x00FF0000, 0x0000FF00,
                                                                 0x000000FF);
    
    // Simple video capture thread
    private VideoCaptureThread              mCaptureThread;
    private volatile boolean                mIsRunning = false;
    
    // Simple video capture thread
    private class VideoCaptureThread extends Thread {
        private volatile boolean running = false;
        private long dropStartTime = 0;
        private boolean dropActive = false;
        private int dropX = 0;
        private int dropY = 0;
        private double figure8Angle = 0;
        private final double FIGURE_8_FREQUENCY = 0.002; // Even slower figure 8 pattern speed
        private double previousFigure8X = 0;
        private double previousFigure8Y = 0;
        private boolean isFirstFrame = true;
        
        @Override
        public void run() {
            running = true;
            try {
                // Simulate video capture
                while (running && mIsRunning) {
                    simulateVideoFrame();
                    Thread.sleep(33); // ~30 FPS
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        private void simulateVideoFrame() {
            // Create a simple animated image for simulation
            int[] pixels = new int[mWidth * mHeight];
            long time = System.currentTimeMillis();
            
            // Initialize first drop at center
            if (!dropActive) {
                dropX = mWidth / 2;
                dropY = mHeight / 2;
                dropStartTime = time;
                dropActive = true;
            }
            
            // Center of the image
            int centerX = mWidth / 2;
            int centerY = mHeight / 2;
            
            // Create figure 8 pattern movement
            if (dropActive) {
                // Calculate figure 8 pattern using parametric equations
                double figure8X = Math.sin(figure8Angle) * (mWidth / 3);
                double figure8Y = Math.sin(figure8Angle * 2) * (mHeight / 4);
                
                // For smooth transitions, interpolate between previous and current positions
                if (isFirstFrame) {
                    dropX = centerX + (int)figure8X;
                    dropY = centerY + (int)figure8Y;
                    previousFigure8X = figure8X;
                    previousFigure8Y = figure8Y;
                    isFirstFrame = false;
                } else {
                    // Smooth interpolation between previous and current positions
                    double smoothX = previousFigure8X * 0.3 + figure8X * 0.7;
                    double smoothY = previousFigure8Y * 0.3 + figure8Y * 0.7;
                    
                    dropX = centerX + (int)smoothX;
                    dropY = centerY + (int)smoothY;
                    
                    // Update previous values for next frame
                    previousFigure8X = smoothX;
                    previousFigure8Y = smoothY;
                }
                
                // Update angle for next frame
                figure8Angle += FIGURE_8_FREQUENCY;
                
                // Keep angle within bounds to ensure smooth looping
                if (figure8Angle > 2 * Math.PI) {
                    figure8Angle = 0;
                }
            }
            
            for (int y = 0; y < mHeight; y++) {
                for (int x = 0; x < mWidth; x++) {
                    // Calculate distance to the drop
                    int dx = x - dropX;
                    int dy = y - dropY;
                    double distance = Math.sqrt(dx * dx + dy * dy);
                    
                    // Create ripple effect
                    double totalRipple = 0;
                    
                    if (dropActive) {
                        // Calculate how long the drop has been active
                        long dropDuration = time - dropStartTime;
                        
                        // Create wave that continues indefinitely without stopping
                        double wave = Math.sin((distance - dropDuration / 50.0) / 20.0);
                        totalRipple += wave / (1 + distance / 200.0);
                    }
                    
                    // Normalize the ripple effect
                    totalRipple = Math.max(-1, Math.min(1, totalRipple));
                    
                    // Create a blue-green color scheme
                    double brightness = 0.1 + 0.9 * (1 + totalRipple) / 2.0;
                    
                    // Create a subtle blue-green color
                    int r = (int)(30 * brightness);
                    int g = (int)(100 * brightness);
                    int b = (int)(160 * brightness);
                    
                    // Clamp values to valid range
                    r = Math.max(0, Math.min(255, r));
                    g = Math.max(0, Math.min(255, g));
                    b = Math.max(0, Math.min(255, b));
                    
                    pixels[y * mWidth + x] = (0xFF << 24) | (r << 16) | (g << 8) | b;
                }
            }
            
            MemoryImageSource sourceImage = new MemoryImageSource(mWidth, mHeight,
                    mColorModel, pixels, 0, mWidth);
            mCurrentImage = mToolkit.createImage(sourceImage);
            
            if (mListener != null) {
                SwingUtilities.invokeLater(() -> mListener.actionPerformed(null));
            }
        }
        
        public void stopRunning() {
            running = false;
        }
    }

    public SimulateVideoSource(Config config) {
        // Set a default resolution
        mWidth = 640;
        mHeight = 480;
        
        System.out.println("Video source initialized with resolution: " + mWidth + "x" + mHeight);
    }

    @Override
    public void start() {
        if (!mIsRunning) {
            mIsRunning = true;
            mCaptureThread = new VideoCaptureThread();
            mCaptureThread.start();
            System.out.println("Video capture started");
        }
    }

    @Override
    public Image getImage() {
        return mCurrentImage;
    }

    @Override
    public void addActionListener(ActionListener listener) {
        mListener = listener;
    }
    
    // Add a method to stop the video capture
    public void stop() {
        mIsRunning = false;
        if (mCaptureThread != null) {
            mCaptureThread.stopRunning();
            try {
                mCaptureThread.join(1000); // Wait up to 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Video capture stopped");
    }
    
    // Add a method to get current dimensions
    public Dimension getDimension() {
        return new Dimension(mWidth, mHeight);
    }
}
