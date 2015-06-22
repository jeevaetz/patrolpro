/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.servlet;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author user
 */
@WebServlet(name = "ImageGPSServlet", urlPatterns = {"/imageGPSServlet/*"})
public class ImageGPSServlet extends HttpServlet {

    private AffineTransform findTranslation(AffineTransform at, BufferedImage bi) {
        Point2D p2din, p2dout;

        p2din = new Point2D.Double(0.0, 0.0);
        p2dout = at.transform(p2din, null);
        double ytrans = p2dout.getY();

        p2din = new Point2D.Double(0, bi.getHeight());
        p2dout = at.transform(p2din, null);
        double xtrans = p2dout.getX();

        AffineTransform tat = new AffineTransform();
        tat.translate(-xtrans, -ytrans);

        return tat;
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
        response.setContentType("image/png;charset=UTF-8");
        try {
            double angle = Double.parseDouble(request.getParameter("angle"));
            ServletContext sc = getServletContext();
            File imageFile = new File(sc.getRealPath("/client/images/white-to-black-arrow.png"));

            BufferedInputStream input = null;
            BufferedOutputStream output = null;
            try {
                //response.setContentLength(input..getDocumentData().length);
                response.setHeader("Content-Disposition", "inline; filename=\"" + "arrow.png" + "\"");

                BufferedImage m_image = ImageIO.read(imageFile);
                output = new BufferedOutputStream(response.getOutputStream());
                try {
                    AffineTransform affineTransform = new AffineTransform();
                    affineTransform.translate(m_image.getWidth() / 2.0, m_image.getHeight() / 2.0);
                    affineTransform.rotate(Math.toRadians(angle));

                    
                    
                    // put origin back to upper left corner
                    affineTransform.translate(-m_image.getWidth() / 2.0, -m_image.getHeight() / 2.0);
                    
                    AffineTransformOp op = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BICUBIC);
                    m_image = op.filter(m_image, null);
            } catch (Exception e) {
                //Do something here
            }

            ImageIO.write(m_image, "png", output);
        } finally {
            if (output != null) {
                try {
                    output.flush();
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

    }
    catch (Exception exe

    
        ) {
            exe.printStackTrace();
    }

    
    

finally {
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
