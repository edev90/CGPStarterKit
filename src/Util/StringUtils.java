package Util;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public final class StringUtils {

	public static boolean isEmpty(String s) {
		return s == null || s.trim().equals("");
	}
	
	public static int getStringWidth(Graphics2D g2d, String s, Font font) {
		g2d.setFont(font);
		FontMetrics fm = g2d.getFontMetrics(font);
		return fm.stringWidth(s);
	}

}
