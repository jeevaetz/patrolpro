/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.model;

import java.util.ArrayList;

/**
 *
 * @author ira
 */
public class JFlotLine {
    private String label;
    private ArrayList<long[]> data;
    
    public JFlotLine() {
        label = "";
        data = new ArrayList<long[]>();
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the data
     */
    public ArrayList<long[]> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(ArrayList<long[]> data) {
        this.data = data;
    }
}
