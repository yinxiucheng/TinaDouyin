attribute vec4 vPosition; //把顶点坐标给这个变量， 确定要画画的形状

attribute vec4 vCoord;//接受纹理坐标，接受采样器采样图片的坐标， 这个在顶点着色器没有用，传给片元着色器

uniform mat4 vMatrix; //变换矩阵， 需要将原本的 vCood（01， 11， 00， 10）与矩阵相乘才能够得到surfacetexture的采样坐标

varying vec2 aCoord;//传给片云着色器 像素点()

void main() {
    //gl_Position 为内置变量，我们把订点数据给这个变量，opengl就知道它要画什么形状了。
	gl_Position = vPosition;

	aCoord = (vMatrix * vCoord).xy;
	//aCoord =  vec2((vMatrix * vCoord).x,(vMatrix * vCoord).y);
}
