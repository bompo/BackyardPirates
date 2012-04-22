package de.swagner.piratesbigsea;

import java.util.ArrayList;

import bloom.Bloom;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import de.swagner.piratesbigsea.com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import de.swagner.piratesbigsea.com.badlogic.gdx.graphics.g3d.model.still.StillModel;

public class MenuScreen extends DefaultScreen implements InputProcessor {

	float startTime = 0;
	PerspectiveCamera cam;
	Frustum camCulling = new Frustum();
	private World world;
	
	private ArrayList<Body> enemies = new ArrayList<Body>();
	private ArrayList<Body> bullets = new ArrayList<Body>();
	
	private Player player;
	

	Music background;
	
	//water
	StillModel modelPlaneObj;
	Texture envMapTex;
	Texture groundTex;
	
	StillModel modelPlaneRealObj;
	
	Texture introTex;
	
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

	float delta;
	float scale, rotate = 0;

	// GLES20
	Matrix4 model = new Matrix4().idt();
	Matrix4 normal = new Matrix4().idt();
	Matrix4 tmp = new Matrix4().idt();

	private ShaderProgram diffuseShader;
	private ShaderProgram flagShaderFog;
	private ShaderProgram waterShaderFog;
	
	Array<Float> waterAmplitude = new Array<Float>();
	Array<Float> waterWavelength= new Array<Float>();
	Array<Float> waterSpeed = new Array<Float>();
	Array<Float> waterAngleX = new Array<Float>();
	Array<Float> waterAngleY = new Array<Float>();

	public MenuScreen(Game game) {
		super(game);
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setInputProcessor(this);
		
		background = Gdx.audio.newMusic(Gdx.files.internal("data/wind.ogg"));
		if(Configuration.getInstance().sound) {
			background.setLooping(true);
			background.play();
		}

		modelPlaneObj = ModelLoaderRegistry.loadStillModel(Gdx.files
				.internal("data/sphere.g3dt"));
		modelPlaneRealObj = ModelLoaderRegistry.loadStillModel(Gdx.files
				.internal("data/plane.g3dt"));
		
		envMapTex = new Texture(Gdx.files.internal("data/envmap.png"));
		
		introTex = new Texture(Gdx.files.internal("data/intro.png"));
		
		groundTex = new Texture(Gdx.files.internal("data/ground.png"), true);
		
		boatTex = new Texture(Gdx.files.internal("data/ship.png"), true);
		boatTexPlayer = new Texture(Gdx.files.internal("data/ship_player.png"), true);
		
		modelBoatObj = ModelLoaderRegistry.loadStillModel(Gdx.files
				.internal("data/boat.g3dt"));
		
		modelWorldObj = ModelLoaderRegistry.loadStillModel(Gdx.files
				.internal("data/world.g3dt"));
		

		modelWorldEdgeObj= ModelLoaderRegistry.loadStillModel(Gdx.files
				.internal("data/worldedge.g3dt"));
		worldEdgeTex  = new Texture(Gdx.files.internal("data/worldedge.png"), true);

		modelCannonBallObj = ModelLoaderRegistry.loadStillModel(Gdx.files
				.internal("data/cannonball.g3dt"));
		
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
		initLevel();
		
		for (int i = 0; i < 4; ++i) {
	        float amplitude = 0.01f / (i + 1);
	        waterAmplitude.add(amplitude);
	
	        float wavelength = .15f * MathUtils.PI / (i + 1);
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
		
	}
	
	private void createPhysicsWorld() {
		// we instantiate a new World with a proper gravity vector
		// and tell it to sleep when possible.
		world = new World(new Vector2(0, 0), false);

//		createEnemies();

		player = new Player(world);
	}
	
	private void createEnemies() {
		PolygonShape boxPoly = new PolygonShape();
		boxPoly.setAsBox(1.5f, 4f);

		for (int i = 0; i < 10; i++) {
			// Create the BodyDef, set a random position above the
			// ground and create a new body
			BodyDef boxBodyDef = new BodyDef();
			boxBodyDef.type = BodyType.DynamicBody;
			boxBodyDef.position.x = 2;
			boxBodyDef.position.y = 2;
			boxBodyDef.angularDamping = 1f;
			boxBodyDef.linearDamping = 1.0f;
			Body boxBody = world.createBody(boxBodyDef);

			boxBody.createFixture(boxPoly, 1);

			// add the box to our list of boxes
			enemies.add(boxBody);
		}

		// we are done, all that's left is disposing the boxPoly
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
			cam = new PerspectiveCamera(45, Gdx.graphics.getWidth(),
					Gdx.graphics.getHeight());
			cam.position.set(oldPosition);
			cam.direction.set(oldDirection);
		} else {
			cam = new PerspectiveCamera(45, Gdx.graphics.getWidth(),
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
		Vector3 position = new Vector3();
		position = new Vector3(0, 8, 0);

		calculateModelMatrix();
	}

	private void calculateModelMatrix() {
		//submarine.calculateMatrix();
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

		processInput();
		cam.update();

		doPhysics();
		updateAI();

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
		
		batch.begin();
		font.drawMultiLine(batch, "Controls\nWASD/Directional Keys: Move\nCRTL: Fire Right\nSHIFT/SPACE: Fire Left\nF: Fullscreen\nB: Bloom\nG: Sound", 40, 150);
		font.drawMultiLine(batch, "a game for LD23\nby @twbompo", 650, 60);
		batch.end();

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
				game.setScreen(new GameScreen(game));
			}
		}
	}


