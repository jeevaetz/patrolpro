/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.servlet;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import schedfoxlib.model.Client;
import schedfoxlib.model.WayPoint;
import schedfoxlib.services.ClientService;
import schedfoxlib.services.WayPointService;

/**
 *
 * @author user
 */
@WebServlet(name = "BarcodeGPSCoordinatesServlet", urlPatterns = {"/client/barcodegpscoordinates"})
public class BarcodeGPSCoordinatesServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ArrayList<WayPoint> nonGPSWayPoints = new ArrayList<WayPoint>();
        try {
            String companyId = request.getParameter("companyId");
            String branchId = request.getParameter("branchId");
            
            ClientService clientService = new ClientService(companyId);
            try {
                Integer clientId = Integer.parseInt(request.getParameter("clientId").toString());
                Client currentClient = clientService.getClientById(clientId);
                WayPointService wayPointService = new WayPointService(companyId);

                ArrayList<WayPoint> wayPoints = wayPointService.getWayPointsForClientId(currentClient.getClientId());
                for (int w = 0; w < wayPoints.size(); w++) {
                    if (wayPoints.get(w).getLatitude() != null && !wayPoints.get(w).getLatitude().equals(new BigDecimal(0))) {
                        nonGPSWayPoints.add(wayPoints.get(w));
                    }
                }
            } catch (Exception exe) {
                exe.printStackTrace();
            }

            response.addHeader("Cache-Control", "no-cache");
            response.addHeader("Pragma", "No-Cache");
            response.setDateHeader("Expires", 0L);
            response.setContentType("application/json");
            
            Gson gson = new Gson();  
            OutputStream oStream = response.getOutputStream();
            oStream.write(gson.toJson(nonGPSWayPoints).getBytes());
            oStream.flush();
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
