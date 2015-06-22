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
import schedfoxlib.model.ClientEquipment;
import schedfoxlib.model.Employee;
import schedfoxlib.services.EmployeeService;

/**
 *
 * @author ira
 */
public class GenerateEmployeeFileServlet extends HttpServlet {

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
        response.setHeader("Content-Disposition", "attachment; filename=\"EmployeeListing.xls\"");
        ServletOutputStream out = response.getOutputStream();
        try {
            String companyId = request.getParameter("companyId");
            
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
            cellA1.setCellValue("First Name");
            cellA1.setCellStyle(headerStyle);

            HSSFCell cellB1 = row1.createCell(1);
            cellB1.setCellValue("Last Name");
            cellB1.setCellStyle(headerStyle);

            HSSFCell cellC1 = row1.createCell(2);
            cellC1.setCellValue("SSN");
            cellC1.setCellStyle(headerStyle);

            HSSFCell cellD1 = row1.createCell(3);
            cellD1.setCellValue("Address");
            cellD1.setCellStyle(headerStyle);

            HSSFCell cellE1 = row1.createCell(4);
            cellE1.setCellValue("Address 2");
            cellE1.setCellStyle(headerStyle);

            HSSFCell cellF1 = row1.createCell(5);
            cellF1.setCellValue("City");
            cellF1.setCellStyle(headerStyle);

            HSSFCell cellG1 = row1.createCell(6);
            cellG1.setCellValue("State");
            cellG1.setCellStyle(headerStyle);

            HSSFCell cellH1 = row1.createCell(7);
            cellH1.setCellValue("Zip");
            cellH1.setCellStyle(headerStyle);

            HSSFCell cellI1 = row1.createCell(8);
            cellI1.setCellValue("Phone Number");
            cellI1.setCellStyle(headerStyle);

            HSSFCell cellJ1 = row1.createCell(9);
            cellJ1.setCellValue("Phone Number 2");
            cellJ1.setCellStyle(headerStyle);

            HSSFCell cellK1 = row1.createCell(10);
            cellK1.setCellValue("Cell Number");
            cellK1.setCellStyle(headerStyle);

            HSSFCell cellL1 = row1.createCell(11);
            cellL1.setCellValue("Email");
            cellL1.setCellStyle(headerStyle);

            HSSFCell cellM1 = row1.createCell(12);
            cellM1.setCellValue("Hire Date");
            cellM1.setCellStyle(headerStyle);

            HSSFCell cellN1 = row1.createCell(13);
            cellN1.setCellValue("BirthDate");
            cellN1.setCellStyle(hiddenStyle);
            
            try {               
                EmployeeService employeeService = new EmployeeService(companyId);
                ArrayList<Employee> emps = employeeService.getAllActiveEmployees();

                for (int c = 0; c < emps.size(); c++) {
                    Employee employee = emps.get(c);
                    HSSFRow currRow = worksheet.createRow(c + 1);

                    HSSFCell cA1 = currRow.createCell(0);
                    try {
                        cA1.setCellValue(employee.getEmployeeFirstName().replaceAll(",", ""));
                    } catch (Exception exe) {
                        cA1.setCellValue("");
                    }

                    HSSFCell cB1 = currRow.createCell(1);
                    try {
                        cB1.setCellValue(employee.getEmployeeLastName().replaceAll(",", ""));
                    } catch (Exception exe) {
                        cB1.setCellValue("");
                    }

                    HSSFCell cC1 = currRow.createCell(2);
                    try {
                        if (!companyId.equals("2")) {
                            cC1.setCellValue(employee.getEmployeeSsn().replaceAll(",", ""));
                        }
                    } catch (Exception exe) {
                        cC1.setCellValue("");
                    }

                    HSSFCell cD1 = currRow.createCell(3);
                    try {
                        cD1.setCellValue(employee.getAddress1().replaceAll(",", ""));
                    } catch (Exception exe) {
                        cD1.setCellValue("");
                    }

                    HSSFCell cE1 = currRow.createCell(4);
                    try {
                        cE1.setCellValue(employee.getAddress2().replaceAll(",", ""));
                    } catch (Exception exe) {
                        cE1.setCellValue("");
                    }

                    HSSFCell cF1 = currRow.createCell(5);
                    try {
                        cF1.setCellValue(employee.getCity().replaceAll(",", ""));
                    } catch (Exception exe) {
                        cF1.setCellValue("");
                    }
                    
                    HSSFCell cG1 = currRow.createCell(6);
                    try {
                        cG1.setCellValue(employee.getState().replaceAll(",", ""));
                    } catch (Exception exe) {
                        cG1.setCellValue("");
                    }

                    HSSFCell cH1 = currRow.createCell(7);
                    try {
                        cH1.setCellValue(employee.getZip().replaceAll(",", ""));
                    } catch (Exception exe) {
                        cH1.setCellValue("");
                    }

                    HSSFCell cI1 = currRow.createCell(8);
                    try {
                        cI1.setCellValue(employee.getEmployeePhone().replaceAll(",", ""));
                    } catch (Exception exe) {
                        cI1.setCellValue("");
                    }

                    HSSFCell cJ1 = currRow.createCell(9);
                    try {
                        cJ1.setCellValue(employee.getEmployeePhone2().replaceAll(",", ""));
                    } catch (Exception exe) {
                        cJ1.setCellValue("");
                    }

                    HSSFCell cK1 = currRow.createCell(10);
                    try {
                        cK1.setCellValue(employee.getEmployeeCell().replaceAll(",", ""));
                    } catch (Exception exe) {
                        cK1.setCellValue("");
                    }

                    HSSFCell cL1 = currRow.createCell(11);
                    try {
                        cL1.setCellValue(employee.getEmployeeEmail().replaceAll(",", ""));
                    } catch (Exception exe) {
                        cL1.setCellValue("");
                    }

                    HSSFCell cM1 = currRow.createCell(12);
                    try {
                        cM1.setCellValue(new SimpleDateFormat("MM/dd/yyyy").format(employee.getEmployeeHireDate()));
                    } catch (Exception exe) {
                        cM1.setCellValue("");
                    }
                    
                    HSSFCell cN1 = currRow.createCell(13);
                    try {
                        cN1.setCellValue(new SimpleDateFormat("MM/dd/yyyy").format(employee.getEmployeeBirthdate()));
                    } catch (Exception exe) {
                        cN1.setCellValue("");
                    }
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
