package com.example.demogame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public class TutorialScreen implements Screen {

    private static final int W = BattleGame.WIDTH;
    private static final int H = BattleGame.HEIGHT;

    private final BattleGame game;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapeRenderer;
    private int page;

    // Tutorial pages
    private static final String[][] PAGES = {
        // Page 0: Welcome
        {
            "TUTORIAL",
            "",
            "Witaj w grze!",
            "Poznaj swoje bronie i umiejetnosci.",
            "",
            "Sterowanie:",
            "A/D - ruch lewo/prawo",
            "W/SPACJA - skok (podwojny skok)",
            "LPM - strzelanie",
            "R - przeladowanie",
            "Scroll - zmiana broni",
            "E - menu umiejetnosci",
            "ESC - pauza",
        },
        // Page 1: Kropidlo
        {
            "BRON 1: KROPIDLO",
            "",
            "Zalety:",
            "+ Zrownowazony obrazenia (10 DMG)",
            "+ Szybkie strzelanie (0.3s)",
            "+ Dobra predkosc pocisku (350)",
            "+ 12 naboi w magazynku",
            "+ Szybkie przeladowanie (2s)",
            "",
            "Wady:",
            "- Sredni zasieg i obrazenia",
            "- Brak specjalnych efektow",
            "",
            "Idealna bron na poczatek!",
        },
        // Page 2: Kadzidlo
        {
            "BRON 2: KADZIDLO",
            "",
            "Zalety:",
            "+ Seria 10 pociskow naraz!",
            "+ Najszybsze pociski (450)",
            "+ Duzy magazynek (30 naboi)",
            "+ Ogromne DPS w serii",
            "",
            "Wady:",
            "- Niskie obrazenia na pocisk (3 DMG)",
            "- Dluzsze przeladowanie (3s)",
            "- Wymaga celnosci w serii",
            "",
            "Swietna do szybkich wrogow!",
        },
        // Page 3: Biblia
        {
            "BRON 3: BIBLIA",
            "",
            "Zalety:",
            "+ Najwyzsze obrazenia (30 DMG!)",
            "+ Potezny pocisk",
            "",
            "Wady:",
            "- Wolne strzelanie (0.8s)",
            "- Wolny pocisk (250)",
            "- Tylko 3 naboje!",
            "- Najdluzsze przeladowanie (4s)",
            "- Maly pocisk",
            "",
            "Potezna ale trudna w uzyciu!",
        },
        // Page 4: Abilities
        {
            "UMIEJETNOSCI (klawisz E)",
            "",
            "1 - ODBICIE (50 many)",
            "  Odbija wrogie pociski przez 2s",
            "  Cooldown: 10s",
            "",
            "2 - LECZENIE (40 many)",
            "  Leczy 30 HP, 2s cooldown",
            "  Cooldown: 17s",
            "",
            "3 - OSWIECENIE (60 many)",
            "  Zamraza wroga na 5s, 20 DMG",
            "  Cooldown: 10s",
            "",
            "Mana regeneruje sie: 5/s",
        },
        // Page 5: Prayer & Angel
        {
            "MODLITWY I ANIOLEK",
            "",
            "Ksiega modlitw (klawisz E):",
            "  Ojcze Nasz - strzela slowami",
            "  5 DMG za slowo, koszt 30 many",
            "",
            "Aniolek:",
            "  Pojawia sie gdy HP <= 20",
            "  Mozesz nadac mu imie!",
            "  Odbija co 10-ty pocisk wroga",
            "",
            "Portale:",
            "  Po zabiciu bossa pojawia sie portal",
            "  Kasa rosnie z kazdym portalem!",
        },
        // Page 6: Tips
        {
            "PORADY",
            "",
            "- Zbieraj kase za zabijanie wrogow",
            "- Kupuj ulepszenia HP i Many w sklepie",
            "- Skin Biskupa kosztuje 1000 kasy",
            "- Uzywaj scroll do zmiany broni",
            "- Leczenie daje niewrazalnosc!",
            "- Kadzidlo jest swietne do bosow",
            "- Biblia jest najlepsza na slabe wrogi",
            "",
            "",
            "Powodzenia!",
        },
    };

    public TutorialScreen(BattleGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, W, H);
        shapeRenderer = new ShapeRenderer();
        page = 0;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.05f, 0.1f, 1f);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            page++;
            if (page >= PAGES.length) {
                game.tutorialDone = true;
                game.saveData();
                game.setScreen(new MenuScreen(game));
                dispose();
                return;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.tutorialDone = true;
            game.saveData();
            game.setScreen(new MenuScreen(game));
            dispose();
            return;
        }

        // Draw panel
        float panelW = 700;
        float panelH = 600;
        float panelX = W / 2f - panelW / 2f;
        float panelY = W / 2f - panelH / 2f - 50;

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.08f, 0.15f, 0.95f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();

        // Draw weapon image on weapon pages
        game.batch.begin();
        if (page >= 1 && page <= 3) {
            float texSize = 80;
            float texX = panelX + panelW - texSize - 30;
            float texY = panelY + panelH - texSize - 30;
            if (page == 1) {
                game.batch.draw(game.playerKropidloTex, texX, texY, texSize, texSize);
                game.batch.draw(game.weaponBulletTex[0], texX - 50, texY + 20, 40, 40);
            } else if (page == 2) {
                game.batch.draw(game.playerKadzidloTex, texX, texY, texSize, texSize);
                game.batch.draw(game.weaponBulletTex[1], texX - 50, texY + 20, 40, 40);
            } else {
                game.batch.draw(game.playerBibliaTex, texX, texY, texSize, texSize);
            }
        }
        // Angel on page 5
        if (page == 5) {
            float texSize = 70;
            game.batch.draw(game.aniolekTex, panelX + panelW - texSize - 30, panelY + panelH - texSize - 30, texSize, texSize);
        }

        // Draw text
        String[] lines = PAGES[page];
        float textX = panelX + 30;
        float textY = panelY + panelH - 30;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (i == 0) {
                // Title
                game.font.getData().setScale(1.1f);
                game.font.setColor(Color.CYAN);
            } else if (line.startsWith("+")) {
                game.font.getData().setScale(0.6f);
                game.font.setColor(Color.GREEN);
            } else if (line.startsWith("-")) {
                game.font.getData().setScale(0.6f);
                game.font.setColor(Color.RED);
            } else {
                game.font.getData().setScale(0.6f);
                game.font.setColor(Color.WHITE);
            }
            game.font.draw(game.batch, line, textX, textY - i * 35);
        }

        // Page indicator & hint
        game.font.getData().setScale(0.5f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, (page + 1) + "/" + PAGES.length, panelX + panelW / 2f - 15, panelY + 25);
        game.font.draw(game.batch, "ENTER - dalej    ESC - pomin", panelX + panelW / 2f - 130, panelY - 10);

        game.font.getData().setScale(1f);
        game.batch.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}