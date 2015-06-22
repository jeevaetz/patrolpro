/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import com.patrolpro.beans.interfaces.RefreshClientListingInterface;
import com.patrolpro.beans.interfaces.EditODRInterface;
import com.patrolpro.model.LogInLogOut;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;


import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;
import schedfoxlib.model.Branch;
import schedfoxlib.model.Client;
import schedfoxlib.model.ClientEquipment;
import schedfoxlib.model.Employee;
import schedfoxlib.model.OfficerDailyReport;
import schedfoxlib.model.OfficerDailyReportText;
import schedfoxlib.services.EmployeeService;
import schedfoxlib.services.EquipmentService;
import schedfoxlib.services.GenericService;
import schedfoxlib.services.OfficerDailyReportService;

/**
 *
 * @author user
 */
@Named("dashboardBean")
@ViewScoped
public class DashboardBean implements Serializable, RefreshClientListingInterface, EditODRInterface {

    private Date selectedDate;
    private Integer numberOfDaysToGoBack;
    private ArrayList<OfficerDailyReport> clientDailyReports;
    private ChartSeries incidents;
    private ArrayList<Date> dateObjects;
    private CartesianChartModel incidentModel;
    private OfficerDailyReportText selectedODRText;
    
    @Inject
    private ClientSessionBean sessionBean;
    
    private OfficerDailyReport selectedODR;
    private boolean showAllLogInData;
    private ArrayList<LogInLogOut> logInLogOut;
    private LogInLogOut selectedLogInLogOut;
    private LogInLogOut logInLogOutToDelete;
    private LogInLogOut logInLogOutToCheckout;
    private OfficerDailyReportText editingReportText;
    
    private ArrayList<Employee> employees;
    
    private Branch clientBranch;
    
    /**
     * Creates a new instance of DashboardBean
     */
    public DashboardBean() {
        numberOfDaysToGoBack = 60;
    }
    
    @PostConstruct
    public void postConstruct() {
        showAllLogInData = true;
        selectedDate = new Date();
        clientDailyReports = new ArrayList<OfficerDailyReport>();
        editingReportText = new OfficerDailyReportText();

        this.refreshEmployeeList();
        this.refresh();
    }
    
    public void refreshEmployeeList() {
        try {
            EmployeeService empService = new EmployeeService(sessionBean.getCompanyId());
            employees = empService.getAllActiveEmployees();
        } catch (Exception exe) {}
    }

    public void setLogInLogOutToCheckOut(LogInLogOut logInOut) {
        logInLogOutToCheckout = logInOut;
        RequestContext.getCurrentInstance().execute("$('#CheckoutConfirmationModal').modal('show')");
    }
    
    public void setLogInLogOutToDelete(LogInLogOut logInOut) {
        this.logInLogOutToDelete = logInOut;
        RequestContext.getCurrentInstance().execute("$('#ConfirmDeleteModal').modal('show')");
    }
    
    public void checkOutLogInLogOut() {
        try {
            OfficerDailyReportService officerService = new OfficerDailyReportService(sessionBean.getCompanyId());
            GenericService genericService = new GenericService(sessionBean.getCompanyId());
            Date logOutTime = new Date(genericService.getCurrentTimeMillis());
            ArrayList<OfficerDailyReport> reports = this.logInLogOutToCheckout.getReports();
            for (int r = 0; r < reports.size(); r++) {
                OfficerDailyReport report = reports.get(r);
                report.setLoggedOut(logOutTime);
                officerService.saveDailyReport(report);
            }
            RequestContext.getCurrentInstance().execute("$('#CheckoutConfirmationModal').modal('hide')");
            this.refresh();
        } catch (Exception exe) {
        
        }
    }
    
