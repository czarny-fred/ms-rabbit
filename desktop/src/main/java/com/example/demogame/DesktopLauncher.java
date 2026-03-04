package com.example.demogame;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;

public class DesktopLauncher {
    public static void main(String[] args) {
        // Use async GLFW so we don't need -XstartOnFirstThread on macOS
        Lwjgl3ApplicationConfiguration.useGlfwAsync();

        // macOS dock icon
        try {
            if (Taskbar.isTaskbarSupported()) {
                Image icon = ImageIO.read(new File("ikona-removebg-preview.png"));
                Taskbar.getTaskbar().setIconImage(icon);
            }
        } catch (Exception ignored) {}

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Iter Lucis");
        config.setWindowedMode(BattleGame.WIDTH, BattleGame.HEIGHT);
        config.setResizable(false);
        config.setForegroundFPS(60);
        config.setIdleFPS(30);
        config.setWindowIcon("ikona-removebg-preview.png");
        new Lwjgl3Application(new BattleGame(), config);
    }
}