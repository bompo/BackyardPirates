package de.swagner.piratesbigsea;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopStarter extends Game {

	public static void main(String[] args) {
		
		DisplayMode displayMode = LwjglApplicationConfiguration.getDesktopDisplayMode();
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.setFromDisplayMode(displayMode);

		config.width = 1280;
		config.height = 800;
		config.title = "Backyard Pirates - ld23 entry by bompo";

		config.fullscreen = false;
		config.samples = 4;
		config.useGL20 = true;
		config.vSyncEnabled = false;
		config.useCPUSynch = true;
		new LwjglApplication(new DesktopStarter(), config);
	}

	@Override
	public void create() {
		Configuration.getInstance().setConfiguration();
		setScreen(new MenuScreen(this));	
	}

}
