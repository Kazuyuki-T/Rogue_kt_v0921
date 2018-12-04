import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.ArrayList;

public class MyCanvas extends Canvas
{
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

    private int selectItem; // インベントリの選択部分(インベントリのハイライト)

    // ルールベースプレイヤー
    private RuleBasePlayer rbp;
    private RuleBasePlayer_bu rbp_bu;

    private int loseCount = 0; // 敗北回数
    private int winCount = 0; // 勝利回数
    private int gameCount = 0; // ゲーム回数

    private long start; // 処理開始前の時間を保持する
    private long end; // 実行時間を保持する

    private static final boolean DATA_COLLECTED = false; // true:データ収集,false:通常の実験
    private static final int BOUND_REACHCOUNT = 100; // 各階層の最低到達回数

    public static final int TRYNUM = 200; // 実行回数

    public static final int DEBUG_INIT = 2; // 初期配置，0:ランダム配置, 1:配置をいじる，2:csv準拠
    
    private static final int initFlr = 0; // 初期化時のフロア階層，開始フロアを変更する際に使用
    public static int floorNumber;  // 現在の階層

    // フレームのサイズ
    private int frameSizeX;
    private int frameSizeY;

    static boolean startFlag; // 初回判定

    private boolean startDrawmapFlag; // マップの表示開始フラグ

    private boolean resultFlag; // リザルト表示判定

    private int scene; // シーン管理変数，0:タイトル画面, 1:ゲームのメイン画面

    public static final int SCENE_TITLE = 0;
    public static final int SCENE_GAMEMAIN = 1;
    public static final int SCENE_RESULT = 2;
    public static final int SCENE_INV = 3;
    public static final int SCENE_PAUSE = 4;

    int shotSPkey_state; // スペースキーの状態
    public static final int SHOT_PRESSED = 1; // 押されている
    public static final int SHOT_DOWN = 2; // 今押されたばかり
    
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
    
    public static final int TOPFLOOR = 4; // ダンジョンの最下層，この数字の階に到達したらクリア

    public static int useItemPotion = 0;
    public static int useItemFood = 0;
    public static int useItemLStaff = 0;
    public static int useItemWStaff = 0;

    public static int[] getItemPotion;
    public static int[] getItemFood;
    public static int[] getItemLStaff;
    public static int[] getItemWstaff;

    public static int[] invItem;

    private int[] loseFloor; // 0:死亡した階数，1:餓死の回数

    private int[] lvFloor;
    private int[] lvFloorNum;

    // 餓死回数
    public static int gasi = 0;
    public static int[] gasif;

    private static int histCount = 0;

    private Info info;

    private int drawlevel; // 10未満：表示なし，10以上：表示あり

    private LData[] ld; // 1->2, 2->3, 3->4における状況

    private LData_bt ld_bt;
    private ArrayList<LData_bt> LDBT_List; // 1ゲーム終了までの戦闘データ

    private int[] reachCount; // 各階層への到達回数
    private int[] dataCount; // 各階層の取得データ数

    public String fileName; // ディレクトリ名（日付）

    private static int GAMEMODE; // 0:人間,1:AIプレイヤ,2:高速周回,3:一時停止
    
    // csv読み込みに使用
    File fname;
    BufferedReader br = null;
    String line = "";
    //String csvFile = "src/mat/ar2fStartFormatData_ar9.csv";
    String csvFile = "src/mat/2flrStartFormatData.csv";
    //String csvFile = "src/mat/test2f.csv";
    
    public RuleBasePlayer getrbp() { return rbp; }

    // コンストラクタ
    public MyCanvas(int x, int y, int lv, int gmode, String fn)
    {
        GAMEMODE = gmode;
        fileName = fn;
        
        random = new Random(); // 乱数

        keyinput = new KeyInput();
        addKeyListener(keyinput); // キーリスナ
        setFocusable(true); // フォーカス
        title = new Title();
        background = new Background(x, y);

        turnmanager = new TurnManager(background, MAPGRIDSIZE_X, MAPGRIDSIZE_Y);

        frameSizeX = x;
        frameSizeY = y;

        floorNumber = initFlr;

        selectItem = -1; // 初期は未選択状態

        // 初期化
        loseFloor = new int[TOPFLOOR];
        gasif = new int[TOPFLOOR];
        lvFloor = new int[TOPFLOOR];
        lvFloorNum = new int[TOPFLOOR];
        getItemPotion = new int[TOPFLOOR];
        getItemFood = new int[TOPFLOOR];
        getItemLStaff = new int[TOPFLOOR];
        getItemWstaff = new int[TOPFLOOR];
        for (int i = 0; i < TOPFLOOR; i++) {
            loseFloor[i] = 0;
            gasif[i] = 0;
            lvFloor[i] = 0;
            lvFloorNum[i] = 0;
            getItemPotion[i] = 0;
            getItemFood[i] = 0;
            getItemLStaff[i] = 0;
            getItemWstaff[i] = 0;
        }
        invItem = new int[TOPFLOOR];

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
        
        // 読み込むcsvファイルの準備
        if(DEBUG_INIT == 2){
            try {
                fname = new File(csvFile);
                br = new BufferedReader(new FileReader(fname));
                line = br.readLine(); // 1行目のラベルを読み込んでおく
            } catch (IOException e) {
                System.out.println(e); // エラー吐き
                System.out.println("constractar error");
            }
        }
        
        start = System.currentTimeMillis(); // 計測開始
    }

