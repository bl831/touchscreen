package gov.lbl.als.bl831;

/**
 * Abstracts the action of clicking a button from any details of how it needs to
 * be handled.
 */
public interface ClickSink {

    /**
     * A virtual button has been clicked.
     * 
     * @param button
     *        the button that was clicked.
     */
    void buttonPressed(VirtualButton button);

    /**
     * The "light" vertical slide has been pressed somewhere.
     * 
     * @param value
     *        the vertical value of the place where the click took place,
     *        normalized to be in the range [0,1).
     */
    void lightSlideClicked(double value);

    /**
     * The video part of the interface has been clicked.
     * 
     * @param x
     *        X coordinate of the click, normalized to [0,1)
     * @param y
     *        Y coordinate of the click, normalized to [0,1)
     * @param heightOverWidth
     *        aspect ratio.
     */
    void videoClicked(double x, double y, double heightOverWidth);

    /**
     * let the server know the client is still there.
     */
    void heartbeat();

    /**
     * Creates a no-op ClickSink that discards all events. Used in simulate
     * mode when there is no DCSS connection.
     */
    static ClickSink noOp() {
        return new ClickSink() {
            @Override public void buttonPressed(VirtualButton button) {}
            @Override public void lightSlideClicked(double value) {}
            @Override public void videoClicked(double x, double y, double heightOverWidth) {}
            @Override public void heartbeat() {}
        };
    }

}
