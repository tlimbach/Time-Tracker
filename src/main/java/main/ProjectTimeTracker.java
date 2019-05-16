package main;

import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.TrayIcon;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import main.exporter.NotepadExporter;
import main.exporter.ScreenExporter;
import main.model.Project;
import main.model.WorkAmount;
import main.ui.ProjectEditor;
import main.ui.ProjectListWindow;
import main.ui.TinyWindow;

public class ProjectTimeTracker {

    private final ProjectListWindow window = new ProjectListWindow(this);
    private final TinyWindow tiny = new TinyWindow(window);

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    protected static final String LASTPROJECFILE = "lastproject";

    public static final int BACKUP_DELAY_SECS = 300; // Speichern alle 5 Minuten

    private List<Project> projects = new ArrayList<Project>();

    public List<Project> getProjects() {
        return projects;
    }

    private final String lastProjekt = null;

    private TrayIcon trayIcon;
    public final static String rootDir = "./";

    public ProjectTimeTracker() {
        loadProjects();

        window.setProjects(projects);

        loadSettings();

        if (lastProjekt != null) {
            window.setProjectActive(Project.getProjectforName(lastProjekt, projects));
        }

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                saveSettings();
            }
        }, BACKUP_DELAY_SECS * 1000, BACKUP_DELAY_SECS * 1000);

        work();
    }

    private void work() {

        new java.util.Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                repaintTiny();

                if (!window.isPausing()) {

                    Project activeProject = window.getActiveProject();

                    if (activeProject != null) {
                        activeProject.increaseTime();
                        window.updateProject(activeProject);
                    }
                }
            }

        }, 1000, 1000);
    }

    public void createProject(final String name, final String number) {

        if (!isProjectNameAvailable(null, name, number)) {
            return;
        }

        final Project project = new Project(name, number, ProjectTimeTracker.rootDir);
        projects.add(project);
        window.setProjects(projects);
        window.drawShowroom();

        window.updateProject(project);
    }

    public void deleteProject(Project toDelete) {
        projects.remove(toDelete);

        window.setProjects(projects);

        toDelete.delete();
        toDelete = null;

        window.drawShowroom();
        window.setPause();
    }

    public void editComment(final Project project, final WorkAmount amount) {

        new ProjectEditor(project, amount);

    }

    public boolean isProjectNameAvailable(final Project projectToBeNamed, final String name, final String number) {
        boolean isAvailable = true;

        for (final Project project : projects) {
            if (project.getProjektName().equals(name) && project != projectToBeNamed) {
                JOptionPane.showMessageDialog(window,
                        "Projekt " + name + " bereits vorhanden für Projeknummer " + project.getProjektNumber() + ".");
                isAvailable = false;
            }

            if (number.length() > 0 && project.getProjektNumber().equals(number) && project != projectToBeNamed) {
                JOptionPane.showMessageDialog(window,
                        "Projektnummer " + number + "  bereits vergeben für Projekt " + project.getProjektName() + ".");
                isAvailable = false;
            }
        }

        return isAvailable;
    }

    private void loadProjects() {
        final String[] plist = new File(ProjectTimeTracker.rootDir).list();

        for (int t = 0; t < plist.length; t++) {
            if (plist[t].endsWith(".cpr")) {

                final Project p = new Project(plist[t].substring(0, plist[t].lastIndexOf(".")),
                        null /* Projektnummer */, ProjectTimeTracker.rootDir);

                projects.add(p);
            }

        }
    }

    private void loadSettings() {
        String lastActiveProjectName = null;
        final String filename = ProjectTimeTracker.rootDir + "/" + LASTPROJECFILE;
        try {
            int maxHeight = 0;
            int maxWidth = 0;

            for (final GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                maxHeight = device.getDisplayMode().getHeight(); // die Höhen zu addieren macht keinen Sinn. Die
                // Monitore stehen hoffentlich nebeneinander...
                maxWidth += device.getDisplayMode().getWidth();
            }

            final RandomAccessFile raf = new RandomAccessFile(filename, "r");
            lastActiveProjectName = raf.readLine();

            int x = Integer.parseInt(raf.readLine());
            int y = Integer.parseInt(raf.readLine());

            if (x >= maxWidth) {
                x = 100;
            }

            if (y >= maxHeight) {
                y = 100;
            }

            window.setLocation(x, y);

            x = Integer.parseInt(raf.readLine());
            y = Integer.parseInt(raf.readLine());

            if (x >= maxWidth) {
                x = 100;
            }

            if (y >= maxHeight) {
                y = 100;
            }

            tiny.setLocation(x, y);
            raf.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        System.out.println("lac " + lastActiveProjectName);

        if (lastActiveProjectName.equals("_")) {
            return;
        }

        window.setProjectActive(Project.getProjectforName(lastActiveProjectName, projects));
    }

    private void saveSettings() {
        final String filename = ProjectTimeTracker.rootDir + "/" + LASTPROJECFILE;
        String activeProjectName = null;

        Project p = window.getActiveProject();

        if (p != null) {
            activeProjectName = p.getProjektName();
        }

        if (activeProjectName == null) {
            activeProjectName = "_";
        }

        try {
            final PrintWriter dos = new PrintWriter(filename);
            dos.println(activeProjectName);
            dos.println(window.getLocation().x);
            dos.println(window.getLocation().y);
            dos.println(tiny.getLocation().x);
            dos.println(tiny.getLocation().y);
            dos.flush();
            dos.close();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void setTrayTooltip(final String projectinfo, final String secondsAsTime, final String secondsThisMonth) {
        if (trayIcon != null) {
            trayIcon.setToolTip(projectinfo + " " + secondsAsTime + " ( " + secondsThisMonth + " )  ");
        }
    }

    public void saveAllProjectsAndSettings() {
        for (final Project p : projects) {

            // Projekte automatisch löschen, für die in den
            // letzten 3 Monaten keine Einträge aufgelaufen sind
            int timeForLastThreeMonth = 0;
            for (final WorkAmount wam : p.getWorkamountVec()) {
                timeForLastThreeMonth += wam.getSecondsThatDay();
            }

            if (timeForLastThreeMonth == 0) {
                p.delete();
            } else {
                p.save();
            }
        }
        saveSettings();
    }

    public void setTinyWindowVisible(boolean show) {
        tiny.setVisible(show);
    }

    public static void main(final String[] args) {

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (final Exception e) {

        }

        new ProjectTimeTracker();
    }

    public Component getWindow() {

        return (Component) window;
    }

    public void updateProject(Project project) {
        window.updateProject(project);
    }

    public void showTiny() {
        if (!tiny.isVisible()) {
            tiny.setVisible(true);
            tiny.setAlwaysOnTop(true);
            tiny.toFront();
        }

    }

    public boolean isTinyShowing() {
        return tiny != null && tiny.isVisible();
    }

    public void repaintTiny() {
        tiny.repaint();

    }

    public void exportReportToScreen() {
        new ScreenExporter(this);

    }

    public void exportReportInNotepad() {
        try {
            new NotepadExporter(this);

            Runtime.getRuntime().exec("notepad " + ProjectTimeTracker.rootDir + "zeiten.txt");

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void setTinyWindowAlwaysOnTop(boolean alwaysOnTop) {
        tiny.setAlwaysOnTop(alwaysOnTop);
    }

}
