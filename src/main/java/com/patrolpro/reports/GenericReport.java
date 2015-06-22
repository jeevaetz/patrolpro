/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.reports;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import net.sf.jasperreports.engine.JasperPrint;
import schedfoxlib.ireports.ReportLookupClass;
import schedfoxlib.model.util.ImageLoader;

/**
 *
 * @author user
 */
public abstract class GenericReport implements Serializable {

    private Integer clientId;
    private java.awt.Image headerImage;

    public abstract ArrayList<ReportObj> getControlsForReport();

    public abstract JasperPrint getReport(ArrayList<ReportObj> val, String companyId, Integer branchId, ArrayList<Integer> selClientIds, ArrayList<Integer> selEmployeeIds, String language);

    public abstract String getReportName();

    /**
     * Loads the header image, first from the company and then secondly from global image, caches it.
     * @param database
     * @return 
     */
    public java.awt.Image getHeaderImage(String database) {
        if (headerImage == null) {
            InputStream imageStream = ReportLookupClass.class.getResourceAsStream("Patrol-Pro-Client-Banner.jpg");
            try {
                BufferedImage bImage = (BufferedImage) ImageLoader.getImage("login_header.jpg", database, "general").getImage();
                if (bImage.getWidth() > 500) {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();

                    ImageIO.write(bImage, "gif", os);
                    imageStream = new ByteArrayInputStream(os.toByteArray());
                }
            } catch (Exception exe) {
            }
            try {
                headerImage = ImageIO.read(imageStream);
            } catch (Exception exe) {}
        }
        return headerImage;
    }

    public static ArrayList<GenericReport> getReport() {
        ArrayList<GenericReport> reports = new ArrayList<GenericReport>();
        reports.add(new BarcodeReport());
        reports.add(new IncidentsReport());
        reports.add(new ODRReport());
        reports.add(new OfficerReport());
        reports.add(new GeoFenceReport());
        return reports;
    }

    public boolean allowCSVDownload() {
        return false;
    }

    /**
     * @return the clientId
     */
    public Integer getClientId() {
        return clientId;
    }

    /**
     * @param clientId the clientId to set
     */
    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public class ReportObj implements Serializable {

        private String reportLabel;
        private String dataType;
        private Object reportData;
        private String reportParameter;

        public ReportObj(String reportLabel, String dataType, Object reportData, String reportParameter) {
            this.reportLabel = reportLabel;
            this.dataType = dataType;
            this.reportData = reportData;
            this.reportParameter = reportParameter;
        }

        /**
         * @return the reportLabel
         */
        public String getReportLabel() {
            return reportLabel;
        }

        /**
         * @param reportLabel the reportLabel to set
         */
        public void setReportLabel(String reportLabel) {
            this.reportLabel = reportLabel;
        }

        /**
         * @return the reportName
         */
        public String getDataType() {
            return dataType;
        }

        /**
         * @param reportName the reportName to set
         */
        public void setDataType(String reportName) {
            this.dataType = reportName;
        }
        
        public String getFormattedDate() {
            try {
                Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
                ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", locale);
                return new SimpleDateFormat(rb.getString("shortDateStyle")).format(reportData);
            } catch (Exception exe) {}
            return "";
        }
        
        public void setFormattedDate(String dateInst) {
            try {
                Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
                ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", locale);
                reportData = new SimpleDateFormat(rb.getString("shortDateStyle")).parse(dateInst);
            } catch (Exception exe) {}
        }
        
        /**
         * @return the reportData
         */
        public Object getReportData() {
            return reportData;
        }

        /**
         * @param reportData the reportData to set
         */
        public void setReportData(Object reportData) {
            this.reportData = reportData;
        }

        /**
         * @return the reportParameter
         */
        public String getReportParameter() {
            return reportParameter;
        }

        /**
         * @param reportParameter the reportParameter to set
         */
        public void setReportParameter(String reportParameter) {
            this.reportParameter = reportParameter;
        }
    }
}
