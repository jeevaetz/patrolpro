/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.reports;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import schedfoxlib.ireports.ReportLookupClass;
import schedfoxlib.model.MobileFormDataSearch;
import schedfoxlib.model.MobileFormFillout;
import schedfoxlib.model.MobileForms;
import schedfoxlib.model.MobileFormsPrintable;
import schedfoxlib.services.MobileFormService;

/**
 *
 * @author ira
 */
@WebServlet(name = "LoadJasperReportServlet", urlPatterns = {"/loadJasperReportServlet/*"})
public class LoadJasperReportServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/pdf");
        response.addHeader("Content-Disposition", "inline;filename=Report.pdf");

        String companyId = request.getParameter("companyId");
        String clientId = request.getParameter("clientId");
        String sDate = request.getParameter("startDate");
        String eDate = request.getParameter("endDate");
        
        String lang = request.getParameter("lang");
        if (lang == null || lang.trim().length() == 0) {
            lang = "en";
        }
        
        Enumeration<String> params = request.getParameterNames();
        ArrayList<MobileFormDataSearch> dataToSearchBy = new ArrayList<MobileFormDataSearch>();
        while (params.hasMoreElements()) {
            try {
                String currentParam = params.nextElement();
                if (currentParam.startsWith("selection_")) {
                    Integer mobileFormDataId = Integer.parseInt(currentParam.replaceAll("selection_", ""));
                    MobileFormDataSearch searchData = new MobileFormDataSearch();
                    searchData.setMobileFormDataId(mobileFormDataId);
                    searchData.setDataToSearch(request.getParameter(currentParam));
                    searchData.setMobileFormDataTypeId(6);
                    if (!searchData.getDataToSearch().trim().isEmpty()) {
                        dataToSearchBy.add(searchData);
                    }
                } else if (currentParam.startsWith("retrieve_text_")) {
                    Integer mobileFormDataId = Integer.parseInt(currentParam.replaceAll("retrieve_text_", ""));
                    MobileFormDataSearch searchData = new MobileFormDataSearch();
                    searchData.setMobileFormDataId(mobileFormDataId);
                    searchData.setDataToSearch(request.getParameter(currentParam));
                    searchData.setMobileFormDataTypeId(1);
                    if (!searchData.getDataToSearch().trim().isEmpty()) {
                        dataToSearchBy.add(searchData);
                    }
                } else if (currentParam.startsWith("freetext_")) {
                    Integer mobileFormDataId = Integer.parseInt(currentParam.replaceAll("freetext_", ""));
                    MobileFormDataSearch searchData = new MobileFormDataSearch();
                    searchData.setMobileFormDataId(mobileFormDataId);
                    searchData.setDataToSearch(request.getParameter(currentParam));
                    searchData.setMobileFormDataTypeId(1);
                    if (!searchData.getDataToSearch().trim().isEmpty()) {
                        dataToSearchBy.add(searchData);
                    }
                } else if (currentParam.startsWith("date_")) {
                    Integer mobileFormDataId = Integer.parseInt(currentParam.replaceAll("date_", ""));
                    MobileFormDataSearch searchData = new MobileFormDataSearch();
                    searchData.setMobileFormDataId(mobileFormDataId);
                    searchData.setDataToSearch(request.getParameter(currentParam));
                    searchData.setMobileFormDataTypeId(5);
                    if (!searchData.getDataToSearch().trim().isEmpty()) {
                        dataToSearchBy.add(searchData);
                    }
                } else if (currentParam.startsWith("yes_no_")) {
                    Integer mobileFormDataId = Integer.parseInt(currentParam.replaceAll("yes_no_", ""));
                    MobileFormDataSearch searchData = new MobileFormDataSearch();
                    searchData.setMobileFormDataId(mobileFormDataId);
                    searchData.setDataToSearch(request.getParameter(currentParam));
                    searchData.setMobileFormDataTypeId(3);
                    if (!searchData.getDataToSearch().trim().isEmpty()) {
                        dataToSearchBy.add(searchData);
                    }
                }
            } catch (Exception exe) {}
        }
        
        MobileFormService mobileService = new MobileFormService(companyId);
        try {
            ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", new Locale(lang));
            
            SimpleDateFormat myFormat = new SimpleDateFormat(rb.getString("shortDateStyle"));
            Date startDate = myFormat.parse(sDate);
            Date endDate = myFormat.parse(eDate);
            
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            MobileForms myForm = mobileService.getForm(Integer.parseInt(request.getParameter("mobileFormId")));
            try {
                myForm.setClientId(Integer.parseInt(clientId));
            } catch (Exception exe) {}
            Class.forName("org.postgresql.Driver");

            String url = "jdbc:postgresql://schedfoxdb.schedfox.com/unique_manage?user=db_user&password=sh0rtNsw33t";
            Connection conn = DriverManager.getConnection(url);

            ArrayList<MobileFormFillout> fillouts = mobileService.getFormFilloutsForClient(myForm.getClientId(), 
                    myForm.getMobileFormsId(), dbFormat.format(startDate), dbFormat.format(endDate),
                    dataToSearchBy);
            
            JasperPrint print = MobileFormsPrintable.generateDailyPrint(ReportLookupClass.class, fillouts, myForm, companyId, startDate, endDate, lang, conn);
            try {
                if (print != null) {
                    ByteArrayOutputStream oStream = new ByteArrayOutputStream();

                    JRExporter exporter = new JRPdfExporter();
                    exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, oStream);
                    exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
                    exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, "UTF-8");
                    exporter.exportReport();

                    byte[] myBytes = oStream.toByteArray();
                    response.setContentLength(myBytes.length);
                    response.getOutputStream().write(myBytes);
                }
                response.getOutputStream().flush();
                response.getOutputStream().close();
            } catch (Exception exe) {
                exe.printStackTrace();
            } finally {
                try {
                    conn.close();
                } catch (Exception exe) {
                }
            }
        } catch (Exception exe) {}
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>


}
