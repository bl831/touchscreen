package gov.lbl.als.bl831;

import java.awt.BorderLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.net.InetSocketAddress;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

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
        videoSource.start();

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
        setSize(1680, 1050);
        setUndecorated(true);
        crystalCenteringPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                "exit");
        crystalCenteringPanel.getActionMap().put("exit", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

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
        final Config config = parseArgs(args);
        try {
            final VideoSource videoSource = configCamera(config);
            PersistentSocket socket = new PersistentSocket(new InetSocketAddress(
                    config.getDcssHostname(), config.getDcssPort()), 2000);
            final ClickSink clickSink = new EmulateTouch(socket.getOutputStream(),
                    config.isEmulate());
            final InputStream inputStream = socket.getInputStream();

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
            System.err.printf("Unable to connect to the Axis Video Server: %s\n", e1);
            System.exit(2);
        }
    }

    /**
     * Creates the appropriate video source for video display.
     * 
     * @param config
     * @return
     * @throws CameraException
     */
    private static VideoSource configCamera(Config config) throws CameraException {
        if (config.isVideoCapture()) {
            return new CaptureVideoSource(config);
        }
        return new AxisVideoSource(config);
    }

    /**
     * Parses the command line arguments into a Config object that will be used
     * by the rest of the program.
     * 
     * @param args
     *        the command line arguments as they were passed into main.
     */
    private static Config parseArgs(String[] args) {
        String hostname = "axis";
        String port = "80";
        String camera = "4";
        String dcsshostname = "dcss";
        String dcssport = "14000";
        boolean videoCapture = false;
        boolean emulate = false;
        Object interpHint = null;
        float framesPerSecond = (float) 24.0;

        for (int index = 0; index < args.length; index++) {
            if (args[index].equals("-h")) {
                hostname = args[++index];
            } else if (args[index].equals("-p")) {
                port = args[++index];
            } else if (args[index].equals("-c")) {
                camera = args[++index];
            } else if (args[index].equals("-d")) {
                dcsshostname = args[++index];
            } else if (args[index].equals("-r")) {
                dcssport = args[++index];
            } else if (args[index].equals("-v")) {
                videoCapture = true;
            } else if (args[index].equals("-e")) {
                emulate = true;
            } else if (args[index].equals("-f")) {
                try {
                    framesPerSecond = Float.parseFloat(args[++index]);
                } catch (NumberFormatException ignore) {
                    // Ignore;
                }
            } else if (args[index].equals("-i")) {
                String s = args[++index];
                if ("nearest".equalsIgnoreCase(s)) {
                    interpHint = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                } else if ("bilinear".equalsIgnoreCase(s)) {
                    interpHint = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                } else if ("bicubic".equalsIgnoreCase(s)) {
                    interpHint = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                }
            } else {
                System.err.printf("Unknown option: %s\n", args[index]);
                System.err.println("Valid options:");
                System.err
                        .println("   -h hostname        the Axis server name. (defaults to localhost)");
                System.err
                        .println("   -p port            the Axis server port. (defaults to 80)");
                System.err
                        .println("   -d dcsshostname    the DCSS touch server name. (defaults to dcss)");
                System.err
                        .println("   -r dcssport        the DCSS touch server port. (defaults to 14000)");
                System.err
                        .println("   -c camera          the camera stream number to display. (defaults to 4)");
                System.err
                        .println("   -v                 causes the internal video capture system to be used.");
                System.err
                        .println("   -e                 emulate old-style touch coordinates for output.");
                System.err
                        .println("   -i hint            image interpolation. (can be 'nearest', 'bilinear', or 'bicubic').");
                System.err
                        .println("   -f frames per sec  capture device frame rate. (can be 15.0, 24.0, or 30.0).");

                System.exit(1);
            }
        }

        return new Config(dcsshostname, dcssport, hostname, port, camera, videoCapture,
                emulate, interpHint, framesPerSecond);
    }
}
