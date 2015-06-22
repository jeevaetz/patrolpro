/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.patrolpro.model;

import java.io.Serializable;
import schedfoxlib.model.Employee;

/**
 *
 * @author ira
 */
public class EmployeeSelected implements Serializable, Comparable {
    private Employee emp;
    private String employeeIcon;
    private Boolean isSelected;
    
    public EmployeeSelected() {
        isSelected = true;
    }

    /**
     * @return the emp
     */
    public Employee getEmp() {
        return emp;
    }

    /**
     * @param emp the emp to set
     */
    public void setEmp(Employee emp) {
        this.emp = emp;
    }

    /**
     * @return the isSelected
     */
    public Boolean getIsSelected() {
        return isSelected;
    }

    /**
     * @param isSelected the isSelected to set
     */
    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public int compareTo(Object o) {
        try {
            return this.emp.getEmployeeLastName().compareTo(((EmployeeSelected)o).getEmp().getEmployeeLastName());
        } catch (Exception exe) {}
        return 0;
    }

    /**
     * @return the employeeIcon
     */
    public String getEmployeeIcon() {
        return employeeIcon;
    }

    /**
     * @param employeeIcon the employeeIcon to set
     */
    public void setEmployeeIcon(String employeeIcon) {
        this.employeeIcon = employeeIcon;
    }
    
}
