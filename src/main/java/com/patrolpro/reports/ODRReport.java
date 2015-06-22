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
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import schedfoxlib.ireports.ReportLookupClass;
import schedfoxlib.model.Company;
import schedfoxlib.model.OfficerDailyReport;
import schedfoxlib.model.OfficerDailyReportDocument;
import schedfoxlib.model.OfficerDailyReportText;
import schedfoxlib.model.WrapperEmployeeClientSelections;
import schedfoxlib.services.CompanyService;
import schedfoxlib.services.OfficerDailyReportService;

/**
 *
 * @author ira
 */
public class ODRReport extends GenericReport implements Serializable {

    private ArrayList<ReportObj> controls;

    public ODRReport() {

    }

    @Override
    public ArrayList<ReportObj> getControlsForReport() {
        if (controls == null) {
            Calendar sdate = Calendar.getInstance();
            Calendar edate = Calendar.getInstance();
            sdate.add(Calendar.DAY_OF_YEAR, -3);

            Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", locale);
            
            controls = new ArrayList<ReportObj>();
            controls.add(new ReportObj(rb.getString("employee"), "working_emp_id", "", "emp_id"));
            controls.add(new ReportObj(rb.getString("startDate"), "date", sdate.getTime(), "startDate"));
            controls.add(new ReportObj(rb.getString("endDate"), "date", edate.getTime(), "endDate"));
        }
        return controls;
    }

    @Override
    public JasperPrint getReport(ArrayList<ReportObj> val, String companyId, Integer branchId, ArrayList<Integer> selectedClientIds, ArrayList<Integer> selectedEmployeeIds, String language) {
        JasperPrint retVal = null;

        HashMap hm = new HashMap();
        for (int c = 0; c < controls.size(); c++) {
            hm.put(controls.get(c).getReportParameter(), controls.get(c).getReportData());
        }
        if (hm.get("emp_id").toString().length() == 0) {
            hm.put("emp_id", "-1");
        }

        Company myComp = null;
        try {
            CompanyService companyService = new CompanyService();
            myComp = companyService.getCompanyById(Integer.parseInt(companyId));
        } catch (Exception exe) {
        }

        try {
            OfficerDailyReportService odrService = new OfficerDailyReportService(companyId);

            WrapperEmployeeClientSelections empClientSel = new WrapperEmployeeClientSelections();
            selectedEmployeeIds.remove(new Integer(-1));
            selectedClientIds.remove(new Integer(-1));
            empClientSel.setSelectedClientIds(selectedClientIds);
            empClientSel.setSelectedEmployeeIds(selectedEmployeeIds);

            ArrayList<OfficerDailyReport> data = odrService.getDailyReportsForEmployeeAndClientsWithText(empClientSel, branchId,
                    (Date) hm.get("startDate"), (Date) hm.get("endDate"));
            for (int d = 0; d < data.size(); d++) {
                try {
                    ArrayList<OfficerDailyReportDocument> bytesData = new ArrayList<OfficerDailyReportDocument>();

                    OfficerDailyReport currData = data.get(d);
                    HashMap param = new HashMap();
                    try {
                        if (currData.getEmployee_id().equals(999999)) {
                            throw new Exception();
                        }
                        param.put("officerName", currData.getOfficer().getFullName());
                    } catch (Exception e) {
                        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
                        ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", locale);
        
                        param.put("officerName", rb.getString("supervisor"));
                    }
                    param.put("clientName", currData.getClient().getClientName());

                    param.put("officeDailyReportDate", (Date) hm.get("startDate"));
                    param.put("loginDate", (Date) currData.getLoggedIn());
                    param.put("logoutDate", (Date) currData.getLoggedOut());
                    param.put("odrTexts", currData.getReportTexts());
                    param.put("active_db", myComp.getCompDB());
                    param.put("incidentCount", currData.getIncidentCount());
                    param.put("wayPointScanCount", currData.getWaypointScan());

                    InputStream reportStream = ReportLookupClass.class.getResourceAsStream("OfficerDailyReport.jasper");

                    try {
                        for (int t = 0; t < currData.getReportTexts().size(); t++) {
                            OfficerDailyReportText currText = currData.getReportTexts().get(t);
                            if (currText.getDocCount() > 0) {
                                ArrayList<OfficerDailyReportDocument> tempDocs = odrService.getDocumentsForReport(currText.getOfficerDailyReportTextId(), true);
                                for (int doc = 0; doc < tempDocs.size(); doc++) {
                                    bytesData.add(tempDocs.get(doc));
                                }
                            }
                        }
                    } catch (Exception exe) {
                    }

                    URL urlImages = new URL(currData.getOfficer().getEmployeeImageUrl(myComp.getDB()));
                    urlImages.openConnection();
                    InputStream iStream = urlImages.openStream();

                    try {
                        ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", new Locale(language));
                        param.put(JRParameter.REPORT_RESOURCE_BUNDLE, rb);
                    } catch (Exception exe) {
                    }

                    param.put("SUBREPORT_DIR", ReportLookupClass.class.getResource("OfficerDailyReport.jasper").toURI().toString().replaceAll("OfficerDailyReport.jasper", ""));
                    param.put("EmployeeImage", iStream);
                    try {
                        param.put("logo", getHeaderImage(myComp.getCompDB()));
                    } catch (Exception exe) {
                    }

                    JasperPrint print = JasperFillManager.fillReport(reportStream, param, new JRBeanCollectionDataSource(bytesData));
                    if (retVal == null) {
                        retVal = print;
                    } else {
                        for (int p = 0; p < print.getPages().size(); p++) {
                            retVal.addPage(print.getPages().get(p));
                        }
                    }
                } catch (Exception exe) {
                    exe.printStackTrace();
                }
            }
        } catch (Exception exe) {
        }
        return retVal;
    }

    @Override
    public String getReportName() {
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", locale);
        return rb.getString("odrReport");
    }

}
