/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import com.patrolpro.beans.interfaces.RefreshClientListingInterface;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;
import schedfoxlib.model.Client;
import schedfoxlib.model.Employee;
import schedfoxlib.model.IncidentReport;
import schedfoxlib.model.IncidentReportDocument;
import schedfoxlib.model.IncidentReportType;
import schedfoxlib.services.EmployeeService;
import schedfoxlib.services.IncidentService;

/**
 *
 * @author user
 */
@Named("incidentBean")
@ViewScoped
public class IncidentsBean implements Serializable, RefreshClientListingInterface {

    private Date selectedDate;
    private ArrayList<IncidentReport> incidentReports;

    @Inject
    private ClientSessionBean sessionBean;

    private IncidentReport selectedIncident;
    private ArrayList<IncidentReportDocument> selectedIncidentDocuments;

    private SelectItem[] incidentTypes;

    private HashMap<Integer, Employee> employeeHash;
    
    public IncidentsBean() {
        employeeHash = new HashMap<Integer, Employee>();
        incidentReports = new ArrayList<IncidentReport>();
    }

    @PostConstruct
    public void postConstruct() {
        this.refresh();
    }

    public void clearIncident() {
        this.selectedIncident = null;
    }

    public Employee getEmployeeById(Integer empId) {
        if (empId.equals(999999)) {
            Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", locale);
            
            Employee supervisorEmployee = new Employee(new Date());
            supervisorEmployee.setEmployeeId(999999);
            supervisorEmployee.setEmployeeFirstName(rb.getString("supervisor"));
            supervisorEmployee.setEmployeeLastName("");
            return supervisorEmployee;
        } else if (!employeeHash.containsKey(empId)) {
            try {
                EmployeeService empService = new EmployeeService(sessionBean.getCompanyId());
                employeeHash.put(empId, empService.getEmployeeById(empId));
            } catch (Exception exe) {}
        }
        return employeeHash.get(empId);
    }
    
    public String getClientName(Integer clientId) {
        try {
            ArrayList<Client> clients = sessionBean.getClients();
            for (int c = 0; c < clients.size(); c++) {
                if (clients.get(c).getClientId().equals(clientId)) {
                    return clients.get(c).getClientName();
                }
            }
        } catch (Exception exe) {
        }
        return "";
    }

    /* Simple refresh for all reports */
    public void refresh() {
        IncidentService incidentService = new IncidentService(sessionBean.getCompanyId());
        getIncidentReports().clear();

        try {
            ArrayList<Integer> clientIds = new ArrayList<Integer>();
            for (int c = 0; c < sessionBean.getClients().size(); c++) {
                clientIds.add(sessionBean.getClients().get(c).getClientId());
            }
            getIncidentReports().addAll(incidentService.getIncidentReportsForClient(clientIds, 90));
            Collections.sort(this.incidentReports);

            ArrayList<IncidentReportType> types = incidentService.getIncidentReportTypes();
            incidentTypes = new SelectItem[types.size() + 1];
            incidentTypes[0] = new SelectItem("", "Select");
            for (int t = 0; t < types.size(); t++) {
                incidentTypes[t + 1] = new SelectItem(types.get(t), types.get(t).getReportType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshBackData() {
        IncidentService incidentService = new IncidentService(sessionBean.getCompanyId());

        try {
            getIncidentReports().clear();

            Calendar dateInstance = Calendar.getInstance();
            dateInstance.setTime(getSelectedDate());
            dateInstance.add(Calendar.HOUR_OF_DAY, 6);

            for (int c = 0; c < sessionBean.getClients().size(); c++) {
                Client currentClient = sessionBean.getClients().get(c);
                getIncidentReports().addAll(incidentService.getIncidentReportsForDateAndClient(dateInstance.getTime(), currentClient.getClientId()));
            }

            for (int r = 0; r < incidentReports.size(); r++) {
                if (this.incidentReports.get(r).getImageCount() > 0) {
                    ArrayList<IncidentReportDocument> documents = incidentService.getIncidentReportDocumentsForIncident(incidentReports.get(r).getIncidentReportId(), false);
                    incidentReports.get(r).setIncidentReportDocuments(documents);
                }
            }
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    public void handleDateSelect(SelectEvent event) {
        this.selectedDate = (Date) event.getObject();
        refreshBackData();
    }

    public void onRowSelect(SelectEvent event) {

    }

    public void onRowSelectIncident() {
        String selectedIncidentId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedIncidentId");
        Integer selectedIncidentIdInt = Integer.parseInt(selectedIncidentId);
        
        try {
            IncidentService incidentService = new IncidentService(sessionBean.getCompanyId());
            this.selectedIncident = incidentService.getIncidentReportById(selectedIncidentIdInt);
            this.selectedIncidentDocuments = incidentService.getIncidentReportDocumentsForIncident(selectedIncident.getIncidentReportId(), true);
            this.selectedIncident.setOfficer(this.getEmployeeById(selectedIncident.getEmployeeId()));
            this.selectedIncident.getIncidentType(sessionBean.getCompanyId());
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    /**
     * @return the incidentReports
     */
    public ArrayList<IncidentReport> getIncidentReports() {
        return incidentReports;
    }

    /**
     * @param incidentReports the incidentReports to set
     */
    public void setIncidentReports(ArrayList<IncidentReport> incidentReports) {
        this.incidentReports = incidentReports;
    }

    /**
     * @return the sessionBean
     */
    public ClientSessionBean getSessionBean() {
        return sessionBean;
    }

    /**
     * @param sessionBean the sessionBean to set
     */
    public void setSessionBean(ClientSessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    /**
     * @return the selectedIncident
     */
    public IncidentReport getSelectedIncident() {
        return selectedIncident;
    }

    /**
     * @param selectedIncident the selectedIncident to set
     */
    public void setSelectedIncident(IncidentReport selectedIncident) {
        IncidentService incidentService = new IncidentService(sessionBean.getCompanyId());

        this.selectedIncident = selectedIncident;

        try {
            this.selectedIncidentDocuments = incidentService.getIncidentReportDocumentsForIncident(selectedIncident.getIncidentReportId(), true);
        } catch (Exception exe) {
            exe.printStackTrace();
        }
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

    /**
     * @return the incidentTypes
     */
    public SelectItem[] getIncidentTypes() {
        if (incidentTypes == null) {
            refresh();
        }
        return incidentTypes;
    }

    /**
     * @param incidentTypes the incidentTypes to set
     */
    public void setIncidentTypes(SelectItem[] incidentTypes) {
        this.incidentTypes = incidentTypes;
    }

    /**
     * @return the selectedIncidentDocuments
     */
    public ArrayList<IncidentReportDocument> getSelectedIncidentDocuments() {
        return selectedIncidentDocuments;
    }

    /**
     * @param selectedIncidentDocuments the selectedIncidentDocuments to set
     */
    public void setSelectedIncidentDocuments(ArrayList<IncidentReportDocument> selectedIncidentDocuments) {
        this.selectedIncidentDocuments = selectedIncidentDocuments;
    }

}
