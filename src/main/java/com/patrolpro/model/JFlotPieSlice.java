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
public class JFlotPieSlice implements Serializable {
    private String label;
    private Integer data;
    
    public JFlotPieSlice() {
        data = 0;
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
     * @return the percent
     */
    public Integer getData() {
        return data;
    }

    /**
     * @param percent the percent to set
     */
    public void setData(Integer data) {
        this.data = data;
    }
    
    
}
