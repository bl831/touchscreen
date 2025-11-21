package gov.lbl.als.bl831.video;

import java.util.Arrays;
import java.util.Objects;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.BytePointer;

import io.tetrah.camerainfo.v4l2.ioctl.V4L2Ioctl;

public class V4L2AVUtils {

    // Helper class to represent table entries
    private static class FmtMap {
        int avPixFmt;
        int avCodecId;
        int v4l2Fmt;

        FmtMap(int avPixFmt, int avCodecId, int v4l2Fmt) {
            this.avPixFmt = avPixFmt;
            this.avCodecId = avCodecId;
            this.v4l2Fmt = v4l2Fmt;
        }
    }

    // Full table based on FFmpeg's ff_fmt_conversion_table (add more if needed)
    // Source: https://github.com/FFmpeg/FFmpeg/blob/master/libavdevice/v4l2-common.c
    private static final FmtMap[] CONVERSION_TABLE = {
        new FmtMap(avutil.AV_PIX_FMT_YUV420P, avcodec.AV_CODEC_ID_RAWVIDEO, 0x32315559),  // V4L2_PIX_FMT_YUV420 ('Y','U','V',' ')
        new FmtMap(avutil.AV_PIX_FMT_YUV420P, avcodec.AV_CODEC_ID_RAWVIDEO, 0x32315659),  // V4L2_PIX_FMT_YVU420 ('Y','V','U',' ')
        new FmtMap(avutil.AV_PIX_FMT_YUV422P, avcodec.AV_CODEC_ID_RAWVIDEO, 0x50323234),  // V4L2_PIX_FMT_YUV422P
        new FmtMap(avutil.AV_PIX_FMT_YUYV422, avcodec.AV_CODEC_ID_RAWVIDEO, 0x56595559),  // V4L2_PIX_FMT_YUYV ('Y','U','Y','V')
        new FmtMap(avutil.AV_PIX_FMT_UYVY422, avcodec.AV_CODEC_ID_RAWVIDEO, 0x59565955),  // V4L2_PIX_FMT_UYVY ('U','Y','V','Y')
        new FmtMap(avutil.AV_PIX_FMT_YUV411P, avcodec.AV_CODEC_ID_RAWVIDEO, 0x50313134),  // V4L2_PIX_FMT_YUV411P
        new FmtMap(avutil.AV_PIX_FMT_YUV410P, avcodec.AV_CODEC_ID_RAWVIDEO, 0x50303134),  // V4L2_PIX_FMT_YUV410
        new FmtMap(avutil.AV_PIX_FMT_RGB555LE, avcodec.AV_CODEC_ID_RAWVIDEO, 0x35395042),  // V4L2_PIX_FMT_RGB555
        new FmtMap(avutil.AV_PIX_FMT_RGB555BE, avcodec.AV_CODEC_ID_RAWVIDEO, 0x58425052),  // V4L2_PIX_FMT_RGB555X
        new FmtMap(avutil.AV_PIX_FMT_BGR565LE, avcodec.AV_CODEC_ID_RAWVIDEO, 0x36314742),  // V4L2_PIX_FMT_BGR565
        new FmtMap(avutil.AV_PIX_FMT_BGR565BE, avcodec.AV_CODEC_ID_RAWVIDEO, 0x58474236),  // V4L2_PIX_FMT_BGR565X
        new FmtMap(avutil.AV_PIX_FMT_RGB565LE, avcodec.AV_CODEC_ID_RAWVIDEO, 0x36314752),  // V4L2_PIX_FMT_RGB565
        new FmtMap(avutil.AV_PIX_FMT_RGB565BE, avcodec.AV_CODEC_ID_RAWVIDEO, 0x58475236),  // V4L2_PIX_FMT_RGB565X
        new FmtMap(avutil.AV_PIX_FMT_BGR24, avcodec.AV_CODEC_ID_RAWVIDEO, 0x34524742),  // V4L2_PIX_FMT_BGR24
        new FmtMap(avutil.AV_PIX_FMT_RGB24, avcodec.AV_CODEC_ID_RAWVIDEO, 0x33424752),  // V4L2_PIX_FMT_RGB24
        new FmtMap(avutil.AV_PIX_FMT_BGR0, avcodec.AV_CODEC_ID_RAWVIDEO, 0x34324742),  // V4L2_PIX_FMT_BGR32
        new FmtMap(avutil.AV_PIX_FMT_0BGR, avcodec.AV_CODEC_ID_RAWVIDEO, 0x34324752),  // V4L2_PIX_FMT_RGB32
        new FmtMap(avutil.AV_PIX_FMT_GRAY8, avcodec.AV_CODEC_ID_RAWVIDEO, 0x59455247),  // V4L2_PIX_FMT_GREY
        new FmtMap(avutil.AV_PIX_FMT_NV12, avcodec.AV_CODEC_ID_RAWVIDEO, 0x3231564E),  // V4L2_PIX_FMT_NV12
        new FmtMap(avutil.AV_PIX_FMT_NV21, avcodec.AV_CODEC_ID_RAWVIDEO, 0x3132564E),  // V4L2_PIX_FMT_NV21
        new FmtMap(avutil.AV_PIX_FMT_BGR0, avcodec.AV_CODEC_ID_RAWVIDEO, 0x34324258),  // V4L2_PIX_FMT_XBGR32
        new FmtMap(avutil.AV_PIX_FMT_0RGB, avcodec.AV_CODEC_ID_RAWVIDEO, 0x34325258),  // V4L2_PIX_FMT_XRGB32
        new FmtMap(avutil.AV_PIX_FMT_BGRA, avcodec.AV_CODEC_ID_RAWVIDEO, 0x34324241),  // V4L2_PIX_FMT_ABGR32
        new FmtMap(avutil.AV_PIX_FMT_ARGB, avcodec.AV_CODEC_ID_RAWVIDEO, 0x34325241),  // V4L2_PIX_FMT_ARGB32
        // Compressed formats (AV_PIX_FMT_NONE)
        new FmtMap(avutil.AV_PIX_FMT_NONE, avcodec.AV_CODEC_ID_MJPEG, 0x47504A4D),  // V4L2_PIX_FMT_MJPEG ('M','J','P','G')
        new FmtMap(avutil.AV_PIX_FMT_NONE, avcodec.AV_CODEC_ID_MJPEG, 0x4745504A),  // V4L2_PIX_FMT_JPEG
        new FmtMap(avutil.AV_PIX_FMT_NONE, avcodec.AV_CODEC_ID_H264, 0x34363248),  // V4L2_PIX_FMT_H264
        new FmtMap(avutil.AV_PIX_FMT_NONE, avcodec.AV_CODEC_ID_H263, 0x33363248),  // V4L2_PIX_FMT_H263
        new FmtMap(avutil.AV_PIX_FMT_NONE, avcodec.AV_CODEC_ID_MPEG1VIDEO, 0x3147504D),  // V4L2_PIX_FMT_MPEG1
        new FmtMap(avutil.AV_PIX_FMT_NONE, avcodec.AV_CODEC_ID_MPEG2VIDEO, 0x3247504D),  // V4L2_PIX_FMT_MPEG2
        new FmtMap(avutil.AV_PIX_FMT_NONE, avcodec.AV_CODEC_ID_MPEG4, 0x3447504D),  // V4L2_PIX_FMT_MPEG4
        new FmtMap(avutil.AV_PIX_FMT_NONE, avcodec.AV_CODEC_ID_H264, 0x44495658),  // V4L2_PIX_FMT_XVID
        new FmtMap(avutil.AV_PIX_FMT_NONE, avcodec.AV_CODEC_ID_VC1, 0x31435657),  // V4L2_PIX_FMT_VC1_ANNEX_G
        new FmtMap(avutil.AV_PIX_FMT_NONE, avcodec.AV_CODEC_ID_VP8, 0x30385056),  // V4L2_PIX_FMT_VP8
        new FmtMap(avutil.AV_PIX_FMT_NONE, avcodec.AV_CODEC_ID_VP9, 0x30395056),  // V4L2_PIX_FMT_VP9
        new FmtMap(avutil.AV_PIX_FMT_NONE, avcodec.AV_CODEC_ID_HEVC, 0x43564548),  // V4L2_PIX_FMT_HEVC
    };

