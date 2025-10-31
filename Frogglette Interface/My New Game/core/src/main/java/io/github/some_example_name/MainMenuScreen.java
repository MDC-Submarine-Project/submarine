package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuScreen implements Screen {

    private final Frog game;
    private Stage stage;
    private Skin skin;
    private Texture buttonTex;
    private Texture buttonPressedTex;

    public MainMenuScreen(Frog game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // --- Skin & font ---
        skin = new Skin();
        BitmapFont font = new BitmapFont();
        skin.add("default", font);

        // --- Normal button texture ---
        Pixmap pixmap = new Pixmap(200, 60, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.DARK_GRAY);
        pixmap.fill();
        buttonTex = new Texture(pixmap);

        // --- Pressed button texture ---
        pixmap.setColor(Color.GRAY);
        pixmap.fill();
        buttonPressedTex = new Texture(pixmap);
        pixmap.dispose();

        TextureRegionDrawable buttonDrawable = new TextureRegionDrawable(buttonTex);
        TextureRegionDrawable buttonPressedDrawable = new TextureRegionDrawable(buttonPressedTex);

        // --- TextButton style ---
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = buttonDrawable;
        textButtonStyle.down = buttonPressedDrawable;
        textButtonStyle.font = font;
        textButtonStyle.fontColor = Color.WHITE;
        skin.add("default", textButtonStyle);

        // --- Label style ---
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", labelStyle);

        // --- Table layout ---
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // --- Title ---
        Label title = new Label("Frog Game", skin);
        title.setFontScale(2f);
        title.setAlignment(Align.center);

        // --- Buttons ---
        TextButton startButton = new TextButton("Start Game", skin);
        TextButton exitButton = new TextButton("Exit", skin);

        // --- Add to table ---
        table.center();
        table.add(title).padBottom(50).row();
        table.add(startButton).padBottom(20).width(200).height(60).row();
        table.add(exitButton).width(200).height(60);

        // --- Button listeners ---
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Launch CubeScreen on click
                game.setScreen(new CubeScreen(game));
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (buttonTex != null) buttonTex.dispose();
        if (buttonPressedTex != null) buttonPressedTex.dispose();
    }
}
