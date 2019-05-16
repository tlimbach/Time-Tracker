package main.model;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import main.Helper;

public class TrackerTableModel implements TableModel {

    private final List<Project> projectsVec;
    private Calendar startDate;

    int daysToShow = 60;

    public TrackerTableModel(List<Project> projectsVec) {
        this.projectsVec = projectsVec;

        startDate = Calendar.getInstance();
        startDate.add(Calendar.DAY_OF_YEAR, -daysToShow);
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        // leer
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {

        if (columnIndex == 0) {
            return String.class;
        } else {
            return WorkAmount.class;
        }
    }

    @Override
    public int getColumnCount() {
        return projectsVec.size() + 2;
    }

    @Override
    public String getColumnName(int columnIndex) {

        if (columnIndex == (projectsVec.size() + 1)) {
            return "Gesamt";
        }

        if (columnIndex == 0) {
            return "Datum";
        } else {
            return projectsVec.get(columnIndex - 1).getProjektName();
        }
    }

    @Override
    public int getRowCount() {
        return daysToShow + 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        // erst Datum rausfinden ...
        Calendar date = (Calendar) startDate.clone();
        date.add(Calendar.DAY_OF_YEAR, rowIndex);

        int dateDay = date.get(Calendar.DAY_OF_MONTH);
        int dateMonth = (date.get(Calendar.MONTH) + 1);
        int dateYear = date.get(Calendar.YEAR);

        // Datum zurückliefern, falls Spalte 0...
        if (columnIndex == 0) {
            String string = Helper.make2(dateDay) + "." + Helper.make2(dateMonth) + "." + dateYear;

            int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
            return string + " (" + Helper.WEEKDAYS[dayOfWeek] + ") ";
        }

        // total rausfinden, falls letzte Spalte
        if (columnIndex == projectsVec.size() + 1) {
            int sum = 0;
            for (Project p : projectsVec) {
                for (WorkAmount wa : p.getWorkamountVec()) {
                    if (dateDay == wa.getDay() && dateMonth == wa.getMonth()
                            && dateYear == wa.getYear()) {
                        sum += wa.getSecondsThatDay();
                        break;
                    }
                }
            }

            WorkAmount wom = new WorkAmount(0, 0, 0, 0, null);
            wom.setSecondsThatDay(sum);

            return wom;
        }

        // jetzt Projekt rausfinden...
        Project p = projectsVec.get(columnIndex - 1);

        // jetzt schauen, ob es in diesem WorkamountVec einen mit dem gesuchten
        // Datum gibt
        for (WorkAmount wa : p.getWorkamountVec()) {
            if (dateDay == wa.getDay() && dateMonth == wa.getMonth()
                    && dateYear == wa.getYear()) {
                return wa;
            }
        }

        return "-x-";

    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        // leer

    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        // leer
    }

}
