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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import schedfoxlib.model.MobileReportGraphData;
import schedfoxlib.services.MobileFormService;

/**
 *
 * @author ira
 */
public class LoadTotalReportUsageServlet extends HttpServlet {

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
            ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", new Locale(request.getParameter("lang")));
            
            String companyId = request.getParameter("companyId");
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            String interval = request.getParameter("interval");
            String[] selectedClientIds = request.getParameterValues("clientId");
            ArrayList<Integer> selectedClientIdsInt = new ArrayList<Integer>();
            
            for (int c = 0; c < selectedClientIds.length; c++) {
                selectedClientIdsInt.add(Integer.parseInt(selectedClientIds[c]));
            }
            
            Date startD = new Date();
            Calendar startCal = Calendar.getInstance();
            try {
                startD = new SimpleDateFormat(rb.getString("shortDateStyle")).parse(startDate);
                startCal.setTime(startD);
                startCal.add(Calendar.DAY_OF_MONTH, -1);
            } catch (Exception exe) {}
            
            Date endD = new Date();
            Calendar endCal = Calendar.getInstance();
            try {
                endD = new SimpleDateFormat(rb.getString("shortDateStyle")).parse(endDate);
                endCal.setTime(endD);
                endCal.add(Calendar.DAY_OF_MONTH, 1);
            } catch (Exception exe) {}
            
            
            
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            MobileFormService mobileService = new MobileFormService(companyId);
            ArrayList<MobileReportGraphData> mobileData = mobileService.getMobileReportGraphDataSummary(dbFormat.format(startCal.getTime()), dbFormat.format(endCal.getTime()), interval, selectedClientIdsInt);
            
            LinkedHashMap<String, JFlotLine> currentLineHash = new LinkedHashMap<String, JFlotLine>();
            for (int d = 0; d < mobileData.size(); d++) {
                MobileReportGraphData currData = mobileData.get(d);

                if (!currentLineHash.containsKey(currData.getMobileFormName())) {
                    currentLineHash.put(currData.getMobileFormName(), new JFlotLine());
                }
                JFlotLine currLine = currentLineHash.get(currData.getMobileFormName());
                
                long[] xys = new long[2];
                xys[0] = currData.getWeekBeginning().getTime();
                xys[1] = currData.getNumberFilledOut();
                currLine.getData().add(xys);
                currLine.setLabel(currData.getMobileFormName());
                
                
            }
            
            JFlotLineGraph myGraph = new JFlotLineGraph();
            
            Object[] lines = currentLineHash.values().toArray();
            JFlotLine[] tempLines = new JFlotLine[lines.length];
            for (int l = 0; l < lines.length; l++) {
                tempLines[l] = (JFlotLine)lines[l];
            }
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
