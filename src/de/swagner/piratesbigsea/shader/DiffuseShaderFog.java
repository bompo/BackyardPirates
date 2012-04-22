package de.swagner.piratesbigsea.shader;

public class DiffuseShaderFog {

    public static final String mVertexShader =
    	"attribute vec4 a_position;\n" +
    	"attribute vec4 a_normal;\n" +
    	"attribute vec2 a_texCoord0;\n" +
    	"uniform mat4 VPMatrix;\n"+
    	"uniform mat4 MMatrix;\n"+
    	"varying vec3 vLightWeighting;\n"+
    	"varying vec2 vTextureCoord;\n"+
    	"void main() {\n"+
    	"	gl_Position = ((VPMatrix * MMatrix) * a_position);\n"+
    	"   vTextureCoord = a_texCoord0;\n"+
        "	float directionalLightWeighting = max(dot(a_normal.xyz,vec3(0.0,-0.5,-0.5)), 0.0);\n"+
        "	vLightWeighting = vec3(0.05,0.05,0.05) * directionalLightWeighting;\n"+
    	"}\n";

    public static final String mFragmentShader =   
        "#ifdef GL_ES\n" +
        "precision mediump float;\n" +
        "#endif\n" +
        "uniform vec4 a_color;\n"+
        "uniform vec4 uFogColor;\n"+
        "varying vec3 vLightWeighting;\n"+
        "varying vec2 vTextureCoord;\n"+
        "uniform sampler2D uSampler;\n"+
    	"void main() {\n"+
    	"float z = gl_FragCoord.z / gl_FragCoord.w;\n"+
    	"float fogFactor = exp2(-0.08 * 0.08 * z* z * 0.442695);\n"+
    	"fogFactor = clamp(fogFactor,0.0,1.0);\n"+
        "vec4 fragColor = vec4(texture2D(uSampler, vec2(vTextureCoord.s, vTextureCoord.t)).rgb + vLightWeighting, 1.0);\n"+
        "gl_FragColor = mix((uFogColor), fragColor, fogFactor);\n"+
        "}\n"; 
}