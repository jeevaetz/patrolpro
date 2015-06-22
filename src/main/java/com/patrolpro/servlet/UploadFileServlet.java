/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.servlet;

import com.patrolpro.beans.admin.SessionBean;
import com.patrolpro.beans.interfaces.UploadFileInterface;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 *
 * @author ira
 */
public class UploadFileServlet extends HttpServlet {

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
            String companyId = request.getParameter("companyId");
            
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);

            List<FileItem> fields = upload.parseRequest(request);
            
            FileItem fileData = null;
            Integer employeeId = null;
            for (int f = 0; f < fields.size(); f++) {
                if (fields.get(f).getFieldName().equals("file_data")) {
                    fileData = fields.get(f);
                } else if (fields.get(f).getFieldName().equals("employeeId")) {
                   employeeId = Integer.parseInt(fields.get(f).getString());
                }
            }

            InputStream iStream = fileData.getInputStream();

            String beanName = request.getParameter("beanName");

            FacesContext context = FacesServletContextFactory.getInstance().getFacesContext(request, response);
            UploadFileInterface reportHolderBean = context.getApplication().evaluateExpressionGet(context, "#{" + beanName + "}", UploadFileInterface.class);

            ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int bufCount = 0;
            while ((bufCount = iStream.read(buffer)) > -1) {
                bOutput.write(buffer, 0, bufCount);
            }
            bOutput.flush();
            String responseStr = reportHolderBean.processFileUpload(companyId, bOutput.toByteArray(), employeeId);
            if (responseStr.equals("success")) {
                out.write("{}");
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
