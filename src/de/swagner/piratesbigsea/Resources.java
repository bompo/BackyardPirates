package de.swagner.piratesbigsea;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import de.swagner.piratesbigsea.shader.DiffuseShader;
import de.swagner.piratesbigsea.shader.DiffuseShaderFogWobble;
import de.swagner.piratesbigsea.shader.WaterShaderFog;

public class Resources {
	
	public static final boolean POSTPROCESSING = true;

	public final boolean debugMode = true;

	public ShaderProgram diffuseShader;
	public ShaderProgram waterShaderFog;
	public ShaderProgram flagShaderFog;

	public BitmapFont font;
	
	public float[] clearColor = { 0.6f, 0.7f, 1.0f, 1.0f };
	public float[] fogColor ={ 0.8f, 0.8f, 0.85f, 1.0f };

	public static Resources instance;

	public static Resources getInstance() {
		if (instance == null) {
			instance = new Resources();
		}
		return instance;
	}

	public Resources() {
		reInit();
	}

	public void reInit() {				
		initShader();

		font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), false);
	}

	public void initShader() {
		diffuseShader = new ShaderProgram(DiffuseShader.mVertexShader, DiffuseShader.mFragmentShader);
		if (diffuseShader.isCompiled() == false) {
			Gdx.app.log("diffuseShader: ", diffuseShader.getLog());
			Gdx.app.exit();
		}
	
		waterShaderFog = new ShaderProgram(WaterShaderFog.mVertexShader, WaterShaderFog.mFragmentShader);
		if (waterShaderFog.isCompiled() == false) {
			Gdx.app.log("waterShaderFog: ", waterShaderFog.getLog());
			Gdx.app.exit();
		}
		
		flagShaderFog = new ShaderProgram(DiffuseShaderFogWobble.mVertexShader, DiffuseShaderFogWobble.mFragmentShader);
		if (flagShaderFog.isCompiled() == false) {
			Gdx.app.log("FlagShaderFog: ", flagShaderFog.getLog());
			Gdx.app.exit();
		}
		
	}

	public void dispose() {
		font.dispose();
	}
}
