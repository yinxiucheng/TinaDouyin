package com.tina.douyin.widget;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.tina.douyin.filiter.CameraFilter;
import com.tina.douyin.filiter.ScreenFiliter;
import com.tina.douyin.util.CameraHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 渲染器
 *
 * @author yxc
 * @date 2018/11/3
 */
public class DouyinRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    CameraHelper mCameraHelper;

    SurfaceTexture mSurfaceTexture;

    DouyinView mDouyinView;

    int[] mTextures;

    //变换矩阵
    private float[] mtx = new float[16];

    private ScreenFiliter mScreenFiliter;
    private CameraFilter mCameraFiliter;


    public DouyinRender(DouyinView douyinView){
        this.mDouyinView = douyinView;
    }

    /**
     * 创建好渲染器
     *
     * @param gl
     * @param config
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //初始化操作
        mCameraHelper = new CameraHelper(Camera.CameraInfo.CAMERA_FACING_BACK);

        //准备好画布
        mTextures = new int[1];
        //这里创建了纹理，直接应用了，没有配置。
        GLES20.glGenTextures(mTextures.length, mTextures, 0);

        mSurfaceTexture = new SurfaceTexture(mTextures[0]);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        //注意
        mCameraFiliter = new CameraFilter(mDouyinView.getContext());
        mScreenFiliter = new ScreenFiliter(mDouyinView.getContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        //开启预览
        mCameraHelper.startPreview(mSurfaceTexture);
        mCameraFiliter.onReady(width, height);
        mScreenFiliter.onReady(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //配置屏幕

        //清理屏幕, 告诉opengl需要把屏幕清理成什么颜色
        GLES20.glClearColor(0, 0, 0, 0);
        //执行上一个：glClearColor配置的屏幕颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //把摄像头的数据显示出来
        //更新纹理，然后我们才能使用opengl从surfaceTexture当中获得数据进行渲染
        mSurfaceTexture.updateTexImage();

        //SurfaceTexture比较特殊， 采用的是sampleExtension（而不是用的Sample2）
        //获得变换矩阵， 变换矩阵是一个 4 * 4 的矩阵
        mSurfaceTexture.getTransformMatrix(mtx);

        //进行画画
        mCameraFiliter.setMatrix(mtx);
        //返回处理后的纹理id
        int id = mCameraFiliter.onDrawFrame(mTextures[0]);
        //加效果滤镜
        //......
        //加完之后显示到屏幕上去
        mScreenFiliter.onDrawFrame(id);
    }


    //有一个新的有效的图片的时候调用，让它调用onDrawFrame方法，通过GLSurfaceview的 requestRender()
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //有数据时调用，省资源省电，然后调用 onDrawFrame，构成循环。
        mDouyinView.requestRender();
    }

}
