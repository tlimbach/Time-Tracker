package main.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JWindow;

import main.Helper;
import main.model.Project;
import main.model.WorkAmount;

public class TinyWindow extends JWindow implements DropTargetListener {

    private static final long serialVersionUID = 1L;

    private final ProjectListWindow cwp;

    protected Point pressedPoint = new Point(0, 0);

    private JComponent cmp;

    private Frame menuFrame;

    private Timer popupDelayTimer = null;

    public TinyWindow(ProjectListWindow project) {
        this.cwp = project;
        createWindow();
    }

    private void createWindow() {

        cmp = new JComponent() {

            private static final long serialVersionUID = 1L;
            int rand = 6;

            @Override
            public void paint(Graphics g) {

                super.paint(g);

                Project activeProject = cwp.getActiveProject();

                g.setColor(!cwp.isPausing() ? Color.green.darker() : Color.red.darker());

                if (activeProject == null) {
                    g.setColor(Color.black);
                }

                g.drawRoundRect(rand / 2, rand / 2, getWidth() - rand, getHeight() - rand, 10, 10);

                if (activeProject == null) {
                    g.drawString("Kein Projekt aktiv!", 10, 20);
                    return;
                }

                g.setColor(Color.darkGray);
                g.setFont(g.getFont().deriveFont(15F));

                g.drawString(activeProject.getProjektName(), 10, 20);
                g.setFont(g.getFont().deriveFont(10F));

                g.drawString(Helper.getSecondsAsTimeString(activeProject.getCurrentWorkAmount().getSecondsThatDay()), 10, 33);
                g.setFont(g.getFont().deriveFont(10F));
                g.drawString(Helper.getSecondsAsTimeString(activeProject.getTotalSecondsThisMonth()), 90, 33);

                g.setColor(activeProject.getCurrentWorkAmount().hasCommentForDay() ? Color.yellow : Color.lightGray);
                g.fillPolygon(new int[]{5, 11, 5}, new int[]{7, 7, 13}, 3);

                if (activeProject.getCurrentWorkAmount().hasCommentForDay()) {
                    setToolTipText(Helper
                            .convertPseudoHTLM(activeProject.getCurrentWorkAmount().getCommentForDay()));
                } else {
                    setToolTipText(null);
                }
            }

        };
        setSize(new Dimension(140, 40));
        setLayout(new BorderLayout());
        add(cmp, BorderLayout.CENTER);
        setAlwaysOnTop(true);

        cmp.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(final MouseEvent e) {

                if (e.getY() > cmp.getHeight() / 3) {
                    return;
                }

                if (popupDelayTimer != null) {
                    popupDelayTimer.cancel();
                }

                popupDelayTimer = new Timer();
                popupDelayTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        PopupMenu p = new PopupMenu();
                        cwp.addProjectItems(p);

                        showPopup(e, p);
                    }
                }, 600);

            }
        });

        cmp.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (popupDelayTimer != null) {
                    popupDelayTimer.cancel();
                }

                if (e.getButton() == 1 && e.getClickCount() == 2) {
                    cwp.getBtnPlay().setSelected(!cwp.getBtnPlay().isSelected());
                    cwp.togglePlayState();
                } else {
                    showPopup(e, cwp.createPopup());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressedPoint = new Point(e.getPoint());
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                cmp.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {

                if (popupDelayTimer != null) {
                    popupDelayTimer.cancel();
                }

            }
        });

        cmp.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {

                if (popupDelayTimer != null) {
                    popupDelayTimer.cancel();
                }

                cmp.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

                Point actPos = getLocationOnScreen();
                setLocation(actPos.x + e.getX() - pressedPoint.x, actPos.y + e.getY() - pressedPoint.y);
                repaint();
            }
        });

        new DropTarget(cmp, DnDConstants.ACTION_COPY_OR_MOVE, this, true);
    }

    public void setCommentTooptip(String comment) {
        cmp.setToolTipText(comment);
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @SuppressWarnings("deprecation")
    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        if (!dtde.getTransferable().isDataFlavorSupported(DataFlavor.plainTextFlavor)) {
            dtde.rejectDrag();
        }
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        String myString = "";
        try {
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            Transferable transferable = dtde.getTransferable();
            @SuppressWarnings("deprecation")
            StringReader sr = (StringReader) transferable.getTransferData(DataFlavor.plainTextFlavor);

            while (true) {
                int i = sr.read();

                if (i == -1) {
                    break;
                }

                myString += (char) i;
            }

            if (myString.indexOf('@') > -1 || myString.indexOf('_') > -1) {
                JOptionPane.showMessageDialog(cwp, "Im Kommentar dürfen keine @ und _ enthalten sein.",
                        "Kommentar kann nicht gespeichert werden", JOptionPane.ERROR_MESSAGE);
                return;
            }

        } catch (HeadlessException | UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }

        WorkAmount currentWorkAmount = this.cwp.getActiveProject().getCurrentWorkAmount();

        String commentForDay = currentWorkAmount.getCommentForDay();
        if (commentForDay == null || commentForDay.trim().length() == 0) {
            currentWorkAmount.setCommentForDay(myString);
        } else {
            this.setAlwaysOnTop(false);
            int showOptionDialog = JOptionPane.showOptionDialog(cmp,
                    "Wie soll mit dem  Kommentar (" + myString + ") verfahren werden ? ", "Kommentar bereits vorhanden",
                    JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    new String[]{"Anhängen", "Ersetzen", "Abbrechen"}, new String("Anhängen"));
            switch (showOptionDialog) {
                case 0: // Anhängen
                    currentWorkAmount.setCommentForDay(commentForDay + "\n" + myString);
                    break;
                case 1: // Ersetzen
                    currentWorkAmount.setCommentForDay(myString);
                    break;
                case 2: // Abbrechen
                default:
                    break;
            }
            this.setAlwaysOnTop(true);
        }
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    private void showPopup(MouseEvent e, PopupMenu p) {
        if (menuFrame == null) {
            menuFrame = new Frame();
        }

        menuFrame.add(p);
        menuFrame.setVisible(true);
        menuFrame.setLocation(-200, -200);// das Menuframe soll gar nicht auf dem Bildschirm auftauchen
        System.out.println("this xpos / ypos " + this.getX()+"/"+this.getY());  // Tinywindow
        System.out.println("e xpos / ypos " + e.getLocationOnScreen()); // Mouseclick
        System.out.println("egetxy " + e.getX()+"/"+e.getY());  // Mousepos relativ in Tinywindow
        System.out.println("menuf " + menuFrame.getX()+"/"+menuFrame.getY()); // Menufram1
        System.out.println("menuf2 " + menuFrame.getLocationOnScreen());
        p.show(menuFrame, 200+e.getLocationOnScreen().x,200+e.getLocationOnScreen().y);
        menuFrame.setVisible(false);

    }
}

