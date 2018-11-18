package com.tina.douyin.video.codec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author yxc
 * @date 2018/11/18
 */
public class VideoCodec {

    private ISurface mISurface;

    private String mPath;
    private MediaExtractor mMediaExtactor;

    private int mWidth;
    private int mHeight;
    private int mFbs;
    private MediaCodec mMediaCodec;
    private boolean isCodeing;
    private byte[] outData;
    private CodeTask mCodecTask;

    public void setDisplay(ISurface surface){
        mISurface = surface;
    }

    public void setDataSource(String path){
        mPath = path;
    }

    /**
     * 解封装
     * 准备方法
     */
    public void prepare(){
       mMediaExtactor = new MediaExtractor();

        try {
            mMediaExtactor.setDataSource(mPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int videoIndex = -1;
        MediaFormat videoMediaFormat = null;
        //mp4 1路音频 1路视频
        int trackCount = mMediaExtactor.getTrackCount();
        for (int i=0; i< trackCount; i++){
            //获得这路流的格式
            MediaFormat mediaFormat = mMediaExtactor.getTrackFormat(i);
            //选择视频
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")){
                videoIndex = i;
                videoMediaFormat = mediaFormat;
                break;
            }
        }


        //默认是-1
        if (null != videoMediaFormat){
            //解码videoIndex 这一路流
            mWidth = videoMediaFormat.getInteger(MediaFormat.KEY_WIDTH);
            mHeight = videoMediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
            mFbs= 20;//这里最好是与之前录屏时设置的一致
            if (videoMediaFormat.containsKey(MediaFormat.KEY_FRAME_RATE)){//帧率
                mFbs = videoMediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
            }
            // 个别手机  小米(x型号) 解码出来不是yuv420p
            //所以设置 解码数据格式 指定为yuv420
            videoMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.
                    CodecCapabilities.COLOR_FormatYUV420Planar);

            try {
                //创建一个解码器
                mMediaCodec = MediaCodec.createDecoderByType(videoMediaFormat.getString(MediaFormat.KEY_MIME));
                mMediaCodec.configure(videoMediaFormat, null, null, 0);//flag 确定是编码还是解码器

            } catch (IOException e) {
                e.printStackTrace();
            }
            //悬着流 后续读取这个流
            mMediaExtactor.selectTrack(videoIndex);
        }

        if (null != mISurface){
            mISurface.setVideoParamerters(mWidth, mHeight, mFbs);
        }
    }


    /**
     * 开始解码
     */
    public void start(){
        isCodeing = true;
        //接受，解码后的数据 yuv数据大小是 w*h*3/2
        outData = new byte[mWidth * mHeight * 3/2];
        mCodecTask = new CodeTask();
        mCodecTask.start();
    }


    /**
     * 停止
     */
    public void stop(){
        isCodeing = false;
        if (null != mCodecTask && mCodecTask.isAlive()){
            try {
                mCodecTask.join(3_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //3s后线程还没有结束
            if (mCodecTask.isAlive()){
                mCodecTask.interrupt();
            }
            mCodecTask = null;
        }
    }

    /**
     * 解码线程
     */
    private class CodeTask extends Thread{

        @Override
        public void run() {
            if (null == mMediaCodec){
                return;
            }
            //开启
            mMediaCodec.start();
            boolean isEOF = false;
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            while (!isInterrupted()){
                if (!isCodeing){
                    break;
                }
                //
                if (!isEOF){
                    isEOF = putBuffer2Codec();
                }
                //从输出缓冲区
                int status = mMediaCodec.dequeueOutputBuffer(bufferInfo, 100);

                if(status >= 0){
                   ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(status);

                   if (bufferInfo.size == outData.length){
                       outputBuffer.get(outData);
                       if (null != mISurface){
                           mISurface.offer(outData);
                       }
                   }
                   //释放输出缓冲区
                   mMediaCodec.releaseOutputBuffer(status, false);
                }

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
                    //解码完了
                    break;
                }

            }

            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
            mMediaExtactor.release();
            mMediaExtactor = null;
        }

        /**
         *
         * @return
         */
        private boolean putBuffer2Codec(){
            //-1 就一直等待
            int status = mMediaCodec.dequeueInputBuffer(100);
            //有效的输入缓冲区 index
            if (status >= 0){
                ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(status);
                inputBuffer.clear();
                //ByteBuffer当成byte数组，读取数据ByteBuffer存到byte数组的第0个开始存
                int size = mMediaExtactor.readSampleData(inputBuffer, 0);
                //没读到数据， 已经没有数据可读了
                if (size < 0){
                    mMediaCodec.queueInputBuffer(status, 0, 0, 0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM);//flag 表示没有更多数据从输出缓冲区读取，解码完了。
                    return true;
                }else {
                    //把塞了数据的输入缓冲区塞回去
                    mMediaCodec.queueInputBuffer(status, 0, size, mMediaExtactor.getSampleTime(), 0);
                    //丢掉已经加入到解码的数据（）
                    mMediaExtactor.advance();
                }
            }
            return false;
        }
    }


}
