package com.example.demogame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Hub — bezpieczna strefa startowa z dwoma portalami i sklepem.
 * Portal lewy  (fioletowy) → tryb VS 2 graczy
 * Portal prawy (pomaranczowy) → swiat bossow (dialog: 1 gracz / 2 graczy CO-OP)
 * Sklep (ikona bobująca) → ulepszenia HP / czystosci / skory
 */
public class HubScreen implements Screen {

    private static final int   W        = BattleGame.WIDTH;   // 1300
    private static final int   H        = BattleGame.HEIGHT;  // 1000
    private static final int   WORLD_W  = 1800;

    // Player physics (same tuning as BattleScreen)
    private static final float PLAYER_W    = 300f;
    private static final float PLAYER_H    = 160f;
    private static final float PLAYER_SPEED = 300f;
    private static final float GRAVITY      = 800f;
    private static final float JUMP_VEL     = 550f;
    private static final float GROUND_Y     = 0f;
    private static final int   MAX_JUMPS    = 2;

    // Portal VS (left, violet)
    private static final float PORTAL_W              = 120f;
    private static final float PORTAL_H              = 200f;
    private static final float PORTAL_VS_X           = 110f;
    private static final float PORTAL_BOSS_X         = 1545f;
    private static final float PORTAL_INTERACT_DIST  = 210f;

    // Shop (world position, bobs up/down)
    private static final float SHOP_SIZE         = 140f;
    private static final float SHOP_X            = 820f;
    private static final float SHOP_Y            = 20f;
    private static final float SHOP_INTERACT_DIST = 210f;

    // Shop panel (screen-space)
    private static final float SHOP_PANEL_W       = 350f;
    private static final float CRATE_ANIM_GROW    = 0.45f;
    private static final float CRATE_ANIM_TOTAL   = 3.2f;
    private static final float CRATE_TARGET_SIZE  = 340f;

    private final BattleGame        game;
    private final OrthographicCamera camera;
    private final ShapeRenderer      shapeRenderer;

    private final Rectangle player;
    private float  playerVelY;
    private int    jumpsLeft = MAX_JUMPS;
    private boolean playerFacingLeft;

    private float portalAnimTimer;

    private boolean bossDialogOpen;
    private boolean shopOpen;
    private boolean screenLeft;

    // Shop state
    private float hpAnimTimer    = -1f;
    private float czystoscAnimTimer = -1f;
    private int   shopSpentAmount;
    private float shopSpentTimer;
    private boolean crateAnimActive;
    private float   crateAnimTimer;
    private float   crateAnimFromX, crateAnimFromY;
    private String  crateResultText = "";
    private float   crateResultTimer;

    private final Vector3 mouseTemp = new Vector3();

    // -------------------------------------------------------------------------
    public HubScreen(BattleGame game) {
        this.game = game;
        camera = new OrthographicCamera(W, H);
        camera.position.set(W / 2f, H / 2f, 0);
        camera.update();
        shapeRenderer = new ShapeRenderer();

        player = new Rectangle(WORLD_W / 2f - PLAYER_W / 2f, GROUND_Y, PLAYER_W, PLAYER_H);

        if (game.backgroundMusic != null && !game.backgroundMusic.isPlaying()) {
            game.backgroundMusic.setVolume(game.musicVolume);
            game.backgroundMusic.play();
        }
    }

    // =========================================================================
    //  RENDER
    // =========================================================================
    @Override
    public void render(float delta) {
        portalAnimTimer += delta;
        if (shopSpentTimer > 0) shopSpentTimer -= delta;
        if (hpAnimTimer >= 0) hpAnimTimer += delta;
        if (czystoscAnimTimer >= 0) czystoscAnimTimer += delta;
        if (crateResultTimer > 0) crateResultTimer -= delta;
        if (crateAnimActive) {
            crateAnimTimer += delta;
            if (crateAnimTimer >= CRATE_ANIM_TOTAL) crateAnimActive = false;
        }
        updatePlayer(delta);
        if (screenLeft) return;
        drawWorld();
        if (bossDialogOpen) drawBossDialog();
        if (shopOpen)       drawShop();
    }

    // =========================================================================
    //  PLAYER UPDATE
    // =========================================================================
    private void updatePlayer(float delta) {
        if (bossDialogOpen) { handleBossDialogInput(); return; }
        if (shopOpen)       { handleShopInput();        return; }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { goToMenu(); return; }

        // Horizontal movement (A / D)
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            player.x -= PLAYER_SPEED * delta;
            playerFacingLeft = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            player.x += PLAYER_SPEED * delta;
            playerFacingLeft = false;
        }
        player.x = MathUtils.clamp(player.x, 0, WORLD_W - PLAYER_W);

