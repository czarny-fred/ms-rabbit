package com.example.demogame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class BattleGame extends Game {

    public static final int WIDTH = 1300;
    public static final int HEIGHT = 1000;

    public SpriteBatch batch;
    public BitmapFont font;
    public BitmapFont menuFont;
    public Texture backgroundTex;
    public Texture backgroundTex2;
    public Texture playerRightTex;
    public Texture playerLeftTex;
    public Texture enemyTex;
    public Texture enemySpecialTex;
    public Texture iwonkaUmieranie1Tex;
    public Texture iwonkaUmieranie2Tex;
    public Texture[] weaponBulletTex;
    public Texture enemyBulletTex;
    public Texture reflectedBulletTex;
    public Texture platformTex;
    public Texture miniEnemyTex;
    public Texture mapTex;
    public Texture startBgTex;
    public Texture demonBgTex;
    public Texture demonTex;
    public Texture kebabTex;
    public Texture portalTex;
    public Texture udkoTex;
    public Texture golabTex;
    public Texture playerKropidloTex;
    public Texture playerKadzidloTex;
    public Texture playerBibliaTex;
    public Texture playerKropidloLeftTex;
    public Texture playerKadzidloLeftTex;
    public Texture playerBibliaLeftTex;
    public Texture playerLeczenieTex;
    public Texture dropTex;
    public Texture zatrucieTex;
    public Texture przedstawienieTex;
    public Texture przedstawienieIwonkaTex;
    public Texture aniolekTex;
    public Texture ksiegaModlitwTex;
    public Texture demonTelefonBgTex;
    public Texture demonTelefonTex;
    public Texture demonTelefonAtakTex;
    public Texture telefonBulletTex;
    public Texture kasaTex;
    public Texture czystoscUpgradeTex;
    public Texture hpUpgradeTex;
    public Texture sklepTex;
    public Texture przedstawienieTelefonTex;
    // ZlyOgrodnik (level 2 boss)
    public Texture zlyOgrodnikTloTex;
    public Texture zlyOgrodnikTex;
    public Texture zlyOgrodnikLewoTex;
    public Texture zlyOgrodnikAtakTex;
    public Texture zlyOgrodnikSmierc1Tex;
    public Texture zlyOgrodnikSmierc2Tex;
    // Kucanie textures
    public Texture playerKucanieTex;
    public Texture playerKucanieLeftTex;
    // Bishop skin textures
    public Texture biskupTex;
    public Texture biskupKropidloTex;
    public Texture biskupKadzidloTex;
    public Texture biskupKsiegaTex;
    public Texture biskupKropidloLeftTex;
    public Texture biskupKadzidloLeftTex;
    public Texture biskupKsiegaLeftTex;
    public Texture biskupLeczenieTex;
    // Relikwie (levels 1-5)
    public Texture[] relikwiaTex;
    // Selected abilities (3 slots, indices: 0=Odbicie,1=Leczenie,2=Oswiecenie,3=Modlitwa,4=Zdrowas,5=Skok)
    public int[] selectedAbilities = {0, 1, 2};

    // Crates (shop)
    public Texture skrzynka1Tex;
    public Texture skrzynka2Tex;
    public Texture skrzynkaOtwartaTex;
    // Profile pictures
    public Texture[] profiloweTex;
    public int profilePictureIndex = -1;
    // Siostry Księgarni (level 3 bosses)
    public Texture siostryKsiegarniTex;
    public Texture siostryKsiegarniAtak1Tex;
    public Texture siostryKsiegarniAtak2Tex;
    public Music backgroundMusic;
    public Music battleMusic;
    public Sound clickSound;
    public int highScore;
    public int money;
    public int hpUpgradePrice = 700;
    public int hpUpgradeAmount = 30;
    public int playerBonusHp = 0;      // total HP bonus from all purchased upgrades
    public int czystoscUpgradePrice = 1000;
    public int czystoscUpgradeAmount = 50;
    public int czystoscBonusMax = 0;   // total czystosc bonus from all purchased upgrades
    public boolean bishopSkin;
    public String playerName = "Gracz";
    public boolean tutorialDone;
    public boolean seenIwonka;
    public boolean seenDemon;
    public boolean seenTelefon;
    public float musicVolume = 0.5f;
    public float clickVolume = 1.0f;
    public int currentProfileId = 0;
    public int profileCount = 1;
    public int[] recentScores = new int[10];
    public int multiplayerMode = 0; // 0=solo, 1=coop, 2=vs

    // Loading steps - each step loads one or more assets
    private Runnable[] loadingSteps;
    private int loadingStep;
    public int totalLoadingSteps;

    @Override
    public void create() {
        batch = new SpriteBatch();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("czcionka/lady_radical/ARCADEPI.TTF"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 24;
        param.color = Color.WHITE;
        font = generator.generateFont(param);
        generator.dispose();

        FreeTypeFontGenerator menuGen = new FreeTypeFontGenerator(Gdx.files.internal("czcionka/lady_radical/Lady Radical.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter menuParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        menuParam.size = 48;
        menuParam.color = Color.WHITE;
        menuFont = menuGen.generateFont(menuParam);
        menuGen.dispose();

        buildLoadingSteps();
        loadData();
        setScreen(new LoadingScreen(this));
    }

    private void buildLoadingSteps() {
        loadingSteps = new Runnable[] {
            () -> backgroundTex = new Texture(Gdx.files.internal("tlo/tlo-dark.png")),
            () -> backgroundTex2 = backgroundTex,
            () -> playerRightTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-prawo.png")),
            () -> playerLeftTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-lewo.png")),
            () -> enemyTex = new Texture(Gdx.files.internal("postacie/iwonka/iwonka.png")),
            () -> enemySpecialTex = new Texture(Gdx.files.internal("postacie/iwonka/umiejetnosc-duzy-krolik.png")),
            () -> iwonkaUmieranie1Tex = new Texture(Gdx.files.internal("postacie/iwonka/iwonka-umieranie-1.png")),
            () -> iwonkaUmieranie2Tex = new Texture(Gdx.files.internal("postacie/iwonka/iwonka-umieranie-2.png")),
            () -> weaponBulletTex = new Texture[] {
                    new Texture(Gdx.files.internal("bronie/krople-kropidlo.png")),
                    new Texture(Gdx.files.internal("bronie/dymek-kadzidlo.png")),
                    createBulletTexture(new Color(0.8f, 0.2f, 1f, 1f), 12),
            },
            () -> enemyBulletTex = new Texture(Gdx.files.internal("bronie/krolik.png")),
            () -> reflectedBulletTex = createBulletTexture(new Color(0.2f, 0.5f, 1f, 1f), 100),
            () -> { platformTex = createPlatformTexture(); miniEnemyTex = createMiniEnemyTexture(); },
            () -> mapTex = new Texture(Gdx.files.internal("tlo/lepsza-mapa.png")),
            () -> startBgTex = new Texture(Gdx.files.internal("tlo/tlo-start.png")),
            () -> demonBgTex = new Texture(Gdx.files.internal("tlo/tlo-demon-jedzenie.png")),
            () -> demonTex = new Texture(Gdx.files.internal("postacie/demon-jedzenie-2.png")),
            () -> kebabTex = new Texture(Gdx.files.internal("bronie/kebab.png")),
            () -> portalTex = new Texture(Gdx.files.internal("tlo/portal.png")),
            () -> udkoTex = new Texture(Gdx.files.internal("bronie/udko-z-kurczaka.png")),
            () -> golabTex = new Texture(Gdx.files.internal("bronie/golab.png")),
            () -> playerKropidloTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-kropidlo.png")),
            () -> playerKadzidloTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-kadzidlo.png")),
            () -> playerBibliaTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-biblia.png")),
            () -> playerKropidloLeftTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-kropidlo-lewo.png")),
            () -> playerKadzidloLeftTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-kadzidlo-lewo.png")),
            () -> playerBibliaLeftTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-biblia-lewo.png")),
            () -> playerLeczenieTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-leczenie.png")),
            () -> dropTex = new Texture(Gdx.files.internal("bronie/drop-z-wroga.png")),
            () -> zatrucieTex = new Texture(Gdx.files.internal("bronie/zatrucie.png")),
            () -> przedstawienieTex = new Texture(Gdx.files.internal("tlo/przedstawienie-demon-jedzenie.png")),
            () -> przedstawienieIwonkaTex = new Texture(Gdx.files.internal("tlo/przedstawienie-iwonka.png")),
            () -> aniolekTex = new Texture(Gdx.files.internal("aniolek.png")),
            () -> ksiegaModlitwTex = new Texture(Gdx.files.internal("bronie/ksiega-z-modlitwami.png")),
            () -> demonTelefonBgTex = new Texture(Gdx.files.internal("tlo/demon-telefon-tlo.png")),
            () -> demonTelefonTex = new Texture(Gdx.files.internal("postacie/demon-telefon/demon-telefon.png")),
            () -> demonTelefonAtakTex = new Texture(Gdx.files.internal("postacie/demon-telefon/demon-telefon-atak.png")),
            () -> telefonBulletTex = new Texture(Gdx.files.internal("bronie/telefon.png")),
            () -> kasaTex = new Texture(Gdx.files.internal("tlo/kasa.png")),
            () -> czystoscUpgradeTex = new Texture(Gdx.files.internal("bronie/mana-upgrade.png")),
            () -> hpUpgradeTex = new Texture(Gdx.files.internal("bronie/HP-upgrade.png")),
            () -> sklepTex = new Texture(Gdx.files.internal("tlo/sklep.png")),
            () -> przedstawienieTelefonTex = new Texture(Gdx.files.internal("tlo/przedstawienie-demon-telefon.png")),
            () -> zlyOgrodnikTloTex = new Texture(Gdx.files.internal("tlo/ZlyOgrodnikTlo.png")),
            () -> zlyOgrodnikTex = new Texture(Gdx.files.internal("postacie/MiniBossy/ZlyOgrodnik.png")),
            () -> zlyOgrodnikLewoTex = new Texture(Gdx.files.internal("postacie/MiniBossy/ZlyOgrodnikLewo.png")),
            () -> zlyOgrodnikAtakTex = new Texture(Gdx.files.internal("postacie/MiniBossy/ZlyOgrodnikAtak.png")),
            () -> zlyOgrodnikSmierc1Tex = new Texture(Gdx.files.internal("postacie/MiniBossy/ZlyOgrodnikSmierc-1.png")),
            () -> zlyOgrodnikSmierc2Tex = new Texture(Gdx.files.internal("postacie/MiniBossy/ZlyOgordnikSmierc-2.png")),
            () -> playerKucanieTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-kucanie.png")),
            () -> playerKucanieLeftTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-kucanie-lewo.png")),
            () -> biskupTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-biskup.png")),
            () -> biskupKropidloTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-biskup-kropidlo.png")),
            () -> biskupKadzidloTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-biskup-kadzidlo.png")),
            () -> biskupKsiegaTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-biskup-ksiega.png")),
            () -> biskupKropidloLeftTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-biskup-kropidlo-lewo.png")),
            () -> biskupKadzidloLeftTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-biskup-kadzidlo-lewo.png")),
            () -> biskupKsiegaLeftTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-biskup-ksiega-lewo.png")),
            () -> biskupLeczenieTex = new Texture(Gdx.files.internal("postacie/ksiadz/ksiadz-biskup-leczenie.png")),
            () -> siostryKsiegarniTex = new Texture(Gdx.files.internal("postacie/MiniBossy/SiostryKsiegarni.png")),
            () -> siostryKsiegarniAtak1Tex = new Texture(Gdx.files.internal("postacie/MiniBossy/SiostryKsiegarniAtak1.png")),
            () -> siostryKsiegarniAtak2Tex = new Texture(Gdx.files.internal("postacie/MiniBossy/SiostryKsiegarniAtak2.png")),
            () -> relikwiaTex = new Texture[] {
                new Texture(Gdx.files.internal("bronie/relikwia_1-removebg-preview.png")),
                new Texture(Gdx.files.internal("bronie/relikwia_2-removebg-preview.png")),
                new Texture(Gdx.files.internal("bronie/relikwia_3-removebg-preview.png")),
                new Texture(Gdx.files.internal("bronie/relikwia_4-removebg-preview.png")),
                new Texture(Gdx.files.internal("bronie/relikwia_5-removebg-preview.png")),
            },
            () -> skrzynka1Tex = new Texture(Gdx.files.internal("all/skrzynka1.png")),
            () -> skrzynka2Tex = new Texture(Gdx.files.internal("all/skrzynka2.png")),
            () -> skrzynkaOtwartaTex = new Texture(Gdx.files.internal("all/sskrzynka otwarta1.png")),
            () -> profiloweTex = new Texture[] {
                new Texture(Gdx.files.internal("all/profillowe1.png")),
                new Texture(Gdx.files.internal("all/profilowe2.png")),
                new Texture(Gdx.files.internal("all/profilowe3.png")),
                new Texture(Gdx.files.internal("all/profilowe4.png")),
            },
            () -> clickSound = Gdx.audio.newSound(Gdx.files.internal("muzyka/freesound_community-camera-shutter-95121 (1).mp3")),
            () -> {
                backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("muzyka/Game soundtrack main menu-kopia.mp3"));
                backgroundMusic.setLooping(true);
                backgroundMusic.setVolume(0.5f);
                backgroundMusic.play();
            },
            () -> {
                battleMusic = Gdx.audio.newMusic(Gdx.files.internal("muzyka/Game soundtrack Rabbit Queen Lullaby.mp3"));
                battleMusic.setLooping(true);
                battleMusic.setVolume(musicVolume);
            },
        };
        loadingStep = 0;
        totalLoadingSteps = loadingSteps.length;
    }

    /** Ustawia linear filtering na wszystkich teksturach (wygładza grafikę) */
    public void applyLinearFiltering() {
        Texture[] all = {
            backgroundTex, backgroundTex2, playerRightTex, playerLeftTex,
            playerKucanieTex, playerKucanieLeftTex,
            enemyTex, enemySpecialTex, iwonkaUmieranie1Tex, iwonkaUmieranie2Tex, enemyBulletTex, reflectedBulletTex,
            platformTex, miniEnemyTex, mapTex, startBgTex, demonBgTex,
            demonTex, kebabTex, portalTex, udkoTex, golabTex,
            playerKropidloTex, playerKadzidloTex, playerBibliaTex,
            playerKropidloLeftTex, playerKadzidloLeftTex, playerBibliaLeftTex,
            playerLeczenieTex, dropTex, zatrucieTex, przedstawienieTex,
            przedstawienieIwonkaTex, aniolekTex, ksiegaModlitwTex,
            demonTelefonBgTex, demonTelefonTex, demonTelefonAtakTex,
            telefonBulletTex, kasaTex, czystoscUpgradeTex, hpUpgradeTex,
            sklepTex, przedstawienieTelefonTex,
            zlyOgrodnikTloTex, zlyOgrodnikTex, zlyOgrodnikLewoTex, zlyOgrodnikAtakTex, zlyOgrodnikSmierc1Tex, zlyOgrodnikSmierc2Tex,
            biskupTex, biskupKropidloTex,
            biskupKadzidloTex, biskupKsiegaTex, biskupKropidloLeftTex,
            biskupKadzidloLeftTex, biskupKsiegaLeftTex, biskupLeczenieTex,
            siostryKsiegarniTex, siostryKsiegarniAtak1Tex, siostryKsiegarniAtak2Tex
        };
        for (Texture t : all) {
            if (t != null) t.setFilter(
                com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,
                com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
        }
        for (Texture t : weaponBulletTex) {
            if (t != null) t.setFilter(
                com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,
                com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
        }
        if (skrzynka1Tex != null) skrzynka1Tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        if (skrzynka2Tex != null) skrzynka2Tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        if (skrzynkaOtwartaTex != null) skrzynkaOtwartaTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        if (profiloweTex != null) for (Texture t : profiloweTex) {
            if (t != null) t.setFilter(
                com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,
                com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
        }
    }

    /** Loads next asset, returns true when all done */
    public boolean loadNext() {
        if (loadingStep < loadingSteps.length) {
            loadingSteps[loadingStep].run();
            loadingStep++;
        }
        return loadingStep >= loadingSteps.length;
    }

    public float getLoadingProgress() {
        return (float) loadingStep / totalLoadingSteps;
    }

    public void saveData() {
        if (highScore >= 300000) bishopSkin = true;
        Preferences prefs = Gdx.app.getPreferences("BattleGameSave");
        prefs.putInteger("currentProfileId", currentProfileId);
        prefs.putInteger("profileCount", profileCount);
        String p = "profile_" + currentProfileId + "_";
        prefs.putInteger(p + "highScore", highScore);
        prefs.putInteger(p + "money", money);
        prefs.putInteger(p + "hpUpgradePrice", hpUpgradePrice);
        prefs.putInteger(p + "hpUpgradeAmount", hpUpgradeAmount);
        prefs.putInteger(p + "playerBonusHp", playerBonusHp);
        prefs.putInteger(p + "czystoscUpgradePrice", czystoscUpgradePrice);
        prefs.putInteger(p + "czystoscUpgradeAmount", czystoscUpgradeAmount);
        prefs.putInteger(p + "czystoscBonusMax", czystoscBonusMax);
        prefs.putBoolean(p + "bishopSkin", bishopSkin);
        prefs.putString(p + "playerName", playerName);
        prefs.putBoolean(p + "tutorialDone", tutorialDone);
        prefs.putBoolean(p + "seenIwonka", seenIwonka);
        prefs.putBoolean(p + "seenDemon", seenDemon);
        prefs.putBoolean(p + "seenTelefon", seenTelefon);
        prefs.putFloat(p + "musicVolume", musicVolume);
        prefs.putFloat(p + "clickVolume", clickVolume);
        prefs.putInteger(p + "profilePictureIndex", profilePictureIndex);
        prefs.putInteger(p + "sel0", selectedAbilities[0]);
        prefs.putInteger(p + "sel1", selectedAbilities[1]);
        prefs.putInteger(p + "sel2", selectedAbilities[2]);
        for (int i = 0; i < 10; i++) prefs.putInteger(p + "rs" + i, recentScores[i]);
        prefs.flush();
    }

    public void loadData() {
        Preferences prefs = Gdx.app.getPreferences("BattleGameSave");
        currentProfileId = prefs.getInteger("currentProfileId", 0);
        profileCount = prefs.getInteger("profileCount", 0);
        // Migrate old single-profile data if no profiles exist yet
        if (profileCount == 0) {
            profileCount = 1;
            playerName = prefs.getString("playerName", "Gracz");
            highScore = prefs.getInteger("highScore", 0);
            money = prefs.getInteger("money", 0);
            hpUpgradePrice = prefs.getInteger("hpUpgradePrice", 70);
            hpUpgradeAmount = prefs.getInteger("hpUpgradeAmount", 30);
            playerBonusHp = prefs.getInteger("playerBonusHp", 0);
            czystoscUpgradePrice = prefs.getInteger("czystoscUpgradePrice", 100);
            czystoscUpgradeAmount = prefs.getInteger("czystoscUpgradeAmount", 50);
            czystoscBonusMax = prefs.getInteger("czystoscBonusMax", 0);
            bishopSkin = prefs.getBoolean("bishopSkin", false);
            tutorialDone = true; // existing players skip tutorial
            currentProfileId = 0;
            saveData();
            return;
        }
        loadProfile(currentProfileId);
    }

    public void loadProfile(int profileId) {
        Preferences prefs = Gdx.app.getPreferences("BattleGameSave");
        currentProfileId = profileId;
        String p = "profile_" + profileId + "_";
        highScore = prefs.getInteger(p + "highScore", 0);
        money = prefs.getInteger(p + "money", 0);
        hpUpgradePrice = prefs.getInteger(p + "hpUpgradePrice", 70);
        hpUpgradeAmount = prefs.getInteger(p + "hpUpgradeAmount", 30);
        playerBonusHp = prefs.getInteger(p + "playerBonusHp", 0);
        czystoscUpgradePrice = prefs.getInteger(p + "czystoscUpgradePrice", 100);
        czystoscUpgradeAmount = prefs.getInteger(p + "czystoscUpgradeAmount", 50);
        czystoscBonusMax = prefs.getInteger(p + "czystoscBonusMax", 0);
        bishopSkin = prefs.getBoolean(p + "bishopSkin", false);
        playerName = prefs.getString(p + "playerName", "Gracz");
        tutorialDone = prefs.getBoolean(p + "tutorialDone", false);
        seenIwonka = prefs.getBoolean(p + "seenIwonka", false);
        seenDemon = prefs.getBoolean(p + "seenDemon", false);
        seenTelefon = prefs.getBoolean(p + "seenTelefon", false);
        musicVolume = prefs.getFloat(p + "musicVolume", 0.5f);
        clickVolume = prefs.getFloat(p + "clickVolume", 1.0f);
        profilePictureIndex = prefs.getInteger(p + "profilePictureIndex", -1);
        selectedAbilities[0] = prefs.getInteger(p + "sel0", 0);
        selectedAbilities[1] = prefs.getInteger(p + "sel1", 1);
        selectedAbilities[2] = prefs.getInteger(p + "sel2", 2);
        for (int i = 0; i < 10; i++) recentScores[i] = prefs.getInteger(p + "rs" + i, 0);
        if (backgroundMusic != null) backgroundMusic.setVolume(musicVolume);
    }

    public void resetCurrentProfile() {
        highScore = 0;
        money = 0;
        hpUpgradePrice = 70;
        hpUpgradeAmount = 30;
        playerBonusHp = 0;
        czystoscUpgradePrice = 100;
        czystoscUpgradeAmount = 50;
        czystoscBonusMax = 0;
        bishopSkin = false;
        seenIwonka = false;
        seenDemon = false;
        seenTelefon = false;
        profilePictureIndex = -1;
        selectedAbilities[0] = 0; selectedAbilities[1] = 1; selectedAbilities[2] = 2;
        recentScores = new int[10];
        saveData();
    }

    public void addRecentScore(int score) {
        if (score <= 0) return;
        for (int i = 9; i > 0; i--) recentScores[i] = recentScores[i - 1];
        recentScores[0] = score;
        if (score > highScore) highScore = score;
        saveData();
    }

    public String getProfileName(int profileId) {
        Preferences prefs = Gdx.app.getPreferences("BattleGameSave");
        return prefs.getString("profile_" + profileId + "_playerName", "Gracz");
    }

    public int getProfileHighScore(int profileId) {
        Preferences prefs = Gdx.app.getPreferences("BattleGameSave");
        return prefs.getInteger("profile_" + profileId + "_highScore", 0);
    }

    public void createNewProfile(String name) {
        Preferences prefs = Gdx.app.getPreferences("BattleGameSave");
        int newId = profileCount;
        profileCount++;
        currentProfileId = newId;
        playerName = name;
        highScore = 0;
        money = 0;
        hpUpgradePrice = 70;
        hpUpgradeAmount = 30;
        playerBonusHp = 0;
        czystoscUpgradePrice = 100;
        czystoscUpgradeAmount = 50;
        czystoscBonusMax = 0;
        bishopSkin = false;
        tutorialDone = false;
        prefs.putInteger("profileCount", profileCount);
        prefs.flush();
        saveData();
    }

    public Color getPlayerClassColor() {
        if (highScore >= 300000) return new Color(1f, 0.84f, 0f, 1f); // zloty
        if (highScore >= 250000) return new Color(0.6f, 0.2f, 0.8f, 1f); // fioletowy
        if (highScore >= 200000) return new Color(1f, 0.4f, 0.7f, 1f); // rozowy
        if (highScore >= 100000) return new Color(0.2f, 0.9f, 0.2f, 1f); // zielony
        if (highScore >= 50000) return Color.WHITE; // bialy
        return Color.WHITE; // bialy
    }

    public String getPlayerClassName() {
        if (highScore >= 300000) return "Zloty";
        if (highScore >= 250000) return "Fioletowy";
        if (highScore >= 200000) return "Rozowy";
        if (highScore >= 100000) return "Zielony";
        return "None";
    }

    private Texture createBulletTexture(Color color, int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillCircle(size / 2, size / 2, size / 2 - 1);
        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(size / 2 - 1, size / 2 - 1, size / 6);
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private Texture createPlatformTexture() {
        int w = 16, h = 16;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.45f, 0.3f, 0.15f, 1f));
        pixmap.fill();
        pixmap.setColor(new Color(0.55f, 0.38f, 0.2f, 1f));
        pixmap.fillRectangle(0, 0, w, 4);
        pixmap.setColor(new Color(0.35f, 0.22f, 0.1f, 1f));
        pixmap.drawLine(0, 4, w, 4);
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private Texture createMiniEnemyTexture() {
        Pixmap pixmap = new Pixmap(24, 24, Pixmap.Format.RGBA8888);
        // Small red-orange creature
        pixmap.setColor(new Color(0.9f, 0.3f, 0.1f, 1f));
        pixmap.fillCircle(12, 14, 9);
        // Eyes
        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(8, 12, 3);
        pixmap.fillCircle(16, 12, 3);
        pixmap.setColor(Color.BLACK);
        pixmap.fillCircle(9, 12, 1);
        pixmap.fillCircle(17, 12, 1);
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        menuFont.dispose();
        backgroundTex.dispose();
        // backgroundTex2 is the same reference as backgroundTex — do not double-dispose
        playerRightTex.dispose();
        playerLeftTex.dispose();
        playerKucanieTex.dispose();
        playerKucanieLeftTex.dispose();
        enemyTex.dispose();
        iwonkaUmieranie1Tex.dispose();
        iwonkaUmieranie2Tex.dispose();
        for (Texture t : weaponBulletTex) t.dispose();
        enemyBulletTex.dispose();
        reflectedBulletTex.dispose();
        platformTex.dispose();
        miniEnemyTex.dispose();
        mapTex.dispose();
        startBgTex.dispose();
        demonBgTex.dispose();
        demonTex.dispose();
        kebabTex.dispose();
        portalTex.dispose();
        udkoTex.dispose();
        golabTex.dispose();
        playerKropidloTex.dispose();
        playerKadzidloTex.dispose();
        playerBibliaTex.dispose();
        playerKropidloLeftTex.dispose();
        playerKadzidloLeftTex.dispose();
        playerBibliaLeftTex.dispose();
        playerLeczenieTex.dispose();
        dropTex.dispose();
        zatrucieTex.dispose();
        przedstawienieTex.dispose();
        przedstawienieIwonkaTex.dispose();
        aniolekTex.dispose();
        ksiegaModlitwTex.dispose();
        demonTelefonBgTex.dispose();
        demonTelefonTex.dispose();
        demonTelefonAtakTex.dispose();
        telefonBulletTex.dispose();
        kasaTex.dispose();
        czystoscUpgradeTex.dispose();
        hpUpgradeTex.dispose();
        sklepTex.dispose();
        przedstawienieTelefonTex.dispose();
        zlyOgrodnikTloTex.dispose();
        zlyOgrodnikTex.dispose();
        zlyOgrodnikLewoTex.dispose();
        zlyOgrodnikAtakTex.dispose();
        zlyOgrodnikSmierc1Tex.dispose();
        zlyOgrodnikSmierc2Tex.dispose();
        biskupTex.dispose();
        biskupKropidloTex.dispose();
        biskupKadzidloTex.dispose();
        biskupKsiegaTex.dispose();
        biskupKropidloLeftTex.dispose();
        biskupKadzidloLeftTex.dispose();
        biskupKsiegaLeftTex.dispose();
        biskupLeczenieTex.dispose();
        siostryKsiegarniTex.dispose();
        siostryKsiegarniAtak1Tex.dispose();
        siostryKsiegarniAtak2Tex.dispose();
        if (relikwiaTex != null) for (Texture t : relikwiaTex) if (t != null) t.dispose();
        if (skrzynka1Tex != null) skrzynka1Tex.dispose();
        if (skrzynka2Tex != null) skrzynka2Tex.dispose();
        if (skrzynkaOtwartaTex != null) skrzynkaOtwartaTex.dispose();
        if (profiloweTex != null) for (Texture t : profiloweTex) if (t != null) t.dispose();
        backgroundMusic.dispose();
        if (battleMusic != null) battleMusic.dispose();
        clickSound.dispose();
    }
}
