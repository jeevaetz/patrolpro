/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.model;

import java.io.Serializable;

/**
 *
 * @author ira
 */
public class JFlotPieGraph implements Serializable {
    private JFlotPieSlice[] data;
    
    public JFlotPieGraph() {
        
    }

    /**
     * @return the data
     */
    public JFlotPieSlice[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(JFlotPieSlice[] data) {
        this.data = data;
    }
}
