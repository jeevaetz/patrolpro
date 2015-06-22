/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.servlet;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import schedfoxlib.services.FileService;
import schedfoxlib.services.IncidentService;

/**
 *
 * @author user
 */
@WebServlet(name = "DocumentServlet", urlPatterns = {"/documents/*"})
public class DocumentServlet extends HttpServlet {

    public DocumentServlet() {
        super();
    }
    
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
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document;charset=UTF-8");
        try {
            String document = request.getPathInfo();
            
            FileService fileService = new FileService();
            byte[] fileContents = fileService.getFile(document).getFileContents();
            
            response.setContentLength(fileContents.length);
            response.setHeader("Content-Disposition", "inline; filename=\"" + document + "\"");

            BufferedInputStream input = null;
            BufferedOutputStream output = null;
            try {
                input = new BufferedInputStream(new ByteArrayInputStream(fileContents));
                output = new BufferedOutputStream(response.getOutputStream());
                byte[] buffer = new byte[8192];
                int length;
                while ((length = input.read(buffer)) > -1) {
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
