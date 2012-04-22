package de.swagner.piratesbigsea;

import com.badlogic.gdx.physics.box2d.Body;

public class CannonBall {
	
	public Body body;
	public float life = 0;
	public boolean friendly;
	
	public CannonBall(Body body, boolean friendly) {
		this.body = body;
		this.friendly = friendly;
	}
	
	public void update(float delta) {
		life+= delta;
	}

}
