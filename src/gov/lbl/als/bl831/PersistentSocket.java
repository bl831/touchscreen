package gov.lbl.als.bl831;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

public class PersistentSocket {

    private final SocketAddress mAddress;
    private final int           mTimeout;
    private Socket              mSocket;
    private InputStream         mInput;
    private OutputStream        mOutput;

    public PersistentSocket(SocketAddress addr, int timeout) {
        mAddress = addr;
        mTimeout = timeout;
    }

    public InputStream getInputStream() {
        return new PersistentInput(this);
    }

    public OutputStream getOutputStream() {
        return new PersistentOutput(this);
    }

    private boolean isConnected() {
        return mSocket != null && mSocket.isConnected() && mOutput != null && mInput != null;
    }

    private void connect() {
        mSocket = new Socket();
        try {
            System.out.printf("Connecting to dcss %s.\n", mAddress.toString());
            mSocket.connect(mAddress, mTimeout);
            mInput = mSocket.getInputStream();
            mOutput = mSocket.getOutputStream();
        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }

    /**
     * @return
     */
    private int read() {
        int ret = -1;
        while (ret < 0) {
            try {
                while (!isConnected()) {
                    connect();
                    Thread.sleep(mTimeout);
                }
                ret = mInput.read();
            } catch (IOException ignore) {
                System.err.printf("Read error: %s.\n", ignore.getMessage());
                if (mSocket != null && !mSocket.isClosed()) {
                    try {
                        mSocket.close();
                    } catch (IOException e) {
                        //
                        // Ignore
                        //
                    }
                    mSocket = null;
                }
            } catch (InterruptedException e) {
                //
                // Ignore
                //
            }
        }
        return ret;
    }

    /**
     * @param b
     * @param off
     * @param len
     * @return
     */
    private int read(byte[] b, int off, int len) {
        int ret = -1;
        while (ret < 0) {
            try {
                while (!isConnected()) {
                    connect();
                    Thread.sleep(mTimeout);
                }
                ret = mInput.read(b, off, len);
            } catch (IOException ignore) {
                System.err.printf("Read error: %s.\n", ignore.getMessage());
                if (mSocket != null && !mSocket.isClosed()) {

                    try {
                        mSocket.close();
                    } catch (IOException e) {
                        //
                        // Ignore
                        //
                    }
                    mSocket = null;
                }
            } catch (InterruptedException e) {
                //
                // Ignore
                //
            }
        }
        return ret;
    }

    private int available() {
        int ret = 0;
        if (isConnected()) {
            try {
                ret = mInput.available();
            } catch (IOException e) {
                //
                // Ignore
                //
            }
        }
        return ret;
    }

    /**
     * Write a byte to the socket establishing a connection if there is none.
     * 
     * @param b
     *        byte to write.
     */
    private void write(int b) {
        try {
            //
            // Try to connect once, if no connection discard data.
            //
            if (!isConnected()) {
                connect();
            }
            if (isConnected()) {
                mOutput.write(b);
            }
        } catch (IOException ignore) {
            System.err.printf("Write error: %s.\n", ignore.getMessage());
            if (mSocket != null && !mSocket.isClosed()) {

                try {
                    mSocket.close();
                } catch (IOException e) {
                    //
                    // Ignore
                    //
                }
                mSocket = null;
            }
        }
    }

    /**
     * Write bytes to a socket.
     * 
     * @param b
     */
    private void write(byte[] b, int off, int len) {
        try {
            //
            // Try to connect once, if no connection discard data.
            //
            if (!isConnected()) {
                connect();
            }
            if (isConnected()) {
                mOutput.write(b, off, len);
            }
        } catch (IOException ignore) {
            System.err.printf("Write error: %s.\n", ignore.getMessage());
            if (mSocket != null && !mSocket.isClosed()) {

                try {
                    mSocket.close();
                } catch (IOException e) {
                    //
                    // Ignore
                    //
                }
                mSocket = null;
            }
        }
    }

    private void flush() {
        try {
            if (!isConnected()) {
                connect();
            }
            if (isConnected()) {
                mOutput.flush();
            }
        } catch (IOException e) {
            //
            // Ignore
            //
        }
    }

    protected static class PersistentInput extends InputStream {

        private final PersistentSocket mSocket;

        protected PersistentInput(PersistentSocket socket) {
            mSocket = socket;
        }

        @Override
        public int read() throws IOException {
            return mSocket.read();
        }

        @Override
        public int read(byte[] b, int off, int len) {
            return mSocket.read(b, off, len);
        }

        @Override
        public int available() {
            return mSocket.available();
        }
    }

    protected static class PersistentOutput extends OutputStream {

        private final PersistentSocket mSocket;

        protected PersistentOutput(PersistentSocket socket) {
            mSocket = socket;
        }

        @Override
        public void write(int b) throws IOException {
            mSocket.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            mSocket.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            mSocket.flush();
        }
    }
}
