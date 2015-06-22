/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.servlet;

import com.patrolpro.beans.admin.SessionBean;
import java.io.IOException;
import java.math.BigDecimal;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import schedfoxlib.model.WayPoint;
import schedfoxlib.services.WayPointService;

/**
 *
 * @author user
 */
@WebServlet(name = "UpdateBarcodeGPSServlet", urlPatterns = {"/client/updatebarcodegpsservlet"})
public class UpdateBarcodeGPSServlet extends HttpServlet {

    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            try {
                Integer wayPointId = Integer.parseInt(request.getParameter("waypointid").toString());
                String companyId = request.getParameter("companyId");
                
                BigDecimal lat = new BigDecimal(request.getParameter("lat").toString());
                BigDecimal lng = new BigDecimal(request.getParameter("lng").toString());
                WayPointService wayPointService = new WayPointService(companyId);

                WayPoint wayPoint = wayPointService.getWayPointById(wayPointId);
                wayPoint.setLatitude(lat);
                wayPoint.setLongitude(lng);
                wayPointService.saveWayPoint(wayPoint);
            } catch (Exception exe) {
                exe.printStackTrace();
            }

            response.setContentType("application/json");
            response.addHeader("Pragma", "No-Cache");
            response.setDateHeader("Expires", 0L);
            response.setContentType("application/json");
            
        } catch (Exception exe) {
            exe.printStackTrace();
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

}
