package main.ui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.lang.model.util.Elements;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import main.Helper;
import main.ProjectListWindowListener;
import main.ProjectPropertiesChangeListener;
import main.ProjectTimeTracker;
import main.model.Project;

public class ProjectListWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private final Image timegreengif = new ImageIcon(this.getClass().getResource("/timeDarkGreen.gif"))
            .getImage();

    private final Image timeredgif = new ImageIcon(this.getClass().getResource("/timeRed.gif")).getImage();

    private MenuItem togglePausePlayMenuItem;
    private JButton btnDeleteProject, btnCreateProject;
    private Menu projectNamesMenu;

    protected long lastMouseExitedAt;

    protected boolean isUnderMouse;

    private JToggleButton btnPlay;

    private JCheckBox rbStayOnTop;

    private ButtonGroup btnGrpProjects;

    private final JPanel showRoom;

    private ProjectTimeTracker cw;

    private final Vector<Project> projectsVec = new Vector<>();

    protected final static String _TITLE = "Projektuhr2000";

    public JButton getBtnCreateProject() {
        return btnCreateProject;
    }

    public JButton getBtnDeleteProject() {
        return btnDeleteProject;
    }

    public JComboBox<String> getExportCombo() {
        return exportCombo;
    }

    public JToggleButton getBtnPlay() {
        return btnPlay;
    }

    private JComboBox<String> exportCombo = new JComboBox<String>(new String[]{"Export", "Notepad", "Tabelle"});

    public ProjectListWindow(final ProjectTimeTracker cw) {
        setIconImage(timegreengif);
        this.cw = cw;

        this.showRoom = new JPanel();
        final int XWIDTH = 400;
        final Dimension dimmin = new Dimension(XWIDTH, 184);
        final Dimension dimmax = new Dimension(XWIDTH, Toolkit.getDefaultToolkit().getScreenSize().height - 30);
        setMaximumSize(dimmin);
        addComponentListener(new WindowResizeListener(dimmin, dimmax));

        addWindowListener(new WindowAdapter() {

            public void windowClosing(final WindowEvent e) {

                cw.saveAllProjectsAndSettings();

                System.exit(1);
            }

            public void windowDeiconified(final WindowEvent e) {
                cw.setTinyWindowVisible(false);
                setVisible(true);
            }

            public void windowIconified(final WindowEvent e) {
                if (rbStayOnTop.isSelected()) {
                    cw.setTinyWindowVisible(true);
                }
                setVisible(false);
            }
        });

        init();
    }

    private void init() {
        setTitle(_TITLE);

        btnGrpProjects = new ButtonGroup();

        final JPanel buttonpanel = new JPanel();
        buttonpanel.setLayout(new FlowLayout());

        buttonpanel.add(btnCreateProject = new JButton("Neu"));
        final ProjectListWindowListener clockworkProjectListener = new ProjectListWindowListener(cw, this);
        btnCreateProject.addActionListener(clockworkProjectListener);
        buttonpanel.add(btnDeleteProject = new JButton("Löschen"));

        buttonpanel.add(btnPlay = new JToggleButton());
        buttonpanel.add(exportCombo);

        btnDeleteProject.addActionListener(clockworkProjectListener);
        exportCombo.addActionListener(clockworkProjectListener);
        btnPlay.addActionListener((e) -> {
            togglePlayState();
        });

        buttonpanel.add(rbStayOnTop = new JCheckBox("<HTML>Minifenster<br>wenn<br>minimiert</HTML>"));

        rbStayOnTop.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                cw.setTinyWindowAlwaysOnTop(rbStayOnTop.isSelected());
            }
        });
        rbStayOnTop.setSelected(true);

        final JScrollPane scrollPane = new JScrollPane(showRoom);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonpanel, BorderLayout.SOUTH);
        togglePlayState(); // einmal zum Zeichnen des Knopfes
