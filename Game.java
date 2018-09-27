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
	public static void main(String args[])
	{
		int dLevel = 1; // フレーム（出力）の有無の決定，0:フレーム出力なし，その他:出力あり
                new Game(dLevel);
	}

        public Game(int drawLv)
        {
                // フレーム出力あり，入力受付あり
                if(drawLv != 0)
                {
                    MyCanvas mc; // キャンバス，メインループ
                    int frameSizeX = 1024;
                    int frameSizeY = 900;
                    
                    
                    //<editor-fold defaultstate="collapsed" desc="レイアウトの設定">
                    GridBagLayout layout;
                    GridBagConstraints gbc;
                    JPanel p;
                    gbc = new GridBagConstraints();

                    layout = new GridBagLayout();
                    p = new JPanel();
                    p.setLayout(layout);
                    //</editor-fold>
		
                
                    //<editor-fold defaultstate="collapsed" desc="メニューバーの設定">
                    JMenuBar menubar;
                    JMenu menu1;
                    JMenu menu2;
                    JMenu menu3;
                    JMenu menu4;
                    JMenuItem menuitem1;
                    JMenuItem menuitem2;
                    JMenuItem menuitem3;

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
                    //</editor-fold>

                
                    //<editor-fold defaultstate="collapsed" desc="キャンバスの設定">
                    // キャンパスの作成(周囲視野)
                    int oplv = 0; // 0:表示なし，10:表示あり
                    mc = new MyCanvas(frameSizeX - 250, frameSizeY, oplv);

                    // 以下，抜くと描画が不可？サイズの変化が原因か
                    mc.setPreferredSize(new Dimension(frameSizeX - 250, frameSizeY)); // 適切なサイズの設定
                    mc.setMinimumSize(new Dimension(frameSizeX - 250, frameSizeY)); // 最小サイズの設定
                    mc.setMaximumSize(new Dimension(frameSizeX - 250, frameSizeY)); // 最大サイズの設定
                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    gbc.gridheight = 2;
                    gbc.weightx = 1.0d;
                    gbc.weighty = 1.0d;
                    gbc.fill = GridBagConstraints.BOTH;
                    layout.setConstraints(mc, gbc);
                    //</editor-fold>

                
                    //<editor-fold defaultstate="collapsed" desc="テキストエリア（ログ用）の設定">
                    JScrollPane scrollpane;
                    Logger.logText = new JTextArea();
                    Logger.logText.append("*-- log --*\n");
                    scrollpane = new JScrollPane();
                    scrollpane.setPreferredSize(new Dimension(250, 800));
                    scrollpane.setMinimumSize(new Dimension(250, 800));
                    Logger.logText.setEditable(false);
                    Logger.logText.setLineWrap(true); // 折り返しアリ

                    Logger.view = scrollpane.getViewport();
                    Logger.view.setView(Logger.logText);

                    // 初期値
                    Logger.view.setViewPosition(new Point(0,0));

                    gbc.gridx = 1;
                    gbc.gridy = 0;
                    gbc.gridheight = 2;
                    gbc.weightx = 1.0d;
                    gbc.weighty = 1.0d;
                    gbc.fill = GridBagConstraints.VERTICAL;
                    layout.setConstraints(scrollpane, gbc);
                    //</editor-fold>
                
                    
                    //<editor-fold defaultstate="collapsed" desc="Jフレームの作成">
                    JFrame frame;
                    frame = new JFrame();
                
                    frame.setSize(frameSizeX, frameSizeY); // ウィンドウのサイズ，実際のサイズはもう少し小さい
                    frame.setResizable(false); // サイズ変更不可
                    frame.setTitle("RogeLike(仮)"); // タイトル
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // クローズ

                    // パネルへの追加
                    p.add(scrollpane); // ログ
                    p.add(mc); // メイン
                    frame.getContentPane().add(p, BorderLayout.CENTER);
                    frame.setJMenuBar(menubar); // フレームにメニューバーを追加
                    
                    frame.setVisible(true); // ウィンドウの表示
                    //</editor-fold>
                    
                    
                    mc.init(); // ゲームデータの初期化
                    mc.run(); // ゲームの開始
                }
                // フレーム出力なし，入力受付なし
                else
                {
                    NoCanvas nc = new NoCanvas(0); // 初期化
                    
                    nc.init(); // 初期化
                    nc.run(); // ゲームの開始
                }
        }
        
	
}

