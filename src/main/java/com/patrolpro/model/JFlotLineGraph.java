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
public class JFlotLineGraph {

    private JFlotLine[] data;

    public JFlotLineGraph() {
        this.data = new JFlotLine[0];
    }

    /**
     * @return the data
     */
    public JFlotLine[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(JFlotLine[] data) {
        this.data = data;
    }

}
