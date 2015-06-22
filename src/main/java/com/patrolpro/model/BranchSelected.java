/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.model;

import schedfoxlib.model.Branch;

/**
 *
 * @author ira
 */
public class BranchSelected {
    private Boolean amISelected;
    private Branch currBranch;
    
    public BranchSelected() {

    }

    /**
     * @return the amISelected
     */
    public Boolean getAmISelected() {
        return amISelected;
    }

    /**
     * @param amISelected the amISelected to set
     */
    public void setAmISelected(Boolean amISelected) {
        this.amISelected = amISelected;
    }

    /**
     * @return the currBranch
     */
    public Branch getCurrBranch() {
        return currBranch;
    }

    /**
     * @param currBranch the currBranch to set
     */
    public void setCurrBranch(Branch currBranch) {
        this.currBranch = currBranch;
    }
}
