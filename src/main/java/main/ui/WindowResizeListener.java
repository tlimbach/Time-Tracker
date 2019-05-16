package main.ui;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class WindowResizeListener extends ComponentAdapter implements
        ComponentListener {

    private final Dimension dimmin, dimmax;

    public WindowResizeListener(Dimension dimmin, Dimension dimmax) {
        this.dimmin = dimmin;
        this.dimmax = dimmax;
    }

    public void componentResized(ComponentEvent e) {

        final ProjectListWindow cwp = (ProjectListWindow) e.getSource();
        Dimension size = (cwp).getSize();

        int x = size.width;
        int y = size.height;

        if (size.width < dimmin.width) {
            x = dimmin.width;
        } else if (size.width > dimmax.width) {
            x = dimmax.width;
        }

        if (size.height < dimmin.height) {
            y = dimmin.height;
        } else if (size.height > dimmax.height) {
            y = dimmax.height;
        }

        cwp.setSize(new Dimension(x, y));

    }

}
