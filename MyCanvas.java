
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.io.BufferedWriter;
import java.util.Random;
import java.util.ArrayList;

public class MyCanvas extends Canvas {

    private ObjectSet objectset;
    private KeyInput keyinput;
    private Random random;
    private Title title;
    private Background background;

    // ターン管理用
    private TurnManager turnmanager;

    private Image imgBuf;
    private Graphics gBuf;
    private boolean gameover;
    private int counter;

    // 選択部分(インベントリのハイライト)
    private int selectItem;

    // ルールベースプレイヤー
    private RuleBasePlayer rbp;
    private RuleBasePlayer_bu rbp_bu;

    private int deathCount = 0;
    private int clearCount = 0;
    private int gameCounter = 0;

    private long start; // 処理開始前の時間を保持する
    private long end; // 実行時間を保持する

    private static final boolean DATA_COLLECTED = false; // true:データ収集,false:通常の実験
    private static final int BOUND_REACHCOUNT = 100; // 各階層の最低到達回数

    // 実行回数
    public static final int TRYNUM = 200;

    // 初期配置，false:ランダム配置，true:配置をいじる
    public static final boolean DEBUG_INIT = true;

    // 現在の階層
    public static int floorNumber = 0;

    // フレームのサイズ
    private int frameSizeX;
    private int frameSizeY;

    // 初回判定
    static boolean startFlag;

    // マップの表示開始フラグ
    private boolean startDrawmapFlag;

    // リザルト表示判定
    private boolean resultFlag;

    // シーン管理変数
    // 0:タイトル画面, 1:ゲームのメイン画面
    private int scene;

    public static final int SCENE_TITLE = 0;
    public static final int SCENE_GAMEMAIN = 1;
    public static final int SCENE_RESULT = 2;
    public static final int SCENE_INV = 3;
    public static final int SCENE_PAUSE = 4;

    // 押されている
    public static final int SHOT_PRESSED = 1;
    // 今押されたばかり
    public static final int SHOT_DOWN = 2;
    int shotSPkey_state;

    // ダンジョンのマップサイズ(グリッド)
    public static final int MAPGRIDSIZE_X = 50;
    public static final int MAPGRIDSIZE_Y = 30;

    // ゲーム画面の視界(グリッド)
    // 奇数のみ(3以上15以下)
    public static final int SCREENGRIDSIZE_X = 9;
    public static final int SCREENGRIDSIZE_Y = 9;

    // おおよそのマップサイズ
    // 32(mcサイズ) * 15(最大) = 480
    public static final int SCREENSIZE_X = 480;
    public static final int SCREENSIZE_Y = 480;

    // グリッド数に応じた適切な倍率のimgサイズ
    public static final int MAPCHIP_MAGX = MyCanvas.SCREENSIZE_X / MyCanvas.SCREENGRIDSIZE_X;
    public static final int MAPCHIP_MAGY = MyCanvas.SCREENSIZE_Y / MyCanvas.SCREENGRIDSIZE_Y;

    // マップチップ1枚のサイズ
    public static final int MAPCHIP_X = 32;
    public static final int MAPCHIP_Y = 32;

    public static int useItemPotion = 0;
    public static int useItemFood = 0;
    public static int useItemLStaff = 0;
    public static int useItemWStaff = 0;

    public static int[] getItemPotion = {0, 0, 0, 0};
    public static int[] getItemFood = {0, 0, 0, 0};
    public static int[] getItemLStaff = {0, 0, 0, 0};
    public static int[] getItemWstaff = {0, 0, 0, 0};

    public static int[] invItem = {0, 0, 0, 0};

    // ダンジョンの最下層
    // この数字の階に到達したらクリア
    public static final int TOPFLOOR = 4;

    // 0:死亡した階数，1:餓死の回数
    private int[] deathFloor = new int[TOPFLOOR];

    private int[] lvFloor = new int[TOPFLOOR];
    private int[] lvFloorNum = new int[TOPFLOOR];

    // 餓死回数
    public static int gasi = 0;
    public static int[] gasif = new int[4];

    private static int histCount = 0;

    private Info info;

    // 10未満：表示なし
    // 10以上：表示あり
    private int drawlevel;

    private LData[] ld; // 1->2, 2->3, 3->4における状況

    private LData_bt ld_bt;
    private ArrayList<LData_bt> LDBT_List; // 1ゲーム終了までの戦闘データ

    private int[] reachCount; // 各階層への到達回数
    private int[] dataCount; // 各階層の取得データ数

    public String fileName;

    private static int GAMEMODE;
    
    public RuleBasePlayer getrbp() {
        return rbp;
    }

    // コンストラクタ
    public MyCanvas(int x, int y, int lv, int gmode, String fn) {
        GAMEMODE = gmode; // 0:人間,1:AIプレイヤ,2:高速周回
        fileName = fn;
        
        random = new Random();		// 乱数

        keyinput = new KeyInput();
        addKeyListener(keyinput);	// キーリスナ
        setFocusable(true);			// フォーカス
        title = new Title();
        background = new Background(x, y);

        turnmanager = new TurnManager(background, MAPGRIDSIZE_X, MAPGRIDSIZE_Y);

        frameSizeX = x;
        frameSizeY = y;

        floorNumber = 0;

        selectItem = 0;

        // 初期化
        for (int i = 0; i < TOPFLOOR; i++) {
            deathFloor[i] = 0;
            gasif[i] = 0;
            lvFloor[i] = 0;
            lvFloorNum[i] = 0;
            getItemPotion[i] = 0;
            getItemFood[i] = 0;
            getItemLStaff[i] = 0;
            getItemWstaff[i] = 0;
        }

        info = new Info(MAPGRIDSIZE_X, MAPGRIDSIZE_Y);

        drawlevel = lv;

        LDBT_List = new ArrayList<LData_bt>();
        LData_bt ld_bt_init = new LData_bt();
        ld_bt_init.csvFileSetLabel(fileName);

        ld = new LData[TOPFLOOR];
        for (int i = 0; i < ld.length; i++) {
            ld[i] = new LData();
        }
        ld[0].csvfileSetLabel(fileName); // csvファイルへのラベル付けのため

        reachCount = new int[TOPFLOOR];
        dataCount = new int[TOPFLOOR];
        for (int flr = 0; flr < TOPFLOOR; flr++) {
            reachCount[flr] = 0;
            dataCount[flr] = 0;
        }

        // 計測開始
        start = System.currentTimeMillis();
    }

