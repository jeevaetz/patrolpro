/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans.admin;

import com.patrolpro.beans.interfaces.EditODRInterface;
import com.patrolpro.model.LogInLogOut;
import com.patrolpro.reports.GenericReport;
import com.patrolpro.reports.ReportHolderBean;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import schedfoxlib.model.ClientEquipment;
import schedfoxlib.model.Employee;
import schedfoxlib.model.IncidentReportType;
import schedfoxlib.model.OfficerDailyReport;
import schedfoxlib.services.EmployeeService;
import schedfoxlib.services.EquipmentService;
import schedfoxlib.services.IncidentService;
import schedfoxlib.services.OfficerDailyReportService;

/**
 *
 * @author ira
 */
@Named("adminReportBean")
@javax.faces.view.ViewScoped
public class AdminReportBean implements Serializable {

    @Inject
    private SessionBean sessionBean;

    private ReportHolderBean reportHolderBean;

    private ArrayList<GenericReport> reports = new ArrayList<GenericReport>();
    private GenericReport selectedReport;
    private TreeMap<Integer, Employee> hashEmployee;
    private ArrayList<Employee> employees;
    private HashMap<Integer, IncidentReportType> incidentTypes;
    private String selectedValue;
    private ArrayList<LogInLogOut> logInLogOut;

    public AdminReportBean() {

    }

    @PostConstruct
    public void postConstruct() {
        try {
            Calendar startDate = Calendar.getInstance();
            Calendar endDate = Calendar.getInstance();
            startDate.add(Calendar.DAY_OF_WEEK, -3);

            FacesContext context = FacesContext.getCurrentInstance();
            reportHolderBean = context.getApplication().evaluateExpressionGet(context, "#{reportHolderBean}", ReportHolderBean.class);

            SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

            OfficerDailyReportService officerService = new OfficerDailyReportService(sessionBean.getCompanyId());
            ArrayList<OfficerDailyReport> odrReports = officerService.getDailyReportsForDates(myFormat.format(startDate.getTime()), myFormat.format(endDate.getTime()), true);

            try {
                EquipmentService equipService = new EquipmentService(sessionBean.getCompanyId());
                HashMap<Integer, ClientEquipment> clientEquipHash = new HashMap<Integer, ClientEquipment>();
                for (int r = 0; r < odrReports.size(); r++) {
                    if (clientEquipHash.get(odrReports.get(r).getClientEquipmentId()) == null) {
                        ClientEquipment equip = equipService.getClientEquipmentById(odrReports.get(r).getClientEquipmentId());
                        clientEquipHash.put(odrReports.get(r).getClientEquipmentId(), equip);
                    }
                    odrReports.get(r).setClientEquipment(clientEquipHash.get(odrReports.get(r).getClientEquipmentId()));
                }
            } catch (Exception exe) {
            }

            this.logInLogOut = LogInLogOut.generateData(odrReports, sessionBean.getCompanyId());
        } catch (Exception exe) {
        }

        try {
            EmployeeService empService = new EmployeeService(sessionBean.getCompanyId());
            employees = empService.getAllActiveEmployees();
            hashEmployee = new TreeMap<Integer, Employee>();
            for (int r = 0; r < employees.size(); r++) {
                if (hashEmployee.get(employees.get(r).getEmployeeId()) == null) {
                    hashEmployee.put(employees.get(r).getEmployeeId(), employees.get(r));
                }
            }
        } catch (Exception exe) {
            exe.printStackTrace();
        }

        try {
            incidentTypes = new HashMap<Integer, IncidentReportType>();
            IncidentService incidentService = new IncidentService(sessionBean.getCompanyId());
            ArrayList<IncidentReportType> types = incidentService.getIncidentReportTypes();
            for (int t = 0; t < types.size(); t++) {
                incidentTypes.put(types.get(t).getIncidentReportTypeId(), types.get(t));
            }
        } catch (Exception exe) {
            exe.printStackTrace();
        }

        reports = GenericReport.getReport();
        this.selectedReport = reports.get(0);
        this.selectedReport.setClientId(-1);
    }

    public void onSelectTable() {
        String selectedLogInLogOutODRs = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("logInOut");
        for (int r = 0; r < this.logInLogOut.size(); r++) {
            if (selectedLogInLogOutODRs.equals(logInLogOut.get(r).getODRIds())) {
                FacesContext context = FacesContext.getCurrentInstance();
                EditODRInterface bean = context.getApplication().evaluateExpressionGet(context, "#{editODRBean}", EditODRInterface.class);
                bean.setSelectedLogInLogOut(logInLogOut.get(r));
                System.out.println("here");
            }
        }
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

    public void setSelectedReportObj(GenericReport genericReport) {
        this.selectedReport = genericReport;
    }

    public GenericReport getSelectedReportObj() {
        return this.selectedReport;
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

    public void selectReport() {
        reportHolderBean.setSelectedReport(selectedReport);
    }

    /**
     * @return the selectedReport
     */
    public String getSelectedReport() {
        return selectedReport.getReportName();
    }

    public void setSelectedReport(GenericReport selectedReport) {
        this.selectedReport = selectedReport;
    }

    /**
     * @param selectedReport the selectedReport to set
     */
    public void setSelectedReport(String selectedReport) {
        this.selectedReport = null;
        for (int i = 0; i < reports.size(); i++) {
            if (reports.get(i).getReportName().equals(selectedReport)) {
                this.selectedReport = reports.get(i);
                this.selectedReport.setClientId(-1);
            }
        }
    }

    /**
     * @return the selectedValue
     */
    public String getSelectedValue() {
        return selectedValue;
    }

    /**
     * @param selectedValue the selectedValue to set
     */
    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }

    /**
     * @return the logInLogOut
     */
    public ArrayList<LogInLogOut> getLogInLogOut() {
        return logInLogOut;
    }

    /**
     * @param logInLogOut the logInLogOut to set
     */
    public void setLogInLogOut(ArrayList<LogInLogOut> logInLogOut) {
        this.logInLogOut = logInLogOut;
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
}
