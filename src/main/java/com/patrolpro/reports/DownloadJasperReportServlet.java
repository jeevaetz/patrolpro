/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.reports;

import com.patrolpro.servlet.FacesServletContextFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRCsvExporter;

/**
 *
 * @author ira
 */
public class DownloadJasperReportServlet extends GenericReportServlet {

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
        response.setContentType("application/csv");
        
        
        try {
            FacesContext context = FacesServletContextFactory.getInstance().getFacesContext(request, response);
            ReportHolderBean reportHolderBean = context.getApplication().evaluateExpressionGet(context, "#{reportHolderBean}", ReportHolderBean.class);

            GenericReport genericReport = reportHolderBean.getSelectedReport();
            response.setHeader("Content-Disposition", "attachment; filename=" + genericReport.getReportName() + ".csv");
            
            String companyId = request.getParameter("companyId");
            ArrayList<Integer> clientIds = new ArrayList<Integer>();
            ArrayList<Integer> employeeIds = new ArrayList<Integer>();
            try {
                String[] clientIdsStr = request.getParameterValues("clientId");
                for (int i = 0; i < clientIdsStr.length; i++) {
                    clientIds.add(Integer.parseInt(clientIdsStr[i]));
                }
            } catch (Exception exe) {}
            try {
                String[] employeeIdsStr = request.getParameterValues("employeeId");
                for (int i = 0; i < employeeIdsStr.length; i++) {
                    employeeIds.add(Integer.parseInt(employeeIdsStr[i]));
                }
            } catch (Exception exe) {}
            Integer branchId = 0;
            try {
                branchId = Integer.parseInt("branchId");
            } catch (Exception exe) {}
            if (branchId == 0) {
                branchId = -1;
            }

            String language = "en";
            try {
                if (request.getParameter("lang") != null) {
                    language = request.getParameter("lang");
                }
            } catch (Exception exe) {}
            
            ByteArrayOutputStream oStream = new ByteArrayOutputStream();
            JasperPrint print = genericReport.getReport(genericReport.getControlsForReport(), companyId, branchId, clientIds, employeeIds, language);

            JRCsvExporter csvExporter = new JRCsvExporter();
            csvExporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
            csvExporter.setParameter(JRExporterParameter.IGNORE_PAGE_MARGINS, true);
            csvExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, oStream);
            csvExporter.exportReport();
            
            byte[] data = oStream.toByteArray();
            response.setContentLength(data.length);
            response.getOutputStream().write(data);
            
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception exe) {
            exe.printStackTrace();
        } finally {

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
