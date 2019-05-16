package main.exporter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

import main.ProjectTimeTracker;
import main.Helper;
import main.model.TrackerTableModel;
import main.model.Project;
import main.model.WorkAmount;

public class ScreenExporter {

    private static final Color LIGHTEST_GRAY = new Color(240, 240, 240);
    private ProjectTimeTracker cwp;

    public ScreenExporter(ProjectTimeTracker clockworkProject) {
        this.cwp = clockworkProject;
        exportReportToScreen();

    }

    public void exportReportToScreen() {
        final JFrame reportDialog = new JFrame();
        reportDialog.setPreferredSize(new Dimension(640, 480));
        reportDialog.setMinimumSize(reportDialog.getPreferredSize());
        reportDialog.setExtendedState(Frame.MAXIMIZED_BOTH);
        reportDialog.setLayout(new BorderLayout());

        final JTable reportTable = new JTable(new TrackerTableModel(cwp.getProjects()));

        final JTextField searchText = new JTextField();
        searchText.setToolTipText("Suche nach Textstücken in Kommentaren");
        searchText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(final KeyEvent e) {
                reportTable.repaint();
            }
        });

        reportTable.setRowSelectionAllowed(true);
        reportTable.getColumnModel().getColumn(0).setMinWidth(110);
        reportTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {

                final int r = reportTable.getSelectedRow();
                final int c = reportTable.getSelectedColumn();

                Project p = cwp.getProjects().get(reportTable.getSelectedColumn() - 1);
                final Object valueAt = reportTable.getModel().getValueAt(r, c);

                if (valueAt instanceof WorkAmount && e.getClickCount() == 2) {

                    cwp.editComment(p, (WorkAmount) valueAt);
                    cwp.updateProject(p);
                }
            }
        });

        final TableCellRenderer cr = new TableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(final JTable table, final Object value,
                    final boolean isSelected, final boolean hasFocus, final int row, final int column) {

                final JLabel label = new JLabel();

                label.setText("---");

                label.setBackground(row % 2 == 0 ? LIGHTEST_GRAY : Color.white);

                if (value instanceof WorkAmount) {
                    final WorkAmount wa = (WorkAmount) value;

                    label.setText(Helper.getSecondsAsTimeString(wa.getSecondsThatDay()));
                    label.setToolTipText(Helper.convertPseudoHTLM(wa.getCommentForDay()));

                    if (wa.getCommentForDay() != null && wa.getCommentForDay().length() > 0) {
                        label.setForeground(Color.BLUE);

                        if (searchText != null && searchText.getText() != null) {
                            final String searchData = searchText.getText().trim();

                            if (searchData.length() > 0
                                    && wa.getCommentForDay().toLowerCase().contains(searchData.toLowerCase())) {
                                label.setBackground(Color.yellow);
                            }
                        }

                    }

                }
                label.setOpaque(true);

                if (isSelected) {
                    label.setBackground(label.getBackground().darker());
                }

                return label;
            }

        };

        reportTable.setDefaultRenderer(WorkAmount.class, cr);

        final JScrollPane spane = new JScrollPane(reportTable);
        reportDialog.add(spane, BorderLayout.CENTER);
        reportDialog.add(searchText, BorderLayout.SOUTH);
        reportDialog.setTitle("Übersicht der Projektzeiten");

        final JScrollBar verticalScrollBar = spane.getVerticalScrollBar();
        verticalScrollBar.setValue(verticalScrollBar.getMaximum());

        reportDialog.setVisible(true);
        searchText.requestFocus();
    }

}
