/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.patrolpro.servlet;

import com.google.gson.Gson;
import com.patrolpro.beans.admin.SessionBean;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import schedfoxlib.model.GeoFencing;
import schedfoxlib.services.GeoFenceService;

/**
 *
 * @author ira
 */
@WebServlet(name = "GetFencesServlet", urlPatterns = {"/client/getfencesservlet"})
public class GetFencesServlet extends HttpServlet {

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
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String companyId = request.getParameter("companyId");
            GeoFenceService geoFenceService = new GeoFenceService(companyId);
            
            Integer clientId = Integer.parseInt(request.getParameter("client_id"));
            ArrayList<GeoFencing> fences = geoFenceService.getGeoFencesForClient(clientId);
            ArrayList<Integer> fenceIds = new ArrayList<Integer>();
            for (int f = 0; f < fences.size(); f++) {
                fences.get(f).setPoints(geoFenceService.getGeoFencePointsForFence(fences.get(f).getGeoFencingId()));
            }
            Gson gson = new Gson();
            out.write(gson.toJson(fences));
        } catch (Exception exe) {
            exe.printStackTrace();
        } finally {
            out.close();
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
