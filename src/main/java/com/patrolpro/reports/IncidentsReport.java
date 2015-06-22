/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.reports;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import schedfoxlib.ireports.ReportLookupClass;
import schedfoxlib.model.Company;
import schedfoxlib.services.CompanyService;

/**
 *
 * @author ira
 */
public class IncidentsReport extends GenericReport implements Serializable {

    private ArrayList<ReportObj> controls;

    public IncidentsReport() {

    }

    @Override
    public ArrayList<ReportObj> getControlsForReport() {
        if (controls == null) {
            Calendar sdate = Calendar.getInstance();
            Calendar edate = Calendar.getInstance();
            sdate.add(Calendar.WEEK_OF_YEAR, -2);

            Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", locale);

            controls = new ArrayList<ReportObj>();
            controls.add(new ReportObj(rb.getString("incidentTypes"), "incident_types", "", "incident_type_id"));
            controls.add(new ReportObj(rb.getString("startDate"), "date", sdate.getTime(), "startDate"));
            controls.add(new ReportObj(rb.getString("endDate"), "date", edate.getTime(), "endDate"));
        }
        return controls;
    }

    @Override
    public JasperPrint getReport(ArrayList<ReportObj> val, String companyId, Integer branchId, ArrayList<Integer> selectedClientIds, ArrayList<Integer> selectedEmployeeIds, String language) {
        InputStream reportStream = ReportLookupClass.class.getResourceAsStream("incident_report.jasper");
        InputStream imageStream = ReportLookupClass.class.getResourceAsStream("Patrol-Pro-Client-Banner.jpg");

        HashMap hm = new HashMap();
        for (int c = 0; c < controls.size(); c++) {
            if (controls.get(c).getReportData() instanceof Date) {
                hm.put(controls.get(c).getReportParameter(), new SimpleDateFormat("yyyy-MM-dd").format(controls.get(c).getReportData()));
            } else {
                hm.put(controls.get(c).getReportParameter(), controls.get(c).getReportData());
            }
        }
        hm.put("logo", imageStream);
        hm.put("selectedClients", selectedClientIds);

        try {
            CompanyService companyService = new CompanyService();
            Company myComp = companyService.getCompanyById(Integer.parseInt(companyId));
            hm.put("schema", myComp.getDB());
        } catch (Exception exe) {
        }

        try {
            ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", new Locale(language));
            hm.put(JRParameter.REPORT_RESOURCE_BUNDLE, rb);
        } catch (Exception exe) {
        }

        try {
            Class.forName("org.postgresql.Driver");

            String url = "jdbc:postgresql://schedfoxdb.schedfox.com/unique_manage?user=db_user&password=sh0rtNsw33t";
            Connection conn = DriverManager.getConnection(url);

            return JasperFillManager.fillReport(reportStream, hm, conn);
        } catch (Exception exe) {
            exe.printStackTrace();
            return null;
        }
    }

    @Override
    public String getReportName() {
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", locale);
        return rb.getString("incidentReports");
    }

}
