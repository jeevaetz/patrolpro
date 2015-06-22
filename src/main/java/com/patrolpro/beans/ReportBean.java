/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import com.patrolpro.beans.interfaces.RefreshClientListingInterface;
import com.patrolpro.reports.GenericReport;
import com.patrolpro.reports.ReportHolderBean;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import schedfoxlib.model.Employee;
import schedfoxlib.model.IncidentReportType;
import schedfoxlib.services.EquipmentService;
import schedfoxlib.services.IncidentService;

/**
 *
 * @author user
 */
@Named("reportBean")
@ViewScoped
public class ReportBean implements Serializable, RefreshClientListingInterface {

    private ArrayList<GenericReport> reports = new ArrayList<GenericReport>();

    private GenericReport selectedReport;

    @Inject
    private ClientSessionBean mySessionBean;

    private ReportHolderBean reportHolderBean;

    private TreeMap<Integer, Employee> hashEmployee;
    private ArrayList<Employee> employees;
    private HashMap<Integer, IncidentReportType> incidentTypes;

    public ReportBean() {

    }

    @PostConstruct
    public void postConstruct() {
        this.refresh();
    }

    public String getFormattedDate(Date inputDate) {
        try {
            ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", mySessionBean.getLocale());
            String dateStyle = rb.getString("shortDateStyle");
            return new SimpleDateFormat(dateStyle).format(inputDate);
        } catch (Exception exe) {
        }
        return "";
    }

    @Override
    public void refresh() {
        reports = GenericReport.getReport();
        hashEmployee = new TreeMap<Integer, Employee>();

        FacesContext context = FacesContext.getCurrentInstance();
        reportHolderBean = context.getApplication().evaluateExpressionGet(context, "#{reportHolderBean}", ReportHolderBean.class);

        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1);
        try {
            EquipmentService equipService = new EquipmentService(mySessionBean.getCompanyId());
            employees = equipService.getEmployeesAtClient(this.mySessionBean.getSelectedClient().getClientId());
            for (int r = 0; r < employees.size(); r++) {
                if (hashEmployee.get(employees.get(r).getEmployeeId()) == null) {
                    hashEmployee.put(employees.get(r).getEmployeeId(), employees.get(r));
                }
            }
            this.selectedReport = reports.get(0);
            this.selectedReport.setClientId(this.mySessionBean.getSelectedClient().getClientId());
        } catch (Exception exe) {
            exe.printStackTrace();
        }
        try {
            incidentTypes = new HashMap<Integer, IncidentReportType>();
            IncidentService incidentService = new IncidentService(mySessionBean.getCompanyId());
            ArrayList<IncidentReportType> types = incidentService.getIncidentReportTypes();
            for (int t = 0; t < types.size(); t++) {
                incidentTypes.put(types.get(t).getIncidentReportTypeId(), types.get(t));
            }
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    public void selectReport() {
        reportHolderBean.setSelectedReport(selectedReport);
    }

    /**
     * @return the reports
     */
    public ArrayList<GenericReport> getReports() {
        return reports;
    }

    /**
     * @param reports the reports to set
     */
    public void setReports(ArrayList<GenericReport> reports) {
        this.reports = reports;
    }

    public GenericReport getSelectedReportObj() {
        return this.selectedReport;
    }

    public void setSelectedReportObj(GenericReport genericReport) {
        this.selectedReport = genericReport;
    }

    /**
     * @return the selectedReport
     */
    public String getSelectedReport() {
        if (selectedReport != null) {
            return selectedReport.getReportName();
        }
        return "";
    }

    /**
     * @param selectedReport the selectedReport to set
     */
    public void setSelectedReport(String selectedReport) {
        this.selectedReport = null;
        for (int i = 0; i < reports.size(); i++) {
            if (reports.get(i).getReportName().equals(selectedReport)) {
                this.selectedReport = reports.get(i);
                this.selectedReport.setClientId(this.mySessionBean.getSelectedClient().getClientId());
            }
        }
    }

    /**
     * @return the hashEmployee
     */
    public TreeMap<Integer, Employee> getHashEmployee() {
        return hashEmployee;
    }

    /**
     * @param hashEmployee the hashEmployee to set
     */
    public void setHashEmployee(TreeMap<Integer, Employee> hashEmployee) {
        this.hashEmployee = hashEmployee;
    }

    /**
     * @return the incidentTypes
     */
    public HashMap<Integer, IncidentReportType> getIncidentTypes() {
        return incidentTypes;
    }

    /**
     * @param incidentTypes the incidentTypes to set
     */
    public void setIncidentTypes(HashMap<Integer, IncidentReportType> incidentTypes) {
        this.incidentTypes = incidentTypes;
    }

    /**
     * @return the employees
     */
    public ArrayList<Employee> getEmployees() {
        return employees;
    }

    /**
     * @param employees the employees to set
     */
    public void setEmployees(ArrayList<Employee> employees) {
        this.employees = employees;
    }

    /**
     * @return the mySessionBean
     */
    public ClientSessionBean getMySessionBean() {
        return mySessionBean;
    }

    /**
     * @param mySessionBean the mySessionBean to set
     */
    public void setMySessionBean(ClientSessionBean mySessionBean) {
        this.mySessionBean = mySessionBean;
    }

}
