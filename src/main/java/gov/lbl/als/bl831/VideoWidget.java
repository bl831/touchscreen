package gov.lbl.als.bl831;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

/**
 *
 */
public class VideoWidget extends JPanel {

    private static final int          kRectangle       = 1;
    private static final int          kEllipse         = 2;
    private static final long         serialVersionUID = 1L;
    private Image                     mCurrentImage;
    private final ClickSink           mClickSink;
    private final Config              mConfig;
    private final Map<Object, Object> mInterpHints;
    private final Map<Object, Object> mAliasHints;
    private Rectangle                 mVideoRect;
    private double                    mBeamX           = 0.5;
    private double                    mBeamY           = 0.5;
    private double                    mBeamH           = 0.0;
    private double                    mBeamW           = 0.0;
    private int                       mShape           = kRectangle;
    private final Stroke              mStroke3         = new BasicStroke(3);
    private final Stroke              mStroke5         = new BasicStroke(5);

    public VideoWidget(ClickSink clickSink, Config config) {
        super();
        mClickSink = clickSink;
        mConfig = config;
        mInterpHints = new HashMap<Object, Object>(1);
        if (mConfig.getInterpolationHint() != null) {
            mInterpHints.put(RenderingHints.KEY_INTERPOLATION, mConfig.getInterpolationHint());
        }
        mAliasHints = new HashMap<Object, Object>(1);
        mAliasHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (mVideoRect != null && mVideoRect.contains(e.getPoint())) {
                    double x = (e.getX() - mVideoRect.x) / (double) mVideoRect.width;
                    double y = (e.getY() - mVideoRect.y) / (double) mVideoRect.height;
                    mClickSink.videoClicked(x, y, (double) mVideoRect.height
                                                  / (double) mVideoRect.width);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(704, 480);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (mCurrentImage != null) {
            int imageWidth = mCurrentImage.getWidth(this);
            int imageHeight = mCurrentImage.getHeight(this);
            if (imageWidth <= 0 || imageHeight <= 0) {
                System.out.printf("Incomplete dimensions: %d x %d\n", imageWidth, imageHeight);
                return;
            }
            Dimension destDim = computeDestinationDimension(mCurrentImage, imageWidth,
                    imageHeight);
            Dimension size = getSize();
            int xOffset = (size.width - destDim.width) / 2;
            int yOffset = (size.height - destDim.height) / 2;
            Graphics2D g2d = (Graphics2D) g;
            if (!mInterpHints.isEmpty()) {
                g2d.addRenderingHints(mInterpHints);
            }
            g.drawImage(mCurrentImage, xOffset, yOffset, destDim.width + xOffset,
                    destDim.height + yOffset, 0, 0, imageWidth, imageHeight, this);
            mVideoRect = new Rectangle(new Point(xOffset, yOffset), destDim);

            if (mBeamH > 0.0 && mBeamW > 0.0) {
                if (!mAliasHints.isEmpty()) {
                    g2d.addRenderingHints(mAliasHints);
                }

                g2d.setPaint(Color.black);
                g2d.setStroke(mStroke5);

                double w = mVideoRect.getWidth() * mBeamW;
                double h = mVideoRect.getHeight() * mBeamH;
                double x = mVideoRect.getWidth() * mBeamX;
                double y = mVideoRect.getHeight() * mBeamY;

                if (mShape == kEllipse) {
                    Ellipse2D.Double ellipse = new Ellipse2D.Double(xOffset + x - w / 2,
                            yOffset + y - h / 2, w, h);
                    g2d.draw(ellipse);

                    g2d.setPaint(Color.white);
                    g2d.setStroke(mStroke3);
                    ellipse = new Ellipse2D.Double(xOffset + x - w / 2 + 1, yOffset + y - h
                                                                            / 2 + 1, w - 3,
                            h - 2);
                    g2d.draw(ellipse);
                } else if (mShape == kRectangle) {
                    Rectangle2D.Double rectangle = new Rectangle2D.Double(xOffset + x - w / 2,
                            yOffset + y - h / 2, w, h);
                    g2d.draw(rectangle);

                    g2d.setPaint(Color.white);
                    g2d.setStroke(mStroke3);
                    rectangle = new Rectangle2D.Double(xOffset + x - w / 2 + 1, yOffset + y
                                                                                - h / 2 + 1,
                            w - 2, h - 2);
                    g2d.draw(rectangle);
                }
            }
        }
    }

    /**
     * @param currentImage
     * @param imageHeight
     * @param imageWidth
     * @return
     */
    private Dimension computeDestinationDimension(Image currentImage, int imageWidth,
                                                  int imageHeight) {
        int areaWidth = getSize().width;
        int areaHeight = getSize().height;

        double scaleFactor = (double) areaHeight / (double) imageHeight;

        int destHeight = (int) (imageHeight * scaleFactor);
        int destWidth = (int) (imageWidth * scaleFactor);

        if (destWidth > areaWidth) {
            //
            // Doesn't fit horizontally... Use the other horizontal axis to
            // figure out the scale instead.
            //
            scaleFactor = (double) areaWidth / (double) imageWidth;
            destHeight = (int) (imageHeight * scaleFactor);
            destWidth = (int) (imageWidth * scaleFactor);
        }
        return new Dimension(destWidth, destHeight);
    }

    /**
     * Updates the image to be displayed.
     * 
     * @param newImage
     *        the new image.
     */
    public void setImage(Image newImage) {
        Image oldImage = mCurrentImage;
        mCurrentImage = newImage;
        if (oldImage != null) {
            oldImage.flush();
        }
        repaint();
    }

    /**
     * Sets beam size
     * 
     * @param w
     *        width of beam
     * @param h
     *        height of beam
     */
    public void setSize(double w, double h) {
        mBeamH = h;
        mBeamW = w;
    }

    public void setCircle(double x, double y) {
        mBeamX = x;
        mBeamY = y;
    }

    public void setShape(String shape) {
        if (shape != null) {
            if ("ellipse".equalsIgnoreCase(shape)) {
                mShape = kEllipse;
            } else if ("rectangle".equalsIgnoreCase(shape)) {
                mShape = kRectangle;
            }
        }
    }
}
