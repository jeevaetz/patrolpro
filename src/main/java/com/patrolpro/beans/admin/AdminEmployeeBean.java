/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans.admin;

import com.patrolpro.beans.interfaces.BranchRefreshInterface;
import com.patrolpro.beans.interfaces.UploadFileInterface;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.ImageIcon;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import schedfoxlib.model.Company;
import schedfoxlib.model.Employee;
import schedfoxlib.model.util.ImageLoader;
import schedfoxlib.services.CompanyService;
import schedfoxlib.services.EmployeeService;

/**
 *
 * @author ira
 */
@Named("adminEmployeeBean")
@ViewScoped
public class AdminEmployeeBean implements Serializable, UploadFileInterface, BranchRefreshInterface {

    @Inject
    private SessionBean sessionBean;

    private ArrayList<Employee> employees;
    private Employee selectedEmployee;

    private Boolean showDeleted;

    private UIComponent employeeFirstNameTxt;
    private UIComponent employeeLastNameTxt;
    private UIComponent employeeSSN;
    private UIComponent employeeDOB;
    private UIComponent employeeAddress1;
    private UIComponent employeeCity;

    public AdminEmployeeBean() {

    }

    @PostConstruct
    public void postConstruct() {
        try {
            selectBranch();
        } catch (Exception exe) {
        }
    }
    
    //Do not remove dummy action
    public void dummyAction() {
        
    }

    public void loadEmployeeInfo() {
        try {
            String selectedEmployeeId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("employeeId");
            Integer selectedEmployeeInt = Integer.parseInt(selectedEmployeeId);
            sessionBean.setAdminSelectedEmployeeId(selectedEmployeeInt);
            for (int r = 0; r < employees.size(); r++) {
                if (employees.get(r).getEmployeeId().equals(selectedEmployeeInt)) {
                    selectedEmployee = employees.get(r);
                }
            }
            try {
                ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", sessionBean.getLocale());
                ((HtmlInputText)employeeDOB).setValue(new SimpleDateFormat(rb.getString("shortDateStyle")).format(selectedEmployee.getEmployeeBirthdate()));
            } catch (Exception exe) {}
        } catch (Exception exe) {
        }
    }

    public void addNew() {
        selectedEmployee = new Employee();
        try {
            selectedEmployee.setBranchId(sessionBean.getSelectedBranchId());
        } catch (Exception exe) {
            FacesContext.getCurrentInstance().addMessage("badBranch", new FacesMessage(exe.getMessage()));
        }
    }

    /**
     * @return the clients
     */
    public ArrayList<Employee> getEmployees() {
        return employees;
    }

    /**
     * @param clients the clients to set
     */
    public void setClients(ArrayList<Employee> employees) {
        this.employees = employees;
    }

    /**
     * @return the selectedEmployee
     */
    public Employee getSelectedEmployee() {
        return selectedEmployee;
    }

    public String getDOBFormatted() {
        try {
            ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", sessionBean.getLocale());
            return new SimpleDateFormat(rb.getString("shortDateStyle")).format(selectedEmployee.getEmployeeBirthdate());
        } catch (Exception exe) {}
        return "";
    }
    
    public void setDOBFormatted(String dob) {
        try {
            ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", sessionBean.getLocale());
            selectedEmployee.setEmployeeBirthdate(new SimpleDateFormat(rb.getString("shortDateStyle")).parse(dob));
        } catch (Exception exe) {}
    }
    
    /**
     * @param selectedEmployee the selectedEmployee to set
     */
    public void setSelectedEmployee(Employee selectedEmployee) {
        this.selectedEmployee = selectedEmployee;
    }

    public void onRowSelect(SelectEvent event) {
    }

