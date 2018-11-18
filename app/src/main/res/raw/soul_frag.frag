//SurfaceTexture比较特殊
//float数据是什么精度的
precision mediump float;

//采样点的坐标
varying vec2 aCoord;

//透明度
uniform float alpha;

//yuv 这一次显示RGBA
uniform sampler2D sampler_y;
uniform sampler2D sampler_u;
uniform sampler2D sampler_v;

void main(){
    //4个float数据，y/u/v保存在向量中的第一个
    float y = texture2D(sampler_y, aCoord).r;
    float u = texture2D(sampler_u, aCoord).r - 0.5;
    float v = texture2D(sampler_v, aCoord).r - 0.5;
    //公式
    //R = Y + 1.402 (v-128)
    //G = Y - 0.34414(u - 128) - 0.71414(v - 128)
    //B = Y + 1.772(u - 128)

    vec3 rgb;
    //u - 128
    //1. glsl中 不能直接将int于float进行计算
    //2. rgba取值都是：0 — 1（128对应 0~255 归一化 128对应 0.5）
    rgb.r = y + 1.402 * v;
    rgb.g = y - 0.34414 * u - 0.71414 * v;
    rgb.b = y + 1.772 * u;
    //rgba
    gl_FragColor = vec4(rgb, alpha);
}