package com.example.demogame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

public class MenuScreen implements Screen {

    private static final int W = BattleGame.WIDTH;
    private static final int H = BattleGame.HEIGHT;

    private static final float BTN_W = 200;
    private static final float BTN_H = 55;

    // Ksiega panel (centered)
    private static final float PANEL_W = 900;
    private static final float PANEL_H = 700;
    private static final float PANEL_X = W / 2f - PANEL_W / 2f;
    private static final float PANEL_Y = H / 2f - PANEL_H / 2f;

    // Left page center (buttons)
    private static final float LEFT_CX = PANEL_X + PANEL_W / 4f + 30;
    private static final float START_BTN_X = LEFT_CX - BTN_W / 2f + 50;
    private static final float START_BTN_Y = PANEL_Y + PANEL_H / 2f + 40;
    private static final float EXIT_BTN_X = LEFT_CX - BTN_W / 2f + 50;
    private static final float EXIT_BTN_Y = START_BTN_Y - BTN_H - 30;

    // Right page center (score + enemies button)
    private static final float RIGHT_CX = PANEL_X + PANEL_W * 3f / 4f - 60;
    private static final float ENEMIES_BTN_X = RIGHT_CX - BTN_W / 2f;
    private static final float ENEMIES_BTN_Y = PANEL_Y + 220;

    // Shop button position (top-right corner)
    private static final float SHOP_BTN_SIZE = 80;
    private static final float SHOP_BTN_X = W - SHOP_BTN_SIZE - 20;
    private static final float SHOP_BTN_Y = H - SHOP_BTN_SIZE - 20;

    // Shop panel
    private static final float SHOP_W = 350;
    private static final float SHOP_X = W - SHOP_W;

    private final BattleGame game;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapeRenderer;
    private boolean enemiesView;
    private int enemiesPage;
    private boolean shopOpen;
    private int shopSpentAmount;
    private float shopSpentTimer;
    private float hpAnimTimer = -1f; // -1 = no animation
    private float czystoscAnimTimer = -1f;
    private boolean firstFrame = true;
    private boolean profileOpen;
    private boolean editingName;
    private String editingNameText = "";
    private boolean creatingProfile;
    private String newProfileName = "";
    private boolean profileListOpen;
    private boolean settingsOpen;
    private boolean confirmReset;
    private boolean classInfoOpen;
    private boolean profilePictureMenuOpen;
    private final Vector3 mouseTemp = new Vector3();

    // Intro animation (triggered when START is pressed)
    private boolean introPlaying = false;
    private float introTimer = 0f;
    private static final float INTRO_DARKEN   = 0.5f;  // fade to black
    private static final float INTRO_TEXT_IN  = 1.8f;  // "Iter Lucis" fades in
    private static final float INTRO_BRIGHTEN = 0.7f;  // fade to clear
    private static final float INTRO_TOTAL    = INTRO_DARKEN + INTRO_TEXT_IN + INTRO_BRIGHTEN;

    // Profile button (top-left corner)
    private static final float PROFILE_BTN_SIZE = 80;
    private static final float PROFILE_BTN_X = 20;
    private static final float PROFILE_BTN_Y = H - PROFILE_BTN_SIZE - 20;

    // Profile panel
    private static final float PROFILE_W = 300;
    private static final float PROFILE_H = 500;
    private static final float PROFILE_X = 0;
    private static final float PROFILE_Y = H - PROFILE_H;

    public MenuScreen(BattleGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, W, H);
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        if (shopSpentTimer > 0) shopSpentTimer -= delta;
        if (hpAnimTimer >= 0) hpAnimTimer += delta;
        if (czystoscAnimTimer >= 0) czystoscAnimTimer += delta;

        if (introPlaying) {
            introTimer += delta;
            drawIntro();
            return;
        }

        // Skip input on first frame to avoid carrying over clicks from previous screen
        if (firstFrame) {
            firstFrame = false;
            drawMainMenu();
            return;
        }

        if (profileOpen) {
            handleProfileInput();
            drawProfileView();
            return;
        }

        if (enemiesView) {
            handleEnemiesInput();
            drawEnemiesView();
            return;
        }

        if (shopOpen) {
            handleShopInput();
            drawShopView();
            return;
        }