    /**
     * Gets the AVCodecID from a V4L2 format.
     * 
     * @param v4l2Fmt The V4L2 format (FOURCC code)
     * @return The corresponding AVCodecID
     */
    public static int getAvCodecId(int v4l2Fmt) {
        for (FmtMap entry : CONVERSION_TABLE) {
            if (entry.v4l2Fmt == v4l2Fmt) {
                return entry.avCodecId;
            }
        }
        return avcodec.AV_CODEC_ID_NONE;
    }

    /**
     * Gets the AVPixelFormat from a V4L2 format and codec ID.
     * 
     * @param v4l2Fmt The V4L2 format (FOURCC code)
     * @param codecId The codec ID
     * @return The corresponding AVPixelFormat
     */
    public static int getAvPixelFormat(int v4l2Fmt, int codecId) {
        for (FmtMap entry : CONVERSION_TABLE) {
            if (entry.v4l2Fmt == v4l2Fmt && entry.avCodecId == codecId) {
                return entry.avPixFmt;
            }
        }
        return avutil.AV_PIX_FMT_NONE;
    }

    /**
     * Converts a V4L2 pixel format to its AV_PIX_FMT_* string representation.
     * 
     * @param v4l2Fmt The V4L2 format (FOURCC code)
     * @return The string representation of the AV_PIX_FMT, or null if not found
     */
    public static String v4l2PixFmt2AvPixFmtString(int v4l2Fmt) {
        int codecId = getAvCodecId(v4l2Fmt);
        int avPixFmt = getAvPixelFormat(v4l2Fmt, codecId);
        BytePointer fmtName = avutil.av_get_pix_fmt_name(avPixFmt);
        return Objects.isNull(fmtName) || fmtName.isNull() ? null : fmtName.getString();
    }

