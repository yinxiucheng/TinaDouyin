package com.tina.douyin.camera.filiter;

import android.content.Context;

import com.tina.douyin.R;

/**
 * @author yxc
 * @date 2018/11/3
 */
public class ScreenFilter extends AbstractFilter {

    public ScreenFilter(Context context) {
        super(context, R.raw.base_vertex,R.raw.base_frag);
    }
}