        // Input
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.clickSound.play(game.clickVolume);
            startGame();
            return;
        }
        if (Gdx.input.justTouched()) {
            mouseTemp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(mouseTemp);
            float mx = mouseTemp.x;
            float my = mouseTemp.y;

            if (mx >= START_BTN_X && mx <= START_BTN_X + BTN_W
                    && my >= START_BTN_Y && my <= START_BTN_Y + BTN_H) {
                game.clickSound.play(game.clickVolume);
                startGame();
                return;
            }
            if (mx >= EXIT_BTN_X && mx <= EXIT_BTN_X + BTN_W
                    && my >= EXIT_BTN_Y && my <= EXIT_BTN_Y + BTN_H) {
                game.clickSound.play(game.clickVolume);
                Gdx.app.exit();
                return;
            }
            float enemiesTextX = ENEMIES_BTN_X + 30;
            float enemiesTextTop = ENEMIES_BTN_Y + BTN_H / 2f + 50;
            if (mx >= enemiesTextX && mx <= enemiesTextX + 230
                    && my >= enemiesTextTop - 40 && my <= enemiesTextTop) {
                game.clickSound.play(game.clickVolume);
                enemiesView = true;
                enemiesPage = 0;
            }
            // Shop button click
            if (mx >= SHOP_BTN_X && mx <= SHOP_BTN_X + SHOP_BTN_SIZE
                    && my >= SHOP_BTN_Y && my <= SHOP_BTN_Y + SHOP_BTN_SIZE) {
                game.clickSound.play(game.clickVolume);
                shopOpen = true;
            }
            // Profile button click
            if (mx >= PROFILE_BTN_X && mx <= PROFILE_BTN_X + PROFILE_BTN_SIZE
                    && my >= PROFILE_BTN_Y && my <= PROFILE_BTN_Y + PROFILE_BTN_SIZE) {
                game.clickSound.play(game.clickVolume);
                profileOpen = true;
                editingName = false;
            }
        }

        drawMainMenu();
    }

    private void drawMainMenu() {
        game.batch.begin();
        game.batch.draw(game.startBgTex, 0, 0, W, H);

        game.menuFont.setColor(Color.BLACK);
        game.menuFont.draw(game.batch, "START", START_BTN_X + BTN_W / 2f - 55, START_BTN_Y + BTN_H / 2f + 18);
        game.menuFont.draw(game.batch, "EXIT", EXIT_BTN_X + BTN_W / 2f - 75, EXIT_BTN_Y + BTN_H / 2f + 18);

        game.font.setColor(Color.BLACK);
        game.font.getData().setScale(0.9f);
        if (game.highScore > 0) {
            game.font.draw(game.batch, "Najlepszy wynik:", RIGHT_CX - 100, PANEL_Y + PANEL_H / 2f + 160);
            game.font.getData().setScale(1.3f);
            game.font.draw(game.batch, "" + game.highScore, RIGHT_CX - 20, PANEL_Y + PANEL_H / 2f + 125);
        } else {
            game.font.draw(game.batch, "Brak wyniku", RIGHT_CX - 70, PANEL_Y + PANEL_H / 2f + 140);
        }

        game.font.getData().setScale(1.2f);
        game.font.setColor(Color.BLACK);
        game.font.draw(game.batch, "BOSSY", ENEMIES_BTN_X + 30, ENEMIES_BTN_Y + BTN_H / 2f + 50);

        // Shop button (graphic)
        game.batch.draw(game.sklepTex, SHOP_BTN_X, SHOP_BTN_Y, SHOP_BTN_SIZE, SHOP_BTN_SIZE);

        // Profile button (top-left)
        game.batch.end();
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 0.8f);
        shapeRenderer.rect(PROFILE_BTN_X, PROFILE_BTN_Y, PROFILE_BTN_SIZE, PROFILE_BTN_SIZE);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(PROFILE_BTN_X, PROFILE_BTN_Y, PROFILE_BTN_SIZE, PROFILE_BTN_SIZE);
        shapeRenderer.end();
        game.batch.begin();
        game.font.setColor(game.getPlayerClassColor());
        game.font.getData().setScale(0.5f);
        game.font.draw(game.batch, game.playerName, PROFILE_BTN_X + 5, PROFILE_BTN_Y + PROFILE_BTN_SIZE / 2f + 8);

        game.font.getData().setScale(1f);
        game.batch.end();
    }

    private void handleShopInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.clickSound.play(game.clickVolume);
            shopOpen = false;
            return;
        }
        if (Gdx.input.justTouched()) {
            mouseTemp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(mouseTemp);
            float mx = mouseTemp.x;
            float my = mouseTemp.y;

            float itemSize = 100;
            float itemX = SHOP_X + SHOP_W / 2f - itemSize / 2f;

            // HP upgrade click
            float hpY = H - 220;
            if (hpAnimTimer < 0 && mx >= itemX && mx <= itemX + itemSize
                    && my >= hpY && my <= hpY + itemSize
                    && game.money >= game.hpUpgradePrice) {
                game.clickSound.play(game.clickVolume);
                game.money -= game.hpUpgradePrice;
                shopSpentAmount = game.hpUpgradePrice;
                shopSpentTimer = 2f;
                hpAnimTimer = 0f;
                // Increase price and amount by 1/4
                game.hpUpgradePrice += game.hpUpgradePrice / 4;
                game.hpUpgradeAmount += game.hpUpgradeAmount / 4;
                game.saveData();
            }

            // Mana upgrade click
            float manaY = H - 380;
            if (czystoscAnimTimer < 0 && mx >= itemX && mx <= itemX + itemSize
                    && my >= manaY && my <= manaY + itemSize
                    && game.money >= game.czystoscUpgradePrice) {
                game.clickSound.play(game.clickVolume);
                game.money -= game.czystoscUpgradePrice;
                shopSpentAmount = game.czystoscUpgradePrice;
                shopSpentTimer = 2f;
                czystoscAnimTimer = 0f;
                // Increase price and amount by 1/4
                game.czystoscUpgradePrice += game.czystoscUpgradePrice / 4;
                game.czystoscUpgradeAmount += game.czystoscUpgradeAmount / 4;
                game.saveData();
            }

            // Click outside shop panel closes it
            if (mx < SHOP_X) {
                game.clickSound.play(game.clickVolume);
                shopOpen = false;
            }
        }
    }

    private void drawShopView() {
        // Background
        game.batch.begin();
        game.batch.draw(game.startBgTex, 0, 0, W, H);
        game.batch.end();

        // Dim overlay
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, W, H);
        shapeRenderer.end();

        // Shop panel
        shapeRenderer.setColor(0.15f, 0.1f, 0.05f, 0.95f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(SHOP_X, 0, SHOP_W, H);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(SHOP_X, 0, SHOP_W, H);
        shapeRenderer.end();

        game.batch.begin();

        // Kasa icon + money in top-left of shop
        float kasaSize = 60;
        game.batch.draw(game.kasaTex, SHOP_X + 10, H - kasaSize - 15, kasaSize, kasaSize);
        game.font.getData().setScale(1f);
        game.font.setColor(Color.YELLOW);
        game.font.draw(game.batch, "" + game.money, SHOP_X + kasaSize + 20, H - 25);

        // Red spent text
        if (shopSpentTimer > 0) {
            game.font.getData().setScale(0.8f);
            game.font.setColor(Color.RED);
            game.font.draw(game.batch, "-" + shopSpentAmount, SHOP_X + kasaSize + 20, H - 55);
        }

        // Title
        game.font.getData().setScale(1.2f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "SKLEP", SHOP_X + SHOP_W / 2f - 50, H - 90);

        // HP upgrade box (with grow animation)
        float fullSize = 100;
        float itemX = SHOP_X + SHOP_W / 2f - fullSize / 2f;
        float hpY = H - 220;
        if (hpAnimTimer >= 0 && hpAnimTimer < 1.5f) {
            // Growing animation
            float progress = Math.min(1f, hpAnimTimer / 1f);
            float scale = 0.04f + 0.96f * progress * progress;
            float hpSize = fullSize * scale;
            float offsetX = (fullSize - hpSize) / 2f;
            float offsetY = (fullSize - hpSize) / 2f;
            game.batch.draw(game.hpUpgradeTex, itemX + offsetX, hpY + offsetY, hpSize, hpSize);
        } else {
            if (hpAnimTimer >= 1.5f) hpAnimTimer = -1f;
            game.batch.draw(game.hpUpgradeTex, itemX, hpY, fullSize, fullSize);
        }
        game.font.getData().setScale(0.8f);
        game.font.setColor(game.money >= game.hpUpgradePrice ? Color.GREEN : Color.RED);
        game.font.draw(game.batch, "Cena: " + game.hpUpgradePrice, itemX, hpY - 5);
        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(0.6f);
        game.font.draw(game.batch, "+" + game.hpUpgradeAmount + " HP", itemX + 10, hpY - 25);

        // Mana upgrade box (with grow animation)
        float manaY = H - 380;
        if (czystoscAnimTimer >= 0 && czystoscAnimTimer < 1.5f) {
            float progress = Math.min(1f, czystoscAnimTimer / 1f);
            float scale = 0.04f + 0.96f * progress * progress;
            float manaSize = fullSize * scale;
            float offsetX = (fullSize - manaSize) / 2f;
            float offsetY = (fullSize - manaSize) / 2f;
            game.batch.draw(game.czystoscUpgradeTex, itemX + offsetX, manaY + offsetY, manaSize, manaSize);
        } else {
            if (czystoscAnimTimer >= 1.5f) czystoscAnimTimer = -1f;
            game.batch.draw(game.czystoscUpgradeTex, itemX, manaY, fullSize, fullSize);
        }
        game.font.getData().setScale(0.8f);
        game.font.setColor(game.money >= game.czystoscUpgradePrice ? Color.GREEN : Color.RED);
        game.font.draw(game.batch, "Cena: " + game.czystoscUpgradePrice, itemX, manaY - 5);
        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(0.6f);
        game.font.draw(game.batch, "+" + game.czystoscUpgradeAmount + " Czystosc", itemX + 10, manaY - 25);

        // Bishop skin — unlocked at gold class
        float skinY = H - 540;
        game.batch.draw(game.biskupTex, itemX, skinY, fullSize, fullSize);
        if (game.bishopSkin) {
            game.font.getData().setScale(0.7f);
            game.font.setColor(Color.GREEN);
            game.font.draw(game.batch, "ODBLOKOWANO", itemX - 5, skinY - 5);
        } else {
            game.font.getData().setScale(0.55f);
            game.font.setColor(new Color(1f, 0.84f, 0f, 1f));
            game.font.draw(game.batch, "Osiagnij klase", itemX - 5, skinY - 5);
            game.font.draw(game.batch, "Zlota!", itemX + 20, skinY - 25);
        }
        game.font.getData().setScale(0.5f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "Ksiadz Biskup", itemX, skinY - 45);

        // Close hint
        game.font.getData().setScale(0.5f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "ESC - zamknij", SHOP_X + SHOP_W / 2f - 50, 30);

        game.font.getData().setScale(1f);
        game.batch.end();
    }

    private void handleEnemiesInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.clickSound.play(game.clickVolume);
            enemiesView = false;
        }
        int maxPage = visibleBossCount() - 1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) && enemiesPage < maxPage) {
            game.clickSound.play(game.clickVolume);
            enemiesPage++;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && enemiesPage > 0) {
            game.clickSound.play(game.clickVolume);
            enemiesPage--;
        }
        if (Gdx.input.justTouched()) {
            game.clickSound.play(game.clickVolume);
            enemiesView = false;
        }
    }

    private void drawEnemiesView() {
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Background + image in single batch
        game.batch.begin();
        game.batch.draw(game.startBgTex, 0, 0, W, H);
        game.batch.end();

        // Dim overlay
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, W, H);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        // Enemy image + hints in single batch
        float imgW = W * 0.7f;
        float imgH = H * 0.7f;
        float imgX = (W - imgW) / 2f;
        float imgY = (H - imgH) / 2f;

        game.batch.begin();
        com.badlogic.gdx.graphics.Texture[] visibleTex = visibleBossTextures();
        if (visibleTex.length == 0) {
            game.font.getData().setScale(0.8f);
            game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, "Nie spotkales jeszcze zadnego bossa!", W / 2f - 250, H / 2f);
            game.font.getData().setScale(1f);
        } else {
            game.batch.draw(visibleTex[enemiesPage], imgX, imgY, imgW, imgH);
        }
        game.batch.end();
    }

    private com.badlogic.gdx.graphics.Texture[] visibleBossTextures() {
        int count = visibleBossCount();
        com.badlogic.gdx.graphics.Texture[] result = new com.badlogic.gdx.graphics.Texture[count];
        int i = 0;
        if (game.seenIwonka) result[i++] = game.przedstawienieIwonkaTex;
        if (game.seenDemon)  result[i++] = game.przedstawienieTex;
        if (game.seenTelefon) result[i++] = game.przedstawienieTelefonTex;
        return result;
    }

    private int visibleBossCount() {
        int count = 0;
        if (game.seenIwonka) count++;
        if (game.seenDemon)  count++;
        if (game.seenTelefon) count++;
        return count;
    }

    private void handleProfileInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (confirmReset) {
                confirmReset = false;
            } else if (settingsOpen) {
                settingsOpen = false;
            } else if (creatingProfile) {
                creatingProfile = false;
            } else if (editingName) {
                editingName = false;
            } else if (profileListOpen) {
                profileListOpen = false;
            } else if (profilePictureMenuOpen) {
                profilePictureMenuOpen = false;
            } else if (classInfoOpen) {
                classInfoOpen = false;
            } else {
                game.clickSound.play(game.clickVolume);
                profileOpen = false;
            }
            return;
        }
        // Settings input
        if (settingsOpen) {
            handleSettingsInput();
            return;
        }
        // Creating new profile - name input
        if (creatingProfile) {
            handleTextInput(true);
            return;
        }
        if (editingName) {
            handleTextInput(false);
            return;
        }
        // Profile list - click to switch
        if (profileListOpen) {
            if (Gdx.input.justTouched()) {
                mouseTemp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(mouseTemp);
                float mx = mouseTemp.x;
                float my = mouseTemp.y;
                float listW = 350;
                float listH = 80 + game.profileCount * 40;
                float listX = W / 2f - listW / 2f;
                float listY = H / 2f - listH / 2f;
                float listTop = listY + listH;
                for (int i = 0; i < game.profileCount; i++) {
                    float itemY = listTop - 55 - i * 40;
                    if (mx >= listX && mx <= listX + listW && my >= itemY - 10 && my <= itemY + 25) {
                        game.clickSound.play(game.clickVolume);
                        game.saveData();
                        game.loadProfile(i);
                        profileListOpen = false;
                        return;
                    }
                }
                // Click outside profile list closes it
                if (mx < listX || mx > listX + listW || my < listY || my > listY + listH) {
                    game.clickSound.play(game.clickVolume);
                    profileListOpen = false;
                }
            }
            return;
        }
        if (classInfoOpen) {
            if (Gdx.input.justTouched()) {
                mouseTemp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(mouseTemp);
                float mx = mouseTemp.x;
                float my = mouseTemp.y;
                float boxW = 370;
                float boxH = 220;
                float boxX = W / 2f - boxW / 2f;
                float boxY = H / 2f - boxH / 2f;
                if (mx < boxX || mx > boxX + boxW || my < boxY || my > boxY + boxH) {
                    game.clickSound.play(game.clickVolume);
                    classInfoOpen = false;
                }
            }
            return;
        }
        if (profilePictureMenuOpen) {
            if (Gdx.input.justTouched()) {
                mouseTemp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(mouseTemp);
                float mx = mouseTemp.x;
                float my = mouseTemp.y;
                float menuW = 450;
                float menuH = 500;
                float menuX = W / 2f - menuW / 2f;
                float menuY = H / 2f - menuH / 2f;
                if (game.profilePictureIndex == -1) {
                    float picSize = 160;
                    float colX1 = W / 2f - picSize - 10;
                    float colX2 = W / 2f + 10;
                    float rowY1 = H / 2f + 40;
                    float rowY2 = H / 2f - picSize - 20;
                    for (int pi = 0; pi < 4; pi++) {
                        float picX = (pi % 2 == 0) ? colX1 : colX2;
                        float picY = (pi < 2) ? rowY1 : rowY2;
                        if (mx >= picX && mx <= picX + picSize && my >= picY && my <= picY + picSize) {
                            game.clickSound.play(game.clickVolume);
                            game.profilePictureIndex = pi;
                            game.saveData();
                            profilePictureMenuOpen = false;
                            return;
                        }
                    }
                } else {
                    int currentIdx = game.profilePictureIndex;
                    int[] others = new int[3];
                    int oi = 0;
                    for (int pi = 0; pi < 4; pi++) {
                        if (pi != currentIdx) others[oi++] = pi;
                    }
                    float smallSize = 90;
                    float gap = 20;
                    float totalW = 3 * smallSize + 2 * gap;
                    float startX = W / 2f - totalW / 2f;
                    float smallY = H / 2f - 100;
                    for (int i = 0; i < 3; i++) {
                        float sx = startX + i * (smallSize + gap);
                        if (mx >= sx && mx <= sx + smallSize && my >= smallY && my <= smallY + smallSize) {
                            game.clickSound.play(game.clickVolume);
                            game.profilePictureIndex = others[i];
                            game.saveData();
                            profilePictureMenuOpen = false;
                            return;
                        }
                    }
                }
                if (mx < menuX || mx > menuX + menuW || my < menuY || my > menuY + menuH) {
                    profilePictureMenuOpen = false;
                }
            }
            return;
        }
        if (Gdx.input.justTouched()) {
            mouseTemp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(mouseTemp);
            float mx = mouseTemp.x;
            float my = mouseTemp.y;
            float top = PROFILE_Y + PROFILE_H;
            // Profile picture area click
            float picAreaSize = 70;
            float picAreaX = PROFILE_X + PROFILE_W - picAreaSize - 10;
            float picAreaY = top - picAreaSize - 10;
            if (mx >= picAreaX && mx <= picAreaX + picAreaSize && my >= picAreaY && my <= picAreaY + picAreaSize) {
                game.clickSound.play(game.clickVolume);
                profilePictureMenuOpen = true;
                return;
            }
            // Click on name area to edit
            float nameY = top - 80;
            if (mx >= PROFILE_X + 15 && mx <= PROFILE_X + PROFILE_W - 15
                    && my >= nameY - 20 && my <= nameY + 20) {
                game.clickSound.play(game.clickVolume);
                editingName = true;
                editingNameText = game.playerName;
            }
            // Click on class area to show class info
            float classY = top - 85;
            if (mx >= PROFILE_X + 15 && mx <= PROFILE_X + PROFILE_W - 15
                    && my >= classY - 20 && my <= classY + 10) {
                game.clickSound.play(game.clickVolume);
                classInfoOpen = true;
            }
            // "+" button (bottom-right of profile panel)
            float plusSize = 35;
            float plusX = PROFILE_X + PROFILE_W - plusSize - 10;
            float plusY = PROFILE_Y + 10;
            if (mx >= plusX && mx <= plusX + plusSize
                    && my >= plusY && my <= plusY + plusSize) {
                game.clickSound.play(game.clickVolume);
                creatingProfile = true;
                newProfileName = "";
            }
            // Profile list button area (below high score)
            float listBtnY = top - 330;
            if (mx >= PROFILE_X + 15 && mx <= PROFILE_X + PROFILE_W - 15
                    && my >= listBtnY - 10 && my <= listBtnY + 20) {
                game.clickSound.play(game.clickVolume);
                profileListOpen = true;
            }
            // Settings button area
            float settingsBtnY = top - 365;
            if (mx >= PROFILE_X + 15 && mx <= PROFILE_X + PROFILE_W - 15
                    && my >= settingsBtnY - 10 && my <= settingsBtnY + 20) {
                game.clickSound.play(game.clickVolume);
                settingsOpen = true;
                confirmReset = false;
            }

            // Click outside profile panel closes it
            if (mx > PROFILE_X + PROFILE_W || my < PROFILE_Y || my > PROFILE_Y + PROFILE_H) {
                game.clickSound.play(game.clickVolume);
                profileOpen = false;
            }
        }
    }

    private void handleTextInput(boolean isNewProfile) {
        String text = isNewProfile ? newProfileName : editingNameText;
        for (int key = Input.Keys.A; key <= Input.Keys.Z; key++) {
            if (Gdx.input.isKeyJustPressed(key) && text.length() < 11) {
                text += Input.Keys.toString(key);
            }
        }
        for (int key = Input.Keys.NUM_0; key <= Input.Keys.NUM_9; key++) {
            if (Gdx.input.isKeyJustPressed(key) && text.length() < 11) {
                text += Input.Keys.toString(key);
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && text.length() < 11) {
            text += " ";
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && text.length() > 0) {
            text = text.substring(0, text.length() - 1);
        }
        if (isNewProfile) {
            newProfileName = text;
        } else {
            editingNameText = text;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && text.length() > 0) {
            if (isNewProfile) {
                game.saveData();
                game.createNewProfile(newProfileName);
                creatingProfile = false;
                game.setScreen(new TutorialScreen(game));
                dispose();
                return;
            } else {
                game.playerName = editingNameText;
                game.saveData();
                editingName = false;
            }
        }
    }

    private void drawProfileView() {
        // Background
        game.batch.begin();
        game.batch.draw(game.startBgTex, 0, 0, W, H);
        game.batch.end();

        // Dim overlay
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, W, H);
        shapeRenderer.end();

        // Profile panel
        shapeRenderer.setColor(0.12f, 0.1f, 0.15f, 0.95f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(PROFILE_X, PROFILE_Y, PROFILE_W, PROFILE_H);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(PROFILE_X, PROFILE_Y, PROFILE_W, PROFILE_H);
        shapeRenderer.end();

        // Profile picture slot (top-right of panel)
        float picAreaSize = 70;
        float picAreaX = PROFILE_X + PROFILE_W - picAreaSize - 10;
        float picAreaY = PROFILE_Y + PROFILE_H - picAreaSize - 10;
        if (game.profilePictureIndex < 0) {
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.2f, 0.2f, 0.4f, 0.8f);
            shapeRenderer.rect(picAreaX, picAreaY, picAreaSize, picAreaSize);
            shapeRenderer.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
        }
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.7f, 0.7f, 1f, 1f);
        shapeRenderer.rect(picAreaX, picAreaY, picAreaSize, picAreaSize);
        shapeRenderer.end();

        // "+" button (bottom-right of profile panel)
        float plusSize = 35;
        float plusX = PROFILE_X + PROFILE_W - plusSize - 10;
        float plusY = PROFILE_Y + 10;
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.6f, 0.2f, 0.9f);
        shapeRenderer.rect(plusX, plusY, plusSize, plusSize);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(plusX, plusY, plusSize, plusSize);
        shapeRenderer.end();

        game.batch.begin();

        // "+" text
        game.font.getData().setScale(1f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "+", plusX + 8, plusY + plusSize - 5);

        float px = PROFILE_X + 15;
        float top = PROFILE_Y + PROFILE_H;

        // Title (hide if player has custom name)
        if (game.playerName.equals("Gracz")) {
            game.font.getData().setScale(0.9f);
            game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, "PROFIL", px, top - 20);
        }

        // Player name with class color
        Color classColor = game.getPlayerClassColor();
        game.font.getData().setScale(0.8f);
        game.font.setColor(classColor);
        if (editingName) {
            game.font.draw(game.batch, editingNameText + "_", px, top - 60);
        } else {
            game.font.draw(game.batch, game.playerName, px, top - 60);
        }

        // Class name
        game.font.getData().setScale(0.5f);
        game.font.setColor(classColor);
        game.font.draw(game.batch, "Klasa: " + game.getPlayerClassName(), px, top - 85);

        // Skin
        game.font.getData().setScale(0.6f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Skin:", px, top - 115);
        float skinSize = 50;
        if (game.bishopSkin) {
            game.batch.draw(game.biskupTex, px, top - 185, skinSize, skinSize);
            game.font.getData().setScale(0.45f);
            game.font.draw(game.batch, "Biskup", px, top - 190);
        } else {
            game.batch.draw(game.playerRightTex, px, top - 185, skinSize, skinSize);
            game.font.getData().setScale(0.45f);
            game.font.draw(game.batch, "Ksiadz", px, top - 190);
        }

        // Money
        game.font.getData().setScale(0.6f);
        game.font.setColor(Color.YELLOW);
        game.font.draw(game.batch, "Kasa: " + game.money, px, top - 220);

        // High score
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Najlepszy wynik:", px, top - 260);
        game.font.getData().setScale(0.7f);
        game.font.draw(game.batch, "" + game.highScore, px, top - 285);

        // Switch profile button
        game.font.getData().setScale(0.45f);
        game.font.setColor(Color.CYAN);
        game.font.draw(game.batch, "Zmien profil (" + game.profileCount + ")", px, top - 320);

        // Settings button
        game.font.getData().setScale(0.45f);
        game.font.setColor(Color.YELLOW);
        game.font.draw(game.batch, "Ustawienia", px, top - 355);

        // Hints
        game.font.getData().setScale(0.35f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "Kliknij nazwe aby zmienic", px, PROFILE_Y + 55);
        game.font.draw(game.batch, "ESC - zamknij", px, PROFILE_Y + 35);

        game.font.getData().setScale(1f);
        game.batch.end();

        // Draw overlays
        if (creatingProfile) {
            drawNewProfileOverlay();
        }
        if (profileListOpen) {
            drawProfileListOverlay();
        }
        if (settingsOpen) {
            drawSettingsOverlay();
        }
        if (classInfoOpen) {
            drawClassInfoOverlay();
        }
    }

    private void drawClassInfoOverlay() {
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.8f);
        shapeRenderer.rect(0, 0, W, H);
        shapeRenderer.end();

        float boxW = 370;
        float boxH = 220;
        float boxX = W / 2f - boxW / 2f;
        float boxY = H / 2f - boxH / 2f;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.08f, 0.15f, 1f);
        shapeRenderer.rect(boxX, boxY, boxW, boxH);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(boxX, boxY, boxW, boxH);
        shapeRenderer.end();

        game.batch.begin();
        float tx = boxX + 20;
        float ty = boxY + boxH - 20;

        game.font.getData().setScale(0.7f);
        game.font.setColor(Color.YELLOW);
        game.font.draw(game.batch, "KLASY", tx, ty);

        float lineH = 28;
        game.font.getData().setScale(0.55f);

        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "None      :       0 - 99 999 pkt", tx, ty - lineH);

        game.font.setColor(new Color(0.2f, 0.9f, 0.2f, 1f));
        game.font.draw(game.batch, "Zielony   :  100 000 - 199 999 pkt", tx, ty - lineH * 2);

        game.font.setColor(new Color(1f, 0.4f, 0.7f, 1f));
        game.font.draw(game.batch, "Rozowy    :  200 000 - 249 999 pkt", tx, ty - lineH * 3);

        game.font.setColor(new Color(0.6f, 0.2f, 0.8f, 1f));
        game.font.draw(game.batch, "Fioletowy :  250 000 - 299 999 pkt", tx, ty - lineH * 4);

        game.font.setColor(new Color(1f, 0.84f, 0f, 1f));
        game.font.draw(game.batch, "Zloty     :  300 000+ pkt", tx, ty - lineH * 5);

        game.font.getData().setScale(0.4f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "ESC - zamknij", tx, boxY + 15);

        game.font.getData().setScale(1f);
        game.batch.end();
    }

    private void drawNewProfileOverlay() {
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.8f);
        shapeRenderer.rect(0, 0, W, H);
        shapeRenderer.end();

        float boxW = 400;
        float boxH = 150;
        float boxX = W / 2f - boxW / 2f;
        float boxY = H / 2f - boxH / 2f;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.12f, 0.2f, 1f);
        shapeRenderer.rect(boxX, boxY, boxW, boxH);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(boxX, boxY, boxW, boxH);
        shapeRenderer.end();

        game.batch.begin();
        game.font.getData().setScale(0.8f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Nowy profil - wpisz nazwe:", boxX + 20, boxY + boxH - 25);

        game.font.getData().setScale(0.9f);
        game.font.setColor(Color.GREEN);
        game.font.draw(game.batch, newProfileName + "_", boxX + 20, boxY + boxH - 70);

        game.font.getData().setScale(0.4f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "ENTER - zatwierdz   ESC - anuluj", boxX + 20, boxY + 20);
        game.font.getData().setScale(1f);
        game.batch.end();
    }

    private void drawProfileListOverlay() {
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.8f);
        shapeRenderer.rect(0, 0, W, H);
        shapeRenderer.end();

        float listW = 350;
        float listH = 80 + game.profileCount * 40;
        float listX = W / 2f - listW / 2f;
        float listY = H / 2f - listH / 2f;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.12f, 0.2f, 1f);
        shapeRenderer.rect(listX, listY, listW, listH);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(listX, listY, listW, listH);
        shapeRenderer.end();

        game.batch.begin();
        game.font.getData().setScale(0.8f);
        game.font.setColor(Color.WHITE);
        float listTop = listY + listH;
        game.font.draw(game.batch, "Wybierz profil:", listX + 20, listTop - 20);

        game.font.getData().setScale(0.6f);
        for (int i = 0; i < game.profileCount; i++) {
            float itemY = listTop - 55 - i * 40;
            String name = game.getProfileName(i);
            int score = game.getProfileHighScore(i);
            boolean isCurrent = (i == game.currentProfileId);
            game.font.setColor(isCurrent ? Color.GREEN : Color.WHITE);
            String prefix = isCurrent ? "> " : "  ";
            game.font.draw(game.batch, prefix + name + "  (" + score + ")", listX + 20, itemY + 20);
        }

        game.font.getData().setScale(0.35f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "ESC - zamknij", listX + 20, listY + 10);
        game.font.getData().setScale(1f);
        game.batch.end();
    }

    private void handleSettingsInput() {
        if (confirmReset) {
            if (Gdx.input.justTouched()) {
                mouseTemp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(mouseTemp);
                float mx = mouseTemp.x;
                float my = mouseTemp.y;
                float boxW = 400;
                float boxX = W / 2f - boxW / 2f;
                float boxY = H / 2f - 50;
                // "TAK" button
                if (mx >= boxX + 30 && mx <= boxX + 130 && my >= boxY + 15 && my <= boxY + 50) {
                    game.clickSound.play(game.clickVolume);
                    game.resetCurrentProfile();
                    confirmReset = false;
                    settingsOpen = false;
                }
                // "NIE" button
                if (mx >= boxX + 170 && mx <= boxX + 270 && my >= boxY + 15 && my <= boxY + 50) {
                    game.clickSound.play(game.clickVolume);
                    confirmReset = false;
                }
                // Click outside confirm box closes it
                float boxH = 120;
                if (mx < boxX || mx > boxX + boxW || my < boxY || my > boxY + boxH) {
                    confirmReset = false;
                }
            }
            return;
        }
        if (Gdx.input.justTouched()) {
            mouseTemp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(mouseTemp);
            float mx = mouseTemp.x;
            float my = mouseTemp.y;
            float panelW = 450;
            float panelH = 400;
            float panelX = W / 2f - panelW / 2f;
            float panelY = H / 2f - panelH / 2f;
            float px = panelX + 25;

            // Music volume: "-" and "+" buttons
            float musicY = panelY + panelH - 100;
            if (mx >= px && mx <= px + 30 && my >= musicY - 5 && my <= musicY + 25) {
                game.clickSound.play(game.clickVolume);
                game.musicVolume = Math.max(0, game.musicVolume - 0.1f);
                game.backgroundMusic.setVolume(game.musicVolume);
                game.saveData();
            }
            if (mx >= px + 180 && mx <= px + 210 && my >= musicY - 5 && my <= musicY + 25) {
                game.clickSound.play(game.clickVolume);
                game.musicVolume = Math.min(1f, game.musicVolume + 0.1f);
                game.backgroundMusic.setVolume(game.musicVolume);
                game.saveData();
            }

            // Click volume: "-" and "+" buttons
            float clickY = musicY - 60;
            if (mx >= px && mx <= px + 30 && my >= clickY - 5 && my <= clickY + 25) {
                game.clickSound.play(game.clickVolume);
                game.clickVolume = Math.max(0, game.clickVolume - 0.1f);
                game.saveData();
            }
            if (mx >= px + 180 && mx <= px + 210 && my >= clickY - 5 && my <= clickY + 25) {
                game.clickVolume = Math.min(1f, game.clickVolume + 0.1f);
                game.clickSound.play(game.clickVolume);
                game.saveData();
            }

            // Fullscreen toggle
            float fullY = clickY - 60;
            if (mx >= px && mx <= px + 300 && my >= fullY - 5 && my <= fullY + 25) {
                game.clickSound.play(game.clickVolume);
                if (Gdx.graphics.isFullscreen()) {
                    Gdx.graphics.setWindowedMode(BattleGame.WIDTH, BattleGame.HEIGHT);
                } else {
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                }
            }

            // Reset button
            float resetY = fullY - 80;
            if (mx >= px && mx <= px + 250 && my >= resetY - 5 && my <= resetY + 25) {
                game.clickSound.play(game.clickVolume);
                confirmReset = true;
            }

            // Click outside settings panel closes it
            if (mx < panelX || mx > panelX + panelW || my < panelY || my > panelY + panelH) {
                game.clickSound.play(game.clickVolume);
                settingsOpen = false;
            }
        }
    }

    private void drawSettingsOverlay() {
        float panelW = 450;
        float panelH = 400;
        float panelX = W / 2f - panelW / 2f;
        float panelY = H / 2f - panelH / 2f;

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.8f);
        shapeRenderer.rect(0, 0, W, H);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.12f, 0.1f, 0.18f, 1f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();

        game.batch.begin();
        float px = panelX + 25;
        float top = panelY + panelH;

        // Title
        game.font.getData().setScale(1f);
        game.font.setColor(Color.YELLOW);
        game.font.draw(game.batch, "USTAWIENIA", px, top - 30);

        // Music volume
        float musicY = top - 100;
        game.font.getData().setScale(0.6f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Muzyka:", px, musicY + 30);
        int musicPct = Math.round(game.musicVolume * 100);
        game.font.setColor(Color.CYAN);
        game.font.draw(game.batch, "[-]", px, musicY);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "" + musicPct + "%", px + 70, musicY);
        game.font.setColor(Color.CYAN);
        game.font.draw(game.batch, "[+]", px + 180, musicY);

        // Click volume
        float clickY = musicY - 60;
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Dzwieki:", px, clickY + 30);
        int clickPct = Math.round(game.clickVolume * 100);
        game.font.setColor(Color.CYAN);
        game.font.draw(game.batch, "[-]", px, clickY);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "" + clickPct + "%", px + 70, clickY);
        game.font.setColor(Color.CYAN);
        game.font.draw(game.batch, "[+]", px + 180, clickY);

        // Fullscreen
        float fullY = clickY - 60;
        game.font.setColor(Color.WHITE);
        String fullText = Gdx.graphics.isFullscreen() ? "Tryb okna" : "Pelny ekran";
        game.font.draw(game.batch, "[ " + fullText + " ]", px, fullY);

        // Reset
        float resetY = fullY - 80;
        game.font.getData().setScale(0.6f);
        game.font.setColor(Color.RED);
        game.font.draw(game.batch, "[ RESETUJ PROFIL ]", px, resetY);

        // Hint
        game.font.getData().setScale(0.35f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "ESC - zamknij", px, panelY + 15);

        game.font.getData().setScale(1f);
        game.batch.end();

        // Confirm reset overlay
        if (confirmReset) {
            drawConfirmResetOverlay();
        }
    }

    private void drawConfirmResetOverlay() {
        float boxW = 400;
        float boxH = 120;
        float boxX = W / 2f - boxW / 2f;
        float boxY = H / 2f - boxH / 2f;

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.5f);
        shapeRenderer.rect(0, 0, W, H);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.05f, 0.05f, 1f);
        shapeRenderer.rect(boxX, boxY, boxW, boxH);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(boxX, boxY, boxW, boxH);
        shapeRenderer.end();

        game.batch.begin();
        game.font.getData().setScale(0.7f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Czy na pewno chcesz", boxX + 40, boxY + boxH - 20);
        game.font.draw(game.batch, "zresetowac profil?", boxX + 50, boxY + boxH - 50);

        game.font.getData().setScale(0.8f);
        game.font.setColor(Color.RED);
        game.font.draw(game.batch, "TAK", boxX + 50, boxY + 35);
        game.font.setColor(Color.GREEN);
        game.font.draw(game.batch, "NIE", boxX + 190, boxY + 35);

        game.font.getData().setScale(1f);
        game.batch.end();
    }

    private void startGame() {
        Gdx.input.setCursorPosition(BattleGame.WIDTH / 2, BattleGame.HEIGHT / 2);
        introPlaying = true;
        introTimer = 0f;
        game.backgroundMusic.stop();
    }

    private void drawIntro() {
        // Phase boundaries
        float phase2Start = INTRO_DARKEN;
        float phase3Start = INTRO_DARKEN + INTRO_TEXT_IN;

        float overlayAlpha;
        float textAlpha;

        if (introTimer < phase2Start) {
            // Phase 1: menu visible, black overlay fades in
            overlayAlpha = introTimer / INTRO_DARKEN;
            textAlpha = 0f;
        } else if (introTimer < phase3Start) {
            // Phase 2: black screen, "Iter Lucis" fades in
            overlayAlpha = 1f;
            textAlpha = (introTimer - phase2Start) / INTRO_TEXT_IN;
        } else if (introTimer < INTRO_TOTAL) {
            // Phase 3: black fades out while text stays
            overlayAlpha = 1f - (introTimer - phase3Start) / INTRO_BRIGHTEN;
            textAlpha = 1f;
        } else {
            // Done — launch game
            game.backgroundMusic.play();
            game.setScreen(new BattleScreen(game));
            dispose();
            return;
        }

        // Phase 1: draw menu behind the overlay
        if (introTimer < phase2Start) {
            drawMainMenu();
        }

        // Black overlay
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, overlayAlpha);
        shapeRenderer.rect(0, 0, W, H);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        // "Iter Lucis" text
        if (textAlpha > 0f) {
            game.batch.begin();
            game.menuFont.setColor(1f, 1f, 1f, textAlpha);
            game.menuFont.getData().setScale(1.6f);
            com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.menuFont, "Iter Lucis");
            game.menuFont.draw(game.batch, "Iter Lucis", W / 2f - layout.width / 2f, H / 2f + layout.height / 2f);
            game.menuFont.getData().setScale(1f);
            game.menuFont.setColor(Color.WHITE);
            game.batch.end();
        }
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