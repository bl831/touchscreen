package gov.lbl.als.bl831;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class CrystalCenteringPanel extends JPanel {

    private static final int BUTTON_WIDTH        = 95;
    private static final int BUTTON_HEIGHT       = 60;
    private static final int LIGHT_BUTTON_WIDTH  = 95;
    private static final int LIGHT_BUTTON_HEIGHT = 40;
    private VideoWidget      mVideoWidget;
    private JPanel           mLeftPanel;
    private JPanel           mRightPanel;
    private JButton          mPin24Button;
    private JButton          mPin10Button;
    private JButton          mPin12Button;
    private JButton          mPin14Button;
    private JButton          mPin18Button;
    private JButton          mPin20Button;
    private JButton          mPin21Button;
    private JButton          mMinus10Button;
    private JButton          mPlus10Button;
    private JButton          mPlus90Button;
    private JButton          mHalfButton;
    private JButton          mMinus90Button;
    private JPanel           mAnglePanel;
    private JPanel           mControlsPanel;
    private JPanel           mArrowPanel;
    private JButton          mHighMagButton;
    private JButton          mLowMagButton;
    private JButton          mDownButton;
    private JButton          mRightButton;
    private JButton          mLeftButton;
    private JButton          mUpButton;
    private JButton          mPolarizerButton;
    private JButton          mForwardButton;
    private JButton          mBackwardButton;
    private JButton          mParkButton;
    private JButton          mUnParkButton;
    private JButton          mUndoButton;
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

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(Color.white);
        Insets insets = new Insets(5, 5, 5, 5);
        add(getPinPanel(), new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, insets, 0, 0));
        add(getLeftPanel(), new GridBagConstraints(0, 1, 1, 1, 0, 1,
                GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, insets, 0, 0));
        add(getVideoWidget(), new GridBagConstraints(1, 1, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
        add(getRightPanel(), new GridBagConstraints(2, 1, 1, 1, 0, 1,
                GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, insets, 0, 0));
        setSize(1013, 480);
    }

    private JPanel getLeftPanel() {
        if (mLeftPanel == null) {
            mLeftPanel = new JPanel(new GridBagLayout());
            mLeftPanel.setOpaque(false);
            Insets insets = new Insets(5, 5, 5, 5);
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

            Insets insets = new Insets(5, 5, 5, 5);
            mLightPanel.add(getUndoButton(), new GridBagConstraints(0, 0, 1, 1, 0, 0,
                    GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, insets, 0,
                    0));
            mLightPanel.add(getParkButton(), new GridBagConstraints(0, 1, 1, 1, 0, 0,
                    GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, insets, 0,
                    0));
            mLightPanel.add(getUnParkButton(), new GridBagConstraints(0, 2, 1, 1, 0, 0,
                    GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, insets, 0,
                    0));
            mLightPanel.add(getLightSliderButton(), new GridBagConstraints(0, 3, 1, 1, 0, 1,
                    GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.VERTICAL, insets,
                    0, 0));
            mLightPanel.add(getPolarizerButton(), new GridBagConstraints(0, 4, 1, 1, 0, 0,
                    GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, insets, 0,
                    0));
        }

        return mLightPanel;
    }

    private JButton getLightSliderButton() {
        if (mLightSliderButton == null) {
            mLightSliderButton = new JButton(new ImageIcon(getClass().getResource(
                    "/resource/light.png")));
            mLightSliderButton.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    double pos = (double) e.getY()
                                 / (double) mLightSliderButton.getSize().height;
                    mClickSink.lightSlideClicked(pos);
                }
            });
        }

        return mLightSliderButton;
    }

    private JPanel getPinPanel() {
        if (mPinPanel == null) {
            mPinPanel = new JPanel(new GridBagLayout());
            mPinPanel.setOpaque(false);

            Insets insets = new Insets(5, 5, 5, 5);
            mPinPanel.add(getPin24Button(), new GridBagConstraints(0, 0, 1, 1, 1, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mPinPanel.add(getPin21Button(), new GridBagConstraints(1, 0, 1, 1, 1, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mPinPanel.add(getPin20Button(), new GridBagConstraints(2, 0, 1, 1, 1, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mPinPanel.add(getPin18Button(), new GridBagConstraints(3, 0, 1, 1, 1, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mPinPanel.add(getPin14Button(), new GridBagConstraints(4, 0, 1, 1, 1, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mPinPanel.add(getPin12Button(), new GridBagConstraints(5, 0, 1, 1, 1, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mPinPanel.add(getPin10Button(), new GridBagConstraints(6, 0, 1, 1, 1, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
        }

        return mPinPanel;
    }

    private JButton getPin24Button() {
        if (mPin24Button == null) {
            mPin24Button = new JButton(new ImageIcon(getClass()
                    .getResource("/resource/24.png")));
            mPin24Button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.Pin24mm);
                }
            });
        }

        return mPin24Button;
    }

    private JButton getPin21Button() {
        if (mPin21Button == null) {
            mPin21Button = new JButton(new ImageIcon(getClass()
                    .getResource("/resource/21.png")));
            mPin21Button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.Pin21mm);
                }
            });
        }

        return mPin21Button;
    }

    private JButton getPin20Button() {
        if (mPin20Button == null) {
            mPin20Button = new JButton(new ImageIcon(getClass()
                    .getResource("/resource/20.png")));
            mPin20Button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.Pin20mm);
                }
            });
        }

        return mPin20Button;
    }

    private JButton getPin18Button() {
        if (mPin18Button == null) {
            mPin18Button = new JButton(new ImageIcon(getClass()
                    .getResource("/resource/18.png")));
            mPin18Button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.Pin18mm);
                }
            });
        }

        return mPin18Button;
    }

    private JButton getPin14Button() {
        if (mPin14Button == null) {
            mPin14Button = new JButton(new ImageIcon(getClass()
                    .getResource("/resource/14.png")));
            mPin14Button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.Pin14mm);
                }
            });
        }

        return mPin14Button;
    }

    private JButton getPin12Button() {
        if (mPin12Button == null) {
            mPin12Button = new JButton(new ImageIcon(getClass()
                    .getResource("/resource/12.png")));
            mPin12Button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.Pin12mm);
                }
            });
        }

        return mPin12Button;
    }

    private JButton getPin10Button() {
        if (mPin10Button == null) {
            mPin10Button = new JButton(new ImageIcon(getClass()
                    .getResource("/resource/10.png")));
            mPin10Button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.Pin10mm);
                }
            });
        }

        return mPin10Button;
    }

    private JPanel getRightPanel() {
        if (mRightPanel == null) {
            mRightPanel = new JPanel(new GridBagLayout());
            mRightPanel.setOpaque(false);

            Insets insets = new Insets(5, 5, 5, 5);
            mRightPanel.add(getAnglePanel(), new GridBagConstraints(0, 0, 1, 1, 0, 1,
                    GridBagConstraints.BASELINE_LEADING, GridBagConstraints.VERTICAL, insets,
                    0, 0));
            // mRightPanel.add(getControlsPanel(), new GridBagConstraints(1, 0,
            // 1, 1, 0, 1,
            // GridBagConstraints.BASELINE_LEADING, GridBagConstraints.VERTICAL,
            // insets,
            // 0, 0));
        }

        return mRightPanel;
    }

    private JPanel getAnglePanel() {
        if (mAnglePanel == null) {
            mAnglePanel = new JPanel(new GridBagLayout());
            Insets insets = new Insets(15, 5, 15, 5);
            mAnglePanel.setOpaque(false);
            mAnglePanel.add(getHighMagButton(), new GridBagConstraints(0, 0, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mAnglePanel.add(getPlus10Button(), new GridBagConstraints(0, 1, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mAnglePanel.add(getPlus90Button(), new GridBagConstraints(0, 2, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mAnglePanel.add(getHalfButton(), new GridBagConstraints(0, 3, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mAnglePanel.add(getMinus90Button(), new GridBagConstraints(0, 4, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mAnglePanel.add(getMinus10Button(), new GridBagConstraints(0, 5, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mAnglePanel.add(getLowMagButton(), new GridBagConstraints(0, 6, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
        }
        return mAnglePanel;
    }

    private JPanel getControlsPanel() {
        if (mControlsPanel == null) {
            mControlsPanel = new JPanel(new GridBagLayout());
            mControlsPanel.setOpaque(false);
            Insets insets = new Insets(5, 5, 5, 5);
            mControlsPanel.add(getArrowPanel(), new GridBagConstraints(0, 2, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
        }
        return mControlsPanel;
    }

    private JButton getHighMagButton() {
        if (mHighMagButton == null) {
            mHighMagButton = new CrystalButton("High Mag", BUTTON_WIDTH, BUTTON_HEIGHT, 59,
                    137, 201);
            mHighMagButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.ZoomIn);
                }
            });
        }

        return mHighMagButton;
    }

    private JButton getPolarizerButton() {
        if (mPolarizerButton == null) {
            mPolarizerButton = new CrystalButton("Polarizer", LIGHT_BUTTON_WIDTH,
                    LIGHT_BUTTON_HEIGHT, 144, 64, 152);
            mPolarizerButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.Polarizer);
                }
            });
        }

        return mPolarizerButton;
    }

    private JButton getForwardButton() {
        if (mForwardButton == null) {
            mForwardButton = new JButton(new ImageIcon(getClass().getResource(
                    "/resource/forward.png")));
            mForwardButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.MoveIn);
                }
            });
        }

        return mForwardButton;
    }

    private JButton getBackwardButton() {
        if (mBackwardButton == null) {
            mBackwardButton = new JButton(new ImageIcon(getClass().getResource(
                    "/resource/backward.png")));
            mBackwardButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.MoveOut);
                }
            });
        }

        return mBackwardButton;
    }

    /**
     * Called when any of the buttons in the UI get clicked. This will trigger a
     * virtual button click by sending "finger simulation events" to our output.
     * 
     * @param button
     *        the button that was clicked.
     */
    protected void buttonPressed(VirtualButton button) {
        mClickSink.buttonPressed(button);
    }

    private JButton getParkButton() {
        if (mParkButton == null) {
            mParkButton = new CrystalButton("Park", LIGHT_BUTTON_WIDTH, LIGHT_BUTTON_HEIGHT,
                    237, 28, 36);
            mParkButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.Park);
                }
            });
        }

        return mParkButton;
    }

    private JButton getUnParkButton() {
        if (mUnParkButton == null) {
            mUnParkButton = new CrystalButton("Un-Park", LIGHT_BUTTON_WIDTH,
                    LIGHT_BUTTON_HEIGHT, 13, 178, 76);
            mUnParkButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.UnPark);
                }
            });
        }

        return mUnParkButton;
    }

    private JButton getUndoButton() {
        if (mUndoButton == null) {
            mUndoButton = new CrystalButton("Undo", LIGHT_BUTTON_WIDTH, LIGHT_BUTTON_HEIGHT,
                    0, 114, 188);
            mUndoButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.Undo);
                }
            });
        }

        return mUndoButton;
    }

    private JButton getLowMagButton() {
        if (mLowMagButton == null) {
            mLowMagButton = new CrystalButton("Low Mag", BUTTON_WIDTH, BUTTON_HEIGHT, 237, 28,
                    36);
            mLowMagButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.ZoomOut);
                }
            });
        }

        return mLowMagButton;
    }

    private JPanel getArrowPanel() {
        if (mArrowPanel == null) {
            mArrowPanel = new JPanel(new GridBagLayout());
            mArrowPanel.setOpaque(false);

            Insets insets = new Insets(5, 5, 5, 5);
            mArrowPanel.add(getUpButton(), new GridBagConstraints(0, 0, 2, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mArrowPanel.add(getLeftButton(), new GridBagConstraints(0, 1, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mArrowPanel.add(getRightButton(), new GridBagConstraints(1, 1, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mArrowPanel.add(getDownButton(), new GridBagConstraints(0, 2, 2, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mArrowPanel.add(getForwardButton(), new GridBagConstraints(0, 3, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
            mArrowPanel.add(getBackwardButton(), new GridBagConstraints(1, 3, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
        }

        return mArrowPanel;
    }

    private JButton getUpButton() {
        if (mUpButton == null) {
            mUpButton = new JButton(new ImageIcon(getClass().getResource("/resource/up.png")));
            mUpButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.MoveUp);
                }
            });
        }

        return mUpButton;
    }

    private JButton getLeftButton() {
        if (mLeftButton == null) {
            mLeftButton = new JButton(new ImageIcon(getClass().getResource(
                    "/resource/left.png")));
            mLeftButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.MoveLeft);
                }
            });
        }

        return mLeftButton;
    }

    private JButton getRightButton() {
        if (mRightButton == null) {
            mRightButton = new JButton(new ImageIcon(getClass().getResource(
                    "/resource/right.png")));
            mRightButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.MoveRight);
                }
            });
        }

        return mRightButton;
    }

    private JButton getDownButton() {
        if (mDownButton == null) {
            mDownButton = new JButton(new ImageIcon(getClass().getResource(
                    "/resource/down.png")));
            mDownButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.MoveDown);
                }
            });
        }

        return mDownButton;
    }

    private JButton getPlus10Button() {
        if (mPlus10Button == null) {
            mPlus10Button = new CrystalButton("+10", BUTTON_WIDTH, BUTTON_HEIGHT, 246, 151,
                    143);
            mPlus10Button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.Plus10);
                }
            });
        }

        return mPlus10Button;
    }

    private JButton getPlus90Button() {
        if (mPlus90Button == null) {
            mPlus90Button = new CrystalButton("+90", BUTTON_WIDTH, BUTTON_HEIGHT, 68, 199, 244);
            mPlus90Button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.Plus90);
                }
            });
        }

        return mPlus90Button;
    }

    private JButton getHalfButton() {
        if (mHalfButton == null) {
            mHalfButton = new CrystalButton("180", BUTTON_WIDTH, BUTTON_HEIGHT, 247, 147, 29);
            mHalfButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.HalfTurn);
                }
            });
        }

        return mHalfButton;
    }

    private JButton getMinus90Button() {
        if (mMinus90Button == null) {
            mMinus90Button = new CrystalButton("-90", BUTTON_WIDTH, BUTTON_HEIGHT, 68, 200,
                    245);
            mMinus90Button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.Minus90);
                }
            });
        }

        return mMinus90Button;
    }

    private JButton getMinus10Button() {
        if (mMinus10Button == null) {
            mMinus10Button = new CrystalButton("-10", BUTTON_WIDTH, BUTTON_HEIGHT, 246, 151,
                    143);
            mMinus10Button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonPressed(VirtualButton.Minus10);
                }
            });
        }

        return mMinus10Button;
    }

    /**
     * @return Creates or gets the widget that displays the video stream.
     */
    VideoWidget getVideoWidget() {
        if (mVideoWidget == null) {
            mVideoWidget = new VideoWidget(mClickSink, mConfig);
            mVideoWidget.setBackground(Color.white);
        }

        return mVideoWidget;
    }

}
