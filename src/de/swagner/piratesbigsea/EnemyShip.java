package de.swagner.piratesbigsea;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class EnemyShip {

	public Body body;
	public float life = 5;

	public float hitAnimation = 0;

	private float lastShot = 0;
	private int approach_sign = 1;

	private float APPROACH_DISTANCE = 15;

	// 0 = approach
	// 1 = turn
	// 2 = shoot
	// 3 = move_away
	private int state = 0;

	// recylce
	Vector2 target_direction = new Vector2();

	public EnemyShip(Body body) {
		this.body = body;
	}

	public void reviseApproach() {
		if (MathUtils.random() < 0.5) {
			approach_sign = 1;
		} else {
			approach_sign = -1;
		}
	}

	public int update(float delta, Player player) {
		lastShot += delta;
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

		if (player.life > 0) {
			
			float target_distance = player.body.getWorldCenter().cpy()
					.dst(body.getWorldCenter().cpy());
			target_direction.set(player.body.getWorldCenter().cpy())
					.sub(body.getWorldCenter().cpy());
			
			if (target_distance > (APPROACH_DISTANCE)) {
				state = 0;
			} 
			

			// approach
			if (state == 0) {
				goTowards(player.body.getWorldCenter());
				if (target_distance < APPROACH_DISTANCE) {
					reviseApproach();
					state = 1;
				}
			}
			// turn
			else if (state == 1) {
				turn(-approach_sign);
				thrust();
				if (target_direction.dot(body.getWorldVector(new Vector2(0,1f)).cpy().nor()) < 0.5f) {
					state = 2;
				}
			}
			// shoot
			else if (state == 2) {
				turn(approach_sign);
				thrust();

				if (lastShot > 10) {
					lastShot = 0;
					state = 3;
					if(approach_sign == -1) {
						return 1;
					}
					if(approach_sign == 1) {
						return 2;
					}
				}
			}
			// move_away
			else if (state == 3) {
				goAway(player.body.getWorldCenter());
			}
			
			System.out.println(state);
		}
		return 0;

	}

	public void turn(float direction) {
		body.applyAngularImpulse(direction * Gdx.graphics.getDeltaTime()
				* 100.f);
	}

	public void thrust() {
		body.applyLinearImpulse(body.getWorldVector(new Vector2(0, 1.5f)),
				body.getWorldCenter());
	}

	public void goTowardsOrAway(Vector2 targetPos, boolean isAway) {
		Vector2 target_direction = targetPos.cpy().sub(body.getWorldCenter().cpy());
		if (isAway) {
			target_direction.mul(-1);
		}

		if (body.getWorldCenter().add(
				body.getWorldVector(new Vector2(0, 1f))).cpy().crs(target_direction) > 0) {
			turn(1);
		} else {
			turn(-1);
		}

		if (body.getWorldCenter().add(
				body.getWorldVector(new Vector2(0, 1f)).cpy()).nor().dot(target_direction) < 0 || isAway) {
			thrust();
		}
	}

	public void goTowards(Vector2 targetPos) {
		goTowardsOrAway(targetPos, false);
	}

	public void goAway(Vector2 targetPos) {
		goTowardsOrAway(targetPos, true);
	}

}
