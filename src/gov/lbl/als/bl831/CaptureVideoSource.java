package gov.lbl.als.bl831;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.util.Vector;

import javax.media.Buffer;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Codec;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.PlugInManager;
import javax.media.control.FormatControl;
import javax.media.format.JPEGFormat;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;

public class CaptureVideoSource implements VideoSource, BufferTransferHandler {

    private ActionListener                  mListener;
    private final Format                    mFormat;
    private final CaptureDeviceInfo         mDeviceInfo;
    private DataSource                      mDataSource;
    private final PushBufferStream          mPbs;
    private final Buffer                    mBuffer;
    private final int                       mWidth;
    private final int                       mHeight;
    private Image                           mCurrentImage;
    private Codec                           mConverter;
    private Buffer                          mBufferIn;
    private final Vector<String>            mImgSizes    = new Vector<String>();
    private final Vector<CaptureDeviceInfo> mCapDevices  = new Vector<CaptureDeviceInfo>();
    private final Vector<Format>            mCapFormats  = new Vector<Format>();
    private final Vector<Codec>             mCapConverts = new Vector<Codec>();
    private final Toolkit                   mToolkit     = Toolkit.getDefaultToolkit();
    private final DirectColorModel          mColorModel  = new DirectColorModel(32,
                                                                 0x00FF0000, 0x0000FF00,
                                                                 0x000000FF);

    public CaptureVideoSource(Config config) {

        selectJMFDevice();

        int idx = selectLargestRGB();
        String camera = mImgSizes.get(idx);
        mDeviceInfo = mCapDevices.elementAt(idx);
        mFormat = mCapFormats.elementAt(idx);
        int wh[] = parseWidthandHeight(camera);
        mWidth = wh[0];
        mHeight = wh[1];

        selectDataSource(config.getFramesPerSecondHint());

        if (mCapConverts.elementAt(idx) != null) {
            mConverter = mCapConverts.elementAt(idx);
            System.out.printf("Converter = %s, %s\n", mConverter.getName(),mConverter.getClass().getName());
        }

        Thread.yield();

        mPbs = ((PushBufferDataSource) mDataSource).getStreams()[0];
        mPbs.setTransferHandler(this);
        Thread.yield();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            // to let camera settle ahead of processing
        }