    // 初期化
    public boolean init()
    {
        objectset = new ObjectSet(background);
        turnmanager = new TurnManager(background, MAPGRIDSIZE_X, MAPGRIDSIZE_Y);

        rbp = new RuleBasePlayer();
        rbp_bu = new RuleBasePlayer_bu();

        scene = SCENE_GAMEMAIN; // 初期化直後の場面

        gameover = false;

        startFlag = false;

        resultFlag = false;

        floorNumber = initFlr;

        // submap-update
        startDrawmapFlag = false;

        for (int i = 0; i < ld.length; i++) {
            ld[i] = new LData();
        }

        ld_bt = new LData_bt();
        LDBT_List.clear();
        
        // ここでプレイヤの開始状態を変更する，csvから1行ずつ
        if(DEBUG_INIT == 2){
            try {
                line = br.readLine(); // 1行ずつ読み込み
                // 最終行の処理が終了しているとき
                if(line == null){
                    br.close(); // 最後まで読み込んでからstreamをクローズ
                    return false;
                } 
                
                double[] ele = new double[10]; // flr,hp,lv,sp,pt,ar,st,unk,st,gc
                String[] str = line.split(",", 0);
                for(int i = 0; i < str.length ; i++){
                    ele[i] = Double.parseDouble(str[i]);
                }
                
                floorNumber = (int)ele[0] + 1; // flr, 階段を下りた際に収集したデータならば，次の階層の開始状態にしたいため
                objectset.player.curFloor = floorNumber;
                //objectset.player.hp = (int)ele[1]; // hp
                objectset.player.setHp((int)ele[1]); // hp
                objectset.player.setLevel((int)ele[2]); // lv
                objectset.player.maxSatiety = ((int)ele[3] > 100) ? (int)ele[3] : 100; // 最大値，100より大きいときはその値，小さいときは100
                objectset.player.satiety = (int)ele[3]; // stm + food
                for(int n = 0; n < (int)ele[4]; n++){
                    objectset.player.inventory.addItem(2); // pt
                }
                int arUsagecount = objectset.player.inventory.setDetail(3).usageCount; // 矢の使用回数
                for(int n = 0; n < (int)ele[5] / arUsagecount; n++){
                    objectset.player.inventory.addItem(3); // ar
                }
                int remainder = (int)ele[5] % arUsagecount; // 余り
                if(remainder != 0) objectset.player.inventory.addItem(3, remainder); // 使用回数調節
                for(int n = 0; n < (int)ele[6]; n++){
                    objectset.player.inventory.addItem(4); // st
                }
                
//                System.out.println("line: " + line);
//                System.out.println("flr : " + (int)ele[0]);
//                System.out.println("hp  : " + (int)ele[1]);
//                System.out.println("lv  : " + (int)ele[2]);
//                System.out.println("st  : " + (int)ele[3]);
//                System.out.println("pt  : " + (int)ele[4]);
//                System.out.println("ar  : " + (int)ele[5]);
//                System.out.println("stf : " + (int)ele[6]);
                
            } catch (IOException e) {
                System.out.println(e); // エラー吐き
                System.out.println("init error");
            }
        }
        
        return true;
    }

    // 描画
    public void paint(Graphics g)
    {
        // ちらつき防止 -> オフスクリーンバッファ使用
        // オフスクリーンバッファの内容を自分にコピー
        g.drawImage(imgBuf, 0, 0, this);
    }

    // バッファのクリア
    public void gBufClear(Graphics g)
    {
        g.setColor(Color.black);
        g.fillRect(0, 0, frameSizeX, frameSizeY);
    }
    
    // オーバーライド
    // クリア防止のため
    public void update(Graphics g) { paint(g); }

    public void run()
    {
        //オフスクリーンバッファ作成
        imgBuf = createImage(frameSizeX, frameSizeY);
        gBuf = imgBuf.getGraphics();

        // 経過管理
        while (true) {
            shotSPkey_state = keyinput.checkSpaceShotKey(); // スペースキーの状態を管理

            gBufClear(gBuf); // バッファをクリア

            //シーン遷移用の変数で分岐
            switch (scene) {
                case 0: //タイトル画面
                    //titleScene();
                    break;
                case 1: //ゲームのメイン画面
                    gameScene();
                    break;
                case 2: // リザルト表示
                    resultScene();
                    break;
                case 3: // ゲーム中インベントリを開いている画面
                    inventoryScene();
                    break;
                case 4: // 一時停止
                    pauseScene();
                    break;
            }

            repaint(); // 再描画

            if (drawlevel >= 10) {
                try {
                    // ループのウェイト管理
                    switch(GAMEMODE) {
                        case 0:  Thread.sleep(10);   break;
                        case 1:  Thread.sleep(500);  break;
                        case 2:  Thread.sleep(0);    break;
                        case 3:  Thread.sleep(10);   break;
                        default: Thread.sleep(10);   break;
                    }
                } catch (InterruptedException e) {
                    // エラー時の処理
                }
            }
        }
    }