    public void deleteLogInLogOut() {
        try {
            OfficerDailyReportService officerService = new OfficerDailyReportService(sessionBean.getCompanyId());
            
            ArrayList<OfficerDailyReport> reports = this.logInLogOutToDelete.getReports();
            for (int r = 0; r < reports.size(); r++) {
                OfficerDailyReport report = reports.get(r);
                report.setActive(false);
                officerService.saveDailyReport(report);
            }
            this.refresh();
        } catch (Exception exe) {
        
        } finally {
            RequestContext.getCurrentInstance().execute("$('#ConfirmDeleteModal').modal('hide')");
        }
    }
    
    public StreamedContent bytesToStreamedContent(byte[] bytes) {
        InputStream is = new ByteArrayInputStream(bytes);
        StreamedContent image = new DefaultStreamedContent(is, "image/png", "image");
        return image;
    }

    public String getSelectedDateString() {
        try {
            Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", locale);
            
            return new SimpleDateFormat(rb.getString("shortDateStyle")).format(this.selectedDate);
        } catch (Exception e) {
            return "";
        }
    }

    public void chartSelect(ItemSelectEvent event) {
        this.selectedDate = dateObjects.get(event.getItemIndex());
        refreshBackData();
    }

    public void blankMethod() {
    }

    /**
     * @return the selectedDate
     */
    public Date getSelectedDate() {
        return selectedDate;
    }