        mBuffer = new Buffer();
    }

    @Override
    public void start() {
        try {
            mDataSource.start();
        } catch (Exception e) {
            System.out.println("Exception dataSource.start() " + e);
        }
    }

    private void selectDataSource(float framesPerSecond) {
        CaptureDeviceInfo CapDevice = mDeviceInfo;
        System.out.println("Video device = " + CapDevice.getName());
        Format CapFormat = mFormat;
        System.out.println("Video format = " + CapFormat.toString());

        MediaLocator loc = CapDevice.getLocator();
        System.out.printf("MediaLocator: %s\n", loc);
        try {
            mDataSource = Manager.createDataSource(loc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // ensures 30 fps or as otherwise preferred
            FormatControl formConts[] = ((CaptureDevice) mDataSource).getFormatControls();
            FormatControl formCont = formConts[0];
            VideoFormat formatVideoNew = new VideoFormat(null, null, -1, null, framesPerSecond);
            Format intersec = CapFormat.intersects(formatVideoNew);
            formCont.setFormat(intersec);
            System.out.printf("Video format negotiated = %s\n", intersec.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectJMFDevice() {
        Vector< ? > deviceList = CaptureDeviceManager.getDeviceList(new VideoFormat(null));

        CaptureDeviceInfo deviceInfo = null;
        String type = "N/A";
        Codec converterCodec = null;
        boolean videoFormatMatch = false;
        for (int i = 0; i < deviceList.size(); i++) {
            //
            // search for video device
            //
            deviceInfo = (CaptureDeviceInfo) deviceList.elementAt(i);
            if (deviceInfo.getName().indexOf("v4l:") < 0
                && deviceInfo.getName().indexOf("vfw:") < 0) {
                continue;
            }

            System.out.printf("video capture device: %s\n", deviceInfo);
            Format deviceFormat[] = deviceInfo.getFormats();
            RGBFormat imgFormat = new RGBFormat(null, -1, Format.intArray, -1, 32, 0x00FF0000,
                    0x0000FF00, 0x000000FF, -1, -1, Format.FALSE, -1);
            for (int f = 0; f < deviceFormat.length; f++) {
                //
                // search for all video formats
                //
                System.out.printf("  Format: %s\n", deviceFormat[f]);
                if (deviceFormat[f].matches(imgFormat)) {
                    converterCodec = null;
                } else {
                    converterCodec = fetchCodec(deviceFormat[f], imgFormat);
                    if (converterCodec == null) {
                        continue;
                    }
                }

                if (deviceFormat[f] instanceof RGBFormat) type = "RGB";
                if (deviceFormat[f] instanceof YUVFormat) type = "YUV";
                if (deviceFormat[f] instanceof JPEGFormat) type = "JPG";

                Dimension size = ((VideoFormat) deviceFormat[f]).getSize();
                mImgSizes.addElement(type + " " + size.width + "x" + size.height);
                mCapDevices.addElement(deviceInfo);
                mCapFormats.addElement(deviceFormat[f]);
                mCapConverts.addElement(converterCodec);
                videoFormatMatch = true;
            }
        }

        if (!videoFormatMatch) {
            //
            // Nothing found!
            //
            System.err.println("No capture device found!");
            System.exit(3);
        }
    }

    private int[] parseWidthandHeight(String size) {
        int ret[] = { 0, 0};
        int pos_x = size.indexOf('x');
        String widthString = size.substring(4, pos_x);
        String heightString = size.substring(pos_x + 1, size.length());
        ret[0] = Integer.parseInt(widthString.trim());
        ret[1] = Integer.parseInt(heightString.trim());
        return ret;
    }

    private int selectLargestRGB() {
        int ret = -1;
        int pixels = 0;
        for (String size : mImgSizes) {
            if (size.indexOf("RGB") < 0) {
                continue;
            }
            int wh[] = parseWidthandHeight(size);
            int pixSize = wh[0] * wh[1];

            if (pixSize > pixels) {
                pixels = pixSize;
                ret = mImgSizes.indexOf(size);
            }
        }
        return ret;
    }

    public Codec fetchCodec(Format inFormat, Format outFormat) {
        Vector< ? > codecs = PlugInManager.getPlugInList(inFormat, outFormat,
                PlugInManager.CODEC);
        if (codecs == null) return null;

        for (int i = 0; i < codecs.size(); i++) {
            Codec codec = null;
            try {
                Class< ? > codecClass = Class.forName((String) codecs.elementAt(i));
                if (codecClass != null) codec = (Codec) codecClass.newInstance();
            } catch (Exception e) {
                continue;
            }
            if (codec == null || codec.setInputFormat(inFormat) == null) continue;

            Format[] outFormats = codec.getSupportedOutputFormats(inFormat);
            if (outFormats == null) continue;
            for (int j = 0; j < outFormats.length; j++) {
                if (outFormats[j].matches(outFormat)) {
                    try {
                        codec.setOutputFormat(outFormats[j]);
                        codec.open();
                        return codec;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Image getImage() {
        return mCurrentImage;
    }

    @Override
    public void addActionListener(ActionListener listener) {
        mListener = listener;
    }

    @Override
    public void transferData(PushBufferStream pbs) {
        try {
            if (mConverter == null) {
                pbs.read(mBuffer);
            } else {
                if (mBufferIn == null) {
                    mBufferIn = new Buffer();
                }
                pbs.read(mBufferIn);
                mConverter.process(mBufferIn, mBuffer);
            }

            if (mBuffer.isDiscard()) {
                mBuffer.setDiscard(false);
                return;
            }
            if (mBuffer.getData() == null) {
                return;
            }
            MemoryImageSource sourceImage = new MemoryImageSource(mWidth, mHeight,
                    mColorModel, (int[]) mBuffer.getData(), 0, mWidth);
            mCurrentImage = mToolkit.createImage(sourceImage);

            if (mListener != null) {
                mListener.actionPerformed(null);
            }

        } catch (Exception ignore) {
            // Ignore.
        }

    }
}