    public void save() {
        EmployeeService employeeService = new EmployeeService(sessionBean.getCompanyId());
        boolean isValid = true;
        try {
            ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", sessionBean.getLocale());
            
            selectedEmployee.setBranchId(sessionBean.getSelectedBranchId());
            FacesContext currContext = FacesContext.getCurrentInstance();
            if (selectedEmployee.getEmployeeFirstName() == null || selectedEmployee.getEmployeeFirstName().trim().length() == 0) {
                currContext.addMessage(employeeFirstNameTxt.getClientId(currContext), new FacesMessage(FacesMessage.SEVERITY_WARN, null, rb.getString("enterAFirstName")));
                isValid = false;
            }
            if (selectedEmployee.getEmployeeLastName() == null || selectedEmployee.getEmployeeLastName().trim().length() == 0) {
                currContext.addMessage(employeeLastNameTxt.getClientId(currContext), new FacesMessage(FacesMessage.SEVERITY_WARN, null, rb.getString("enterALastName")));
                isValid = false;
            }
            if (selectedEmployee.getEmployeeSsn() == null || selectedEmployee.getEmployeeSsn().trim().length() == 0) {
                if (employeeSSN != null) {
                    currContext.addMessage(employeeSSN.getClientId(currContext), new FacesMessage(FacesMessage.SEVERITY_WARN, null, rb.getString("enterASSN")));
                    isValid = false;
                }
            }
            try {
                selectedEmployee.setEmployeeBirthdate(new SimpleDateFormat(rb.getString("shortDateStyle")).parse(((HtmlInputText)employeeDOB).getValue().toString()));
            } catch (Exception exe) {}
            
            if (selectedEmployee.getEmployeeBirthdate() == null) {
                currContext.addMessage(employeeDOB.getClientId(currContext), new FacesMessage(FacesMessage.SEVERITY_WARN, null, rb.getString("enterADOB")));
                isValid = false;
            }
            if (selectedEmployee.getEmployeeAddress() == null || selectedEmployee.getEmployeeAddress().trim().length() == 0) {
                currContext.addMessage(employeeAddress1.getClientId(currContext), new FacesMessage(FacesMessage.SEVERITY_WARN, null, rb.getString("enterAAddress")));
                isValid = false;
            }
            if (this.employeeCity.isRendered() && (selectedEmployee.getEmployeeCity() == null || selectedEmployee.getEmployeeCity().trim().length() == 0)) {
                currContext.addMessage(employeeCity.getClientId(currContext), new FacesMessage(FacesMessage.SEVERITY_WARN, null, "Please enter a city!"));
                isValid = false;
            }
            if (this.employeeCity.isRendered() && (selectedEmployee.getEmployeeState() == null || selectedEmployee.getEmployeeState().trim().length() == 0)) {
                currContext.addMessage(employeeCity.getClientId(currContext), new FacesMessage(FacesMessage.SEVERITY_WARN, null, "Please enter a state!"));
                isValid = false;
            }
            if (this.employeeCity.isRendered() && (selectedEmployee.getEmployeeZip() == null || selectedEmployee.getEmployeeZip().trim().length() == 0)) {
                currContext.addMessage(employeeCity.getClientId(currContext), new FacesMessage(FacesMessage.SEVERITY_WARN, null, "Please enter a zipcode!"));
                isValid = false;
            }
            if (isValid) {
                try {
                    employeeService.saveEmployee(selectedEmployee);

                    RequestContext.getCurrentInstance().execute("$('#EmployeeModal').modal('hide')");

                    selectBranch();
                    this.selectedEmployee = null;
                } catch (Exception exe) {
                }
            }
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    /**
     * @return the sessionBean
     */
    public SessionBean getSessionBean() {
        return sessionBean;
    }

    /**
     * @param sessionBean the sessionBean to set
     */
    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    /**
     * @return the showDeleted
     */
    public Boolean getShowDeleted() {
        return showDeleted;
    }

    /**
     * @param showDeleted the showDeleted to set
     */
    public void setShowDeleted(Boolean showDeleted) {
        this.showDeleted = showDeleted;
    }

    /**
     * @return the employeeFirstNameTxt
     */
    public UIComponent getEmployeeFirstNameTxt() {
        return employeeFirstNameTxt;
    }

    /**
     * @param employeeFirstNameTxt the employeeFirstNameTxt to set
     */
    public void setEmployeeFirstNameTxt(UIComponent employeeFirstNameTxt) {
        this.employeeFirstNameTxt = employeeFirstNameTxt;
    }

    /**
     * @return the employeeLastNameTxt
     */
    public UIComponent getEmployeeLastNameTxt() {
        return employeeLastNameTxt;
    }

    /**
     * @param employeeLastNameTxt the employeeLastNameTxt to set
     */
    public void setEmployeeLastNameTxt(UIComponent employeeLastNameTxt) {
        this.employeeLastNameTxt = employeeLastNameTxt;
    }

    /**
     * @return the employeeSSN
     */
    public UIComponent getEmployeeSSN() {
        return employeeSSN;
    }

    /**
     * @param employeeSSN the employeeSSN to set
     */
    public void setEmployeeSSN(UIComponent employeeSSN) {
        this.employeeSSN = employeeSSN;
    }

    /**
     * @return the employeeDOB
     */
    public UIComponent getEmployeeDOB() {
        return employeeDOB;
    }

    /**
     * @param employeeDOB the employeeDOB to set
     */
    public void setEmployeeDOB(UIComponent employeeDOB) {
        this.employeeDOB = employeeDOB;
    }

    /**
     * @return the employeeAddress1
     */
    public UIComponent getEmployeeAddress1() {
        return employeeAddress1;
    }

    /**
     * @param employeeAddress1 the employeeAddress1 to set
     */
    public void setEmployeeAddress1(UIComponent employeeAddress1) {
        this.employeeAddress1 = employeeAddress1;
    }

    /**
     * @return the employeeCity
     */
    public UIComponent getEmployeeCity() {
        return employeeCity;
    }

    /**
     * @param employeeCity the employeeCity to set
     */
    public void setEmployeeCity(UIComponent employeeCity) {
        this.employeeCity = employeeCity;
    }

    @Override
    public String processFileUpload(String companyId, byte[] imageData, Object objectKey) {
        EmployeeService empService = new EmployeeService(companyId);
        try {
            selectedEmployee = empService.getEmployeeById((Integer) objectKey);
            if (selectedEmployee == null || selectedEmployee.getEmployeeId() == null) {
                return "save_object_first";
            } else {
                try {
                    CompanyService companyController = new CompanyService();
                    Company comp = companyController.getCompanyById(Integer.parseInt(companyId));
                    
                    ImageIcon myIcon = new ImageIcon(ImageIO.read(new BufferedInputStream(new ByteArrayInputStream(imageData))));
                    ImageLoader.saveImage(selectedEmployee.getEmployeeId() + ".jpg", comp.getDB(), "employee", myIcon);
                    return "success";
                } catch (Exception exe) {
                    return "error";
                }
            }
        } catch (Exception exe) {
            return "error";
        }
    }

    @Override
    public void selectBranch() {
        try {
            EmployeeService employeeService = new EmployeeService(sessionBean.getCompanyId());
            employees = employeeService.getAllEmployeesByBranch(sessionBean.getSelectedBranchId(), showDeleted);
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

}
