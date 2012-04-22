package de.swagner.piratesbigsea;

import java.util.List;

import bloom.Bloom;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.utils.Array;

import de.swagner.piratesbigsea.com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import de.swagner.piratesbigsea.com.badlogic.gdx.graphics.g3d.model.still.StillModel;

public class GameScreen extends DefaultScreen implements InputProcessor {

	float startTime = 0;
	PerspectiveCamera cam;
	Frustum camCulling = new Frustum();
	private World world;
	
	private Array<EnemyShip> enemies = new Array<EnemyShip>();
	private Array<CannonBall> bullets = new Array<CannonBall>();
	private Array<DeadCannonBall> deadBullets = new Array<DeadCannonBall>();
	private Array<DeadEnemyShip> deadEnemies = new Array<DeadEnemyShip>();
	
	private Player player;
	private Body groundBody;
	
	Music background;
	Sound hit;
	Sound shoot;

	//water
	StillModel modelPlaneObj;
	Texture envMapTex;
	Texture groundTex;
	
	StillModel modelPlaneRealObj;
	Texture getReadyTex;
	Texture sinkemTex;
	Texture winTex;
	Texture loseTex;
	
	Texture blackTex;
	
	StillModel modelGrassObj;
	Texture grassTex;
	
	Texture gardenTex;
	
	//boat
	StillModel modelBoatObj;
	Texture boatTex;
	Texture boatTexPlayer;
	
	StillModel modelCannonBallObj;
	
	//world
	StillModel modelWorldObj;
	StillModel modelWorldEdgeObj;
	Texture worldEdgeTex;

	SpriteBatch batch;
	SpriteBatch fadeBatch;
	SpriteBatch fontbatch;
	BitmapFont font;
	Sprite blackFade;
	
	Bloom bloom = new Bloom();

	float fade = 1.0f;
	boolean finished = false;
	
	boolean win = false;
	boolean lose = false;

	float delta;
	float scale, rotate = 0;

	float levelCounter = 5;
	int currentLevel = 1;
	float winLoseCounter = -1;
	
	// GLES20
	Matrix4 model = new Matrix4().idt();
	Matrix4 normal = new Matrix4().idt();
	Matrix4 tmp = new Matrix4().idt();

	private ShaderProgram diffuseShader;
	private ShaderProgram waterShaderFog;
	private ShaderProgram flagShaderFog;
	
	Array<Float> waterAmplitude = new Array<Float>();
	Array<Float> waterWavelength= new Array<Float>();
	Array<Float> waterSpeed = new Array<Float>();
	Array<Float> waterAngleX = new Array<Float>();
	Array<Float> waterAngleY = new Array<Float>();
	
//	Box2DDebugRenderer debugRenderer;

	public GameScreen(Game game) {
		super(game);
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setInputProcessor(this);
		
		background = Gdx.audio.newMusic(Gdx.files.internal("data/wind.ogg"));
		if(Configuration.getInstance().sound) {
			background.setLooping(true);
			background.play();
		}
		
		hit = Gdx.audio.newSound(Gdx.files.internal("data/hit.ogg"));
		shoot = Gdx.audio.newSound(Gdx.files.internal("data/shoot.ogg"));

		modelPlaneObj = ModelLoaderRegistry.loadStillModel(Gdx.files
				.internal("data/sphere.g3dt"));
		envMapTex = new Texture(Gdx.files.internal("data/envmap.png"));
		
		modelPlaneRealObj = ModelLoaderRegistry.loadStillModel(Gdx.files
				.internal("data/plane.g3dt"));
		
		modelGrassObj = ModelLoaderRegistry.loadStillModel(Gdx.files
				.internal("data/plane.g3dt"));
		grassTex = new Texture(Gdx.files.internal("data/grass.png"), true);
		grassTex.setWrap(TextureWrap.Repeat,TextureWrap.Repeat);
		gardenTex = new Texture(Gdx.files.internal("data/garden.png"), true);
		
		groundTex = new Texture(Gdx.files.internal("data/ground.png"), true);
		
		boatTex = new Texture(Gdx.files.internal("data/ship.png"), true);
		boatTexPlayer = new Texture(Gdx.files.internal("data/ship_player.png"), true);
		
		blackTex = new Texture(Gdx.files.internal("data/black.png"), true);
		
		modelBoatObj = ModelLoaderRegistry.loadStillModel(Gdx.files
				.internal("data/boat.g3dt"));
		
		modelWorldObj = ModelLoaderRegistry.loadStillModel(Gdx.files
				.internal("data/world.g3dt"));
		

		modelWorldEdgeObj= ModelLoaderRegistry.loadStillModel(Gdx.files
				.internal("data/worldedge.g3dt"));
		worldEdgeTex  = new Texture(Gdx.files.internal("data/worldedge.png"), true);

		modelCannonBallObj = ModelLoaderRegistry.loadStillModel(Gdx.files
				.internal("data/cannonball.g3dt"));
		
		getReadyTex  = new Texture(Gdx.files.internal("data/getready.png"), true);
		sinkemTex  = new Texture(Gdx.files.internal("data/sinkem.png"), true);
		winTex  = new Texture(Gdx.files.internal("data/win.png"), true);
		loseTex  = new Texture(Gdx.files.internal("data/lose.png"), true);
		
		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		fontbatch = new SpriteBatch();

		blackFade = new Sprite(
				new Texture(Gdx.files.internal("data/black.png")));
		fadeBatch = new SpriteBatch();
		fadeBatch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);

