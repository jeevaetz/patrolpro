/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.reports;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Date;
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
import schedfoxlib.model.*;
import schedfoxlib.model.util.ImageLoader;
import schedfoxlib.services.ClientService;
import schedfoxlib.services.CompanyService;
import schedfoxlib.services.EmployeeService;
import schedfoxlib.services.OfficerDailyReportService;

/**
 *
 * @author user
 */
@WebServlet(name = "PrintODRReportServlet", urlPatterns = {"/client/printodrservlet"})
public class PrintODRReportServlet extends HttpServlet {

    public PrintODRReportServlet() {
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
            response.setContentType("application/pdf;charset=UTF-8");

            String odrs = request.getParameter("odrReportId");
            String companyId = request.getParameter("companyId");
            String[] odrArray = odrs.split(",");
            String clientName = "";

            Date startDate = null;
            Date endDate = null;
            Employee employee = null;
            ArrayList<OfficerDailyReportText> texts = new ArrayList<OfficerDailyReportText>();
            ArrayList<Integer> docs = new ArrayList<Integer>();
            ClientService clientService = new ClientService(companyId);
            CompanyService compService = new CompanyService();
            ArrayList<OfficerDailyReportDocument> bytesData = new ArrayList<OfficerDailyReportDocument>();
            Integer clientId = -1;
            for (int o = 0; o < odrArray.length; o++) {
                OfficerDailyReportService odrService = new OfficerDailyReportService(companyId);
                OfficerDailyReport odrReport = odrService.getDailyReportById(Integer.parseInt(odrArray[o]));
                Client client = clientService.getClientById(odrReport.getClientId());
                clientName = client.getClientName();
                ArrayList<OfficerDailyReportText> reportTexts = odrReport.getReportTexts(companyId);
                texts.addAll(reportTexts);
                for (int t = 0; t < reportTexts.size(); t++) {
                    ArrayList<OfficerDailyReportDocument> tempDocs = odrService.getDocumentsForReport(reportTexts.get(t).getOfficerDailyReportTextId(), true);
                    for (int d = 0; d < tempDocs.size(); d++) {
                        docs.add(tempDocs.get(d).getOfficerDailyReportDocumentId());
                        bytesData.add(tempDocs.get(d));
                    }
                }
                clientId = odrReport.getClientId();
                if (employee == null) {
                    employee = new EmployeeService(companyId).getEmployeeById(odrReport.getEmployee_id());
                }
                try {
                    if (startDate == null || startDate.after(odrReport.getLoggedIn())) {
                        startDate = odrReport.getLoggedIn();
                    }
                } catch (Exception exe) {
                }
                try {
                    if (endDate == null || endDate.after(odrReport.getLoggedOut())) {
                        endDate = odrReport.getLoggedOut();
                    }
                } catch (Exception exe) {
                }

            }

            CompanyService companyService = new CompanyService();
            Company comp = companyService.getCompanyById(Integer.parseInt(companyId));
            Company myCompObj = companyService.getCompanyById(Integer.parseInt(companyId));
            
            Client client = clientService.getClientById(clientId);
            Branch clientBranch = null;
            try {
                clientBranch = companyService.getBranchById(client.getBranchId());
            } catch (Exception exe) {
                try {
                    clientBranch = companyService.getBranchById(2);
                } catch (Exception e) {
                }
            }

            URL urlImages = new URL(employee.getEmployeeImageUrl(comp.getCompDB()));
            urlImages.openConnection();
            InputStream iStream = urlImages.openStream();

            HashMap hm = new HashMap();
            hm.put("officerName", employee.getFullName());
            hm.put("officeDailyReportDate", startDate);
            hm.put("loginDate", startDate);
            hm.put("logoutDate", endDate);
            hm.put("odrTexts", texts);
            hm.put("clientName", clientName);
            hm.put("documentIds", docs);
            hm.put("active_db", comp.getCompDB());

            try {
                hm.put("REPORT_TIME_ZONE", TimeZone.getTimeZone(clientBranch.getTimezone()));
            } catch (Exception exe) {
            }

            InputStream reportStream = ReportLookupClass.class.getResourceAsStream("OfficerDailyReport.jasper");
            InputStream imageStream = ReportLookupClass.class.getResourceAsStream("Patrol-Pro-Client-Banner.jpg");

            String language = "en";
            try {
                if (request.getParameter("lang") != null) {
                    language = request.getParameter("lang");
                }
            } catch (Exception exe) {}
            ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", new Locale(language));
            hm.put(JRParameter.REPORT_RESOURCE_BUNDLE, rb); 
            
            BufferedImage bImage = null;
            try {
                bImage = (BufferedImage) ImageLoader.getImage("login_header.jpg", myCompObj.getDB(), "general").getImage();
                if (bImage.getWidth() > 500) {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();

                    ImageIO.write(bImage, "gif", os);
                    imageStream = new ByteArrayInputStream(os.toByteArray());
                }
            } catch (Exception exe) {
            }

            hm.put("SUBREPORT_DIR", ReportLookupClass.class.getResource("OfficerDailyReport.jasper").toURI().toString().replaceAll("OfficerDailyReport.jasper", ""));
            hm.put("EmployeeImage", iStream);
            try {
                hm.put("logo", ImageIO.read(imageStream));
            } catch (Exception exe) {
            }
            hm.put("REPORT_TIME_ZONE", TimeZone.getTimeZone(clientBranch.getTimezone()));

            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://schedfoxdb.schedfox.com/unique_manage?user=db_user&password=sh0rtNsw33t";
            Connection conn = DriverManager.getConnection(url);

            JasperPrint print = JasperFillManager.fillReport(reportStream, hm, new JRBeanCollectionDataSource(bytesData));
            // Create an Exporter
            JRExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
            exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, "UTF-8");
            // Export the file
            exporter.exportReport();

            response.getOutputStream().flush();
            response.getOutputStream().close();

            try {
                conn.close();
            } catch (Exception exe) {
            }
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
