/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.servlet;

import com.google.gson.Gson;
import com.patrolpro.model.LatLongObj;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import schedfoxlib.model.Client;
import schedfoxlib.model.ClientEquipment;
import schedfoxlib.model.Employee;
import schedfoxlib.model.GPSCoordinate;
import schedfoxlib.model.WayPointCoordinate;
import schedfoxlib.services.ClientService;
import schedfoxlib.services.CompanyService;
import schedfoxlib.services.EmployeeService;
import schedfoxlib.services.EquipmentService;
import schedfoxlib.services.GPSService;

/**
 *
 * @author user
 */
@WebServlet(name = "GPSCoordinatesServlet", urlPatterns = {"/client/gpscoordinates"})
public class GPSCoordinatesServlet extends HttpServlet {

    public static Integer numberOfSeconds = 5;

    public GPSCoordinatesServlet() {
        super();
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        TreeMap<Integer, TreeMap<Integer, ArrayList<LatLongObj>>> markers = new TreeMap<Integer, TreeMap<Integer, ArrayList<LatLongObj>>>();
        try {
            String companyId = request.getParameter("companyId");
            String branchId = request.getParameter("branchId");

            GPSService gpsService = new GPSService(companyId);
            EmployeeService employeeService = new EmployeeService(companyId);
            EquipmentService equipmentService = new EquipmentService(companyId);

            Calendar selectedStartDate = Calendar.getInstance();

            ArrayList<GPSCoordinate> gpsCoordinates = new ArrayList<GPSCoordinate>();
            ArrayList<WayPointCoordinate> wayPointCoordinates = new ArrayList<WayPointCoordinate>();
            HashMap<Integer, Employee> employeeHash = new HashMap<Integer, Employee>();
            HashMap<Integer, ClientEquipment> equipmentHash = new HashMap<Integer, ClientEquipment>();
            String dateStr = "";
            Integer clientId = 0;
            try {
                dateStr = request.getParameter("date").toString();
                clientId = Integer.parseInt(request.getParameter("clientId").toString());
                String timeZone = "US/Central";
                try {
                    CompanyService compService = new CompanyService();
                    if (branchId == null || branchId.toString().equals("0")) {
                        ClientService clientService = new ClientService(companyId);
                        Client myCli = clientService.getClientById(clientId);
                        timeZone = compService.getBranchById(myCli.getBranchId()).getTimezone();
                    } else {
                        timeZone = compService.getBranchById(Integer.parseInt(branchId)).getTimezone();
                    }
                } catch (Exception exe) {
                }

                Date selectedDate = new SimpleDateFormat("MM/dd/yy").parse(dateStr);

                selectedStartDate.setTime(selectedDate);
                selectedStartDate.set(Calendar.HOUR_OF_DAY, 0);
                selectedStartDate.set(Calendar.MINUTE, 0);

                Calendar selectedEndDate = Calendar.getInstance();
                selectedEndDate.setTime(selectedDate);
                selectedEndDate.add(Calendar.DAY_OF_MONTH, 1);
                selectedEndDate.set(Calendar.HOUR_OF_DAY, 0);
                selectedEndDate.set(Calendar.MINUTE, 0);

                ArrayList<ClientEquipment> clientEquipment = equipmentService.getClientEquipmentByTypeAndId(2, clientId, false);
                ArrayList<Integer> clientIds = new ArrayList<Integer>();
                for (int c = 0; c < clientEquipment.size(); c++) {
                    clientIds.add(clientEquipment.get(c).getClientEquipmentId());
                }

                gpsCoordinates = gpsService.getCoordinatesForDateForClientAndTimezone(new SimpleDateFormat("yyyy-MM-dd").format(selectedDate), new SimpleDateFormat("yyyy-MM-dd").format(selectedDate), clientId, timeZone);
                wayPointCoordinates = gpsService.getWayPointsCoordinateByTimezone(selectedStartDate.getTime(), selectedEndDate.getTime(), clientId, timeZone);
            } catch (Exception exe) {
                exe.printStackTrace();
            }

            SimpleDateFormat myFormat = new SimpleDateFormat("HHmm");

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

            Calendar timeOfLastEntry = Calendar.getInstance();
            Calendar timeOfCurrentEntry = Calendar.getInstance();

            Calendar myCal = Calendar.getInstance();
            myCal.setTime(selectedStartDate.getTime());
            myCal.add(Calendar.DAY_OF_MONTH, -1);

            for (int g = 0; g < gpsCoordinates.size(); g++) {
                try {
                    if (gpsCoordinates.get(g).getRecordedOn().after(myCal.getTime())) {
                        timeOfCurrentEntry.setTime(gpsCoordinates.get(g).getRecordedOn());
                        timeOfCurrentEntry.add(Calendar.SECOND, -1 * numberOfSeconds);

                        timeOfLastEntry.setTime(gpsCoordinates.get(g).getRecordedOn());
                        GPSCoordinate gps = gpsCoordinates.get(g);
                        LatLongObj obj = new LatLongObj();
                        obj.setMarkerId(0);
                        obj.setIsMarker(false);
                        try {
                            obj.setSpeed(gps.getSpeed().doubleValue());
                        } catch (Exception exe) {
                            obj.setSpeed(0.0);
                        }
                        obj.setLat(gps.getLatitude().doubleValue());
                        obj.setLog(gps.getLongitude().doubleValue());
                        obj.setTime(dateFormat.format(gps.getRecordedOn()));
                        obj.setMarkerScanned(gps.getRecordedOn());
                        obj.setAccuracy(gps.getAccuracy().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        try {
                            if (employeeHash.get(gps.getEmployeeId()) == null) {
                                employeeHash.put(gps.getEmployeeId(), employeeService.getEmployeeById(gps.getEmployeeId()));
                            }
                            obj.setEmployeeName(employeeHash.get(gps.getEmployeeId()).getFullName());
                            obj.setImageUrl(employeeHash.get(gps.getEmployeeId()).getEmployeeImageUrl());
                        } catch (Exception e) {
                            employeeHash.put(gps.getEmployeeId(), new Employee());
                        }
                        try {
                            if (equipmentHash.get(gps.getEquipmentId()) == null) {
                                equipmentHash.put(gps.getEquipmentId(), equipmentService.getEquipmentByPrimaryId(gps.getEquipmentId()));
                            }
                            obj.setDeviceNickname(equipmentHash.get(gps.getEquipmentId()).getNickname());
                        } catch (Exception e) {
                            obj.setDeviceNickname("");
                        }
                        int entryArea = Integer.parseInt(myFormat.format(gps.getRecordedOn()));
                        entryArea = entryArea - (entryArea % numberOfSeconds);
                        if (markers.get(entryArea) == null) {
                            markers.put(entryArea, new TreeMap<Integer, ArrayList<LatLongObj>>());
                        }
                        TreeMap<Integer, ArrayList<LatLongObj>> tempMarkers = markers.get(entryArea);
                        if (tempMarkers.get(gps.getEmployeeId()) == null) {
                            tempMarkers.put(gps.getEmployeeId(), new ArrayList<LatLongObj>());
                        }
                        tempMarkers.get(gps.getEmployeeId()).add(obj);
                    }
                } catch (Exception exe) {
                    //exe.printStackTrace();
                }

            }
            for (int w = 0; w < wayPointCoordinates.size(); w++) {
                WayPointCoordinate coord = wayPointCoordinates.get(w);
                int entryArea = Integer.parseInt(myFormat.format(coord.getRecordedOn()));

                LatLongObj obj = new LatLongObj();
                obj.setMarkerId(coord.getIdentifier());
                obj.setIsMarker(true);
                obj.setLat(coord.getLatitude().doubleValue());
                obj.setLog(coord.getLongitude().doubleValue());
                obj.setTime(dateFormat.format(coord.getRecordedOn()));
                obj.setMarkerScanned(coord.getRecordedOn());
                obj.setAccuracy(0);
                obj.setDeviceNickname("");
                obj.setSpeed(0);
                try {
                    if (employeeHash.get(coord.getEmployeeId()) == null) {
                        employeeHash.put(coord.getEmployeeId(), employeeService.getEmployeeById(coord.getEmployeeId()));
                    }
                    obj.setEmployeeName(employeeHash.get(coord.getEmployeeId()).getFullName());
                    obj.setImageUrl(employeeHash.get(coord.getEmployeeId()).getEmployeeImageUrl());
                } catch (Exception e) {
                    obj.setEmployeeName("");
                }

                if (markers.get(entryArea) == null) {
                    markers.put(entryArea, new TreeMap<Integer, ArrayList<LatLongObj>>());
                }

                TreeMap<Integer, ArrayList<LatLongObj>> tempMarkers = markers.get(entryArea);
                if (tempMarkers.get(coord.getEmployeeId()) == null) {
                    tempMarkers.put(coord.getEmployeeId(), new ArrayList<LatLongObj>());
                }
                tempMarkers.get(coord.getEmployeeId()).add(obj);
            }

            Iterator<Integer> keys = markers.keySet().iterator();
            while (keys.hasNext()) {
                Integer key = keys.next();
                TreeMap<Integer, ArrayList<LatLongObj>> values = markers.get(key);
                Iterator<Integer> keyIterator = values.keySet().iterator();
                ArrayList<LatLongObj> myKeys = values.get(keyIterator.next());
                double minValue = 10000.0;
                for (int k = myKeys.size() - 1; k >= 0; k--) {
                    if (myKeys.get(k).getAccuracy() < minValue + 20.0) {
                        minValue = myKeys.get(k).getAccuracy();
                    } else {
                        myKeys.remove(k);
                    }
                }
                try {
                    Collections.sort(myKeys);
                } catch (Exception exe) {
                }
            }

            response.setContentType("application/json");
            response.addHeader("Pragma", "No-Cache");
            response.setDateHeader("Expires", 0L);
            response.setContentType("application/json");

            Gson gson = new Gson();
            OutputStream oStream = response.getOutputStream();
            oStream.write(gson.toJson(markers).getBytes());
            oStream.flush();
        } catch (Exception exe) {
            exe.printStackTrace();
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

}
