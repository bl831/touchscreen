package gov.lbl.als.bl831;

/**
 * Establishes a link between button functions and the coordinates those
 * functions should trigger.
 */
public enum VirtualButton {

    Center(650, 600, "center"), Clean(70, 81, "clean"), Plus10(680, 230, "+10deg"), Plus90(
            680, 300, "+90deg"), HalfTurn(680, 380, "180deg"), Minus90(680, 460, "-90deg"), Minus10(
            680, 540, "-10deg"), ZoomIn(280, 600, "zoomin"), ZoomOut(600, 600, "zoomout"), MoveOut(
            400, 620, "out"), MoveIn(460, 620, "in"), MoveUp(440, 600, "up"), MoveDown(440,
            650, "down"), MoveLeft(360, 620, "left"), MoveRight(500, 620, "right"), Pin24mm(
            250, 180, "24mm"), Pin21mm(300, 180, "21mm"), Pin20mm(400, 180, "20mm"), Pin18mm(
            450, 180, "18mm"), Pin14mm(520, 180, "14mm"), Pin12mm(580, 180, "12mm"), Pin10mm(
            640, 180, "10mm"), Undo(180, 220, "undo"), Park(180, 300, "park"), UnPark(180,
            340, "unpark"), Polarizer(180, 620, "polarizer");

    private final int x;
    private final int y;
    private final String symbol;

    /**
     * Creates a new Enum constant.
     * 
     * @param x
     *        the X coordinate that should be used when this button type is
     *        used.
     * @param y
     *        the Y coordinate that should be used when this button type is
     *        used.
     * @param symbol
     *        the name of the symbol to emit.
     */
    VirtualButton(int x, int y, String symbol) {
        this.x = x;
        this.y = y;
        this.symbol = symbol;
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
}
