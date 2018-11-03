package com.tina.douyin.widget;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * @author yxc
 * @date 2018/11/3
 */
public class DouyinView extends GLSurfaceView {

    public DouyinView(Context context) {
        this(context, null);
    }

    public DouyinView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEGLContextClientVersion(2);
        setRenderer(new DouyinRender(this));

        //
        //设置按需渲染，当我们调用requestRender();请求GLThread回调一次onDrawFrame
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }



}
