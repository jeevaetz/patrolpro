/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.servlet;

import com.google.gson.Gson;
import com.patrolpro.model.JFlotLine;
import com.patrolpro.model.JFlotLineGraph;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import schedfoxlib.model.CompanyObj;
import schedfoxlib.model.DeviceCounts;
import schedfoxlib.services.CompanyService;
import schedfoxlib.services.EquipmentService;

/**
 *
 * @author ira
 */
public class LoadDeviceCountsServlet extends HttpServlet {

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
//            String startDate = request.getParameter("startDate");
//            String endDate = request.getParameter("endDate");
//            String interval = request.getParameter("interval");
//            String[] selectedClientIds = request.getParameterValues("clientId");
            
            EquipmentService equipService = new EquipmentService(companyId);
            CompanyService compService = new CompanyService();
            ArrayList<CompanyObj> companyObj = compService.getCompaniesForPatrolPro();
            ArrayList<Integer> companyIds = new ArrayList<Integer>();
            for (int c = 0; c < companyObj.size(); c++) {
                companyIds.add(companyObj.get(c).getCompanyId());
            }
            ArrayList<DeviceCounts> counts = equipService.getDeviceCounts(companyIds);
  
            LinkedHashMap<Long, Integer> countsConsolidated = new LinkedHashMap<Long, Integer>();
            for (int c = 0; c < counts.size(); c++) {
                if (!countsConsolidated.containsKey(counts.get(c).getTimeIncrementEnds().getTime())) {
                    countsConsolidated.put(counts.get(c).getTimeIncrementEnds().getTime(), 0);
                }
                Integer currCount = countsConsolidated.get(counts.get(c).getTimeIncrementEnds().getTime());
                currCount += counts.get(c).getCount();
                countsConsolidated.put(counts.get(c).getTimeIncrementEnds().getTime(), currCount);
            }
            
            JFlotLine currLine = new JFlotLine();
            currLine.setLabel("Daily Device Usage");
            Iterator<Long> keys = countsConsolidated.keySet().iterator();
            while (keys.hasNext()) {
                Long key = keys.next();
                Integer count = countsConsolidated.get(key);

                long[] xys = new long[2];
                xys[0] = key;
                xys[1] = count;
                currLine.getData().add(xys);
            }
            
            JFlotLineGraph myGraph = new JFlotLineGraph();
            
            JFlotLine[] tempLines = new JFlotLine[1];
            tempLines[0] = currLine;
            myGraph.setData(tempLines);
            
            Gson gson = new Gson();
            gson.toJson(myGraph, out);
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
