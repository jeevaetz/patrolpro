/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.servlet;

import com.patrolpro.beans.admin.SessionBean;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import schedfoxlib.model.Client;
import schedfoxlib.model.ClientEquipment;
import schedfoxlib.services.ClientService;
import schedfoxlib.services.EquipmentService;

/**
 *
 * @author ira
 */
public class GenerateEquipmentFileServlet extends HttpServlet {

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
        response.setContentType("application/xls;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"EquipmentListing.xls\"");
        ServletOutputStream out = response.getOutputStream();
        try {
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet worksheet = workbook.createSheet("POI Worksheet");

            // index from 0,0... cell A1 is cell(0,0)
            HSSFRow row1 = worksheet.createRow(0);

            HSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(HSSFColor.LIGHT_CORNFLOWER_BLUE.index);
            headerStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

            HSSFCellStyle hiddenStyle = workbook.createCellStyle();
            hiddenStyle.setHidden(true);
            
            HSSFCell cellA1 = row1.createCell(0);
            cellA1.setCellValue("Account Number");
            cellA1.setCellStyle(headerStyle);

            HSSFCell cellB1 = row1.createCell(1);
            cellB1.setCellValue("Phone Number");
            cellB1.setCellStyle(headerStyle);

            HSSFCell cellC1 = row1.createCell(2);
            cellC1.setCellValue("User");
            cellC1.setCellStyle(headerStyle);

            HSSFCell cellD1 = row1.createCell(3);
            cellD1.setCellValue("Plan");
            cellD1.setCellStyle(headerStyle);

            HSSFCell cellE1 = row1.createCell(4);
            cellE1.setCellValue("Voice");
            cellE1.setCellStyle(headerStyle);

            HSSFCell cellF1 = row1.createCell(5);
            cellF1.setCellValue("Data");
            cellF1.setCellStyle(headerStyle);

            HSSFCell cellG1 = row1.createCell(6);
            cellG1.setCellValue("Text");
            cellG1.setCellStyle(headerStyle);

            HSSFCell cellH1 = row1.createCell(7);
            cellH1.setCellValue("Cost");
            cellH1.setCellStyle(headerStyle);

            HSSFCell cellI1 = row1.createCell(8);
            cellI1.setCellValue("Extra Features");
            cellI1.setCellStyle(headerStyle);

            HSSFCell cellJ1 = row1.createCell(9);
            cellJ1.setCellValue("Extra Costs");
            cellJ1.setCellStyle(headerStyle);

            HSSFCell cellK1 = row1.createCell(10);
            cellK1.setCellValue("Total");
            cellK1.setCellStyle(headerStyle);

            HSSFCell cellL1 = row1.createCell(11);
            cellL1.setCellValue("Account Expiration");
            cellL1.setCellStyle(headerStyle);

            HSSFCell cellM1 = row1.createCell(12);
            cellM1.setCellValue("Voice Mail Code");
            cellM1.setCellStyle(headerStyle);

            HSSFCell cellN1 = row1.createCell(13);
            cellN1.setCellValue("ID");
            cellN1.setCellStyle(hiddenStyle);
            
            try {
                SessionBean mySessionBean = (SessionBean)request.getSession().getAttribute("sessionBean");
                
                EquipmentService equipmentService = new EquipmentService(mySessionBean.getCompanyId());
                ClientService clientService = new ClientService(mySessionBean.getCompanyId());
                ArrayList<ClientEquipment> clientEquipment = equipmentService.getClientEquipmentGroupedByMdn(-1, -1, true);
                HashMap<Integer, Client> clientHash = new HashMap<Integer, Client>();

                for (int c = 0; c < clientEquipment.size(); c++) {
                    ClientEquipment equipment = clientEquipment.get(c);
                    HSSFRow currRow = worksheet.createRow(c + 1);

                    if (clientHash.get(equipment.getClientId()) == null) {
                        clientHash.put(equipment.getClientId(), clientService.getClientById(equipment.getClientId()));
                    }
                    equipment.setEntity(clientHash.get(equipment.getClientId()));

                    HSSFCell cA1 = currRow.createCell(0);
                    cA1.setCellValue("");

                    HSSFCell cB1 = currRow.createCell(1);
                    cB1.setCellValue(equipment.getPhoneNumber());

                    HSSFCell cC1 = currRow.createCell(2);
                    cC1.setCellValue(equipment.getEntity().getClientName());

                    HSSFCell cD1 = currRow.createCell(3);
                    cD1.setCellValue("");

                    HSSFCell cE1 = currRow.createCell(4);
                    cE1.setCellValue("");

                    HSSFCell cF1 = currRow.createCell(5);
                    if (equipment.getEquipmentId() == 2) {
                        cF1.setCellValue(true);
                    } else {
                        cF1.setCellValue("");
                    }
                    
                    HSSFCell cG1 = currRow.createCell(6);
                    cG1.setCellValue("");

                    HSSFCell cH1 = currRow.createCell(7);
                    try {
                        cH1.setCellValue(equipment.getCost().doubleValue());
                    } catch (Exception exe) {
                        cH1.setCellValue("");
                    }

                    HSSFCell cI1 = currRow.createCell(8);
                    cI1.setCellValue("");

                    HSSFCell cJ1 = currRow.createCell(9);
                    cJ1.setCellValue("");

                    HSSFCell cK1 = currRow.createCell(10);
                    cK1.setCellValue("");

                    HSSFCell cL1 = currRow.createCell(11);
                    try {
                        cL1.setCellValue(new SimpleDateFormat("MM/dd/yyyy").format(equipment.getDateOfContractRenewal()));
                    } catch (Exception exe) {
                        cL1.setCellValue("");
                    }

                    HSSFCell cM1 = currRow.createCell(12);
                    cM1.setCellValue("");
                    
                    HSSFCell cN1 = currRow.createCell(13);
                    cN1.setCellValue(equipment.getClientEquipmentId());
                    cN1.setCellStyle(hiddenStyle);
                }
            } catch (Exception exe) {
                exe.printStackTrace();
            }
            workbook.write(out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
