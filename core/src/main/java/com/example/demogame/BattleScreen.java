package com.example.demogame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class BattleScreen implements Screen {

    private static final int W = BattleGame.WIDTH;
    private static final int H = BattleGame.HEIGHT;
    private static final int WORLD_W = 4000;

    // Player config
    private static final float PLAYER_SPEED = 300f;
    private static final int PLAYER_MAX_HP = 100;
    private static final int PLAYER_W = 300;
    private static final int PLAYER_H = 160;
    private static final float GRAVITY = 800f;
    private static final float JUMP_VELOCITY = 550f;

    private static final int MAX_JUMPS = 2;
    private static final float GROUND_Y = 0f;

    // Weapons: [0] Pistolet (yellow), [1] Karabin (blue), [2] Armata (purple)
    private static final String[] WEAPON_NAMES = {"KROPIDlO", "KADZIDlO", "BIBLIA"};
    private static final float[] WEAPON_COOLDOWNS = {0.3f, 0.12f, 0.8f};
    private static final float[] WEAPON_BULLET_SPEEDS = {350f, 450f, 250f};
    private static final int[] WEAPON_DAMAGE = {10, 3, 30};
    private static final int[] WEAPON_BULLET_SIZES = {60, 60, 12};
    private static final Color[] WEAPON_COLORS = {
            new Color(1f, 0.9f, 0.2f, 1f),
            new Color(0.2f, 0.6f, 1f, 1f),
            new Color(0.8f, 0.2f, 1f, 1f)
    };
    private static final Color CZYSTOSC_COLOR = new Color(0.2f, 0.4f, 1f, 1f);
    private static final Color SHIELD_COLOR  = new Color(1f, 0.8f, 0.1f, 1f);
    private static final int   MAX_SHIELD    = 100;
    private static final int   SHIELD_PER_RELIC = 20;
    private static final float RELIC_SIZE    = 64f;
    private static final int[] WEAPON_MAX_AMMO = {12, 30, 3};
    private static final float[] WEAPON_RELOAD_TIME = {2f, 3f, 4f};
    private static final float PLAYER_BULLET_RANGE = 700f;
    private static final float PLAYER_BULLET_CURVE_DIST = 400f;
    private static final float PLAYER_BULLET_CURVE_ANGLE = 7f; // degrees

    // Kadzidlo burst config
    private static final int KADZIDLO_BURST_COUNT = 5;
    private static final float KADZIDLO_BURST_INTERVAL = 0.02f;
    private static final float KADZIDLO_BURST_COOLDOWN = 0.4f;

    // Enemy config (base values, scaled per level)
    private static final float ENEMY_BULLET_SPEED_BASE = 200f;
    private static final float ENEMY_SHOOT_COOLDOWN_BASE = 1.2f;
    private static final int ENEMY_MAX_HP_BASE = 100;
    private static final float ENEMY_SPEED_BASE = 50f;
    private static final float ENEMY_MIN_DISTANCE = 100f; // min spawn distance from player
    private static final int ENEMY_W = 128;
    private static final int ENEMY_H = 160;
    private static final int OGRODNIK_H = 130;

    // Siostry Księgarni (level 3)
    private static final int SISTER_W = 128;
    private static final int SISTER_H = 160;
    private static final float SISTER_SPEED = 55f;
    private static final float SISTER_JUMP_VELOCITY = 500f;
    private static final float SISTER_JUMP_COOLDOWN = 1.8f;
    private static final int SISTER_MAX_HP = 130;
    private static final float SISTER_SHOOT_COOLDOWN = 1.6f;
    private static final int SISTER_BULLET_DMG = 8;
    private static final int SISTER_BULLET_SIZE = 40;
    private static final float SISTER_SPECIAL_COOLDOWN = 3f;
    // Iwonka song attack
    private static final float SONG_ATTACK_COOLDOWN  = SPECIAL_BULLET_COOLDOWN * 2f; // 2x rarer than big rabbit
    private static final float SONG_WORD_INTERVAL    = 0.35f;
    private static final int   SONG_WORD_DAMAGE      = 10;
    private static final float SONG_WORD_SPEED       = 230f;
    private static final float SONG_WAVE_AMPLITUDE   = 75f;
    private static final float SONG_WAVE_FREQUENCY   = 4.5f;
    private static final String[] SONG_WORDS = {
        "Przez", "twe", "oczy", "zielone,", "przez", "twe",
        "usta", "szkarlatne,", "przez", "te", "lata", "minione"
    };
    private static final int SISTER_SPECIAL_DMG = 20;
    private static final int SISTER_SPECIAL_SIZE = 80;
    private static final float SISTER_WINDUP = 1.5f;
    private static final float SISTER_MIN_SEPARATION = 250f;
    private static final float SISTER_BOOK_W = 64f;
    private static final float SISTER_BOOK_H = 64f;
    // Fazy intro sióstr
    private static final int SINT_BOOK     = 0;
    private static final int SINT_FADEIN   = 1;
    private static final int SINT_TEXT_IN  = 2;
    private static final int SINT_TEXT_HOLD= 3;
    private static final int SINT_TEXT_OUT = 4;
    private static final int SINT_FADEOUT  = 5;
    private static final int SINT_CAM_S1   = 6;
    private static final int SINT_SHAKE_S1 = 7;
    private static final int SINT_CAM_S2   = 8;
    private static final int SINT_SHAKE_S2 = 9;
    private static final int SINT_CAM_BACK = 10;
    private static final float ENEMY_JUMP_VELOCITY = 550f;
    private static final float ENEMY_JUMP_COOLDOWN = 1.5f;

    // Mini enemies (from level 5+)
    private static final int MINI_W = 48;
    private static final int MINI_H = 48;
    private static final float MINI_SPEED = 110f;
    private static final int MINI_HP = 2;
    private static final int MINI_DAMAGE = 1;
    private static final float MINI_SPAWN_COOLDOWN_BASE = 3f;

    // Abilities
    private static final float REFLECT_COOLDOWN = 10f;
    private static final float REFLECT_DURATION = 2f;
    private static final int REFLECT_DAMAGE = 2;
    private static final int MAX_CZYSTOSC = 100;
    private static final int REFLECT_CZYSTOSC_COST = 50;
    private static final float CZYSTOSC_REGEN = 5f; // per second (after 3s delay)
    private static final float CZYSTOSC_REGEN_DELAY = 3f;
    private static final int HEAL_CZYSTOSC_COST = 40;
    // NOTE: mana renamed to czystosc throughout
    private static final float HEAL_PERCENT = 0.15f; // 15% max HP
    private static final float HEAL_COOLDOWN = 17f;
    private static final float HEAL_DURATION = 2f;
    private static final int ENLIGHTEN_CZYSTOSC_COST = 60;
    private static final float ENLIGHTEN_COOLDOWN = 10f;
    private static final float ENLIGHTEN_DURATION = 5f;
    private static final int ENLIGHTEN_DAMAGE = 20;
    private static final int PRAYER_CZYSTOSC_COST = 30;
    private static final float PRAYER_WORD_INTERVAL = 0.5f;
    private static final float PRAYER_WORD_SPEED = 300f;
    private static final int PRAYER_WORD_DAMAGE = 5;
    private static final String[] PRAYER_WORDS = {
            "Ojcze", "nasz", "ktory", "jestes", "w", "niebie",
            "swiec", "sie", "imie", "Twoje", "przyjdz",
            "krolestwo", "Twoje", "badz", "wola", "Twoja"
    };
    // Angel
    private static final int ANGEL_W = 80;
    private static final int ANGEL_H = 80;
    private static final int ANGEL_REFLECT_EVERY = 10; // reflects every 10th enemy shot
    private static final float ANGEL_ENRAGED_DURATION = 20f;
    private static final int ANGEL_ENRAGED_MAX_HP = 50;
    private static final int ANGEL_ENRAGED_DAMAGE = 8;
    private static final float ANGEL_ATTACK_INTERVAL = 0.8f;
    private static final int DEMON_KILLS_FOR_BOOK = 5;
    private static final int ZDROWAS_CZYSTOSC_COST = 40;
    private static final String[] ZDROWAS_WORDS = {
            "Zdrowas", "Mario", "laski", "pelna", "Pan", "z", "Toba",
            "blogoslawionas", "Ty", "miedzy", "niewiastami"
    };

    private static final float DASH_DISTANCE = 99f;
    private static final float DASH_COOLDOWN = 7f;
    private static final float DASH_DURATION = 0.15f;
    private static final float DASH_SPEED = DASH_DISTANCE / DASH_DURATION;

    // Drabina (level 3)
    private static final float LADDER_X = 60f;
    private static final float LADDER_BASE_Y = 320f;   // starts at 3rd platform (PLATFORM_Y2)
    private static final float LADDER_W = 44f;
    private static final float LADDER_H = 450f;
    private static final float LADDER_CLIMB_SPEED = 180f;
    private static final float LADDER_ITEM_SIZE = 50f;
    private static final float LADDER_VISIBLE_DIST = 700f; // only drawn when player is near

    // Sprint (double-tap A or D)
    private static final float SPRINT_DOUBLE_TAP_WINDOW = 0.3f;
    private static final float SPRINT_DURATION = 3f;
    private static final float SPRINT_COOLDOWN = 5f;
    private static final float SPRINT_SPEED_MULT = 1.8f;

    // Hitbox gracza do kolizji pocisków (mniejszy niż sprite, dopasowany do sylwetki)
    private static final int PLAYER_HIT_INSET_X = 90;
    private static final int PLAYER_HIT_INSET_Y = 10;
    private static final int PLAYER_HIT_W = PLAYER_W - 2 * PLAYER_HIT_INSET_X;
    private static final int PLAYER_HIT_H = PLAYER_H - 2 * PLAYER_HIT_INSET_Y;

    private static final int ENEMY_BULLET_SIZE = ENEMY_W / 3;
    private static final float BURST_INTERVAL = 0.15f;
    private static final float SPECIAL_BULLET_COOLDOWN = 10f;
    private static final int SPECIAL_BULLET_SIZE = ENEMY_BULLET_SIZE * 3;
    private static final int SPECIAL_BULLET_DAMAGE = 10;
    private static final float ENEMY_BULLET_RANGE = 1300f;
    private static final float SPECIAL_WINDUP = 2f;

    // Demon special (udko-z-kurczaka)
    private static final float DEMON_SPECIAL_COOLDOWN = 5f;
    private static final int DEMON_SPECIAL_DAMAGE = 20;
    private static final int DEMON_SPECIAL_SIZE = ENEMY_BULLET_SIZE * 2;
    private static final float DEMON_SPECIAL_SPEED_MULT = 1.2f;
    private static final float DEMON_SHOOT_BLOCK = 2f;
    private static final float DEMON_SLOW_DURATION = 5f;
    private static final float DEMON_SLOW_FACTOR = 0.4f; // 40% speed

    // Demon telefon (50 HP more than demon-jedzenie max, 5 more damage)
    private static final int TELEFON_HP_BONUS = 50;
    private static final int TELEFON_DAMAGE_BONUS = 5;
    private static final float TELEFON_SPECIAL_COOLDOWN  = 5f;
    private static final float HYPNO_COOLDOWN             = TELEFON_SPECIAL_COOLDOWN * 2f; // 10s
    private static final float HYPNO_WINDUP               = 1.0f;
    private static final int   HYPNO_WAVE_COUNT           = 3;
    private static final float HYPNO_WAVE_DURATION        = 2.0f;
    private static final float HYPNO_WAVE_SPAWN_INTERVAL  = 0.55f;
    private static final int   HYPNO_DAMAGE               = 30;
    private static final float HYPNO_PLAYER_DURATION      = 3f;
    private static final float HYPNO_SLOW_DURATION        = 15f;
    private static final int TELEFON_SPECIAL_DAMAGE = DEMON_SPECIAL_DAMAGE + TELEFON_DAMAGE_BONUS;
    private static final int TELEFON_SPECIAL_SIZE = DEMON_SPECIAL_SIZE;
    private static final float TELEFON_SPECIAL_SPEED_MULT = 1.3f;

    // Platforms (invisible, pass-through from below)
    private static final float PLATFORM_H = 10f;
    private static final float PLATFORM_Y1 = 160f;
    private static final float PLATFORM_Y2 = 320f;
    private static final float[][] PLATFORMS = {
            {0, PLATFORM_Y2, 200, PLATFORM_H},   // left-end platform for ladder access
            {200, PLATFORM_Y1, 400, PLATFORM_H},
            {800, PLATFORM_Y1, 400, PLATFORM_H},
            {1400, PLATFORM_Y1, 400, PLATFORM_H},
            {2000, PLATFORM_Y1, 400, PLATFORM_H},
            {2600, PLATFORM_Y1, 400, PLATFORM_H},
            {3200, PLATFORM_Y1, 400, PLATFORM_H},
            {500, PLATFORM_Y2, 400, PLATFORM_H},
            {1100, PLATFORM_Y2, 400, PLATFORM_H},
            {1700, PLATFORM_Y2, 400, PLATFORM_H},
            {2300, PLATFORM_Y2, 400, PLATFORM_H},
            {2900, PLATFORM_Y2, 400, PLATFORM_H},
    };

    // Portal
    private static final float PORTAL_W = 250;
    private static final float PORTAL_H = 200;
    private static final float PORTAL_X = WORLD_W - PORTAL_W - 50;

    // Drop
    private static final float DROP_W = 64;
    private static final float DROP_H = 64;

    private final BattleGame game;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapeRenderer;

    // Player statetak
    private final Rectangle player;
    private int playerHp;
    private float playerShootTimer;
    private float playerInvincibleTimer;
    private int currentWeapon; // 0, 1, 2
    private final int[] ammo = new int[3];
    private final float[] reloadTimer = new float[3];
    private float playerVelY;
    private int jumpsLeft;
    private float walkStateTime;
    private boolean playerMoving;
    private boolean playerFacingLeft;
    private float dropThroughTimer;
    private int playerBulletCounter;
    private float playerShootingTimer; // >0 means player is shooting (show weapon texture)
    private int kadzidloBurstRemaining;
    private float kadzidloBurstTimer;
    private float kadzidloDirX, kadzidloDirY;
    private int kadzidloCurrentBurstSize;

    // Enemy state
    private final Rectangle enemy;
    private int enemyHp;
    private int enemyMaxHp;
    private float enemyShootTimer;
    private float enemySpeed;
    private float enemyBulletSpeed;
    private float enemyShootCooldown;
    private float enemyVelY;
    private float enemyJumpTimer;
    private int enemyBurstRemaining;
    private float enemyBurstTimer;
    private float specialBulletTimer;
    private float specialWindupTimer;
    private float demonSpecialTimer;
    private float demonShootBlockTimer;
    private float playerSlowTimer;
    private float   telefonSpecialTimer;
    // Hypnosis beam ability (Telefon)
    private float   hypnoCooldownTimer;
    private boolean hypnoWindupActive;
    private float   hypnoWindupTimer;
    private boolean hypnoBeamActive;
    private float   hypnoBeamTimer;
    private final float[]   hypnoWaveProgress = new float[3];
    private final float[]   hypnoWaveStartX   = new float[3];
    private final float[]   hypnoWaveStartY   = new float[3];
    private final float[]   hypnoWaveEndX     = new float[3];
    private final float[]   hypnoWaveEndY     = new float[3];
    private final boolean[] hypnoWaveActive   = new boolean[3];
    private final boolean[] hypnoWaveHit      = new boolean[3];
    private float   playerHypnoTimer;
    private float   playerAttackSlowTimer;
    private boolean telefonStopped;
    private boolean enemyFacingLeft;
    // ZlyOgrodnik ground-slam attack
    private boolean ogrodnikAtkActive;
    private boolean ogrodnikAtkImpacted; // true once slam has landed (after 1s)
    private float ogrodnikAtkTimer;
    private float ogrodnikAtkX, ogrodnikAtkY;
    // ZlyOgrodnik death animation
    private boolean ogrodnikDying;
    private float ogrodnikDyingTimer;
    private boolean ogrodnikDied;

    // Bullets
    private final Array<Bullet> playerBullets;
    private final Array<Bullet> enemyBullets;

    // Abilities
    private float reflectCooldownTimer;
    private float reflectActiveTimer;
    private int czystosc;
    private float healCooldownTimer;
    private float healActiveTimer;
    private boolean abilityMenuOpen;
    private float czystoscRegenDelayTimer;
    private float czystoscRegenAccumulator;
    private float dashCooldownTimer;
    private float dashTimer;
    private float dashDir;
    private boolean sprintActive;
    private float sprintTimer;
    private float sprintCooldownTimer;
    // Shield / relics
    private int shield;
    private boolean relikwiaDropActive;
    // Song attack (Iwonka)
    private boolean songAttackActive;
    private float   songAttackCooldownTimer;
    private float   songWordTimer;
    private int     songWordIndex;
    // Portal shop shield upgrade
    private boolean portalShieldUpgradeBought;
    private float relikwiaDropX, relikwiaDropY;
    private int relikwiaDropIndex;
    private boolean relikwiaCutsceneActive;
    private float relikwiaCutsceneTimer;

    private boolean playerOnLadder;
    private boolean ladderTopReached;
    private boolean ladderKasaActive;
    private boolean ladderKsiegaActive;
    private float lastPressATimer = 999f;
    private float lastPressDTimer = 999f;
    private float enlightenCooldownTimer;
    private float enlightenActiveTimer;
    private boolean prayerBookOpen;
    private boolean hasPrayerBook;
    private boolean prayerAttackActive;
    private float prayerWordTimer;
    private int prayerWordIndex;
    private boolean prayerBookDropActive;
    private float prayerBookDropX, prayerBookDropY;
    private final Array<PrayerWord> prayerWords = new Array<>();

    // Angel state
    private boolean angelActive;
    private int angelShotCounter; // counts enemy shots, reflects every 10th
    private boolean angelEnraged;
    private float angelEnragedTimer;
    private int angelHp;
    private float angelAttackTimer;
    private int demonKillCount;
    private boolean hasZdrowasBook;
    private boolean demonStopped;
    private boolean zdrowasAttackActive;
    private float zdrowasWordTimer;
    private int zdrowasWordIndex;
    private boolean angelCutsceneActive;
    private float angelCutsceneTimer;
    private boolean angelNaming; // true during name input phase
    private String angelName = "";
    private boolean angelNamed; // true after name is confirmed

    // Money
    private float moneyPopupTimer;
    private int moneyPopupAmount;
    private static final int MONEY_PER_LEVEL = 10;
    private int moneyMultiplier = 1;

    // Mini enemies
    private final Array<MiniEnemy> miniEnemies;
    private float miniSpawnTimer;

    // Game state
    private int level;
    private int score;
    private boolean paused;
    private boolean screenLeft;
    private boolean gameOver;
    private int pauseSelection; // 0 = resume, 1 = quit
    private float scrollAmount;
    private boolean mapOpen;
    private boolean demonLevel; // true for demon-jedzenie levels
    private boolean telefonLevel; // true for demon-telefon levels
    private boolean portalActive; // portal spawned after beating level 5 enemy
    private boolean enemyAlive; // whether enemy is alive on current level
    private float portalTimer; // animation timer
    private boolean portalShopOpen; // shop shown when entering portal
    // Sister level (level 3)
    private boolean sisterLevel;
    private boolean sisterBookActive;
    private boolean sister1Alive, sister2Alive;
    private final Rectangle sister1 = new Rectangle();
    private final Rectangle sister2 = new Rectangle();
    private int sister1Hp, sister2Hp;
    private float sister1VelY, sister2VelY;
    private float sister1JumpTimer, sister2JumpTimer;
    private float sister1ShootTimer, sister2ShootTimer;
    private float sister1SpecialTimer, sister2SpecialTimer;
    private float sister1WindupTimer, sister2WindupTimer;
    private boolean sister1FacingLeft, sister2FacingLeft;
    private float sister1ShootingTimer, sister2ShootingTimer;
    private int sister1BurstRemaining, sister2BurstRemaining;
    private float sister1BurstTimer, sister2BurstTimer;
    // Cutscene sióstr
    private boolean sisterIntroCutsceneActive;
    private int sisterIntroPhase;
    private float sisterIntroTimer;
    private float sisterIntroTextAlpha;
    private float sisterIntroFadeAlpha;
    private float sisterIntroCamX;
    private float sisterIntroCamTargetX;
    private float healParticleAngle; // for animating heal lung particles
    private final Array<HealPlus> healPlusses = new Array<>();
    private float healPlusSpawnTimer;
    private float fadeAlpha = 0f;
    private boolean fadingToBlack = false;
    private boolean fadingFromBlack = false;
    private static final float FADE_SPEED = 1.5f;
    private boolean iwonkaDying = false;
    private float iwonkaDyingTimer = 0f;
    private boolean iwonkaDied = false; // prevents re-triggering death animation
    private boolean dropActive;
    private float dropX, dropY;
    private final Rectangle portalRect = new Rectangle();
    private final Rectangle dropRect = new Rectangle();
    private final Vector3 mouseTemp = new Vector3();
    private final Rectangle playerHitbox = new Rectangle();
    private final Rectangle prayerDropRectReuse = new Rectangle();
    private boolean playerCrouching;

    public BattleScreen(BattleGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, W, H);
        shapeRenderer = new ShapeRenderer();

        player = new Rectangle(W / 2f - PLAYER_W / 2f, 30, PLAYER_W, PLAYER_H);
        enemy = new Rectangle(W / 2f - ENEMY_W / 2f, H - ENEMY_H - 40, ENEMY_W, ENEMY_H);

        playerBullets = new Array<>();
        enemyBullets = new Array<>();
        miniEnemies = new Array<>();

        level = 1;
        score = 0;
        playerHp = effectiveMaxHp();
        czystosc = effectiveMaxCzystosc();
        for (int i = 0; i < 3; i++) ammo[i] = WEAPON_MAX_AMMO[i];

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                scrollAmount = amountY;
                return true;
            }
        });

        fadingFromBlack = true;
        fadeAlpha = 1f;
        setupLevel();

        // Muzyka bitewna
        if (game.battleMusic != null) {
            game.backgroundMusic.stop();
            game.battleMusic.setVolume(game.musicVolume);
            game.battleMusic.play();
        }
    }

    private void setupLevel() {
        sisterLevel = (level == 3);
        telefonLevel = (level >= 6 && demonStopped);
        demonLevel = (level >= 6 && !demonStopped);
        portalActive = false;
        dropActive = false;
        enemyFacingLeft = false;
        ogrodnikAtkActive = false;
        ogrodnikAtkImpacted = false;
        ogrodnikAtkTimer = 0f;
        ogrodnikDying = false;
        ogrodnikDyingTimer = 0f;
        ogrodnikDied = false;
        if (sisterLevel) {
            enemyAlive = false;
        } else if (telefonLevel) {
            enemyAlive = !telefonStopped;
        } else if (demonLevel) {
            enemyAlive = !demonStopped;
        } else {
            enemyAlive = true;
        }
        portalTimer = 0;
        iwonkaDying = false;
        iwonkaDyingTimer = 0f;
        iwonkaDied = false;
        camera.zoom = 1.0f;
        if (telefonLevel) {
            // Demon telefon: 50 HP more than demon-jedzenie's best HP
            int demonBestHp = ENEMY_MAX_HP_BASE + (level - 2) * 20; // HP demon had before transition
            enemyMaxHp = demonBestHp + TELEFON_HP_BONUS;
        } else {
            enemyMaxHp = ENEMY_MAX_HP_BASE + (level - 1) * 20;
        }
        enemyHp = enemyMaxHp;
        enemySpeed = ENEMY_SPEED_BASE;
        enemyBulletSpeed = ENEMY_BULLET_SPEED_BASE + (level - 1) * 30f;
        enemyShootCooldown = 1.5f;
        enemyShootTimer = 0;
        enemyVelY = 0;
        enemyJumpTimer = 0;
        specialWindupTimer = 0;

        // Mark boss as seen
        if (telefonLevel && !game.seenTelefon) { game.seenTelefon = true; game.saveData(); }
        else if (demonLevel && !game.seenDemon) { game.seenDemon = true; game.saveData(); }
        else if (!demonLevel && !telefonLevel && !game.seenIwonka) { game.seenIwonka = true; game.saveData(); }

        if (!sisterLevel) spawnEnemyAtRandomPosition();

        relikwiaDropActive = false;
        relikwiaCutsceneActive = false;
        relikwiaCutsceneTimer = 0f;
        songAttackActive = false;
        songAttackCooldownTimer = SONG_ATTACK_COOLDOWN;
        songWordTimer = 0f;
        songWordIndex = 0;
        portalShieldUpgradeBought = false;
        enemyBullets.clear();
        playerBullets.clear();
        miniEnemies.clear();
        prayerWords.clear();
        prayerAttackActive = false;
        prayerBookOpen = false;
        zdrowasAttackActive = false;
        miniSpawnTimer = MINI_SPAWN_COOLDOWN_BASE;
        specialBulletTimer = SPECIAL_BULLET_COOLDOWN;
        demonSpecialTimer = DEMON_SPECIAL_COOLDOWN;
        demonShootBlockTimer = 0;
        telefonSpecialTimer = TELEFON_SPECIAL_COOLDOWN;
        hypnoCooldownTimer = HYPNO_COOLDOWN;
        hypnoWindupActive = false;
        hypnoBeamActive   = false;
        hypnoBeamTimer    = 0f;
        playerHypnoTimer  = 0f;
        playerAttackSlowTimer = 0f;
        for (int i = 0; i < 3; i++) { hypnoWaveActive[i] = false; hypnoWaveHit[i] = false; }

        if (sisterLevel) {
            sisterBookActive = true;
            sister1Alive = false;
            sister2Alive = false;
            sisterIntroCutsceneActive = false;
            sisterIntroPhase = SINT_BOOK;
            sisterIntroFadeAlpha = 0f;
            sisterIntroTextAlpha = 0f;
            sister1ShootTimer = SISTER_SHOOT_COOLDOWN + 1f;
            sister2ShootTimer = SISTER_SHOOT_COOLDOWN + 1f;
            sister1SpecialTimer = SISTER_SPECIAL_COOLDOWN;
            sister2SpecialTimer = SISTER_SPECIAL_COOLDOWN;
            sister1WindupTimer = 0f;
            sister2WindupTimer = 0f;
            sister1BurstRemaining = 0;
            sister2BurstRemaining = 0;
            sister1Hp = SISTER_MAX_HP;
            sister2Hp = SISTER_MAX_HP;
            sister1VelY = 0; sister2VelY = 0;
            sister1JumpTimer = 0; sister2JumpTimer = 0;
            sister1.set(80, GROUND_Y, SISTER_W, SISTER_H);
            sister2.set(WORLD_W - SISTER_W - 80, GROUND_Y, SISTER_W, SISTER_H);
            playerOnLadder = false;
            ladderTopReached = false;
            ladderKasaActive = false;
            ladderKsiegaActive = false;
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        // Camera follows player, clamped to world bounds
        if (sisterIntroCutsceneActive) {
            sisterIntroCamX += (sisterIntroCamTargetX - sisterIntroCamX) * Math.min(1f, 4f * delta);
            camera.position.x = MathUtils.clamp(sisterIntroCamX, W / 2f, WORLD_W - W / 2f);
            camera.position.y = H / 2f;
            camera.zoom = 1.0f;
            if (sisterIntroPhase == SINT_SHAKE_S1 || sisterIntroPhase == SINT_SHAKE_S2) {
                camera.position.x += MathUtils.random(-8f, 8f);
                camera.position.y += MathUtils.random(-5f, 5f);
            }
        } else if (iwonkaDying || ogrodnikDying) {
            float targetX = MathUtils.clamp(enemy.x + ENEMY_W / 2f, W / 2f, WORLD_W - W / 2f);
            camera.position.x += (targetX - camera.position.x) * Math.min(1f, 20f * delta);
            camera.position.y += (H / 2f - camera.position.y) * Math.min(1f, 20f * delta);
            camera.zoom += (0.45f - camera.zoom) * Math.min(1f, 20f * delta);
        } else {
            camera.position.x = MathUtils.clamp(player.x + PLAYER_W / 2f, W / 2f, WORLD_W - W / 2f);
            camera.position.y = H / 2f;
            camera.zoom = 1.0f;
        }
        // Screen shake during ZlyOgrodnik ground-slam
        if (ogrodnikAtkActive) {
            float strength = ogrodnikAtkTimer > 1.5f ? 10f : 5f;
            camera.position.x += MathUtils.random(-strength, strength);
            camera.position.y += MathUtils.random(-strength, strength);
        }
        camera.update();

        // Toggle map
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            mapOpen = !mapOpen;
        }

        // Toggle ability menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && !paused && !gameOver) {
            abilityMenuOpen = !abilityMenuOpen;
        }

        // Handle pause toggle / back to menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (prayerBookOpen) {
                prayerBookOpen = false;
            } else if (abilityMenuOpen) {
                abilityMenuOpen = false;
            } else if (gameOver) {
                goToMenu();
                return;
            } else {
                paused = !paused;
                pauseSelection = 0;
            }
        }

        if (screenLeft) return;

        // Money popup timer
        if (moneyPopupTimer > 0) moneyPopupTimer -= delta;

        // Angel cutscene: pause game, animate, wait for Enter, then name input
        if (relikwiaCutsceneActive) {
            relikwiaCutsceneTimer += delta;
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                relikwiaCutsceneActive = false;
            }
        } else if (angelCutsceneActive) {
            angelCutsceneTimer += delta;
            if (!angelNaming) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    angelNaming = true;
                    angelName = "";
                }
            } else {
                // Name input phase
                handleAngelNameInput();
            }
        } else if (!paused && !gameOver && !mapOpen && !fadingToBlack && !fadingFromBlack && !iwonkaDying && !ogrodnikDying && !portalShopOpen && !sisterIntroCutsceneActive) {
            update(delta);
        } else if (paused) {
            handlePauseInput();
        } else {
            handleGameOverInput();
        }

        handleFade(delta);
        updateIwonkaDeath(delta);
        updateOgrodnikDeath(delta);
        if (sisterLevel) updateSisterIntro(delta);

        if (screenLeft) return;

        // Portal shop: show black screen with shop items
        if (portalShopOpen) {
            handlePortalShopInput();
            drawPortalShop();
            return;
        }

        // Draw everything
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Background
        if (telefonLevel) {
            for (int bx = 0; bx < WORLD_W; bx += W) {
                game.batch.draw(game.demonTelefonBgTex, bx, 0, W, H);
            }
        } else if (demonLevel) {
            for (int bx = 0; bx < WORLD_W; bx += W) {
                game.batch.draw(game.demonBgTex, bx, 0, W, H);
            }
        } else if (level == 2) {
            for (int bx = 0; bx < WORLD_W; bx += W) {
                game.batch.draw(game.zlyOgrodnikTloTex, bx, 0, W, H);
            }
        } else {
            game.batch.draw(game.backgroundTex, 0, 0, W, H);
            for (int bx = W; bx < WORLD_W; bx += W) {
                game.batch.draw(game.backgroundTex2, bx, 0, W, H);
            }
        }

        // Player (blink when invincible, texture changes based on current weapon, healing, or prayer)
        if (playerInvincibleTimer <= 0 || (int) (playerInvincibleTimer * 10) % 2 == 0) {
            Texture playerTex;
            boolean bp = game.bishopSkin;
            game.batch.setColor(0.75f, 0.75f, 0.75f, 1f);
            if (prayerAttackActive || healActiveTimer > 0) {
                playerTex = bp ? game.biskupLeczenieTex : game.playerLeczenieTex;
                float healW = PLAYER_W * 9f / 10f;
                float healH = PLAYER_H * 9f / 10f;
                float healX = player.x + (PLAYER_W - healW) / 2f;
                game.batch.draw(playerTex, healX, player.y, healW, healH);
            } else if (playerCrouching) {
                if (playerFacingLeft) {
                    game.batch.draw(game.playerKucanieTex, player.x + PLAYER_W, player.y, -PLAYER_W, PLAYER_H);
                } else {
                    game.batch.draw(game.playerKucanieTex, player.x, player.y, PLAYER_W, PLAYER_H);
                }
            } else {
                if (playerFacingLeft) {
                    switch (currentWeapon) {
                        case 0: playerTex = bp ? game.biskupKropidloLeftTex : game.playerKropidloLeftTex; break;
                        case 1: playerTex = bp ? game.biskupKadzidloLeftTex : game.playerKadzidloLeftTex; break;
                        case 2: playerTex = bp ? game.biskupKsiegaLeftTex : game.playerBibliaLeftTex; break;
                        default: playerTex = bp ? game.biskupTex : game.playerLeftTex; break;
                    }
                } else {
                    switch (currentWeapon) {
                        case 0: playerTex = bp ? game.biskupKropidloTex : game.playerKropidloTex; break;
                        case 1: playerTex = bp ? game.biskupKadzidloTex : game.playerKadzidloTex; break;
                        case 2: playerTex = bp ? game.biskupKsiegaTex : game.playerBibliaTex; break;
                        default: playerTex = bp ? game.biskupTex : game.playerRightTex; break;
                    }
                }
                game.batch.draw(playerTex, player.x, player.y, PLAYER_W, PLAYER_H);
            }
            game.batch.setColor(1f, 1f, 1f, 1f);
        }

        // Angel (next to player, close to priest)
        if (angelActive) {
            float angelDrawW, angelDrawH, angelX, angelY;
            if (angelEnraged) {
                angelDrawW = ANGEL_W * 3;
                angelDrawH = ANGEL_H * 3;
                angelX = player.x + PLAYER_W - 40;
                angelY = player.y;
            } else {
                angelDrawW = ANGEL_W;
                angelDrawH = ANGEL_H;
                angelX = player.x + PLAYER_W - 30;
                angelY = player.y + PLAYER_H - ANGEL_H + 10;
            }
            game.batch.draw(game.aniolekTex, angelX, angelY, angelDrawW, angelDrawH);
            if (angelNamed) {
                game.font.getData().setScale(0.5f);
                game.font.setColor(Color.WHITE);
                game.font.draw(game.batch, angelName, angelX + angelDrawW / 2f - angelName.length() * 4, angelY + angelDrawH + 15);
                game.font.getData().setScale(1f);
            }
        }

        // Poison overlay when slowed
        if (playerSlowTimer > 0) {
            game.batch.setColor(1f, 1f, 1f, 0.7f);
            game.batch.draw(game.zatrucieTex, player.x - PLAYER_W * 0.15f, player.y,
                    PLAYER_W * 1.3f, PLAYER_H * 1.2f);
            game.batch.setColor(1f, 1f, 1f, 1f);
        }

        // Enemy (special texture during windup, demon texture on demon levels)
        if (enemyAlive) {
            Texture enemyCurrentTex;
            if (telefonLevel) {
                enemyCurrentTex = telefonSpecialTimer <= 1f && telefonSpecialTimer > 0 ? game.demonTelefonAtakTex : game.demonTelefonTex;
            } else if (demonLevel) {
                enemyCurrentTex = game.demonTex;
            } else if (level == 2) {
                enemyCurrentTex = enemyFacingLeft ? game.zlyOgrodnikLewoTex : game.zlyOgrodnikTex;
            } else {
                enemyCurrentTex = specialWindupTimer > 0 ? game.enemySpecialTex : game.enemyTex;
            }
            int drawH = (level == 2) ? OGRODNIK_H : ENEMY_H;
            game.batch.draw(enemyCurrentTex, enemy.x, enemy.y, ENEMY_W, drawH);

            // Dove above enemy during enlighten
            if (enlightenActiveTimer > 0) {
                float golabW = 80;
                float golabH = 80;
                float golabX = enemy.x + ENEMY_W / 2f - golabW / 2f;
                float golabY = enemy.y + ENEMY_H + 20;
                game.batch.draw(game.golabTex, golabX, golabY, golabW, golabH);
            }
        }

        // Siostry Księgarni (level 3) – każda siostra to połowa wspólnej tekstury
        if (sister1Alive) {
            Texture mainTex = game.siostryKsiegarniTex;
            int halfW = mainTex.getWidth() / 2;
            game.batch.draw(mainTex,
                    sister1.x, sister1.y, SISTER_W, SISTER_H,
                    0, 0, halfW, mainTex.getHeight(),
                    sister1FacingLeft, false);
        }
        if (sister2Alive) {
            Texture mainTex = game.siostryKsiegarniTex;
            int halfW = mainTex.getWidth() / 2;
            game.batch.draw(mainTex,
                    sister2.x, sister2.y, SISTER_W, SISTER_H,
                    halfW, 0, halfW, mainTex.getHeight(),
                    sister2FacingLeft, false);
        }
        if (sisterBookActive) {
            game.batch.setColor(1f, 0.85f, 0.3f, 1f);
            game.batch.draw(game.ksiegaModlitwTex,
                    WORLD_W / 2f - SISTER_BOOK_W / 2f, GROUND_Y,
                    SISTER_BOOK_W, SISTER_BOOK_H);
            game.batch.setColor(1f, 1f, 1f, 1f);
        }

        // Iwonka death animation
        if (iwonkaDying) {
            Texture dyingTex = iwonkaDyingTimer < 0.8f ? game.iwonkaUmieranie1Tex : game.iwonkaUmieranie2Tex;
            game.batch.draw(dyingTex, enemy.x, enemy.y, ENEMY_W, ENEMY_H);
        }

        // ZlyOgrodnik death animation
        if (ogrodnikDying) {
            Texture dyingTex = ogrodnikDyingTimer < 0.8f ? game.zlyOgrodnikSmierc1Tex : game.zlyOgrodnikSmierc2Tex;
            game.batch.draw(dyingTex, enemy.x, enemy.y, ENEMY_W, OGRODNIK_H);
        }

        // Mini enemies
        for (MiniEnemy m : miniEnemies) {
            game.batch.draw(game.miniEnemyTex, m.rect.x, m.rect.y, MINI_W, MINI_H);
        }

        // Bullets
        for (Bullet b : playerBullets) {
            if (b.weaponIndex >= 0) {
                game.batch.draw(game.weaponBulletTex[b.weaponIndex], b.pos.x, b.pos.y, b.size, b.size);
            } else {
                game.batch.draw(game.reflectedBulletTex, b.pos.x, b.pos.y, b.size, b.size);
            }
        }
        for (Bullet b : enemyBullets) {
            if (b.weaponIndex == -8) {
                // Song word bullets: draw as flowing green text
                game.font.getData().setScale(0.75f);
                float pulse = 0.7f + 0.3f * (float) Math.sin(b.age * 6f + b.phase);
                game.font.setColor(0.2f, 1f, 0.35f, pulse);
                game.font.draw(game.batch, b.word, b.pos.x, b.pos.y + 30f);
                game.font.getData().setScale(1f);
                continue;
            }
            Texture bulletTex;
            if (b.weaponIndex == -6) {
                bulletTex = game.siostryKsiegarniAtak1Tex;
            } else if (b.weaponIndex == -7) {
                bulletTex = game.siostryKsiegarniAtak2Tex;
            } else if (b.weaponIndex == -5) {
                bulletTex = game.telefonBulletTex;
            } else if (b.weaponIndex == -4) {
                bulletTex = game.udkoTex;

            } else {
                bulletTex = telefonLevel ? game.telefonBulletTex : (demonLevel ? game.kebabTex : game.enemyBulletTex);
            }
            if (b.spinSpeed > 0) {
                float half = b.size / 2f;
                game.batch.draw(bulletTex,
                        b.pos.x, b.pos.y, half, half, b.size, b.size,
                        1f, 1f, b.rotation,
                        0, 0, bulletTex.getWidth(), bulletTex.getHeight(), false, false);
            } else {
                game.batch.draw(bulletTex, b.pos.x, b.pos.y, b.size, b.size);
            }
        }

        // Portal
        if (portalActive) {
            game.batch.draw(game.portalTex, PORTAL_X, GROUND_Y, PORTAL_W, PORTAL_H);
        }

        // ZlyOgrodnik ground-slam attack sprite (appears after 1s of shaking)
        if (ogrodnikAtkActive && ogrodnikAtkTimer <= 1f) {
            game.batch.draw(game.zlyOgrodnikAtakTex, ogrodnikAtkX, ogrodnikAtkY, ENEMY_W, OGRODNIK_H);
        }

        // Drop from enemy
        if (dropActive) {
            game.batch.draw(game.dropTex, dropX, dropY, DROP_W, DROP_H);
        }

        // Ladder items at top (level 3)
        if (sisterLevel) {
            float ladderTop = LADDER_BASE_Y + LADDER_H;
            if (ladderKasaActive) {
                game.batch.draw(game.kasaTex, LADDER_X - LADDER_ITEM_SIZE - 10f, ladderTop, LADDER_ITEM_SIZE, LADDER_ITEM_SIZE);
            }
            if (ladderKsiegaActive) {
                game.batch.setColor(1f, 0.85f, 0.3f, 1f);
                game.batch.draw(game.ksiegaModlitwTex, LADDER_X + LADDER_W + 10f, ladderTop, LADDER_ITEM_SIZE, LADDER_ITEM_SIZE);
                game.batch.setColor(1f, 1f, 1f, 1f);
            }
            // Hint (only near ladder)
            if (!playerOnLadder) {
                float distToLadder = Math.abs(player.x + PLAYER_W / 2f - (LADDER_X + LADDER_W / 2f));
                if (distToLadder < 180f) {
                    game.font.getData().setScale(0.65f);
                    game.font.setColor(Color.WHITE);
                    game.font.draw(game.batch, "Przytrzymaj W", LADDER_X - 25f, LADDER_BASE_Y + LADDER_H + 40f);
                    game.font.getData().setScale(1f);
                }
            }
        }

        // HUD (relative to camera)
        float camLeft = camera.position.x - W / 2f;
        float camTop = camera.position.y + H / 2f;
        game.font.getData().setScale(1f);
        game.font.setColor(Color.BLACK);
        game.font.draw(game.batch, "Poziom: " + level, camLeft + 10, camTop - 10);
        game.font.draw(game.batch, "Punkty: " + score, camLeft + 10, camTop - 38);

        // Weapon selector with ammo (vertical, black text)
        game.font.getData().setScale(0.8f);
        float wpnX = camLeft + W - 250;
        float wpnY = camTop - 10;
        for (int i = 0; i < 3; i++) {
            String prefix = (i == currentWeapon) ? "> " : "  ";
            game.font.setColor(Color.BLACK);
            String ammoText;
            if (reloadTimer[i] > 0) {
                ammoText = "[" + String.format("%.1f", reloadTimer[i]) + "s]";
            } else {
                ammoText = ammo[i] + "/" + WEAPON_MAX_AMMO[i];
            }
            game.font.draw(game.batch, prefix + WEAPON_NAMES[i] + " " + ammoText,
                    wpnX, wpnY - i * 24);
        }
        // Money popup (kasa animation like angel)
        if (moneyPopupTimer > 0) {
            float popScale = Math.min(1f, (2f - moneyPopupTimer) / 0.5f);
            float popSize = 80 * popScale;
            float popX = camera.position.x - popSize / 2f;
            float popY = camera.position.y + 50;
            game.batch.draw(game.kasaTex, popX, popY, popSize, popSize);
            game.font.getData().setScale(1.2f);
            game.font.setColor(Color.GREEN);
            game.font.draw(game.batch, "+" + moneyPopupAmount, popX + popSize + 10, popY + popSize / 2f + 10);
        }

        // Sister book hint
        if (sisterBookActive) {
            game.font.getData().setScale(0.8f);
            game.font.setColor(Color.YELLOW);
            game.font.draw(game.batch, "Podejdz do ksiegi!", camLeft + W / 2f - 120, camTop - 50);
        }

        // Portal hint
        if (portalActive) {
            game.font.getData().setScale(1f);
            game.font.setColor(Color.BLACK);
            game.font.draw(game.batch, "Idz do portalu!", camLeft + W / 2f - 100, camTop - 50);
        }

        // Relic drop on ground
        if (relikwiaDropActive && game.relikwiaTex != null && relikwiaDropIndex < game.relikwiaTex.length) {
            float pulse = 1f + 0.08f * (float) Math.sin(portalTimer * 4f);
            float rSize = RELIC_SIZE * pulse;
            game.batch.draw(game.relikwiaTex[relikwiaDropIndex],
                    relikwiaDropX - (rSize - RELIC_SIZE) / 2f, relikwiaDropY, rSize, rSize);
        }

        // Prayer book drop (draw as drop texture with golden tint)
        if (prayerBookDropActive) {
            game.batch.setColor(1f, 0.85f, 0.3f, 1f);
            game.batch.draw(game.dropTex, prayerBookDropX, prayerBookDropY, DROP_W, DROP_H);
            game.batch.setColor(1f, 1f, 1f, 1f);
        }

        // Prayer word projectiles flying toward enemy
        for (PrayerWord pw : prayerWords) {
            game.font.getData().setScale(0.9f);
            game.font.setColor(Color.YELLOW);
            game.font.draw(game.batch, pw.text, pw.x, pw.y + 15);
        }
        game.font.getData().setScale(1f);

        game.font.setColor(Color.BLACK);
        game.font.getData().setScale(1f);

        // Lecące plusiki podczas leczenia
        for (HealPlus hp : healPlusses) {
            game.font.getData().setScale(0.9f);
            game.font.setColor(0f, 0.85f, 0.2f, Math.max(0f, hp.alpha));
            game.font.draw(game.batch, "+", hp.x, hp.y);
        }
        game.font.getData().setScale(1f);
        game.font.setColor(Color.WHITE);

        game.batch.end();

        // Drabina (drawn with ShapeRenderer)
        if (sisterLevel) drawLadder();
        // Hypnosis beam (Telefon boss)
        if (telefonLevel) drawHypnoBeam();

        // HP bars (drawn with ShapeRenderer)
        shapeRenderer.setProjectionMatrix(camera.combined);
        drawHpBars(delta);

        // Map overlay
        if (mapOpen) {
            drawMap();
        }

        // Pause / Game Over overlay
        if (paused) {
            drawPauseMenu();
        } else if (gameOver) {
            drawGameOver();
        }

        // Ability menu overlay
        if (abilityMenuOpen) {
            drawAbilityMenu();
        }

        // Prayer book overlay
        if (prayerBookOpen) {
            drawPrayerBook();
        }

        // Relic cutscene overlay
        if (relikwiaCutsceneActive) {
            drawRelikwiaCutscene();
        }

        // Angel cutscene overlay
        if (angelCutsceneActive) {
            drawAngelCutscene();
        }

        // Sister intro overlay (on top of everything)
        if (sisterLevel && (sisterIntroCutsceneActive || sisterIntroFadeAlpha > 0f)) {
            drawSisterIntroOverlay();
        }

        // Portal fade overlay (on top of everything)
        if (fadingToBlack || fadingFromBlack) {
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            float cx = camera.position.x, cy = camera.position.y;
            shapeRenderer.setColor(0f, 0f, 0f, fadeAlpha);
            shapeRenderer.rect(cx - W / 2f, cy - H / 2f, W, H);
            shapeRenderer.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
        }
    }

    private void handleFade(float delta) {
        if (fadingToBlack) {
            fadeAlpha = Math.min(1f, fadeAlpha + FADE_SPEED * delta);
            if (fadeAlpha >= 1f) {
                fadingToBlack = false;
                portalShopOpen = true;
            }
        } else if (fadingFromBlack) {
            fadeAlpha = Math.max(0f, fadeAlpha - FADE_SPEED * delta);
            if (fadeAlpha <= 0f) {
                fadingFromBlack = false;
            }
        }
    }

    private void advanceFromPortalShop() {
        portalShopOpen = false;
        playerHp = Math.min(effectiveMaxHp(), playerHp + 20);
        moneyMultiplier++;
        level++;
        setupLevel();
        game.saveData();
        fadingFromBlack = true;
        fadeAlpha = 1f;
    }

    private void drawAbilityMenu() {
        float px = player.x - 310;
        float py = player.y + PLAYER_H - 10;

        // Background panel
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.75f);
        shapeRenderer.rect(px - 10, py - 190, 300, 210);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(px - 10, py - 190, 300, 210);
        shapeRenderer.end();

        game.batch.begin();
        game.font.getData().setScale(0.7f);
        game.font.setColor(Color.BLACK);
        game.font.draw(game.batch, "UMIEJETNOSCI:", px, py);

        // Reflect
        String reflectInfo = reflectCooldownTimer > 0 ? " [" + (int)Math.ceil(reflectCooldownTimer) + "s]" : "";
        game.font.setColor(reflectCooldownTimer <= 0 && czystosc >= REFLECT_CZYSTOSC_COST ? Color.CYAN : Color.GRAY);
        game.font.draw(game.batch, "1 - Odbicie " + REFLECT_CZYSTOSC_COST + reflectInfo, px, py - 28);

        // Heal
        String healInfo = healCooldownTimer > 0 ? " [" + (int)Math.ceil(healCooldownTimer) + "s]" : "";
        game.font.setColor(healCooldownTimer <= 0 && czystosc >= HEAL_CZYSTOSC_COST ? Color.GREEN : Color.GRAY);
        game.font.draw(game.batch, "2 - Leczenie(+15%HP) " + HEAL_CZYSTOSC_COST + healInfo, px, py - 56);

        // Enlighten
        String enlightenInfo = enlightenCooldownTimer > 0 ? " [" + (int)Math.ceil(enlightenCooldownTimer) + "s]" : "";
        game.font.setColor(enlightenCooldownTimer <= 0 && czystosc >= ENLIGHTEN_CZYSTOSC_COST ? Color.YELLOW : Color.GRAY);
        game.font.draw(game.batch, "3 - Oswiecenie " + ENLIGHTEN_CZYSTOSC_COST + enlightenInfo, px, py - 84);

        // Prayer
        game.font.setColor(hasPrayerBook ? Color.ORANGE : Color.GRAY);
        String prayerLabel = hasPrayerBook ? "4 - Modlitwy [GOTOWE]" : "4 - Modlitwy [BRAK]";
        game.font.draw(game.batch, prayerLabel, px, py - 112);

        // Dash
        String dashInfo = dashCooldownTimer > 0 ? " [" + (int)Math.ceil(dashCooldownTimer) + "s]" : "";
        game.font.setColor(dashCooldownTimer <= 0 ? Color.PURPLE : Color.GRAY);
        game.font.draw(game.batch, "SHIFT - Dash" + dashInfo, px, py - 140);

        game.font.getData().setScale(0.5f);
        game.font.setColor(Color.BLACK);
        game.font.draw(game.batch, "E - zamknij", px, py - 170);
        game.font.getData().setScale(1f);
        game.batch.end();
    }

    private void drawRelikwiaCutscene() {
        float cx = camera.position.x, cy = camera.position.y;

        // Dim background
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.75f);
        shapeRenderer.rect(cx - W / 2f, cy - H / 2f, W, H);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        // Relic image growing from tiny to full
        if (game.relikwiaTex == null || relikwiaDropIndex >= game.relikwiaTex.length) return;
        float grow = Math.min(1f, relikwiaCutsceneTimer / 1.2f);
        float scale = 0.04f + 0.96f * grow * grow;
        float size = 220f * scale;

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.batch.draw(game.relikwiaTex[relikwiaDropIndex], cx - size / 2f, cy - 20f, size, size);

        // Title
        game.font.getData().setScale(1.4f);
        game.font.setColor(SHIELD_COLOR);
        game.font.draw(game.batch, "RELIKWIA " + (relikwiaDropIndex + 1), cx - 120f, cy + size / 2f + 65f);

        // Shield info
        game.font.getData().setScale(0.85f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Tarcza +" + SHIELD_PER_RELIC, cx - 70f, cy - size / 2f + 10f);
        game.font.getData().setScale(0.7f);
        game.font.setColor(SHIELD_COLOR);
        game.font.draw(game.batch, "Tarcza: " + shield + " / " + MAX_SHIELD, cx - 80f, cy - size / 2f - 20f);

        // Prompt
        game.font.getData().setScale(0.6f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "ENTER - kontynuuj", cx - 95f, cy - size / 2f - 60f);

        game.font.getData().setScale(1f);
        game.font.setColor(Color.WHITE);
        game.batch.end();
    }

    private void drawAngelCutscene() {
        float cx = camera.position.x, cy = camera.position.y;

        // Dim background
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(cx - W / 2f, cy - H / 2f, W, H);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        // Draw angel growing from small to full size
        game.batch.begin();
        float growProgress = Math.min(1f, angelCutsceneTimer / 1.5f);
        float scale = 0.04f + 0.96f * growProgress * growProgress; // starts at ~10px, ends at 250px
        float angelDrawW = 250 * scale;
        float angelDrawH = 250 * scale;
        game.batch.draw(game.aniolekTex, cx - angelDrawW / 2f, cy - 20 + (250 - angelDrawH) / 2f, angelDrawW, angelDrawH);

        // Title text
        game.font.getData().setScale(1.5f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Aniolek", cx - 80, cy + angelDrawH / 2f + 60);

        if (!angelNaming) {
            // Subtitle
            game.font.getData().setScale(0.8f);
            game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, "Aniolek dolaczyl do walki!", cx - 150, cy - angelDrawH / 2f + 10);

            // Prompt
            game.font.getData().setScale(0.6f);
            game.font.setColor(Color.GRAY);
            game.font.draw(game.batch, "ENTER - kontynuuj", cx - 90, cy - angelDrawH / 2f - 30);
        } else {
            // Name input
            game.font.getData().setScale(0.8f);
            game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, "Nadaj imie swojemu aniolkowi:", cx - 180, cy - angelDrawH / 2f + 20);

            game.font.getData().setScale(1.2f);
            game.font.setColor(Color.YELLOW);
            String displayName = angelName + ((int)(angelCutsceneTimer * 3) % 2 == 0 ? "_" : "");
            game.font.draw(game.batch, displayName, cx - 80, cy - angelDrawH / 2f - 20);

            game.font.getData().setScale(0.5f);
            game.font.setColor(Color.GRAY);
            game.font.draw(game.batch, "ENTER - zatwierdz", cx - 80, cy - angelDrawH / 2f - 55);
        }
        game.font.getData().setScale(1f);
        game.batch.end();
    }

    private void handleAngelNameInput() {
        for (int key = Input.Keys.A; key <= Input.Keys.Z; key++) {
            if (Gdx.input.isKeyJustPressed(key)) {
                if (angelName.length() < 15) {
                    angelName += Input.Keys.toString(key);
                }
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && angelName.length() < 15) {
            angelName += " ";
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && angelName.length() > 0) {
            angelName = angelName.substring(0, angelName.length() - 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && angelName.length() > 0) {
            angelNaming = false;
            angelNamed = true;
            angelCutsceneActive = false;
        }
    }

    private void drawPrayerBook() {
        // Position: same as ability menu (next to player)
        float px = player.x - 310;
        float py = player.y + PLAYER_H - 10;
        float bookW = 300;
        float bookH = hasZdrowasBook ? 240 : 200;

        // Book background texture
        game.batch.begin();
        game.batch.draw(game.ksiegaModlitwTex, px - 10, py - bookH, bookW, bookH + 10);

        if (!hasPrayerBook && !hasZdrowasBook) {
            game.font.getData().setScale(0.7f);
            game.font.setColor(Color.GRAY);
            game.font.draw(game.batch, "Brak modlitw", px + 20, py - 50);
        } else {
            float lineY = py - 40;
            // Ojcze Nasz entry
            if (hasPrayerBook && !prayerAttackActive) {
                game.font.getData().setScale(0.8f);
                game.font.setColor(Color.YELLOW);
                game.font.draw(game.batch, ">> Ojcze Nasz <<", px + 10, lineY);
            } else if (prayerAttackActive) {
                game.font.getData().setScale(0.6f);
                game.font.setColor(Color.YELLOW);
                game.font.draw(game.batch, "Ojcze Nasz - w toku...", px + 10, lineY);
            } else {
                game.font.getData().setScale(0.6f);
                game.font.setColor(Color.GRAY);
                game.font.draw(game.batch, "Ojcze Nasz - brak", px + 10, lineY);
            }

            // Zdrowas Mario entry
            if (hasZdrowasBook) {
                float lineY2 = lineY - 30;
                if (!angelEnraged) {
                    game.font.getData().setScale(0.8f);
                    game.font.setColor(Color.CYAN);
                    game.font.draw(game.batch, ">> Zdrowas Mario <<", px + 10, lineY2);
                } else {
                    game.font.getData().setScale(0.6f);
                    game.font.setColor(Color.CYAN);
                    game.font.draw(game.batch, "Zdrowas Mario - aktywne", px + 10, lineY2);
                }
            }

            game.font.getData().setScale(0.5f);
            game.font.setColor(0.4f, 0.25f, 0.1f, 1f);
            game.font.draw(game.batch, "Kliknij modlitwe (" + PRAYER_CZYSTOSC_COST + " czystosci)", px + 5, py - bookH + 30);
        }

        game.font.getData().setScale(0.5f);
        game.font.setColor(0.4f, 0.25f, 0.1f, 1f);
        game.font.draw(game.batch, "4 - zamknij", px + 80, py - bookH + 15);
        game.font.getData().setScale(1f);
        game.batch.end();
    }

    private void drawMap() {
        float cx = camera.position.x, cy = camera.position.y;

        // Dim background
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.5f);
        shapeRenderer.rect(cx - W / 2f, cy - H / 2f, W, H);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        // Map image (fullscreen)
        float mapW = W;
        float mapH = H;
        float mapX = cx - W / 2f;
        float mapY = cy - H / 2f;

        game.batch.begin();
        game.batch.draw(game.mapTex, mapX, mapY, mapW, mapH);
        game.batch.end();

        // Red dots for entities (scaled from world coords to map coords)
        float scaleX = mapW / WORLD_W;
        float scaleY = mapH / H;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Player dot (green)
        shapeRenderer.setColor(Color.GREEN);
        float px = mapX + (player.x + PLAYER_W / 2f) * scaleX;
        float py = mapY + (player.y + PLAYER_H / 2f) * scaleY;
        shapeRenderer.circle(px, py, 6);

        // Enemy dot (red)
        shapeRenderer.setColor(Color.RED);
        float ex = mapX + (enemy.x + ENEMY_W / 2f) * scaleX;
        float ey = mapY + (enemy.y + ENEMY_H / 2f) * scaleY;
        shapeRenderer.circle(ex, ey, 6);

        // Mini enemies dots (red, smaller)
        for (MiniEnemy m : miniEnemies) {
            float mx = mapX + (m.rect.x + MINI_W / 2f) * scaleX;
            float my = mapY + (m.rect.y + MINI_H / 2f) * scaleY;
            shapeRenderer.circle(mx, my, 4);
        }

        shapeRenderer.end();
    }

    private void update(float delta) {
        // Aktualizacja hitboxa gracza (mniejszy niż sprite, pasuje do sylwetki)
        playerHitbox.set(player.x + PLAYER_HIT_INSET_X, player.y + PLAYER_HIT_INSET_Y,
                PLAYER_HIT_W, PLAYER_HIT_H);

        // Mana regeneration (5/s after 3s delay from last mana use)
        if (czystoscRegenDelayTimer > 0) {
            czystoscRegenDelayTimer -= delta;
        } else if (czystosc < effectiveMaxCzystosc()) {
            czystoscRegenAccumulator += CZYSTOSC_REGEN * delta;
            if (czystoscRegenAccumulator >= 1f) {
                int gain = (int) czystoscRegenAccumulator;
                czystosc = Math.min(effectiveMaxCzystosc(), czystosc + gain);
                czystoscRegenAccumulator -= gain;
            }
        }

        // Ability timers
        if (reflectCooldownTimer > 0) reflectCooldownTimer -= delta;
        if (reflectActiveTimer > 0) reflectActiveTimer -= delta;
        if (healCooldownTimer > 0) healCooldownTimer -= delta;
        if (healActiveTimer > 0) healActiveTimer -= delta;
        if (dashCooldownTimer > 0) dashCooldownTimer -= delta;
        if (playerSlowTimer > 0) playerSlowTimer -= delta;
        if (sprintCooldownTimer > 0) sprintCooldownTimer -= delta;
        lastPressATimer += delta;
        lastPressDTimer += delta;
        if (sprintActive) {
            sprintTimer -= delta;
            if (sprintTimer <= 0) sprintActive = false;
        }
        if (enlightenCooldownTimer > 0) enlightenCooldownTimer -= delta;
        if (enlightenActiveTimer > 0) enlightenActiveTimer -= delta;

        // Heal plus signs: spawn while healing, always update
        if (healActiveTimer > 0) {
            healPlusSpawnTimer -= delta;
            if (healPlusSpawnTimer <= 0) {
                healPlusSpawnTimer = 0.12f;
                float spawnX = player.x + PLAYER_HIT_INSET_X + MathUtils.random(PLAYER_HIT_W - 10f);
                healPlusses.add(new HealPlus(spawnX, player.y));
            }
        }
        for (int i = healPlusses.size - 1; i >= 0; i--) {
            HealPlus hp = healPlusses.get(i);
            hp.y += hp.vy * delta;
            hp.alpha -= delta * 0.7f;
            if (hp.alpha <= 0) healPlusses.removeIndex(i);
        }

        // Angel enraged timer & attack
        if (angelEnraged) {
            angelEnragedTimer -= delta;
            if (angelEnragedTimer <= 0 || angelHp <= 0) {
                angelEnraged = false;
                zdrowasAttackActive = false;
                prayerWords.clear();
            } else if (enemyAlive) {
                angelAttackTimer -= delta;
                if (angelAttackTimer <= 0) {
                    angelAttackTimer = ANGEL_ATTACK_INTERVAL;
                    // Angel shoots a bullet at enemy
                    float ax = player.x + PLAYER_W - 30 + ANGEL_W / 2f;
                    float ay = player.y + PLAYER_H / 2f;
                    float adx = (enemy.x + ENEMY_W / 2f) - ax;
                    float ady = (enemy.y + ENEMY_H / 2f) - ay;
                    float alen = (float) Math.sqrt(adx * adx + ady * ady);
                    if (alen > 0) { adx /= alen; ady /= alen; }
                    playerBullets.add(new Bullet(ax, ay, adx * 400f, ady * 400f,
                            ANGEL_ENRAGED_DAMAGE, 20, -5));
                }
            }
        }

        // Zdrowas Mario attack (similar to Ojcze Nasz but for angel)
        if (zdrowasAttackActive && enemyAlive) {
            zdrowasWordTimer -= delta;
            if (zdrowasWordTimer <= 0 && zdrowasWordIndex < ZDROWAS_WORDS.length) {
                zdrowasWordTimer = PRAYER_WORD_INTERVAL;
                float startX = player.x + PLAYER_W - 30 + ANGEL_W / 2f;
                float startY = player.y + PLAYER_H / 2f;
                float dx = (enemy.x + ENEMY_W / 2f) - startX;
                float dy = (enemy.y + ENEMY_H / 2f) - startY;
                float len = (float) Math.sqrt(dx * dx + dy * dy);
                if (len > 0) { dx /= len; dy /= len; }
                prayerWords.add(new PrayerWord(startX, startY,
                        dx * PRAYER_WORD_SPEED, dy * PRAYER_WORD_SPEED,
                        ZDROWAS_WORDS[zdrowasWordIndex]));
                zdrowasWordIndex++;
            }
        }

        // Prayer attack update
        if (prayerAttackActive && enemyAlive) {
            prayerWordTimer -= delta;
            if (prayerWordTimer <= 0 && prayerWordIndex < PRAYER_WORDS.length) {
                prayerWordTimer = PRAYER_WORD_INTERVAL;
                float startX = player.x + PLAYER_W / 2f;
                float startY = player.y + PLAYER_H / 2f;
                float dx = (enemy.x + ENEMY_W / 2f) - startX;
                float dy = (enemy.y + ENEMY_H / 2f) - startY;
                float len = (float) Math.sqrt(dx * dx + dy * dy);
                if (len > 0) { dx /= len; dy /= len; }
                prayerWords.add(new PrayerWord(startX, startY,
                        dx * PRAYER_WORD_SPEED, dy * PRAYER_WORD_SPEED,
                        PRAYER_WORDS[prayerWordIndex]));
                prayerWordIndex++;
            }
            if (prayerWordIndex >= PRAYER_WORDS.length && prayerWords.size == 0) {
                prayerAttackActive = false;
                prayerBookOpen = false;
                hasPrayerBook = false;
            }
        }
        // Update prayer word projectiles
        for (int i = prayerWords.size - 1; i >= 0; i--) {
            PrayerWord pw = prayerWords.get(i);
            pw.x += pw.vx * delta;
            pw.y += pw.vy * delta;
            if (enemyAlive && pw.x + 30 > enemy.x && pw.x < enemy.x + ENEMY_W
                    && pw.y + 20 > enemy.y && pw.y < enemy.y + ENEMY_H) {
                prayerWords.removeIndex(i);
                enemyHp -= PRAYER_WORD_DAMAGE;
                score += 10 * PRAYER_WORD_DAMAGE;
                if (enemyHp <= 0) {
                    prayerAttackActive = false;
                    prayerBookOpen = false;
                    handleEnemyKill();
                    return;
                }
                continue;
            }
            if (pw.x < -50 || pw.x > WORLD_W + 50 || pw.y < -50 || pw.y > H + 50) {
                prayerWords.removeIndex(i);
            }
        }

        // Activate reflect (1 key) — requires mana
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && reflectCooldownTimer <= 0 && reflectActiveTimer <= 0
                && czystosc >= REFLECT_CZYSTOSC_COST) {
            reflectActiveTimer = REFLECT_DURATION;
            reflectCooldownTimer = REFLECT_COOLDOWN;
            consumeCzystosc(REFLECT_CZYSTOSC_COST);
        }

        // Activate heal (2 key) — requires mana, 2s channel: no movement, invincible
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && healCooldownTimer <= 0 && healActiveTimer <= 0
                && czystosc >= HEAL_CZYSTOSC_COST && playerHp < effectiveMaxHp()) {
            healActiveTimer = HEAL_DURATION;
            int healAmount = Math.max(1, (int)(effectiveMaxHp() * HEAL_PERCENT));
            playerHp = Math.min(effectiveMaxHp(), playerHp + healAmount);
            healCooldownTimer = HEAL_COOLDOWN;
            consumeCzystosc(HEAL_CZYSTOSC_COST);
        }

        // Activate enlighten (3 key) — freezes enemy, deals damage
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && enlightenCooldownTimer <= 0
                && enlightenActiveTimer <= 0 && czystosc >= ENLIGHTEN_CZYSTOSC_COST && enemyAlive) {
            enlightenActiveTimer = ENLIGHTEN_DURATION;
            enlightenCooldownTimer = ENLIGHTEN_COOLDOWN;
            consumeCzystosc(ENLIGHTEN_CZYSTOSC_COST);
            enemyHp -= ENLIGHTEN_DAMAGE;
            if (enemyHp <= 0) {
                handleEnemyKill();
                return;
            }
        }

        // Toggle prayer book (4 key)
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            if (prayerAttackActive) {
                // Cancel prayer attack
                prayerAttackActive = false;
                prayerBookOpen = false;
                prayerWords.clear();
            } else {
                prayerBookOpen = !prayerBookOpen;
            }
        }

        // Click prayers in book (positioned near player, same as ability menu)
        if (prayerBookOpen && Gdx.input.justTouched() && czystosc >= PRAYER_CZYSTOSC_COST) {
            mouseTemp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(mouseTemp);
            float bpx = player.x - 310;
            float bpy = player.y + PLAYER_H - 10;
            float lineY1 = bpy - 40;
            float lineY2 = lineY1 - 30;

            // Click "Ojcze Nasz"
            if (hasPrayerBook && !prayerAttackActive && enemyAlive
                    && mouseTemp.x >= bpx && mouseTemp.x <= bpx + 280
                    && mouseTemp.y >= lineY1 - 20 && mouseTemp.y <= lineY1 + 5) {
                prayerAttackActive = true;
                prayerWordTimer = 0;
                prayerWordIndex = 0;
                prayerWords.clear();
                consumeCzystosc(PRAYER_CZYSTOSC_COST);
                mapOpen = false;
            }

            // Click "Zdrowas Mario"
            if (hasZdrowasBook && angelActive && !angelEnraged && enemyAlive
                    && mouseTemp.x >= bpx && mouseTemp.x <= bpx + 280
                    && mouseTemp.y >= lineY2 - 20 && mouseTemp.y <= lineY2 + 5) {
                angelEnraged = true;
                angelEnragedTimer = ANGEL_ENRAGED_DURATION;
                angelHp = ANGEL_ENRAGED_MAX_HP;
                angelAttackTimer = 0;
                zdrowasAttackActive = true;
                zdrowasWordTimer = 0;
                zdrowasWordIndex = 0;
                consumeCzystosc(ZDROWAS_CZYSTOSC_COST);
                prayerBookOpen = false;
                mapOpen = false;
            }
        }

        // Prayer book drop pickup
        if (prayerBookDropActive) {
            prayerDropRectReuse.set(prayerBookDropX, prayerBookDropY, DROP_W, DROP_H);
            if (player.overlaps(prayerDropRectReuse)) {
                hasPrayerBook = true;
                prayerBookDropActive = false;
            }
        }

        // Dash (SHIFT key)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) && dashCooldownTimer <= 0 && dashTimer <= 0) {
            dashDir = playerFacingLeft ? -1f : 1f;
            dashTimer = DASH_DURATION;
            dashCooldownTimer = DASH_COOLDOWN;
        }
        if (dashTimer > 0) {
            dashTimer -= delta;
            player.x += dashDir * DASH_SPEED * delta;
            player.x = MathUtils.clamp(player.x, 0, WORLD_W - PLAYER_W);
        }

        boolean reflecting = reflectActiveTimer > 0;

        // Double-tap A/D -> sprint
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            if (lastPressATimer < SPRINT_DOUBLE_TAP_WINDOW && !sprintActive && sprintCooldownTimer <= 0 && playerSlowTimer <= 0) {
                sprintActive = true;
                sprintTimer = SPRINT_DURATION;
                sprintCooldownTimer = SPRINT_COOLDOWN;
            }
            lastPressATimer = 0f;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            if (lastPressDTimer < SPRINT_DOUBLE_TAP_WINDOW && !sprintActive && sprintCooldownTimer <= 0 && playerSlowTimer <= 0) {
                sprintActive = true;
                sprintTimer = SPRINT_DURATION;
                sprintCooldownTimer = SPRINT_COOLDOWN;
            }
            lastPressDTimer = 0f;
        }

        // Player horizontal movement (A/D) — blocked during reflect, heal, and prayer attack
        if (playerHypnoTimer > 0) playerHypnoTimer -= delta;
        if (playerAttackSlowTimer > 0) playerAttackSlowTimer -= delta;
        boolean healing = healActiveTimer > 0;
        float sprintMult = (sprintActive && playerSlowTimer <= 0) ? SPRINT_SPEED_MULT : 1f;
        float currentSpeed = playerSlowTimer > 0 ? PLAYER_SPEED * DEMON_SLOW_FACTOR : PLAYER_SPEED * sprintMult;
        float prevX = player.x;
        playerMoving = false;
        if (playerHypnoTimer > 0 && enemyAlive) {
            // Hypnotized: forced march toward enemy
            float hdx = (enemy.x + ENEMY_W / 2f) - (player.x + PLAYER_W / 2f);
            if (hdx > 2f) { player.x += PLAYER_SPEED * delta; playerMoving = true; playerFacingLeft = false; }
            else if (hdx < -2f) { player.x -= PLAYER_SPEED * delta; playerMoving = true; playerFacingLeft = true; }
        } else if (!reflecting && !healing && !prayerAttackActive) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                player.x -= currentSpeed * delta;
                playerMoving = true;
                playerFacingLeft = true;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                player.x += currentSpeed * delta;
                playerMoving = true;
                playerFacingLeft = false;
            }
        }
        // Animation driven by distance walked (1 step = 80px)
        float distMoved = Math.abs(player.x - prevX);
        if (playerMoving && distMoved > 0) {
            walkStateTime += distMoved / 80f;
        } else {
            walkStateTime = 0;
        }
        player.x = MathUtils.clamp(player.x, 0, WORLD_W - PLAYER_W);

        // Ladder enter check — before jump so W enters ladder instead of jumping
        if (sisterLevel) {
            float ladderTop = LADDER_BASE_Y + LADDER_H;
            boolean inLadderX = player.x + PLAYER_W > LADDER_X + 4 && player.x < LADDER_X + LADDER_W - 4;
            boolean inLadderY = player.y + PLAYER_H > LADDER_BASE_Y && player.y < ladderTop;
            if (inLadderX && inLadderY && Gdx.input.isKeyPressed(Input.Keys.W)) playerOnLadder = true;
            if (!inLadderX) playerOnLadder = false;
        }

        // Jump (W key) — double jump allowed, blocked on ladder
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && jumpsLeft > 0 && !reflecting && !healing && !playerOnLadder) {
            playerVelY = JUMP_VELOCITY;
            jumpsLeft--;
        }

        // Drop through platform (S key)
        if (dropThroughTimer > 0) dropThroughTimer -= delta;
        if (Gdx.input.isKeyJustPressed(Input.Keys.S) && player.y > GROUND_Y) {
            dropThroughTimer = 0.2f;
            player.y -= 1;
            playerVelY = -50f;
        }
        // Kucanie (trzymanie S)
        playerCrouching = Gdx.input.isKeyPressed(Input.Keys.S);

        // Gravity / ladder climbing
        if (playerOnLadder) {
            if (Gdx.input.isKeyPressed(Input.Keys.W))      playerVelY = LADDER_CLIMB_SPEED;
            else if (Gdx.input.isKeyPressed(Input.Keys.S)) playerVelY = -LADDER_CLIMB_SPEED;
            else                                            playerVelY = 0f;
            player.y += playerVelY * delta;
            float ladderTop = LADDER_BASE_Y + LADDER_H;
            if (player.y >= ladderTop) {
                player.y = ladderTop;
                playerVelY = 0f;
                playerOnLadder = false;
                jumpsLeft = MAX_JUMPS;
                if (!ladderTopReached) {
                    ladderTopReached = true;
                    ladderKasaActive = true;
                    ladderKsiegaActive = true;
                }
            }
            if (player.y <= LADDER_BASE_Y) {
                player.y = LADDER_BASE_Y;
                playerVelY = 0f;
                playerOnLadder = false;
                jumpsLeft = MAX_JUMPS;
            }
        } else {
            playerVelY -= GRAVITY * delta;
            player.y += playerVelY * delta;
        }

        // Ground collision
        if (player.y <= GROUND_Y) {
            player.y = GROUND_Y;
            playerVelY = 0;
            jumpsLeft = MAX_JUMPS;
        }
        // Platform collision (pass-through: only land when falling, disabled during drop-through and ladder)
        if (playerVelY <= 0 && dropThroughTimer <= 0 && !playerOnLadder) {
            for (float[] p : PLATFORMS) {
                float pLeft = p[0], pTop = p[1] + p[3], pRight = p[0] + p[2];
                if (player.x + PLAYER_W > pLeft && player.x < pRight
                        && player.y <= pTop && player.y >= pTop - 20) {
                    player.y = pTop;
                    playerVelY = 0;
                    jumpsLeft = MAX_JUMPS;
                }
            }
        }
        player.y = MathUtils.clamp(player.y, GROUND_Y, H - PLAYER_H);

        // Ladder item pickups (level 3)
        if (sisterLevel) {
            float ladderTop = LADDER_BASE_Y + LADDER_H;
            float iSize = LADDER_ITEM_SIZE;
            if (ladderKasaActive) {
                float kasaX = LADDER_X - iSize - 10f, kasaY = ladderTop;
                if (player.x + PLAYER_W > kasaX && player.x < kasaX + iSize
                        && player.y + PLAYER_H > kasaY && player.y < kasaY + iSize + PLAYER_H) {
                    game.money += 50;
                    moneyPopupAmount = 50;
                    moneyPopupTimer = 2f;
                    game.saveData();
                    ladderKasaActive = false;
                }
            }
            if (ladderKsiegaActive) {
                float ksiegaX = LADDER_X + LADDER_W + 10f, ksiegaY = ladderTop;
                if (player.x + PLAYER_W > ksiegaX && player.x < ksiegaX + iSize
                        && player.y + PLAYER_H > ksiegaY && player.y < ksiegaY + iSize + PLAYER_H) {
                    hasPrayerBook = true;
                    ladderKsiegaActive = false;
                }
            }
        }

        // Weapon switching (scroll wheel only)
        float scroll = scrollAmount;
        scrollAmount = 0;
        if (scroll < 0) currentWeapon = (currentWeapon + 2) % 3;
        if (scroll > 0) currentWeapon = (currentWeapon + 1) % 3;

        // Ammo reload timers
        for (int i = 0; i < 3; i++) {
            if (reloadTimer[i] > 0) {
                reloadTimer[i] -= delta;
                if (reloadTimer[i] <= 0) {
                    ammo[i] = WEAPON_MAX_AMMO[i];
                }
            }
        }

        // Manual reload (R key)
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)
                && ammo[currentWeapon] < WEAPON_MAX_AMMO[currentWeapon]
                && reloadTimer[currentWeapon] <= 0) {
            reloadTimer[currentWeapon] = WEAPON_RELOAD_TIME[currentWeapon];
        }

        // Player shooting (left mouse button toward cursor, SPACE in facing direction)
        playerShootTimer -= delta;
        if (playerShootingTimer > 0) playerShootingTimer -= delta;
        boolean shootingMouse = playerHypnoTimer <= 0 && Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        boolean shootingSpace = playerHypnoTimer <= 0 && Gdx.input.isKeyPressed(Input.Keys.SPACE);
        float hypnoSlowMult = playerAttackSlowTimer > 0 ? 2f : 1f;

        // Kadzidlo burst: start burst on click/press
        if (currentWeapon == 1 && (shootingMouse || shootingSpace) && playerShootTimer <= 0
                && kadzidloBurstRemaining <= 0 && ammo[1] > 0 && reloadTimer[1] <= 0 && !healing) {
            kadzidloBurstRemaining = Math.min(KADZIDLO_BURST_COUNT, ammo[1]);
            kadzidloBurstTimer = 0;
            playerShootingTimer = 0.3f;
            kadzidloCurrentBurstSize = WEAPON_BULLET_SIZES[1] + MathUtils.random(-14, 18);
            // Save direction for entire burst
            if (shootingSpace) {
                kadzidloDirX = playerFacingLeft ? -1f : 1f;
                kadzidloDirY = 0;
            } else {
                mouseTemp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                Vector3 mouseWorld = camera.unproject(mouseTemp);
                kadzidloDirX = mouseWorld.x - (player.x + PLAYER_W / 2f);
                kadzidloDirY = mouseWorld.y - (player.y + PLAYER_H / 2f);
                float len = (float) Math.sqrt(kadzidloDirX * kadzidloDirX + kadzidloDirY * kadzidloDirY);
                if (len > 0) { kadzidloDirX /= len; kadzidloDirY /= len; }
            }
        }

        // Kadzidlo burst: fire bullets in burst
        if (kadzidloBurstRemaining > 0) {
            kadzidloBurstTimer -= delta;
            if (kadzidloBurstTimer <= 0) {
                kadzidloBurstTimer = KADZIDLO_BURST_INTERVAL;
                int bSize = kadzidloCurrentBurstSize;
                float bx = player.x + PLAYER_W / 2f - bSize / 2f;
                float by = player.y + PLAYER_H / 2f - bSize / 2f;
                float speed = WEAPON_BULLET_SPEEDS[1];
                Bullet pb = new Bullet(bx, by, kadzidloDirX * speed, kadzidloDirY * speed,
                        WEAPON_DAMAGE[1], bSize, 1);
                pb.curveUp = (playerBulletCounter % 2 == 0);
                playerBulletCounter++;
                playerBullets.add(pb);
                ammo[1]--;
                kadzidloBurstRemaining--;
                if (ammo[1] <= 0) {
                    reloadTimer[1] = WEAPON_RELOAD_TIME[1];
                    kadzidloBurstRemaining = 0;
                }
                if (kadzidloBurstRemaining <= 0) {
                    playerShootTimer = KADZIDLO_BURST_COOLDOWN;
                }
            }
        }

        // Normal shooting (non-kadzidlo weapons)
        if (currentWeapon != 1 && (shootingMouse || shootingSpace) && playerShootTimer <= 0
                && ammo[currentWeapon] > 0 && reloadTimer[currentWeapon] <= 0 && !healing) {
            playerShootTimer = WEAPON_COOLDOWNS[currentWeapon] * hypnoSlowMult;
            playerShootingTimer = 0.3f;
            ammo[currentWeapon]--;
            if (ammo[currentWeapon] <= 0) {
                reloadTimer[currentWeapon] = WEAPON_RELOAD_TIME[currentWeapon];
            }
            int bSize = WEAPON_BULLET_SIZES[currentWeapon];
            float bx = player.x + PLAYER_W / 2f - bSize / 2f;
            float by = player.y + PLAYER_H / 2f - bSize / 2f;
            float dx, dy;
            if (shootingSpace) {
                dx = playerFacingLeft ? -1f : 1f;
                dy = 0;
            } else {
                mouseTemp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                Vector3 mouseWorld = camera.unproject(mouseTemp);
                dx = mouseWorld.x - (player.x + PLAYER_W / 2f);
                dy = mouseWorld.y - (player.y + PLAYER_H / 2f);
                float len = (float) Math.sqrt(dx * dx + dy * dy);
                if (len > 0) { dx /= len; dy /= len; }
            }
            float speed = WEAPON_BULLET_SPEEDS[currentWeapon];
            Bullet pb = new Bullet(bx, by, dx * speed, dy * speed,
                    WEAPON_DAMAGE[currentWeapon], bSize, currentWeapon);
            pb.curveUp = (playerBulletCounter % 2 == 0);
            playerBulletCounter++;
            playerBullets.add(pb);
        }

        // Invincibility timer
        if (playerInvincibleTimer > 0) {
            playerInvincibleTimer -= delta;
        }

        // Enemy AI (only when alive, frozen during enlighten and prayer attack)
        if (enemyAlive && enlightenActiveTimer <= 0 && !prayerAttackActive) {
            // Enemy horizontal movement (chase player on ground) — frozen during special windup or ogrodnik slam
            if (specialWindupTimer <= 0 && !ogrodnikAtkActive && !songAttackActive && !hypnoWindupActive && !hypnoBeamActive) {
                float chaseDx = (player.x + PLAYER_W / 2f) - (enemy.x + ENEMY_W / 2f);
                if (chaseDx > 0) {
                    enemy.x += enemySpeed * delta;
                    enemyFacingLeft = false;
                } else if (chaseDx < 0) {
                    enemy.x -= enemySpeed * delta;
                    enemyFacingLeft = true;
                }
                enemy.x = MathUtils.clamp(enemy.x, 0, WORLD_W - ENEMY_W);

                // Enemy jumps when player is above
                enemyJumpTimer -= delta;
                boolean enemyOnGround = enemyVelY == 0;
                if (enemyOnGround && player.y > enemy.y + ENEMY_H * 0.5f && enemyJumpTimer <= 0) {
                    enemyVelY = ENEMY_JUMP_VELOCITY;
                    enemyJumpTimer = ENEMY_JUMP_COOLDOWN;
                }
            }

            // Enemy gravity
            enemyVelY -= GRAVITY * delta;
            enemy.y += enemyVelY * delta;
            if (enemy.y <= GROUND_Y) {
                enemy.y = GROUND_Y;
                enemyVelY = 0;
            }
            // Enemy platform collision
            if (enemyVelY <= 0) {
                for (float[] p : PLATFORMS) {
                    float pLeft = p[0], pTop = p[1] + p[3], pRight = p[0] + p[2];
                    if (enemy.x + ENEMY_W > pLeft && enemy.x < pRight
                            && enemy.y <= pTop && enemy.y >= pTop - 20) {
                        enemy.y = pTop;
                        enemyVelY = 0;
                    }
                }
            }
            enemy.y = MathUtils.clamp(enemy.y, GROUND_Y, H - ENEMY_H);

            // Enemy shooting (burst system) — blocked after demon udko
            if (demonShootBlockTimer > 0) demonShootBlockTimer -= delta;
            enemyShootTimer -= delta;
            if (enemyShootTimer <= 0 && demonShootBlockTimer <= 0) {
                enemyShootTimer = enemyShootCooldown;
                boolean doubleBurst = (demonLevel || telefonLevel) ? (level >= 9) : (level >= 5);
            enemyBurstRemaining = doubleBurst ? 2 : 1;
                enemyBurstTimer = 0;
            }
            if (enemyBurstRemaining > 0) {
                enemyBurstTimer -= delta;
                if (enemyBurstTimer <= 0) {
                    spawnEnemyBullet();
                    enemyBurstRemaining--;
                    enemyBurstTimer = BURST_INTERVAL;
                }
            }

            // Special attack — only on iwonka levels
            if (!demonLevel && !telefonLevel) {
                if (level == 2) {
                    // ZlyOgrodnik: ground-slam at player position
                    specialBulletTimer -= delta;
                    if (specialBulletTimer <= 0 && !ogrodnikAtkActive) {
                        ogrodnikAtkActive = true;
                        ogrodnikAtkImpacted = false;
                        ogrodnikAtkTimer = 2f;
                        specialBulletTimer = SPECIAL_BULLET_COOLDOWN;
                    }
                    if (ogrodnikAtkActive) {
                        ogrodnikAtkTimer -= delta;
                        // Sprite + damage appear after 1s of shaking
                        if (!ogrodnikAtkImpacted && ogrodnikAtkTimer <= 1f) {
                            ogrodnikAtkImpacted = true;
                            ogrodnikAtkX = player.x;
                            ogrodnikAtkY = player.y;
                            if (playerInvincibleTimer <= 0 && healActiveTimer <= 0) {
                                applyPlayerDamage(SPECIAL_BULLET_DAMAGE, 0);
                            }
                        }
                        if (ogrodnikAtkTimer <= 0) {
                            ogrodnikAtkActive = false;
                        }
                    }
                } else {
                    // Normal Iwonka: big rabbit with windup
                    if (!songAttackActive) {
                        specialBulletTimer -= delta;
                        if (specialBulletTimer <= 0 && specialWindupTimer <= 0) {
                            specialWindupTimer = SPECIAL_WINDUP;
                        }
                        if (specialWindupTimer > 0) {
                            specialWindupTimer -= delta;
                            if (specialWindupTimer <= 0) {
                                spawnSpecialEnemyBullet();
                                specialBulletTimer = SPECIAL_BULLET_COOLDOWN;
                            }
                        }
                    }
                    // Song attack (2x rarer than big rabbit)
                    if (!songAttackActive) {
                        songAttackCooldownTimer -= delta;
                        if (songAttackCooldownTimer <= 0) {
                            songAttackActive = true;
                            songWordTimer = 0f;
                            songWordIndex = 0;
                        }
                    } else {
                        songWordTimer -= delta;
                        if (songWordTimer <= 0 && songWordIndex < SONG_WORDS.length) {
                            spawnSongWordBullet();
                            songWordIndex++;
                            songWordTimer = SONG_WORD_INTERVAL;
                        }
                        if (songWordIndex >= SONG_WORDS.length && songWordTimer <= 0) {
                            songAttackActive = false;
                            songAttackCooldownTimer = SONG_ATTACK_COOLDOWN;
                        }
                    }
                }
            }

            // Demon special: udko-z-kurczaka every 5s
            if (demonLevel && !telefonLevel) {
                demonSpecialTimer -= delta;
                if (demonSpecialTimer <= 0) {
                    spawnDemonSpecialBullet();
                    demonSpecialTimer = DEMON_SPECIAL_COOLDOWN;
                    demonShootBlockTimer = DEMON_SHOOT_BLOCK;
                }
            }

            // Telefon special: telefon projectile every 5s + hypnosis beam every 10s
            if (telefonLevel) {
                // Hypnosis beam logic
                if (!hypnoBeamActive && !hypnoWindupActive) {
                    hypnoCooldownTimer -= delta;
                    if (hypnoCooldownTimer <= 0) {
                        hypnoWindupActive = true;
                        hypnoWindupTimer  = HYPNO_WINDUP;
                    }
                }
                if (hypnoWindupActive) {
                    hypnoWindupTimer -= delta;
                    if (hypnoWindupTimer <= 0) {
                        hypnoWindupActive = false;
                        hypnoBeamActive   = true;
                        hypnoBeamTimer    = 0f;
                        for (int i = 0; i < 3; i++) { hypnoWaveActive[i] = false; hypnoWaveHit[i] = false; }
                        spawnHypnoWave(0);
                    }
                }
                if (hypnoBeamActive) {
                    hypnoBeamTimer += delta;
                    // Spawn waves with delay
                    for (int wi = 1; wi < HYPNO_WAVE_COUNT; wi++) {
                        if (!hypnoWaveActive[wi] && !hypnoWaveHit[wi]
                                && hypnoBeamTimer >= wi * HYPNO_WAVE_SPAWN_INTERVAL) {
                            spawnHypnoWave(wi);
                        }
                    }
                    // Advance wave progress + hit check
                    for (int wi = 0; wi < HYPNO_WAVE_COUNT; wi++) {
                        if (!hypnoWaveActive[wi]) continue;
                        hypnoWaveProgress[wi] += delta / HYPNO_WAVE_DURATION;
                        if (hypnoWaveProgress[wi] >= 1f) {
                            hypnoWaveProgress[wi] = 1f;
                            hypnoWaveActive[wi]   = false;
                            if (!hypnoWaveHit[wi]) {
                                hypnoWaveHit[wi] = true;
                                applyPlayerDamage(HYPNO_DAMAGE, -9);
                                playerHypnoTimer      = HYPNO_PLAYER_DURATION;
                                playerAttackSlowTimer = HYPNO_SLOW_DURATION;
                            }
                        }
                    }
                    // End beam
                    float beamEnd = (HYPNO_WAVE_COUNT - 1) * HYPNO_WAVE_SPAWN_INTERVAL + HYPNO_WAVE_DURATION + 0.4f;
                    if (hypnoBeamTimer >= beamEnd) {
                        hypnoBeamActive    = false;
                        hypnoCooldownTimer = HYPNO_COOLDOWN;
                    }
                }
                // Regular telefon special (blocked during hypno)
                if (!hypnoBeamActive && !hypnoWindupActive) {
                    telefonSpecialTimer -= delta;
                    if (telefonSpecialTimer <= 0) {
                        spawnTelefonSpecialBullet();
                        telefonSpecialTimer  = TELEFON_SPECIAL_COOLDOWN;
                        demonShootBlockTimer = DEMON_SHOOT_BLOCK;
                    }
                }
            }
        }

        // Update player bullets
        for (int i = playerBullets.size - 1; i >= 0; i--) {
            Bullet b = playerBullets.get(i);
            b.pos.x += b.vel.x * delta;
            b.pos.y += b.vel.y * delta;

            float dist = b.pos.dst(b.startPos);

            // Curve every other bullet at 400 distance
            if (!b.curved && b.weaponIndex >= 0 && dist >= PLAYER_BULLET_CURVE_DIST) {
                b.curved = true;
                float angle = b.curveUp ? PLAYER_BULLET_CURVE_ANGLE : -PLAYER_BULLET_CURVE_ANGLE;
                float rad = angle * MathUtils.degreesToRadians;
                float cos = MathUtils.cos(rad);
                float sin = MathUtils.sin(rad);
                float nvx = b.vel.x * cos - b.vel.y * sin;
                float nvy = b.vel.x * sin + b.vel.y * cos;
                b.vel.x = nvx;
                b.vel.y = nvy;
            }

            // Range limit + out of bounds
            if (b.pos.y > H || b.pos.y < -b.size || b.pos.x < -b.size || b.pos.x > WORLD_W
                    || (b.weaponIndex >= 0 && dist > PLAYER_BULLET_RANGE)) {
                playerBullets.removeIndex(i);
                continue;
            }

            // Check hit on enemy
            if (enemyAlive && bulletHitsRect(b, enemy, ENEMY_W, ENEMY_H)) {
                playerBullets.removeIndex(i);
                enemyHp -= b.damage;
                score += 10 * b.damage;
                if (enemyHp <= 0) {
                    handleEnemyKill();
                    return;
                }
                continue;
            }
            // Check hit on sisters
            if (sisterLevel) {
                if (sister1Alive && bulletHitsRect(b, sister1, SISTER_W, SISTER_H)) {
                    playerBullets.removeIndex(i);
                    sister1Hp -= b.damage;
                    score += 10 * b.damage;
                    if (sister1Hp <= 0) { sister1Alive = false; handleSisterKill(); return; }
                    continue;
                }
                if (sister2Alive && bulletHitsRect(b, sister2, SISTER_W, SISTER_H)) {
                    playerBullets.removeIndex(i);
                    sister2Hp -= b.damage;
                    score += 10 * b.damage;
                    if (sister2Hp <= 0) { sister2Alive = false; handleSisterKill(); return; }
                    continue;
                }
            }
        }

        // Update enemy bullets
        for (int i = enemyBullets.size - 1; i >= 0; i--) {
            Bullet b = enemyBullets.get(i);
            if (b.weaponIndex == -8) {
                // Song word: horizontal + sine wave Y
                b.age += delta;
                b.pos.x += b.vel.x * delta;
                b.pos.y = b.startPos.y + SONG_WAVE_AMPLITUDE * (float) Math.sin(b.age * SONG_WAVE_FREQUENCY + b.phase);
            } else {
                b.pos.x += b.vel.x * delta;
                b.pos.y += b.vel.y * delta;
            }

            // Bouncing rabbit bullets (Iwonka): gravity + ground/platform bounce
            if (b.bouncing) {
                b.vel.y -= 420f * delta;
                boolean removeBounce = false;
                if (b.pos.y <= GROUND_Y) {
                    b.pos.y = GROUND_Y;
                    b.vel.y = Math.abs(b.vel.y) * 0.72f;
                    if (++b.bounceCount >= 4) removeBounce = true;
                } else if (b.vel.y < 0) {
                    for (float[] p : PLATFORMS) {
                        float pTop = p[1] + p[3];
                        if (b.pos.x + b.size > p[0] && b.pos.x < p[0] + p[2]
                                && b.pos.y <= pTop && b.pos.y >= pTop - 22) {
                            b.pos.y = pTop;
                            b.vel.y = Math.abs(b.vel.y) * 0.72f;
                            if (++b.bounceCount >= 4) removeBounce = true;
                            break;
                        }
                    }
                }
                if (removeBounce) { enemyBullets.removeIndex(i); continue; }
            }

            // Spinning bullets (demon/telefon): update rotation angle
            if (b.spinSpeed > 0) {
                b.rotation = (b.rotation + b.spinSpeed * delta) % 360f;
            }

            float dist = b.pos.dst(b.startPos);
            if (b.pos.y < -b.size || b.pos.x < -b.size || b.pos.x > WORLD_W || b.pos.y > H
                    || dist > ENEMY_BULLET_RANGE) {
                enemyBullets.removeIndex(i);
                continue;
            }

            if (bulletHitsRect(b, playerHitbox, PLAYER_HIT_W, PLAYER_HIT_H)) {
                if (reflecting) {
                    // Reflect bullet toward enemy
                    enemyBullets.removeIndex(i);
                    float rdx = (enemy.x + ENEMY_W / 2f) - b.pos.x;
                    float rdy = (enemy.y + ENEMY_H / 2f) - b.pos.y;
                    float rlen = (float) Math.sqrt(rdx * rdx + rdy * rdy);
                    if (rlen > 0) { rdx /= rlen; rdy /= rlen; }
                    float rspeed = WEAPON_BULLET_SPEEDS[0];
                    playerBullets.add(new Bullet(b.pos.x, b.pos.y, rdx * rspeed, rdy * rspeed,
                            REFLECT_DAMAGE, b.size, -2));
                } else if (healActiveTimer > 0) {
                    // Invincible during healing — ignore bullet
                    enemyBullets.removeIndex(i);
                } else if (angelEnraged) {
                    // Angel takes all hits during enraged state
                    enemyBullets.removeIndex(i);
                    angelHp -= b.damage;
                } else if (angelActive && ++angelShotCounter >= ANGEL_REFLECT_EVERY) {
                    // Angel reflects every 10th enemy shot
                    angelShotCounter = 0;
                    enemyBullets.removeIndex(i);
                    float rdx = (enemy.x + ENEMY_W / 2f) - b.pos.x;
                    float rdy = (enemy.y + ENEMY_H / 2f) - b.pos.y;
                    float rlen = (float) Math.sqrt(rdx * rdx + rdy * rdy);
                    if (rlen > 0) { rdx /= rlen; rdy /= rlen; }
                    float rspeed = WEAPON_BULLET_SPEEDS[0];
                    playerBullets.add(new Bullet(b.pos.x, b.pos.y, rdx * rspeed, rdy * rspeed,
                            REFLECT_DAMAGE, b.size, -2));
                } else if (playerInvincibleTimer <= 0) {
                    enemyBullets.removeIndex(i);
                    applyPlayerDamage(b.damage, b.weaponIndex);
                }
            }
        }

        // Relic pickup
        if (relikwiaDropActive) {
            if (player.x + PLAYER_W > relikwiaDropX && player.x < relikwiaDropX + RELIC_SIZE
                    && player.y + PLAYER_H > relikwiaDropY && player.y < relikwiaDropY + RELIC_SIZE + PLAYER_H) {
                relikwiaDropActive = false;
                relikwiaCutsceneActive = true;
                relikwiaCutsceneTimer = 0f;
                shield = Math.min(MAX_SHIELD, shield + SHIELD_PER_RELIC);
                int relikwiaHeal = Math.max(1, (int)(effectiveMaxHp() * 0.20f));
                playerHp = Math.min(playerHp + relikwiaHeal, effectiveMaxHp());
            }
        }

        // Drop pickup check
        if (dropActive) {
            dropRect.set(dropX, dropY, DROP_W, DROP_H);
            if (player.overlaps(dropRect)) {
                czystosc = effectiveMaxCzystosc();
                dropActive = false;
            }
        }

        // Portal check
        if (portalActive) {
            portalTimer += delta;
            portalRect.set(PORTAL_X, GROUND_Y, PORTAL_W, PORTAL_H);
            if (player.overlaps(portalRect) && !fadingToBlack && !fadingFromBlack) {
                fadingToBlack = true;
                fadeAlpha = 0f;
            }
        }

        // Sister book pickup
        if (sisterLevel && sisterBookActive) {
            float bx = WORLD_W / 2f - SISTER_BOOK_W / 2f;
            if (player.x + PLAYER_W > bx && player.x < bx + SISTER_BOOK_W
                    && player.y < SISTER_BOOK_H + 10f) {
                sisterBookActive = false;
                sisterIntroCutsceneActive = true;
                sisterIntroPhase = SINT_FADEIN;
                sisterIntroTimer = 0f;
                sisterIntroCamX = MathUtils.clamp(
                        player.x + PLAYER_W / 2f, W / 2f, WORLD_W - W / 2f);
                sisterIntroCamTargetX = sisterIntroCamX;
            }
        }
        // Sister AI
        if (sisterLevel && !sisterIntroCutsceneActive) {
            updateSistersAI(delta);
        }

        // Mini enemies (only on iwonka levels 5+, only when enemy alive)
        if (level >= 5 && !demonLevel && !telefonLevel && enemyAlive) {
            // Spawn timer
            miniSpawnTimer -= delta;
            if (miniSpawnTimer <= 0) {
                float cooldown = Math.max(1f, MINI_SPAWN_COOLDOWN_BASE - (level - 5) * 0.3f);
                miniSpawnTimer = cooldown;
                spawnMiniEnemy();
            }

            // Update mini enemies
            for (int i = miniEnemies.size - 1; i >= 0; i--) {
                MiniEnemy m = miniEnemies.get(i);
                // Chase player
                float mdx = (player.x + PLAYER_W / 2f) - (m.rect.x + MINI_W / 2f);
                float mdy = (player.y + PLAYER_H / 2f) - (m.rect.y + MINI_H / 2f);
                float mLen = (float) Math.sqrt(mdx * mdx + mdy * mdy);
                if (mLen > 0) {
                    m.rect.x += (mdx / mLen) * MINI_SPEED * delta;
                    m.rect.y += (mdy / mLen) * MINI_SPEED * delta;
                }

                // Touch player — deal damage and disappear (not during healing)
                if (playerInvincibleTimer <= 0 && healActiveTimer <= 0 && m.rect.overlaps(player)) {
                    miniEnemies.removeIndex(i);
                    applyPlayerDamage(MINI_DAMAGE, 0);
                    continue;
                }

                // Check if hit by player bullets
                for (int j = playerBullets.size - 1; j >= 0; j--) {
                    Bullet b = playerBullets.get(j);
                    if (b.pos.x + b.size > m.rect.x && b.pos.x < m.rect.x + MINI_W
                            && b.pos.y + b.size > m.rect.y && b.pos.y < m.rect.y + MINI_H) {
                        m.hp -= b.damage;
                        playerBullets.removeIndex(j);
                        if (m.hp <= 0) {
                            miniEnemies.removeIndex(i);
                            score += 15;
                        }
                        break;
                    }
                }
            }
        }
    }

    private void updateIwonkaDeath(float delta) {
        if (!iwonkaDying) return;
        iwonkaDyingTimer += delta;
        if (iwonkaDyingTimer >= 1.6f) {
            iwonkaDying = false;
            iwonkaDied = true;
            camera.zoom = 1.0f;
            handleEnemyKill();
        }
    }

    private void updateOgrodnikDeath(float delta) {
        if (!ogrodnikDying) return;
        ogrodnikDyingTimer += delta;
        if (ogrodnikDyingTimer >= 1.6f) {
            ogrodnikDying = false;
            ogrodnikDied = true;
            camera.zoom = 1.0f;
            handleEnemyKill();
        }
    }

    private void handleEnemyKill() {
        if (!demonLevel && !telefonLevel && !ogrodnikDying && !ogrodnikDied && level == 2) {
            ogrodnikDying = true;
            ogrodnikDyingTimer = 0f;
            ogrodnikAtkActive = false;
            enemyAlive = false;
            enemyBullets.clear();
            return;
        }
        if (!demonLevel && !telefonLevel && !iwonkaDying && !iwonkaDied && (level == 1 || level == 5)) {
            iwonkaDying = true;
            iwonkaDyingTimer = 0f;
            enemyAlive = false;
            enemyBullets.clear();
            return;
        }

        score += 50 * level;

        // Earn money
        int earned = Math.min(100, MONEY_PER_LEVEL * level * moneyMultiplier);
        game.money += earned;
        moneyPopupAmount = earned;
        moneyPopupTimer = 2f;
        game.saveData();

        // Relic drop for levels 1-5 (not demon, not telefon, not sister which uses handleSisterKill)
        if (!demonLevel && !telefonLevel && level >= 1 && level <= 5 && level != 3) {
            relikwiaDropActive = true;
            relikwiaDropIndex = level - 1;
            relikwiaDropX = enemy.x + ENEMY_W / 2f - RELIC_SIZE / 2f;
            relikwiaDropY = enemy.y + 10f;
        }

        // Track demon kills
        if (demonLevel && !telefonLevel) {
            demonKillCount++;
            if (demonKillCount >= DEMON_KILLS_FOR_BOOK) {
                demonStopped = true;
                hasZdrowasBook = true;
            }
        }

        if (level == 5) {
            // After level 5 (iwonka final): portal + czystosc drop + prayer book
            portalActive = true;
            dropActive = true;
            dropX = enemy.x + ENEMY_W / 2f - DROP_W / 2f;
            dropY = enemy.y;
            prayerBookDropActive = true;
            prayerBookDropX = enemy.x + ENEMY_W / 2f - DROP_W / 2f + 40;
            prayerBookDropY = enemy.y;
            enemyAlive = false;
            miniEnemies.clear();
        } else if (demonLevel && demonStopped && !telefonLevel) {
            // After killing demon-jedzenie 5 times: portal + czystosc drop
            portalActive = true;
            dropActive = true;
            dropX = enemy.x + ENEMY_W / 2f - DROP_W / 2f;
            dropY = enemy.y;
            enemyAlive = false;
            miniEnemies.clear();
        } else if (telefonLevel) {
            // Demon telefon killed: portal + czystosc drop
            telefonStopped = true;
            portalActive = true;
            dropActive = true;
            dropX = enemy.x + ENEMY_W / 2f - DROP_W / 2f;
            dropY = enemy.y;
            enemyAlive = false;
            miniEnemies.clear();
        } else if (!demonLevel) {
            // Regular boss kill (Iwonka levels 1-4, ZlyOgrodnik after death anim): portal + czystosc drop
            portalActive = true;
            dropActive = true;
            dropX = enemy.x + ENEMY_W / 2f - DROP_W / 2f;
            dropY = enemy.y;
            enemyAlive = false;
            miniEnemies.clear();
        } else {
            // Intermediate demon-jedzenie kill (not final) — no portal, respawn
            level++;
            setupLevel();
        }
    }

    private void getEnemyTargetPos(float[] out) {
        if (angelEnraged) {
            out[0] = player.x + PLAYER_W - 40 + ANGEL_W * 1.5f;
            out[1] = player.y + ANGEL_H * 1.5f;
        } else {
            out[0] = player.x + PLAYER_W / 2f;
            out[1] = player.y + PLAYER_H / 2f;
        }
    }

    private final float[] targetTemp = new float[2];

    private void spawnEnemyBullet() {
        int bulletDmg = telefonLevel ? 10 + TELEFON_DAMAGE_BONUS : 10;
        Bullet eb = createEnemyBullet(ENEMY_BULLET_SIZE, bulletDmg, -1, 1.0f);
        if (!demonLevel && !telefonLevel) eb.bouncing = true; // Iwonka rabbits bounce
        else eb.spinSpeed = 540f;                             // demon/telefon bullets spin
        enemyBullets.add(eb);
    }

    private void spawnSpecialEnemyBullet() {
        enemyBullets.add(createEnemyBullet(SPECIAL_BULLET_SIZE, SPECIAL_BULLET_DAMAGE, -3, 1.0f));
    }

    private void spawnDemonSpecialBullet() {
        Bullet ds = createEnemyBullet(DEMON_SPECIAL_SIZE, DEMON_SPECIAL_DAMAGE, -4, DEMON_SPECIAL_SPEED_MULT);
        ds.spinSpeed = 720f;
        enemyBullets.add(ds);
    }

    private void spawnHypnoWave(int wi) {
        hypnoWaveStartX[wi]   = enemy.x + ENEMY_W / 2f;
        hypnoWaveStartY[wi]   = enemy.y + ENEMY_H * 0.78f; // eye level
        hypnoWaveEndX[wi]     = player.x + PLAYER_W / 2f;
        hypnoWaveEndY[wi]     = player.y + PLAYER_H / 2f;
        hypnoWaveProgress[wi] = 0f;
        hypnoWaveActive[wi]   = true;
        hypnoWaveHit[wi]      = false;
    }

    private void drawHypnoBeam() {
        if (!hypnoBeamActive && !hypnoWindupActive) return;

        float ex = enemy.x + ENEMY_W / 2f;
        float ey = enemy.y + ENEMY_H * 0.78f;
        float px2 = player.x + PLAYER_W / 2f;
        float py2 = player.y + PLAYER_H / 2f;

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);

        if (hypnoWindupActive) {
            // Pulsing eyes glow
            float pulse = 0.4f + 0.4f * (float) Math.sin((HYPNO_WINDUP - hypnoWindupTimer) * 18f);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0f, 0.5f, 1f, pulse);
            shapeRenderer.circle(ex - 8, ey, 10);
            shapeRenderer.circle(ex + 8, ey, 10);
            shapeRenderer.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
            return;
        }

        // Thin continuous beam (dotted line)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (float t = 0.05f; t < 1f; t += 0.06f) {
            float bx = ex + (px2 - ex) * t;
            float by = ey + (py2 - ey) * t;
            float alpha = 0.08f + 0.10f * (float) Math.sin(hypnoBeamTimer * 10f + t * 30f);
            shapeRenderer.setColor(0.1f, 0.55f, 1f, alpha);
            shapeRenderer.circle(bx, by, 4f);
        }
        shapeRenderer.end();

        // Growing wave rings
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int wi = 0; wi < HYPNO_WAVE_COUNT; wi++) {
            if (!hypnoWaveActive[wi]) continue;
            float prog   = hypnoWaveProgress[wi];
            float wvx    = hypnoWaveStartX[wi] + (hypnoWaveEndX[wi] - hypnoWaveStartX[wi]) * prog;
            float wvy    = hypnoWaveStartY[wi] + (hypnoWaveEndY[wi] - hypnoWaveStartY[wi]) * prog;
            float radius = 18f + prog * 65f; // grows 18→83 as it travels
            float alpha  = (float) Math.sin(prog * Math.PI);
            // Outer glow ring
            shapeRenderer.setColor(0.2f, 0.7f, 1f, alpha * 0.35f);
            shapeRenderer.circle(wvx, wvy, radius + 12f, 24);
            // Mid ring
            shapeRenderer.setColor(0f, 0.85f, 1f, alpha * 0.75f);
            shapeRenderer.circle(wvx, wvy, radius, 24);
            // Inner bright ring
            shapeRenderer.setColor(0.7f, 0.95f, 1f, alpha * 0.5f);
            shapeRenderer.circle(wvx, wvy, radius * 0.45f, 16);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
    }

    private void spawnTelefonSpecialBullet() {
        Bullet ts = createEnemyBullet(TELEFON_SPECIAL_SIZE, TELEFON_SPECIAL_DAMAGE, -5, TELEFON_SPECIAL_SPEED_MULT);
        ts.spinSpeed = 720f;
        enemyBullets.add(ts);
    }

    private void spawnEnemyAtRandomPosition() {
        // Spawn on ground, far enough from player horizontally
        for (int attempt = 0; attempt < 50; attempt++) {
            float ex = MathUtils.random(0, WORLD_W - ENEMY_W);
            float dx = ex - player.x;
            if (Math.abs(dx) >= ENEMY_MIN_DISTANCE) {
                enemy.x = ex;
                enemy.y = GROUND_Y;
                return;
            }
        }
        // Fallback: opposite side from player
        enemy.x = player.x < WORLD_W / 2f ? WORLD_W - ENEMY_W - 20 : 20;
        enemy.y = GROUND_Y;
    }

    private boolean bulletHitsRect(Bullet b, Rectangle rect, int rw, int rh) {
        return b.pos.x + b.size > rect.x && b.pos.x < rect.x + rw
                && b.pos.y + b.size > rect.y && b.pos.y < rect.y + rh;
    }


    private void drawHpBars(float delta) {
        // Green lung particles during healing
        if (healActiveTimer > 0) {
            drawHealParticles(delta);
        }

        // Reflect shield effect
        if (reflectActiveTimer > 0) {
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.2f, 0.5f, 1f, 0.25f);
            shapeRenderer.circle(player.x + PLAYER_W / 2f, player.y + PLAYER_H / 2f, 100, 32);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.3f, 0.6f, 1f, 0.8f);
            shapeRenderer.circle(player.x + PLAYER_W / 2f, player.y + PLAYER_H / 2f, 100, 32);
            shapeRenderer.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
        }

        // Enlighten effect (yellow sphere around enemy)
        if (enlightenActiveTimer > 0 && enemyAlive) {
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 0.95f, 0.3f, 0.3f);
            shapeRenderer.circle(enemy.x + ENEMY_W / 2f, enemy.y + ENEMY_H / 2f, 100, 32);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1f, 0.9f, 0.1f, 0.9f);
            shapeRenderer.circle(enemy.x + ENEMY_W / 2f, enemy.y + ENEMY_H / 2f, 100, 32);
            shapeRenderer.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Player HP bar (bottom-left, relative to camera)
        float camLeft = camera.position.x - W / 2f;
        float camBottom = camera.position.y - H / 2f;
        float barW = 150;
        float barH = 12;
        float px = camLeft + 10, py = camBottom + 10;
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(px, py, barW, barH);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(px, py, barW * ((float) playerHp / effectiveMaxHp()), barH);

        // Player Czystosc bar (above HP bar)
        float mpy = py + barH + 4;
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(px, mpy, barW, barH);
        shapeRenderer.setColor(CZYSTOSC_COLOR);
        shapeRenderer.rect(px, mpy, barW * ((float) czystosc / effectiveMaxCzystosc()), barH);

        // Hypnosis bar (above Czystosc bar, only when active)
        if (playerHypnoTimer > 0) {
            float hypnoBarY = mpy + barH + 4;
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(px, hypnoBarY, barW, barH);
            shapeRenderer.setColor(0f, 0.6f, 1f, 1f);
            shapeRenderer.rect(px, hypnoBarY, barW * (playerHypnoTimer / HYPNO_PLAYER_DURATION), barH);
        }

        // Shield bar (above Czystosc bar)
        float spy = mpy + barH + 4;
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(px, spy, barW, barH);
        if (shield > 0) {
            shapeRenderer.setColor(SHIELD_COLOR);
            shapeRenderer.rect(px, spy, barW * ((float) shield / MAX_SHIELD), barH);
        }

        // Angel HP bar (above angel, only when enraged)
        if (angelEnraged) {
            float angelBarX = player.x + PLAYER_W - 40;
            float angelBarY = player.y + ANGEL_H * 3 + 5;
            float angelBarW = ANGEL_W * 3;
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(angelBarX, angelBarY, angelBarW, barH);
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(angelBarX, angelBarY, angelBarW * ((float) angelHp / ANGEL_ENRAGED_MAX_HP), barH);
        }

        // HP bary sióstr
        if (sister1Alive) {
            float sbarW = SISTER_W + 10;
            float s1bx = sister1.x + SISTER_W / 2f - sbarW / 2f;
            float s1by = sister1.y + SISTER_H + 5;
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(s1bx, s1by, sbarW, barH);
            shapeRenderer.setColor(new Color(0.9f, 0.3f, 0.7f, 1f));
            shapeRenderer.rect(s1bx, s1by, sbarW * ((float) sister1Hp / SISTER_MAX_HP), barH);
        }
        if (sister2Alive) {
            float sbarW = SISTER_W + 10;
            float s2bx = sister2.x + SISTER_W / 2f - sbarW / 2f;
            float s2by = sister2.y + SISTER_H + 5;
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(s2bx, s2by, sbarW, barH);
            shapeRenderer.setColor(new Color(0.6f, 0.1f, 0.8f, 1f));
            shapeRenderer.rect(s2bx, s2by, sbarW * ((float) sister2Hp / SISTER_MAX_HP), barH);
        }

        // Enemy HP bar (above enemy's head)
        if (enemyAlive) {
            float enemyBarW = ENEMY_W + 10;
            float ebx = enemy.x + ENEMY_W / 2f - enemyBarW / 2f;
            float eby = enemy.y + ENEMY_H + 5;
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(ebx, eby, enemyBarW, barH);
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(ebx, eby, enemyBarW * ((float) enemyHp / enemyMaxHp), barH);
        }

        shapeRenderer.end();

        // Labels
        game.batch.begin();
        game.font.getData().setScale(0.7f);
        game.font.setColor(Color.BLACK);
        game.font.draw(game.batch, "HP", px + barW + 5, py + barH + 4);
        game.font.draw(game.batch, "CZYSTOSC", px + barW + 5, mpy + barH + 4);
        game.font.setColor(shield > 0 ? SHIELD_COLOR : Color.DARK_GRAY);
        game.font.draw(game.batch, "TARCZA", px + barW + 5, spy + barH + 4);
        // Hypnosis / slow indicators
        if (playerHypnoTimer > 0) {
            game.font.setColor(0f, 0.6f, 1f, 1f);
            game.font.draw(game.batch, "HIPNOZA " + String.format("%.1f", playerHypnoTimer) + "s",
                    px + barW + 5, mpy + barH + 24);
        }
        if (playerAttackSlowTimer > 0) {
            game.font.getData().setScale(0.55f);
            game.font.setColor(0.4f, 0.7f, 1f, 1f);
            game.font.draw(game.batch, "SPOWOLNIENIE " + (int) Math.ceil(playerAttackSlowTimer) + "s",
                    px, mpy - 14);
        }
        // Sprint indicator
        if (sprintActive) {
            game.font.getData().setScale(0.75f);
            game.font.setColor(new Color(1f, 0.55f, 0f, 1f));
            game.font.draw(game.batch, "SPRINT " + String.format("%.1f", sprintTimer) + "s", px, mpy + barH + 22);
        } else if (sprintCooldownTimer > 0) {
            game.font.getData().setScale(0.55f);
            game.font.setColor(Color.GRAY);
            game.font.draw(game.batch, "sprint [" + (int) Math.ceil(sprintCooldownTimer) + "s]", px, mpy + barH + 22);
        }
        game.font.getData().setScale(1f);
        game.batch.end();
    }

    private void drawPauseMenu() {
        float cx = camera.position.x, cy = camera.position.y;

        // Dim background
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.6f);
        shapeRenderer.rect(cx - W / 2f, cy - H / 2f, W, H);

        // Stats panel background (left edge + 100)
        float panelX = cx - W / 2f + 100;
        float panelY = cy - 140;
        float panelW = 280;
        float panelH = 280;
        shapeRenderer.setColor(0.08f, 0.08f, 0.12f, 0.92f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();

        // Stats panel border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.LIGHT_GRAY);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();

        // HP bar inside panel
        float barW = panelW - 20;
        float barH = 12;
        float barX = panelX + 10;
        float hpBarY = panelY + panelH - 65;
        float czBarY = hpBarY - 30;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // HP bar background
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(barX, hpBarY, barW, barH);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(barX, hpBarY, barW * ((float) playerHp / effectiveMaxHp()), barH);
        // Czystosc bar background
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(barX, czBarY, barW, barH);
        shapeRenderer.setColor(CZYSTOSC_COLOR);
        shapeRenderer.rect(barX, czBarY, barW * ((float) czystosc / effectiveMaxCzystosc()), barH);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        // Text
        game.batch.begin();

        // Pause menu buttons (left side)
        game.font.getData().setScale(1.2f);
        game.font.setColor(Color.WHITE);
        String resume = (pauseSelection == 0 ? "> " : "  ") + "Wznow";
        String quit   = (pauseSelection == 1 ? "> " : "  ") + "Wyjdz do menu";
        game.font.draw(game.batch, resume, cx - 60, cy + 20);
        game.font.draw(game.batch, quit,   cx - 60, cy - 20);

        // Panel title
        game.font.getData().setScale(0.85f);
        game.font.setColor(Color.YELLOW);
        game.font.draw(game.batch, "STATYSTYKI", panelX + 65, panelY + panelH - 12);

        // HP / Czystosc labels + values
        game.font.getData().setScale(0.7f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "HP:        " + playerHp + " / " + effectiveMaxHp(), barX, hpBarY - 4);
        game.font.draw(game.batch, "Czystosc:  " + czystosc + " / " + effectiveMaxCzystosc(),  barX, czBarY - 4);

        // Weapons
        game.font.getData().setScale(0.75f);
        game.font.setColor(Color.LIGHT_GRAY);
        game.font.draw(game.batch, "BRONIE:", barX, czBarY - 30);

        float lineY = czBarY - 55;
        for (int i = 0; i < 3; i++) {
            boolean selected = (i == currentWeapon);
            game.font.getData().setScale(selected ? 0.75f : 0.65f);
            game.font.setColor(selected ? Color.WHITE : new Color(0.6f, 0.6f, 0.6f, 1f));

            String arrow = selected ? "> " : "  ";
            String ammoStr;
            if (reloadTimer[i] > 0) {
                ammoStr = "[reload " + String.format("%.1f", reloadTimer[i]) + "s]";
            } else {
                ammoStr = ammo[i] + "/" + WEAPON_MAX_AMMO[i];
            }
            game.font.draw(game.batch, arrow + WEAPON_NAMES[i] + "  " + ammoStr, barX, lineY);
            lineY -= 28;
        }

        // Level + score
        game.font.getData().setScale(0.65f);
        game.font.setColor(new Color(0.7f, 0.9f, 1f, 1f));
        game.font.draw(game.batch, "Poziom: " + level + "   Punkty: " + score, barX, panelY + 22);

        game.font.getData().setScale(1f);
        game.batch.end();
    }

    private void handlePauseInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            pauseSelection = 0;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            pauseSelection = 1;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (pauseSelection == 0) {
                paused = false;
            } else {
                goToMenu();
                return;
            }
        }
        // Mouse click on pause menu items
        if (Gdx.input.justTouched()) {
            float cx = camera.position.x, cy = camera.position.y;
            mouseTemp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(mouseTemp);
            float mx = mouseTemp.x;
            float my = mouseTemp.y;
            // "Wznow" button area
            if (mx >= cx - 60 && mx <= cx + 140 && my >= cy + 0 && my <= cy + 30) {
                paused = false;
            }
            // "Wyjdz" button area
            if (mx >= cx - 60 && mx <= cx + 140 && my >= cy - 40 && my <= cy - 10) {
                goToMenu();
                return;
            }
        }
    }

    private void drawGameOver() {
        float cx = camera.position.x, cy = camera.position.y;
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.3f, 0, 0, 0.7f);
        shapeRenderer.rect(cx - W / 2f, cy - H / 2f, W, H);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        game.batch.begin();
        game.font.setColor(Color.BLACK);
        game.font.getData().setScale(2f);
        game.font.draw(game.batch, "KONIEC GRY", cx - 120, cy + 60);
        game.font.getData().setScale(1f);
        game.font.draw(game.batch, "Punkty: " + score, cx - 70, cy - 5);
        game.font.draw(game.batch, "Poziom: " + level, cx - 70, cy - 35);
        game.font.getData().setScale(0.7f);
        game.font.draw(game.batch, "ENTER - zagraj ponownie  |  ESC - menu", cx - 200, cy - 80);
        game.font.getData().setScale(1f);
        game.batch.end();
    }

    private void handleGameOverInput() {
        if (!gameOver) return;
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            restartGame();
        }
    }

    private void goToMenu() {
        if (score > game.highScore) { game.highScore = score; game.saveData(); }
        if (game.battleMusic != null) game.battleMusic.stop();
        game.backgroundMusic.play();
        screenLeft = true;
        game.setScreen(new MenuScreen(game));
        dispose();
    }

    private void restartGame() {
        level = 1;
        score = 0;
        playerHp = effectiveMaxHp();
        gameOver = false;
        paused = false;
        player.x = W / 2f - PLAYER_W / 2f;
        player.y = 30;
        playerInvincibleTimer = 0;
        playerVelY = 0;
        jumpsLeft = MAX_JUMPS;
        reflectCooldownTimer = 0;
        reflectActiveTimer = 0;
        healCooldownTimer = 0;
        healActiveTimer = 0;
        dashCooldownTimer = 0;
        sprintActive = false;
        sprintTimer = 0;
        sprintCooldownTimer = 0;
        lastPressATimer = 999f;
        lastPressDTimer = 999f;
        enlightenCooldownTimer = 0;
        enlightenActiveTimer = 0;
        prayerBookOpen = false;
        hasPrayerBook = false;
        prayerAttackActive = false;
        prayerBookDropActive = false;
        prayerWords.clear();
        angelActive = false;
        angelCutsceneActive = false;
        angelShotCounter = 0;
        angelEnraged = false;
        angelEnragedTimer = 0;
        zdrowasAttackActive = false;
        demonKillCount = 0;
        hasZdrowasBook = false;
        demonStopped = false;
        playerSlowTimer = 0;
        czystoscRegenDelayTimer = 0;
        czystoscRegenAccumulator = 0;
        czystosc = effectiveMaxCzystosc();
        portalShopOpen = false;
        ogrodnikDying = false;
        ogrodnikDied = false;
        ogrodnikAtkActive = false;
        healParticleAngle = 0f;
        healPlusses.clear();
        healPlusSpawnTimer = 0f;
        sisterBookActive = false;
        sister1Alive = false;
        sister2Alive = false;
        sisterIntroCutsceneActive = false;
        playerOnLadder = false;
        ladderTopReached = false;
        ladderKasaActive = false;
        ladderKsiegaActive = false;
        sisterIntroPhase = SINT_BOOK;
        sisterIntroFadeAlpha = 0f;
        sisterIntroTextAlpha = 0f;
        abilityMenuOpen = false;
        currentWeapon = 0;
        for (int i = 0; i < 3; i++) { ammo[i] = WEAPON_MAX_AMMO[i]; reloadTimer[i] = 0; }
        setupLevel();
    }

    private void drawLadder() {
        // Only visible when player is near the left end of the map
        if (player.x > LADDER_X + LADDER_VISIBLE_DIST) return;
        float ladderTop = LADDER_BASE_Y + LADDER_H;
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // Rails (left and right vertical bars) — dark brown
        shapeRenderer.setColor(0.22f, 0.10f, 0.02f, 1f);
        shapeRenderer.rect(LADDER_X, LADDER_BASE_Y, 7f, LADDER_H);
        shapeRenderer.rect(LADDER_X + LADDER_W - 7f, LADDER_BASE_Y, 7f, LADDER_H);
        // Rungs (horizontal bars) — slightly less dark
        shapeRenderer.setColor(0.30f, 0.15f, 0.03f, 1f);
        for (float ry = 35f; ry < LADDER_H; ry += 42f) {
            shapeRenderer.rect(LADDER_X + 7f, LADDER_BASE_Y + ry, LADDER_W - 14f, 6f);
        }
        // Larger platform at the top
        shapeRenderer.setColor(0.20f, 0.09f, 0.02f, 1f);
        shapeRenderer.rect(LADDER_X - 80f, ladderTop, LADDER_W + 160f, 12f);
        shapeRenderer.end();
    }

    private void drawHealParticles(float delta) {
        healParticleAngle = (healParticleAngle + 90f * delta) % 360f;
        float cx = player.x + PLAYER_W / 2f;
        float cy = player.y + PLAYER_H / 2f;
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        int count = 5;
        float orbit = 90f;
        for (int i = 0; i < count; i++) {
            float angle = (float) Math.toRadians(healParticleAngle + i * (360f / count));
            float lx = cx + (float) Math.cos(angle) * orbit;
            float ly = cy + (float) Math.sin(angle) * orbit;
            float pulse = 0.55f + 0.35f * (float) Math.sin(Math.toRadians(healParticleAngle * 4 + i * 72));
            shapeRenderer.setColor(0f, 0.85f, 0.2f, Math.max(0.3f, pulse));
            // Lung shape: two overlapping ellipses (left and right lobe)
            shapeRenderer.ellipse(lx - 14, ly - 14, 15, 26);
            shapeRenderer.ellipse(lx - 1, ly - 14, 15, 26);
            // Bright inner highlight
            shapeRenderer.setColor(0.4f, 1f, 0.5f, 0.5f);
            shapeRenderer.circle(lx - 7, ly, 4);
            shapeRenderer.circle(lx + 6, ly, 4);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
    }

    private static final int   PORTAL_SHOP_HP_AMOUNT  = 100;
    private static final int   PORTAL_SHOP_CZ_AMOUNT  = 100;
    private static final int   PORTAL_SHOP_SHIELD_AMT = 25;
    private static final int   PORTAL_SHOP_SHIELD_PRICE = 30;

    private void handlePortalShopInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            advanceFromPortalShop();
            return;
        }
        if (Gdx.input.justTouched()) {
            mouseTemp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(mouseTemp);
            float mx = mouseTemp.x;
            float my = mouseTemp.y;
            float cx = camera.position.x;
            float cy = camera.position.y;
            float itemSize = 100f;

            // HP upgrade (always +100)
            float hpItemX = cx - 230;
            float hpItemY = cy - itemSize / 2f;
            if (mx >= hpItemX && mx <= hpItemX + itemSize
                    && my >= hpItemY && my <= hpItemY + itemSize
                    && game.money >= game.hpUpgradePrice) {
                game.clickSound.play(game.clickVolume);
                game.money -= game.hpUpgradePrice;
                game.playerBonusHp += PORTAL_SHOP_HP_AMOUNT;
                playerHp = Math.min(playerHp + PORTAL_SHOP_HP_AMOUNT, effectiveMaxHp());
                game.hpUpgradePrice += game.hpUpgradePrice / 4;
                game.saveData();
                return;
            }

            // Czystosc upgrade (always +100)
            float czItemX = cx - 50;
            float czItemY = cy - itemSize / 2f;
            if (mx >= czItemX && mx <= czItemX + itemSize
                    && my >= czItemY && my <= czItemY + itemSize
                    && game.money >= game.czystoscUpgradePrice) {
                game.clickSound.play(game.clickVolume);
                game.money -= game.czystoscUpgradePrice;
                game.czystoscBonusMax += PORTAL_SHOP_CZ_AMOUNT;
                czystosc = Math.min(czystosc + PORTAL_SHOP_CZ_AMOUNT, effectiveMaxCzystosc());
                game.czystoscUpgradePrice += game.czystoscUpgradePrice / 4;
                game.saveData();
                return;
            }

            // Shield upgrade (+25, one-time per run)
            float shItemX = cx + 130;
            float shItemY = cy - itemSize / 2f;
            if (!portalShieldUpgradeBought
                    && mx >= shItemX && mx <= shItemX + itemSize
                    && my >= shItemY && my <= shItemY + itemSize
                    && game.money >= PORTAL_SHOP_SHIELD_PRICE) {
                game.clickSound.play(game.clickVolume);
                game.money -= PORTAL_SHOP_SHIELD_PRICE;
                shield = Math.min(MAX_SHIELD, shield + PORTAL_SHOP_SHIELD_AMT);
                portalShieldUpgradeBought = true;
                game.saveData();
                return;
            }

            // SKIP button area
            float skipX = cx - 80;
            float skipY = cy - 200;
            if (mx >= skipX && mx <= skipX + 160 && my >= skipY && my <= skipY + 50) {
                game.clickSound.play(game.clickVolume);
                advanceFromPortalShop();
            }
        }
    }

    private void drawPortalShop() {
        float cx = camera.position.x;
        float cy = camera.position.y;
        float itemSize = 100f;

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Title
        game.font.getData().setScale(1.8f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "SKLEP", cx - 70, cy + 330);

        // Money display
        game.font.getData().setScale(1f);
        game.font.setColor(Color.YELLOW);
        float kasaSize = 40f;
        game.batch.draw(game.kasaTex, cx - 90, cy + 260, kasaSize, kasaSize);
        game.font.draw(game.batch, "" + game.money, cx - 40, cy + 295);

        // HP upgrade item (always +100)
        float hpItemX = cx - 230;
        float hpItemY = cy - itemSize / 2f;
        game.batch.draw(game.hpUpgradeTex, hpItemX, hpItemY, itemSize, itemSize);
        game.font.getData().setScale(0.75f);
        game.font.setColor(game.money >= game.hpUpgradePrice ? Color.GREEN : Color.RED);
        game.font.draw(game.batch, "Cena: " + game.hpUpgradePrice, hpItemX, hpItemY - 10);
        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(0.65f);
        game.font.draw(game.batch, "+" + PORTAL_SHOP_HP_AMOUNT + " HP", hpItemX + 10, hpItemY - 30);

        // Czystosc upgrade item (always +100)
        float czItemX = cx - 50;
        float czItemY = cy - itemSize / 2f;
        game.batch.draw(game.czystoscUpgradeTex, czItemX, czItemY, itemSize, itemSize);
        game.font.getData().setScale(0.75f);
        game.font.setColor(game.money >= game.czystoscUpgradePrice ? Color.GREEN : Color.RED);
        game.font.draw(game.batch, "Cena: " + game.czystoscUpgradePrice, czItemX, czItemY - 10);
        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(0.65f);
        game.font.draw(game.batch, "+" + PORTAL_SHOP_CZ_AMOUNT + " Czystosc", czItemX + 10, czItemY - 30);

        // Shield upgrade item (+25, one-time)
        float shItemX = cx + 130;
        float shItemY = cy - itemSize / 2f;
        if (game.relikwiaTex != null && game.relikwiaTex.length > 0) {
            game.batch.setColor(portalShieldUpgradeBought ? 0.4f : 1f, portalShieldUpgradeBought ? 0.4f : 1f, portalShieldUpgradeBought ? 0.4f : 1f, 1f);
            game.batch.draw(game.relikwiaTex[0], shItemX, shItemY, itemSize, itemSize);
            game.batch.setColor(1f, 1f, 1f, 1f);
        }
        game.font.getData().setScale(0.75f);
        if (portalShieldUpgradeBought) {
            game.font.setColor(Color.GRAY);
            game.font.draw(game.batch, "KUPIONO", shItemX, shItemY - 10);
        } else {
            game.font.setColor(game.money >= PORTAL_SHOP_SHIELD_PRICE ? new Color(1f, 0.8f, 0.1f, 1f) : Color.RED);
            game.font.draw(game.batch, "Cena: " + PORTAL_SHOP_SHIELD_PRICE, shItemX, shItemY - 10);
        }
        game.font.setColor(new Color(1f, 0.8f, 0.1f, 1f));
        game.font.getData().setScale(0.65f);
        game.font.draw(game.batch, "+25 TARCZA", shItemX + 5, shItemY - 30);

        // SKIP button
        game.font.getData().setScale(1.2f);
        game.font.setColor(Color.YELLOW);
        game.font.draw(game.batch, "[ SKIP ]", cx - 60, cy - 165);

        game.font.getData().setScale(0.6f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "ENTER lub kliknij SKIP aby kontynuowac", cx - 185, cy - 210);
        game.font.draw(game.batch, "Kliknij na przedmiot aby kupic", cx - 145, cy - 235);

        game.font.getData().setScale(1f);
        game.batch.end();
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

    // --- Helper methods ---

    /** Odejmuje czystosc i resetuje opóźnienie regeneracji. */
    private void consumeCzystosc(int cost) {
        czystosc -= cost;
        czystoscRegenDelayTimer = CZYSTOSC_REGEN_DELAY;
        czystoscRegenAccumulator = 0;
    }

    private int effectiveMaxHp() { return PLAYER_MAX_HP + game.playerBonusHp; }
    private int effectiveMaxCzystosc() { return MAX_CZYSTOSC + game.czystoscBonusMax; }

    /** Aktywuje aniolka (cutscenę) gdy HP gracza jest niskie. */
    private void tryActivateAngel() {
        if (!angelActive && playerHp <= 20) {
            angelActive = true;
            angelCutsceneActive = true;
            angelCutsceneTimer = 0;
            angelShotCounter = 0;
        }
    }

    /** Zadaje obrażenia graczowi: nietykalność, efekty specjalne, game over. */
    private void applyPlayerDamage(int damage, int weaponIndex) {
        tryActivateAngel();
        // Tarcza absorbuje obrażenia przed HP
        if (shield > 0) {
            int absorbed = Math.min(shield, damage);
            shield -= absorbed;
            damage -= absorbed;
        }
        playerHp -= damage;
        playerInvincibleTimer = 1.5f;
        if (weaponIndex == -3 && enlightenActiveTimer <= 0) {
            enemyHp = Math.min(enemyHp + enemyMaxHp / 5, enemyMaxHp);
        }
        if (weaponIndex == -4 || weaponIndex == -5) {
            playerSlowTimer = DEMON_SLOW_DURATION;
        }
        if (playerHp <= 0) {
            gameOver = true;
            abilityMenuOpen = false;
            prayerBookOpen = false;
            if (score > game.highScore) { game.highScore = score; game.saveData(); }
        }
    }

    /** Tworzy pocisk przeciwnika wycelowany w gracza (lub aniolka). */
    private Bullet createEnemyBullet(int bulletSize, int damage, int weaponIndex, float speedMult) {
        float ex = enemy.x + ENEMY_W / 2f - bulletSize / 2f;
        float ey = enemy.y + ENEMY_H / 2f - bulletSize / 2f;
        getEnemyTargetPos(targetTemp);
        float dx = targetTemp[0] - (enemy.x + ENEMY_W / 2f);
        float dy = targetTemp[1] - (enemy.y + ENEMY_H / 2f);
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) { dx /= len; dy /= len; }
        return new Bullet(ex, ey, dx * enemyBulletSpeed * speedMult, dy * enemyBulletSpeed * speedMult,
                damage, bulletSize, weaponIndex);
    }

    private void spawnMiniEnemy() {
        // Spawn at random edge of visible screen
        float camLeft = camera.position.x - W / 2f;
        float mx, my;
        int edge = MathUtils.random(3);
        switch (edge) {
            case 0: mx = MathUtils.random(camLeft, camLeft + W - MINI_W); my = H; break;
            case 1: mx = MathUtils.random(camLeft, camLeft + W - MINI_W); my = -MINI_H; break;
            case 2: mx = camLeft - MINI_W; my = MathUtils.random(0, H - MINI_H); break;
            default: mx = camLeft + W; my = MathUtils.random(0, H - MINI_H); break;
        }
        miniEnemies.add(new MiniEnemy(mx, my, MINI_HP));
    }

    // --- Sister methods ---

    private void updateSisterIntro(float delta) {
        if (sisterIntroPhase == SINT_BOOK) return;
        sisterIntroTimer -= delta;
        switch (sisterIntroPhase) {
            case SINT_FADEIN:
                sisterIntroFadeAlpha = Math.min(1f, sisterIntroFadeAlpha + 1.5f * delta);
                if (sisterIntroFadeAlpha >= 1f) {
                    sisterIntroPhase = SINT_TEXT_IN;
                    sisterIntroTimer = 0.7f;
                }
                break;
            case SINT_TEXT_IN:
                sisterIntroTextAlpha = Math.min(1f, sisterIntroTextAlpha + 1.5f * delta);
                if (sisterIntroTimer <= 0) {
                    sisterIntroPhase = SINT_TEXT_HOLD;
                    sisterIntroTimer = 2.5f;
                }
                break;
            case SINT_TEXT_HOLD:
                if (sisterIntroTimer <= 0) {
                    sisterIntroPhase = SINT_TEXT_OUT;
                    sisterIntroTimer = 0.7f;
                }
                break;
            case SINT_TEXT_OUT:
                sisterIntroTextAlpha = Math.max(0f, sisterIntroTextAlpha - 1.5f * delta);
                if (sisterIntroTimer <= 0) {
                    sisterIntroPhase = SINT_FADEOUT;
                    sisterIntroTimer = 0.8f;
                }
                break;
            case SINT_FADEOUT:
                sisterIntroFadeAlpha = Math.max(0f, sisterIntroFadeAlpha - 1.5f * delta);
                if (sisterIntroTimer <= 0) {
                    sisterIntroPhase = SINT_CAM_S1;
                    sisterIntroTimer = 1.5f;
                    sisterIntroCamTargetX = MathUtils.clamp(
                            sister1.x + SISTER_W / 2f, W / 2f, WORLD_W - W / 2f);
                }
                break;
            case SINT_CAM_S1:
                if (sisterIntroTimer <= 0) {
                    sisterIntroPhase = SINT_SHAKE_S1;
                    sisterIntroTimer = 1.0f;
                    sister1Alive = true;
                }
                break;
            case SINT_SHAKE_S1:
                if (sisterIntroTimer <= 0) {
                    sisterIntroPhase = SINT_CAM_S2;
                    sisterIntroTimer = 1.5f;
                    sisterIntroCamTargetX = MathUtils.clamp(
                            sister2.x + SISTER_W / 2f, W / 2f, WORLD_W - W / 2f);
                }
                break;
            case SINT_CAM_S2:
                if (sisterIntroTimer <= 0) {
                    sisterIntroPhase = SINT_SHAKE_S2;
                    sisterIntroTimer = 1.0f;
                    sister2Alive = true;
                }
                break;
            case SINT_SHAKE_S2:
                if (sisterIntroTimer <= 0) {
                    sisterIntroPhase = SINT_CAM_BACK;
                    sisterIntroTimer = 1.5f;
                    sisterIntroCamTargetX = MathUtils.clamp(
                            player.x + PLAYER_W / 2f, W / 2f, WORLD_W - W / 2f);
                }
                break;
            case SINT_CAM_BACK:
                if (sisterIntroTimer <= 0) {
                    sisterIntroCutsceneActive = false;
                    sisterIntroPhase = SINT_BOOK; // reset, fight started
                }
                break;
        }
    }

    private void updateSistersAI(float delta) {
        if (sister1ShootingTimer > 0) sister1ShootingTimer -= delta;
        if (sister2ShootingTimer > 0) sister2ShootingTimer -= delta;

        // Sister 1 – chase player, bouncing bullets
        if (sister1Alive) {
            float dx1 = (player.x + PLAYER_W / 2f) - (sister1.x + SISTER_W / 2f);
            if (dx1 > 0) { sister1.x += SISTER_SPEED * delta; sister1FacingLeft = false; }
            else if (dx1 < 0) { sister1.x -= SISTER_SPEED * delta; sister1FacingLeft = true; }
            sister1.x = MathUtils.clamp(sister1.x, 0, WORLD_W - SISTER_W);

            // Gravity + jump
            sister1JumpTimer -= delta;
            boolean s1OnGround = (sister1VelY == 0 && sister1.y <= GROUND_Y + 1);
            if (s1OnGround && player.y > sister1.y + SISTER_H * 0.5f && sister1JumpTimer <= 0) {
                sister1VelY = SISTER_JUMP_VELOCITY;
                sister1JumpTimer = SISTER_JUMP_COOLDOWN;
            }
            sister1VelY -= GRAVITY * delta;
            sister1.y += sister1VelY * delta;
            if (sister1.y <= GROUND_Y) { sister1.y = GROUND_Y; sister1VelY = 0; }
            if (sister1VelY <= 0) {
                for (float[] p : PLATFORMS) {
                    float pTop = p[1] + p[3];
                    if (sister1.x + SISTER_W > p[0] && sister1.x < p[0] + p[2]
                            && sister1.y <= pTop && sister1.y >= pTop - 20) {
                        sister1.y = pTop; sister1VelY = 0; break;
                    }
                }
            }
            sister1.y = MathUtils.clamp(sister1.y, GROUND_Y, H - SISTER_H);

            // Shoot
            sister1ShootTimer -= delta;
            if (sister1ShootTimer <= 0) {
                sister1ShootTimer = SISTER_SHOOT_COOLDOWN;
                sister1BurstRemaining = 1;
                sister1BurstTimer = 0;
                sister1ShootingTimer = 0.4f;
            }
            if (sister1BurstRemaining > 0) {
                sister1BurstTimer -= delta;
                if (sister1BurstTimer <= 0) {
                    spawnSister1Bullet();
                    sister1BurstRemaining--;
                    sister1BurstTimer = BURST_INTERVAL;
                }
            }
        }

        // Sister 2 – chase player, special (big) bullets with windup
        if (sister2Alive) {
            if (sister2WindupTimer <= 0) {
                float dx2 = (player.x + PLAYER_W / 2f) - (sister2.x + SISTER_W / 2f);
                if (dx2 > 0) { sister2.x += SISTER_SPEED * delta; sister2FacingLeft = false; }
                else if (dx2 < 0) { sister2.x -= SISTER_SPEED * delta; sister2FacingLeft = true; }
            }
            sister2.x = MathUtils.clamp(sister2.x, 0, WORLD_W - SISTER_W);

            sister2JumpTimer -= delta;
            boolean s2OnGround = (sister2VelY == 0 && sister2.y <= GROUND_Y + 1);
            if (s2OnGround && player.y > sister2.y + SISTER_H * 0.5f && sister2JumpTimer <= 0) {
                sister2VelY = SISTER_JUMP_VELOCITY;
                sister2JumpTimer = SISTER_JUMP_COOLDOWN;
            }
            sister2VelY -= GRAVITY * delta;
            sister2.y += sister2VelY * delta;
            if (sister2.y <= GROUND_Y) { sister2.y = GROUND_Y; sister2VelY = 0; }
            if (sister2VelY <= 0) {
                for (float[] p : PLATFORMS) {
                    float pTop = p[1] + p[3];
                    if (sister2.x + SISTER_W > p[0] && sister2.x < p[0] + p[2]
                            && sister2.y <= pTop && sister2.y >= pTop - 20) {
                        sister2.y = pTop; sister2VelY = 0; break;
                    }
                }
            }
            sister2.y = MathUtils.clamp(sister2.y, GROUND_Y, H - SISTER_H);

            // Special attack with windup
            sister2SpecialTimer -= delta;
            if (sister2WindupTimer > 0) {
                sister2WindupTimer -= delta;
                if (sister2WindupTimer <= 0) {
                    spawnSister2Bullet();
                    sister2SpecialTimer = SISTER_SPECIAL_COOLDOWN;
                }
            } else if (sister2SpecialTimer <= 0) {
                sister2WindupTimer = SISTER_WINDUP;
                sister2ShootingTimer = SISTER_WINDUP + 0.3f;
            }
        }

        // Minimum separation between sisters
        if (sister1Alive && sister2Alive) {
            float dist = Math.abs((sister1.x + SISTER_W / 2f) - (sister2.x + SISTER_W / 2f));
            if (dist < SISTER_MIN_SEPARATION) {
                float push = (SISTER_MIN_SEPARATION - dist) * 0.5f * delta * 80f;
                if (sister1.x < sister2.x) {
                    sister1.x -= push; sister2.x += push;
                } else {
                    sister1.x += push; sister2.x -= push;
                }
                sister1.x = MathUtils.clamp(sister1.x, 0, WORLD_W - SISTER_W);
                sister2.x = MathUtils.clamp(sister2.x, 0, WORLD_W - SISTER_W);
            }
        }
    }

    private void spawnSister1Bullet() {
        float ex = sister1.x + SISTER_W / 2f - SISTER_BULLET_SIZE / 2f;
        float ey = sister1.y + SISTER_H / 2f - SISTER_BULLET_SIZE / 2f;
        float dx = (player.x + PLAYER_W / 2f) - (sister1.x + SISTER_W / 2f);
        float dy = (player.y + PLAYER_H / 2f) - (sister1.y + SISTER_H / 2f);
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) { dx /= len; dy /= len; }
        float spd = ENEMY_BULLET_SPEED_BASE + (level - 1) * 20f;
        Bullet b = new Bullet(ex, ey, dx * spd, dy * spd, SISTER_BULLET_DMG, SISTER_BULLET_SIZE, -6);
        enemyBullets.add(b);
    }

    private void spawnSister2Bullet() {
        float ex = sister2.x + SISTER_W / 2f - SISTER_SPECIAL_SIZE / 2f;
        float ey = sister2.y + SISTER_H / 2f - SISTER_SPECIAL_SIZE / 2f;
        float dx = (player.x + PLAYER_W / 2f) - (sister2.x + SISTER_W / 2f);
        float dy = (player.y + PLAYER_H / 2f) - (sister2.y + SISTER_H / 2f);
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) { dx /= len; dy /= len; }
        float spd = (ENEMY_BULLET_SPEED_BASE + (level - 1) * 20f) * 0.75f;
        Bullet b = new Bullet(ex, ey, dx * spd, dy * spd, SISTER_SPECIAL_DMG, SISTER_SPECIAL_SIZE, -7);
        b.spinSpeed = 360f;
        enemyBullets.add(b);
    }

    private void spawnSongWordBullet() {
        float sx = enemy.x + ENEMY_W / 2f - 20f;
        float sy = enemy.y + ENEMY_H + 5f;
        float dir = (player.x + PLAYER_W / 2f) >= (enemy.x + ENEMY_W / 2f) ? 1f : -1f;
        Bullet b = new Bullet(sx, sy, dir * SONG_WORD_SPEED, 0f, SONG_WORD_DAMAGE, 40, -8);
        b.word = SONG_WORDS[songWordIndex];
        b.phase = songWordIndex * 0.9f;
        enemyBullets.add(b);
    }

    private void handleSisterKill() {
        if (sister1Alive || sister2Alive) return; // czekaj aż obie martwe
        score += 50 * level;
        int earned = Math.min(100, MONEY_PER_LEVEL * level * moneyMultiplier);
        game.money += earned;
        moneyPopupAmount = earned;
        moneyPopupTimer = 2f;
        game.saveData();
        // Relic drop for level 3 (sisters)
        relikwiaDropActive = true;
        relikwiaDropIndex = 2; // relikwia_3
        relikwiaDropX = WORLD_W / 2f - RELIC_SIZE / 2f;
        relikwiaDropY = GROUND_Y + 10f;
        portalActive = true;
        dropActive = true;
        dropX = WORLD_W / 2f - DROP_W / 2f;
        dropY = GROUND_Y;
        enemyBullets.clear();
    }

    private void drawSisterIntroOverlay() {
        float cx = camera.position.x, cy = camera.position.y;
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, sisterIntroFadeAlpha);
        shapeRenderer.rect(cx - W / 2f, cy - H / 2f, W, H);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        if (sisterIntroTextAlpha > 0f) {
            game.batch.setProjectionMatrix(camera.combined);
            game.batch.begin();
            float lh = 26f;
            float ty = cy + 195;
            game.font.getData().setScale(0.75f);
            game.font.setColor(1f, 0.12f, 0.12f, sisterIntroTextAlpha);
            game.font.draw(game.batch, "Gdy kurz wiekow opadl z okladki,", cx - 230, ty);
            game.font.draw(game.batch, "atrament przebudzil sie niczym krew w starych zylach...", cx - 315, ty - lh);
            game.font.getData().setScale(0.7f);
            game.font.setColor(0.9f, 0.05f, 0.05f, sisterIntroTextAlpha);
            game.font.draw(game.batch, "\"Nie czytaj na glos.", cx - 135, ty - lh * 2.6f);
            game.font.draw(game.batch, " Nie wypowiadaj ich imion.", cx - 175, ty - lh * 3.5f);
            game.font.draw(game.batch, " Nie otwieraj tomu po raz drugi.\"", cx - 230, ty - lh * 4.4f);
            game.font.getData().setScale(0.7f);
            game.font.setColor(1f, 0.12f, 0.12f, sisterIntroTextAlpha);
            game.font.draw(game.batch, "W ciszy miedzy stronami zapisano przestroge", cx - 285, ty - lh * 6.0f);
            game.font.draw(game.batch, "przed tymi, ktore strzega slow jak grobow.", cx - 280, ty - lh * 7.0f);
            game.font.draw(game.batch, "Mowi sie, ze nie umarly.", cx - 160, ty - lh * 8.0f);
            game.font.draw(game.batch, "One jedynie czekaja... miedzy rozdzialami.", cx - 278, ty - lh * 9.0f);
            game.font.getData().setScale(0.75f);
            game.font.setColor(1f, 0.22f, 0.1f, sisterIntroTextAlpha);
            game.font.draw(game.batch, "Otworzyles ksiege. Atrament przyjal twoje imie.", cx - 310, ty - lh * 10.5f);
            game.font.getData().setScale(1.0f);
            game.font.setColor(1f, 0f, 0f, sisterIntroTextAlpha);
            game.font.draw(game.batch, "Rozdzial zostal rozpoczety.", cx - 215, ty - lh * 12.2f);
            game.font.getData().setScale(1f);
            game.font.setColor(Color.WHITE);
            game.batch.end();
        }
    }

    private static class MiniEnemy {
        final Rectangle rect;
        int hp;

        MiniEnemy(float x, float y, int hp) {
            rect = new Rectangle(x, y, MINI_W, MINI_H);
            this.hp = hp;
        }
    }

    private static class PrayerWord {
        float x, y, vx, vy;
        final String text;

        PrayerWord(float x, float y, float vx, float vy, String text) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.text = text;
        }
    }

    private static class HealPlus {
        float x, y, vy, alpha;
        HealPlus(float x, float y) {
            this.x = x;
            this.y = y;
            this.vy = 90f + MathUtils.random(50f);
            this.alpha = 1f;
        }
    }

    private static class Bullet {
        final Vector2 pos;
        final Vector2 vel;
        final Vector2 startPos;
        final int damage;
        final int size;
        final int weaponIndex; // -1 for enemy bullets, -3 for special
        boolean curved; // whether bullet already changed direction
        boolean curveUp; // true = curve up, false = curve down
        boolean bouncing; // rabbit bullets bounce off ground/platforms
        int bounceCount;  // how many times it has bounced
        float rotation;   // current angle in degrees (for spinning bullets)
        float spinSpeed;  // degrees per second (0 = no spin)
        float age;        // elapsed time in seconds
        float phase;      // sine wave phase offset
        String word;      // text label for song-word bullets (weaponIndex == -8)

        Bullet(float x, float y, float vx, float vy, int damage, int size, int weaponIndex) {
            pos = new Vector2(x, y);
            startPos = new Vector2(x, y);
            vel = new Vector2(vx, vy);
            this.damage = damage;
            this.size = size;
            this.weaponIndex = weaponIndex;
        }
    }
}