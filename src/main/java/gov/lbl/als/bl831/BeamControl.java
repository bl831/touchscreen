package gov.lbl.als.bl831;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A thread that reads circle control commands from a stream, parses them, and
 * sends the appropriate commands to the video widget which actually renders the
 * circle.
 */
public class BeamControl extends Thread {

    private final BufferedReader mIn;
    private final VideoWidget    mVideoWidget;
    private final Pattern        mMovePattern;
    private final Pattern        mSizePattern;
    private final Pattern        mShapePattern;

    /**
     * @param input
     *        the file that we need to read commands from.
     * @param videoWidget
     *        where the circle will be rendered.
     */
    public BeamControl(InputStream input, VideoWidget videoWidget) {
        mIn = input != null ? new BufferedReader(new InputStreamReader(input)) : null;
        mVideoWidget = videoWidget;

        setDaemon(true);
        setName("Circle Control");

        mMovePattern = Pattern.compile("beam_center ([0-9.]+) ([0-9.]+)",
                Pattern.CASE_INSENSITIVE);
        mSizePattern = Pattern.compile("beam_size ([0-9.]+) ([0-9.]+)",
                Pattern.CASE_INSENSITIVE);
        mShapePattern = Pattern.compile("beam_shape ([a-z]+)", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public void run() {
        if (mIn == null) {
            //
            // No input. Don't do circle control
            //
            System.out.println("input not found.  No circle control available.");
            return;
        }
        while (true) {
            //
            // Main loop. Each command is a single line.
            //
            try {
                while (true) {
                    // StringBuffer buff = new StringBuffer();
                    // int b = -1;
                    // do {
                    // b = mIn.read();
                    // if (b != -1) {
                    // buff.append(Character.valueOf((char) b));
                    // }
                    // } while (b != -1 && b != 10);
                    // String line = buff.toString();
                    // line = line.toLowerCase().trim();
                    String line = mIn.readLine();
                    if (line == null) {
                        continue;
                    }
                    if (line.isEmpty()) {
                        continue;
                    }
                    Matcher matcher = mMovePattern.matcher(line);
                    if (matcher.find()) {
                        move(matcher.group(1), matcher.group(2));
                    } else {
                        matcher = mSizePattern.matcher(line);
                        if (matcher.find()) {
                            size(matcher.group(1), matcher.group(2));
                        } else {
                            matcher = mShapePattern.matcher(line);
                            if (matcher.find()) {
                                shape(matcher.group(1));
                            } else {
                                System.err.printf("Unknown circle command: %s\n", line);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void move(String x, String y) {
        System.out.printf("Move to %s, %s\n", x, y);
        mVideoWidget.setCircle(Double.parseDouble(x), Double.parseDouble(y));
    }

    private void size(String w, String h) {
        System.out.printf("Size to %s, %s\n", w, h);
        mVideoWidget.setSize(Double.parseDouble(w), Double.parseDouble(h));
    }

    private void shape(String shape) {
        System.out.printf("Shape to %s\n", shape);
        mVideoWidget.setShape(shape);
    }
}
