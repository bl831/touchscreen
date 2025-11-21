package gov.lbl.als.bl831;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import gov.lbl.als.bl831.V4L2UriParser.V4L2UriComponents;
import gov.lbl.als.bl831.video.AxisVideoSource;
import gov.lbl.als.bl831.video.FFmpegVideoSource;
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

        CommandLineArgs cla = new CommandLineArgs();
        try {
            CommandLine cl = new CommandLine(cla);
            ParseResult parseResult = cl.parseArgs(args);
            if (CommandLine.printHelpIfRequested(parseResult)) {
                System.exit(0);
            };
        } catch (ParameterException ex) {
            System.err.println(ex.getMessage() + System.lineSeparator());
            ex.getCommandLine().usage(System.err);
            System.exit(1);
        }

        final Config config = convertCla(cla);

        if (cla.getVideoCaptureListUris()) {
            try {
                listAllV4L2Uris();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }

        try {
            final VideoSource videoSource = configCamera(cla);
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
    private static VideoSource configCamera(CommandLineArgs cla) throws CameraException {
        if (!cla.getVideoCaptureUri().isEmpty()) {
            try {
                // return new SimulateVideoSource(config);
                V4L2UriComponents c = V4L2UriParser.parseUri(cla.getVideoCaptureUri());
                return new FFmpegVideoSource("/dev/" + c.getDevice(), c.getPixelFormat(), c.getWidth(), c.getHeight(), (int)c.getFps());
            } catch (IllegalArgumentException ex) {
                throw new CameraException ("'" + cla.getVideoCaptureUri() + "' could not be created. " + ex.getMessage());
            }   
        }
        return new AxisVideoSource(cla);
    }

    /**
     * Parses the command line arguments into a Config object that will be used
     * by the rest of the program.
     * 
     * @param args
     *        the command line arguments as they were passed into main.
     */
    private static Config convertCla(CommandLineArgs args) {
        return new Config(args.getDcsshostname(), Integer.toString(args.getDcssport()), args.getHostname(), Integer.toString(args.getPort()),
                Integer.toString(args.getCamera()), !args.getVideoCaptureUri().isEmpty(),
                args.getEmulate(), args.getInterpolation(), args.getFramesPerSecond());
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