    /**
     * @param selectedDate the selectedDate to set
     */
    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }

    public void handleCalendarValueChange(ValueChangeEvent event) {
        System.out.println("Running");
    }

    public void onRowSelectIncident(SelectEvent event) {
    }

    /*
     * Simple refresh for all reports
     */
    public void refresh() {
        OfficerDailyReportService officerService = new OfficerDailyReportService(sessionBean.getCompanyId());
        getClientDailyReports().clear();
        EquipmentService equipService = new EquipmentService(sessionBean.getCompanyId());
        HashMap<Integer, ClientEquipment> clientEquipHash = new HashMap<Integer, ClientEquipment>();
        try {
            if (sessionBean.getClients() != null) {
                ArrayList<Integer> clientIds = new ArrayList<Integer>();
                for (int c = 0; c < sessionBean.getClients().size(); c++) {
                    clientIds.add(sessionBean.getClients().get(c).getClientId());
                }
                getClientDailyReports().addAll(officerService.getDailyReportsForClientsWithText(clientIds, numberOfDaysToGoBack, false));

                ArrayList<OfficerDailyReport> reports = getClientDailyReports();
                
                HashMap<Integer, Integer> clientEquipmentIds = new HashMap<Integer, Integer>();
                for (int c = 0; c < reports.size(); c++) {
                    clientEquipmentIds.put(reports.get(c).getClientEquipmentId(), reports.get(c).getClientEquipmentId());
                }
                
                ArrayList<Integer> fetchClientEquipIds = new ArrayList<Integer>();
                Iterator<Integer> keyIterator = clientEquipmentIds.keySet().iterator();
                while (keyIterator.hasNext()) {
                    fetchClientEquipIds.add(keyIterator.next());
                }
                ArrayList<ClientEquipment> equip = equipService.getClientEquipmentByIds(fetchClientEquipIds);
                for (int e = 0; e < equip.size(); e++) {
                    clientEquipHash.put(equip.get(e).getClientEquipmentId(), equip.get(e));
                }
                
                for (int r = 0; r < getClientDailyReports().size(); r++) {
                    getClientDailyReports().get(r).setClientEquipment(clientEquipHash.get(getClientDailyReports().get(r).getClientEquipmentId()));
                }
                logInLogOut = LogInLogOut.generateData(clientDailyReports, sessionBean.getCompanyId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshBackData() {
        OfficerDailyReportService officerService = new OfficerDailyReportService(sessionBean.getCompanyId());
        try {
            Calendar dateInstance = Calendar.getInstance();
            dateInstance.setTime(selectedDate);
            dateInstance.add(Calendar.HOUR_OF_DAY, 6);

            getClientDailyReports().clear();

            for (int c = 0; c < sessionBean.getClients().size(); c++) {
                Client currentClient = sessionBean.getClients().get(c);
                getClientDailyReports().addAll(officerService.getDailyReportsForClientAndDate(currentClient.getClientId(), dateInstance.getTime()));
            }
            logInLogOut = LogInLogOut.generateData(clientDailyReports, sessionBean.getCompanyId());
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    

    public void handleDateSelect(SelectEvent event) {
        this.selectedDate = (Date)event.getObject();
        refreshBackData();
    }

    /**
     * @return the clientDailyReports
     */
    public ArrayList<OfficerDailyReport> getClientDailyReports() {
        return clientDailyReports;
    }

    /**
     * @param clientDailyReports the clientDailyReports to set
     */
    public void setClientDailyReports(ArrayList<OfficerDailyReport> clientDailyReports) {
        this.clientDailyReports = clientDailyReports;
    }

    /**
     * @return the incidentModel
     */
    public CartesianChartModel getIncidentModel() {
        return incidentModel;
    }

    /**
     * @param incidentModel the incidentModel to set
     */
    public void setIncidentModel(CartesianChartModel incidentModel) {
        this.incidentModel = incidentModel;
    }

    /**
     * @return the selectedODR
     */
    public OfficerDailyReport getSelectedODR() {
        return selectedODR;
    }

    /**
     * @param selectedODR the selectedODR to set
     */
    public void setSelectedODR(OfficerDailyReport selectedODR) {
        this.selectedODR = selectedODR;
    }

    /**
     * @return the selectedODRText
     */
    public OfficerDailyReportText getSelectedODRText() {
        return selectedODRText;
    }

    /**
     * @param selectedODRText the selectedODRText to set
     */
    public void setSelectedODRText(OfficerDailyReportText selectedODRText) {
        this.selectedODRText = selectedODRText;
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
     * @return the editingReportText
     */
    public OfficerDailyReportText getEditingReportText() {
        return editingReportText;
    }

    /**
     * @param editingReportText the editingReportText to set
     */
    public void setEditingReportText(OfficerDailyReportText editingReportText) {
        this.editingReportText = editingReportText;
    }

    /**
     * @return the numberOfDaysToGoBack
     */
    public Integer getNumberOfDaysToGoBack() {
        return numberOfDaysToGoBack;
    }

    /**
     * @param numberOfDaysToGoBack the numberOfDaysToGoBack to set
     */
    public void setNumberOfDaysToGoBack(Integer numberOfDaysToGoBack) {
        this.numberOfDaysToGoBack = numberOfDaysToGoBack;
    }

    @Override
    public void setSelectedLogInLogOut(LogInLogOut selectedLogInLogOut) {
        this.selectedLogInLogOut = selectedLogInLogOut;
        this.selectedLogInLogOut.setTexts(null);
    }

    /**
     * @return the selectedLogInLogOut
     */
    @Override
    public LogInLogOut getSelectedLogInLogOut() {
        return selectedLogInLogOut;
    }
    
    @Override
    public void onRowSelect() {
        FacesContext context = FacesContext.getCurrentInstance();
        String selectedLogInLogOut = context.getExternalContext().getRequestParameterMap().get("selectedLogInLogOut");
        for (int l = 0; l < this.logInLogOut.size(); l++) {
            try {
                if (selectedLogInLogOut.equals(this.logInLogOut.get(l).getODRIds())) {
                    this.selectedLogInLogOut = this.logInLogOut.get(l);
                    this.selectedLogInLogOut.setTexts(null);
                    this.selectedLogInLogOut.getTexts();
                }
            } catch (Exception exe) {}
        }
    }

    @Override
    public void clearODR() {
        this.selectedODR = null;
    }

    @Override
    public void editNote(OfficerDailyReportText reportText) {
        editingReportText = reportText;
    }

    @Override
    public void saveReportText() {
        try {
            OfficerDailyReportService officerService = new OfficerDailyReportService(sessionBean.getCompanyId());
            officerService.saveDailyReportText(editingReportText);
            editingReportText = new OfficerDailyReportText(); 
        } catch (Exception exe) {
            exe.printStackTrace();
        }
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
}
