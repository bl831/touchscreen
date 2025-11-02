/*
 * Camera.java
 */

package willibert.NetCamLib;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import javax.imageio.ImageIO;

/**
 * Contains functions for image and video retrieval, image manupulation and PTZ
 * control. Supports cameras and encoders from Axis Communications with firmware
 * 4.x.
 * 
 * @author willibert
 * @version 0.1
 */
public class Camera implements ActionListener {

    private String cameraIPAddress;
    private int cameraPort;

    private boolean useAuthentication;
    String cameraUsername;
    String cameraPassword;

    private boolean useProxy;
    private String proxyIPAddress;
    private int proxyPort;

    private String channel;

    private boolean ptzEnabled;

    private String resolution;
    private int compression;
    private int framerate;

    private Image lastImage;

    private CameraReadMJPEG reader;

    private ActionEvent cameraEvent;
    private ActionListener cameraListener;

    private String savingPath;
    private String savingBaseName;
    private String savingType;
    private int savingMaxCount;
    private int savingCounter;

    private boolean manipulateEnabled;
    private boolean cropEnabled;
    private int[] cropParameter;
    private boolean flipEnabled;
    private String flipParameter;
    private boolean rotateEnabled;
    private String rotateParameter;

    /**
     * Creates a new instance of Camera
     */
    public Camera() {
        super();

        cameraIPAddress = "";
        cameraPort = 80;

        useAuthentication = false;
        cameraUsername = "root";
        cameraPassword = "pass";

        useProxy = false;
        proxyIPAddress = "";
        proxyPort = 8080;

        channel = "1";

        ptzEnabled = false;

        resolution = "";
        compression = -1;
        framerate = -1;

        lastImage = null;

        reader = null;

        cameraEvent = null;
        cameraListener = null;

        savingPath = "";
        savingBaseName = "";
        savingType = "";
        savingMaxCount = 0;
        savingCounter = 0;

        manipulateEnabled = false;
        cropEnabled = false;
        cropParameter = null;
        flipEnabled = false;
        flipParameter = "";
        rotateEnabled = false;
        rotateParameter = "";
    }

    /**
     * Sets the IP address and port of the camera
     */
    public void setIPAddress(String ipaddress, int port) {
        cameraIPAddress = ipaddress;
        cameraPort = port;
    }

    /**
     * Enables or disables the use of authentication
     */
    public void enableAuthentication(boolean value) {
        useAuthentication = value;
    }

    /**
     * Sets user name and password
     */
    public void setAuthentication(String username, String password) {
        cameraUsername = username;
        cameraPassword = password;
    }

    /**
     * Enables or disables the use of a proxy server
     */
    public void enableProxy(boolean value) {
        useProxy = value;
    }

    /**
     * Sets the IP address and port of the proxy server
     */
    public void setProxy(String ipaddress, int port) {
        proxyIPAddress = ipaddress;
        proxyPort = port;
    }

    /**
     * Sets the channel if the encoder has multiple channels
     * 
     * @param value
     *        defines the channel of the encoder, 0 is used for quad view
     */
    public void setChannel(int value) throws CameraException {
        if ((value < 0) || (value > 4)) {
            throw new CameraException("Parameter Exception");
        }
        channel = String.valueOf(value);
        if (value == 0) {
            channel = "quad";
        }
    }

    /**
     * Sets the PTZ functionality
     */
    public void enablePTZ(boolean value) {
        ptzEnabled = value;
    }

    /**
     * Returns PTZ functionality
     */
    public boolean isPTZEnabled() {
        return ptzEnabled;
    }

    /**
     * Sets the resolution of the video stream
     * 
     * @param value
     *        has to be a valid resolution such as "640x480"
     */
    public void setResolution(String value) {
        resolution = value;
    }

    /**
     * Sets the compression of the video stream
     * 
     * @param value
     *        is the compression rate between 0 (lowest) and 100 (highest)
     */
    public void setCompression(int value) {
        compression = value;
    }

    /**
     * Sets the frame rate of the video stream
     * 
     * @param value
     *        has to be a valid frame rate in fps
     */
    public void setFramerate(int value) {
        framerate = value;
    }

