/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import java.io.Serializable;
import java.util.ArrayList;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import schedfoxlib.model.Employee;
import schedfoxlib.services.EmployeeService;

/**
 *
 * @author user
 */
@Named("guardsBean")
@ViewScoped
public class GuardsBean implements Serializable {
    
    private ArrayList<Employee> employees;
    private Employee selectedEmployee;
    
    public GuardsBean() {
        
    }
    
    public void addGuard() {
        selectedEmployee = new Employee();
    }
    
    public void saveGuard() {
        if (selectedEmployee != null) {
            try {
                EmployeeService employeeService = new EmployeeService();
                employeeService.saveEmployee(selectedEmployee);
            } catch (Exception exe) {
                exe.printStackTrace();
            }
        }
    }
    
    public ArrayList<Employee> getEmployees() {
        if (employees == null) {
            try {
                EmployeeService empService = new EmployeeService();
                employees = empService.getAllActiveEmployees();
            } catch (Exception exe) {
                exe.printStackTrace();
            }
        }
        return employees;
    }
    
    public void setEmployees(ArrayList<Employee> employees) {
        this.employees = employees;
    }

    /**
     * @return the selectedEmployee
     */
    public Employee getSelectedEmployee() {
        return selectedEmployee;
    }

    /**
     * @param selectedEmployee the selectedEmployee to set
     */
    public void setSelectedEmployee(Employee selectedEmployee) {
        this.selectedEmployee = selectedEmployee;
    }
    
}