		font = Resources.getInstance().font;
		font.setScale(1);

		waterShaderFog = Resources.getInstance().waterShaderFog;
		diffuseShader = Resources.getInstance().diffuseShader;

		flagShaderFog = Resources.getInstance().flagShaderFog;

		initRender();
		
		for (int i = 0; i < 4; ++i) {
	        float amplitude = 0.01f / (i + 1);
	        waterAmplitude.add(amplitude);
	
	        float wavelength = .2f * MathUtils.PI / (i + 1);
	        waterWavelength.add(wavelength);
	
	        float speed = 0.000005f + 0.05f*i;
	        waterSpeed.add(speed);
	        
	        float angle = MathUtils.random(-MathUtils.PI/3, MathUtils.PI/3);
	        waterAngleX.add(MathUtils.cos(angle));
	        waterAngleY.add(MathUtils.sin(angle));
		};
		
		createPhysicsWorld();
		
		bloom.setBloomIntesity(0.8f);
		bloom.setClearColor(Resources.getInstance().clearColor[0],
				Resources.getInstance().clearColor[1],
				Resources.getInstance().clearColor[2],
				Resources.getInstance().clearColor[3]);
		

		initLevel();
		
	}
	
	private void createPhysicsWorld() {
		world = new World(new Vector2(0, 0), false);
		
//		debugRenderer = new Box2DDebugRenderer( true, true, true, true );
		
		{
			PolygonShape groundPoly = new PolygonShape();
			Vector2[] vertices = new Vector2[4];	
			
		    vertices[0] = new Vector2(-50f  , -50f  );
		    vertices[1] = new Vector2(-45f , -50f  );
		    vertices[2] = new Vector2(-45f , 50f);
		    vertices[3] = new Vector2(-50f , 50f);
		    groundPoly.set(vertices);
	
			BodyDef groundBodyDef = new BodyDef();
			groundBodyDef.type = BodyType.StaticBody;
			groundBodyDef.position.x = 10;
			groundBodyDef.position.y = 0;
			groundBody = world.createBody(groundBodyDef);
	
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = groundPoly;
			fixtureDef.filter.groupIndex = 0;
			groundBody.createFixture(fixtureDef);
			groundPoly.dispose();
		}
		{
			PolygonShape groundPoly = new PolygonShape();
			Vector2[] vertices = new Vector2[4];	
			
		    vertices[0] = new Vector2(45f  , -50f  );
		    vertices[1] = new Vector2(50f , -50f  );
		    vertices[2] = new Vector2(50f , 50f);
		    vertices[3] = new Vector2(45f , 50f);
		    groundPoly.set(vertices);
	
			BodyDef groundBodyDef = new BodyDef();
			groundBodyDef.type = BodyType.StaticBody;
			groundBodyDef.position.x = 10;
			groundBodyDef.position.y = 0;
			groundBody = world.createBody(groundBodyDef);
	
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = groundPoly;
			fixtureDef.filter.groupIndex = 0;
			groundBody.createFixture(fixtureDef);
			groundPoly.dispose();
		}
		
		{
			PolygonShape groundPoly = new PolygonShape();
			Vector2[] vertices = new Vector2[4];	
			
		    vertices[0] = new Vector2(50f  , -40f  );
		    vertices[1] = new Vector2(50f , -35f  );
		    vertices[2] = new Vector2(-50f , -35f);
		    vertices[3] = new Vector2(-50f , -40f);
		    groundPoly.set(vertices);
	
			BodyDef groundBodyDef = new BodyDef();
			groundBodyDef.type = BodyType.StaticBody;
			groundBodyDef.position.x = 10;
			groundBodyDef.position.y = 0;
			groundBody = world.createBody(groundBodyDef);
	
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = groundPoly;
			fixtureDef.filter.groupIndex = 0;
			groundBody.createFixture(fixtureDef);
			groundPoly.dispose();
		}
		
		{
			PolygonShape groundPoly = new PolygonShape();
			Vector2[] vertices = new Vector2[4];	
			
		    vertices[0] = new Vector2(50f  , 55f  );
		    vertices[1] = new Vector2(50f , 60f  );
		    vertices[2] = new Vector2(-50f , 60f);
		    vertices[3] = new Vector2(-50f , 55f);
		    groundPoly.set(vertices);
	
			BodyDef groundBodyDef = new BodyDef();
			groundBodyDef.type = BodyType.StaticBody;
			groundBodyDef.position.x = 10;
			groundBodyDef.position.y = 0;
			groundBody = world.createBody(groundBodyDef);
	
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = groundPoly;
			fixtureDef.filter.groupIndex = 0;
			groundBody.createFixture(fixtureDef);
			groundPoly.dispose();
		}
		
		{
			PolygonShape groundPoly = new PolygonShape();
			Vector2[] vertices = new Vector2[4];	
			
		    vertices[0] = new Vector2(60f  , 0f  );
		    vertices[1] = new Vector2(70f , 20f  );
		    vertices[2] = new Vector2(0f , -55);
		    vertices[3] = new Vector2(5f , -75f);
		    groundPoly.set(vertices);
	
			BodyDef groundBodyDef = new BodyDef();
			groundBodyDef.type = BodyType.StaticBody;
			groundBodyDef.position.x = 10;
			groundBodyDef.position.y = 0;
			groundBody = world.createBody(groundBodyDef);
	
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = groundPoly;
			fixtureDef.filter.groupIndex = 0;
			groundBody.createFixture(fixtureDef);
			groundPoly.dispose();
		}
		
		{
			PolygonShape groundPoly = new PolygonShape();
			Vector2[] vertices = new Vector2[4];	
			
		    vertices[0] = new Vector2(80f  , 0f  );
		    vertices[1] = new Vector2(90f , 10f  );
		    vertices[2] = new Vector2(0f , 85);
		    vertices[3] = new Vector2(5f , 68f);
		    groundPoly.set(vertices);
	
			BodyDef groundBodyDef = new BodyDef();
			groundBodyDef.type = BodyType.StaticBody;
			groundBodyDef.position.x = 10;
			groundBodyDef.position.y = 0;
			groundBody = world.createBody(groundBodyDef);
	
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = groundPoly;
			fixtureDef.filter.groupIndex = 0;
			groundBody.createFixture(fixtureDef);
			groundPoly.dispose();
		}
		
		{
			PolygonShape groundPoly = new PolygonShape();
			Vector2[] vertices = new Vector2[4];	
			
		    vertices[0] = new Vector2(0f  , -60f  );
		    vertices[1] = new Vector2(-50f , 0f  );
		    vertices[2] = new Vector2(-60f , 0);
		    vertices[3] = new Vector2(-10f , -60f);
		    groundPoly.set(vertices);
	
			BodyDef groundBodyDef = new BodyDef();
			groundBodyDef.type = BodyType.StaticBody;
			groundBodyDef.position.x = 10;
			groundBodyDef.position.y = 0;
			groundBody = world.createBody(groundBodyDef);
	
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = groundPoly;
			fixtureDef.filter.groupIndex = 0;
			groundBody.createFixture(fixtureDef);
			groundPoly.dispose();
		}
		
		{
			PolygonShape groundPoly = new PolygonShape();
			Vector2[] vertices = new Vector2[4];	
			
		    vertices[0] = new Vector2(0f  , 75f  );
		    vertices[1] = new Vector2(-65f , 30f  );
		    vertices[2] = new Vector2(-50f , 30);
		    vertices[3] = new Vector2(10f , 75f);
		    groundPoly.set(vertices);
	
			BodyDef groundBodyDef = new BodyDef();
			groundBodyDef.type = BodyType.StaticBody;
			groundBodyDef.position.x = 10;
			groundBodyDef.position.y = 0;
			groundBody = world.createBody(groundBodyDef);
	
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = groundPoly;
			fixtureDef.filter.groupIndex = 0;
			groundBody.createFixture(fixtureDef);
			groundPoly.dispose();
		}



		player = new Player(world);
	}
	
	private void createEnemies(int howMuch) {
		PolygonShape boxPoly = new PolygonShape();
		boxPoly.setAsBox(1.5f, 4f);

		for (int i = 0; i < howMuch; i++) {
			BodyDef boxBodyDef = new BodyDef();
			boxBodyDef.type = BodyType.DynamicBody;
			boxBodyDef.position.x = MathUtils.random(-25, 45);
			boxBodyDef.position.y = MathUtils.random(-20, 45);
			boxBodyDef.angularDamping = 1f;
			boxBodyDef.linearDamping = 1.0f;
			Body boxBody = world.createBody(boxBodyDef);

			boxBody.createFixture(boxPoly, 1);

			enemies.add(new EnemyShip(boxBody));
			boxBody.setUserData(enemies.get(enemies.size-1));
		}

		boxPoly.dispose();
	}

	public void initRender() {
		Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());

		Gdx.gl.glClearColor(Resources.getInstance().clearColor[0],
				Resources.getInstance().clearColor[1],
				Resources.getInstance().clearColor[2],
				Resources.getInstance().clearColor[3]);

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
	}

	public void resize(int width, int height) {
		super.resize(width, height);
		Vector3 oldPosition = new Vector3();
		Vector3 oldDirection = new Vector3();
		if (cam != null) {
			oldPosition.set(cam.position);
			oldDirection.set(cam.direction);
			cam = new PerspectiveCamera(28, Gdx.graphics.getWidth(),
					Gdx.graphics.getHeight());
			cam.position.set(oldPosition);
			cam.direction.set(oldDirection);
		} else {
			cam = new PerspectiveCamera(28, Gdx.graphics.getWidth(),
					Gdx.graphics.getHeight());
			cam.position.set(0, 0f, 16f);
			cam.direction.set(0, 0, -1);
		}
		cam.up.set(0, 1, 0);
		cam.near = 0.5f;
		cam.far = 600f;

		initRender();
		
//		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
	}

	private void initLevel() {
		winLoseCounter -= delta;
		
		if(winLoseCounter<0) {
			
			boolean found = false;
			do {
				found = false;
				for (int e = 0; e < enemies.size; e++) {
					world.destroyBody(enemies.get(e).body);
					enemies.removeIndex(e);
					found = true;
					break;
				}
			} while (found);

			if(win) {
				currentLevel += 1;
			} else {
				player = new Player(world);
			}
			levelCounter = 4;
			
			System.out.println(enemies.size);
			
			createEnemies(currentLevel);
			win = false;
			lose = false;
			winLoseCounter = 5;
		}
	}

	private void reset() {
		initLevel();
	}

	public void show() {
	}

	public void render(float deltaTime) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		delta = Math.min(0.1f, deltaTime);

		startTime += delta;

		scale += (MathUtils.sin(startTime) * delta) / 20.f;
		rotate += (MathUtils.cos(startTime) * delta) / 10.f;

		if(levelCounter<0) {
			processInput();
			doPhysics();
			updateAI();
		} else {
			levelCounter -= delta;
		}
		
		cam.update();

		if(Configuration.getInstance().bloom) {
			bloom.capture();
			renderScene();				
			bloom.render();
		} else {
			renderScene();
		}
		
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
		Gdx.gl.glDepthMask(true);
		

	
		boolean renderFlag = false;
		if(levelCounter>1) {
			getReadyTex.bind(0);
			renderFlag = true;
			
		} else if(levelCounter>0) {
			sinkemTex.bind(0);
			renderFlag = true;
		}
		
		if(win) {
			winTex.bind(0);
			renderFlag = true;
		}
		if(lose) {
			loseTex.bind(0);
			renderFlag = true;
		}
		
		if(renderFlag) {
			//render intro flag
			flagShaderFog.begin();
			flagShaderFog.setUniformMatrix("VPMatrix", cam.projection.setToOrtho(0, 16, 0, 10, 1, 100));
			flagShaderFog.setUniformf("time", startTime);
			
			
			flagShaderFog.setUniformi("uSampler", 0);
	
			tmp.idt();
			model.idt();
	
			tmp.setToTranslation(8, 5.5f, -8);
			model.mul(tmp);
			
			tmp.setToRotation(Vector3.Z, -90);
			model.mul(tmp);
			
			tmp.setToRotation(Vector3.X, 90);
			model.mul(tmp);
	
			tmp.setToScaling(2, 2, 2);
			model.mul(tmp);
			
			flagShaderFog.setUniformMatrix("MMatrix", model);
			modelPlaneRealObj.render(flagShaderFog);
			flagShaderFog.end();
		}

		// FadeInOut
		if (!finished && fade > 0) {
			fade = Math.max(fade - (delta), 0);
			fadeBatch.begin();
			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g,
					blackFade.getColor().b, fade);
			blackFade.draw(fadeBatch);
			fadeBatch.end();
		}

		if (finished) {
			fade = Math.min(fade + (delta), 1);
			fadeBatch.begin();
			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g,
					blackFade.getColor().b, fade);
			blackFade.draw(fadeBatch);
			fadeBatch.end();
			if (fade >= 1) {
				game.setScreen(new MenuScreen(game));
			}
		}
	}


	private void updateAI() {
		player.update(delta);
		
		for(CannonBall ball:bullets) {
			ball.update(delta);
		}
		
		for(DeadCannonBall ball:deadBullets) {
			ball.update(delta);
		}
		
		for(EnemyShip ball:enemies) {
			int shootcode = ball.update(delta, player);
			if(shootcode==1) {
				shootEnemy(ball.body.getWorldCenter().add(ball.body.getWorldVector(new Vector2(3f,1f))), ball.body.getWorldVector(new Vector2(0,1f)).rotate(-90).cpy());
				ball.hitAnimation = 4;
			} else if(shootcode==2) {
				shootEnemy(ball.body.getWorldCenter().add(ball.body.getWorldVector(new Vector2(-3f,1f))), ball.body.getWorldVector(new Vector2(0,1f)).rotate(90).cpy());
				ball.hitAnimation = -4;
			}
			
		}
		
		for(DeadEnemyShip ship:deadEnemies) {
			ship.update(delta);
		}
		
		for (int e = 0; e < bullets.size; e++) {
			if (bullets.get(e).body.getLinearVelocity().len2()<5) {
				deadBullets.add(new DeadCannonBall(new Vector3(bullets.get(e).body.getWorldCenter().x,bullets.get(e).body.getWorldCenter().y,0), bullets.get(e).body.getAngle()));
				
				world.destroyBody(bullets.get(e).body);
				bullets.removeIndex(e);
				
			}
		}		
		
		while(deadBullets.size>100) {
			deadBullets.removeIndex(0);				
		}
		
		while(deadEnemies.size>20) {
			deadEnemies.removeIndex(0);				
		}		
		
		for (int e = 0; e < enemies.size; e++) {
			if (enemies.get(e).life <= 0 && enemies.get(e).body.getLinearVelocity().len2()<5) {
				deadEnemies.add(new DeadEnemyShip(new Vector3(enemies.get(e).body.getWorldCenter().x,enemies.get(e).body.getWorldCenter().y,0), enemies.get(e).body.getAngle()));
				
				world.destroyBody(enemies.get(e).body);
				enemies.removeIndex(e);
				
			}
		}
		
		//WIN LOSE?
		if(player.life<=0) {
			lose = true;
			win = false;
			initLevel();
		}
		if(enemies.size<=0) {
			win = true;
			lose = false;
			initLevel();
		}
	}

	private void renderScene() {
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		
//		Matrix4 debug = new Matrix4(cam.view);
//		model.idt();
//
//		tmp.setToTranslation(2f, 0.0f, 2.5f);
//		debug.mul(tmp);
//		
//		tmp.setToScaling(0.2f,0.2f,0.2f);
//		debug.mul(tmp);
//		tmp.idt();
//		tmp.setToRotation(Vector3.X, 90);
//		debug.mul(tmp);
//		tmp.setToRotation(Vector3.Z, 180);
//		debug.mul(tmp);
//		
//		debugRenderer.render( world, cam.projection.cpy().mul(debug));
		
		diffuseShader.begin();
		diffuseShader.setUniformMatrix("VPMatrix", cam.combined);
		diffuseShader.setUniformi("uSampler", 0);
		
		//render grass
		grassTex.bind(0);
		
		tmp.idt();
		model.idt();

		tmp.setToTranslation(-0.3f, -2.0f, 0);
		model.mul(tmp);

		tmp.setToScaling(30.8f, 0.5f, 30.5f);
		model.mul(tmp);
		
		
		diffuseShader.setUniformMatrix("MMatrix", model);
		modelGrassObj.render(diffuseShader);
		
		//render garden
		gardenTex.bind(0);
		
		tmp.idt();
		model.idt();

		tmp.setToTranslation(-0.3f, 2.5f, 0);
		model.mul(tmp);

		tmp.setToScaling(20.8f, 3.5f, 20.5f);
		model.mul(tmp);
		
		
		diffuseShader.setUniformMatrix("MMatrix", model);
		modelWorldEdgeObj.render(diffuseShader);
		

		groundTex.bind(0);
		
		//render world

		tmp.idt();
		model.idt();

		tmp.setToTranslation(-0.3f, -2.0f, 0);
		model.mul(tmp);

		tmp.setToScaling(10.8f, 0.5f, 10.5f);
		model.mul(tmp);
		
		
		diffuseShader.setUniformMatrix("MMatrix", model);
		modelWorldObj.render(diffuseShader);
		
		tmp.idt();
		model.idt();

		tmp.setToTranslation(0, -1.0f, 0);
		model.mul(tmp);

		tmp.setToScaling(10.0f, 1.5f, 10.1f);
		model.mul(tmp);
		
		
		diffuseShader.setUniformMatrix("MMatrix", model);
		
		worldEdgeTex.bind(0);
		modelWorldEdgeObj.render(diffuseShader);
		
		boatTex.bind(0);
		
		//render enemies
		for (int i = 0; i < enemies.size; i++) {
			
			EnemyShip box = enemies.get(i);
			
			tmp.idt();
			model.idt();
			
			tmp.setToScaling(0.2f,0.2f,0.2f);
			model.mul(tmp);
			
			tmp.setToRotation(Vector3.X, 90);
			model.mul(tmp);
			
			Vector2 position = box.body.getPosition();
			tmp.setToTranslation(-position.x+10, -position.y+10, -1f);
			model.mul(tmp);

			tmp.setToRotation(Vector3.Z, MathUtils.radiansToDegrees * box.body.getAngle());
			model.mul(tmp);
			
			tmp.setToRotation(Vector3.Y, MathUtils.sin(box.hitAnimation)*20.f);
			model.mul(tmp);
			
			diffuseShader.setUniformMatrix("MMatrix", model);
			modelBoatObj.render(diffuseShader);
		}
		
		//render dead enemies
		for (int i = 0; i < deadEnemies.size; i++) {
			
			DeadEnemyShip box = deadEnemies.get(i);
			
			tmp.idt();
			model.idt();
			
			tmp.setToScaling(0.2f,0.2f,0.2f);
			model.mul(tmp);
			
			tmp.setToRotation(Vector3.X, 90);
			model.mul(tmp);
			
			tmp.setToTranslation(-box.position.x+10, -box.position.y+10, box.position.z);
			model.mul(tmp);
			
			tmp.setToRotation(Vector3.Y, MathUtils.radiansToDegrees * (box.sinkAngle+1)/5.f);
			model.mul(tmp);

			tmp.setToRotation(Vector3.Z, MathUtils.radiansToDegrees * box.angle);
			model.mul(tmp);
			
			diffuseShader.setUniformMatrix("MMatrix", model);
			modelBoatObj.render(diffuseShader);
		}
		
		blackTex.bind(0);
		
		//render bullets
		for (int i = 0; i < bullets.size; i++) {
			
			CannonBall box = bullets.get(i);
			
			tmp.idt();
			model.idt();
			
			tmp.setToScaling(0.2f,0.2f,0.2f);
			model.mul(tmp);
			
			tmp.setToRotation(Vector3.X, 90);
			model.mul(tmp);
			
			Vector2 position = box.body.getPosition();
			tmp.setToTranslation(-position.x+10, -position.y+10, -1);
			model.mul(tmp);
			

			tmp.setToScaling(0.2f,0.2f,0.2f);
			model.mul(tmp);

			tmp.setToRotation(Vector3.Z, MathUtils.radiansToDegrees * box.body.getAngle());
			model.mul(tmp);
			
			diffuseShader.setUniformMatrix("MMatrix", model);
			modelCannonBallObj.render(diffuseShader);
		}
		
		//render dead bullets
		for (int i = 0; i < deadBullets.size; i++) {
			
			DeadCannonBall box = deadBullets.get(i);
			
			tmp.idt();
			model.idt();
			
			tmp.setToScaling(0.2f,0.2f,0.2f);
			model.mul(tmp);
			
			tmp.setToRotation(Vector3.X, 90);
			model.mul(tmp);
			
			tmp.setToTranslation(-box.position.x+10, -box.position.y+10, box.position.z);
			model.mul(tmp);
			

			tmp.setToScaling(0.2f,0.2f,0.2f);
			model.mul(tmp);

			tmp.setToRotation(Vector3.Z, MathUtils.radiansToDegrees * box.angle);
			model.mul(tmp);
			
			diffuseShader.setUniformMatrix("MMatrix", model);
			modelCannonBallObj.render(diffuseShader);
		}
		
		
		boatTexPlayer.bind(0);
		
		//render player
		tmp.idt();
		model.idt();


		tmp.setToScaling(0.2f,0.2f,0.2f);
		model.mul(tmp);
		
		tmp.setToRotation(Vector3.X, 90);
		model.mul(tmp);		
		
		Vector2 position = player.body.getPosition();
		tmp.setToTranslation(-position.x+10, -position.y+10, player.posz);
		model.mul(tmp);

		tmp.setToRotation(Vector3.Z, MathUtils.radiansToDegrees * player.body.getAngle());
		model.mul(tmp);
		
		if(player.life<=0) {
			tmp.setToRotation(Vector3.Y, MathUtils.radiansToDegrees * (player.sinkAngle+1)/5.f);
			model.mul(tmp);
		}
		
		tmp.setToRotation(Vector3.Y, MathUtils.sin(player.hitAnimation)*20.f);
		model.mul(tmp);
		
		diffuseShader.setUniformMatrix("MMatrix", model);
		modelBoatObj.render(diffuseShader);
		
		
		diffuseShader.end();
		
		//update cam
		Vector3 position3 = new Vector3();
		model.getTranslation(position3);
		cam.position.set(position3.x, (-position3.z/4.0f)+10, position3.y-20);
		cam.lookAt(0, (position3.y/1.5f)-2, position3.y);
		
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc ( GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA );
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);

		//render water
		waterShaderFog.begin();
		waterShaderFog.setUniformMatrix("VPMatrix", cam.combined);
		
		waterShaderFog.setUniformf("waterHeight", 0.001f);
		waterShaderFog.setUniformf("time", startTime);
		waterShaderFog.setUniformi("numWaves", 6);
		
		for (int i = 0; i < 4; ++i) {
	        waterShaderFog.setUniformf("amplitude[" + i +  "]", waterAmplitude.get(i));
	        waterShaderFog.setUniformf("wavelength[" + i +  "]", waterWavelength.get(i));
	        waterShaderFog.setUniformf("speed[" + i +  "]", waterSpeed.get(i));
	        waterShaderFog.setUniformf("direction[" + i +  "]", waterAngleX.get(i), waterAngleY.get(i));
		};
		waterShaderFog.setUniformf("eyePos", cam.position.x, cam.position.y, cam.position.z);
				
		envMapTex.bind(0);
		waterShaderFog.setUniformi("envMap", 0);

		tmp.idt();
		model.idt();

		tmp.setToTranslation(0, 0.1f, 0);
		model.mul(tmp);

		tmp.setToScaling(10, 10, 10);
		model.mul(tmp);
		
		waterShaderFog.setUniformMatrix("MMatrix", model);
		modelPlaneObj.render(waterShaderFog);
		waterShaderFog.end();
		
	}

	private void processInput() {
		if(player.life<=0) return;
		
		if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) {
			player.body.applyLinearImpulse(player.body.getWorldVector(new Vector2(0,1.5f)),player.body.getWorldCenter());
		}

		if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) {
			player.body.applyLinearImpulse(player.body.getWorldVector(new Vector2(0,0.0f)),player.body.getWorldCenter());
		}		

		if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) {
			player.body.applyAngularImpulse(-Gdx.graphics.getDeltaTime()*100.f);
		}

		if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) {
			player.body.applyAngularImpulse(Gdx.graphics.getDeltaTime()*100.f);
		}
		
		if (Gdx.input.isKeyPressed(Keys.F2)) {
			System.out.println(player.body.getWorldCenter());
		}
		
		if (Gdx.input.isKeyPressed(Keys.SPACE) || Gdx.input.isKeyPressed(Keys.X) || Gdx.input.isKeyPressed(Keys.E) || Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)) {
			boolean shooted = shoot(player.body.getWorldCenter().add(player.body.getWorldVector(new Vector2(3f,1f))), player.body.getWorldVector(new Vector2(0,1f)).rotate(-90).cpy());
			if(shooted) {
				player.hitAnimation = 4;
			}
		}
		
		if (Gdx.input.isKeyPressed(Keys.Z) || Gdx.input.isKeyPressed(Keys.Y) || Gdx.input.isKeyPressed(Keys.Q)  || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT) || Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
			boolean shooted = shoot(player.body.getWorldCenter().add(player.body.getWorldVector(new Vector2(-3f,1f))), player.body.getWorldVector(new Vector2(0,1f)).rotate(90).cpy());
			if(shooted) {
				player.hitAnimation = -4;
			}
		}

	}

	private void doPhysics() {		
		world.step(Gdx.graphics.getDeltaTime(), 6, 2);
		
	
			List<Contact> contactList = world.getContactList();
			for (int i = 0; i < contactList.size(); i++) {
				Contact contact = contactList.get(i);
				if (contact.isTouching()) {
					Object a = contact.getFixtureA().getBody().getUserData();
					Object b = contact.getFixtureB().getBody().getUserData();

					if (a instanceof CannonBall && b instanceof EnemyShip) {
						if(((CannonBall) a).life < 10) {
							((EnemyShip) b).life -= 1;
							((CannonBall) a).life = 10;
							if(Configuration.getInstance().sound) {
								hit.play();
							}
						}
					}
					
					if (a instanceof EnemyShip && b instanceof CannonBall) {
						if(((CannonBall) b).life < 10) {
							((EnemyShip) a).life -= 1;
							((CannonBall) b).life = 10;
							if(Configuration.getInstance().sound) {
								hit.play();
							}
						}
					}
					
					if (a instanceof CannonBall && b instanceof Player) {
						if(((CannonBall) a).life < 10 && ((CannonBall) a).friendly == false) {
							((Player) b).life -= 1;
							((CannonBall) a).life = 10;
							if(Configuration.getInstance().sound) {
								hit.play();
							}
						}
					}
					
					if (a instanceof Player && b instanceof CannonBall) {
						if(((CannonBall) b).life < 10 && ((CannonBall) b).friendly == false) {
							((Player) a).life -= 1;
							((CannonBall) b).life = 10;
							if(Configuration.getInstance().sound) {
								hit.play();
							}
						}
					}
				}
			}

		
	}
	
	public boolean shoot(final Vector2 position, final Vector2 velocity) {
		if(player.lastShot < 1) {
			return false;
		}
		
		for(int i=0; i<3; i++) {
			
			Body box = createCircle(BodyType.DynamicBody, 0.01f, 10000);
			box.setBullet(true);		 
			box.setTransform(position.x + i, position.y, 0);
			box.setLinearVelocity(velocity.mul(1000));

			bullets.add(new CannonBall(box, true));
			box.setUserData(bullets.get(bullets.size-1));
		}
		
		player.lastShot = 0;
		
		if(Configuration.getInstance().sound) {
			shoot.play();
		}
		
		return true;
	}
	
	public boolean shootEnemy(final Vector2 position, final Vector2 velocity) {		
		for(int i=0; i<3; i++) {
			
			Body box = createCircle(BodyType.DynamicBody, 0.01f, 10000);
			box.setBullet(true);		 
			box.setTransform(position.x + i, position.y, 0);
			box.setLinearVelocity(velocity.mul(1000));

			bullets.add(new CannonBall(box, false));
			box.setUserData(bullets.get(bullets.size-1));
		}
		if(Configuration.getInstance().sound) {
			shoot.play();
		}
		
		return true;
	}
	
	public Body createCircle(BodyType type, float radius, float density) {
		BodyDef def = new BodyDef();
		def.type = type;
		def.linearDamping = 5;
		Body box = world.createBody(def);

		CircleShape poly = new CircleShape();
		poly.setRadius(radius);
		Fixture fix = box.createFixture(poly, density);
		poly.dispose();

		return box;
	}

	@Override
	public boolean keyDown(int keycode) {
		if (Gdx.input.isTouched())
			return false;
		if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
			finished = true;
		}
		if (keycode == Input.Keys.F) {			
			if(Gdx.app.getType() == ApplicationType.Desktop) {
				if(!Gdx.graphics.isFullscreen()) {
					Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, Gdx.graphics.getDesktopDisplayMode().height, true);
					Configuration.getInstance().setFullscreen(true);
				} else {
					Gdx.graphics.setDisplayMode(800,480, false);		
					Configuration.getInstance().setFullscreen(false);
				}				
			}			
		}
		if (keycode == Input.Keys.B) {			
			Configuration.getInstance().setBloom(!Configuration.getInstance().bloom);			
		}

		return false;
	}


	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchMoved(int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}
}
