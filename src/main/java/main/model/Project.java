package main.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JOptionPane;

import main.Helper;
import main.ProjectTimeTracker;

public class Project implements Comparable<Project> {

    private List<WorkAmount> workamountVec;

    private WorkAmount currentWorkAmount = null;

    private String projektName;

    private String projektNumber;

    private String rootDir;

    private final static String _ENDOFFILE = "endoffile";

    private int lastDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

    private boolean backup;

    private long lastBackupmillis;

    private WorkAmount createNewAmount() {
        Calendar calendar = Calendar.getInstance();

        int day, month, year;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH) + 1;
        year = calendar.get(Calendar.YEAR);

        return new WorkAmount(day, month, year, 0, null);
    }

    public Project(String pname, String pnumber, String rootdir) {
        setProjektName(pname);
        setProjektNumber(pnumber);

        rootDir = rootdir;

        this.workamountVec = new ArrayList<WorkAmount>();

        this.currentWorkAmount = createNewAmount();

        getWorkamountVec().add(this.currentWorkAmount);

        if (new File(getProjectFilename()).exists()) {
            load();
        }

        // TODO: der Timer hat hier nichts verloren
        new java.util.Timer().scheduleAtFixedRate(new TimerTask() {
            private int lastSecond;

            private boolean backuptimeReached() {

                long currentTimeMillis = System.currentTimeMillis();
                if ((currentTimeMillis - lastBackupmillis) > (1000 * ProjectTimeTracker.BACKUP_DELAY_SECS)) {
                    lastBackupmillis = currentTimeMillis;
                    return true;
                } else {
                    return false;
                }
            }

            private boolean midnightPassed() {
                int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

                if (lastDay != currentDay) {
                    lastDay = currentDay;

                    return true;
                }

                return false;
            }

            public void run() {
                {

                    int thisSecond = Calendar.getInstance().get(Calendar.SECOND); // nur damit nicht zu schnell gezählt
                    // wird
                    if (thisSecond == lastSecond) {
                        return;
                    }

                    lastSecond = thisSecond;

                    if (midnightPassed() || backuptimeReached()) {
                        save();
                        load();
                    }

                }
            }
        }, 5000, 999);

    }

    private String getProjectFilename() {
        return rootDir + "/" + projektName + ".cpr";
    }

    public void increaseTime() {

        getCurrentWorkAmount().increaseSeconds();

    }

    public int getTotalSecondsThisMonth() {

        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;

        int s = 0;
        for (int t = 0; t < getWorkamountVec().size(); t++) {
            WorkAmount wa = getWorkamountVec().get(t);

            if (wa.getMonth() == month) {
                s += wa.getSecondsThatDay();
            }
        }
        return s;
    }

    public synchronized void save() {
        saveAsFile(".cpr");
        saveAsFile(".bak");
    }

    private boolean saveAsFile(String ext) {
        System.out.println("save " + ext);
        PrintWriter dos = null;
        Calendar lastDateToSave = GregorianCalendar.getInstance();
        lastDateToSave.add(Calendar.MONTH, -3); // Speichern der letzten 3
        // Monate
        try {
            dos = new PrintWriter(rootDir + "/" + getProjektName() + ext);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        dos.println("#" + getProjektNumber());

        for (int t = 0; t < getWorkamountVec().size(); t++) {
            WorkAmount amount = getWorkamountVec().get(t);

            Calendar dateOfThisAmount = Calendar.getInstance();
            dateOfThisAmount.set(amount.getYear(), amount.getMonth() - 1, amount.getDay());

            if (dateOfThisAmount.before(lastDateToSave)) {
                continue;
            }

            if (amount.getSecondsThatDay() == 0) {
                continue; // keine Einträge mit 0 Sekunden schreiben
            }
            String string = amount.getDay() + "." + amount.getMonth() + "." + amount.getYear() + " Seconds "
                    + amount.getSecondsThatDay() + " Time: " + Helper.getSecondsAsTimeString(amount.getSecondsThatDay())
                    + " " + (amount.hasCommentForDay() ? getEncoded(amount.getCommentForDay()) : "");

            dos.println(string);
        }

        dos.println(_ENDOFFILE);
        dos.close();
        return true;
    }

    private String getEncoded(String commentForDay) {
        return commentForDay.replace(' ', '_').replace('\n', '@');
    }

    private String getDecoded(String commentFotDay) {
        return commentFotDay.replace('_', ' ').replace('@', '\n');
    }

    private synchronized void load() {
        backup = true;
        System.out.println("loading file " + rootDir + "/" + getProjektName());
        int day = 0, month = 0, year = 0, secsThatDay = 0;
        String commentForDay = null;

        RandomAccessFile raf = null;
        Calendar now = GregorianCalendar.getInstance();
        try {
            File file = new File(getProjectFilename());
            raf = new RandomAccessFile(file, "rw");

            System.out.println("loaded " + file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Default Workamount entfernen
        getWorkamountVec().clear();
        this.currentWorkAmount = null;

        if (raf == null) {
            return;
        }

        while (true) {
            try {
                String line = raf.readLine();

                // Prüfen ob Projektnummer
                if (line.charAt(0) == '#') {
                    setProjektNumber(line.substring(1));
                    continue;
                }

                if (line.equals(_ENDOFFILE)) {
                    break;
                }

                StringTokenizer st = new StringTokenizer(line, " ");
                String date = st.nextToken();
                st.nextToken(); // "Seconds" in Configfile überlesen

                secsThatDay = Integer.parseInt(st.nextToken());

                StringTokenizer datetokens = new StringTokenizer(date, ".");

                day = Integer.parseInt(datetokens.nextToken());
                month = Integer.parseInt(datetokens.nextToken());
                year = Integer.parseInt(datetokens.nextToken());

                try {
                    st.nextToken(); // "Time:"
                    st.nextToken(); // die Human Readable Zeit

                    commentForDay = getDecoded(st.nextToken());

                } catch (Exception e) {
                    // kein Problem, kein Kommentar ...
                    commentForDay = null;
                }

            } catch (Exception e) {

                e.printStackTrace();

                try {
                    backup = false;
                    JOptionPane
                            .showMessageDialog(null,
                                     "Beim Laden von Projekt "
                                    + getProjektName()
                                    + " ist ein Fehler aufgetreten. Die gespeicherten Zeiten stimmen möglicherweise nicht!\nBitte den Fehler korrigieren und Programm erneut starten",
                                    "Fehler beim Laden von Projekt "
                                    + getProjektName(),
                                    JOptionPane.ERROR_MESSAGE);
                    raf.close();
                    System.exit(1);
                    return;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }

            WorkAmount amount = new WorkAmount(day, month, year, secsThatDay, commentForDay);

            // prüfen ob der gerade gelesene Workamount der für heute ist
            if (now.get(Calendar.DAY_OF_MONTH) == day && now.get(Calendar.MONTH) + 1 == month
                    && now.get(Calendar.YEAR) == year) {
                this.currentWorkAmount = amount;
            }

            getWorkamountVec().add(amount);
        }
        try {
            raf.close();

            // falls für den aktuellen Tag kein Workampount existiert einen
            // anlegen und merken
            if (getCurrentWorkAmount() == null) {
                this.currentWorkAmount = createNewAmount();
                getWorkamountVec().add(getCurrentWorkAmount());
            }

            if (backup) {
                saveAsFile(".bak");
            }

            return;

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void delete() {
        new File(rootDir + "/" + getProjektName() + ".cpr").delete();
        new File(rootDir + "/" + getProjektName() + ".bak").delete();
    }

    public void renameTo(String name, String number) {

        setProjektNumber(number);
        save();

        new File(rootDir + "/" + getProjektName() + ".cpr").renameTo(new File(rootDir + "/" + name + ".cpr"));

        setProjektName(name);

        load();
    }

    public int compareTo(Project o) {
        Project p2 = (Project) o;
        return getProjektName().toLowerCase().compareTo(p2.getProjektName().toLowerCase());
    }

    public WorkAmount getCurrentWorkAmount() {
        return currentWorkAmount;
    }

    public String getProjektName() {
        return projektName;
    }

    public void setProjektName(String projektName) {
        this.projektName = projektName;
    }

    public String getProjektNumber() {
        return projektNumber;
    }

    private void setProjektNumber(String projektNumber) {
        this.projektNumber = projektNumber;
    }

    public List<WorkAmount> getWorkamountVec() {
        return workamountVec;
    }

    public static Project getProjectforName(String name, List<Project> projectsVec) {

        for (Project p : projectsVec) {

            if (p.getProjektName().equals(name)) {
                return p;
            }

        }

        return null;
    }

    public static Project getProjectForName(final List<Project> projects, final String text) {
        for (final Project p : projects) {
            if (p.getProjektName().equals(text)) {
                return p;
            }
        }

        return null;
    }

}
