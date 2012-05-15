package de.swagner.piratesbigsea;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class NetworkShip {

	public Body body;

	//NETWORK stuff
	public String id;
	public Integer place;
	
	public float lastShot = 0;
	public float hitAnimation = 0;
	public float life = 5;
	
	public enum STATE {
		IDLE, UP, LEFT, RIGHT, UPLEFT, UPRIGHT
	}
	public STATE state = STATE.IDLE;
	
	public float posz = -1;
	
	public float sinkAngle = -1;
	public float sinkSpeed = MathUtils.random(-1,1);


	public NetworkShip(String id, int place, World world, int x, int y, float angle) {
		this.id = id;
		this.place = place;
		
		PolygonShape boxPoly = new PolygonShape();
		boxPoly.setAsBox(1.5f, 4f);
		
		BodyDef boxBodyDef = new BodyDef();
		boxBodyDef.type = BodyType.DynamicBody;
		boxBodyDef.position.x = x;
		boxBodyDef.position.y = y;
		boxBodyDef.angle = angle;
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
		
		if (state == STATE.UP || state == STATE.UPLEFT || state == STATE.UPRIGHT) {
			body.applyLinearImpulse(body.getWorldVector(new Vector2(0,1.5f)), body.getWorldCenter());
		}
		
		if (state == STATE.LEFT  || state == STATE.UPLEFT) {
			body.applyAngularImpulse(-delta*100.f);
		}

		if (state == STATE.RIGHT  || state == STATE.UPRIGHT) {
			body.applyAngularImpulse(delta*100.f);
		}	
		
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
