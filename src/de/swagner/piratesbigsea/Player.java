package de.swagner.piratesbigsea;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Player {
	
	public Body body;
	public float lastShot = 0;
	public float hitAnimation = 0;
	public float life = 5;

	public float posz = -1;
	
	public float sinkAngle = -1;
	public float sinkSpeed = MathUtils.random(-1,1);
	
	public Player(World world) {
		PolygonShape boxPoly = new PolygonShape();
		boxPoly.setAsBox(1.5f, 4f);
		
		BodyDef boxBodyDef = new BodyDef();
		boxBodyDef.type = BodyType.DynamicBody;
		boxBodyDef.position.x = 5;
		boxBodyDef.position.y = 5;
		boxBodyDef.angularDamping = 1f;
		boxBodyDef.linearDamping = 1.0f;
		Body boxBody = world.createBody(boxBodyDef);

		boxBody.createFixture(boxPoly, 1);
		// add the box to our list of boxes
		body = boxBody;
		
		body.setUserData(this);
		

		// we are done, all that's left is disposing the boxPoly
		boxPoly.dispose();
	}
	
	public void update(float delta) {
		if(life>0) {
			lastShot+= delta;
			if(hitAnimation !=0) {
				if (hitAnimation<0)  {
					hitAnimation+=delta;
					if(hitAnimation>0) {
						hitAnimation = 0;
					}
				} else {
					hitAnimation-=delta;
					if(hitAnimation<0) {
						hitAnimation = 0;
					}
				}
			}
		} else {
			if(posz<5) {
				posz += delta;

				sinkAngle+=(sinkSpeed*delta);
			}
		}
	}

}
