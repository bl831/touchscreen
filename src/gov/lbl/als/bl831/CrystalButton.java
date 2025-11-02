package gov.lbl.als.bl831;

import javax.swing.JButton;

public class CrystalButton extends JButton {

    public CrystalButton(String label, int width, int height, int red, int green, int blue) {
        String fontColor;
        if (red + green + blue > 400)
            fontColor = "black";
        else
            fontColor = "white";
        String text = String
                .format("<html><table><tr><td style=\"background-color:%02x%02x%02x;color:%s;width:%dpx;height:%dpx;font-weight:bold;font-size:14px;text-align:center\">%s</td></tr></table>",
                        red, green, blue, fontColor, width, height, label);
        setText(text);
    }
}
