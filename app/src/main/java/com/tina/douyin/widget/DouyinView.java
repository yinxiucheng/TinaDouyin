package com.tina.douyin.widget;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

/**
 * @author yxc
 * @date 2018/11/3
 */
public class DouyinView extends GLSurfaceView {

    //默认正常速度
    private Speed mSpeed = Speed.MODE_NORMAL;

    public enum Speed {
        MODE_EXTRA_SLOW, MODE_SLOW, MODE_NORMAL, MODE_FAST, MODE_EXTRA_FAST
    }


    DouyinRender mRender;
    public DouyinView(Context context) {
        this(context, null);
    }

    public DouyinView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //设置EGL版本
        setEGLContextClientVersion(2);
        mRender = new DouyinRender(this);
        setRenderer(mRender);
        //
        //设置按需渲染，当我们调用requestRender();请求GLThread回调一次onDrawFrame
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        mRender.onSurfaceDestroyed();
    }

    public void setSpeed(Speed speed){
        mSpeed = speed;
    }

    public void startRecord() {
        float speed = 1.f;
        switch (mSpeed) {
            case MODE_EXTRA_SLOW:
                speed = 0.3f;
                break;
            case MODE_SLOW:
                speed = 0.5f;
                break;
            case MODE_NORMAL:
                speed = 1.f;
                break;
            case MODE_FAST:
                speed = 1.5f;
                break;
            case MODE_EXTRA_FAST:
                speed = 3.f;
                break;
        }
        mRender.startRecord(speed);
    }


    public void stopRecord() {
        mRender.stopRecord();
    }
}
