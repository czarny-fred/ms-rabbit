package com.example.demogame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class GameScreen implements Screen {

    private static final int WORLD_WIDTH = 800;
    private static final int WORLD_HEIGHT = 480;
    private static final int BUCKET_SIZE = 64;
    private static final int DROPLET_SIZE = 32;
    private static final int BUCKET_SPEED = 400;
    private static final int DROP_SPEED = 120;
    private static final long SPAWN_INTERVAL_NS = 500_000_000L;

    private final DropGame game;
    private final OrthographicCamera camera;
    private final BitmapFont font;
    private final Rectangle bucket;
    private final Array<Rectangle> droplets;

    private long lastDropTime;
    private int score;
    private int missed;
    private boolean gameOver;

    public GameScreen(DropGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        bucket = new Rectangle();
        bucket.x = (WORLD_WIDTH - BUCKET_SIZE) / 2f;
        bucket.y = 20;
        bucket.width = BUCKET_SIZE;
        bucket.height = BUCKET_SIZE;

        droplets = new Array<>();
        spawnDroplet();
    }

    private void spawnDroplet() {
        Rectangle drop = new Rectangle();
        drop.x = MathUtils.random(0, WORLD_WIDTH - DROPLET_SIZE);
        drop.y = WORLD_HEIGHT;
        drop.width = DROPLET_SIZE;
        drop.height = DROPLET_SIZE;
        droplets.add(drop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(game.bucketTexture, bucket.x, bucket.y, BUCKET_SIZE, BUCKET_SIZE);
        for (Rectangle drop : droplets) {
            game.batch.draw(game.dropletTexture, drop.x, drop.y, DROPLET_SIZE, DROPLET_SIZE);
        }
        font.draw(game.batch, "Score: " + score, 10, WORLD_HEIGHT - 10);
        font.draw(game.batch, "Missed: " + missed + " / 10", 10, WORLD_HEIGHT - 30);
        if (gameOver) {
            font.draw(game.batch, "GAME OVER - Press ENTER to restart", WORLD_WIDTH / 2f - 130, WORLD_HEIGHT / 2f);
        }
        game.batch.end();

        if (gameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                restart();
            }
            return;
        }

        // Move bucket with arrow keys
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucket.x -= BUCKET_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucket.x += BUCKET_SPEED * delta;
        }
        // Clamp to screen bounds
        bucket.x = MathUtils.clamp(bucket.x, 0, WORLD_WIDTH - BUCKET_SIZE);

        // Spawn new droplets
        if (TimeUtils.nanoTime() - lastDropTime > SPAWN_INTERVAL_NS) {
            spawnDroplet();
        }

        // Move droplets and check collisions
        for (int i = droplets.size - 1; i >= 0; i--) {
            Rectangle drop = droplets.get(i);
            drop.y -= DROP_SPEED * delta;

            if (drop.overlaps(bucket)) {
                score++;
                droplets.removeIndex(i);
            } else if (drop.y + DROPLET_SIZE < 0) {
                missed++;
                droplets.removeIndex(i);
                if (missed >= 10) {
                    gameOver = true;
                }
            }
        }
    }

    private void restart() {
        score = 0;
        missed = 0;
        gameOver = false;
        droplets.clear();
        bucket.x = (WORLD_WIDTH - BUCKET_SIZE) / 2f;
        spawnDroplet();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void show() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        font.dispose();
    }
}