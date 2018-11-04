package com.tina.douyin.filiter;

import android.content.Context;
import android.opengl.GLES20;

import com.tina.douyin.R;
import com.tina.douyin.util.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11Ext;

/**
 * @author yxc
 * @date 2018/11/3
 */
public class ScreenFiliter extends AbstractFilter {

    public ScreenFiliter(Context context) {
        super(context, R.raw.base_vertex,R.raw.base_frag);
    }
}
