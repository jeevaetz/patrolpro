/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.reports;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import schedfoxlib.ireports.ReportLookupClass;
import schedfoxlib.model.Branch;
import schedfoxlib.model.Client;
import schedfoxlib.model.Company;
import schedfoxlib.model.Employee;
import schedfoxlib.model.IncidentReport;
import schedfoxlib.model.util.ImageLoader;
import schedfoxlib.services.ClientService;
import schedfoxlib.services.CompanyService;
import schedfoxlib.services.EmployeeService;
import schedfoxlib.services.IncidentService;

/**
 *
 * @author user
 */
@WebServlet(name = "PrintIncidentReportServlet", urlPatterns = {"/client/printincidentservlet"})
public class PrintIndividualIncidentReportServlet extends HttpServlet {

    public PrintIndividualIncidentReportServlet() {
        super();
    }

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
            response.reset();
    
            OutputStream out = response.getOutputStream();
            
            Integer incidentReportId = Integer.parseInt(request.getParameter("incidentReportId"));
            String companyId = request.getParameter("companyId");

            CompanyService companyService = new CompanyService();
            Company comp = companyService.getCompanyById(Integer.parseInt(companyId));
            Company myCompObj = companyService.getCompanyById(Integer.parseInt(companyId));

            IncidentService incidentService = new IncidentService(companyId);
            IncidentReport incidentReport = incidentService.getIncidentReportById(incidentReportId);

            ClientService clientService = new ClientService(companyId);
            
            Employee employee = new EmployeeService(companyId).getEmployeeById(incidentReport.getEmployeeId());
            Client client = clientService.getClientById(incidentReport.getClientId());
            
            URL urlImages = new URL(employee.getEmployeeImageUrl(comp.getCompDB()));
            urlImages.openConnection();
            InputStream iStream = urlImages.openStream();

            Branch clientBranch = null;
            try {
                clientBranch = companyService.getBranchById(client.getBranchId());
            } catch (Exception exe) {
                try {
                    clientBranch = companyService.getBranchById(2);
                } catch (Exception e) {
                }
            }

            HashMap hm = new HashMap();
            hm.put("officerName", employee.getFullName());
            hm.put("incidentDate", incidentReport.getDateEntered());
            hm.put("incidentType", incidentReport.getIncidentType(companyId).getReportType());
            hm.put("incidentText", incidentReport.getText());
            hm.put("incidentReportId", incidentReport.getReadableIncidentId());
            hm.put("REPORT_TIME_ZONE", TimeZone.getTimeZone(clientBranch.getTimezone()));

            try {
                hm.put("clientName", clientService.getClientById(incidentReport.getClientId()).getClientName());
            } catch (Exception exe) {
                hm.put("clientName", "");
            }

            InputStream reportStream = ReportLookupClass.class.getResourceAsStream("IndividualIncidentReport.jasper");
            InputStream imageStream = ReportLookupClass.class.getResourceAsStream("Patrol-Pro-Client-Banner.jpg");

            String language = "en";
            try {
                if (request.getParameter("lang") != null) {
                    language = request.getParameter("lang");
                }
            } catch (Exception exe) {}
            ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", new Locale(language));
            hm.put(JRParameter.REPORT_RESOURCE_BUNDLE, rb); 
            
            try {
                BufferedImage bImage = (BufferedImage) ImageLoader.getImage("login_header.jpg", myCompObj.getDB(), "general").getImage();
                if (bImage.getWidth() > 500) {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();

                    ImageIO.write(bImage, "gif", os);
                    imageStream = new ByteArrayInputStream(os.toByteArray());
                }
            } catch (Exception exe) {
            }
            hm.put("EmployeeImage", iStream);
            hm.put("logo", imageStream);

            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            JasperPrint print = JasperFillManager.fillReport(reportStream, hm, new JRBeanCollectionDataSource(incidentService.getIncidentReportDocumentsForIncident(incidentReport.getIncidentReportId(), true)));
//            // Create an Exporter
            JRExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, bOut);
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
            exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, "UTF-8");
            // Export the file
            exporter.exportReport();
            
            byte[] reportData = bOut.toByteArray();
//            response.addHeader("Content-Length", reportData.length + "");
            response.setContentType("application/pdf;charset=UTF-8");
//            response.addHeader("Content-Disposition", "attachment;filename=\"IncidentReport.PDF\"");
            BufferedOutputStream buffOut = new BufferedOutputStream(out);
            buffOut.write(reportData);
            buffOut.flush();

           
            //out.close();
        } catch (Exception e) {
            e.printStackTrace();
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
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
