/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.servlet.utils;

import com.patrolpro.beans.ClientSessionBean;
import java.util.ArrayList;
import java.util.HashMap;
import javax.faces.context.FacesContext;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import schedfoxlib.model.Client;
import schedfoxlib.model.WayPoint;
import schedfoxlib.services.WayPointService;

/**
 *
 * @author ira
 */
public class BarcodeMapGenerator {

    public static MapModel refreshBarCodes(Boolean allowDrag, MapModel myModel, boolean showTracking, ClientSessionBean sessionBean) {
        try {
            myModel = new DefaultMapModel();
            WayPointService wayPointService = new WayPointService(sessionBean.getCompanyId());

            String appName = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
           
            
            ArrayList<Client> clients = sessionBean.getClients();

            for (int com = 0; com < clients.size(); com++) {
                Client currClient = clients.get(com);
                ArrayList<WayPoint> wayPoints = wayPointService.getWayPointsForClientId(currClient.getClientId());
                HashMap<String, String> wayPointURL = new HashMap<String, String>();
                for (int w = 0; w < wayPoints.size(); w++) {
                    WayPoint currWay = wayPoints.get(w);
                    String iconURL = "";
                    try {
                        String firstChar = currWay.getWaypointName().substring(0, 1);
                        if (wayPointURL.get(firstChar) == null) {
                            iconURL = appName + "/GenerateMarkerIcon?text=" + firstChar;
                            wayPointURL.put(firstChar, firstChar);
                        } else {
                            for (int c = 1; c < 50; c++) {
                                if (wayPointURL.get(firstChar + c) == null) {
                                    iconURL = appName + "/GenerateMarkerIcon?text=" + (firstChar + c);
                                    wayPointURL.put(firstChar + c, firstChar + c);
                                    c = 50;
                                }
                            }
                        }
                    } catch (Exception exe) {
                    }

                    Marker myMarker = new Marker(new LatLng(currWay.getLatitude().doubleValue(), currWay.getLongitude().doubleValue()), currWay.getWaypointName(), currWay.getWaypointData(), iconURL, "../images/non-scanned.png");
                    myMarker.setClickable(allowDrag);
                    myMarker.setDraggable(allowDrag);
                    myMarker.setZindex(currWay.getClientWaypointId());
                    myMarker.setId(currWay.getClientWaypointId() + "");
                    myMarker.setData(currWay.getClientWaypointId());
                    if (showTracking) {
                        myModel.addOverlay(myMarker);
                    }
                }
            }
        } catch (Exception exe) {
        }
        return myModel;
    }
}
