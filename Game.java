import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;

public class Game extends JFrame
{
	// サブマップ
	//public static CanvasMap canvm;

	// ログ
	private static JTextArea text;
        private JScrollPane scrollpane;
	private static JViewport view;
        
        // キャンバス
        // メインループ
        private MyCanvas mc;
        
	public static void main(String args[])
	{
		// フレーム（出力）の有無の決定
                // 0:フレーム出力なし，その他:出力あり
                int dLevel = 1; 
                new Game(dLevel);
	}

        public Game(int drawLv)
        {
                // フレーム出力あり
                // 入力受付あり
                if(drawLv != 0)
                {
                    // 0:表示なし
                    // 10:表示あり
                    int oplv = 10;
                    // フレームの作成
                    // 出力の有無の選択など
                    createGameFrame(oplv);
                    // ゲームの開始（スレッドの稼働）
                    mc.init(); // ゲームデータの初期化
                    mc.initThread(); // スレッドを作成
                }
                // フレーム出力なし
                // 入力受付なし
                else
                {
                    // 初期化
                    NoCanvas nc = new NoCanvas(0);
                    // 初期化
                    nc.init();
                    // ゲームの開始
                    nc.run();
                }
        }
        
//        public void startGame(int oplv)
//        {
//		mc.init(); // ゲームデータの初期化
//		mc.initThread(); // スレッドを作成
//        }
        
	// コンストラクタ
	public void createGameFrame(int oplv)
	{
		JFrame frame;

                int fsizeX;
                int fsizeY;

                // テキスト表示用の欄を設ける
                GridBagLayout layout;
                JPanel p;
		
                GridBagConstraints gbc;

                JMenuBar menubar;
                JMenu menu1;
                JMenu menu2;
                JMenu menu3;
                JMenu menu4;
                JMenuItem menuitem1;
                JMenuItem menuitem2;
                JMenuItem menuitem3;
                
                frame = new JFrame();

		fsizeX = 1024;
		fsizeY = 900;

		// テキスト表示用の欄を設ける
		layout = new GridBagLayout();
		p = new JPanel();
		p.setLayout(layout);

		gbc = new GridBagConstraints();

		menubar = new JMenuBar();
                menu1 = new JMenu("File");
                menu2 = new JMenu("Menu");
                menu3 = new JMenu("Setting");
                menu4 = new JMenu("Option");
                menubar.add(menu1);
                menubar.add(menu2);
                menubar.add(menu3);
                menubar.add(menu4);
                menuitem1 = new JMenuItem("New");
                menuitem2 = new JMenuItem("Open");
                menuitem3 = new JMenuItem("Close");
                menu1.add(menuitem1);
                menu1.add(menuitem2);
                menu1.add(menuitem3);
                // フレームにメニューバーを追加
                frame.setJMenuBar(menubar);

                
                //<editor-fold defaultstate="collapsed" desc="プレイヤビューの設定">
		// キャンパスの作成(周囲視野)
		mc = new MyCanvas(fsizeX - 250, fsizeY, oplv);
                
		// 以下，抜くと描画が不可？サイズの変化が原因か
		mc.setPreferredSize(new Dimension(fsizeX - 250, fsizeY)); // 適切なサイズの設定
		mc.setMinimumSize(new Dimension(fsizeX - 250, fsizeY)); // 最小サイズの設定
		mc.setMaximumSize(new Dimension(fsizeX - 250, fsizeY)); // 最大サイズの設定
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		gbc.weightx = 1.0d;
		gbc.weighty = 1.0d;
                gbc.fill = GridBagConstraints.BOTH;
		layout.setConstraints(mc, gbc);
                //</editor-fold>

                //<editor-fold defaultstate="collapsed" desc="テキストエリア（ログ用）の設定">
		text = new JTextArea();
		text.append("*-- log --*\n");
		scrollpane = new JScrollPane();
		scrollpane.setPreferredSize(new Dimension(250, 800));
		scrollpane.setMinimumSize(new Dimension(250, 800));
		text.setEditable(false);
		text.setLineWrap(true); // 折り返しアリ

		view = scrollpane.getViewport();
		view.setView(text);

		// 初期値
		view.setViewPosition(new Point(0,0));

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		gbc.weightx = 1.0d;
		gbc.weighty = 1.0d;
                gbc.fill = GridBagConstraints.VERTICAL;
		layout.setConstraints(scrollpane, gbc);
                //</editor-fold>
                
                // パネルに追加
		p.add(scrollpane);
		p.add(mc);

		// 実際のサイズはもう少し小さい
		frame.setSize(fsizeX, fsizeY); // ウィンドウのサイズ
		frame.setResizable(false); // サイズ変更不可
		frame.setTitle("RogeLike(仮)"); // タイトル

		// クローズ
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().add(p, BorderLayout.CENTER);
		frame.setVisible(true); // ウィンドウの表示
        }
        
	// ログの更新
	public static void appendLog(String str)
	{
		// 末尾に str を追加
		text.append(str + "\n");
		// 16:1行当たりのおおよその高さ
		int textPosY = text.getPreferredSize().height - 200;
		view.setViewPosition(new Point(0, textPosY >= 0 ? textPosY : 0));
	}

	public static String getLog()
	{
		return text.getText();
	}
        
        public static void initLog()
	{
		text.setText("");
	}
}

