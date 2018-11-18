package com.tina.douyin.video.codec;

/**
 * @author yxc
 * @date 2018/11/18
 */
public interface ISurface {

    void offer(byte[] data);

    byte[] poll();

    void setVideoParamerters(int width, int height, int fps);


}
