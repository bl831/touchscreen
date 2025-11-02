/*
 * CameraImageTransform.java
 */

package willibert.NetCamLib;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

/**
 * 
 * @author willibert
 */
class CameraImageTransform {

    /** Creates a new instance of CameraImageTransform */
    public CameraImageTransform() {
        //
    }

    // Copies a rectangular part of the original image
    public Image crop(Image original, int[] parameter, ImageObserver observer) {
        int x = parameter[0];
        int y = parameter[1];

        int width = parameter[2];
        int height = parameter[3];

        int originalWidth = original.getWidth(observer);
        int originalHeight = original.getHeight(observer);

        if ((x >= originalWidth) || (y >= originalHeight)) {
            return original;
        }
        if ((x + width) > originalWidth) {
            width = originalWidth - x;
        }
        if ((x + width) > originalWidth) {
            width = originalWidth - x;
        }

        return mirror(original, 0, 0, width, height, x, y, x + width, y + height, observer);
    }

    // Resizes the original image by the given percentage value
    public Image resize(Image original, double percentage, ImageObserver observer) {
        int width = (int) (original.getWidth(observer) * percentage);
        int height = (int) (original.getHeight(observer) * percentage);

        return resize(original, width, height, observer);
    }

    // Resizes the original image to the new size
    public Image resize(Image original, int width, int height, ImageObserver observer) {
        BufferedImage bufferedImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = bufferedImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(original, 0, 0, width, height, observer);

        return bufferedImage;
    }

    // Flips the image horizontally
    public Image horizontalFlip(Image original, ImageObserver observer) {
        int width = original.getWidth(observer);
        int height = original.getHeight(observer);
        return mirror(original, width, 0, 0, height, 0, 0, width, height, observer);
    }

    // Flips the image vertically
    public Image verticalFlip(Image original, ImageObserver observer) {
        int width = original.getWidth(observer);
        int height = original.getHeight(observer);
        return mirror(original, 0, height, width, 0, 0, 0, width, height, observer);
    }

    // Rotates the image by 90 degrees
    public Image rotate090(Image original, ImageObserver observer) {
        return rotate90(original, true, observer);
    }

    // Rotates the image by 180 degrees
    public Image rotate180(Image original, ImageObserver observer) {
        Image temp = rotate90(original, true, observer);
        return rotate90(temp, true, observer);
    }

    // Rotates the image by 270 degrees
    public Image rotate270(Image original, ImageObserver observer) {
        return rotate90(original, false, observer);
    }

    private Image mirror(Image original, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1,
                         int sx2, int sy2, ImageObserver observer) {
        int width = Math.abs(sx1 - sx2);
        int height = Math.abs(sy1 - sy2);
        BufferedImage bufferedImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        Graphics g = bufferedImage.getGraphics();

        g.drawImage(original, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
        return bufferedImage;
    }

    private Image rotate90(Image original, boolean clockwise, ImageObserver observer) {
        int width = original.getWidth(observer);
        int height = original.getHeight(observer);

        BufferedImage bufferedImage = new BufferedImage(height, width,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = bufferedImage.createGraphics();
        if (clockwise) {
            AffineTransform aff = AffineTransform.getRotateInstance(Math.toRadians(90), 0, 0);
            g2.setTransform(aff);
            g2.drawImage(original, 0, -height, observer);
        } else {
            AffineTransform aff = AffineTransform.getRotateInstance(Math.toRadians(-90), 0, 0);
            g2.setTransform(aff);
            g2.drawImage(original, -width, 0, observer);
        }

        return bufferedImage;
    }

}
