package de.swagner.piratesbigsea;

import com.badlogic.gdx.math.Vector2;

public class UpdatePackage {
	
	public NetworkShip ship;
	public float angle;
	public Vector2 pos;
	
	public UpdatePackage(NetworkShip ship, Vector2 pos, float angle) {
		this.ship = ship;
		this.pos = pos;
		this.angle = angle;
	}
	
	public void apply() {
		this.ship.body.setTransform(pos, angle);
	}
	
	

}
