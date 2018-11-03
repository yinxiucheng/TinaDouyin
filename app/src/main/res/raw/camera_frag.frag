#extension GL_OES_EGL_image_external : require
//SurfaceTexture比较特殊,需要声明一下

//float 数据是什么精度的， 高、中、低三挡
precision mediump float;

//采用点坐标
varying vec2 aCoord;//从顶点着色器拿过来的

//采样器
uniform samplerExternalOES vTexture;

void main() {
    //变量 接受像素值 vec4(1.0, 0.0, 0.0, 0.0);//红色
    //赋值给 gl_FragColor 就可以了
	gl_FragColor = texture2D(vTexture);
}