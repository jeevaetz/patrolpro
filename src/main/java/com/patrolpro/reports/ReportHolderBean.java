/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.patrolpro.reports;

import java.io.Serializable;
import javax.faces.bean.SessionScoped;
import javax.inject.Named;



/**
 *
 * @author ira
 */
@Named("reportHolderBean")
@SessionScoped
public class ReportHolderBean implements Serializable {
    private GenericReport selectedReport;
    
    public ReportHolderBean() {
        
    }

    /**
     * @return the selectedReport
     */
    public GenericReport getSelectedReport() {
        return selectedReport;
    }

    /**
     * @param selectedReport the selectedReport to set
     */
    public void setSelectedReport(GenericReport selectedReport) {
        this.selectedReport = selectedReport;
    }
}