    /**
     * Converts a V4L2 pixel format string to its AV_PIX_FMT_* string representation.
     * 
     * @param v4l2FourccFmt A 4-character string representing the V4L2 format
     * @return The string representation of the AV_PIX_FMT, or null if not found
     */
    public static String v4l2PixFmt2AvPixFmtString(String v4l2FourccFmt) {
        return v4l2PixFmt2AvPixFmtString(V4L2Ioctl.fourcc(v4l2FourccFmt));
    }

    /**
     * Converts a V4L2 pixel format to its AV_CODEC_* string representation.
     * 
     * @param v4l2Fmt The V4L2 format (FOURCC code)
     * @return The string representation of the AV_CODEC, or null if not found
     */
    public static String v4l2PixFmt2AvCodecString(int v4l2Fmt) {
        int codecId = getAvCodecId(v4l2Fmt);
        BytePointer codecName = avcodec.avcodec_get_name(codecId);
        return Objects.isNull(codecName) || codecName.isNull() ? null : codecName.getString();
    }

    /**
     * Converts a V4L2 pixel format string to its AV_CODEC_* string representation.
     * 
     * @param v4l2FourccFmt A 4-character string representing the V4L2 format
     * @return The string representation of the AV_CODEC, or null if not found
     */
    public static String v4l2PixFmt2AvCodecString(String v4l2FourccFmt) {
        return v4l2PixFmt2AvCodecString(V4L2Ioctl.fourcc(v4l2FourccFmt));
    }

    // Example usage: Convert V4L2 pix_fmt int to AV_PIX_FMT_*
    public static void main(String[] args) {

        Arrays.asList("MJPG", "YUYV", "UYVY", "NV12").forEach((s) -> {
            // int v4l2Fmt = 0x47504A4D;  // Example: V4L2_PIX_FMT_MJPEG ('M','J','P','G')
            int v4l2Fmt = V4L2Ioctl.fourcc(s);
            int codecId = getAvCodecId(v4l2Fmt);
            int avPixFmt = getAvPixelFormat(v4l2Fmt, codecId);
            System.out.println("FOURCC: " + s);  // AV_CODEC_ID_MJPEG
            System.out.println("AV_CODEC_ID: " + codecId);  // AV_CODEC_ID_MJPEG
            System.out.println("AV_PIX_FMT: " + avPixFmt);  // AV_PIX_FMT_NONE (expected for compressed)
            System.out.printf("AV_CODEC_NAME: %s%n", v4l2PixFmt2AvCodecString(v4l2Fmt));
            System.out.printf("AV_PIX_FMT_NAME: %s%n", v4l2PixFmt2AvPixFmtString(v4l2Fmt));
        });
    }
}