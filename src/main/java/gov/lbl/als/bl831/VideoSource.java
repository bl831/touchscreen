package gov.lbl.als.bl831;

import java.awt.Image;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Abstraction of a video source.
 */
public interface VideoSource {

    /**
     * @return the most recent image.
     */
    Image getImage();

    /**
     * Adds an listener that will be called whenever a new image is available.
     * 
     * @param listener
     *        the listener that needs to be added.
     */
    void addActionListener(ActionListener listener);

    /**
     * Starts video grabbing.
     * @throws IOException 
     */
    void start() throws IOException;

     /**
     * Stops video grabbing.
     * @throws IOException 
     */
    void stop() throws IOException;
}
