public class DesktopLauncher {
    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Frog Game");
        config.setWindowedMode(800, 600);
        new Lwjgl3Application(new Frog(), config);
    }
}
