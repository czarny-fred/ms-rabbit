package com.example.demogame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class LoadingScreen implements Screen {

    private final BattleGame game;
    private final ShapeRenderer shapeRenderer;

    private static final float BAR_HEIGHT = 30;
    private static final float BAR_MARGIN = 40;

    public LoadingScreen(BattleGame game) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render(float delta) {
        // Load one asset per frame
        boolean done = game.loadNext();

        float progress = game.getLoadingProgress();

        // Dark/dimmed background
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float screenW = BattleGame.WIDTH;
        float screenH = BattleGame.HEIGHT;

        // Draw progress bar background (dark gray)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(BAR_MARGIN, BAR_MARGIN, screenW - BAR_MARGIN * 2, BAR_HEIGHT);

        // Draw progress bar fill (green)
        shapeRenderer.setColor(0.1f, 0.8f, 0.1f, 1f);
        shapeRenderer.rect(BAR_MARGIN, BAR_MARGIN, (screenW - BAR_MARGIN * 2) * progress, BAR_HEIGHT);
        shapeRenderer.end();

        // Draw percentage text and title
        int percent = (int) (progress * 100);
        game.batch.begin();
        game.menuFont.setColor(com.badlogic.gdx.graphics.Color.RED);
        game.menuFont.getData().setScale(2f);
        game.menuFont.draw(game.batch, "Iter Lucis", screenW / 2 - 230, screenH / 2 + 60);
        game.menuFont.getData().setScale(1f);
        game.font.draw(game.batch, percent + "%", screenW / 2 - 20, BAR_MARGIN + BAR_HEIGHT + 30);
        game.batch.end();

        if (done) {
            game.applyLinearFiltering();
            if (!game.tutorialDone) {
                game.setScreen(new TutorialScreen(game));
            } else {
                game.setScreen(new MenuScreen(game));
            }
        }
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
        shapeRenderer.dispose();
    }
}