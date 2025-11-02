package gov.lbl.als.bl831;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * In charge of emulating the Keytec touch screen.
 */
public class EmulateTouch implements ClickSink {

    private final OutputStream mOut;
    private final boolean      mEmulated;

    public EmulateTouch(OutputStream output, boolean emulated) {
        mEmulated = emulated;
        mOut = new BufferedOutputStream(output);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buttonPressed(VirtualButton button) {
        try {
            if (mEmulated) {
                sendDownUp(button.x(), button.y());
            } else {
                mOut.write(String.format("%s\n", button.getSymbol()).getBytes());
                mOut.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a "Finger Down" followed by a "Finger Up" event.
     * 
     * @param x
     *        the x coordinate of the event.
     * @param y
     *        the y coordinate of the event.
     * @throws IOException
     *         if there is a problem writing to the output.
     */
    private void sendDownUp(int x, int y) throws IOException {
        //
        // Emulate "Finger down".
        //
        mOut.write(0300);
        mOut.write(x >> 7);
        mOut.write(x & 0x7f);
        mOut.write(y >> 7);
        mOut.write(y & 0x7f);

        //
        // Emulate "Finder release" at same location.
        //
        mOut.write(0200);
        mOut.write(x >> 7);
        mOut.write(x & 0x7f);
        mOut.write(y >> 7);
        mOut.write(y & 0x7f);

        mOut.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void lightSlideClicked(double value) {
        try {
            if (mEmulated) {
                sendDownUp(180, (int) (370 + value * 210));
            } else {
                mOut.write(String.format("light %g\n", 1.0 - value).getBytes());
                mOut.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void videoClicked(double x, double y, double heightOverWidth) {
        try {
            if (mEmulated) {
                sendDownUp((int) (240 + x * 410), (int) (210 + y * 345));
            } else {
                mOut.write(String.format("video %g %g\n", x, y).getBytes());
                mOut.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void heartbeat() {
        try {
            if (!mEmulated) {
                mOut.write("heartbeat\n".getBytes());
                mOut.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