    /**
     * Sets the values for the image crop function
     * 
     * @param parameter
     *        defnes the coordinates of the window (left, upper, width, height)
     */
    public void setCrop(boolean enabled, int[] parameter) throws CameraException {
        cropEnabled = false;

        if (enabled == true) {
            if (parameter.length == 4) {
                cropParameter = new int[4];
                for (int i = 0; i < 4; i++) {
                    cropParameter[i] = parameter[i];
                }
                cropEnabled = true;
            } else {
                throw new CameraException("Parameter Exception");
            }
        }

        setManipulate();
    }

    /**
     * Sets the values for the image flip function
     * 
     * @param parameter
     *        defines the orientation, possible values are "hor" for horizontal
     *        flip and "ver" for vertical flip, respectively
     */
    public void setFlip(boolean enabled, String parameter) throws CameraException {
        flipEnabled = false;

        if (enabled == true) {
            flipParameter = "";
            if (parameter.startsWith("hor") == true) {
                flipParameter = parameter;
            }
            if (parameter.startsWith("ver") == true) {
                flipParameter = parameter;
            }
            if (flipParameter.length() > 0) {
                flipEnabled = true;
            } else {
                throw new CameraException("Parameter Exception");
            }
        }

        setManipulate();

    }

    /**
     * Sets the values for the image rotation
     * 
     * @param parameter
     *        defines the rotation angle, possibe values are "090", "180" and
     *        "270"
     */
    public void setRotate(boolean enabled, String parameter) throws CameraException {
        rotateEnabled = false;

        if (enabled == true) {
            rotateParameter = "";
            if (parameter.startsWith("090") == true) {
                rotateParameter = parameter;
            }
            if (parameter.startsWith("180") == true) {
                rotateParameter = parameter;
            }
            if (parameter.startsWith("270") == true) {
                rotateParameter = parameter;
            }
            if (rotateParameter.length() > 0) {
                rotateEnabled = true;
            } else {
                throw new CameraException("Parameter Exception");
            }
        }

        setManipulate();
    }

    // Sets the image maninpulation flag
    private void setManipulate() {
        manipulateEnabled = false;
        if ((cropEnabled == true) || (flipEnabled == true) || (rotateEnabled == true)) {
            manipulateEnabled = true;
        }
    }

    // Manipulates image
    private Image manipulate(Image image) {
        if (cropEnabled == true) {
            image = new CameraImageTransform().crop(image, cropParameter, null);
        }

        if (flipEnabled == true) {
            if (flipParameter.startsWith("hor") == true) {
                image = new CameraImageTransform().horizontalFlip(image, null);
            }
            if (flipParameter.startsWith("ver") == true) {
                image = new CameraImageTransform().verticalFlip(image, null);
            }
        }

        if (rotateEnabled == true) {
            if (rotateParameter.startsWith("090") == true) {
                image = new CameraImageTransform().rotate090(image, null);
            }
            if (rotateParameter.startsWith("180") == true) {
                image = new CameraImageTransform().rotate180(image, null);
            }
            if (rotateParameter.startsWith("270") == true) {
                image = new CameraImageTransform().rotate270(image, null);
            }
        }
        return image;
    }

    // Corrects the pan tilt values when image is transformed
    private float[] correctPanTilt(float x, float y) {
        float temp;
        float[] result = new float[2];

        if (flipEnabled == true) {
            if (flipParameter.startsWith("hor") == true) {
                x = x * -1.0f;
            }
            if (flipParameter.startsWith("ver") == true) {
                y = y * -1.0f;
            }
        }

        if (rotateEnabled == true) {
            if (rotateParameter.startsWith("090") == true) {
                temp = x;
                x = y * -1.0f;
                y = temp;
            }
            if (rotateParameter.startsWith("180") == true) {
                x = x * -1.0f;
                y = y * -1.0f;
            }
            if (rotateParameter.startsWith("270") == true) {
                temp = x;
                x = y;
                y = temp * -1.0f;
            }
        }

        result[0] = x;
        result[1] = y;

        return result;
    }

    // Executes a given command according to VAPIX API
    private void executeCommand(String command) throws CameraException {
        URL url = null;
        URLConnection connection = null;

        try {
            if (useAuthentication == true) {
                Authenticator.setDefault(new MyAuthenticator());
            }

            url = new URL(composeURL("") + command);

            if (useProxy == false) {
                connection = url.openConnection();
            } else {
                SocketAddress address = new InetSocketAddress(proxyIPAddress, proxyPort);
                Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
                connection = url.openConnection(proxy);
            }

            connection.setDoOutput(true);
            connection.connect();

            DataInputStream input = new DataInputStream(new BufferedInputStream(
                    connection.getInputStream()));

            // while(input.readByte() != -1) {}

            input.close();
        }

        catch (MalformedURLException exeption) {
            throw new CameraException("URL Exception");
        }

        catch (IOException exeption) {
            throw new CameraException("IO Exception");
        }

        finally {
            connection = null;
            url = null;
        }
    }

