import java.awt.Canvas;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;


public abstract class LoadImg extends Canvas
{
	// オブジェクトの持つ外見イメージ
	private BufferedImage img;

	// イメージのロード
	public BufferedImage loadImage(String name)
	{
		try{
			FileInputStream in = new FileInputStream(name);// FileInputStream
			BufferedImage rv = ImageIO.read(in);//
			in.close();//
                        img = rv;
			return rv;//
		}catch(IOException e){
			System.out.println("Err e=" + e);//
                        Logger.appendLog("Err e=" + e);
			return null;// null
		}
	}
        
        public BufferedImage getImg(){
            return img;
        }
        public void setImg(BufferedImage img){
            this.img = img;
        }
}