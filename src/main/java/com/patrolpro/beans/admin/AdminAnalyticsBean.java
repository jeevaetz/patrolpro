/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans.admin;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import schedfoxlib.model.CompanyObj;
import schedfoxlib.model.DeviceCountSummary;
import schedfoxlib.services.CompanyService;
import schedfoxlib.services.EquipmentService;

/**
 *
 * @author ira
 */
@Named("adminAnalyticsBean")
@javax.faces.view.ViewScoped
public class AdminAnalyticsBean implements Serializable {
    
    @Inject
    private SessionBean sessionBean;

    private ArrayList<DeviceCountSummary> summary;
    
    public AdminAnalyticsBean() {
        
    }
    
    @PostConstruct
    public void postConstruct() {
        try {
            CompanyService compService = new CompanyService();
            EquipmentService equipService = new EquipmentService("");
            ArrayList<CompanyObj> companies = compService.getCompaniesForPatrolPro();
            ArrayList<Integer> companyIds = new ArrayList<Integer>();
            for (int c = 0; c < companies.size(); c++) {
                companyIds.add(companies.get(c).getCompanyId());
            }
            SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            summary = equipService.getSummaries(companyIds, myFormat.format(new Date()));
            
            for (int s = 0; s < summary.size(); s++) {
                try {
                    URL versionURL = new URL("http://www.patrolpro.com/d/" + summary.get(s).getCompany().getCompanyUrl() + "/version");
                    URLConnection urlConn = versionURL.openConnection();
                    InputStream myInput = urlConn.getInputStream();
                    byte[] buffer = new byte[2048];
                    int numRead = -1;
                    ByteArrayOutputStream oStream = new ByteArrayOutputStream();
                    while ((numRead = myInput.read(buffer)) > -1) {
                        oStream.write(buffer, 0, numRead);
                    }
                    summary.get(s).setCurrentVersion(new String(oStream.toByteArray()));
                    myInput.close();
                } catch (Exception exe) {}
                
            }
            
        } catch (Exception exe) {}
    }
    
    public Integer getTotalDeviceCount() {
        Integer totalCount = 0;
        try {
            for (int s = 0; s < summary.size(); s++) {
                totalCount += summary.get(s).getDeviceCount();
            }
        } catch (Exception exe) {
            return 0;
        }
        return totalCount;
    }
    
    public String dummyString() {
        return "";
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
     * @return the summary
     */
    public ArrayList<DeviceCountSummary> getSummary() {
        return summary;
    }

    /**
     * @param summary the summary to set
     */
    public void setSummary(ArrayList<DeviceCountSummary> summary) {
        this.summary = summary;
    }
    
}
