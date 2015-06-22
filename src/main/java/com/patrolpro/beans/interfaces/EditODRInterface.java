/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.patrolpro.beans.interfaces;

import com.patrolpro.model.LogInLogOut;
import schedfoxlib.model.OfficerDailyReport;
import schedfoxlib.model.OfficerDailyReportText;


public interface EditODRInterface {

    /**
     * @return the selectedLogInLogOut
     */
    public LogInLogOut getSelectedLogInLogOut();

    /**
     * @param selectedLogInLogOut the selectedLogInLogOut to set
     */
    public void setSelectedLogInLogOut(LogInLogOut selectedLogInLogOut);
    
    public void onRowSelect();
    
    public void clearODR();
    
    public void editNote(OfficerDailyReportText reportText);

    /**
     * @return the selectedODR
     */
    public OfficerDailyReport getSelectedODR();

    /**
     * @param selectedODR the selectedODR to set
     */
    public void setSelectedODR(OfficerDailyReport selectedODR);

    /**
     * @return the editingReportText
     */
    public OfficerDailyReportText getEditingReportText();

    /**
     * @param editingReportText the editingReportText to set
     */
    public void setEditingReportText(OfficerDailyReportText editingReportText);
    
    public void saveReportText();
}
