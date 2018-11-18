package com.tina.douyin.video.widget;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.tina.douyin.video.codec.ISurface;
import com.tina.douyin.video.codec.VideoCodec;
import com.tina.douyin.video.filter.SoulFilter;

import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VideoView extends GLSurfaceView implements GLSurfaceView.Renderer, ISurface {
    private VideoCodec videoCodec;
    private int mWidth;
    private int mHeight;
    private int mFps;
    private LinkedList<byte[]> queue;
    private SoulFilter mSoulFilter;
    private long lastRenderTime;
    private int interval;

    public VideoView(Context context) {
        this(context,null);
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setRenderer(this);
        //主动刷新模式
        setRenderMode(RENDERMODE_CONTINUOUSLY);

        queue = new LinkedList<>();
        videoCodec = new VideoCodec();
        videoCodec.setDisplay(this);
    }


    public void setDataSource(String path){
        videoCodec.setDataSource(path);
    }

    /**
     * 开始解码
     */
    public void startPlay(){
        videoCodec.prepare();
        videoCodec.start();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mSoulFilter = new SoulFilter(getContext());
        mSoulFilter.onReady2(mWidth,mHeight,mFps);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);
    }

    /**
     * 16ms  vsync: 垂直同步信号
     *  如果视频达不到 60fps 那么就不对了 变成快进效果了
     *
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        //这一次渲染距离上一次的时间
        long diff = System.currentTimeMillis()-lastRenderTime;
        //如果不满足 fps算出的时间 就sleep
        long delay = interval - diff;
        if (delay > 0){
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //清理屏幕
        GLES20.glClearColor(0,0,0,0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //取出yuv数据
        byte[] yuv = poll();
        if (null != yuv){
            mSoulFilter.onDrawFrame(yuv);
        }
        lastRenderTime = System.currentTimeMillis();
    }

    /**
     * 加入队列
     * @param data
     */
    @Override
    public void offer(byte[] data) {
        synchronized (this){
            byte[] yuv = new byte[data.length];
            System.arraycopy(data,0,yuv,0,yuv.length);
            queue.offer(yuv);
        }
    }

    /**
     * 从队列取出
     * @return
     */
    @Override
    public byte[] poll() {
        synchronized (this){
            return queue.poll();
        }
    }

    /**
     * 获得视频参数
     * @param width
     * @param height
     * @param fps
     */
    @Override
    public void setVideoParamerters(int width, int height, int fps) {
        mWidth = width;
        mHeight = height;
        mFps = fps;
        interval = 1000/mFps;
       if (null != mSoulFilter){
           mSoulFilter.onReady2(mWidth,mHeight,mFps);
       }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        videoCodec.stop();
    }


}
