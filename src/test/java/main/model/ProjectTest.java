package main.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import main.Helper;

@RunWith(JUnitPlatform.class)
class ProjectTest {

    private static final String PREFIX = "_TEST";

    @Test
    void testProject() {
        Project project = new Project(PREFIX + "pname", "number", ".");

        assertThat(project.getProjektName(), is(PREFIX + "pname"));
        assertThat(project.getProjektNumber(), is("number"));
        assertThat(project.getTotalSecondsThisMonth(), is(0));

        WorkAmount cwa = project.getCurrentWorkAmount();

        assertNull(cwa.getCommentForDay());

        cwa.setCommentForDay("testbla");
        assertThat(cwa.getCommentForDay(), is("testbla"));
    }

    @Test
    void testIncreaseTime() throws Exception {
        Project project = new Project(PREFIX + "name", "num", ".");
        WorkAmount cwa = project.getCurrentWorkAmount();

        assertNull(cwa.getCommentForDay());
        assertThat(cwa.getSecondsThatDay(), is(0));

        project.increaseTime();
        project.increaseTime();
        project.increaseTime();

        assertThat(cwa.getSecondsThatDay(), is(3));

        cwa.setSecondsThatDay(100);
        assertThat(Helper.getSecondsAsTimeString(3), is("00:00:03"));
        assertThat(Helper.getSecondsAsTimeString(100), is("00:01:40"));
    }

    @Test
    void testLoadSave() {

        Project project = new Project(PREFIX + "test", "num", ".");
        project.increaseTime();
        project.save();

        WorkAmount cwa = project.getCurrentWorkAmount();
        cwa.setCommentForDay("testkommentar");
        cwa.setSecondsThatDay(180);

        project.save();

        project = null;
        cwa = null;

        project = new Project(PREFIX + "test", null, ".");

        cwa = project.getCurrentWorkAmount();
        assertThat(cwa.getCommentForDay(), is("testkommentar"));
        assertThat(cwa.getSecondsThatDay(), is(180));
    }

    @AfterAll
    public static void afterall() {
        delete("pname");
        delete("name");
        delete("test");

    }

    private static void delete(String name) {
        new File(PREFIX + name + ".bak").deleteOnExit();
        new File(PREFIX + name + ".cpr").deleteOnExit();
    }

}
