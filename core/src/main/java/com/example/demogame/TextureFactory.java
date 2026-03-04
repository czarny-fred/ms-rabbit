package com.example.demogame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class TextureFactory {

    public static Texture createBucket() {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        // Bucket body
        pixmap.setColor(Color.GRAY);
        pixmap.fillRectangle(8, 16, 48, 48);
        // Bucket rim (wider top)
        pixmap.setColor(Color.DARK_GRAY);
        pixmap.fillRectangle(4, 12, 56, 8);
        // Bucket handle
        pixmap.setColor(Color.DARK_GRAY);
        pixmap.fillRectangle(24, 0, 16, 16);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public static Texture createDroplet() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        // Teardrop shape: circle + triangle top
        pixmap.setColor(new Color(0.2f, 0.5f, 1f, 1f));
        pixmap.fillCircle(16, 20, 11);
        pixmap.fillTriangle(5, 18, 27, 18, 16, 2);
        // Highlight
        pixmap.setColor(new Color(0.6f, 0.8f, 1f, 0.7f));
        pixmap.fillCircle(12, 17, 4);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}