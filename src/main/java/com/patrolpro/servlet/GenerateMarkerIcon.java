/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.servlet;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author ira
 */
public class GenerateMarkerIcon extends HttpServlet {

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
        String text = request.getParameter("text");
        Boolean isSelected = false;
        try {
            isSelected = Boolean.parseBoolean(request.getParameter("isSel"));
        } catch (Exception exe) {}
        response.setContentType("image/png;charset=UTF-8");
        InputStream reportStream = request.getServletContext().getResourceAsStream("/images/blue_dot.png");
        if (isSelected) {
            reportStream = request.getServletContext().getResourceAsStream("/images/green_dot.png");
        }
        BufferedImage image = ImageIO.read(reportStream);
        ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        try {
            Font font1 = new Font("Arial Black", Font.PLAIN, 10);
            
            Graphics graphics = image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.setFont(font1);

            FontMetrics fm1 = graphics.getFontMetrics(font1);
            //graphics.drawString(s[1], width / 2 - fm1.stringWidth(s[1]) / 2, height / 4 - 70);

            graphics.drawString(text, (image.getWidth() / 2 - fm1.stringWidth(text) / 2) + 1, 16);

            ImageIO.write(image, "PNG", oStream);
            
            byte[] data = oStream.toByteArray();
            
            response.addHeader("Content-Length", data.length + "");
            response.getOutputStream().write(data);
            
            response.flushBuffer();
            reportStream.close();
        } finally {
            reportStream.close();
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
