/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.patrolpro.servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.patrolpro.beans.admin.SessionBean;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import schedfoxlib.controller.exceptions.SaveDataException;
import schedfoxlib.model.GeoFencing;
import schedfoxlib.model.GeoFencingPoints;
import schedfoxlib.services.GeoFenceService;

/**
 *
 * @author ira
 */
@WebServlet(name = "PersistGeoFenceServlet", urlPatterns = {"/client/persistgeofenceservlet"})
public class PersistGeoFenceServlet extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            SessionBean mySessionBean = (SessionBean) request.getSession().getAttribute("sessionBean");
            
            BufferedReader iStream = request.getReader();

            Type collectionType = new TypeToken<Collection<GeoFencingPoints>>(){}.getType();
            Gson myGson = new Gson();
            
            GeoFencing geoFence = new GeoFencing();
            geoFence.setGeoFenceName("New GeoFence");
            geoFence.setGeoFenceAddedBy(mySessionBean.getLoggedInUser().getId());
            geoFence.setClientId(mySessionBean.getActiveClient().getClientId());
            //geoFence.setGeoFenceAddedBy(Integer.MIN_VALUE);
            
            ArrayList<GeoFencingPoints> points = myGson.fromJson(iStream, collectionType);
            
            GeoFenceService geoFenceService = new GeoFenceService(mySessionBean.getCompanyId());
            Integer mobileFormId = geoFenceService.saveGeoFence(geoFence);
            
            for (int p = 0; p < points.size(); p++) {
                points.get(p).setGeoFencingId(mobileFormId);
            }
            geoFenceService.saveGeoFencePoints(points);
            
            System.out.println("Here");
            
        } catch (SaveDataException sde) {
            sde.printStackTrace();
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
