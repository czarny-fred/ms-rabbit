package com.example.demogame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
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
    private static final float START_BTN_X = LEFT_CX - BTN_W / 2f + 10;
    private static final float START_BTN_Y = PANEL_Y + PANEL_H / 2f + 40;
    private static final float EXIT_BTN_X = LEFT_CX - BTN_W / 2f + 50;
    private static final float EXIT_BTN_Y = START_BTN_Y - BTN_H - 30;

    // Right page center (score + enemies button)
    private static final float RIGHT_CX = PANEL_X + PANEL_W * 3f / 4f - 60;
    private static final float ENEMIES_BTN_X = RIGHT_CX - BTN_W / 2f;
    private static final float ENEMIES_BTN_Y = PANEL_Y + 220;
    // Equipment button (between score and BOSSY)
    private static final float EQUIP_BTN_X = ENEMIES_BTN_X;
    private static final float EQUIP_BTN_Y = ENEMIES_BTN_Y + BTN_H + 60;

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
    private String crateResultText = "";
    private float crateResultTimer = 0f;
    private boolean equipmentOpen;
    private int equipSlotSelected = 0; // which slot is highlighted (0/1/2)
    // Ability info (mirrors BattleScreen constants for display)
    private static final String[] ABILITY_NAMES = {"Odbicie","Leczenie","Oswiecenie","Modlitwa","Zdrowas","Skok","Egzorcyzm","Benedykcja","Namaszczenie"};
    private static final String[] ABILITY_DESCS = {
        "Odbija pociski przez 2s | 50 czyst. | cd 10s",
        "Leczy 15% HP | 40 czyst. | cd 17s",
        "Zamraza wroga, 20 dmg | 60 czyst. | cd 10s",
        "Modlitwy slow (wymaga ksiegi) | 30 czyst.",
        "Zdrowas Mario (wymaga ksiegi) | 40 czyst.",
        "Skok + ogluszone wrogie 3s | 45 czyst. | cd 15s",
        "Spowolnia wroga 8s + -30% HP max | 70 czyst. | cd 25s",
        "Podwajasz dmg 6s + odpychasz wrogw | 55 czyst. | cd 20s",
        "Gdy HP<=30%: reset cd + -50% dmg przez 8s | cd 60s"
    };
    private boolean crateAnimActive;
    private float crateAnimTimer;
    private float crateAnimFromX, crateAnimFromY;
    private static final float CRATE_ANIM_GROW = 0.45f;
    private static final float CRATE_ANIM_TOTAL = 3.2f;
    private static final float CRATE_TARGET_SIZE = 340f;
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
    private boolean scoresViewOpen;
    private float equipScrollOffset = 0f;
    private static final float EQUIP_ROW_H = 68f;
    private boolean twoPlayerMenuOpen;
    // 2-player button: below EXIT on left side
    private static final float TWO_PLAYER_BTN_X = LEFT_CX - BTN_W / 2f + 45;
    private static final float TWO_PLAYER_BTN_Y = EXIT_BTN_Y - BTN_H - 45;
    private final Vector3 mouseTemp = new Vector3();

    // Profile export / import
    private float exportCopiedTimer = 0f;   // > 0 → show "Skopiowano!" flash
    private boolean importProfileOpen;       // overlay showing imported profile card
    private String  importProfileText = "";  // raw imported text to display

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
        if (crateResultTimer > 0) crateResultTimer -= delta;
        if (exportCopiedTimer > 0) exportCopiedTimer -= delta;
        if (crateAnimActive) {
            crateAnimTimer += delta;
            if (crateAnimTimer >= CRATE_ANIM_TOTAL) crateAnimActive = false;
        }

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

        if (equipmentOpen) {
            handleEquipmentInput();
            drawEquipmentView();
            return;
        }
        if (shopOpen) {
            handleShopInput();
            drawShopView();
            return;
        }

        // Scores view (overlay, close on any click)
        if (scoresViewOpen) {
            if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                scoresViewOpen = false;
            }
            drawMainMenu();
            drawScoresView();
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

            // START – click area matches text draw position
            float startTx = START_BTN_X + BTN_W / 2f - 55;
            float startTy = START_BTN_Y + BTN_H / 2f + 18;
            if (mx >= startTx - 10 && mx <= startTx + 180 && my >= startTy - 52 && my <= startTy + 8) {
                game.clickSound.play(game.clickVolume);
                startGame();
                return;
            }
            // EXIT – click area matches text
            float exitTx = EXIT_BTN_X + BTN_W / 2f - 75;
            float exitTy = EXIT_BTN_Y + BTN_H / 2f + 18;
            if (mx >= exitTx - 10 && mx <= exitTx + 160 && my >= exitTy - 52 && my <= exitTy + 8) {
                game.clickSound.play(game.clickVolume);
                Gdx.app.exit();
                return;
            }
            // BOSSY – click area matches text
            float bossyTx = ENEMIES_BTN_X + 30;
            float bossyTy = ENEMIES_BTN_Y + BTN_H / 2f + 50;
            if (mx >= bossyTx && mx <= bossyTx + 150 && my >= bossyTy - 36 && my <= bossyTy + 6) {
                game.clickSound.play(game.clickVolume);
                enemiesView = true;
                enemiesPage = 0;
            }
            // EKWIPUNEK – click area matches text draw position
            float equipTx = EQUIP_BTN_X + 10;
            float equipTy = EQUIP_BTN_Y + BTN_H / 2f + 30;
            if (mx >= equipTx && mx <= equipTx + 220 && my >= equipTy - 28 && my <= equipTy + 6) {
                game.clickSound.play(game.clickVolume);
                equipmentOpen = true;
                equipSlotSelected = 0;
                equipScrollOffset = 0;
            }
            // Profile button click
            if (mx >= PROFILE_BTN_X && mx <= PROFILE_BTN_X + PROFILE_BTN_SIZE
                    && my >= PROFILE_BTN_Y && my <= PROFILE_BTN_Y + PROFILE_BTN_SIZE) {
                game.clickSound.play(game.clickVolume);
                profileOpen = true;
                editingName = false;
            }
            // 2 GRACZY button
            float tpTx = TWO_PLAYER_BTN_X + BTN_W / 2f - 80;
            float tpTy = TWO_PLAYER_BTN_Y + BTN_H / 2f + 18;
            if (mx >= tpTx - 10 && mx <= tpTx + 220 && my >= tpTy - 52 && my <= tpTy + 8) {
                game.clickSound.play(game.clickVolume);
                twoPlayerMenuOpen = !twoPlayerMenuOpen;
            }
            if (twoPlayerMenuOpen) {
                // CO-OP button
                float coopX = tpTx; float coopY = tpTy - 70;
                if (mx >= coopX - 10 && mx <= coopX + 160 && my >= coopY - 36 && my <= coopY + 6) {
                    game.clickSound.play(game.clickVolume);
                    game.multiplayerMode = 1;
                    twoPlayerMenuOpen = false;
                    startGame();
                    return;
                }
                // VS button
                float vsX = tpTx; float vsY = tpTy - 130;
                if (mx >= vsX - 10 && mx <= vsX + 100 && my >= vsY - 36 && my <= vsY + 6) {
                    game.clickSound.play(game.clickVolume);
                    game.multiplayerMode = 2;
                    twoPlayerMenuOpen = false;
                    startGame();
                    return;
                }
            }
            // High score click → scores history
            if (game.highScore > 0) {
                float scoreTx = RIGHT_CX - 110;
                float scoreTy = PANEL_Y + PANEL_H / 2f + 165;
                if (mx >= scoreTx && mx <= scoreTx + 220 && my >= scoreTy - 80 && my <= scoreTy + 8) {
                    game.clickSound.play(game.clickVolume);
                    scoresViewOpen = true;
                }
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
        game.font.getData().setScale(1.0f);
        game.font.setColor(new Color(0.4f, 0.8f, 1f, 1f));
        game.font.draw(game.batch, "EKWIPUNEK", EQUIP_BTN_X + 10, EQUIP_BTN_Y + BTN_H / 2f + 30);

        // 2 GRACZY button
        game.font.getData().setScale(0.85f);
        game.font.setColor(new Color(0.1f, 0.55f, 0.15f, 1f));
        game.font.draw(game.batch, "2 GRACZY", TWO_PLAYER_BTN_X + BTN_W / 2f - 80, TWO_PLAYER_BTN_Y + BTN_H / 2f + 18);
        if (twoPlayerMenuOpen) {
            game.font.getData().setScale(0.75f);
            game.font.setColor(new Color(0.2f, 0.9f, 0.4f, 1f));
            game.font.draw(game.batch, "CO-OP", TWO_PLAYER_BTN_X + BTN_W / 2f - 70, TWO_PLAYER_BTN_Y + BTN_H / 2f - 52);
            game.font.setColor(new Color(1f, 0.3f, 0.3f, 1f));
            game.font.draw(game.batch, "VS", TWO_PLAYER_BTN_X + BTN_W / 2f - 50, TWO_PLAYER_BTN_Y + BTN_H / 2f - 112);
        }
        game.font.getData().setScale(1f);

        // Profile button (top-left) — kwadrat ze zdjeciem profilowym + nazwa ponizej
        game.batch.end();
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 0.8f);
        shapeRenderer.rect(PROFILE_BTN_X, PROFILE_BTN_Y, PROFILE_BTN_SIZE, PROFILE_BTN_SIZE);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(game.getPlayerClassColor());
        shapeRenderer.rect(PROFILE_BTN_X, PROFILE_BTN_Y, PROFILE_BTN_SIZE, PROFILE_BTN_SIZE);
        shapeRenderer.end();
        game.batch.begin();
        // Zdjecie profilowe wewnatrz kwadratu (jesli ustawione)
        if (game.profilePictureIndex >= 0 && game.profiloweTex != null
                && game.profilePictureIndex < game.profiloweTex.length) {
            game.batch.draw(game.profiloweTex[game.profilePictureIndex],
                    PROFILE_BTN_X, PROFILE_BTN_Y, PROFILE_BTN_SIZE, PROFILE_BTN_SIZE);
        }
        // Nazwa gracza pod kwadratem
        game.font.setColor(game.getPlayerClassColor());
        game.font.getData().setScale(0.45f);
        game.font.draw(game.batch, game.playerName, PROFILE_BTN_X + 3, PROFILE_BTN_Y - 5);

        game.font.getData().setScale(1f);
        game.batch.end();
    }

    private void drawScoresView() {
        float panelW = 420, panelH = 580;
        float panelX = W / 2f - panelW / 2f, panelY = H / 2f - panelH / 2f;

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.88f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(new Color(1f, 0.85f, 0.1f, 1f));
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();

        game.batch.begin();
        game.menuFont.getData().setScale(0.75f);
        game.menuFont.setColor(new Color(1f, 0.85f, 0.1f, 1f));
        game.menuFont.draw(game.batch, "HISTORIA WYNIKOW", panelX + 55, panelY + panelH - 18);
        game.menuFont.getData().setScale(1f);

        float lineY = panelY + panelH - 70;
        float lineH = 44f;
        for (int i = 0; i < 10; i++) {
            int sc = game.recentScores[i];
            if (sc == 0) break;
            boolean best = (sc == game.highScore);
            game.font.getData().setScale(i == 0 ? 1.1f : 0.85f);
            game.font.setColor(best ? new Color(1f, 0.85f, 0.1f, 1f) : (i == 0 ? Color.WHITE : Color.LIGHT_GRAY));
            game.font.draw(game.batch, (i + 1) + ".  " + sc + (best ? "  ★" : ""), panelX + 40, lineY - i * lineH);
        }

        game.font.getData().setScale(0.55f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "Kliknij aby zamknac", panelX + panelW / 2f - 75, panelY + 22);
        game.font.getData().setScale(1f);
        game.batch.end();
    }

    // Equipment layout helpers (shared between draw and input)
    private float eqPanelX()    { return W / 2f - 480; }
    private float eqPanelY()    { return 60; }
    private float eqPanelW()    { return 960; }
    private float eqPanelH()    { return 870; }
    private float eqTopH()      { return 290; }
    private float eqTopY()      { return eqPanelY() + eqPanelH() - 58 - eqTopH(); }  // below title
    private float eqSkinX()     { return eqPanelX() + 35; }
    private float eqSkinW()     { return 270; }
    private float eqSkinH()     { return 260; }
    private float eqSkinY()     { return eqTopY() + (eqTopH() - eqSkinH()) / 2f; }
    private float eqSlotSize()  { return 120; }
    private float eqSlot0X()    { return eqSkinX() + eqSkinW() + 50; }
    private float eqSlot1X()    { return eqSlot0X() + eqSlotSize() + 22; }
    private float eqSlot2X()    { return eqSlot1X() + eqSlotSize() + 22; }
    private float eqSlotY()     { return eqTopY() + (eqTopH() - eqSlotSize()) / 2f; }
    private float eqListTop()   { return eqTopY() - 42; }   // just below separator
    private float eqListBottom(){ return eqPanelY() + 50; }

    private void handleEquipmentInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.clickSound.play(game.clickVolume);
            equipmentOpen = false;
            return;
        }
        // Arrow key scroll for list
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            float maxScroll = Math.max(0, ABILITY_NAMES.length * EQUIP_ROW_H - equipListVisibleH());
            equipScrollOffset = MathUtils.clamp(equipScrollOffset + EQUIP_ROW_H, 0, maxScroll);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            float maxScroll = Math.max(0, ABILITY_NAMES.length * EQUIP_ROW_H - equipListVisibleH());
            equipScrollOffset = MathUtils.clamp(equipScrollOffset - EQUIP_ROW_H, 0, maxScroll);
        }

        if (!Gdx.input.justTouched()) return;
        mouseTemp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseTemp);
        float mx = mouseTemp.x, my = mouseTemp.y;

        float slotSize = eqSlotSize();
        float slotY = eqSlotY();
        float[] slotXs = { eqSlot0X(), eqSlot1X(), eqSlot2X() };

        // Click a slot to select it
        for (int s = 0; s < 3; s++) {
            if (mx >= slotXs[s] && mx <= slotXs[s] + slotSize
                    && my >= slotY && my <= slotY + slotSize) {
                game.clickSound.play(game.clickVolume);
                equipSlotSelected = s;
                return;
            }
        }

        // Click an ability in the scrollable single-column list
        float listTop = eqListTop();
        float listBottom = eqListBottom();
        float listX = eqPanelX() + 20;
        float listW = eqPanelW() - 40;
        if (mx >= listX && mx <= listX + listW && my >= listBottom && my <= listTop) {
            // Figure out which row was clicked (accounting for scroll)
            float relY = listTop - my + equipScrollOffset; // distance from content top
            int row = (int)(relY / EQUIP_ROW_H);
            if (row >= 0 && row < ABILITY_NAMES.length) {
                game.clickSound.play(game.clickVolume);
                boolean alreadyInOtherSlot = false;
                for (int s = 0; s < 3; s++) {
                    if (s != equipSlotSelected && game.selectedAbilities[s] == row) {
                        alreadyInOtherSlot = true;
                        break;
                    }
                }
                if (!alreadyInOtherSlot) {
                    game.selectedAbilities[equipSlotSelected] = row;
                    game.saveData();
                }
            }
        }
    }

    private void drawEquipmentView() {
        float panelX = eqPanelX(), panelW = eqPanelW();
        float panelY = eqPanelY(), panelH = eqPanelH();
        float topY     = eqTopY();
        float skinX    = eqSkinX(), skinY = eqSkinY();
        float skinW    = eqSkinW(), skinH  = eqSkinH();
        float slotSize = eqSlotSize();
        float slotY    = eqSlotY();
        float[] slotXs = { eqSlot0X(), eqSlot1X(), eqSlot2X() };
        float listTop    = eqListTop();
        float listBottom = eqListBottom();
        float listX      = panelX + 20;
        float listW      = panelW - 40;

        // ── PASS 1: background texture ──────────────────────────────────────
        game.batch.begin();
        game.batch.draw(game.startBgTex, 0, 0, W, H);
        game.batch.end();

        // ── PASS 2: all ShapeRenderer calls ─────────────────────────────────
        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // Panel
        shapeRenderer.setColor(0.06f, 0.05f, 0.10f, 0.97f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        // Skin bg
        shapeRenderer.setColor(0.12f, 0.10f, 0.18f, 1f);
        shapeRenderer.rect(skinX - 8, skinY - 8, skinW + 16, skinH + 16);
        // Slots
        for (int s = 0; s < 3; s++) {
            boolean sel = (s == equipSlotSelected);
            shapeRenderer.setColor(sel ? 0.18f : 0.10f, sel ? 0.16f : 0.10f, sel ? 0.28f : 0.14f, 1f);
            shapeRenderer.rect(slotXs[s], slotY, slotSize, slotSize);
        }
        // Separator
        shapeRenderer.setColor(0.4f, 0.8f, 1f, 0.3f);
        shapeRenderer.rect(panelX + 15, topY - 2, panelW - 30, 2);
        // Row highlights for assigned abilities
        for (int i = 0; i < ABILITY_NAMES.length; i++) {
            float rowTop = listTop - i * EQUIP_ROW_H + equipScrollOffset;
            float rowBot = rowTop - EQUIP_ROW_H;
            if (rowTop < listBottom || rowBot > listTop) continue;
            int inSlot = -1;
            for (int s = 0; s < 3; s++) if (game.selectedAbilities[s] == i) { inSlot = s; break; }
            if (inSlot >= 0) {
                shapeRenderer.setColor(0.1f, 0.25f, 0.1f, 0.55f);
                shapeRenderer.rect(listX, rowBot + 4, listW - 20, EQUIP_ROW_H - 6);
            }
        }
        // Scroll bar
        float maxScroll = Math.max(1, ABILITY_NAMES.length * EQUIP_ROW_H - equipListVisibleH());
        if (maxScroll > 1) {
            float trackH = listTop - listBottom;
            float thumbH = Math.max(30, trackH * (trackH / (ABILITY_NAMES.length * EQUIP_ROW_H)));
            float thumbY = listBottom + (trackH - thumbH) * (1f - MathUtils.clamp(equipScrollOffset / maxScroll, 0, 1));
            shapeRenderer.setColor(0.25f, 0.25f, 0.3f, 1f);
            shapeRenderer.rect(panelX + panelW - 18, listBottom, 8, trackH);
            shapeRenderer.setColor(0.5f, 0.7f, 1f, 1f);
            shapeRenderer.rect(panelX + panelW - 18, thumbY, 8, thumbH);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.4f, 0.8f, 1f, 1f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.setColor(0.4f, 0.8f, 1f, 0.6f);
        shapeRenderer.rect(skinX - 8, skinY - 8, skinW + 16, skinH + 16);
        for (int s = 0; s < 3; s++) {
            shapeRenderer.setColor(s == equipSlotSelected ? Color.YELLOW : Color.WHITE);
            shapeRenderer.rect(slotXs[s], slotY, slotSize, slotSize);
            if (s == equipSlotSelected) shapeRenderer.rect(slotXs[s]+1, slotY+1, slotSize-2, slotSize-2);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        // ── PASS 3: all SpriteBatch / font calls ─────────────────────────────
        game.batch.begin();

        // Title
        game.menuFont.getData().setScale(0.9f);
        game.menuFont.setColor(new Color(0.4f, 0.8f, 1f, 1f));
        game.menuFont.draw(game.batch, "EKWIPUNEK", panelX + panelW / 2f - 130, panelY + panelH - 18);
        game.menuFont.getData().setScale(1f);

        // Skin
        Texture skinTex = game.bishopSkin ? game.biskupTex : game.playerRightTex;
        game.batch.draw(skinTex, skinX, skinY, skinW, skinH);

        // Slot labels + ability name
        for (int s = 0; s < 3; s++) {
            int abilId = game.selectedAbilities[s];
            float sx = slotXs[s];
            game.font.getData().setScale(0.5f);
            game.font.setColor(s == equipSlotSelected ? Color.YELLOW : Color.LIGHT_GRAY);
            game.font.draw(game.batch, "SLOT " + (s + 1), sx + 6, slotY + slotSize - 6);
            game.font.getData().setScale(0.6f);
            game.font.setColor(new Color(0.5f, 1f, 0.5f, 1f));
            String name = ABILITY_NAMES[abilId];
            int split = name.length() > 10 ? Math.max(1, name.lastIndexOf(' ', 10)) : -1;
            if (split > 0) {
                game.font.draw(game.batch, name.substring(0, split),        sx + 6, slotY + slotSize / 2f + 18);
                game.font.draw(game.batch, name.substring(split).trim(),     sx + 6, slotY + slotSize / 2f - 2);
            } else {
                game.font.draw(game.batch, name, sx + 6, slotY + slotSize / 2f + 10);
            }
        }

        // Slot hint
        game.font.getData().setScale(0.48f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "Kliknij slot, potem umiejetnosc ponizej", slotXs[0], slotY - 12);

        // List header
        game.font.getData().setScale(0.65f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "DOSTEPNE UMIEJETNOSCI:", listX, listTop + 2);

        // Ability rows
        for (int i = 0; i < ABILITY_NAMES.length; i++) {
            float rowTop = listTop - i * EQUIP_ROW_H + equipScrollOffset;
            float rowBot = rowTop - EQUIP_ROW_H;
            if (rowTop < listBottom || rowBot > listTop) continue;
            int inSlot = -1;
            for (int s = 0; s < 3; s++) if (game.selectedAbilities[s] == i) { inSlot = s; break; }
            game.font.getData().setScale(0.75f);
            game.font.setColor(inSlot >= 0 ? new Color(0.3f, 1f, 0.3f, 1f) : Color.WHITE);
            game.font.draw(game.batch, ABILITY_NAMES[i] + (inSlot >= 0 ? "  [Slot " + (inSlot + 1) + "]" : ""), listX + 10, rowTop - 10);
            game.font.getData().setScale(0.52f);
            game.font.setColor(Color.GRAY);
            game.font.draw(game.batch, ABILITY_DESCS[i], listX + 10, rowTop - 32);
        }

        // Footer hint
        game.font.getData().setScale(0.52f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "ESC - zamknij   |   Scroll / strzalki - przewijaj", panelX + panelW / 2f - 180, panelY + 22);
        game.font.getData().setScale(1f);

        game.batch.end();
    }

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

            // Crates - stacked vertically, bigger
            float crateSize = 130;
            float crateX = SHOP_X + SHOP_W / 2f - crateSize / 2f;
            float crate1Y = H - 730;
            float crate2Y = crate1Y - crateSize - 18;
            int crateCost = 1000;
            if (mx >= crateX && mx <= crateX + crateSize
                    && my >= crate1Y && my <= crate1Y + crateSize
                    && game.money >= crateCost) {
                game.clickSound.play(game.clickVolume);
                game.money -= crateCost;
                shopSpentAmount = crateCost;
                shopSpentTimer = 2f;
                applyCrateReward();
                crateAnimActive = true;
                crateAnimTimer = 0f;
                crateAnimFromX = crateX + crateSize / 2f;
                crateAnimFromY = crate1Y + crateSize / 2f;
                game.saveData();
            }
            if (mx >= crateX && mx <= crateX + crateSize
                    && my >= crate2Y && my <= crate2Y + crateSize
                    && game.money >= crateCost) {
                game.clickSound.play(game.clickVolume);
                game.money -= crateCost;
                shopSpentAmount = crateCost;
                shopSpentTimer = 2f;
                applyCrateReward();
                crateAnimActive = true;
                crateAnimTimer = 0f;
                crateAnimFromX = crateX + crateSize / 2f;
                crateAnimFromY = crate2Y + crateSize / 2f;
                game.saveData();
            }

            // Click outside shop panel closes it
            if (mx < SHOP_X) {
                game.clickSound.play(game.clickVolume);
                shopOpen = false;
            }
        }
    }

    private void applyCrateReward() {
        int roll = (int)(Math.random() * 3);
        if (roll == 0) {
            int gained = game.hpUpgradeAmount;
            game.playerBonusHp += gained;
            game.hpUpgradePrice += game.hpUpgradePrice / 4;
            game.hpUpgradeAmount += game.hpUpgradeAmount / 4;
            crateResultText = "+" + gained + " HP MAX!";
        } else if (roll == 1) {
            int gained = game.czystoscUpgradeAmount;
            game.czystoscBonusMax += gained;
            game.czystoscUpgradePrice += game.czystoscUpgradePrice / 4;
            game.czystoscUpgradeAmount += game.czystoscUpgradeAmount / 4;
            crateResultText = "+" + gained + " CZYSTOSC MAX!";
        } else {
            int bonus = 500;
            game.money += bonus;
            crateResultText = "+500 monet!";
        }
        crateResultTimer = 3f;
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

        // Crates - stacked vertically, bigger
        float crateSize = 130;
        float crateX = SHOP_X + SHOP_W / 2f - crateSize / 2f;
        float crate1Y = H - 730;
        float crate2Y = crate1Y - crateSize - 18;
        int crateCost = 1000;
        if (game.skrzynka1Tex != null) {
            game.batch.draw(game.skrzynka1Tex, crateX, crate1Y, crateSize, crateSize);
            game.batch.draw(game.skrzynka1Tex, crateX, crate2Y, crateSize, crateSize);
        }
        game.font.getData().setScale(0.6f);
        game.font.setColor(game.money >= crateCost ? Color.GREEN : Color.RED);
        game.font.draw(game.batch, "" + crateCost, crateX + 25, crate1Y - 5);
        game.font.draw(game.batch, "" + crateCost, crateX + 25, crate2Y - 5);
        game.font.getData().setScale(0.5f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Losowe ulepszenie", crateX - 5, crate1Y - 24);
        game.font.draw(game.batch, "Losowe ulepszenie", crateX - 5, crate2Y - 24);

        // Close hint
        game.font.getData().setScale(0.5f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "ESC - zamknij", SHOP_X + SHOP_W / 2f - 50, 30);

        game.font.getData().setScale(1f);
        game.batch.end();

        // Crate opening animation overlay
        if (crateAnimActive) {
            float centerX = W / 2f;
            float centerY = H / 2f;

            if (crateAnimTimer < CRATE_ANIM_GROW) {
                // Growing phase: crate grows from purchase point toward center
                float t = crateAnimTimer / CRATE_ANIM_GROW;
                // ease-out curve
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
                // Open phase: dim overlay + open crate + result text
                Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
                Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(0f, 0f, 0f, 0.65f);
                shapeRenderer.rect(0, 0, W, H);
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
                game.font.draw(game.batch, "Kliknij aby zamknac", centerX - 80, 30);
                game.font.getData().setScale(1f);
                game.batch.end();
            }
        }
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

        // Profile picture or "+" in picture slot
        if (game.profilePictureIndex >= 0 && game.profiloweTex != null
                && game.profilePictureIndex < game.profiloweTex.length) {
            game.batch.draw(game.profiloweTex[game.profilePictureIndex], picAreaX, picAreaY, picAreaSize, picAreaSize);
        } else {
            game.font.getData().setScale(1.5f);
            game.font.setColor(new Color(0.6f, 0.6f, 0.9f, 1f));
            game.font.draw(game.batch, "+", picAreaX + 18, picAreaY + picAreaSize - 8);
            game.font.getData().setScale(1f);
        }

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
        if (profilePictureMenuOpen) {
            drawProfilePictureMenu();
        }
    }

    private void drawProfilePictureMenu() {
        float menuW = 450;
        float menuH = 500;
        float menuX = W / 2f - menuW / 2f;
        float menuY = H / 2f - menuH / 2f;

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.85f);
        shapeRenderer.rect(0, 0, W, H);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.08f, 0.15f, 1f);
        shapeRenderer.rect(menuX, menuY, menuW, menuH);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(menuX, menuY, menuW, menuH);
        shapeRenderer.end();

        game.batch.begin();
        game.font.getData().setScale(0.6f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Wybierz zdjecie profilowe", menuX + 20, menuY + menuH - 20);
        game.font.getData().setScale(0.35f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "ESC - zamknij", menuX + 20, menuY + 15);

        if (game.profilePictureIndex == -1) {
            // 2x2 grid
            float picSize = 160;
            float colX1 = W / 2f - picSize - 10;
            float colX2 = W / 2f + 10;
            float rowY1 = H / 2f + 40;
            float rowY2 = H / 2f - picSize - 20;
            for (int pi = 0; pi < 4 && game.profiloweTex != null; pi++) {
                float picX = (pi % 2 == 0) ? colX1 : colX2;
                float picY = (pi < 2) ? rowY1 : rowY2;
                game.batch.draw(game.profiloweTex[pi], picX, picY, picSize, picSize);
            }
        } else {
            int currentIdx = game.profilePictureIndex;
            float bigSize = 200;
            float bigX = W / 2f - bigSize / 2f;
            float bigY = H / 2f + 50;
            if (game.profiloweTex != null && currentIdx < game.profiloweTex.length) {
                game.batch.draw(game.profiloweTex[currentIdx], bigX, bigY, bigSize, bigSize);
            }
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
            for (int i = 0; i < 3 && game.profiloweTex != null; i++) {
                float sx = startX + i * (smallSize + gap);
                if (others[i] < game.profiloweTex.length) {
                    game.batch.draw(game.profiloweTex[others[i]], sx, smallY, smallSize, smallSize);
                }
            }
        }
        game.font.getData().setScale(1f);
        game.batch.end();
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
        if (importProfileOpen) return; // overlay handles its own clicks
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
            float panelH = 520;
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

            // Export profile (copy to clipboard)
            float exportY = resetY - 70;
            if (mx >= px && mx <= px + 280 && my >= exportY - 5 && my <= exportY + 25) {
                game.clickSound.play(game.clickVolume);
                Gdx.app.getClipboard().setContents(buildProfileExport());
                exportCopiedTimer = 2.5f;
            }

            // Import / view profile from clipboard
            float importY = exportY - 55;
            if (mx >= px && mx <= px + 280 && my >= importY - 5 && my <= importY + 25) {
                game.clickSound.play(game.clickVolume);
                String clip = Gdx.app.getClipboard().getContents();
                if (clip != null && clip.contains("=== PROFIL ITER LUCIS ===")) {
                    importProfileText = clip;
                    importProfileOpen = true;
                }
            }

            // Click outside settings panel closes it
            if (mx < panelX || mx > panelX + panelW || my < panelY || my > panelY + panelH) {
                game.clickSound.play(game.clickVolume);
                settingsOpen = false;
            }
        }
    }

    private String buildProfileExport() {
        String[] abilityNames2 = {"Odbicie","Leczenie","Oswiecenie","Modlitwa","Zdrowas","Skok","Egzorcyzm","Benedykcja","Namaszczenie"};
        StringBuilder sb = new StringBuilder();
        sb.append("=== PROFIL ITER LUCIS ===\n");
        sb.append("Imie: ").append(game.playerName).append("\n");
        sb.append("Najlepszy wynik: ").append(game.highScore).append("\n");
        sb.append("Kasa: ").append(game.money).append(" monet\n");
        sb.append("Skin biskupa: ").append(game.bishopSkin ? "odblokowany" : "nie").append("\n");
        sb.append("Max HP: ").append(100 + game.playerBonusHp)
          .append(" (+").append(game.playerBonusHp).append(" ulepszen)\n");
        sb.append("Max Czystosc: ").append(100 + game.czystoscBonusMax)
          .append(" (+").append(game.czystoscBonusMax).append(" ulepszen)\n");
        sb.append("Zdolnosci: ");
        for (int i = 0; i < game.selectedAbilities.length; i++) {
            int id = game.selectedAbilities[i];
            String name = (id >= 0 && id < abilityNames2.length) ? abilityNames2[id] : "brak";
            if (i > 0) sb.append(", ");
            sb.append(name);
        }
        sb.append("\n");
        sb.append("Ostatnie wyniki:");
        boolean anyScore = false;
        for (int s : game.recentScores) { if (s > 0) { anyScore = true; break; } }
        if (!anyScore) {
            sb.append(" brak");
        } else {
            for (int s : game.recentScores) { if (s > 0) sb.append(" ").append(s); }
        }
        sb.append("\n");
        sb.append("=========================\n");
        return sb.toString();
    }

    private void drawSettingsOverlay() {
        float panelW = 450;
        float panelH = 520;
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

        // Export profile
        float exportY = resetY - 70;
        game.font.getData().setScale(0.6f);
        if (exportCopiedTimer > 0) {
            game.font.setColor(Color.GREEN);
            game.font.draw(game.batch, "[ SKOPIUJ PROFIL ]   Skopiowano!", px, exportY);
        } else {
            game.font.setColor(new Color(0.4f, 0.9f, 1f, 1f));
            game.font.draw(game.batch, "[ SKOPIUJ PROFIL ]", px, exportY);
        }

        // Import / view profile from clipboard
        float importY = exportY - 55;
        game.font.setColor(new Color(0.8f, 0.6f, 1f, 1f));
        game.font.draw(game.batch, "[ WCZYTAJ PROFIL ZE SCHOWKA ]", px, importY);

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
        // Import profile overlay
        if (importProfileOpen) {
            drawImportProfileOverlay();
        }
    }

    private void drawImportProfileOverlay() {
        float boxW = 520;
        float boxH = 380;
        float boxX = W / 2f - boxW / 2f;
        float boxY = H / 2f - boxH / 2f;

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.85f);
        shapeRenderer.rect(0, 0, W, H);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.08f, 0.08f, 0.2f, 1f);
        shapeRenderer.rect(boxX, boxY, boxW, boxH);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(new Color(0.4f, 0.9f, 1f, 1f));
        shapeRenderer.rect(boxX, boxY, boxW, boxH);
        shapeRenderer.end();

        game.batch.begin();
        float tx = boxX + 20;
        float ty = boxY + boxH - 25;

        game.font.getData().setScale(0.7f);
        game.font.setColor(new Color(0.4f, 0.9f, 1f, 1f));
        game.font.draw(game.batch, "PROFIL ZE SCHOWKA", tx, ty);

        game.font.getData().setScale(0.5f);
        game.font.setColor(Color.WHITE);
        String[] lines = importProfileText.split("\n");
        float lineH = 26f;
        float curY = ty - 35;
        for (String line : lines) {
            if (curY < boxY + 40) break;
            if (!line.startsWith("===")) {
                game.font.draw(game.batch, line, tx, curY);
                curY -= lineH;
            }
        }

        game.font.getData().setScale(0.45f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "Kliknij gdziekolwiek, zeby zamknac", tx, boxY + 20);

        game.font.getData().setScale(1f);
        game.batch.end();

        if (Gdx.input.justTouched()) {
            importProfileOpen = false;
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
            // Done — solo goes to hub; multiplayer modes go directly to battle
            if (game.multiplayerMode > 0) {
                game.setScreen(new BattleScreen(game));
            } else {
                game.setScreen(new HubScreen(game));
            }
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
    @Override public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                if (equipmentOpen) {
                    float maxScroll = Math.max(0, ABILITY_NAMES.length * EQUIP_ROW_H - equipListVisibleH());
                    equipScrollOffset = MathUtils.clamp(equipScrollOffset + amountY * 40f, 0, maxScroll);
                }
                return true;
            }
        });
    }

    private float equipListVisibleH() {
        // List area height (bottom of list = panelY+80, top = panelY+panelH-320)
        return (60 + 870 - 320) - (60 + 80);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}