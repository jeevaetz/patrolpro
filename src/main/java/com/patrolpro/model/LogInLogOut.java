/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import schedfoxlib.model.Client;
import schedfoxlib.model.Employee;
import schedfoxlib.model.OfficerDailyReport;
import schedfoxlib.model.OfficerDailyReportText;
import schedfoxlib.services.ClientService;

/**
 *
 * @author ira
 */
public class LogInLogOut implements Comparable, Serializable {

    private Date startDate;
    private Date endDate;
    private ArrayList<OfficerDailyReport> reports;
    private String scheduleId;
    private Employee employee;
    private Client client;
    private String companyId;
    private String deviceName;

    private ArrayList<OfficerDailyReportText> texts;

    public LogInLogOut(String companyId) {
        this.companyId = companyId;
        reports = new ArrayList<OfficerDailyReport>();
    }

    public String getODRIds() {
        StringBuilder retVal = new StringBuilder();
        for (int r = 0; r < reports.size(); r++) {
            if (r != 0) {
                retVal.append(",");
            }
            retVal.append(reports.get(r).getOfficerDailyReportId().toString());
        }
        return retVal.toString();
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the reports
     */
    public ArrayList<OfficerDailyReport> getReports() {
        return reports;
    }

    /**
     * @param reports the reports to set
     */
    public void setReports(ArrayList<OfficerDailyReport> reports) {
        this.reports = reports;
    }

    /**
     * @return the scheduleId
     */
    public String getScheduleId() {
        return scheduleId;
    }

    /**
     * @param scheduleId the scheduleId to set
     */
    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    /**
     * @return the employee
     */
    public Employee getEmployee() {
        return employee;
    }

    /**
     * @param employee the employee to set
     */
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    /**
     * @return the texts
     */
    public ArrayList<OfficerDailyReportText> getTexts() {
        if (texts == null) {
            texts = new ArrayList<OfficerDailyReportText>();
            for (int o = 0; o < this.reports.size(); o++) {
                this.reports.get(0).setReportTexts(null);
                texts.addAll(this.reports.get(o).getReportTexts(this.companyId));
            }
        }
        return texts;
    }

    /**
     * @param texts the texts to set
     */
    public void setTexts(ArrayList<OfficerDailyReportText> texts) {
        this.texts = texts;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof LogInLogOut) {
            try {
                LogInLogOut logO = (LogInLogOut) o;
                int retVal = this.getStartDate().compareTo(logO.getStartDate()) * -1;
                if (retVal == 0) {
                    retVal = this.reports.get(0).getOfficerDailyReportId().compareTo(logO.getReports().get(0).getOfficerDailyReportId());
                }
                return retVal;
            } catch (Exception exe) {
                return 1;
            }
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        try {
            if (obj instanceof LogInLogOut) {
                return this.getODRIds().equals(((LogInLogOut) obj).getODRIds());
            }
        } catch (Exception exe) {
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.getODRIds() != null ? this.getODRIds().hashCode() : 0);
        return hash;
    }

    public static ArrayList<LogInLogOut> generateData(ArrayList<OfficerDailyReport> dailyReports, String companyId) {
        HashMap<String, LogInLogOut> cacheOfData = new HashMap<String, LogInLogOut>();
        ArrayList<LogInLogOut> logInLogOut = new ArrayList<LogInLogOut>();

        HashMap<Integer, Client> cacheOfClients = new HashMap<Integer, Client>();

        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", locale);
            
        Employee supervisorEmployee = new Employee(new Date());
        supervisorEmployee.setEmployeeId(999999);
        supervisorEmployee.setEmployeeFirstName(rb.getString("supervisor"));
        supervisorEmployee.setEmployeeLastName("");
        
        for (int d = 0; d < dailyReports.size(); d++) {
            OfficerDailyReport currOdr = dailyReports.get(d);
            LogInLogOut currLogInOut = null;
            if (currOdr.getShiftId() > 0) {
                if (cacheOfData.get(currOdr.getShiftId() + "") == null) {
                    cacheOfData.put(currOdr.getShiftId() + "", new LogInLogOut(companyId));
                }
                currLogInOut = cacheOfData.get(currOdr.getShiftId() + "");
            } else {
                if (cacheOfData.get((-1 * currOdr.getOfficerDailyReportId()) + "") == null) {
                    cacheOfData.put((-1 * currOdr.getOfficerDailyReportId()) + "" + "", new LogInLogOut(companyId));
                }
                currLogInOut = cacheOfData.get((-1 * currOdr.getOfficerDailyReportId()) + "");
            }
            if (currLogInOut.getEmployee() == null) {
                currLogInOut.setEmployee(currOdr.getOfficer());
            }
            try {
                currLogInOut.setDeviceName(currOdr.getClientEquipment().getNickname());
                if (currLogInOut.getDeviceName() == null || currLogInOut.getDeviceName().length() == 0) {
                    currLogInOut.setDeviceName(currOdr.getClientEquipment().getPhoneNumber());
                }
            } catch (Exception exe) {}
            if (currLogInOut.getEmployee() == null || currLogInOut.getEmployee().getEmployeeId() == null || currLogInOut.getEmployee().getBranchId() == 0) {
                currLogInOut.setEmployee(supervisorEmployee);
            }
            if (currLogInOut.getClient() == null) {
                try {
                    if (cacheOfClients.get(currOdr.getClientId()) == null) {
                        ClientService clientService = new ClientService(companyId);
                        Client currClient = clientService.getClientById(currOdr.getClientId());
                        cacheOfClients.put(currOdr.getClientId(), currClient);
                    }
                    currLogInOut.setClient(cacheOfClients.get(currOdr.getClientId()));
                } catch (Exception exe) {
                }
            }
            try {
                if (currLogInOut.getStartDate() == null || currLogInOut.getStartDate().compareTo(currOdr.getLoggedIn()) > 0) {
                    currLogInOut.setStartDate(currOdr.getLoggedIn());
                }
            } catch (Exception exe) {
            }
            try {
                if (currLogInOut.getEndDate() == null || currLogInOut.getEndDate().compareTo(currOdr.getLoggedOut()) < 0) {
                    currLogInOut.setEndDate(currOdr.getLoggedOut());
                }
            } catch (Exception exe) {
            }
            currLogInOut.setScheduleId(currOdr.getShiftId() + "");
            currLogInOut.getReports().add(currOdr);
        }

        Iterator<String> keyIterator = cacheOfData.keySet().iterator();
        while (keyIterator.hasNext()) {
            logInLogOut.add(cacheOfData.get(keyIterator.next()));
        }
        Collections.sort(logInLogOut);
        return logInLogOut;
    }

    /**
     * @return the client
     */
    public Client getClient() {
        return client;
    }

    /**
     * @param client the client to set
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * @return the deviceName
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * @param deviceName the deviceName to set
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