    /**
     * Returns a single image
     */
    public Image getSnapshot() throws CameraException {
        Image image = null;

        URL url = null;
        URLConnection connection = null;

        try {
            if (useAuthentication == true) {
                Authenticator.setDefault(new MyAuthenticator());
            }

            url = new URL(composeURL("JPEG"));

            if (useProxy == false) {
                connection = url.openConnection();
            } else {
                SocketAddress address = new InetSocketAddress(proxyIPAddress, proxyPort);
                Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
                connection = url.openConnection(proxy);
            }

            connection.connect();
            BufferedInputStream input = new BufferedInputStream(connection.getInputStream());
            image = ImageIO.read(input);
            if (manipulateEnabled == true) {
                image = manipulate(image);
            }
            input.close();
        }

        catch (MalformedURLException exeption) {
            throw new CameraException("URL Exception");
        }

        catch (IOException exeption) {
            throw new CameraException("IO Exception");
        }

        finally {
            connection = null;
            url = null;
        }

        return image;
    }

    /**
     * Starts the video stream
     */
    public void start() throws CameraException {

        URL url = null;
        URLConnection connection = null;

        try {
            if (useAuthentication == true) {
                Authenticator.setDefault(new MyAuthenticator());
            }

            url = new URL(composeURL("MJPEG"));

            if (useProxy == false) {
                connection = url.openConnection();
            } else {
                SocketAddress address = new InetSocketAddress(proxyIPAddress, proxyPort);
                Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
                connection = url.openConnection(proxy);
            }

            connection.connect();

            DataInputStream input = new DataInputStream(new BufferedInputStream(
                    connection.getInputStream()));
            cameraEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "message");

            reader = new CameraReadMJPEG(input);
            reader.addActionListener(this);
            reader.start();
        }

        catch (MalformedURLException exeption) {
            throw new CameraException("URL Exception");
        }

        catch (IOException exeption) {
            throw new CameraException("IO Exception");
        }

