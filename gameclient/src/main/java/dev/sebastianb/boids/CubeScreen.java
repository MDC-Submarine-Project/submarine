package dev.sebastianb.boids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class CubeScreen implements Screen {

    private final Frog game;

    private PerspectiveCamera cam;
    private ModelBatch modelBatch;
    private Model cubeModel, floorModel;
    private ModelInstance cubeInstance, floorInstance;
    private btDiscreteDynamicsWorld dynamicsWorld;
    private btRigidBody cubeBody, floorBody;
    private btDefaultCollisionConfiguration collisionConfig;
    private btCollisionDispatcher dispatcher;
    private btDbvtBroadphase broadphase;
    private btSequentialImpulseConstraintSolver solver;
    private btCollisionShape floorShape, cubeShape;

    private Stage stage;
    private Skin skin;
    private Label depthLabel, tempLabel, speedLabel;
    private Random random;

    private float logTimer = 0f;
    private File logFile;

    private CubeController controller;

    public CubeScreen(Frog game) {
        this.game = game;
    }

    @Override
    public void show() {
        Bullet.init();
        random = new Random();

        // Camera setup
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(5f, 5f, 10f);
        cam.lookAt(0, 0, 0);
        cam.near = 0.1f;
        cam.far = 100f;

        modelBatch = new ModelBatch();

        // Models
        ModelBuilder mb = new ModelBuilder();
        cubeModel = mb.createBox(1f, 1f, 1f,
                new Material(ColorAttribute.createDiffuse(Color.RED)),
                Usage.Position | Usage.Normal);
        cubeInstance = new ModelInstance(cubeModel);
        cubeInstance.transform.setToTranslation(0f, 0f, 0f);

        floorModel = mb.createBox(100f, 1f, 100f,
                new Material(ColorAttribute.createDiffuse(Color.BLUE)),
                Usage.Position | Usage.Normal);
        floorInstance = new ModelInstance(floorModel);
        floorInstance.transform.setToTranslation(0f, -50f, 0f);

        // Physics setup
        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);
        broadphase = new btDbvtBroadphase();
        solver = new btSequentialImpulseConstraintSolver();
        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfig);
        dynamicsWorld.setGravity(new Vector3(0, -10f, 0));

        // Floor body
        floorShape = new btBoxShape(new Vector3(50f, 0.5f, 50f));
        floorBody = new btRigidBody(0f, null, floorShape);
        floorBody.setWorldTransform(floorInstance.transform);
        dynamicsWorld.addRigidBody(floorBody);

        // Cube body
        cubeShape = new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f));
        Vector3 inertia = new Vector3();
        cubeShape.calculateLocalInertia(1f, inertia);
        btRigidBody.btRigidBodyConstructionInfo cubeInfo =
                new btRigidBody.btRigidBodyConstructionInfo(1f, null, cubeShape, inertia);
        cubeBody = new btRigidBody(cubeInfo);
        cubeBody.setWorldTransform(cubeInstance.transform);
        dynamicsWorld.addRigidBody(cubeBody);
        cubeInfo.dispose();

        // UI Setup
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin();
        BitmapFont font = new BitmapFont();
        skin.add("default", font);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
        btnStyle.down = skin.newDrawable("white", Color.GRAY);
        btnStyle.font = font;
        btnStyle.fontColor = Color.WHITE;
        skin.add("default", btnStyle);

        // Sensor Labels
        Table sensorTable = new Table();
        sensorTable.top().left();
        sensorTable.setFillParent(true);
        stage.addActor(sensorTable);

        depthLabel = new Label("Depth: 0", new Label.LabelStyle(font, Color.WHITE));
        tempLabel = new Label("Temp: 0", new Label.LabelStyle(font, Color.WHITE));
        speedLabel = new Label("Speed: 0", new Label.LabelStyle(font, Color.WHITE));

        sensorTable.add(depthLabel).pad(5).row();
        sensorTable.add(tempLabel).pad(5).row();
        sensorTable.add(speedLabel).pad(5).row();

        // Control buttons
        Table controls = new Table();
        controls.bottom().left();
        controls.setFillParent(true);
        stage.addActor(controls);

        TextButton up = new TextButton("Up", skin);
        TextButton down = new TextButton("Down", skin);
        TextButton left = new TextButton("Left", skin);
        TextButton right = new TextButton("Right", skin);
        TextButton dive = new TextButton("Dive", skin);
        TextButton surface = new TextButton("Surface", skin);
        TextButton ballastUp = new TextButton("Ballast +", skin);
        TextButton ballastDown = new TextButton("Ballast -", skin);
        TextButton restart = new TextButton("Restart", skin);
        TextButton exit = new TextButton("Exit", skin);

        controls.add(up).pad(5).row();
        Table horiz = new Table();
        horiz.add(left).pad(5);
        horiz.add(right).pad(5);
        controls.add(horiz).row();
        controls.add(down).pad(5).row();
        controls.add(dive).pad(5).row();
        controls.add(surface).pad(5).row();
        controls.add(ballastUp).pad(5).row();
        controls.add(ballastDown).pad(5).row();
        controls.add(restart).pad(5).row();
        controls.add(exit).pad(5).row();

        // Hook controller
        controller = new CubeController(cubeBody, 5f, 5f);
        controller.addControl(up, "up");
        controller.addControl(down, "down");
        controller.addControl(left, "left");
        controller.addControl(right, "right");
        controller.addControl(dive, "dive");
        controller.addControl(surface, "surface");
        controller.addControl(ballastUp, "ballastUp");
        controller.addControl(ballastDown, "ballastDown");

        // Restart button
        restart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new CubeScreen(game));
            }
        });

        // Exit button
        exit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // Logging setup - create new file each run
        File logDir = Gdx.files.local("logs").file();
        if (!logDir.exists()) logDir.mkdirs();

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        logFile = new File(logDir, "sensor_log_" + timestamp + ".txt");

        try {
            if (!logFile.exists()) logFile.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void render(float delta) {
        dynamicsWorld.stepSimulation(delta, 5);
        cubeBody.getWorldTransform(cubeInstance.transform);

        // Apply buoyancy & ballast from controller
        controller.applyBuoyancy(0f);

        // Sensor simulation
        Vector3 cubePos = cubeInstance.transform.getTranslation(new Vector3());
        float depth = Math.max(0f, -cubePos.y);
        float speed = cubeBody.getLinearVelocity().len();
        float temp = 20 + random.nextFloat() * 10f;

        depthLabel.setText(String.format("Depth: %.2f", depth));
        tempLabel.setText(String.format("Temp: %.2f °C", temp));
        speedLabel.setText(String.format("Speed: %.2f", speed));

        // Logging
        logTimer += delta;
        if (logTimer >= 1f) {
            logTimer = 0f;
            try (FileWriter w = new FileWriter(logFile, true)) {
                w.write(String.format("Depth: %.2f, Temp: %.2f, Speed: %.2f%n", depth, temp, speed));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Render scene
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        cam.update();
        modelBatch.begin(cam);
        modelBatch.render(floorInstance);
        modelBatch.render(cubeInstance);
        modelBatch.end();

        // Render UI
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { Gdx.input.setInputProcessor(null); }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        modelBatch.dispose();
        cubeModel.dispose();
        floorModel.dispose();

        // Dispose of Bullet physics objects in the correct order
        // First remove rigid bodies from the dynamics world
        if (dynamicsWorld != null) {
            if (cubeBody != null) dynamicsWorld.removeRigidBody(cubeBody);
            if (floorBody != null) dynamicsWorld.removeRigidBody(floorBody);
        }

        // Dispose of rigid bodies
        if (cubeBody != null) cubeBody.dispose();
        if (floorBody != null) floorBody.dispose();

        // Dispose of collision shapes
        if (cubeShape != null) cubeShape.dispose();
        if (floorShape != null) floorShape.dispose();

        // Dispose of dynamics world and related objects
        if (dynamicsWorld != null) dynamicsWorld.dispose();
        if (solver != null) solver.dispose();
        if (broadphase != null) broadphase.dispose();
        if (dispatcher != null) dispatcher.dispose();
        if (collisionConfig != null) collisionConfig.dispose();
    }
}
