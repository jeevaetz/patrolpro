/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import schedfoxlib.model.CompanyObj;
import schedfoxlib.model.util.FileLoader;
import schedfoxlib.services.CompanyService;

/**
 *
 * @author ira
 */
public class UploadPostOrderServlet extends HttpServlet {

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
        try {
            String companyId = request.getParameter("companyId");
            String clientId = request.getParameter("clientId");
            String cid = request.getParameter("cid");
            
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);

            List<FileItem> fields = upload.parseRequest(request);
            
            FileItem fileData = null;
            for (int f = 0; f < fields.size(); f++) {
                if (fields.get(f).getFieldName().equals("file_data")) {
                    fileData = fields.get(f);
                } else if (fields.get(f).getFieldName().equals("companyId")) {
                    companyId = fields.get(f).getString();
                } else if (fields.get(f).getFieldName().equals("clientId")) {
                    clientId = fields.get(f).getString();
                } else if (fields.get(f).getFieldName().equals("cid")) {
                    cid = fields.get(f).getString();
                }
            }
            
            File tempFile = File.createTempFile("postinstr", "tmnp");
            tempFile.deleteOnExit();
            
            CompanyService compService = new CompanyService();
            CompanyObj comp = compService.getCompanyObjById(Integer.parseInt(companyId));
            
            FileOutputStream oStream = new FileOutputStream(tempFile);
            InputStream iStream = fileData.getInputStream();
            byte[] buffer = new byte[2048];
            int numRead = 0;
            while ((numRead = iStream.read(buffer)) > -1) {
                oStream.write(buffer, 0, numRead);
            }
            oStream.flush();
            oStream.close();
            iStream.close();

            boolean isSuccesfull = FileLoader.saveAdditionalFile("location_additional_files",
                    comp.getCompanyDb(), clientId, fileData.getName(), tempFile);
            tempFile.delete();
            
            response.sendRedirect("/client/post_instructions.xhtml?windowId=" + cid);
        } catch (Exception exe) {
            exe.printStackTrace();
        } finally {

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
