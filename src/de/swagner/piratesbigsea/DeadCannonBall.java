package de.swagner.piratesbigsea;

import com.badlogic.gdx.math.Vector3;

public class DeadCannonBall {
	
	public float life = 0;
	public float angle = 0;
	public Vector3 position;
	
	public DeadCannonBall(Vector3 position, float angle) {
		this.position = position;
		this.position.z = -1;
		this.angle = angle;
	}
	
	public void update(float delta) {
		if(position.z<5) {
			life+= delta;
			position.z += delta;
		}
	}

}
