
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import javax.swing.JTextArea;
import javax.swing.JViewport;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Ikeda-Lab15
 */
public class Logger {

    public static JTextArea logText; // ログ中のテキスト
    public static JViewport view; // ログのスクロール部分？

    private Logger() {

    }

    public static void OutputFileLog(String name, String str) {
        try {
            File file = new File(name);

            if (checkBeforeWritefile(file)) {
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
                pw.print(str);
                pw.close();
            } else {
                System.out.println("ファイルに書き込めません");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void OutputFileLog(String name, String str, boolean tf) {
        try {
            File file = new File(name);

            if (checkBeforeWritefile(file)) {
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, tf)));
                pw.print(str);
                pw.close();
            } else {
                System.out.println("ファイルに書き込めません");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static boolean checkBeforeWritefile(File file) {
        if (file.exists()) {
            if (file.isFile() && file.canWrite()) {
                return true;
            }
        } else {
            try {
                file.createNewFile();
                return true;
            } catch (IOException e) {
                System.out.println(e);
            }
        }

        return false;
    }

    // lebel,logの初期化
    public void initLog(int level) {
        //this.LogLevel = level;
        //this.logstr = new StringBuilder();
    }

    // ログの更新
    public static void appendLog(String str) {
        //logText.append(str + "\n"); // 末尾に str を追加
        //int textPosY = logText.getPreferredSize().height - 200; // 16:1行当たりのおおよその高さ
        //view.setViewPosition(new Point(0, textPosY >= 0 ? textPosY : 0));
    }

    public static String getLog() {
        return logText.getText();
    }

    public static void initLog() {
        logText.setText("");
    }
}