//		togglePlayState(); // einmal zum Starten der App
        pack();
        setVisible(true);

        addTrayIcon();
    }

    public void togglePlayState() {

        boolean pause = isPausing();

        btnPlay.setIcon(
                new ImageIcon(this.getClass().getResource(pause ? "/VCRPlay.gif" : "/VCRPause.gif")));

        if (togglePausePlayMenuItem != null) {
            togglePausePlayMenuItem.setLabel(pause ? "Zeit anhalten" : "Zeit weiterzählen");

            if (trayIcon != null) {
                trayIcon.setImage(!pause ? timegreengif : timeredgif);
            }

            setIconImage(pause ? timeredgif : timegreengif);

            cw.repaintTiny();

            if (pause) {
                setTitle(_TITLE + " [PAUSE]");
                trayIcon.setToolTip(trayIcon.getToolTip() + "[PAUSE]");
            }
        }

    }

    private void addTrayIcon() {

        trayIcon = new TrayIcon(timegreengif, "Timetracker", createPopup());
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent arg0) {

                togglePlayState();
            }
        });

        // Das Popupmenue muss vor am anzeigen getauscht werden
        trayIcon.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(final MouseEvent arg0) {

                if (arg0.getClickCount() == 1 && arg0.getButton() == 1) {

                    showMainWindow(!isVisible());
                }

                trayIcon.setPopupMenu(createPopup());
            }

        });

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (final AWTException e1) {
            e1.printStackTrace();
        }

    }

    public PopupMenu createPopup() {

        lastMouseExitedAt = System.currentTimeMillis();
        final PopupMenu menu = new PopupMenu();
        projectNamesMenu = new Menu("Projekt wählen...");
        final MenuItem itm_maximize = new MenuItem("Hauptfenster anzeigen");

        menu.add(itm_maximize);
        itm_maximize.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                showMainWindow(true);
            }
        });
        final MenuItem itm_showMiniwindow = new MenuItem("Minifenster anzeigen");

        if (cw.isTinyShowing()) {
            itm_showMiniwindow.setEnabled(false);
        }

        menu.add(itm_showMiniwindow);
        itm_showMiniwindow.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                cw.showTiny();
            }
        });

        menu.addSeparator();

        togglePausePlayMenuItem = new MenuItem(!isPausing() ? "Pause" : "Pause zuende");
        togglePausePlayMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                btnPlay.setSelected(!btnPlay.isSelected());
                togglePlayState();
            }
        });

        menu.add(togglePausePlayMenuItem);

        menu.addSeparator();

        if (getActiveProject() != null) {
            final MenuItem editCommentItem = new MenuItem(
                    getActiveProject().getCurrentWorkAmount().hasCommentForDay() ? "Kommentar ändern.."
                    : "Kommentar  anlegen..");
            menu.add(editCommentItem);
            editCommentItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    cw.editComment(getActiveProject(), getActiveProject().getCurrentWorkAmount());
                }
            });
        }

        fillProjectMenu();

        menu.add(projectNamesMenu);

        menu.addSeparator();

        final MenuItem exportItem = new MenuItem("Tabelle anzeigen");
        exportItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        cw.exportReportToScreen();
                    }
                }.start();
            }

        });
        menu.add(exportItem);

        menu.addSeparator();
        final MenuItem exitItem = new MenuItem("Beenden");
        exitItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                cw.saveAllProjectsAndSettings();
                System.exit(1);
            }

        });

        menu.add(exitItem);

        menu.setEnabled(true);

        return menu;
    }

    private void showMainWindow(boolean doShow) {
        setVisible(doShow);

        setExtendedState(doShow ? JFrame.NORMAL:JFrame.ICONIFIED);

        if (doShow) {
           toFront();
        } else {
            toBack();
        }
    }

    private void fillProjectMenu() {

        if (projectNamesMenu == null) {
            return;
        }

        projectNamesMenu.removeAll();
        addProjectItems(projectNamesMenu);
        projectNamesMenu.addSeparator();
        final MenuItem menuItem = new MenuItem("Neues Projekt ...");
        projectNamesMenu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                final ProjektDialog pd = new ProjektDialog();
                final boolean hasResult = pd.open(null, null);

                if (!hasResult) {
                    return;
                }

                final String name = pd.getProjectName();
                final String number = pd.getProjectNumber();

                if (name.length() > 15) {
                    JOptionPane.showMessageDialog(null,
                            "Projekt kann nicht angelegt werden, maximale Anzahl von 15 Zeichen überschritten.");
                    return;
                }

                if (name.trim().length() < 1) {
                    JOptionPane.showMessageDialog(null,
                            "Projekt kann ohne Namen nicht angelegt werden.");
                    return;
                }

                cw.createProject(name, number);
            }

        });
    }

    Map<Project, Elements> mapElements = new HashMap<>();

    private TrayIcon trayIcon;

    public void drawShowroom() {

        fillProjectMenu();

        showRoom.removeAll();
        final GridBagLayout gb = new GridBagLayout();
        showRoom.setLayout(gb);

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 3, 0, 3);
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        gbc.gridx = 0;

        final JLabel lblProjectname = new JLabel("  Projekt");
        final JLabel lblToday = new JLabel("Heute");
        final JLabel lblMonth = new JLabel("Monat");

        gb.setConstraints(lblProjectname, gbc);
        showRoom.add(lblProjectname);

        gbc.gridx = 1;

        gbc.anchor = GridBagConstraints.WEST;
        gb.setConstraints(lblToday, gbc);
        showRoom.add(lblToday);

        gbc.gridx = 2;

        gb.setConstraints(lblMonth, gbc);
        showRoom.add(lblMonth);

        gbc.fill = GridBagConstraints.BOTH;

        Collections.sort(projectsVec);

        for (int t = 0; t < projectsVec.size(); t++) {

            System.out.println("Showroom elemts = " + projectsVec.size());

            final Project p = (Project) projectsVec.elementAt(t);

            gbc.gridy = t + 1;

            // Radiobutton
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.WEST;

            btnGrpProjects.add(getRadioButton(p));
            gb.setConstraints(getRadioButton(p), gbc);
            showRoom.add(getRadioButton(p));

            // Projektzeit heute
            gbc.gridx = 1;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.EAST;
            gb.setConstraints(getLblToday(p), gbc);
            showRoom.add(getLblToday(p));

            // Projektzeit Monat
            gbc.gridx = 2;
            gbc.gridwidth = 1;
            gb.setConstraints(getLblTotal(p), gbc);
            showRoom.add(getLblTotal(p));
        }
        showRoom.revalidate();
    }

    public void addProjectItems(final Menu menu) {
        Collections.sort(projectsVec);

        Project ap = getActiveProject();

        for (final Project p : projectsVec) {

            final CheckboxMenuItem item = new CheckboxMenuItem(p.getProjektName(), ap == p);
            menu.add(item);

            item.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(final ItemEvent e) {
                    final CheckboxMenuItem item = (CheckboxMenuItem) e.getSource();
                    final String label = item.getLabel();
                    final Project p = Project.getProjectforName(label, projectsVec);
                    setProjectActive(p);
                    updateProject(p);
                }
            });

        }
    }

    public void setProjects(List<Project> projectsVec) {
        this.projectsVec.clear();
        this.projectsVec.addAll(projectsVec);
        mapElements.clear();
        drawShowroom();

        for (Project p : this.projectsVec) {
            updateProject(p);
        }

    }

    private Elements getElements(Project project) {

        Elements elements = mapElements.get(project);

        if (elements == null) {

            elements = new Elements(project);
            mapElements.put(project, elements);
        }

        return elements;
    }

    private JRadioButton getRadioButton(Project project) {
        return getElements(project).getRadioButton();
    }

    private JLabel getLblToday(Project project) {
        return getElements(project).getLblToday();
    }

    private JLabel getLblTotal(Project project) {
        return getElements(project).getLblTotal();
    }

    private class Elements {

        private JRadioButton radioButton;
        private JLabel lblToday;
        private JLabel lblTotal;

        public Elements(Project project) {
            radioButton = new JRadioButton();
            lblToday = new JLabel();
            lblTotal = new JLabel();

            ProjectPropertiesChangeListener pListener = new ProjectPropertiesChangeListener(project, cw);

            radioButton.addMouseListener(pListener);
            lblToday.addFocusListener(pListener);
            lblToday.addMouseListener(pListener);
        }

        public JLabel getLblToday() {
            return lblToday;
        }

        public JLabel getLblTotal() {
            return lblTotal;
        }

        public JRadioButton getRadioButton() {
            return radioButton;
        }

    }

    public boolean isPausing() {
        return !btnPlay.isSelected();
    }

    public void updateProject(Project project) {

        getRadioButton(project).setText(project.getProjektName());
        getRadioButton(project).setToolTipText("Projektnummer: " + project.getProjektNumber());

        String secondsAsTime = Helper.getSecondsAsTimeString(0);
        if (project.getCurrentWorkAmount() != null) {

            secondsAsTime = Helper.getSecondsAsTimeString(project.getCurrentWorkAmount().getSecondsThatDay());

            if (project.getCurrentWorkAmount().hasCommentForDay()) {
                getLblToday(project)
                        .setToolTipText(Helper.convertPseudoHTLM(project.getCurrentWorkAmount().getCommentForDay()));
            }

            setTitle(_TITLE + "   " + project.getProjektName() + "   "
                    + secondsAsTime + "   ("
                    + Helper.getSecondsAsTimeString(project.getTotalSecondsThisMonth()) + ")");
        }

        String secondsThisMonth = Helper.getSecondsAsTimeString(project.getTotalSecondsThisMonth());
        getLblTotal(project).setText("" + secondsThisMonth);
        getLblToday(project).setText("" + secondsAsTime);
    }

    public void setProjectActive(Project activeProject) {
        getRadioButton(activeProject).setSelected(true);
    }

    public Project getActiveProject() {
        Iterator<Entry<Project, Elements>> i = mapElements.entrySet().iterator();
        while (i.hasNext()) {

            Entry<Project, Elements> entry = i.next();
            boolean isTheActiveOne = entry.getValue().getRadioButton().isSelected();

            if (isTheActiveOne) {
                return entry.getKey();
            }

        }
        return null;
    }

    public void setPause() {
        if (!isPausing()) {
            btnPlay.setSelected(false);
            togglePlayState();
        }
    }
}