    // 初期化
    public void init() {
        objectset = new ObjectSet(background);
        turnmanager = new TurnManager(background, MAPGRIDSIZE_X, MAPGRIDSIZE_Y);

        rbp = new RuleBasePlayer();
        rbp_bu = new RuleBasePlayer_bu();

        //シーンはタイトル画面
        //scene = SCENE_TITLE;
        scene = SCENE_GAMEMAIN;

        gameover = false;

        startFlag = false;

        resultFlag = false;

        floorNumber = 0;

        counter = 0;

        // submap-update
        //Game.canvm.init(this, 10);
        startDrawmapFlag = false;

        for (int i = 0; i < ld.length; i++) {
            ld[i] = new LData();
        }

        ld_bt = new LData_bt();
        LDBT_List.clear();
    }

    // 描画
    public void paint(Graphics g) {
        // ちらつき防止 -> オフスクリーンバッファ使用
        // オフスクリーンバッファの内容を自分にコピー
        g.drawImage(imgBuf, 0, 0, this);
    }

    public void gBufClear(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, frameSizeX, frameSizeY);
    }

    public void run() {
        //オフスクリーンバッファ作成
        imgBuf = createImage(frameSizeX, frameSizeY);
        gBuf = imgBuf.getGraphics();

        // counterにより経過管理
        for (counter = 0;; counter++) {
            shotSPkey_state = keyinput.checkSpaceShotKey();

            // バッファをクリア
            gBufClear(gBuf);

            //シーン遷移用の変数で分岐
            switch (scene) {
                //タイトル画面
                case 0:
                    //titleScene();
                    break;

                //ゲームのメイン画面
                case 1:
                    gameScene();
                    break;

                // リザルト表示
                case 2:
                    resultScene();
                    break;

                // ゲーム中インベントリを開いている画面
                case 3:
                    inventoryScene();
                    break;
                    
                case 4:
                    pauseScene();
                    break;
            }

            if(drawlevel >= 10) drawMap(); // マップの描画

            repaint(); // 再描画

            if (drawlevel >= 10) {
                try {
                    if(GAMEMODE == 0)       Thread.sleep(10); // ループのウェイト
                    else if(GAMEMODE == 1)  Thread.sleep(500);
                    else if(GAMEMODE == 2)  Thread.sleep(0);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public void drawMap() {
        // マップ描画
        if (startFlag == true) {
            int biasX = 0; // x方向のバイアス，表示領域調整用
            int biasY = 600; // y方向のバイアス，表示領域調整用

            // プレイヤーの持つマップと比較し，描画する
            for (int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++) {
                for (int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++) {
                    // プレイヤーの場合
                    if (objectset.getpmap(x, y) == true && background.getMapUnit(x, y) == 6) {
                        gBuf.setColor(Color.ORANGE);
                        gBuf.fillRect(x * 8 + biasX, y * 8 + biasY, 8, 8);
                        continue;
                    }
                    // 敵
                    if (objectset.getpmap(x, y) == true && background.getMapUnit(x, y) == 3) {
                        // 視界にない(部屋外，周囲視界になし)とき，表示しない

                        // もし，同じ部屋の中ではなく，プレイヤの周囲視界内に敵が存在しないとき
                        // 探索済みのマップ部分だとしても，表示を行わない
                        if (objectset.getpCurmap(x, y) == true) {
                            gBuf.setColor(Color.red);
                            gBuf.fillRect(x * 8 + biasX, y * 8 + biasY, 8, 8);
                            continue;
                        }

                        // 常に見える状態にできる
                        /*
							gBuf.setColor(Color.red);
							gBuf.fillRect(x*8, y*8, 8, 8);
							continue;
                         */
                    }
                    // アイテム
                    if (objectset.getpmap(x, y) == true && background.getMapObject(x, y) == 4) {
                        gBuf.setColor(Color.green);
                        gBuf.fillRect(x * 8 + biasX, y * 8 + biasY, 8, 8);
                        continue;
                    }
                    // 階段
                    if (objectset.getpmap(x, y) == true && background.getMapObject(x, y) == 5) {
                        gBuf.setColor(Color.gray);
                        gBuf.fillRect(x * 8 + biasX, y * 8 + biasY, 8, 8);
                        continue;
                    }
                    if (false) // ルールベース時の目標点を表示 
                    {
                        Point tg = rbp.getTarget();
                        if (tg.x == x && tg.y == y) {
                            gBuf.setColor(Color.yellow);
                            gBuf.fillRect(x * 8 + 1, y * 8 + 1, 6, 6);
                            continue;
                        }
                    }
                    // 通行可能な探索済みの部分
                    if (objectset.getpmap(x, y) == true && background.getMap(x, y) == 0) {
                        gBuf.setColor(Color.white);
                        gBuf.fillRect(x * 8 + biasX, y * 8 + biasY, 8, 8);
                        continue;
                    }
                }
            }
        }
    }

    // オーバーライド
    // クリア防止のため
    public void update(Graphics g) {
        paint(g);
    }

    // 結果出力画面の処理
    void resultScene() {
        // マップ描画(バッファをクリア)
        // プレイヤーの座標
        background.drawGameBG(gBuf, objectset.player);
        // マップに対してグリッド線の表示
        background.drawGridBG(gBuf);

        if (resultFlag == false) {
            Logger.appendLog("*-- result --*");
            resultFlag = true;
        }

        //スペースキーが押された
        if (shotSPkey_state == SHOT_DOWN) {
            // ゲームの再初期化
            init();
        }
    }

    // 一時停止画面の処理
    void pauseScene() {
        if (drawlevel >= 10) {
            // 描画タイミングをできるだけ近づける
            // マップ描画(バッファをクリア)
            // プレイヤーの座標
            background.drawGameBG(gBuf, objectset.player);
            // マップに対してグリッド線の表示
            background.drawGridBG(gBuf);
            // ゲームオブジェクトの一括描画処理
            objectset.drawAll(gBuf);
            // ステータスバー描画
            drawStatusBar(gBuf, objectset.player);
            if (objectset.isGameover()) {
                // ゲームオーバー文字を表示
                title.drawGameover(gBuf);
            } else if (floorNumber == TOPFLOOR) {
                // ゲームクリア文字表示
                title.drawClear(gBuf);
            }
        }

        // 一時停止解除
        if (keyinput.checkAShotKey() == SHOT_PRESSED) {
            scene = SCENE_GAMEMAIN;
        }
    }
    
    // インベントリを開いた状態
    void inventoryScene() {
        // 現在の所持アイテム個数
        int itemnum = objectset.player.inventory.getInvItemNum();
        // インベントリ：1ページ10個まで
        int oneItemSize = 523 / 10;
        // 現在のターン数
        int crrentTurn = turnmanager.getTurn();

        // 所持アイテム数が1個以上あるとき，
        // ターンの経過管理を行う
        if (itemnum > 0) {
            // TurnManagerを用いてターン管理を行う
            // インベントリ画面とゲーム画面では引数が異なる
            // アイテムの使用，投擲，配置によるターン経過を管理する
            turnmanager.turnCount(objectset, keyinput, selectItem, background);

            // 所持アイテム数の更新
            itemnum = objectset.player.inventory.getInvItemNum();
        }

        // マップ描画(バッファをクリア)
        // プレイヤーの座標
        background.drawGameBG(gBuf, objectset.player);

        // マップに対してグリッド線の表示
        background.drawGridBG(gBuf);

        // ゲームオブジェクトの一括描画処理
        objectset.drawAll(gBuf);

        // ステータスバー描画
        drawStatusBar(gBuf, objectset.player);

        // インベントリのbg表示
        gBuf.setColor(Color.white);
        gBuf.fillRect(frameSizeX / 2, 0, frameSizeX / 2, frameSizeY);

        // frameSizeY 523がちょうど？
        //gBuf.setColor(Color.red);
        //gBuf.fillRect(frameSizeX/2, 0, frameSizeX/2, 1);
        //System.out.println(MAPCHIP_MAGY / 2 + SCREENSIZE_Y);
        // インベントリの中身の表示
        for (int i = 0; i < itemnum; i++) {
            //System.out.println(objectset.player.inventory.getInvItemNum());
            if (i == selectItem) {
                gBuf.setColor(Color.ORANGE);
                gBuf.fillRect(frameSizeX / 2, i * oneItemSize, frameSizeX / 2, oneItemSize - 1);
            } else {
                gBuf.setColor(Color.LIGHT_GRAY);
                gBuf.fillRect(frameSizeX / 2, i * oneItemSize, frameSizeX / 2, oneItemSize - 1);
            }

            String iName = objectset.player.inventory.getInvItemName(i);
            int iUCount = objectset.player.inventory.getInvItemUsageCount(i);
            gBuf.setColor(Color.red);
            gBuf.setFont(new Font("Alial", Font.BOLD, 20));
            gBuf.drawString(iName + " (" + iUCount + ")", frameSizeX / 2 + 10, i * oneItemSize + oneItemSize / 2);
        }

        // 表示されたインベントリ内の移動
        if (keyinput.checkDownShotKey() == SHOT_DOWN && selectItem < itemnum - 1) {
            selectItem++;
        }
        if (keyinput.checkUpShotKey() == SHOT_DOWN && selectItem > 0) {
            selectItem--;
        }
        /*
		if (keyinput.checkLeftShotKey() == SHOT_DOWN && selectItem > 10)
		{
			selectItem-=10;
		}
		if (keyinput.checkRightShotKey() == SHOT_DOWN && selectItem <= 10 && itemnum > 10)
		{
			selectItem+=10;
			if(selectItem > itemnum)
			{
				selectItem = itemnum;
			}
		}
         */

        // eが押された，アイテムの使用・投擲によりターンが進む
        if (keyinput.checkEShotKey() == SHOT_DOWN || crrentTurn != turnmanager.getTurn()) {
            // インベントリ画面が閉じるたびに選択アイテムは先頭に戻る
            selectItem = 0;
            // メイン画面に行く
            scene = SCENE_GAMEMAIN;
        }
    }

    // ゲーム画面の処理
    void gameScene() {
        // ゲームオーバー判定
        if (objectset.isGameover()) {
            // データをセット
            ld_bt.setLData_bt(gameCounter, floorNumber, objectset.player, rbp.getStairRoomID(), objectset.getpmap());
            // ラベルをセット
            ld_bt.setLabel_bt(false);
            // 出力
            ld_bt.sysoutput();
            // リストに追加
            LDBT_List.add(ld_bt);
            // 初期化
            ld_bt = new LData_bt();

            //System.out.println("set-label");
            // 勝敗ラベルの設定
            for (int i = 0; i < LDBT_List.size(); i++) {
                dataCount[LDBT_List.get(i).fl]++;
                LDBT_List.get(i).dcountnum = dataCount[LDBT_List.get(i).fl];
                LDBT_List.get(i).rcountnum = reachCount[LDBT_List.get(i).fl];
                LDBT_List.get(i).setLabel(false);
                LDBT_List.get(i).sysoutput();
                LDBT_List.get(i).csvFileOutput(new String("data"));
            }

            lvFloor[floorNumber] += objectset.player.level;
            lvFloorNum[floorNumber]++;

            // 敗北時の状態反映
            ld[floorNumber].setLData(floorNumber, objectset.player);
            
            for (int i = 0; i < ld.length; i++) {
                if (i < floorNumber - 1) {
                    ld[i].setNextFlabel(true);
                } else {
                    ld[i].setNextFlabel(false);
                }

                ld[i].setLabel(false);
                ld[i].sysoutput();

                if (ld[i].updateFlag == true) {
                    ld[i].csvFileOutput(new String("data"));
                }
            }

//                        for(int fnum = 0; fnum < TOPFLOOR; fnum++)
//                        {
//                            System.out.println("f" + fnum + ":" + turnmanager.getTurn(fnum));
//                        }
//                        System.out.println("sum:" + turnmanager.getTurn());
            deathCount++;
            deathFloor[floorNumber]++;
            gameCounter++;
            System.out.println("win:" + clearCount + "/" + gameCounter);
            init();

            if (shotSPkey_state == SHOT_DOWN) {
                //メイン画面に行く
                scene = SCENE_RESULT;
            }
        } // ゲームクリア判定
        else if (floorNumber == TOPFLOOR) {
            // 直前の階層におけるフラグを更新
            for (int i = 0; i < LDBT_List.size(); i++) {
                if (LDBT_List.get(i).fl == floorNumber - 1) {
                    // フラグを更新
                    LDBT_List.get(i).setLabel_cf(true);
                }
            }

            // 前前の階層におけるフラグを更新
            for (int i = 0; i < LDBT_List.size(); i++) {
                if (LDBT_List.get(i).fl == floorNumber - 2) {
                    // フラグを更新
                    LDBT_List.get(i).setLabel_nf(true);
                }
            }

            //System.out.println("set-label");
            // 勝敗ラベルの設定
            for (int i = 0; i < LDBT_List.size(); i++) {
                dataCount[LDBT_List.get(i).fl]++;
                LDBT_List.get(i).dcountnum = dataCount[LDBT_List.get(i).fl];
                LDBT_List.get(i).rcountnum = reachCount[LDBT_List.get(i).fl];
                LDBT_List.get(i).setLabel(true);
                LDBT_List.get(i).sysoutput();
                LDBT_List.get(i).csvFileOutput(new String("data"));
            }

            lvFloor[floorNumber - 1] += objectset.player.level;
            lvFloorNum[floorNumber - 1]++;

            // クリア時の状態反映
            ld[floorNumber - 1].setLData(floorNumber - 1, objectset.player);
            
            //System.out.println("down the stairs");
            for (int i = 0; i < ld.length; i++) {
                if (i < floorNumber - 1) {
                    ld[i].setNextFlabel(true);
                } else {
                    ld[i].setNextFlabel(false);
                }

                ld[i].setLabel(true);
                ld[i].sysoutput();

                if (ld[i].updateFlag == true) {
                    ld[i].csvFileOutput(new String("data"));
                }
            }

            clearCount++;
            gameCounter++;
            System.out.println("win:" + clearCount + "/" + gameCounter);
            init();

            if (shotSPkey_state == SHOT_DOWN) {
                //メイン画面に行く
                scene = SCENE_RESULT;
            }
        } else {
            // ゲーム開始直後の場合，オブジェクトを設置する
            if (startFlag == false) {
                if (floorNumber == 0) {
                    System.out.println("num:" + gameCounter);
                }

                // フロア数が1より大きいとき
                if (floorNumber >= 1) {
                    lvFloor[floorNumber - 1] += objectset.player.level;
                    lvFloorNum[floorNumber - 1]++;
                }

                // 階層が1より大きいとき，現在のプレイヤ情報を格納
                if (floorNumber >= 1) {
                    ld[floorNumber - 1].setLData(floorNumber - 1, objectset.player);
                    
                    // 獲得アイテム数のために
                    ld[floorNumber].setLData(floorNumber, objectset.player);
                }

                // フロア数が1より大きいとき，直前の階層におけるフラグを更新
                if (floorNumber >= 1) {
                    for (int i = 0; i < LDBT_List.size(); i++) {
                        if (LDBT_List.get(i).fl == floorNumber - 1) {
                            // フラグを更新
                            LDBT_List.get(i).setLabel_cf(true);
                        }
                    }
                }

                // フロア数が2より大きいとき，前前の階層におけるフラグを更新
                if (floorNumber >= 2) {
                    for (int i = 0; i < LDBT_List.size(); i++) {
                        if (LDBT_List.get(i).fl == floorNumber - 2) {
                            // フラグを更新
                            LDBT_List.get(i).setLabel_nf(true);
                        }
                    }
                }

                // 到達回数のカウント
                reachCount[floorNumber]++;

                Logger.appendLog(floorNumber + "F");
                Logger.appendLog("\n" + "[turn : " + turnmanager.getTurn() + "]");

                if (DEBUG_INIT == false) {
                    // マップの情報の更新
                    background.mapUpdate();

                    // プレイヤーの配置
                    // プレイヤーの持つマップ情報の初期化・更新を含む
                    objectset.setObjectRand(new String("player"));

                    // オブジェクトの初期化
                    objectset.initObjectsetExceptPlayer();

                    // オブジェクトの配置
                    // 敵
                    for (int i = 0; i < ObjectSet.ENEMY_MAX; i++) {
                        objectset.setObjectRand(new String("enemy"));
                    }

                    // (x, y, アイテム種類)
                    // 1/2の確率で4個
                    int randItemNum = ObjectSet.ITEM_MAX - 1;
                    if (random.nextInt(2) == 0) {
                        if (random.nextInt(2) == 0) {
                            randItemNum--;
                        } else {
                            randItemNum++;
                        }
                    }
                    for (int i = 0; i < randItemNum; i++) {
                        objectset.setObjectRand(new String("item"));
                    }

                    // 階段
                    // 通路の直前，部屋に入るとすぐのグリッドに生成されないように調整
                    objectset.setObjectRand(new String("stair"));

                    // マップ視野の描画を開始する
                    //CanvasMap.startDrawmap();
                    startDrawmapFlag = true;

                    startFlag = true;

                    // ログに現在の状態を出力，0F以外
                    // ゲーム回数，階層数，プレイヤーの各アイテム数
                } else {
                    // マップの情報の更新
                    // true:通路ランダム削除あり, false:通路ランダム削除なし
                    background.mapUpdate("src/mat/d3.txt", false);

                    // プレイヤーの配置
                    // プレイヤーの持つマップ情報の初期化・更新を含む
                    objectset.setObject(new String("player"), -1, 19, 5);

                    //floorNumber = 0;
                    
                    // プレイヤー情報の変更
                    //if (floorNumber == 0) { // このif必要？
                        objectset.player.addExp(50); // レベルの調整
                        objectset.player.hp = objectset.player.maxHp;
                        objectset.player.satiety = objectset.player.maxSatiety;
                        objectset.player.inventory.addItem(4); // wst追加
                        objectset.player.inventory.addItem(4);
                        //objectset.player.inventory.addItem(4);
                        //objectset.player.inventory.addItem(4);
                    //}

                    // オブジェクトの初期化
                    objectset.initObjectsetExceptPlayer();

                    // オブジェクトの配置
                    // 敵
                    objectset.setObject(new String("enemy"), 0, 17, 5);
                    objectset.setObject(new String("enemy"), 0, 17, 6);
                    //ObjectSet.enemy[1].hp = 45;
                    //objectset.setObject(new String("enemy"), 3, 11, 8);
                    //objectset.setObject(new String("enemy"), 3, 11, 9);

                    // 階段
                    // 通路の直前，部屋に入るとすぐのグリッドに生成されないように調整
                    objectset.setObject(new String("stair"), -1, 15, 22);

                    for (int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++) {
                        for (int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++) {
                            //objectset.setpmap(x, y, true);
                            //objectset.setpCurmap(x, y, true);
                        }
                    }
                    
                    // マップ視野の描画を開始する
                    //CanvasMap.startDrawmap();
                    startDrawmapFlag = true;

                    startFlag = true;
                    
                    if(GAMEMODE == 1) scene = SCENE_PAUSE;
                    
                    // ログに現在の状態を出力，0F以外
                    // ゲーム回数，階層数，プレイヤーの各アイテム数
                }
            } else {
                // ログのデータ収集における情報更新，毎ターンチェック
                ld[floorNumber].setLData_everyTurn(floorNumber, objectset.player, objectset.getpmap());


                info.stairpos = (rbp.getStairRoomID() != -1) ? true : false; // stairRoomID != -1 -> 階段発見済み

                // TurnManagerを用いてターン管理を行う
                // 移動・攻撃によるターン経過を管理する
                if(GAMEMODE == 0){
                    turnmanager.turnCount(objectset, keyinput, background);
                }
                else{
                    // エージェント用
                    //turnmanager.turnCount(objectset, background, rbp_bu);
                    turnmanager.turnCount(objectset, background, rbp);
                }

                // インベントリ
                if (keyinput.checkEShotKey() == SHOT_DOWN) {
                    scene = SCENE_INV;
                }

                /*-- 以下デバッグ用 --*/
                /*--------------------*/
                
                if(GAMEMODE == 1) scene = SCENE_PAUSE;
                
                // ダンジョン探索済みとする
                if (keyinput.checkCShotKey() == SHOT_DOWN) {
                    for (int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++) {
                        for (int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++) {
                            objectset.setpmap(x, y, true);
                            objectset.setpCurmap(x, y, true);
                        }
                    }
                }
                // 一時停止
                if (keyinput.checkAShotKey() == SHOT_DOWN) {
                    scene = SCENE_PAUSE;
                }
                // ゲームに敗北する
                if (keyinput.checkWShotKey() == SHOT_DOWN) {
                    objectset.player.active = false;
                }
                // 描画のonoff，sleepタイムの調節
                // ミニマップはどうする？
                // スクロール部分は？
                if (keyinput.checkTShotKey() == SHOT_DOWN) {
                    if (drawlevel >= 10) {
                        setDrawLevel(-10);
                    } else {
                        setDrawLevel(10);
                    }
                }
                // ミニマップ，ターゲットの描画on

                // ミニマップ，ターゲットの描画off
                /*--------------------*/
                /*--------------------*/
                // ld_btの追加
                // 直前の行動が戦闘中 -> 現在の行動が戦闘以外
                if (rbp.getBattleEnd() == true && floorNumber != TOPFLOOR) {
                    // データをセット
                    ld_bt.setLData_bt(gameCounter, floorNumber, objectset.player, rbp.getStairRoomID(), objectset.getpmap());
                    // ラベルをセット
                    ld_bt.setLabel_bt(true);
                    // 出力
                    ld_bt.sysoutput();
                    // リストに追加
                    LDBT_List.add(ld_bt);
                    // 初期化
                    ld_bt = new LData_bt();
                }
            }
        }

        if (drawlevel >= 10) {
            // 描画タイミングをできるだけ近づける
            // マップ描画(バッファをクリア)
            // プレイヤーの座標
            background.drawGameBG(gBuf, objectset.player);
            // マップに対してグリッド線の表示
            background.drawGridBG(gBuf);
            // ゲームオブジェクトの一括描画処理
            objectset.drawAll(gBuf);
            // ステータスバー描画
            drawStatusBar(gBuf, objectset.player);
            if (objectset.isGameover()) {
                // ゲームオーバー文字を表示
                title.drawGameover(gBuf);
            } else if (floorNumber == TOPFLOOR) {
                // ゲームクリア文字表示
                title.drawClear(gBuf);
            }
        }

        ///*
        // 1ゲーム毎に終了時
        if (histCount != gameCounter) {
            // ログに追加がここ？初期化のタイミングとの兼ね合い

            // logの初期化
            Logger.initLog();

            histCount = gameCounter;
        }

        // 既定のゲーム回数終了時
        if (gameCounter == TRYNUM && DATA_COLLECTED == false) {
            scene = SCENE_TITLE;

            // 結果をstrに
            StringBuilder restr = new StringBuilder();
            int arriveNum = TRYNUM;
            int[] arriveArr = new int[]{arriveNum, 0, 0, 0};

            restr.append("試行回数" + TRYNUM + System.getProperty("line.separator"));
            // 計測終了
            end = System.currentTimeMillis();
            restr.append(((double) (end - start) / 1000) + "sec" + System.getProperty("line.separator"));
            restr.append("clear:" + clearCount + System.getProperty("line.separator"));
            restr.append("death:" + deathCount + System.getProperty("line.separator"));
            for (int i = 0; i < TOPFLOOR; i++) {
                if (i >= 1) {
                    arriveNum -= deathFloor[i - 1];
                    arriveArr[i] = arriveNum;
                }
                restr.append("deathFloor" + i + ":" + deathFloor[i] + "(" + gasif[i] + ")" + System.getProperty("line.separator"));
            }
            restr.append("gasi:" + gasi + System.getProperty("line.separator"));
            restr.append("useFood  :" + useItemFood + System.getProperty("line.separator"));
            restr.append("usePotion:" + useItemPotion + System.getProperty("line.separator"));
            restr.append("useLStaff:" + useItemLStaff + System.getProperty("line.separator"));
            restr.append("useWStaff:" + useItemWStaff + System.getProperty("line.separator"));
            for (int i = 0; i < TOPFLOOR; i++) {
                restr.append("Level-ave" + i + ":" + (lvFloor[i] / lvFloorNum[i]) + System.getProperty("line.separator"));
            }
            for (int i = 0; i < TOPFLOOR; i++) {
                restr.append(i + "f(arrive:" + arriveArr[i] + ")" + System.getProperty("line.separator"));
                restr.append("getFood  :" + getItemFood[i] + System.getProperty("line.separator"));
                restr.append("getPotion:" + getItemPotion[i] + System.getProperty("line.separator"));
                restr.append("getLStaff:" + getItemLStaff[i] + System.getProperty("line.separator"));
                restr.append("getWStaff:" + getItemWstaff[i] + System.getProperty("line.separator"));
                int sum = getItemFood[i] + getItemPotion[i] + getItemLStaff[i] + getItemWstaff[i];
                restr.append("sum:" + sum + System.getProperty("line.separator"));
            }

            System.out.println(new String(restr));

            //
            Logger.OutputFileLog(new String(fileName + ".txt"), new String(restr));

            System.exit(0);
        }
        //*/

        // 各階層への到達数が上限以上 -> 終了
        // かつ，ゲームが一区切りついたとき
        if (isReachCount() == true && startFlag == false && floorNumber == 0 && DATA_COLLECTED == true) {
            scene = SCENE_TITLE;

            // 結果をstrに
            StringBuilder restr = new StringBuilder();
            int arriveNum = TRYNUM;
            int[] arriveArr = new int[]{arriveNum, 0, 0, 0};

            restr.append("試行回数" + gameCounter + System.getProperty("line.separator"));
            // 計測終了
            end = System.currentTimeMillis();
            restr.append(((double) (end - start) / 1000) + "sec" + System.getProperty("line.separator"));
            restr.append("clear:" + clearCount + System.getProperty("line.separator"));
            restr.append("death:" + deathCount + System.getProperty("line.separator"));
            for (int i = 0; i < TOPFLOOR; i++) {
                if (i >= 1) {
                    arriveNum -= deathFloor[i - 1];
                    arriveArr[i] = arriveNum;
                }
                restr.append("deathFloor" + i + ":" + deathFloor[i] + "(" + gasif[i] + ")" + System.getProperty("line.separator"));
            }
            restr.append("gasi:" + gasi + System.getProperty("line.separator"));
            restr.append("useFood  :" + useItemFood + System.getProperty("line.separator"));
            restr.append("usePotion:" + useItemPotion + System.getProperty("line.separator"));
            restr.append("useLStaff:" + useItemLStaff + System.getProperty("line.separator"));
            restr.append("useWStaff:" + useItemWStaff + System.getProperty("line.separator"));
            for (int i = 0; i < TOPFLOOR; i++) {
                restr.append("Level-ave" + i + ":" + (lvFloor[i] / lvFloorNum[i]) + System.getProperty("line.separator"));
            }
            for (int i = 0; i < TOPFLOOR; i++) {
                restr.append(i + "f(arrive:" + arriveArr[i] + ")" + System.getProperty("line.separator"));
                restr.append("getFood  :" + getItemFood[i] + System.getProperty("line.separator"));
                restr.append("getPotion:" + getItemPotion[i] + System.getProperty("line.separator"));
                restr.append("getLStaff:" + getItemLStaff[i] + System.getProperty("line.separator"));
                restr.append("getWStaff:" + getItemWstaff[i] + System.getProperty("line.separator"));
                int sum = getItemFood[i] + getItemPotion[i] + getItemLStaff[i] + getItemWstaff[i];
                restr.append("sum:" + sum + System.getProperty("line.separator"));
            }
            restr.append("reachCount" + System.getProperty("line.separator"));
            for (int i = 0; i < TOPFLOOR; i++) {
                restr.append(i + "f:" + reachCount[i] + System.getProperty("line.separator"));
            }
            restr.append("dataCount" + System.getProperty("line.separator"));
            for (int i = 0; i < TOPFLOOR; i++) {
                restr.append(i + "f:" + dataCount[i] + System.getProperty("line.separator"));
            }

            System.out.println(new String(restr));

            //
            Logger.OutputFileLog(new String(fileName + ".txt"), new String(restr));

            System.exit(0);
        }
    }

    // 各階層への到達回数が一定数を超えているか
    public boolean isReachCount() {
        for (int flr = 0; flr < TOPFLOOR; flr++) {
            if (reachCount[flr] < BOUND_REACHCOUNT) {
                return false;
            }
        }
        return true;
    }

    public void setDrawLevel(int lv) {
        this.drawlevel = lv;
    }

    public int getDrawLevel() {
        return drawlevel;
    }

    // ステータスの描画
    public void drawStatusBar(Graphics g, Player iplayer) {
        Font statusFont = new Font("Alial", Font.BOLD, 30);

        g.setColor(Color.white);
        g.setFont(statusFont);
        g.drawString("hp : " + iplayer.hp + "/" + iplayer.maxHp, 30, 30);

        g.setColor(Color.white);
        g.setFont(statusFont);
        g.drawString("sp : " + iplayer.satiety + "/" + iplayer.maxSatiety, 30, 60);

        g.setColor(Color.white);
        g.setFont(statusFont);
        g.drawString(floorNumber + "F", 30, 90);

        g.setColor(Color.white);
        g.setFont(statusFont);
        g.drawString("Lv : " + iplayer.level, 30, 120);
    }

    // 階段降りた際に収集したデータ
    public class LData {
        int fl; // 階層
        int hp; // hp
        int lv; // レベル
        int sp; // 満腹度（＋食料）
        int pt; // ポーション数
        int ar; // 矢数
        int st; // 杖数
        boolean nextFlabel; // 次階層の勝敗，true:突破成功，false:突破失敗
        boolean label; // 勝敗，true:クリア，false:ゲームオーバー
        
        int stm; // 満腹度
        int fd; // 食料数
        double exp; // 経験値の割合，あとどのくらいでlvupするか
        double unknownAreaPer; // 未知エリアの割合
        
        // 現在（降りる前）のフロアで使ったアイテム数
        int currentFlrUseFd;
        int currentFlrUsePt;
        int currentFlrUseAr;
        int currentFlrUseSt;
        // 現在のフロアでゲットしたアイテム数
        int currentFlrGetFd;
        int currentFlrGetPt;
        int currentFlrGetAr;
        int currentFlrGetSt;
        
        boolean updateFlag;
        
        public LData() {
            fl = 0;
            hp = 0;
            lv = 0;
            sp = 0;
            pt = 0;
            ar = 0;
            st = 0;
            nextFlabel = false;
            label = false;
            
            stm = 0;
            fd = 0;
            exp = 0;
            unknownAreaPer = 0;
            
            currentFlrUseFd = 0;
            currentFlrUsePt = 0;
            currentFlrUseAr = 0;
            currentFlrUseSt = 0;
            
            currentFlrGetFd = 0;
            currentFlrGetPt = 0;
            currentFlrGetAr = 0;
            currentFlrGetSt = 0;
            
            updateFlag = false;
        }

        public void setLData(int fnum, Player p) {
            fl = fnum;
            hp = p.hp;
            lv = p.level;
            sp = p.satiety + (p.inventory.getInvItemNum(1) * p.inventory.getFoodHealVal());
            pt = p.inventory.getInvItemNum(2);
            ar = p.inventory.getInvItemNum(3);
            st = p.inventory.getInvItemNum(4);
            
            stm = p.satiety;
            fd = p.inventory.getInvItemNum(1);
            exp = (double)p.exp / p.lvupExp;
            
            updateFlag = true;
        }
        
        public void setLData_everyTurn(int fnum, Player p, boolean[][] pmap) {
            // アイテム数の変化や未知領域の割合など毎ターン更新する（必要ないかも）
            int provFd = p.inventory.getInvItemNum(1); // 現在の食料数
            int provPt = p.inventory.getInvItemNum(2);
            int provAr = p.inventory.getInvItemNum(3);
            int provSt = p.inventory.getInvItemNum(4);
            // 直前のターンの個数と異なるとき
            if(provFd != fd) {
                if(provFd == fd - 1){ // アイテム使用で1個減ってる
                    currentFlrUseFd++;
                }
                else if(provFd == fd + 1){ // アイテムゲットで1個増えている
                    currentFlrGetFd++;
                }
                else{
                    // デバッグモードでないとき
                    if(DEBUG_INIT != true) System.out.println("ログ収集系統fd，何かしらのエラー:" + fd + "->" + provFd);
                }
                fd = provFd;
            }
            if(provPt != pt) {
                if(provPt == pt - 1){ // アイテム使用で1個減ってる
                    currentFlrUsePt++;
                }
                else if(provPt == pt + 1){ // アイテムゲットで1個増えている
                    currentFlrGetPt++;
                }
                else{
                    // デバッグモードでないとき
                    if(DEBUG_INIT != true) System.out.println("ログ収集系統pt，何かしらのエラー:" + pt + "->" + provPt);
                }
                pt = provPt;
            }
            if(provAr != ar) {
                if(provAr == ar - 1){ // アイテム使用で1個減ってる
                    currentFlrUseAr++;
                }
                else if(provAr == ar + 3){ // アイテムゲットで"3個"増えている
                    currentFlrGetAr += 3;
                }
                else{
                    // デバッグモードでないとき
                    if(DEBUG_INIT != true) System.out.println("ログ収集系統ar，何かしらのエラー:" + ar + "->" + provAr);
                }
                ar = provAr;
            }
            if(provSt != st) {
                if(provSt == st - 1){ // アイテム使用で1個減ってる
                    currentFlrUseSt++;
                }
                else if(provSt == st + 1){ // アイテムゲットで1個増えている
                    currentFlrGetSt++;
                }
                else{
                    // デバッグモードでないとき
                    if(DEBUG_INIT != true) System.out.println("ログ収集系統st，何かしらのエラー:" + st + "->" + provSt);
                }
                st = provSt;
            }
            
            // 未知領域割合計算
            int count = 0;
            for (int y = 0; y < MAPGRIDSIZE_Y; y++) {
                for (int x = 0; x < MAPGRIDSIZE_X; x++) {
                    if (pmap[y][x] == false) {
                        count++;
                    }
                }
            }
            unknownAreaPer = (count * 100.0) / (double) (MAPGRIDSIZE_Y * MAPGRIDSIZE_X);
        }

        public void setNextFlabel(boolean tf) {
            nextFlabel = tf;
        }

        public void setLabel(boolean tf) {
            label = tf;
        }

        public int[] getLData() {
            return new int[]{hp, lv, sp, pt, ar, st};
        }

        public void sysoutput() {
            System.out.println(fl + " : {" + hp + ", " + lv + ", " + sp + ", " + pt + ", " + ar + ", " + st + ", " + nextFlabel + "} -> " + label);
        }

        public void csvfileSetLabel(String fn) {
            String str = new String("fl,hp,lv,sp,pt,ar,st,game clear,next floor clear,"
                    + "stm,fd,exp,unknownAreaPer,"
                    + "currentFlrUseFd,currentFlrUsePt,currentFlrUseAr,currentFlrUseSt,"
                    + "currentFlrGetFd,currentFlrGetPt,currentFlrGetAr,currentFlrGetSt"
                    + System.getProperty("line.separator"));

            Logger.OutputFileLog(new String(fileName + ".csv"), str, true);
        }

        public void csvFileOutput(String fn) {
            StringBuilder data = new StringBuilder();
            data.append(fl + "," + hp + "," + lv + "," + sp + "," + pt + "," + ar + "," + st + ", " + (label == true ? 1 : -1) + ", " + (nextFlabel == true ? 1 : -1)
                    + "," + stm + "," + fd + "," + exp + "," + unknownAreaPer
                    + "," + currentFlrUseFd + "," + currentFlrUsePt + "," + currentFlrUseAr + "," + currentFlrUseSt
                    + "," + currentFlrGetFd + "," + currentFlrGetPt + "," + currentFlrGetAr + "," + currentFlrGetSt
                    + System.getProperty("line.separator"));

            Logger.OutputFileLog(new String(fileName + ".csv"), new String(data), true);
        }
    }

    // 戦闘後に収集したデータ
    public class LData_bt {

        int number; // ゲーム回数

        int rcountnum; // 到達回数，何番目か
        int dcountnum; // データ数，何番目か

        int fl; // 階層
        int hp; // hp
        int lv; // レベル
        int sp; // 満腹度（＋食料）
        int pt; // ポーション数
        int ar; // 矢数
        int st; // 杖数

        int flfd; // 現階層で拾った食料数
        int flpt; // 現階層で拾ったポーション数
        int flar; // 現階層で拾った矢数
        int flst; // 現階層で拾った杖数

        boolean stairflag; // true:階段発見済み, fasle:未発見
        int getflooritemnum; // この階層でゲットしたアイテム数
        int getallfitemnum; // 現在までにゲットしたアイテム数
        double unknownAreaPer; // 未知エリアの割合

        boolean label_bt; // 勝敗，true:戦闘に勝利，false:ゲームオーバー
        boolean label_cf; // 現在の階層を突破できたか，true:突破できた，false:ゲームオーバー
        boolean label_nf; // 次階層を突破できたか，true:突破できた，false:ゲームオーバー
        boolean label; // ゲームクリアの有無，true:ゲームクリア，false:ゲームオーバー

        public LData_bt() {
            number = 0;

            rcountnum = 0;
            dcountnum = 0;

            fl = 0;
            hp = 0;
            lv = 0;
            sp = 0;
            pt = 0;
            ar = 0;
            st = 0;

            flfd = 0;
            flpt = 0;
            flar = 0;
            flst = 0;

            stairflag = false;
            getflooritemnum = 0;
            getallfitemnum = 0;
            unknownAreaPer = 100.0;

            label_bt = false;
            label_cf = false;
            label_nf = false;
            label = false;
        }

        public void setLData_bt(int gcount, int fnum, Player p, int stairid, boolean[][] pmap) {
            number = gcount;

            fl = fnum;
            hp = p.hp;
            lv = p.level;
            sp = p.satiety + (p.inventory.getInvItemNum(1) * p.inventory.getFoodHealVal());
            pt = p.inventory.getInvItemNum(2);
            ar = p.inventory.getInvItemNum(3);
            st = p.inventory.getInvItemNum(4);

            flfd = p.getItemfloorFood[p.curFloor];
            flpt = p.getItemfloorPotion[p.curFloor];
            flar = p.getItemfloorLStaff[p.curFloor];
            flst = p.getItemfloorWstaff[p.curFloor];

            stairflag = (stairid != -1) ? true : false; // stairRoomID != -1 -> 階段発見済み
            getflooritemnum = p.getItemfloorFood[p.curFloor] + p.getItemfloorPotion[p.curFloor] + p.getItemfloorLStaff[p.curFloor] + p.getItemfloorWstaff[p.curFloor];

            for (int f = 0; f <= p.curFloor; f++) {
                getallfitemnum += (p.getItemfloorFood[f] + p.getItemfloorPotion[f] + p.getItemfloorLStaff[f] + p.getItemfloorWstaff[f]);
            }

            int count = 0;
            for (int y = 0; y < MAPGRIDSIZE_Y; y++) {
                for (int x = 0; x < MAPGRIDSIZE_X; x++) {
                    if (pmap[y][x] == false) {
                        count++;
                    }
                }
            }
            unknownAreaPer = (count * 100.0) / (double) (MAPGRIDSIZE_Y * MAPGRIDSIZE_X);
        }

        // 戦闘の勝敗
        public void setLabel_bt(boolean tf) {
            label_bt = tf;
        }

        public void setLabel_cf(boolean tf) {
            label_cf = tf;
        }

        public void setLabel_nf(boolean tf) {
            label_nf = tf;
        }

        // ゲームクリアの有無
        public void setLabel(boolean tf) {
            label = tf;
        }

        public int[] getLData_bt() {
            return new int[]{hp, lv, sp, pt, ar, st};
        }

        public void sysoutput() {
//                System.out.println(number + ", " + fl + " : {" + hp + ", " + lv + ", " + sp + ", " + pt + ", " + ar + ", " + st + ", " + 
//                        getflooritemnum + ", " + getallfitemnum + ", " + unknownAreaPer + ", " + (stairflag == true ? 1 : -1) + "}");
//                System.out.println("    label_bt : " + label_bt);
//                System.out.println("    label_cf : " + label_cf);
//                System.out.println("    label_nf : " + label_nf);
//                System.out.println("    label    : " + label);
        }

        public void csvFileSetLabel(String fn) {
            String str = new String("game number,reach number,data number,fl,hp,lv,sp,pt,ar,st,get floor-item,"
                    + "get floor-fd,get floor-pt,get floor-ar,get floor-st,"
                    + "get all item,unknown area,stair,"
                    + "battle result,current floor clear,next floor clear,game clear"
                    + System.getProperty("line.separator"));

            Logger.OutputFileLog(new String(fileName + "_bt.csv"), str, true);
        }

        public void csvFileOutput(String fn) {
            StringBuilder data = new StringBuilder();
            data.append(number + "," + rcountnum + "," + dcountnum + ","
                    + fl + "," + hp + "," + lv + "," + sp + "," + pt + "," + ar + "," + st + ", "
                    + getflooritemnum + ", " + flfd + ", " + flpt + ", " + flar + ", " + flst + ", "
                    + getallfitemnum + ", " + unknownAreaPer + ", "
                    + (stairflag == true ? 1 : -1) + ", " + (label_bt == true ? 1 : -1) + ", "
                    + (label_cf == true ? 1 : -1) + ", " + (label_nf == true ? 1 : -1) + ", "
                    + (label == true ? 1 : -1) + System.getProperty("line.separator"));

            Logger.OutputFileLog(new String(fileName + "_bt.csv"), new String(data), true);
        }
    }
}
