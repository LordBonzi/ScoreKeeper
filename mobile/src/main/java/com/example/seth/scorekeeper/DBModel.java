package com.example.seth.scorekeeper;

/**
 * Created by Seth Schroeder on 21/05/2016.
 */

public class DBModel {

    int rowID;
    String players;
    String scores;


    public int getRowID() {
        return rowID;
    }

    public void setRowID(int rowID) {
        this.rowID = rowID;
    }

    public String getPlayers() {
        return players;
    }

    public void setPlayers(String players) {
        this.players = players;
    }

    public String getScores() {
        return scores;
    }

    public void setScores(String scores) {
        this.scores = scores;
    }
}
