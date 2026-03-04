package com.example.demogame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class
DropGame extends Game {

    SpriteBatch batch;
    Texture bucketTexture;
    Texture dropletTexture;

    @Override
    public void create() {
        batch = new SpriteBatch();
        bucketTexture = TextureFactory.createBucket();
        dropletTexture = TextureFactory.createDroplet();
        setScreen(new GameScreen(this));
    }

    @Override
    public void dispose() {
        batch.dispose();
        bucketTexture.dispose();
        dropletTexture.dispose();
    }
}