	private void updateAI() {
		player.update(delta);
	}

	private void renderScene() {
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		
		diffuseShader.begin();
		diffuseShader.setUniformMatrix("VPMatrix", cam.combined);
		diffuseShader.setUniformi("uSampler", 0);

		groundTex.bind(0);
		
		//render world

		tmp.idt();
		model.idt();

		tmp.setToTranslation(-0.3f, -1.6f, 0);
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
		
//		worldEdgeTex.bind(0);
		modelWorldEdgeObj.render(diffuseShader);
		
		boatTex.bind(0);
		
		//render enemies
		for (int i = 0; i < enemies.size(); i++) {
			
			Body box = enemies.get(i);
			
			tmp.idt();
			model.idt();
			
			tmp.setToScaling(0.2f,0.2f,0.2f);
			model.mul(tmp);
			
			tmp.setToRotation(Vector3.X, 90);
			model.mul(tmp);
			
			Vector2 position = box.getPosition();
			tmp.setToTranslation(-position.x+10, -position.y+10, -1f);
			model.mul(tmp);

			tmp.setToRotation(Vector3.Z, MathUtils.radiansToDegrees * box.getAngle());
			model.mul(tmp);
			
			diffuseShader.setUniformMatrix("MMatrix", model);
			modelBoatObj.render(diffuseShader);
		}
		
		//render bullets
		for (int i = 0; i < bullets.size(); i++) {
			
			Body box = bullets.get(i);
			
			tmp.idt();
			model.idt();
			
			tmp.setToScaling(0.2f,0.2f,0.2f);
			model.mul(tmp);
			
			tmp.setToRotation(Vector3.X, 90);
			model.mul(tmp);
			
			Vector2 position = box.getPosition();
			tmp.setToTranslation(-position.x+10, -position.y+10, -1f);
			model.mul(tmp);
			

			tmp.setToScaling(0.2f,0.2f,0.2f);
			model.mul(tmp);

			tmp.setToRotation(Vector3.Z, MathUtils.radiansToDegrees * box.getAngle());
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
		tmp.setToTranslation(-position.x+10, -position.y+10, -1f);
		model.mul(tmp);

		tmp.setToRotation(Vector3.Z, MathUtils.radiansToDegrees * player.body.getAngle());
		model.mul(tmp);
		
		diffuseShader.setUniformMatrix("MMatrix", model);
		modelBoatObj.render(diffuseShader);
		
		
		diffuseShader.end();
		
		//update cam
		Vector3 position3 = new Vector3();
		model.getTranslation(position3);
		cam.position.set(position3.x+1, (-position3.z)+2.2f+scale, position3.z+2);
		cam.lookAt(0, (position3.y/1.5f), position3.y);
		
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA );
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
		
		//render intro flag
		flagShaderFog.begin();
		flagShaderFog.setUniformMatrix("VPMatrix", cam.projection.setToOrtho(0, 16, 0, 10, 1, 100));
		flagShaderFog.setUniformf("time", startTime);
	
		introTex.bind(0);
		flagShaderFog.setUniformi("uSampler", 0);

		tmp.idt();
		model.idt();

		tmp.setToTranslation(3, 7.1f, -8);
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


	private void processInput() {

	}

	private void doPhysics() {		
		world.step(Gdx.graphics.getDeltaTime(), 6, 2);
	}
	
	public void shoot(final Vector2 position, final Vector2 velocity, final Vector2 xdir) {
		if(player.lastShot < 1) {
			return;
		}
		
		for(int i=0; i<3; i++) {
			Body box = createCircle(BodyType.DynamicBody, 0.01f, 10000);
	
			box.setBullet(true);		 
			box.setTransform(position.x + i, position.y, 0);
	//		Vector2 dir = new Vector2(xdir, 0);
	//		dir = dir.nor();
	//		b.speed += (velocity.x*xdir);
	//		dir.mul(b.speed);
			box.setLinearVelocity(velocity.mul(1000));
	
	//		b.body = box;
	//		b.body.setUserData(b);
	//		b.body.getFixtureList().get(0).setFilterData(GameInstance.getInstance().bulletCollideFilter);
			bullets.add(box);
		}
		
		player.lastShot = 0;
	}
	
	public Body createCircle(BodyType type, float radius, float density) {
		BodyDef def = new BodyDef();
		def.type = type;
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
			finished = true;
		if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
			Gdx.app.exit();
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
		finished = true;
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
