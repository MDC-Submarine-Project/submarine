package dev.sebastianb.boids.utils;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import dev.senj.common.Constants;

public class RenderUtils {


    public static int monitorWidth, monitorHeight;


    // TODO: actually make the window scale dynamically + icon textures
    public static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle(Constants.GAME_NAME);
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        int[] monitorSize = {Lwjgl3ApplicationConfiguration.getDisplayMode().width, Lwjgl3ApplicationConfiguration.getDisplayMode().height};
        configuration.setWindowSizeLimits(monitorSize[0] / 3, monitorSize[1] / 3, monitorSize[0], monitorSize[1]);
        monitorWidth = monitorSize[0] / 3;
        monitorHeight = monitorSize[1] / 3;

        configuration.setWindowedMode(monitorSize[0] / 2, monitorSize[1] / 2); // this line changes the size of the window

        configuration.setResizable(false); // TODO: actually make resizeable as this breaks the world render


        return configuration;
    }

}