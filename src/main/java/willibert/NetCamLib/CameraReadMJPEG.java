/*
 * CameraReadMJPEG.java
 */

package willibert.NetCamLib;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 
 * @author willibert
 */
class CameraReadMJPEG extends Thread {

    private final DataInputStream input;

    private ActionListener imageListener;

    private Image lastImage;

    private boolean imageLoop;

    private final char[] buffer = new char[10000];
    private int bufferPosition;
    private int bufferSize;
    private boolean readBuffer;

    /** Creates a new instance of CameraReadMJPEG */
    public CameraReadMJPEG(DataInputStream dis) {
        super();

        input = dis;

        imageListener = null;

        lastImage = null;

        imageLoop = false;

        bufferPosition = 0;
        bufferSize = 0;
        readBuffer = false;
    }

    // Starts the video stream
    @Override
    public void run() {
        byte value;

        ActionEvent imageEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "image");

        try {
            // start loop
            imageLoop = true;
            while (imageLoop == true) {
                value = input.readByte();
                if (readBuffer == true) {
                    int readPosition;
                    byte[] imageBuffer = new byte[bufferSize];

                    readPosition = 0;
                    value = input.readByte();

                    while (readPosition < bufferSize) {
                        value = input.readByte();
                        imageBuffer[readPosition++] = value;
                    }

                    lastImage = ImageIO.read(new ByteArrayInputStream(imageBuffer));
                    if (lastImage != null && imageListener != null) {
                        imageListener.actionPerformed(imageEvent);
                    }

                    imageBuffer = null;
                    readBuffer = false;
                } else {
                    boolean parseResult = parseStream(value);
                    if (parseResult == true) {
                        readBuffer = true;
                    }
                }
            }

            input.close();

        }

        catch (IOException exeption) {
            //
        }

    }

    // Stops the video stream
    public void halt() {
        imageLoop = false;
    }

    // Gets last image from video stream
    public Image getImage() {
        return lastImage;
    }

    // Analyzes the data stream
    private boolean parseStream(byte value) {
        boolean result = false;
        buffer[bufferPosition++] = (char) value;

        if (value == '\n') {
            String text = String.valueOf(buffer, 0, bufferPosition);
            if (text.startsWith("Content-Length") == true) {
                String size = text.substring(16, bufferPosition - 2);
                bufferSize = Integer.parseInt(size);
                result = true;
            }
            bufferPosition = 0;
        }

        return result;
    }

    // Returns the status of the video stream
    public boolean isRunning() {
        return imageLoop;
    }

    // Adds an action listener
    public void addActionListener(ActionListener listener) {
        imageListener = listener;
    }

}
