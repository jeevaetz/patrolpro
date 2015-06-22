/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.servlet;

import com.patrolpro.beans.admin.SessionBean;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import schedfoxlib.model.MobileFormData;
import schedfoxlib.model.MobileForms;
import schedfoxlib.services.MobileFormService;

/**
 *
 * @author ira
 */
public class UploadJasperReportServlet extends HttpServlet {

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
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);

            String companyId = request.getParameter("companyId");
            
            FileItem fileData = null;
            Integer mobileFormId = null;
            List<FileItem> fields = upload.parseRequest(request);
            for (int f = 0; f < fields.size(); f++) {
                if (fields.get(f).getFieldName().equals("file_data")) {
                    fileData = fields.get(f);
                } else if (fields.get(f).getFieldName().equals("mobileFormId")) {
                   mobileFormId = Integer.parseInt(fields.get(f).getString());
                }
            }
            if (fileData == null || !fileData.getName().endsWith(".jrxml")) {
                out.write("{\"error\": \"Invalid file type! Please select a jrxml (Jasper Report) file!\"}");
                out.flush();
            } else {
                InputStream iStream = fileData.getInputStream();

                MobileFormService mobileService = new MobileFormService(companyId);

                ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
                byte[] buffer = new byte[2048];
                int bufCount = 0;
                while ((bufCount = iStream.read(buffer)) > -1) {
                    bOutput.write(buffer, 0, bufCount);
                }
                bOutput.flush();

                byte[] rawData = bOutput.toByteArray();
                JasperReport jasperReport = JasperCompileManager.compileReport(new ByteArrayInputStream(rawData));
                JRParameter[] params = jasperReport.getParameters();
                HashMap<String, Class> reportParams = new HashMap<String, Class>();
                for (int p = 0; p < params.length; p++) {
                    JRParameter param = params[p];
                    int searchPos = -1;
                    for (int a = 0; a < MobileForms.getReservedIdentifiers().length; a++) {
                        if (MobileForms.getReservedIdentifiers()[a].equals(param.getName())) {
                            searchPos = a;
                        }
                    }
                    if (!param.isSystemDefined() && searchPos < 0 && !param.getName().startsWith("nfc_l_")  && !param.getName().endsWith("_loc")) {
                        reportParams.put(param.getName(), param.getValueClass());
                    }
                }

                
                
                ByteArrayOutputStream oStream = new ByteArrayOutputStream();
                
                JasperCompileManager.writeReportToXmlStream(jasperReport, oStream);
                //JasperCompileManager.compileReportToStream(new ByteArrayInputStream(rawData), oStream);
                
                MobileForms selectedForm = mobileService.getForm(mobileFormId);
                selectedForm.setReportData(oStream.toByteArray());
                mobileService.saveForm(selectedForm);

                Iterator<String> keyIterator = reportParams.keySet().iterator();
                ArrayList<MobileFormData> currData = mobileService.getFormData(selectedForm.getMobileFormsId());
                int numberInserted = 1;
                while (keyIterator.hasNext()) {
                    String key = keyIterator.next();

                    boolean hasData = false;
                    for (int d = 0; d < currData.size(); d++) {
                        if (currData.get(d).getDataLabel().equals(key) && currData.get(d).getActive()) {
                            hasData = true;
                        }
                    }
                    if (!hasData) {
                        MobileFormData formData = new MobileFormData();
                        formData.setActive(true);
                        formData.setMobileFormsId(selectedForm.getMobileFormsId());
                        formData.setDataLabel(key);
                        if (reportParams.get(key) == Date.class) {
                            formData.setDateType(5);
                        } else if (reportParams.get(key) == InputStream.class) {
                            formData.setDateType(8);
                        } else if (reportParams.get(key) == Boolean.class) {
                            formData.setDateType(3);
                        } else {
                            formData.setDateType(1);
                        }
                        formData.setOrdering(currData.size() + numberInserted);
                        mobileService.saveFormData(formData);

                        numberInserted++;
                    }
                }

                out.write("{}");
                out.flush();
            }
        } catch (JRException jr) {
            out.write("{\"error\": \"" + jr.getMessage().replaceAll("\n", "").replaceAll(":", "").replaceAll("\t","") + "\"}");
            out.flush();
        } catch (Exception e) {
            out.write("{\"error\": \"Exception uploading report, " + e + "\"}");
            out.flush();
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
