package gov.lbl.als.bl831;

/**
 * Establishes a link between button functions and the coordinates those
 * functions should trigger. Also carries display information for button
 * creation: either a CrystalButton label with colors, or an icon resource
 * path from the jar.
 */
public enum VirtualButton {

    //
    // CrystalButton style (label, width, height, r, g, b)
    //
    ZoomIn(680, 230, "zoomin", "High Mag", 95, 60, 59, 137, 201),
    ZoomOut(600, 600, "zoomout", "Low Mag", 95, 60, 237, 28, 36),
    Plus10(680, 230, "+10deg", "+10", 95, 60, 246, 151, 143),
    Plus90(680, 300, "+90deg", "+90", 95, 60, 68, 199, 244),
    HalfTurn(680, 380, "180deg", "180", 95, 60, 247, 147, 29),
    Minus90(680, 460, "-90deg", "-90", 95, 60, 68, 200, 245),
    Minus10(680, 540, "-10deg", "-10", 95, 60, 246, 151, 143),
    Undo(180, 220, "undo", "Undo", 95, 40, 0, 114, 188),
    Park(180, 300, "park", "Park", 95, 40, 237, 28, 36),
    UnPark(180, 340, "unpark", "Un-Park", 95, 40, 13, 178, 76),
    Polarizer(180, 620, "polarizer", "Polarizer", 95, 40, 144, 64, 152),

    //
    // Icon style (jar resource path)
    //
    Pin24mm(250, 180, "24mm", "/icons/24.png"),
    Pin21mm(300, 180, "21mm", "/icons/21.png"),
    Pin20mm(400, 180, "20mm", "/icons/20.png"),
    Pin18mm(450, 180, "18mm", "/icons/18.png"),
    Pin14mm(520, 180, "14mm", "/icons/14.png"),
    Pin12mm(580, 180, "12mm", "/icons/12.png"),
    Pin10mm(640, 180, "10mm", "/icons/10.png"),
    MoveUp(440, 600, "up", "/icons/up.png"),
    MoveDown(440, 650, "down", "/icons/down.png"),
    MoveLeft(360, 620, "left", "/icons/left.png"),
    MoveRight(500, 620, "right", "/icons/right.png"),
    MoveIn(460, 620, "in", "/icons/forward.png"),
    MoveOut(400, 620, "out", "/icons/backward.png"),
    Light(180, 370, "light", "/icons/light.png"),

    //
    // No button — triggered by other interactions
    //
    Center(650, 600, "center"),
    Clean(70, 81, "clean");

    private final int     x;
    private final int     y;
    private final String  symbol;
    private final boolean crystalButton;
    private final String  label;
    private final int     width;
    private final int     height;
    private final int     red;
    private final int     green;
    private final int     blue;
    private final String  iconResource;

    /**
     * CrystalButton style — text label with background color.
     */
    VirtualButton(int x, int y, String symbol, String label,
                  int width, int height, int red, int green, int blue) {
        this.x = x;
        this.y = y;
        this.symbol = symbol;
        this.crystalButton = true;
        this.label = label;
        this.width = width;
        this.height = height;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.iconResource = null;
    }

    /**
     * Icon style — image from jar resources.
     */
    VirtualButton(int x, int y, String symbol, String iconResource) {
        this.x = x;
        this.y = y;
        this.symbol = symbol;
        this.crystalButton = false;
        this.label = null;
        this.width = 0;
        this.height = 0;
        this.red = 0;
        this.green = 0;
        this.blue = 0;
        this.iconResource = iconResource;
    }

    /**
     * No-button style — triggered by other interactions (video click, etc.)
     */
    VirtualButton(int x, int y, String symbol) {
        this.x = x;
        this.y = y;
        this.symbol = symbol;
        this.crystalButton = false;
        this.label = null;
        this.width = 0;
        this.height = 0;
        this.red = 0;
        this.green = 0;
        this.blue = 0;
        this.iconResource = null;
    }

    /**
     * @return the X coordinate.
     */
    public int x() {
        return x;
    }

    /**
     * @return the Y coordinate.
     */
    public int y() {
        return y;
    }

    /**
     * @return the symbolic name.
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @return <code>true</code> if this button uses CrystalButton style.
     */
    public boolean isCrystalButton() {
        return crystalButton;
    }

    /**
     * @return the button label, or null for icon-style buttons.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the button width for CrystalButton style.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the button height for CrystalButton style.
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the red color component for CrystalButton style.
     */
    public int getRed() {
        return red;
    }

    /**
     * @return the green color component for CrystalButton style.
     */
    public int getGreen() {
        return green;
    }

    /**
     * @return the blue color component for CrystalButton style.
     */
    public int getBlue() {
        return blue;
    }

    /**
     * @return the jar resource path for the icon, or null for CrystalButton
     *         style or no-button entries.
     */
    public String getIconResource() {
        return iconResource;
    }
}
