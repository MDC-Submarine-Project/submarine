package dev.sebastianb.boids;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import dev.sebastianb.boids.utils.RenderUtils;

public class Main {
    public static void main(String[] args) {
        System.out.println("Game Client Starting...");
        var config = RenderUtils.getDefaultConfiguration();
        new Lwjgl3Application(new Frog(), config);
    }
}