        finally {
            connection = null;
            url = null;
        }
    }

    /**
     * Stops the video stream
     */
    public void halt() {
        if (reader != null) {
            reader.halt();
        }
    }

    /**
     * Gets last image from video stream
     */
    public Image getImage() {
        if (manipulateEnabled == true) {
            lastImage = manipulate(lastImage);
        }
        return lastImage;
    }

    /**
     * Returns the status of the video stream
     */
    public boolean isRunning() {
        if (reader == null) {
            return false;
        }

        return reader.isRunning();
    }

    /**
     * Reacts on image event
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        lastImage = reader.getImage();
        cameraListener.actionPerformed(cameraEvent);
    }

    /**
     * Adds an action listener
     */
    public void addActionListener(ActionListener listener) {
        cameraListener = listener;
    }

    /**
     * Sends a pan/tilt command to the camera
     * 
     * @param x
     *        defines the relative pan value
     * @param y
     *        defines the relative tilt value
     */
    public void movePanTilt(float x, float y) throws CameraException {
        String command;

        if (manipulateEnabled == true) {
            float[] value = new float[2];
            value = correctPanTilt(x, y);
            x = value[0];
            y = value[1];
        }

        String xDirection = String.valueOf(x);
        String yDirection = String.valueOf(y);

        command = "axis-cgi/com/ptz.cgi?camera=" + channel;
        command = command + "&rpan=";
        command = command + xDirection;
        command = command + "&rtilt=";
        command = command + yDirection;

        try {
            executeCommand(command);
        } catch (CameraException exception) {
            throw new CameraException("PTZ Exception");
        }
    }

    /**
     * Sends a zoom command to the camera
     * 
     * @param z
     *        defines the relative zoom value
     */
    public void moveZoom(int z) throws CameraException {
        String command;

        String zDirection = String.valueOf(z);

        command = "axis-cgi/com/ptz.cgi?camera=" + channel;
        command = command + "&rzoom=";
        command = command + zDirection;

        try {
            executeCommand(command);
        } catch (CameraException exception) {
            throw new CameraException("PTZ Exception");
        }
    }

    /**
     * Sends a focus command to the camera
     * 
     * @param value
     *        defines the relative focus value, 0 represents autofocus on
     */
    public void changeFocus(int value) throws CameraException {
        String command;

        String focus = String.valueOf(value);

        command = "axis-cgi/com/ptz.cgi?camera=" + channel;
        if (value == 0) {
            command = command + "&autofocus=on";
        } else {
            command = command + "&autofocus=off&rfocus=" + focus;
        }

        try {
            executeCommand(command);
        } catch (CameraException exception) {
            throw new CameraException("PTZ Exception");
        }
    }

    /**
     * Sends an iris command to the camera
     * 
     * @param value
     *        defines the relative focus value, 0 represents autoiris on
     */
    public void changeIris(int value) throws CameraException {
        String command;

        String iris = String.valueOf(value);

        command = "axis-cgi/com/ptz.cgi?camera=" + channel;
        if (value == 0) {
            command = command + "&autoiris=on";
        } else {
            command = command + "&autoiris=off&riris=" + iris;
        }

        try {
            executeCommand(command);
        } catch (CameraException exception) {
            throw new CameraException("PTZ Exception");
        }
    }

    /**
     * Sets IR cut filter mode
     * 
     * @param mode
     *        defines the IR cut filter mode, possible values are "on" and "off"
     */
    public void setIRCutFilterMode(String mode) throws CameraException {
        String command;

        command = "axis-cgi/admin/param.cgi?action=update&";
        command = command + "PTZ.Various.V" + channel;
        command = command + ".IRCutFilter=" + mode;

        try {
            executeCommand(command);
        } catch (CameraException exception) {
            throw new CameraException("PTZ Exception");
        }
    }

    /**
     * Configures automatic saving of files
     * 
     * @param path
     *        defines the path for the archive
     * @param baseName
     *        defines the file name
     * @param type
     *        defines if there is an additional part in the filename, possible
     *        values are "Index" and "DateTime"
     * @param maxCount
     *        defines the maximum index number
     */
    public void configureSaving(String path, String baseName, String type, int maxCount) {
        savingPath = path;
        savingBaseName = baseName;
        savingType = type;
        savingMaxCount = maxCount;
        savingCounter = 0;
    }

    /**
     * Saves file according to specification
     */
    public void saveToFile(Image image) throws CameraException {
        String savingFileName;
        String savingFileSuffix = ".jpg";
        String savingExtra = "";

        if (savingType.startsWith("Index") == true) {
            savingExtra = " " + String.valueOf(savingCounter);
            savingCounter++;
            if (savingCounter > savingMaxCount) {
                savingCounter = 0;
            }
        }
        if (savingType.startsWith("DateTime") == true) {
            Calendar now = Calendar.getInstance();
            savingExtra = String.format(" %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL", now);
        }

        savingFileName = savingPath + savingBaseName + savingExtra + savingFileSuffix;

        try {
            File file = new File(savingFileName);
            ImageIO.write((BufferedImage) image, "jpg", file);
        }

        catch (IOException exception) {
            throw new CameraException("IO Exception");
        }
    }

    // Composes a URL string for a snapshot
    private String composeURL(String streamType) {
        String urlString;

        urlString = "http://" + cameraIPAddress + ":" + String.valueOf(cameraPort) + "/";

        if (streamType.length() == 0) {
            return urlString;
        }

        if (streamType.startsWith("MJPEG") == true) {
            urlString = urlString + "axis-cgi/mjpg/video.cgi?";
        }
        if (streamType.startsWith("JPEG") == true) {
            urlString = urlString + "axis-cgi/jpg/image.cgi?";
        }

        urlString = urlString + "showlength=1";
        urlString = urlString + "&camera=" + channel;

        if (resolution.length() > 0) {
            urlString = urlString + "&resolution=" + resolution;
        }
        if (compression != -1) {
            urlString = urlString + "&compression=" + String.valueOf(compression);
        }
        if ((framerate != -1) && (streamType.startsWith("MJPEG") == true)) {
            urlString = urlString + "&fps=" + String.valueOf(framerate);
        }

        return urlString;
    }

    // MyAuthenticator class
    class MyAuthenticator extends Authenticator {

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(cameraUsername, cameraPassword.toCharArray());
        }
    }
}
