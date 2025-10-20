package dev.sebastianb.boids;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Main game client class for the application.
 * This class handles the game lifecycle and rendering.
 */
public class GameClient extends ApplicationAdapter {
    private SpriteBatch batch;
    private BitmapFont font;
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
    }

    @Override
    public void render() {
        // Clear the screen with a dark blue color
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.begin();
        font.draw(batch, "Game Client Running", 100, Gdx.graphics.getHeight() / 2);
        batch.end();
    }
    
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}