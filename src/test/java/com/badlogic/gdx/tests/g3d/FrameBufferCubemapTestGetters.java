
package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.glutils.FrameBufferCubemap;
import com.badlogic.gdx.math.QuaternionDouble;
import com.badlogic.gdx.utils.ScreenUtils;

/** Render a basic scene in a FrameBufferCubemap and displays it in a rotating cube. */
public class FrameBufferCubemapTestGetters extends Basic3DSceneTest {
	protected PerspectiveCamera camFb;
	protected PerspectiveCamera camCube;
	protected FrameBufferCubemap fb;
	protected Cubemap cubemap;
	protected Model cubeMesh;
	protected ModelInstance cubeInstance;
	protected ModelBatch cubeBatch;
	protected QuaternionDouble quat;

	@Override
	public void create () {
		super.create();

		quat = new QuaternionDouble();
//		quat = new Quaternion();

		camFb = new PerspectiveCamera(90, 800, 800);
		camFb.position.set(10f, 10f, 10f);
		camFb.lookAt(0, 0, 0);
		camFb.near = 0.1f;
		camFb.far = 1000f;
		camFb.update();

		fb = new FrameBufferCubemap(Format.RGBA8888, 800, 800, true);
		cubemap = fb.getColorBufferTexture();

		ObjLoader objLoader = new ObjLoader();
		cubeMesh = objLoader.loadModel(Gdx.files.internal("data/cube.obj"));
		cubeInstance = new ModelInstance(cubeMesh);

		cubeBatch = new ModelBatch(Gdx.files.internal("data/shaders/cubemap-vert.glsl"),
				Gdx.files.internal("data/shaders/cubemap-frag.glsl"));

		cubeInstance.materials.get(0).set(new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap));

		camCube = new PerspectiveCamera(67, Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f);
		camCube.position.set(0f, 2f, 2f);
		camCube.lookAt(0, 0, 0);
		camCube.near = 1f;
		camCube.far = 300f;
		camCube.update();
	}

	@Override
	public void render () {
		renderScene();
		renderCube();
	}

	public void renderScene () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

		// Render scene to screen
		super.render();

		// Render scene to cubemap
		camFb.position.set(cam.position);
		camFb.near = cam.near;
		camFb.far = cam.far;
		fb.begin();
		while (fb.nextSide()) {
			fb.getSide().getUp(camFb.up);
			fb.getSide().getDirection(camFb.direction);
			camFb.update();

			ScreenUtils.clear(1, 1, 1, 1, true);

			modelBatch.begin(camFb);
			for (ModelInstance instance : instances)
				modelBatch.render(instance, lights);
			if (space != null) modelBatch.render(space);
			modelBatch.end();
		}
		fb.end();
	}

	float yaw, pitch, roll;

	public void renderCube () {
		int w = Gdx.graphics.getBackBufferWidth();
		int h = Gdx.graphics.getBackBufferHeight();
		int x = (int)(w - w * 0.5f);
		int y = (int)(h - h * 0.5f);
		w >>= 1;
		h >>= 1;

		Gdx.gl.glViewport(x, y, w, h);
		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
		Gdx.gl.glScissor(x, y, w, h);
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		quat.setEulerAngles(
				quat.getYaw() + 45 * Gdx.graphics.getDeltaTime(),
				quat.getPitch() + 25 * Gdx.graphics.getDeltaTime(),
				quat.getRoll() + 10 * Gdx.graphics.getDeltaTime()
				);
		System.out.println("Yaw: " + quat.getYaw() + ", Pitch: " + quat.getPitch() + ", Roll: " + quat.getRoll());
		cubeInstance.transform.set((float) quat.x, (float) quat.y, (float) quat.z, (float) quat.w);
		cubeBatch.begin(camCube);
		cubeBatch.render(cubeInstance);
		cubeBatch.end();
	}

	public static void main(String[] args){
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("FrameBufferCubemapTest");
		config.setWindowedMode(800, 600);
		config.disableAudio(true);
		config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
		config.useVsync(true);
		new Lwjgl3Application(new FrameBufferCubemapTestGetters(), config);
	}
}
