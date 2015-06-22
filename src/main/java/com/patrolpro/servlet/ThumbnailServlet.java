/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.servlet;

import com.patrolpro.beans.admin.SessionBean;
import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import schedfoxlib.model.ImageInterface;
import schedfoxlib.services.IncidentService;
import schedfoxlib.services.OfficerDailyReportService;

/**
 *
 * @author user
 */
@WebServlet(name = "ThumbnailServlet", urlPatterns = {"/ThumbnailServlet"})
public class ThumbnailServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("image/gif;charset=UTF-8");
        try {
            String companyId = request.getParameter("companyId");
            Integer imageId = Integer.parseInt(request.getParameter("imageId"));
            
            ImageInterface image = null;
            if (imageId < 0) {
                OfficerDailyReportService officeService = new OfficerDailyReportService(companyId);
                image = officeService.getDocumentForReport(imageId.intValue() * -1, false);
            } else {
                IncidentService incidentService = new IncidentService(companyId);
                image = incidentService.getIncidentReportDocumentById(imageId);
            }

            //response.setContentLength(image.getThumbnailData().length);
            response.setContentLength(image.getThumbnailData().length);
            response.setHeader("Content-Disposition", "inline; filename=\"" + "ImageThumbnail.png" + "\"");
            //response.setContentType("image/png");

            BufferedInputStream input = null;
            BufferedOutputStream output = null;
            try {
                input = new BufferedInputStream(new ByteArrayInputStream(image.getThumbnailData()));
                //input = new BufferedInputStream(new ByteArrayInputStream(image.getThumbnailData()));
                output = new BufferedOutputStream(response.getOutputStream());
                byte[] buffer = new byte[8192];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
                output.flush();
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException logOrIgnore) {
                    }
                }
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException logOrIgnore) {
                    }
                }
            }

        } catch (Exception exe) {
            exe.printStackTrace();
        } finally {
            
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
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
     * Handles the HTTP
     * <code>POST</code> method.
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
