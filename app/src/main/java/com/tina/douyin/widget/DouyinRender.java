package com.tina.douyin.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.tina.douyin.face.Face;
import com.tina.douyin.face.FaceTrack;
import com.tina.douyin.filiter.BigEyeFilter;
import com.tina.douyin.filiter.CameraFilter;
import com.tina.douyin.filiter.ScreenFiliter;
import com.tina.douyin.filiter.StickFilter;
import com.tina.douyin.record.MediaRecorder;
import com.tina.douyin.util.CameraHelper;
import com.tina.douyin.util.OpenGLUtils;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 渲染器
 *
 * @author yxc
 * @date 2018/11/3
 */
public class DouyinRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener, Camera.PreviewCallback {

    CameraHelper mCameraHelper;

    SurfaceTexture mSurfaceTexture;

    DouyinView mDouyinView;

    int[] mTextures;

    //变换矩阵
    private float[] mtx = new float[16];

    private ScreenFiliter mScreenFiliter;
    private CameraFilter mCameraFiliter;
    private BigEyeFilter mBigEyeFilter;
    private StickFilter mStickFilter;
    private MediaRecorder mMediaRecorder;
    private FaceTrack mFaceTrack;

    public DouyinRender(DouyinView douyinView){
        this.mDouyinView = douyinView;

        Context context = douyinView.getContext();

        //拷贝 模型
        OpenGLUtils.copyAssets2SdCard(context, "lbpcascade_frontalface.xml",
                "/sdcard/lbpcascade_frontalface.xml");
        OpenGLUtils.copyAssets2SdCard(context, "seeta_fa_v1.1.bin",
                "/sdcard/seeta_fa_v1.1.bin");
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
        mCameraHelper = new CameraHelper(Camera.CameraInfo.CAMERA_FACING_FRONT);
        mCameraHelper.setPreviewCallback(this);

        //准备好画布
        mTextures = new int[1];
        //这里创建了纹理，直接应用了，没有配置。
        GLES20.glGenTextures(mTextures.length, mTextures, 0);
        mSurfaceTexture = new SurfaceTexture(mTextures[0]);

        mSurfaceTexture.setOnFrameAvailableListener(this);

        //注意，必须在Gl线程中创建文件
        mCameraFiliter = new CameraFilter(mDouyinView.getContext());
        mBigEyeFilter = new BigEyeFilter(mDouyinView.getContext());
        mScreenFiliter = new ScreenFiliter(mDouyinView.getContext());
        mStickFilter = new StickFilter(mDouyinView.getContext());

        //渲染线程的上下文，需要给到自己的EGL环境下作为share_context
        EGLContext eglContext = EGL14.eglGetCurrentContext();
        mMediaRecorder = new MediaRecorder(mDouyinView.getContext(), "/sdcard/a.mp4", CameraHelper.HEIGHT, CameraHelper.WIDTH, eglContext);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 创建跟踪器
        mFaceTrack = new FaceTrack("/sdcard/lbpcascade_frontalface.xml",
                "/sdcard/seeta_fa_v1.1.bin", mCameraHelper);
        //启动跟踪器
        mFaceTrack.startTrack();

        //开启预览
        mCameraHelper.startPreview(mSurfaceTexture);
        mCameraFiliter.onReady(width, height);
        mBigEyeFilter.onReady(width, height);
        mStickFilter.onReady(width, height);
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

        //进行画画，设置变换矩阵。
        mCameraFiliter.setMatrix(mtx);
        //返回处理后的纹理id
        int id = mCameraFiliter.onDrawFrame(mTextures[0]);

        //加效果滤镜
        // id  = 效果1.onDrawFrame(id);
        // id = 效果2.onDrawFrame(id);
        //......
        Face face = mFaceTrack.getFace();
        if (null != face) {
            Log.e("face", face.toString());
        }
        mBigEyeFilter.setFace(face);
        id = mBigEyeFilter.onDrawFrame(id);

        mStickFilter.setFace(face);
        id = mStickFilter.onDrawFrame(id);
        //加完之后显示到屏幕上去
        mScreenFiliter.onDrawFrame(id);
        //录制
        mMediaRecorder.encodeFrame(id, mSurfaceTexture.getTimestamp());
    }

    //有一个新的有效的图片的时候调用，让它调用onDrawFrame方法，通过GLSurfaceview的 requestRender()
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //有数据时调用，省资源省电，然后调用 onDrawFrame，构成循环。
        mDouyinView.requestRender();
    }

    public void onSurfaceDestroyed() {
        mCameraHelper.stopPreview();
        //关闭跟踪器
        mFaceTrack.stopTrack();
    }

    public void startRecord(float speed) {
        try {
            mMediaRecorder.start(speed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord() {
        mMediaRecorder.stop();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //data 送去跟踪器检测人脸 与 关键点定位。 这个detector很耗时，开线程
        mFaceTrack.detector(data);
    }
}
