package gov.lbl.als.bl831;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class CrystalCenteringPanel extends JPanel {

    private VideoWidget      mVideoWidget;
    private JPanel           mLeftPanel;
    private JPanel           mRightPanel;
    private JPanel           mAnglePanel;
    private JPanel           mControlsPanel;
    private JPanel           mArrowPanel;
    private JPanel           mPinPanel;
    private JPanel           mLightPanel;
    private JButton          mLightSliderButton;
    private final ClickSink  mClickSink;
    private final Config     mConfig;

    /**
     * @param clickSink
     *        where button clicks go.
     */
    public CrystalCenteringPanel(ClickSink clickSink, Config config) {
        mClickSink = clickSink;
        mConfig = config;
        initComponents();
    }

    /**
     * Creates a JButton for the given VirtualButton. If an icon override
     * exists in the config, uses that icon at native resolution. If the
     * button is a CrystalButton style, creates an HTML-styled text button.
     * Otherwise loads the default icon from jar resources. The button is
     * set invisible if marked hidden in config.
     */
    private JButton createButton(VirtualButton virtualButton) {
        ButtonConfig buttonConfig = mConfig.getButtonOverrides().get(virtualButton);

        boolean hidden = buttonConfig != null && buttonConfig.isHidden();

        JButton button;
        if (buttonConfig != null && buttonConfig.getIconPath() != null) {
            button = new JButton(new ImageIcon(buttonConfig.getIconPath()));
            if (buttonConfig.getPressedIconPath() != null) {
                button.setPressedIcon(
                        new ImageIcon(buttonConfig.getPressedIconPath()));
            }
        } else if (virtualButton.isCrystalButton()) {
            button = new CrystalButton(virtualButton.getLabel(),
                    virtualButton.getWidth(), virtualButton.getHeight(),
                    virtualButton.getRed(), virtualButton.getGreen(),
                    virtualButton.getBlue());
        } else {
            button = new JButton(new ImageIcon(getClass()
                    .getResource(virtualButton.getIconResource())));
        }

        if (mConfig.isBorderlessButtons()) {
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
        }

        if (hidden) {
            button.setVisible(false);
        } else {
            button.addActionListener(e -> buttonPressed(virtualButton));
        }

        return button;
    }

    private Insets defaultInsets() {
        return mConfig.isBorderlessButtons()
                ? new Insets(2, 2, 2, 2)
                : new Insets(5, 5, 5, 5);
    }

    private Insets wideInsets() {
        return mConfig.isBorderlessButtons()
                ? new Insets(11, 2, 11, 2)
                : new Insets(15, 5, 15, 5);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        Color bg = mConfig.getBackground();
        setBackground(bg != null ? bg : Color.white);
        Insets insets = defaultInsets();
        add(getPinPanel(), new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, insets, 0, 0));
        add(getLeftPanel(), new GridBagConstraints(0, 1, 1, 1, 0, 1,
                GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, insets, 0, 0));
        Insets videoInsets = new Insets(insets.top, insets.left,
                insets.bottom + 15, insets.right);
        add(getVideoWidget(), new GridBagConstraints(1, 1, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, videoInsets, 0, 0));
        add(getRightPanel(), new GridBagConstraints(2, 1, 1, 1, 0, 1,
                GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, insets, 0, 0));
        setSize(1013, 480);
    }

    private JPanel getLeftPanel() {
        if (mLeftPanel == null) {
            mLeftPanel = new JPanel(new GridBagLayout());
            mLeftPanel.setOpaque(false);
            Insets insets = defaultInsets();
            mLeftPanel.add(getLightPanel(), new GridBagConstraints(0, 0, 1, 1, 0, 0,
                    GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.VERTICAL, insets,
                    0, 0));
            mLeftPanel.add(getControlsPanel(), new GridBagConstraints(0, 1, 1, 1, 0, 0,
                    GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.VERTICAL, insets,
                    0, 0));
        }

        return mLeftPanel;
    }

    private JPanel getLightPanel() {
        if (mLightPanel == null) {
            mLightPanel = new JPanel(new GridBagLayout());
            mLightPanel.setOpaque(false);

            Insets insets = defaultInsets();
            mLightPanel.add(createButton(VirtualButton.Undo),
                    new GridBagConstraints(0, 0, 1, 1, 0, 0,
                    GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, insets, 0,
                    0));
            mLightPanel.add(createButton(VirtualButton.Park),
                    new GridBagConstraints(0, 1, 1, 1, 0, 0,
                    GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, insets, 0,
                    0));
            mLightPanel.add(createButton(VirtualButton.UnPark),
                    new GridBagConstraints(0, 2, 1, 1, 0, 0,
                    GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, insets, 0,
                    0));
            mLightPanel.add(getLightSliderButton(),
                    new GridBagConstraints(0, 3, 1, 1, 0, 0,
                    GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, insets,
                    0, 0));
            mLightPanel.add(createButton(VirtualButton.Polarizer),
                    new GridBagConstraints(0, 4, 1, 1, 0, 0,
                    GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, insets, 0,
                    0));
        }

        return mLightPanel;
    }

    private JButton getLightSliderButton() {
        if (mLightSliderButton == null) {
            ButtonConfig buttonConfig = mConfig.getButtonOverrides()
                    .get(VirtualButton.Light);
            if (buttonConfig != null && buttonConfig.getIconPath() != null) {
                mLightSliderButton = new JButton(
                        new ImageIcon(buttonConfig.getIconPath()));
                if (buttonConfig.getPressedIconPath() != null) {
                    mLightSliderButton.setPressedIcon(
                            new ImageIcon(buttonConfig.getPressedIconPath()));
                }
            } else {
                mLightSliderButton = new JButton(new ImageIcon(getClass()
                        .getResource(VirtualButton.Light.getIconResource())));
            }
            if (mConfig.isBorderlessButtons()) {
                mLightSliderButton.setBorderPainted(false);
                mLightSliderButton.setFocusPainted(false);
                mLightSliderButton.setContentAreaFilled(false);
            }
            mLightSliderButton.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    double pos = (double) e.getY()
                                 / (double) mLightSliderButton.getSize().height;
                    ButtonConfig buttonConfig = mConfig.getButtonOverrides()
                            .get(VirtualButton.Light);
                    if (buttonConfig != null && buttonConfig.getCommand() != null) {
                        String command = buttonConfig.getCommand()
                                .replace("${value}",
                                        String.format("%g", 1.0 - pos));
                        mClickSink.sendCommand(command);
                    } else {
                        mClickSink.lightSlideClicked(pos);
                    }
                }
            });
        }

        return mLightSliderButton;
    }

    private JPanel getPinPanel() {
        if (mPinPanel == null) {
            mPinPanel = new JPanel(new GridBagLayout());
            mPinPanel.setOpaque(false);

            Insets insets = defaultInsets();
            mPinPanel.add(createButton(VirtualButton.Pin24mm),
                    new GridBagConstraints(0, 0, 1, 1, 1, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mPinPanel.add(createButton(VirtualButton.Pin21mm),
                    new GridBagConstraints(1, 0, 1, 1, 1, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mPinPanel.add(createButton(VirtualButton.Pin20mm),
                    new GridBagConstraints(2, 0, 1, 1, 1, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mPinPanel.add(createButton(VirtualButton.Pin18mm),
                    new GridBagConstraints(3, 0, 1, 1, 1, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mPinPanel.add(createButton(VirtualButton.Pin14mm),
                    new GridBagConstraints(4, 0, 1, 1, 1, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mPinPanel.add(createButton(VirtualButton.Pin12mm),
                    new GridBagConstraints(5, 0, 1, 1, 1, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mPinPanel.add(createButton(VirtualButton.Pin10mm),
                    new GridBagConstraints(6, 0, 1, 1, 1, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
        }

        return mPinPanel;
    }

    private JPanel getRightPanel() {
        if (mRightPanel == null) {
            mRightPanel = new JPanel(new GridBagLayout());
            mRightPanel.setOpaque(false);

            Insets insets = defaultInsets();
            mRightPanel.add(getAnglePanel(), new GridBagConstraints(0, 0, 1, 1, 0, 1,
                    GridBagConstraints.BASELINE_LEADING, GridBagConstraints.VERTICAL, insets,
                    0, 0));
        }

        return mRightPanel;
    }

    private JPanel getAnglePanel() {
        if (mAnglePanel == null) {
            mAnglePanel = new JPanel(new GridBagLayout());
            Insets insets = wideInsets();
            mAnglePanel.setOpaque(false);
            mAnglePanel.add(createButton(VirtualButton.ZoomIn),
                    new GridBagConstraints(0, 0, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mAnglePanel.add(createButton(VirtualButton.Plus10),
                    new GridBagConstraints(0, 1, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mAnglePanel.add(createButton(VirtualButton.Plus90),
                    new GridBagConstraints(0, 2, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mAnglePanel.add(createButton(VirtualButton.HalfTurn),
                    new GridBagConstraints(0, 3, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mAnglePanel.add(createButton(VirtualButton.Minus90),
                    new GridBagConstraints(0, 4, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mAnglePanel.add(createButton(VirtualButton.Minus10),
                    new GridBagConstraints(0, 5, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mAnglePanel.add(createButton(VirtualButton.ZoomOut),
                    new GridBagConstraints(0, 6, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
        }
        return mAnglePanel;
    }

    private JPanel getControlsPanel() {
        if (mControlsPanel == null) {
            mControlsPanel = new JPanel(new GridBagLayout());
            mControlsPanel.setOpaque(false);
            Insets insets = defaultInsets();
            mControlsPanel.add(getArrowPanel(), new GridBagConstraints(0, 2, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
        }
        return mControlsPanel;
    }

    private JPanel getArrowPanel() {
        if (mArrowPanel == null) {
            mArrowPanel = new JPanel(new GridBagLayout());
            mArrowPanel.setOpaque(false);

            Insets insets = defaultInsets();
            mArrowPanel.add(createButton(VirtualButton.MoveUp),
                    new GridBagConstraints(0, 0, 2, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mArrowPanel.add(createButton(VirtualButton.MoveLeft),
                    new GridBagConstraints(0, 1, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mArrowPanel.add(createButton(VirtualButton.MoveRight),
                    new GridBagConstraints(1, 1, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mArrowPanel.add(createButton(VirtualButton.MoveDown),
                    new GridBagConstraints(0, 2, 2, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mArrowPanel.add(createButton(VirtualButton.MoveIn),
                    new GridBagConstraints(0, 3, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mArrowPanel.add(createButton(VirtualButton.MoveOut),
                    new GridBagConstraints(1, 3, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
        }

        return mArrowPanel;
    }

    /**
     * Called when any of the buttons in the UI get clicked. This will trigger a
     * virtual button click by sending "finger simulation events" to our output.
     *
     * @param button
     *        the button that was clicked.
     */
    /**
     * Called when any of the buttons in the UI get clicked. If a command
     * override exists in the config, sends that command. Otherwise sends
     * the default virtual button press.
     *
     * @param virtualButton
     *        the button that was clicked.
     */
    protected void buttonPressed(VirtualButton virtualButton) {
        ButtonConfig buttonConfig = mConfig.getButtonOverrides().get(virtualButton);
        if (buttonConfig != null && buttonConfig.getCommand() != null) {
            mClickSink.sendCommand(buttonConfig.getCommand());
        } else {
            mClickSink.buttonPressed(virtualButton);
        }
    }

    /**
     * @return Creates or gets the widget that displays the video stream.
     */
    VideoWidget getVideoWidget() {
        if (mVideoWidget == null) {
            mVideoWidget = new VideoWidget(mClickSink, mConfig);
            Color bg = mConfig.getBackground();
            mVideoWidget.setBackground(bg != null ? bg : Color.white);
        }

        return mVideoWidget;
    }

}
