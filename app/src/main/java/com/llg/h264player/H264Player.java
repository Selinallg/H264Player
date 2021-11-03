package com.llg.h264player;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * 同步解码
 */
public class H264Player implements Runnable {
    private static final String TAG = "H264Player";
    private Context context;

    private String path;
//mediaCodec   手机硬件不一样    dsp  芯片  不一样
    //    解码H264  解压     android 硬编  兼容   dsp  1ms   7000k码率   700k码率    4k   8k
//    码率  直接奔溃 联发科  ----》     音频
    private MediaCodec mediaCodec;
//画面
    private Surface surface;

    public H264Player(Context context,String path, Surface surface) {

        this.surface = surface;
        this.path = path;
        this.context = context;

        try {
//            h265  --ISO hevc  兼容 硬编   不兼容   电视    -----》8k  4K
            try {
                mediaCodec = MediaCodec.createDecoderByType("video/avc");
            } catch (Exception e) {
//                不支持硬编
            }

//            MediaFormat mediaformat = MediaFormat.createVideoFormat("video/avc", 368, 384);
            MediaFormat mediaformat = MediaFormat.createVideoFormat("video/avc", 2880, 1600);
//            MediaFormat mediaformat = MediaFormat.createVideoFormat("video/avc", 4320, 2160);
//            MediaFormat mediaformat = MediaFormat.createVideoFormat("video/avc", 2160, 4320);

//            MediaFormat mediaformat = MediaFormat.createVideoFormat("video/avc", 368, 384);
//            mediaformat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            mediaCodec.configure(mediaformat, surface, null, 0);
        } catch ( Exception e) {
            e.printStackTrace();
        }
    }
//MediaExtractor  视频      画面H264
    public void play() {
        mediaCodec.start();





















        //java线程本质是什么线程         linux线程






        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            decodeH264();
        } catch (Exception e) {
            Log.e(TAG, "run: "+e);
        }
    }

    long lastTimeMillis;
    private void decodeH264() {
        byte[] bytes = null;
        try {
//            偷懒   文件  加载内存     文件 1G  1G
            bytes = getBytes(path);
        } catch ( Exception e) {
            e.printStackTrace();
        }
//内部的队列     不是每一个都可以用
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();

//
        int startIndex = 0;
//总字节数
        int totalSize = bytes.length;
        while (true) {
            if (totalSize == 0 ||startIndex >= totalSize) {
                break;
            }
//            寻找索引
            int nextFrameStart =   findByFrame(bytes, startIndex+2, totalSize);

            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
//            查询哪一个bytebuffer能够用
            int inIndex =   mediaCodec.dequeueInputBuffer(10000);
            if (inIndex >= 0) {
//                Log.d(TAG, "decodeH264: -------------------inIndex---------"+inIndex);
//            找到了  david
                ByteBuffer byteBuffer = inputBuffers[inIndex];
                byteBuffer.clear();
                byteBuffer.put(bytes, startIndex, nextFrameStart - startIndex);
//
                mediaCodec.queueInputBuffer(inIndex, 0, nextFrameStart - startIndex, 0, 0);
                startIndex = nextFrameStart;
            }else {
                continue;
            }

//            得到数据
           int outIndex= mediaCodec.dequeueOutputBuffer(info, 10000);

//            Log.d(TAG, "decodeH264:===outIndex= "+ outIndex);
            
//音视频   裁剪一段 true  1    false   2
            if (outIndex >= 0) {
//                try {
//                    Thread.sleep(33);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                long currentTimeMillis = System.currentTimeMillis();
                long useTime = currentTimeMillis - lastTimeMillis;
                Log.d(TAG, "decodeH264: useTime="+useTime);
                lastTimeMillis = currentTimeMillis;

                mediaCodec.releaseOutputBuffer(outIndex, true);
            }else {
//视频同步  不能  做到  1ms    60ms 差异   3600ms
            }

        }

    }

    private int findByFrame( byte[] bytes, int start, int totalSize) {

        int j = 0;
        for (int i = start; i < totalSize-4; i++) {
            if (bytes[i] == 0x00 && bytes[i + 1] == 0x00 && bytes[i + 2] == 0x00 && bytes[i + 3] == 0x01) {
                return i;
            }

        }
        return -1;
    }
    public   byte[] getBytes(String path) throws IOException {
        InputStream is =   new DataInputStream(new FileInputStream(new File(path)));
        int len;
        int size = 1024;
        byte[] buf;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        buf = new byte[size];
        while ((len = is.read(buf, 0, size)) != -1)
            bos.write(buf, 0, len);
        buf = bos.toByteArray();
        return buf;
    }
}