    public void drawMap()
    {
        // マップ描画
        if (startFlag == true) {
            int biasX = 0;      // x方向のバイアス，表示領域調整用
            int biasY = 600;    // y方向のバイアス，表示領域調整用

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
                    // ルールベース時の目標点を表示 
                    if (false) {
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

    // 結果出力画面の処理
    void resultScene() {
        // マップ描画(バッファをクリア)
        background.drawGameBG(gBuf, objectset.player); // プレイヤーの座標
        background.drawGridBG(gBuf); // マップに対してグリッド線の表示

        if (resultFlag == false) {
            //Logger.appendLog("*-- result --*");
            resultFlag = true;
        }

        // 1ゲーム毎の処理
        if (histCount != gameCount) {
            // ログに追加がここ？初期化のタイミングとの兼ね合い
            Logger.OutputFileLog(new String(fileName + "_gamelog.txt"), Logger.getLog(), true);
            Logger.initLog(); // logの初期化，必須
            histCount = gameCount;
        }
 
        if(DEBUG_INIT != 2) outputResultLog(true); // ログ吐き，規定回数終了時やデータ収集DATACORRECTではここで終了
        
        if(GAMEMODE == 0){
            // 人間操作で，スペースキーが押されたとき
            if (shotSPkey_state == SHOT_DOWN) init(); // ゲームの再初期化
        }
        else{
            // 人間操作以外でゲームが終了したとき
            boolean tf = init(); // ゲームの初期化
            if(tf == false) {
                outputResultLog(tf);
                System.exit(0);
            } // 初期化に失敗しているとき（csvファイル処理が終了したとき）
        }
    }

    // 一時停止画面の処理
    void pauseScene()
    {
        // キャンバス内すべての描画
        if (drawlevel >= 10) drawAll();
        // 一時停止解除
        if (keyinput.checkAShotKey() == SHOT_PRESSED) scene = SCENE_GAMEMAIN;
    }
    
    // インベントリを開いた状態
    void inventoryScene()
    {
        int currentItemNum = objectset.player.inventory.getInvItemNum(); // 現在の所持アイテム個数
        // そもそもアイテム持ってないとき，
        if(currentItemNum == 0) {
            scene = SCENE_GAMEMAIN;
            return;
        }
        
        if(selectItem == -1) selectItem = 0; // インベントリ画面を開くたびに0選択
        
        int crrentTurn = turnmanager.getTurn(); // 現在のターン数

        // 所持アイテム数が1個以上あるとき，
        // ターンの経過管理を行う
        if (currentItemNum > 0) {
            // TurnManagerを用いてターン管理を行う
            // インベントリ画面とゲーム画面では引数が異なる
            // アイテムの使用，投擲，配置によるターン経過を管理する
            turnmanager.turnCount(objectset, keyinput, selectItem, background);

            // 所持アイテム数の更新
            currentItemNum = objectset.player.inventory.getInvItemNum();
        }

        // キャンバス内すべての描画
        drawAll();
        
        // 表示されたインベントリ内の移動
        if (keyinput.checkDownShotKey() == SHOT_DOWN) {
            if(selectItem < currentItemNum - 1) selectItem++;
            else selectItem = 0;
        }
        if (keyinput.checkUpShotKey() == SHOT_DOWN) {
            if(selectItem > 0) selectItem--;
            else selectItem = currentItemNum - 1;
        }
        if (keyinput.checkLeftShotKey() == SHOT_DOWN) {
            selectItem -= 5;
            if(selectItem < 0) selectItem = 0;
        }
	if (keyinput.checkRightShotKey() == SHOT_DOWN) {
            selectItem += 5;
            if(selectItem >= currentItemNum - 1) selectItem = currentItemNum - 1;
	}

        // eが押された，アイテムの使用・投擲によりターンが進む
        if (keyinput.checkEShotKey() == SHOT_DOWN || crrentTurn != turnmanager.getTurn()) {
            selectItem = -1; // インベントリ画面が閉じるたびに未選択状態
            scene = SCENE_GAMEMAIN; // メイン画面に行く
        }
    }

    // ゲームオーバーの処理
    public void gameOverProcess() {
        ld_bt.setLData_bt(gameCount, floorNumber, objectset.player, rbp.getStairRoomID(), objectset.getpmap()); // データをセット
        ld_bt.setLabel_bt(false); // ラベルをセット
        ld_bt.sysoutput(); // 出力
        LDBT_List.add(ld_bt); // リストに追加
        ld_bt = new LData_bt(); // 初期化

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

        ld[floorNumber].setLData(floorNumber, objectset.player); // 敗北時の状態反映

        for (int i = 0; i < ld.length; i++) {
            if (i < floorNumber - 1) ld[i].setNextFlabel(true);
            else                     ld[i].setNextFlabel(false);

            ld[i].setLabel(false);
            ld[i].sysoutput();

            if (ld[i].updateFlag == true) ld[i].csvFileOutput(new String("data"));
        }

        loseCount++;
        loseFloor[floorNumber]++;
        gameCount++;
        sysoutputCurrentRate(); // ゲーム数，勝率のコンソール上への出力

        scene = SCENE_RESULT; // リザルト画面に行く
    }
    
    // ゲームクリアの処理
    public void gameClearProcess() {
        // 直前の階層におけるフラグを更新
        for (int i = 0; i < LDBT_List.size(); i++) {
            if (LDBT_List.get(i).fl == floorNumber - 1) LDBT_List.get(i).setLabel_cf(true); // フラグを更新
        }

        // 前前の階層におけるフラグを更新
        for (int i = 0; i < LDBT_List.size(); i++) {
            if (LDBT_List.get(i).fl == floorNumber - 2) LDBT_List.get(i).setLabel_nf(true); // フラグを更新
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

        
        ld[floorNumber - 1].setLData(floorNumber - 1, objectset.player); // クリア時の状態反映

        //System.out.println("down the stairs");
        for (int i = 0; i < ld.length; i++) {
            if (i < floorNumber - 1)    ld[i].setNextFlabel(true);
            else                        ld[i].setNextFlabel(false);

            ld[i].setLabel(true);
            ld[i].sysoutput();

            if (ld[i].updateFlag == true) ld[i].csvFileOutput(new String("data"));
        }

        winCount++;
        gameCount++;
        sysoutputCurrentRate(); // コンソール上への出力

        scene = SCENE_RESULT; // リザルト画面に行く
    }
    
    // ゲーム画面の処理
    public void gameScene() {
        if (objectset.isGameover() == true) gameOverProcess();     // ゲームオーバー判定なら
        else if (floorNumber == TOPFLOOR)   gameClearProcess();    // ゲームクリア判定なら
        else {
            // ゲーム開始直後の場合，オブジェクトを設置する
            if (startFlag == false) {
                if (floorNumber == 0) System.out.println("num:" + gameCount);

                // フロア数が1より大きいとき
                if (floorNumber >= 1) {
                    lvFloor[floorNumber - 1] += objectset.player.level;
                    lvFloorNum[floorNumber - 1]++;
                }

                // 階層が1より大きいとき，現在のプレイヤ情報を格納
                // かつ，初期化階層と異なるとき
                if (floorNumber >= 1 && floorNumber != initFlr) {
                    ld[floorNumber - 1].setLData(floorNumber - 1, objectset.player);
                    
                    // 獲得アイテム数のために
                    ld[floorNumber].setLData(floorNumber, objectset.player);
                }

                // フロア数が1より大きいとき，直前の階層におけるフラグを更新
                if (floorNumber >= 1) {
                    for (int i = 0; i < LDBT_List.size(); i++) {
                        if (LDBT_List.get(i).fl == floorNumber - 1) LDBT_List.get(i).setLabel_cf(true); // フラグを更新
                    }
                }

                // フロア数が2より大きいとき，前前の階層におけるフラグを更新
                if (floorNumber >= 2) {
                    for (int i = 0; i < LDBT_List.size(); i++) {
                        if (LDBT_List.get(i).fl == floorNumber - 2) LDBT_List.get(i).setLabel_nf(true); // フラグを更新
                    }
                }

                // 到達回数のカウント
                reachCount[floorNumber]++;

                Logger.appendLog(floorNumber + "F");
                Logger.appendLog("\n" + "[turn : " + turnmanager.getTurn() + "]");

                flrSetting(DEBUG_INIT); // フロアの設定，デバッグモードにより変化
            } else {
                info.stairpos = (rbp.getStairRoomID() != -1) ? true : false; // stairRoomID != -1 -> 階段発見済み
                ld[floorNumber].setLData_everyTurn(floorNumber, objectset.player, objectset.getpmap(), info.stairpos); // ログのデータ収集における情報更新，毎ターンチェック
                

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
                
                if(GAMEMODE == 3) scene = SCENE_PAUSE;
                
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
                    ld_bt.setLData_bt(gameCount, floorNumber, objectset.player, rbp.getStairRoomID(), objectset.getpmap());
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
            drawAll(); // キャンバス内すべての描画
        }
    }
    
    public void outputResultLog(boolean tf) {
        //<editor-fold defaultstate="collapsed" desc="ログ取り">
        // 既定のゲーム回数終了時
        if (DATA_COLLECTED == false && gameCount == TRYNUM && tf == true) {
            scene = SCENE_TITLE;

            // 結果をstrに
            StringBuilder restr = new StringBuilder();
            int arriveNum = TRYNUM;
            int[] arriveArr = new int[]{arriveNum, 0, 0, 0};

            // 実験設定
            
            
            restr.append("trials" + "," + TRYNUM + System.getProperty("line.separator"));
            // 計測終了
            end = System.currentTimeMillis();
            restr.append("time" + "," + ((double) (end - start) / 1000) + "," + "sec" + System.getProperty("line.separator"));
            restr.append("clear" + "," + winCount + System.getProperty("line.separator"));
            restr.append("death" + "," + loseCount + System.getProperty("line.separator"));
            for (int i = 0; i < TOPFLOOR; i++) {
                if (i >= 1) {
                    arriveNum -= loseFloor[i - 1];
                    arriveArr[i] = arriveNum;
                }
                restr.append("deathFloor" + i + "," + loseFloor[i] + "," + gasif[i] + System.getProperty("line.separator"));
            }
            restr.append("gasi" + "," + gasi + System.getProperty("line.separator"));
            restr.append("useFood" + "," + useItemFood + System.getProperty("line.separator"));
            restr.append("usePotion" + "," + useItemPotion + System.getProperty("line.separator"));
            restr.append("useLStaff" + "," + useItemLStaff + System.getProperty("line.separator"));
            restr.append("useWStaff" + "," + useItemWStaff + System.getProperty("line.separator"));
            for (int i = 0; i < TOPFLOOR; i++) {
                restr.append("Level-ave" + i + "," + (lvFloor[i] / lvFloorNum[i]) + System.getProperty("line.separator"));
            }
            for (int i = 0; i < TOPFLOOR; i++) {
                restr.append(i + "farrive" + "," + arriveArr[i] + System.getProperty("line.separator"));
                restr.append("getFood" + "," + getItemFood[i] + System.getProperty("line.separator"));
                restr.append("getPotion" + "," + getItemPotion[i] + System.getProperty("line.separator"));
                restr.append("getLStaff" + "," + getItemLStaff[i] + System.getProperty("line.separator"));
                restr.append("getWStaff" + "," + getItemWstaff[i] + System.getProperty("line.separator"));
                int sum = getItemFood[i] + getItemPotion[i] + getItemLStaff[i] + getItemWstaff[i];
                restr.append("sum" + "," + sum + System.getProperty("line.separator"));
            }

            System.out.println(new String(restr));

            //
            Logger.OutputFileLog(new String(fileName + "_result.txt"), new String(restr));
            Logger.OutputFileLog(new String(fileName + "_result.csv"), new String(restr));

            System.exit(0);
        }

        // 各階層への到達数が上限以上 -> 終了
        // かつ，ゲームが一区切りついたとき
        if (DATA_COLLECTED == true && isReachCount() == true && startFlag == false && floorNumber == 0 && tf == true) {
            scene = SCENE_TITLE;

            // 結果をstrに
            StringBuilder restr = new StringBuilder();
            int arriveNum = TRYNUM;
            int[] arriveArr = new int[]{arriveNum, 0, 0, 0};

            restr.append("試行回数" + "," + gameCount + System.getProperty("line.separator"));
            // 計測終了
            end = System.currentTimeMillis();
            restr.append("実験時間" + "," + ((double) (end - start) / 1000) + "," + "sec" + System.getProperty("line.separator"));
            restr.append("clear" + "," + winCount + System.getProperty("line.separator"));
            restr.append("death" + "," + loseCount + System.getProperty("line.separator"));
            for (int i = 0; i < TOPFLOOR; i++) {
                if (i >= 1) {
                    arriveNum -= loseFloor[i - 1];
                    arriveArr[i] = arriveNum;
                }
                restr.append("deathFloor" + i + "," + loseFloor[i] + "," + gasif[i] + System.getProperty("line.separator"));
            }
            restr.append("gasi" + "," + gasi + System.getProperty("line.separator"));
            restr.append("useFood" + "," + useItemFood + System.getProperty("line.separator"));
            restr.append("usePotion" + "," + useItemPotion + System.getProperty("line.separator"));
            restr.append("useLStaff" + "," + useItemLStaff + System.getProperty("line.separator"));
            restr.append("useWStaff" + "," + useItemWStaff + System.getProperty("line.separator"));
            for (int i = 0; i < TOPFLOOR; i++) {
                restr.append("Level-ave" + i + "," + (lvFloor[i] / lvFloorNum[i]) + System.getProperty("line.separator"));
            }
            for (int i = 0; i < TOPFLOOR; i++) {
                restr.append(i + "farrive" + "," + arriveArr[i] + System.getProperty("line.separator"));
                restr.append("getFood" + "," + getItemFood[i] + System.getProperty("line.separator"));
                restr.append("getPotion" + "," + getItemPotion[i] + System.getProperty("line.separator"));
                restr.append("getLStaff" + "," + getItemLStaff[i] + System.getProperty("line.separator"));
                restr.append("getWStaff" + "," + getItemWstaff[i] + System.getProperty("line.separator"));
                int sum = getItemFood[i] + getItemPotion[i] + getItemLStaff[i] + getItemWstaff[i];
                restr.append("sum" + "," + sum + System.getProperty("line.separator"));
            }
            restr.append("reachCount" + System.getProperty("line.separator"));
            for (int i = 0; i < TOPFLOOR; i++) {
                restr.append(i + "f" + "," + reachCount[i] + System.getProperty("line.separator"));
            }
            restr.append("dataCount" + System.getProperty("line.separator"));
            for (int i = 0; i < TOPFLOOR; i++) {
                restr.append(i + "f" + "," + dataCount[i] + System.getProperty("line.separator"));
            }

            System.out.println(new String(restr));

            //
            Logger.OutputFileLog(new String(fileName + "_result.txt"), new String(restr));
            Logger.OutputFileLog(new String(fileName + "_result.csv"), new String(restr));

            System.exit(0);
        }
        
        if(DEBUG_INIT == 2 && tf == false){
            scene = SCENE_TITLE;

            // 結果をstrに
            StringBuilder restr = new StringBuilder();
            int arriveNum = TRYNUM;
            int[] arriveArr = new int[]{arriveNum, 0, 0, 0};

            // 実験設定
            restr.append("csv" + "," + csvFile + System.getProperty("line.separator")); // 初期状態に使用したcsv
            // aiプレイヤの種類
            // それに応じた諸々設定
            
            
            restr.append("試行回数" + "," + gameCount + System.getProperty("line.separator"));
            // 計測終了
            end = System.currentTimeMillis();
            restr.append("実験時間" + "," + ((double) (end - start) / 1000) + "," + "sec" + System.getProperty("line.separator"));
            restr.append("clear" + "," + winCount + System.getProperty("line.separator"));
            restr.append("death" + "," + loseCount + System.getProperty("line.separator"));
            for (int i = 0; i < TOPFLOOR; i++) {
                if (i >= 1) {
                    arriveNum -= loseFloor[i - 1];
                    arriveArr[i] = arriveNum;
                }
                restr.append("deathFloor" + i + "," + loseFloor[i] + "," + gasif[i] + System.getProperty("line.separator"));
            }
            restr.append("gasi" + "," + gasi + System.getProperty("line.separator"));
            restr.append("useFood" + "," + useItemFood + System.getProperty("line.separator"));
            restr.append("usePotion" + "," + useItemPotion + System.getProperty("line.separator"));
            restr.append("useLStaff" + "," + useItemLStaff + System.getProperty("line.separator"));
            restr.append("useWStaff" + "," + useItemWStaff + System.getProperty("line.separator"));
            for (int i = 0; i < TOPFLOOR; i++) {
                if(lvFloorNum[i] != 0) restr.append("Level-ave" + i + "," + (lvFloor[i] / lvFloorNum[i]) + System.getProperty("line.separator"));
            }
            for (int i = 0; i < TOPFLOOR; i++) {
                restr.append(i + "farrive" + "," + arriveArr[i] + System.getProperty("line.separator"));
                restr.append("getFood" + "," + getItemFood[i] + System.getProperty("line.separator"));
                restr.append("getPotion" + "," + getItemPotion[i] + System.getProperty("line.separator"));
                restr.append("getLStaff" + "," + getItemLStaff[i] + System.getProperty("line.separator"));
                restr.append("getWStaff" + "," + getItemWstaff[i] + System.getProperty("line.separator"));
                int sum = getItemFood[i] + getItemPotion[i] + getItemLStaff[i] + getItemWstaff[i];
                restr.append("sum" + "," + sum + System.getProperty("line.separator"));
            }
            
            System.out.println(new String(restr));

            //
            Logger.OutputFileLog(new String(fileName + "_result.txt"), new String(restr));
            Logger.OutputFileLog(new String(fileName + "_result.csv"), new String(restr));
        }
        
        //</editor-fold>
    }
    
    // 階またいだ時の更新
    public void flrSetting(int debugInit){
        if (debugInit == 0) {
            // ランダム配置
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
                if (random.nextInt(2) == 0) randItemNum--;
                else                        randItemNum++;
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

            // 高速周回（jar用）の際に，ゲーム数及び勝率がわかりやすいように
            if (GAMEMODE == 2) {
                if(DEBUG_INIT != 0) Logger.appendLog(floorNumber + "F, " + gameCount + " game (win:" + winCount + ", lose:" + loseCount + ")", true);
                else                Logger.appendLog(floorNumber + "F, " + gameCount + " / " + TRYNUM + " game (win:" + winCount + ", lose:" + loseCount + ")", true);
            }

            // ログに現在の状態を出力，0F以外
            // ゲーム回数，階層数，プレイヤーの各アイテム数
        } else if (debugInit == 1) {
            // 固定配置
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
            //objectset.player.hp = objectset.player.maxHp;
            objectset.player.setHp(objectset.player.getMaxHp());
            objectset.player.satiety = objectset.player.maxSatiety;
            objectset.player.inventory.addItem(4); // wst追加
            objectset.player.inventory.addItem(4);
//                        objectset.player.inventory.addItem(3);
//                        objectset.player.inventory.addItem(2);
//                        objectset.player.inventory.addItem(1);
//                        objectset.player.inventory.addItem(4);
//                        objectset.player.inventory.addItem(3);
//                        objectset.player.inventory.addItem(2);
//                        objectset.player.inventory.addItem(1);
//                        objectset.player.inventory.addItem(4);
//                        objectset.player.inventory.addItem(3);
//                        objectset.player.inventory.addItem(2);
//                        objectset.player.inventory.addItem(1);
//                        objectset.player.inventory.addItem(4);
//                        objectset.player.inventory.addItem(3);
//                        objectset.player.inventory.addItem(2);
//                        objectset.player.inventory.addItem(1);
//                        objectset.player.inventory.addItem(4);
//                        objectset.player.inventory.addItem(3);
            //objectset.player.inventory.addItem(2);
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

            if (GAMEMODE == 1 || GAMEMODE == 3) {
                scene = SCENE_PAUSE;
            }

            // ログに現在の状態を出力，0F以外
            // ゲーム回数，階層数，プレイヤーの各アイテム数
        } else if (debugInit == 2) {
            // csvから，プレイヤ以外更新
            background.mapUpdate(); // マップの情報の更新
            objectset.setObjectRand(new String("player")); // プレイヤーの配置, プレイヤーの持つマップ情報の初期化・更新を含む
            objectset.initObjectsetExceptPlayer(); // オブジェクトの初期化
            // オブジェクトの配置
            // 敵
            for (int i = 0; i < ObjectSet.ENEMY_MAX; i++) {
                objectset.setObjectRand(new String("enemy"));
            }

            // (x, y, アイテム種類)
            // 1/2の確率で4個
            int randItemNum = ObjectSet.ITEM_MAX - 1;
            if (random.nextInt(2) == 0) {
                if (random.nextInt(2) == 0) randItemNum--;
                else                        randItemNum++;
            }
            for (int i = 0; i < randItemNum; i++) {
                objectset.setObjectRand(new String("item"));
            }

            objectset.setObjectRand(new String("stair")); // 階段, 通路の直前，部屋に入るとすぐのグリッドに生成されないように調整
            startDrawmapFlag = true; // マップ視野の描画を開始する
            startFlag = true; // 階層初回判定
        }
    }

    public void sysoutputCurrentRate() {
        if(DEBUG_INIT != 0) System.out.println(gameCount + " game (win:" + winCount + ", lose:" + loseCount + ")");
        else                System.out.println(gameCount + " / " + TRYNUM + " game (win:" + winCount + ", lose:" + loseCount + ")");
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
    
    // キャンバス内に必要なものをすべて描画
    public void drawAll() {
        // 描画タイミングをできるだけ近づける
        background.drawGameBG(gBuf, objectset.player);  // マップ描画(バッファをクリア)，プレイヤーの座標
        background.drawGridBG(gBuf);                    // マップに対してグリッド線の表示
        objectset.drawAllobject(gBuf);                  // ゲームオブジェクトの一括描画処理
        drawStatusBar(gBuf, objectset.player);          // ステータスバー描画
        drawInventory();                                // インベントリの描画
        drawMap();                                      // マップの描画
        
        if (objectset.isGameover() == true) title.drawGameover(gBuf);   // ゲームオーバー文字を表示
        else if (floorNumber == TOPFLOOR)   title.drawClear(gBuf);      // ゲームクリア文字表示
    }
    
    // インベントリの描画
    public void drawInventory() {
        int cItemNum = objectset.player.inventory.getInvItemNum(); // 現在の所持アイテム個数
        int maxItemNum = Inventory.MAX_INV; // 所持できる最大のアイテム数
        int oneItemHeight = (frameSizeY - (frameSizeY / maxItemNum)) / maxItemNum; // そのままのフレームサイズでは，はみ出る

        // インベントリのbg表示
        gBuf.setColor(Color.white);
        gBuf.fillRect(frameSizeX * 2 / 3, 0, frameSizeX / 3, frameSizeY);

        // frameSizeY 523がちょうど？
        //gBuf.setColor(Color.red);
        //gBuf.fillRect(frameSizeX/2, 0, frameSizeX/2, 1);
        //System.out.println(MAPCHIP_MAGY / 2 + SCREENSIZE_Y);
        
        // インベントリの中身の表示
        for (int i = 0; i < maxItemNum; i++) {
            // 所持しているアイテム欄
            if(i < cItemNum){
                // 欄の表示，未選択：グレー、選択中：オレンジ
                if (i == selectItem) {
                    gBuf.setColor(Color.ORANGE);
                    gBuf.fillRect(frameSizeX * 2 / 3, i * oneItemHeight, frameSizeX / 3, oneItemHeight - 1);
                } else {
                    gBuf.setColor(Color.gray);
                    gBuf.fillRect(frameSizeX * 2 / 3, i * oneItemHeight, frameSizeX / 3, oneItemHeight - 1);
                }

                // 該当アイテムの表示
                String iName = objectset.player.inventory.getInvItemName(i);
                int iUCount = objectset.player.inventory.getInvItemUsageCount(i);
                gBuf.setColor(Color.white);
                gBuf.setFont(new Font("Meiryo", Font.PLAIN, 15));
                gBuf.drawString(iName + " (" + iUCount + ")", frameSizeX * 2 / 3 + 10, i * oneItemHeight + oneItemHeight / 2);
            }
            // 所持していないアイテム欄
            else{
                gBuf.setColor(Color.gray);
                gBuf.fillRect(frameSizeX * 2 / 3, i * oneItemHeight, frameSizeX / 3, oneItemHeight - 1);
            }
        }
    }

    // ステータスの描画
    public void drawStatusBar(Graphics g, Player iplayer) {
        Font statusFont = new Font("Alial", Font.BOLD, 30);

        g.setColor(Color.white);
        g.setFont(statusFont);
        //g.drawString("hp : " + iplayer.hp + "/" + iplayer.maxHp, 30, 30);
        g.drawString("hp : " + iplayer.getHp() + "/" + iplayer.getMaxHp(), 30, 30);

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
        boolean stairflag; // 階段発見の有無
        int beatsCount; // 現在のフロアで倒した敵の数
        
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
        
        boolean updateFlag; // ファイルへ出力するか否か
        
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
            stairflag = false;
            beatsCount = 0;
            
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
            //hp = p.hp;
            hp = p.getHp();
            lv = p.level;
            sp = p.satiety + (p.inventory.getInvItemNum(1) * p.inventory.getFoodHealVal());
            pt = p.inventory.getInvItemNum(2);
            ar = p.inventory.getInvItemNum(3);
            st = p.inventory.getInvItemNum(4);
            
            stm = p.satiety;
            fd = p.inventory.getInvItemNum(1);
            exp = (double)p.exp / p.lvupExp;
            beatsCount = p.getBeatsEnemyCount(fnum);
            
            updateFlag = true;
        }
        
        public void setLData_everyTurn(int fnum, Player p, boolean[][] pmap, boolean stflag) {
            // アイテム数の変化や未知領域の割合など毎ターン更新する（必要ないかも）
            int provFd = p.inventory.getInvItemNum(1); // 現在の食料数
            int provPt = p.inventory.getInvItemNum(2);
            int provAr = p.inventory.getInvItemNum(3);
            int provSt = p.inventory.getInvItemNum(4);
            // 直前のターンの個数と異なるとき
            if(provFd != fd) {
                if(provFd == fd - 1) { // アイテム使用で1個減ってる
                    currentFlrUseFd++;
                } else if(provFd == fd + 1) { // アイテムゲットで1個増えている
                    currentFlrGetFd++;
                } else {
                    // デバッグモードでないとき
                    if(DEBUG_INIT == 0) System.out.println("ログ収集系統fd，何かしらのエラー:" + fd + "->" + provFd);
                }
                fd = provFd;
            }
            if(provPt != pt) {
                if(provPt == pt - 1) { // アイテム使用で1個減ってる
                    currentFlrUsePt++;
                } else if(provPt == pt + 1) { // アイテムゲットで1個増えている
                    currentFlrGetPt++;
                } else {
                    // デバッグモードでないとき
                    if(DEBUG_INIT == 0) System.out.println("ログ収集系統pt，何かしらのエラー:" + pt + "->" + provPt);
                }
                pt = provPt;
            }
            if(provAr != ar) {
                if(provAr == ar - 1) { // アイテム使用で1個減ってる
                    currentFlrUseAr++;
                } else if(provAr == ar + 3) { // アイテムゲットで"3個"増えている
                    currentFlrGetAr += 3;
                } else {
                    // デバッグモードでないとき
                    if(DEBUG_INIT == 0) System.out.println("ログ収集系統ar，何かしらのエラー:" + ar + "->" + provAr);
                }
                ar = provAr;
            }
            if(provSt != st) {
                if(provSt == st - 1) { // アイテム使用で1個減ってる
                    currentFlrUseSt++;
                } else if(provSt == st + 1) { // アイテムゲットで1個増えている
                    currentFlrGetSt++;
                } else {
                    // デバッグモードでないとき
                    if(DEBUG_INIT == 0) System.out.println("ログ収集系統st，何かしらのエラー:" + st + "->" + provSt);
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
            
            stairflag = stflag;
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
                    + "currentFlrGetFd,currentFlrGetPt,currentFlrGetAr,currentFlrGetSt,"
                    + "beatsCount,stairFlag"
                    + System.getProperty("line.separator"));

            Logger.OutputFileLog(new String(fileName + ".csv"), str, true);
        }

        public void csvFileOutput(String fn) {
            StringBuilder data = new StringBuilder();
            data.append(fl + "," + hp + "," + lv + "," + sp + "," + pt + "," + ar + "," + st + ", " + (label == true ? 1 : -1) + ", " + (nextFlabel == true ? 1 : -1)
                    + "," + stm + "," + fd + "," + exp + "," + unknownAreaPer
                    + "," + currentFlrUseFd + "," + currentFlrUsePt + "," + currentFlrUseAr + "," + currentFlrUseSt
                    + "," + currentFlrGetFd + "," + currentFlrGetPt + "," + currentFlrGetAr + "," + currentFlrGetSt
                    + "," + beatsCount + "," + ((stairflag == true) ? 1 : -1)
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
            //hp = p.hp;
            hp = p.getHp();
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
