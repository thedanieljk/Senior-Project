package logic;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Mask {
	public boolean[][] mask;
	public ArrayList<BufferedImage> imgs;
	public Mask(boolean[][] mask, ArrayList <BufferedImage> imgs) {
		this.mask = mask;
		this.imgs = imgs;
	}

}
