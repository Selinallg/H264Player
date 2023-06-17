package com.llg.h264player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Range;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    private static final String CODE_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;

    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

        }
        return false;
    }

    H264Player2 h264Player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        initSurface();
        //checkMediaCodec();
//        dsp  1   图形  视频 一个专用芯片  cpu  2
//        MediaPlayer mediaPlayer = new MediaPlayer();
//        try {
//            mediaPlayer.setDataSource("input.mp4");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    private void checkMediaCodec() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaCodecList list = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
            MediaCodecInfo[] supportCodes = list.getCodecInfos();
            Log.i(TAG, "解码器列表：");
            for (MediaCodecInfo codec : supportCodes) {
                if (!codec.isEncoder()) {
                    String name = codec.getName();
                    if (name.startsWith("OMX.google")) {
                        Log.i(TAG, "软解->" + name);
                        String[] supportedTypes = codec.getSupportedTypes();
                        for (int i = 0; i < supportedTypes.length; i++) {
                            Log.d(TAG, "软解-> checkMediaCodec: supportedTypes=" + supportedTypes[i]);
                        }
                        Log.d(TAG, " ---------------");
                    }
                }
            }
            for (MediaCodecInfo codec : supportCodes) {
                if (!codec.isEncoder()) {
                    String name = codec.getName();
                    String[] supportedTypes = codec.getSupportedTypes();
                    for (int i = 0; i < supportedTypes.length; i++) {
                        //Log.d(TAG, "checkMediaCodec: supportedTypes=" + supportedTypes[i]);

                        Log.d(TAG, "硬解-> checkMediaCodec: supportedTypes=" + supportedTypes[i]);
                        Log.d(TAG, " ---------------");

                        if (supportedTypes[i].equals(CODE_TYPE)) {
                            MediaCodecInfo.CodecCapabilities dd = codec.getCapabilitiesForType(CODE_TYPE);  // "video/avc";
                            if (dd != null) {

                                MediaCodecInfo.VideoCapabilities videoCapabilities = dd.getVideoCapabilities();
                                Range<Integer> supportedHeights = videoCapabilities.getSupportedHeights();
                                Range<Integer> supportedWidths = videoCapabilities.getSupportedWidths();
                                Log.d(TAG, "checkMediaCodec: supportedHeights=" + supportedHeights.getLower() + "--" + supportedHeights.getUpper());
                                Log.d(TAG, "checkMediaCodec: supportedWidths=" + supportedWidths.getLower() + "--" + supportedWidths.getUpper());

                                Range<Integer> supportedHeightsFor3840 = videoCapabilities.getSupportedHeightsFor(3840);
                                Range<Integer> supportedWidthsFor3840 = videoCapabilities.getSupportedWidthsFor(3840);
                                Log.d(TAG, "checkMediaCodec: supportedHeightsFor3840 ==>" + supportedHeightsFor3840);
                                Log.d(TAG, "checkMediaCodec: supportedWidthsFor3840 ==>" + supportedWidthsFor3840);

                                boolean sizeSupported = videoCapabilities.isSizeSupported(3840, 3840);
                                Log.d(TAG, "checkMediaCodec:  3840 * 3840 sizeSupported=" + sizeSupported);
                                boolean sizeSupported2 = videoCapabilities.isSizeSupported(1920, 3840);
                                Log.d(TAG, "checkMediaCodec:  1920 * 3840 sizeSupported2=" + sizeSupported2);

                                boolean sizeSupported3 = videoCapabilities.isSizeSupported(3840, 1920);
                                Log.d(TAG, "checkMediaCodec:  3840  * 1920 sizeSupported3=" + sizeSupported3);
                            }
                        }
                    }
                    if (!name.startsWith("OMX.google")) {
                        Log.i(TAG, "硬解->" + name);
                    }
                }
            }
            Log.i(TAG, "编码器列表：");
            for (MediaCodecInfo codec : supportCodes) {
                if (codec.isEncoder()) {
                    String name = codec.getName();
                    if (name.startsWith("OMX.google")) {
                        Log.i(TAG, "软编->" + name);
                    }
                }
            }
            for (MediaCodecInfo codec : supportCodes) {
                if (codec.isEncoder()) {
                    String name = codec.getName();
                    if (!name.startsWith("OMX.google")) {
                        Log.i(TAG, "硬编->" + name);
                    }
                }
            }
        }
    }

    private void initSurface() {
        SurfaceView surface = (SurfaceView) findViewById(R.id.preview);
        final SurfaceHolder surfaceHolder = surface.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                h264Player = new H264Player2(MainActivity.this,
                        new File(Environment.getExternalStorageDirectory(),"265.ts" ).getAbsolutePath(),
//                        new File(Environment.getExternalStorageDirectory()+"/cybervr/test/","264.ts" ).getAbsolutePath(),
//                        new File(Environment.getExternalStorageDirectory(), "4k_output_file_video.h264").getAbsolutePath(),
//                        new File(Environment.getExternalStorageDirectory(), "output.h265").getAbsolutePath(),
                        surfaceHolder.getSurface());
                h264Player.play();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });

    }

}