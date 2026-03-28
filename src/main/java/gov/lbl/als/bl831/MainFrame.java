package gov.lbl.als.bl831;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import gov.lbl.als.bl831.V4L2UriParser.V4L2UriComponents;
import gov.lbl.als.bl831.video.FFmpegVideoSource;
import gov.lbl.als.bl831.video.SimulateVideoSource;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;
import willibert.NetCamLib.CameraException;

public class MainFrame extends JFrame {

    private static final long     serialVersionUID = 1L;
    private CrystalCenteringPanel mCrystalCenteringPanel;

    public MainFrame(ClickSink clickSink, final VideoSource videoSource, InputStream input,
                     Config config) {
        final VideoWidget videoWidget = initComponents(clickSink, config);
        videoSource.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                videoWidget.setImage(videoSource.getImage());
            }
        });

        try {
            videoSource.start();
        } catch (IOException ex) {
            System.err.println("Error starting video service. " + ex.getMessage());
        }

        //
        // Start the circle listener
        //
        BeamControl circleControl;
        circleControl = new BeamControl(input, videoWidget);
        circleControl.start();
    }

    private VideoWidget initComponents(ClickSink clickSink, Config config) {
        CrystalCenteringPanel crystalCenteringPanel = getCrystalCenteringPanel(clickSink,
                config);
        add(crystalCenteringPanel, BorderLayout.CENTER);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setUndecorated(true);

        AbstractAction exitAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };
        crystalCenteringPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                "exit");
        crystalCenteringPanel.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK), "exit");
        crystalCenteringPanel.getActionMap().put("exit", exitAction);

        return crystalCenteringPanel.getVideoWidget();
    }

    private CrystalCenteringPanel getCrystalCenteringPanel(ClickSink clickSink, Config config) {
        if (mCrystalCenteringPanel == null) {
            mCrystalCenteringPanel = new CrystalCenteringPanel(clickSink, config);
        }

        return mCrystalCenteringPanel;
    }

    /**
     * Main entry of the class. Note: This class is only created so that you can
     * easily preview the result at runtime. It is not expected to be managed by
     * the designer. You can modify it as you like.
     */
    public static void main(String[] args) {

        CommandLineArgs cla = new CommandLineArgs();
        ParseResult parseResult = null;
        try {
            CommandLine cl = new CommandLine(cla);
            parseResult = cl.parseArgs(args);
            if (CommandLine.printHelpIfRequested(parseResult)) {
                System.exit(0);
            };
        } catch (ParameterException ex) {
            System.err.println(ex.getMessage() + System.lineSeparator());
            ex.getCommandLine().usage(System.err);
            System.exit(1);
        }

        String configFile = cla.getConfigFile();
        if (!configFile.isEmpty()) {
            Properties props = loadConfigFile(configFile);
            if (props != null) {
                cla.applyConfigFile(props, parseResult);
            }
        }

        final Config config = convertCla(cla);

        if (cla.getListV4l2()) {
            try {
                listAllV4L2Uris();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }

        try {
            final VideoSource videoSource = configCamera(cla);
            final ClickSink clickSink;
            final InputStream inputStream;

            if (cla.getSimulate()) {
                clickSink = createNoOpClickSink();
                inputStream = null;
            } else {
                if (config.getTouchHostname() == null) {
                    System.err.println("Touch server URI is required. Use -t or --touch to specify.");
                    System.exit(1);
                }
                PersistentSocket socket = new PersistentSocket(new InetSocketAddress(
                        config.getTouchHostname(), config.getTouchPort()), 2000);
                clickSink = new EmulateTouch(socket.getOutputStream(),
                        config.isEmulate());
                inputStream = socket.getInputStream();
            }

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    MainFrame frame = new MainFrame(clickSink, videoSource, inputStream,
                            config);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setTitle("MainFrame");
                    frame.getContentPane().setPreferredSize(frame.getSize());
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                    if (cla.getSimulate()) {
                        setupSimulatedBeam(frame, videoSource);
                    }
                }
            });

            Thread ping = new Thread() {

                @Override
                public void run() {
                    do {
                        clickSink.heartbeat();
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            //
                            // Ignore
                            //
                        }
                    } while (true);
                }
            };
            ping.setDaemon(true);
            ping.setName("Heartbeat");
            ping.start();

        } catch (CameraException e1) {
            System.err.printf("Unable to start video source: %s\n", e1);
            System.exit(2);
        }
    }

    /**
     * Creates a no-op ClickSink that discards all events. Used in simulate
     * mode when there is no DCSS connection.
     */
    private static ClickSink createNoOpClickSink() {
        return new ClickSink() {
            @Override public void buttonPressed(VirtualButton button) {}
            @Override public void lightSlideClicked(double value) {}
            @Override public void videoClicked(double x, double y, double heightOverWidth) {}
            @Override public void heartbeat() {}
        };
    }

    /**
     * Sets up a simulated beam overlay. Waits for the first video frame on a
     * background thread to get the source dimensions, then draws a small
     * circle centered on the video.
     */
    private static void setupSimulatedBeam(MainFrame frame, VideoSource videoSource) {
        new Thread(() -> {
            try {
                Image img = null;
                for (int i = 0; i < 50; i++) {
                    img = videoSource.getImage();
                    if (img != null && img.getWidth(null) > 0) break;
                    Thread.sleep(100);
                }
                final double srcWidth = (img != null && img.getWidth(null) > 0)
                        ? img.getWidth(null) : 704.0;
                final double srcHeight = (img != null && img.getHeight(null) > 0)
                        ? img.getHeight(null) : 480.0;
                double radius = 0.03;
                double beamW = radius * srcHeight / srcWidth;
                VideoWidget vw = frame.mCrystalCenteringPanel.getVideoWidget();
                System.out.printf("VideoWidget: %dx%d, source: %.0fx%.0f, beamW=%.4f, beamH=%.4f%n",
                        vw.getSize().width, vw.getSize().height,
                        srcWidth, srcHeight, beamW, radius);
                SwingUtilities.invokeLater(() -> {
                    vw.setShape("ellipse");
                    vw.setSize(beamW, radius);
                    vw.setCircle(0.5, 0.5);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "SimBeamSetup").start();
    }

    /**
     * Creates the appropriate video source for video display.
     *
     * @param cla
     *        the parsed command line arguments.
     * @return the configured video source.
     * @throws CameraException
     *         if the video source could not be created.
     */
    private static VideoSource configCamera(CommandLineArgs cla) throws CameraException {
        if (cla.getSimulate()) {
            return new SimulateVideoSource();
        }

        String videoUri = cla.getVideoUri();

        if (videoUri.startsWith("axis://")) {
            return new FFmpegVideoSource(AxisUriParser.toHttpUri(videoUri));
        }

        if (videoUri.startsWith("v4l2://")) {
            try {
                V4L2UriComponents c = V4L2UriParser.parseUri(videoUri);
                return new FFmpegVideoSource("/dev/" + c.getDevice(), c.getPixelFormat(),
                        c.getWidth(), c.getHeight(), (int) c.getFps());
            } catch (IllegalArgumentException ex) {
                throw new CameraException("'" + videoUri + "' could not be created. "
                        + ex.getMessage());
            }
        }

        if (!videoUri.isEmpty()) {
            throw new CameraException("Unknown video URI scheme: " + videoUri
                    + "\nSupported schemes: axis://, v4l2://");
        }

        //
        // No video URI specified.
        //
        System.err.println("Video source URI is required. Use -v or --video to specify.");
        System.exit(1);
        return null;
    }

    /**
     * Converts the command line arguments into a Config object.
     *
     * @param cla
     *        the parsed command line arguments.
     * @return the configuration.
     */
    private static Config convertCla(CommandLineArgs cla) {
        String touchHostname = null;
        int touchPort = 14000;

        String touchUri = cla.getTouchUri();
        if (touchUri != null && !touchUri.isEmpty()) {
            try {
                URI uri = new URI(touchUri.replaceFirst("^touch://", "http://"));
                touchHostname = uri.getHost();
                if (uri.getPort() != -1) {
                    touchPort = uri.getPort();
                }
            } catch (URISyntaxException e) {
                System.err.printf("Invalid touch URI '%s'.%n", touchUri);
            }
        }

        return new Config(touchHostname, touchPort, cla.getEmulate(), cla.getInterpolation());
    }

    /**
     * Loads a properties file from the given path.
     *
     * @param path
     *        the path to the config file.
     * @return the loaded properties, or null if the file could not be read.
     */
    private static Properties loadConfigFile(String path) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
            return props;
        } catch (IOException e) {
            System.err.printf("Unable to load config file '%s': %s%n",
                    path, e.getMessage());
            return null;
        }
    }

    private static void listAllV4L2Uris() throws IOException {

        List<String> uris = new ArrayList<>();

        CameraEnumerator ce = new CameraEnumerator();
        Collection<String> devices = ce.getVideoDevices();
        for (String device : devices) {
            uris.addAll(ce.getV4L2Uris(device));
        }

        if (uris.isEmpty()) {
            System.out.println("There are no available video capture devices");
        } else {
            uris.forEach(u -> System.out.println(u));
        }
    }
}
