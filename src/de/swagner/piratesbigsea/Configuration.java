package de.swagner.piratesbigsea;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class Configuration {
	
	public Preferences preferences;
	public boolean fullscreen;
	public boolean bloom;
	public boolean sound;
	public float brighness = 0.0f;
	
	static Configuration instance;
	
	private Configuration() {
		preferences = Gdx.app.getPreferences("PiratesofthebigSea");
		loadConfig();
	}
	
	private void loadConfig() {
		fullscreen = preferences.getBoolean("fullscreen", true);
		bloom = preferences.getBoolean("bloom", true);
		sound = preferences.getBoolean("sound", true);
	}
	
	public void setConfiguration() {
		if(Gdx.app.getType() == ApplicationType.Desktop) {
			if(fullscreen) {
				Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, Gdx.graphics.getDesktopDisplayMode().height, true);
			} else {
				Gdx.graphics.setDisplayMode(800,480, false);
			}
		}
	}
	
	public void setFullscreen(boolean onOff) {
		preferences.putBoolean("fullscreen", onOff);
		fullscreen = onOff;
		preferences.flush();
	}
	
	
	public void setSound(boolean onOff) {
		preferences.putBoolean("sound", onOff);
		sound = onOff;
		preferences.flush();
	}
	
	public void setBloom(boolean onOff) {
		preferences.putBoolean("bloom", onOff);
		bloom = onOff;
		preferences.flush();
	}
	
	public static Configuration getInstance() {
		if(instance!=null) return instance;
		instance = new Configuration();		
		return instance;
	}	
	


}
