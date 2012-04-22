package de.swagner.piratesbigsea.shader;

public class WaterShaderFog {

    public static final String mVertexShader =
    		"attribute vec4 a_position;\n" +
    		"uniform mat4 VPMatrix;\n"+
        	"uniform mat4 MMatrix;\n"+
	        "uniform float waterHeight;\n" +
	        "uniform float time;\n" +
	        "uniform int numWaves;\n" +
	        "uniform float amplitude[8];\n" +
	        "uniform float wavelength[8];\n" +
	        "uniform float speed[8];\n" +
	        "uniform vec2 direction[8];\n" +
	        "varying vec3 position;\n" +
	        "varying vec3 worldNormal;\n" +
	        "varying vec3 eyeNormal;\n" +
	        "float wave(int i, float x, float y) {\n" +
	        "    float frequency = 2.0*3.14159/wavelength[i];\n" +
	        "    float phase = speed[i] * frequency;\n" +
	        "    float theta = dot(direction[i], vec2(x, y));\n" +
	        "    return amplitude[i] * sin(theta * frequency + time * phase);\n" +
	        "}\n" +
	        "float waveHeight(float x, float y) {\n" +
	        "    float height = 0.0;\n" +
	        "    for (int i = 0; i < numWaves; ++i)\n" +
	        "        height += wave(i, x, y);\n" +
	        "    return height;\n" +
	        "}\n" +
	        "float dWavedx(int i, float x, float y) {\n" +
	        "    float frequency = 2.0*3.14159/wavelength[i];\n" +
	        "    float phase = speed[i] * frequency;\n" +
	        "    float theta = dot(direction[i], vec2(x, y));\n" +
	        "    float A = amplitude[i] * direction[i].x * frequency;\n" +
	        "    return A * cos(theta * frequency + time * phase);\n" +
	        "}\n" +
	        "float dWavedy(int i, float x, float y) {\n" +
	        "    float frequency = 2.0*3.14159/wavelength[i];\n" +
	        "    float phase = speed[i] * frequency;\n" +
	        "    float theta = dot(direction[i], vec2(x, y));\n" +
	        "    float A = amplitude[i] * direction[i].y * frequency;\n" +
	        "    return A * cos(theta * frequency + time * phase);\n" +
	        "}\n" +
	        "vec3 waveNormal(float x, float y) {\n" +
	        "    float dx = 0.0;\n" +
	        "    float dy = 0.0;\n" +
	        "    for (int i = 0; i < numWaves; ++i) {\n" +
	        "        dx += dWavedx(i, x, y);\n" +
	        "        dy += dWavedy(i, x, y);\n" +
	        "    }\n" +
	        "    vec3 n = vec3(-dx, -dy, 1.0);\n" +
	        "    return normalize(n);\n" +
	        "}\n" +
	        "void main() {\n" +
	        "    vec4 pos = a_position;\n" +
	        "    pos.y = waterHeight + waveHeight(pos.x, pos.z);\n" +
	        "    position = pos.xyz / pos.w;\n" +
	        "    worldNormal = waveNormal(pos.x, pos.z);\n" +
	        "    eyeNormal = (MMatrix * vec4(worldNormal, 0.0)).xyz;\n" +
	        "    gl_Position = VPMatrix * (MMatrix * vec4(pos.rgb, 1.0));\n" +
	        "}\n";

    public static final String mFragmentShader =   
        "#ifdef GL_ES\n" +
        "precision highp float;\n" +
        "#endif\n" +
        "varying vec3 position;\n" +
        "varying vec3 worldNormal;\n" +
        "varying vec3 eyeNormal;\n" +
        "uniform vec3 eyePos;\n" +
        "uniform sampler2D envMap;\n" +
        "void main() {\n" +
        "     vec3 eye = normalize(eyePos - position);\n" +
        "     vec3 r = reflect(eye, worldNormal);\n" +
        "     vec2 uv = vec2(atan((r.z,r.x)/3.14159*2.0+0.5),asin(r.y)/3.14159*2.0+0.5);\n"+	
        "     vec4 color = texture2D(envMap, uv)/2.0;\n" +
        "     color.a = 0.6;\n" +
        "     gl_FragColor = color;\n" +
        "}\n";
        
}