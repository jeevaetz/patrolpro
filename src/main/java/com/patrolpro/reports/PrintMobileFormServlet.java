/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.reports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import schedfoxlib.ireports.ReportLookupClass;
import schedfoxlib.model.CompanyObj;
import schedfoxlib.model.MobileFormFillout;
import schedfoxlib.model.MobileForms;
import schedfoxlib.model.MobileFormsPrintable;
import schedfoxlib.services.CompanyService;
import schedfoxlib.services.MobileFormService;

/**
 *
 * @author ira
 */
public class PrintMobileFormServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String companyId = request.getParameter("companyId");
            String mobileFormFilloutId = request.getParameter("mobileFormFilloutId");
            MobileFormService mobileService = new MobileFormService(companyId);
            MobileFormFillout fillout = mobileService.getFormFillout(Integer.parseInt(mobileFormFilloutId));
            MobileForms mobileForm = mobileService.getForm(fillout.getMobileFormId());

            Class.forName("org.postgresql.Driver");

            String url = "jdbc:postgresql://schedfoxdb.schedfox.com/unique_manage?user=db_user&password=sh0rtNsw33t";
            Connection conn = DriverManager.getConnection(url);

            CompanyService companyService = new CompanyService();
            CompanyObj companyObj = companyService.getCompanyObjById(Integer.parseInt(companyId));

            HashMap<String, Object> params = new HashMap<String, Object>();

            ByteArrayInputStream iStream = new ByteArrayInputStream(mobileForm.getReportData());
            JasperReport jasperReport = null;
                try {
                    iStream = new ByteArrayInputStream(mobileForm.getReportData());
                    jasperReport = (JasperReport) JRLoader.loadObject(iStream);
                } catch (Exception e) {
                    iStream = new ByteArrayInputStream(mobileForm.getReportData());
                    jasperReport = JasperCompileManager.compileReport(iStream);
                }

            try {
                params.put("logo", new ByteArrayInputStream(MobileFormsPrintable.getHeaderByteArray(ReportLookupClass.class, companyObj)));
            } catch (Exception exe) {}
            
            JasperPrint print = MobileFormsPrintable.addJasperPrintToTotalReport(ReportLookupClass.class, null, fillout, params, conn, null, new Date(), companyId, jasperReport, companyObj);
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
            System.out.println("Here");
        } catch (Exception exe) {
            exe.printStackTrace();
        }
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
