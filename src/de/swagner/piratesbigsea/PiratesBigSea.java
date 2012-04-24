package de.swagner.piratesbigsea;

import com.badlogic.gdx.Game;

public class PiratesBigSea extends Game {
	@Override
	public void create() {
		setScreen(new MenuScreen(this));
	}
}
