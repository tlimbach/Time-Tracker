package main.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class ProjektDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final int BORDERWIDTH = 20;

    JTextField tf_name, tf_number;

    JButton btn_ok, btn_abbrechen;

    private boolean res;

    public ProjektDialog() {
    }

    public void setTitleText(String text) {
        setTitle(text);
    }

    private void init() {
        Color backgroundColor = new Color(235, 235, 235);

        BorderLayout borderLayout = new BorderLayout();

        setLayout(borderLayout);

        JPanel mainpanel = new JPanel();
        mainpanel.setBackground(backgroundColor);
        mainpanel.setLayout(new GridLayout(3, 1));

        setTitle("Projekt einrichten");

        mainpanel.add(tf_name = new JTextField(15));
        TitledBorder titledBorder = new TitledBorder("Projektname");
        titledBorder.setTitleColor(Color.black);

        tf_name.setBorder(titledBorder);
        tf_name.addActionListener(this);
        tf_name.setBackground(backgroundColor);
        mainpanel.add(tf_number = new JTextField(15));
        tf_number.setBorder(new TitledBorder("Projektnummer"));
        tf_number.addActionListener(this);
        tf_number.setBackground(backgroundColor);

        JPanel pnl_button = new JPanel();
        pnl_button.setBackground(backgroundColor);
        pnl_button.setLayout(new FlowLayout(FlowLayout.RIGHT));
        pnl_button.add(btn_ok = new JButton("ok"));
        btn_ok.addActionListener(this);
        pnl_button.add(btn_abbrechen = new JButton("Abbrechen"));
        btn_abbrechen.addActionListener(this);
        mainpanel.add(pnl_button);
        add(mainpanel, BorderLayout.CENTER);
        JPanel eastPanel = new JPanel();
        eastPanel.setPreferredSize(new Dimension(BORDERWIDTH, 0));
        eastPanel.setBackground(backgroundColor);
        JPanel westPanel = new JPanel();
        westPanel.setPreferredSize(new Dimension(BORDERWIDTH, 0));
        westPanel.setBackground(backgroundColor);

        add(westPanel, BorderLayout.WEST);
        add(eastPanel, BorderLayout.EAST);

        pack();

        setLocationRelativeTo(getParent());
    }

    public String getProjectNumber() {
        return tf_number.getText();
    }

    public String getProjectName() {
        return tf_name.getText();
    }

    public boolean open(String presetName, String presetNumber) {
        init();

        if (presetName != null) {
            tf_name.setText(presetName);
        }

        if (presetNumber != null && !presetNumber.equals("null")) {
            tf_number.setText(presetNumber);
        }

        setModal(true);
        setVisible(true);
        return res;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btn_ok) {
            res = true;
            setVisible(false);
            dispose();
        } else if (e.getSource() == btn_abbrechen) {
            res = false;
            setVisible(false);
            dispose();
        } else if (e.getSource() == tf_name) {
            tf_number.requestFocus();
        } else if (e.getSource() == tf_number) {
            btn_ok.doClick();
        }
    }

}
