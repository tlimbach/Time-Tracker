package main.model;

public class WorkAmount {

    private int day;

    private int month;

    private int year;

    private int secondsThatDay;

    private String commentForDay = null;

    public WorkAmount(int day, int month, int year, int seconds, String commentForDay) {
        this.day = day;
        this.month = month;
        this.year = year;

        this.setSecondsThatDay(seconds);
        this.commentForDay = commentForDay;
    }

    public void increaseSeconds() {
        setSecondsThatDay(getSecondsThatDay() + 1);
    }

    public String getCommentForDay() {
        return commentForDay;
    }

    public void setCommentForDay(String commentForDay) {

        if (commentForDay != null && commentForDay.equals("null")) {
            commentForDay = null;
        }

        this.commentForDay = commentForDay;
    }

    public boolean hasCommentForDay() {
        return this.commentForDay != null;
    }

    public int getSecondsThatDay() {
        return secondsThatDay;
    }

    public void setSecondsThatDay(int secondsThatDay) {
        this.secondsThatDay = secondsThatDay;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }
}