        // Jump (W) — double jump allowed
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && jumpsLeft > 0) {
            playerVelY = JUMP_VEL;
            jumpsLeft--;
        }

        // Gravity
        playerVelY -= GRAVITY * delta;
        player.y += playerVelY * delta;
        if (player.y <= GROUND_Y) {
            player.y     = GROUND_Y;
            playerVelY   = 0;
            jumpsLeft    = MAX_JUMPS;
        }

        // Camera follow
        camera.position.x = MathUtils.clamp(player.x + PLAYER_W / 2f, W / 2f, WORLD_W - W / 2f);
        camera.position.y = H / 2f;
        camera.update();

        // E interactions
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            float px = player.x + PLAYER_W / 2f;
            if (Math.abs(px - (PORTAL_VS_X + PORTAL_W / 2f)) < PORTAL_INTERACT_DIST) {
                enterVsMode();
            } else if (Math.abs(px - (PORTAL_BOSS_X + PORTAL_W / 2f)) < PORTAL_INTERACT_DIST) {
                bossDialogOpen = true;
            } else if (Math.abs(px - (SHOP_X + SHOP_SIZE / 2f)) < SHOP_INTERACT_DIST) {
                shopOpen = true;
            }
        }
    }

    // =========================================================================
    //  DRAW WORLD
    // =========================================================================
    private void drawWorld() {
        ScreenUtils.clear(0, 0, 0, 1);
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Tiled background
        for (int bx = 0; bx < WORLD_W; bx += W)
            game.batch.draw(game.backgroundTex, bx, 0, W, H);

        // VS portal — pulsing violet
        float vsScale = 1f + 0.05f * MathUtils.sin(portalAnimTimer * 3f);
        game.batch.setColor(0.7f, 0.35f, 1f, 1f);
        game.batch.draw(game.portalTex,
                PORTAL_VS_X - (PORTAL_W * vsScale - PORTAL_W) / 2f, GROUND_Y,
                PORTAL_W * vsScale, PORTAL_H * vsScale);
        game.batch.setColor(1, 1, 1, 1);

        // Boss portal — pulsing orange
        float bossScale = 1f + 0.05f * MathUtils.sin(portalAnimTimer * 3f + 1.2f);
        game.batch.setColor(1f, 0.55f, 0.1f, 1f);
        game.batch.draw(game.portalTex,
                PORTAL_BOSS_X - (PORTAL_W * bossScale - PORTAL_W) / 2f, GROUND_Y,
                PORTAL_W * bossScale, PORTAL_H * bossScale);
        game.batch.setColor(1, 1, 1, 1);

        // Shop icon — bobs up/down
        float shopBob = MathUtils.sin(portalAnimTimer * 2f) * 10f;
        game.batch.draw(game.sklepTex, SHOP_X, SHOP_Y + shopBob, SHOP_SIZE, SHOP_SIZE);

        // Player sprite
        com.badlogic.gdx.graphics.Texture pt = playerFacingLeft
                ? (game.bishopSkin ? game.biskupKropidloLeftTex : game.playerKropidloLeftTex)
                : (game.bishopSkin ? game.biskupKropidloTex     : game.playerKropidloTex);
        game.batch.draw(pt, player.x, player.y, PLAYER_W, PLAYER_H);

        // Portal / shop labels
        game.font.getData().setScale(0.78f);
        game.font.setColor(0.85f, 0.6f, 1f, 1f);
        game.font.draw(game.batch, "VS 2 GRACZY",   PORTAL_VS_X - 5,   GROUND_Y + PORTAL_H + 55);
        game.font.setColor(1f, 0.72f, 0.25f, 1f);
        game.font.draw(game.batch, "SWIAT BOSSOW",  PORTAL_BOSS_X - 8, GROUND_Y + PORTAL_H + 55);
        game.font.setColor(1f, 0.9f, 0.3f, 1f);
        game.font.draw(game.batch, "SKLEP",          SHOP_X + 32, SHOP_Y + SHOP_SIZE + shopBob + 30);

        // Proximity hints
        float px = player.x + PLAYER_W / 2f;
        game.font.getData().setScale(0.68f);
        game.font.setColor(Color.YELLOW);
        if (Math.abs(px - (PORTAL_VS_X + PORTAL_W / 2f)) < PORTAL_INTERACT_DIST)
            game.font.draw(game.batch, "[E] - Wejdz", PORTAL_VS_X, GROUND_Y + PORTAL_H + 90);
        if (Math.abs(px - (PORTAL_BOSS_X + PORTAL_W / 2f)) < PORTAL_INTERACT_DIST)
            game.font.draw(game.batch, "[E] - Wejdz", PORTAL_BOSS_X, GROUND_Y + PORTAL_H + 90);
        if (Math.abs(px - (SHOP_X + SHOP_SIZE / 2f)) < SHOP_INTERACT_DIST)
            game.font.draw(game.batch, "[E] - Sklep", SHOP_X + 12, SHOP_Y + SHOP_SIZE + shopBob + 65);

        // ESC hint — fixed to camera top-left
        float camLeft = camera.position.x - W / 2f;
        game.font.setColor(0.55f, 0.55f, 0.55f, 1f);
        game.font.getData().setScale(0.55f);
        game.font.draw(game.batch, "ESC - powrot do menu", camLeft + 15, H - 15);

        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(1f);
        game.batch.end();

        // Ground bar
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.28f, 0.18f, 0.07f, 1f);
        shapeRenderer.rect(0, 0, WORLD_W, 6f);
        shapeRenderer.end();
    }

    // =========================================================================
    //  BOSS DIALOG  (wybor trybu)
    // =========================================================================
    private void handleBossDialogInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { bossDialogOpen = false; return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1))  { launchBossWorld(0); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2))  { launchBossWorld(1); return; }
        if (Gdx.input.justTouched()) {
            camera.update();
            mouseTemp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(mouseTemp);
            float mx = mouseTemp.x, my = mouseTemp.y;
            float cx = camera.position.x, cy = camera.position.y;
            if (mx >= cx-140 && mx <= cx+140 && my >= cy+20  && my <= cy+75)  { launchBossWorld(0); return; }
            if (mx >= cx-140 && mx <= cx+140 && my >= cy-50  && my <= cy+5)   { launchBossWorld(1); return; }
            if (mx >= cx-140 && mx <= cx+140 && my >= cy-120 && my <= cy-65)  { bossDialogOpen = false; }
        }
    }

    private void drawBossDialog() {
        float cx = camera.position.x, cy = camera.position.y;

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // Dark overlay
        shapeRenderer.setColor(0f, 0f, 0f, 0.72f);
        shapeRenderer.rect(cx - W / 2f, cy - H / 2f, W, H);
        // Panel
        shapeRenderer.setColor(0.1f, 0.1f, 0.2f, 1f);
        shapeRenderer.rect(cx - 210, cy - 155, 420, 330);
        // 1 GRACZ
        shapeRenderer.setColor(0.1f, 0.45f, 0.15f, 1f);
        shapeRenderer.rect(cx - 140, cy + 20, 280, 55);
        // 2 GRACZY CO-OP
        shapeRenderer.setColor(0.1f, 0.2f, 0.55f, 1f);
        shapeRenderer.rect(cx - 140, cy - 50, 280, 55);
        // ANULUJ
        shapeRenderer.setColor(0.45f, 0.1f, 0.1f, 1f);
        shapeRenderer.rect(cx - 140, cy - 120, 280, 55);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(1.05f);
        game.font.draw(game.batch, "Wybierz tryb gry:", cx - 145, cy + 155);
        game.font.getData().setScale(0.82f);
        game.font.draw(game.batch, "[1]  1 GRACZ",           cx - 105, cy + 57);
        game.font.draw(game.batch, "[2]  2 GRACZY - CO-OP",  cx - 125, cy - 13);
        game.font.draw(game.batch, "[ESC]  ANULUJ",          cx - 90,  cy - 83);
        game.font.getData().setScale(1f);
        game.batch.end();
    }

    // =========================================================================
    //  SHOP
    // =========================================================================
    // ========= SHOP (screen-space panel, matches MenuScreen style) ============

    /** Converts screen-space X (0..W) to world X for current camera. */
    private float sx(float screenX) { return camera.position.x - W / 2f + screenX; }
    /** Converts screen-space Y (0..H) to world Y for current camera. */
    private float sy(float screenY) { return camera.position.y - H / 2f + screenY; }

    private void handleShopInput() {
        if (crateAnimActive) {
            if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                crateAnimActive = false;
            }
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.clickSound.play(game.clickVolume);
            shopOpen = false;
            return;
        }
        if (!Gdx.input.justTouched()) return;

        camera.update();
        mouseTemp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseTemp);
        float mx = mouseTemp.x, my = mouseTemp.y;

        // Panel X range in world coords
        float panelLeft = sx(W - SHOP_PANEL_W);

        float fullSize = 100f;
        float itemX = sx(W - SHOP_PANEL_W + SHOP_PANEL_W / 2f - fullSize / 2f);

        // HP upgrade
        float hpSY = H - 220f;
        float hpWY = sy(hpSY);
        if (hpAnimTimer < 0 && mx >= itemX && mx <= itemX + fullSize
                && my >= hpWY && my <= hpWY + fullSize
                && game.money >= game.hpUpgradePrice) {
            game.clickSound.play(game.clickVolume);
            game.money -= game.hpUpgradePrice;
            shopSpentAmount = game.hpUpgradePrice;
            shopSpentTimer = 2f;
            hpAnimTimer = 0f;
            game.hpUpgradePrice  += game.hpUpgradePrice / 4;
            game.hpUpgradeAmount += game.hpUpgradeAmount / 4;
            game.saveData();
        }

        // Czystosc upgrade
        float manaSY = H - 380f;
        float manaWY = sy(manaSY);
        if (czystoscAnimTimer < 0 && mx >= itemX && mx <= itemX + fullSize
                && my >= manaWY && my <= manaWY + fullSize
                && game.money >= game.czystoscUpgradePrice) {
            game.clickSound.play(game.clickVolume);
            game.money -= game.czystoscUpgradePrice;
            shopSpentAmount = game.czystoscUpgradePrice;
            shopSpentTimer = 2f;
            czystoscAnimTimer = 0f;
            game.czystoscUpgradePrice  += game.czystoscUpgradePrice / 4;
            game.czystoscUpgradeAmount += game.czystoscUpgradeAmount / 4;
            game.saveData();
        }

        // Crates
        float crateSize = 130f;
        float crateX = sx(W - SHOP_PANEL_W + SHOP_PANEL_W / 2f - crateSize / 2f);
        float crate1WY = sy(H - 730f);
        float crate2WY = sy(H - 730f - crateSize - 18f);
        int crateCost = 1000;
        if (mx >= crateX && mx <= crateX + crateSize
                && my >= crate1WY && my <= crate1WY + crateSize
                && game.money >= crateCost) {
            game.clickSound.play(game.clickVolume);
            game.money -= crateCost;
            shopSpentAmount = crateCost; shopSpentTimer = 2f;
            applyCrateRewardHub();
            crateAnimActive = true; crateAnimTimer = 0f;
            crateAnimFromX = crateX + crateSize / 2f;
            crateAnimFromY = crate1WY + crateSize / 2f;
            game.saveData();
        }
        if (mx >= crateX && mx <= crateX + crateSize
                && my >= crate2WY && my <= crate2WY + crateSize
                && game.money >= crateCost) {
            game.clickSound.play(game.clickVolume);
            game.money -= crateCost;
            shopSpentAmount = crateCost; shopSpentTimer = 2f;
            applyCrateRewardHub();
            crateAnimActive = true; crateAnimTimer = 0f;
            crateAnimFromX = crateX + crateSize / 2f;
            crateAnimFromY = crate2WY + crateSize / 2f;
            game.saveData();
        }

        // Click outside panel closes shop
        if (mx < panelLeft) {
            game.clickSound.play(game.clickVolume);
            shopOpen = false;
        }
    }

    private void applyCrateRewardHub() {
        int roll = (int)(Math.random() * 3);
        if (roll == 0) {
            int gained = game.hpUpgradeAmount;
            game.playerBonusHp += gained;
            game.hpUpgradePrice  += game.hpUpgradePrice / 4;
            game.hpUpgradeAmount += game.hpUpgradeAmount / 4;
            crateResultText = "+" + gained + " HP MAX!";
        } else if (roll == 1) {
            int gained = game.czystoscUpgradeAmount;
            game.czystoscBonusMax  += gained;
            game.czystoscUpgradePrice  += game.czystoscUpgradePrice / 4;
            game.czystoscUpgradeAmount += game.czystoscUpgradeAmount / 4;
            crateResultText = "+" + gained + " CZYSTOSC MAX!";
        } else {
            game.money += 500;
            crateResultText = "+500 monet!";
        }
        crateResultTimer = 3f;
    }

    private void drawShop() {
        // Panel is on the right side of the SCREEN (screen-space → world-space via sx/sy)
        float panelX  = sx(W - SHOP_PANEL_W);
        float panelY  = sy(0);
        float fullSize = 100f;
        float itemX   = sx(W - SHOP_PANEL_W + SHOP_PANEL_W / 2f - fullSize / 2f);

        // ── ShapeRenderer: dim overlay + panel + border ──────────────────────
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.1f, 0.05f, 0.95f);
        shapeRenderer.rect(panelX, panelY, SHOP_PANEL_W, H);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(panelX, panelY, SHOP_PANEL_W, H);
        shapeRenderer.end();

        // ── SpriteBatch: icons + text ─────────────────────────────────────────
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Kasa icon + money
        float kasaSize = 60f;
        game.batch.draw(game.kasaTex, panelX + 10, sy(H - kasaSize - 15f), kasaSize, kasaSize);
        game.font.getData().setScale(1f);
        game.font.setColor(Color.YELLOW);
        game.font.draw(game.batch, "" + game.money, panelX + kasaSize + 20, sy(H - 25f));

        if (shopSpentTimer > 0) {
            game.font.getData().setScale(0.8f);
            game.font.setColor(Color.RED);
            game.font.draw(game.batch, "-" + shopSpentAmount, panelX + kasaSize + 20, sy(H - 55f));
        }

        // Title
        game.font.getData().setScale(1.2f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "SKLEP", panelX + SHOP_PANEL_W / 2f - 50, sy(H - 90f));

        // HP upgrade
        float hpWY = sy(H - 220f);
        if (hpAnimTimer >= 0 && hpAnimTimer < 1.5f) {
            float progress = Math.min(1f, hpAnimTimer / 1f);
            float scale = 0.04f + 0.96f * progress * progress;
            float s = fullSize * scale;
            float off = (fullSize - s) / 2f;
            game.batch.draw(game.hpUpgradeTex, itemX + off, hpWY + off, s, s);
        } else {
            if (hpAnimTimer >= 1.5f) hpAnimTimer = -1f;
            game.batch.draw(game.hpUpgradeTex, itemX, hpWY, fullSize, fullSize);
        }
        game.font.getData().setScale(0.8f);
        game.font.setColor(game.money >= game.hpUpgradePrice ? Color.GREEN : Color.RED);
        game.font.draw(game.batch, "Cena: " + game.hpUpgradePrice, itemX, hpWY - 5f);
        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(0.6f);
        game.font.draw(game.batch, "+" + game.hpUpgradeAmount + " HP", itemX + 10, hpWY - 25f);

        // Czystosc upgrade
        float manaWY = sy(H - 380f);
        if (czystoscAnimTimer >= 0 && czystoscAnimTimer < 1.5f) {
            float progress = Math.min(1f, czystoscAnimTimer / 1f);
            float scale = 0.04f + 0.96f * progress * progress;
            float s = fullSize * scale;
            float off = (fullSize - s) / 2f;
            game.batch.draw(game.czystoscUpgradeTex, itemX + off, manaWY + off, s, s);
        } else {
            if (czystoscAnimTimer >= 1.5f) czystoscAnimTimer = -1f;
            game.batch.draw(game.czystoscUpgradeTex, itemX, manaWY, fullSize, fullSize);
        }
        game.font.getData().setScale(0.8f);
        game.font.setColor(game.money >= game.czystoscUpgradePrice ? Color.GREEN : Color.RED);
        game.font.draw(game.batch, "Cena: " + game.czystoscUpgradePrice, itemX, manaWY - 5f);
        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(0.6f);
        game.font.draw(game.batch, "+" + game.czystoscUpgradeAmount + " Czystosc", itemX + 10, manaWY - 25f);

        // Bishop skin
        float skinWY = sy(H - 540f);
        game.batch.draw(game.biskupTex, itemX, skinWY, fullSize, fullSize);
        if (game.bishopSkin) {
            game.font.getData().setScale(0.7f);
            game.font.setColor(Color.GREEN);
            game.font.draw(game.batch, "ODBLOKOWANO", itemX - 5, skinWY - 5f);
        } else {
            game.font.getData().setScale(0.55f);
            game.font.setColor(new Color(1f, 0.84f, 0f, 1f));
            game.font.draw(game.batch, "Osiagnij klase", itemX - 5, skinWY - 5f);
            game.font.draw(game.batch, "Zlota!", itemX + 20, skinWY - 25f);
        }
        game.font.getData().setScale(0.5f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "Ksiadz Biskup", itemX, skinWY - 45f);

        // Crates
        float crateSize = 130f;
        float crateX = sx(W - SHOP_PANEL_W + SHOP_PANEL_W / 2f - crateSize / 2f);
        float crate1WY = sy(H - 730f);
        float crate2WY = sy(H - 730f - crateSize - 18f);
        int crateCost = 1000;
        if (game.skrzynka1Tex != null) {
            game.batch.draw(game.skrzynka1Tex, crateX, crate1WY, crateSize, crateSize);
            game.batch.draw(game.skrzynka1Tex, crateX, crate2WY, crateSize, crateSize);
        }
        game.font.getData().setScale(0.6f);
        game.font.setColor(game.money >= crateCost ? Color.GREEN : Color.RED);
        game.font.draw(game.batch, "" + crateCost, crateX + 25, crate1WY - 5f);
        game.font.draw(game.batch, "" + crateCost, crateX + 25, crate2WY - 5f);
        game.font.getData().setScale(0.5f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Losowe ulepszenie", crateX - 5, crate1WY - 24f);
        game.font.draw(game.batch, "Losowe ulepszenie", crateX - 5, crate2WY - 24f);

        // Close hint
        game.font.getData().setScale(0.5f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "ESC - zamknij", panelX + SHOP_PANEL_W / 2f - 50, sy(30f));

        game.font.getData().setScale(1f);
        game.batch.end();

        // Crate opening animation overlay
        if (crateAnimActive) {
            float centerX = sx(W / 2f);
            float centerY = sy(H / 2f);

            if (crateAnimTimer < CRATE_ANIM_GROW) {
                float t = crateAnimTimer / CRATE_ANIM_GROW;
                float ease = 1f - (1f - t) * (1f - t);
                float curSize = 130 + (CRATE_TARGET_SIZE - 130) * ease;
                float curCx = crateAnimFromX + (centerX - crateAnimFromX) * ease;
                float curCy = crateAnimFromY + (centerY - crateAnimFromY) * ease;
                if (game.skrzynka1Tex != null) {
                    game.batch.begin();
                    game.batch.draw(game.skrzynka1Tex,
                            curCx - curSize / 2f, curCy - curSize / 2f, curSize, curSize);
                    game.batch.end();
                }
            } else {
                Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
                Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(0f, 0f, 0f, 0.65f);
                shapeRenderer.rect(sx(0), sy(0), W, H);
                shapeRenderer.end();
                Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

                Texture openTex = game.skrzynkaOtwartaTex != null ? game.skrzynkaOtwartaTex : game.skrzynka2Tex;
                game.batch.begin();
                if (openTex != null) {
                    game.batch.draw(openTex,
                            centerX - CRATE_TARGET_SIZE / 2f,
                            centerY - CRATE_TARGET_SIZE / 2f + 40,
                            CRATE_TARGET_SIZE, CRATE_TARGET_SIZE);
                }
                game.menuFont.getData().setScale(0.9f);
                game.menuFont.setColor(Color.YELLOW);
                com.badlogic.gdx.graphics.g2d.GlyphLayout gl =
                        new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.menuFont, crateResultText);
                game.menuFont.draw(game.batch, crateResultText,
                        centerX - gl.width / 2f, centerY - CRATE_TARGET_SIZE / 2f + 15);
                game.menuFont.getData().setScale(1f);
                game.font.getData().setScale(0.55f);
                game.font.setColor(Color.GRAY);
                game.font.draw(game.batch, "Kliknij aby zamknac", centerX - 80, sy(30f));
                game.font.getData().setScale(1f);
                game.batch.end();
            }
        }
    }

    private void enterVsMode() {
        game.multiplayerMode = 2;
        if (game.backgroundMusic != null) game.backgroundMusic.stop();
        screenLeft = true;
        game.setScreen(new BattleScreen(game));
        dispose();
    }

    private void launchBossWorld(int mode) {
        game.multiplayerMode = mode; // 0 = solo, 1 = co-op
        if (game.backgroundMusic != null) game.backgroundMusic.stop();
        screenLeft = true;
        game.setScreen(new BattleScreen(game));
        dispose();
    }

    private void goToMenu() {
        game.multiplayerMode = 0;
        if (game.backgroundMusic != null && !game.backgroundMusic.isPlaying()) {
            game.backgroundMusic.setVolume(game.musicVolume);
            game.backgroundMusic.play();
        }
        screenLeft = true;
        game.setScreen(new MenuScreen(game));
        dispose();
    }

    // =========================================================================
    //  Screen lifecycle
    // =========================================================================
    @Override public void show()   {}
    @Override public void resize(int w, int h) {}
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}