package main.ui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import main.model.Project;
import main.model.WorkAmount;

public class ProjectEditor {

    public ProjectEditor(Project project, final WorkAmount amount) {
        final JDialog dialog = new JDialog();
        dialog.setResizable(false);
        dialog.setModal(true);

        JButton ok, cancel;
        final GridBagLayout gridBagLayout = new GridBagLayout();
        dialog.setLayout(gridBagLayout);
        dialog.setTitle("Kommentar ändern für Projekt " + project.getProjektName());

        final GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;

        gc.gridwidth = 5;
        final JTextArea ta = new JTextArea(10, 40);
        ta.setText(amount.getCommentForDay());
        dialog.add(new JScrollPane(ta), gc);

        gc.gridwidth = 4;
        gc.gridx = 0;
        gc.gridy = 5;

        final JPanel btp = new JPanel();
        btp.setLayout(new FlowLayout());

        ok = new JButton("OK");
        cancel = new JButton("Abbrechen");

        btp.add(ok);
        btp.add(cancel);

        dialog.add(btp, gc);

        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                dialog.dispose();
            }
        });

        ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {

                if (ta.getText().indexOf('@') > -1 || ta.getText().indexOf('_') > -1) {
                    JOptionPane.showMessageDialog(dialog, "Es dürfen keine @ und _ enthalten sein.",
                            "Kommentar kann nicht gespeichert werden", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                amount.setCommentForDay(ta.getText().trim().length() > 0 ? ta.getText() : null);
                dialog.dispose();
            }
        });
        dialog.pack();
        dialog.setLocation(500, 300);
        dialog.setVisible(true);
    }

}
