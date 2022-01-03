package com.example.ndfsa_to_dfsa;

import java.util.HashMap;

public class States {
    private String state;
    private boolean final_stat=false;
    private HashMap<String,String> states_trans=new HashMap<>();
    private boolean marked=false;
    private boolean isVisited=false;

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean visited) {
        isVisited = visited;
    }

    public States(String state, boolean final_stat) {
        this.state = state;
        this.final_stat = final_stat;
    }

    public boolean isFinal_stat() {
        return final_stat;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }


    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    public boolean isFinal_stat(boolean b) {
        return final_stat;
    }

    public void setFinal_stat(boolean final_stat) {
        this.final_stat = final_stat;
    }

    public HashMap<String, String> getStates_trans() {
        return states_trans;
    }

    public void setStates_trans(HashMap<String, String> states_trans) {
        this.states_trans = states_trans;
    }



}
