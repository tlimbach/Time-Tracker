package main.exporter;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import main.Helper;
import main.ProjectTimeTracker;
import main.model.Project;
import main.model.WorkAmount;

public class NotepadExporter {

    private ProjectTimeTracker clockworkProject;
    private List<Project> projects;
    private static final String NO_VALUE = "--------";

    public NotepadExporter(ProjectTimeTracker clockworkProject) {
        this.clockworkProject = clockworkProject;
        this.projects = clockworkProject.getProjects();
        exportReport();
    }

    private void exportReport() {

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(ProjectTimeTracker.rootDir + "/zeiten.txt");
        } catch (final FileNotFoundException e) {
            JOptionPane.showMessageDialog(clockworkProject.getWindow(),
                    "Datei " + ProjectTimeTracker.rootDir + "/zeiten.txt" + " kann nicht zum Schreiben geöffnet werden!", "Problem",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        final HashMap<String, Integer> monthlyTimes = new HashMap<>();

        final Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -2);

        // Überschrift
        String line = Helper.make12("Datum");
        line += "    ";
        for (int t = 0; t < projects.size(); t++) {
            line += Helper.make12(projects.get(t).getProjektName());
        }
        line += Helper.make12("Total");

        pw.println(line);

        // Projektnummern
        line = Helper.make12("Prj. Nummer");
        line += "    ";
        for (int t = 0; t < projects.size(); t++) {
            String string = projects.get(t).getProjektNumber();

            if (string == null || string.equals("null")) {
                string = NO_VALUE;
            }

            line += Helper.make12(string);
        }
        pw.println(line);

        final Calendar enddate = Calendar.getInstance();

        do {
            startDate.add(Calendar.DAY_OF_MONTH, 1);

            // bei neuem Monat Bilanz des vergangenen ausgeben
            if (startDate.get(Calendar.DAY_OF_MONTH) == 1) {

                line = "    " + Helper.make12("Total");
                for (int t = 0; t < projects.size(); t++) {
                    final Integer ti = (Integer) monthlyTimes.get(projects.get(t).getProjektName());

                    int time = 0;

                    if (ti != null) {
                        time = ti.intValue();
                    }

                    line += Helper.make12(Helper.getSecondsAsTimeString(time));
                }

                monthlyTimes.clear();

                pw.println(line);
                pw.println();
                pw.println(startDate.get(Calendar.MONTH) + 1 + " / " + startDate.get(Calendar.YEAR));
            }
            // Ende Bilanzausgabe

            line = "";
            final int dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK);
            line += " " + Helper.WEEKDAYS[dayOfWeek] + " ";

            final int day = startDate.get(Calendar.DAY_OF_MONTH);
            final int month = startDate.get(Calendar.MONTH) + 1;
            final int year = startDate.get(Calendar.YEAR);

            int totalSecsPerDay = 0;
            line += Helper.make12(Helper.make2(day) + "." + Helper.make2(month) + "." + year);

            for (int t = 0; t < projects.size(); t++) {
                final Project project = projects.get(t);

                boolean amountIsAdded = false;
                for (int wa = 0; wa < project.getWorkamountVec().size(); wa++) {
                    final WorkAmount amount = project.getWorkamountVec().get(wa);

                    if (amount.getDay() == day && amount.getMonth() == month && amount.getYear() == year) {
                        line += Helper.make12(Helper.getSecondsAsTimeString(amount.getSecondsThatDay()));

                        totalSecsPerDay += amount.getSecondsThatDay();

                        /*
						 * Gesamtsumme für Monat bestimmen
                         */
                        Integer i = (Integer) monthlyTimes.get(project.getProjektName());

                        if (i == null) {
                            monthlyTimes.put(project.getProjektName(), new Integer(0));
                            i = (Integer) monthlyTimes.get(project.getProjektName());
                        }

                        int ii = i.intValue();
                        ii += amount.getSecondsThatDay();
                        monthlyTimes.put(project.getProjektName(), new Integer(ii));

                        amountIsAdded = true;
                    }
                }

                if (!amountIsAdded) // Platzhalter
                {
                    line += Helper.make12(NO_VALUE);
                }
            }

            line += Helper.make12(Helper.getSecondsAsTimeString(totalSecsPerDay));
            pw.println(line);

        } while (startDate.before(enddate)); // durchlaufen bis heute

        pw.close();
    }
}
