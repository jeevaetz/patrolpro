/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import com.patrolpro.beans.interfaces.AbstractMappingBean;
import com.patrolpro.servlet.utils.BarcodeMapGenerator;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.map.MarkerDragEvent;
import org.primefaces.event.map.StateChangeEvent;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import schedfoxlib.model.*;
import schedfoxlib.services.CompanyService;
import schedfoxlib.services.EmployeeService;
import schedfoxlib.services.WayPointService;

/**
 *
 * @author user
 */
@Named("barcodeBean")
@ViewScoped
public class BarcodeBean extends AbstractMappingBean implements Serializable {

    @Inject
    private ClientSessionBean sessionBean;

    private ArrayList<WayPoint> wayPoints;
    private MapModel polylineModel;
    private ArrayList<ClientWaypointScan> wayPointScans;
    private WayPoint selectedWayPoint;
    private Date startDate;
    private Date endDate;
    private Integer currentWayPointId;
    private Integer itemToDelete;

    public BarcodeBean() {

    }

    @PostConstruct
    public void postConstruct() {
        try {
            itemToDelete = new Integer(0);
            wayPoints = new ArrayList<WayPoint>();
            selectedWayPoint = new WayPoint();
            wayPointScans = new ArrayList<ClientWaypointScan>();

            startDate = new Date();
            endDate = new Date();
            
            this.refreshWayPoints();
            this.refreshMap(sessionBean);
            
            polylineModel = BarcodeMapGenerator.refreshBarCodes(sessionBean.getIsLoggedInAsAdmin(), this.polylineModel, true, this.getSessionBean());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleLogView(WayPoint wayPoint) {
        this.selectedWayPoint = wayPoint;
        this.loadWayPointScans(selectedWayPoint.getClientWaypointId());
    }

    public void handleStartDateSelect(SelectEvent event) {
        this.startDate = (Date) event.getObject();
        loadWayPointScans(this.selectedWayPoint.getClientWaypointId());
    }

    public void handleEndDateSelect(SelectEvent event) {
        this.endDate = (Date) event.getObject();
        loadWayPointScans(this.selectedWayPoint.getClientWaypointId());
    }

    public void setLatLng(Integer clientWaypointId) {
        try {
            for (int w = 0; w < wayPoints.size(); w++) {
                if (wayPoints.get(w).getClientWaypointId() == clientWaypointId.intValue()) {
                    selectedWayPoint = wayPoints.get(w);
                }
            }
            if (selectedWayPoint != null) {
                if (selectedWayPoint.getLatitude() == null || selectedWayPoint.getLatitude().doubleValue() == 0) {
                    selectedWayPoint.setLatitude(new BigDecimal(this.getLatitude()));
                    selectedWayPoint.setLongitude(new BigDecimal(this.getLongitude()));

                    WayPointService wayPointService = new WayPointService(sessionBean.getCompanyId());
                    wayPointService.saveWayPoint(selectedWayPoint);

                    selectedWayPoint = new WayPoint();

                    this.refreshWayPoints();
                }
            }
        } catch (Exception exe) {
        }
    }

    public void addMarkerGPS() {
        for (int m = 0; m < wayPoints.size(); m++) {
            try {
                if (currentWayPointId.intValue() == wayPoints.get(m).getClientWaypointId().intValue()) {
                    wayPoints.get(m).setLatitude(new BigDecimal(this.getLatitude()));
                    wayPoints.get(m).setLongitude(new BigDecimal(this.getLongitude()));
                    WayPointService wayPointService = new WayPointService(sessionBean.getCompanyId());
                    wayPointService.saveWayPoint(wayPoints.get(m));

                    refreshWayPoints();
                }
            } catch (Exception exe) {
                exe.printStackTrace();
            }
        }
    }

    private void refreshWayPoints() {
        try {
            WayPointService wayPointService = new WayPointService(sessionBean.getCompanyId());

            ArrayList<Client> clients = sessionBean.getClients();

            wayPoints = new ArrayList<WayPoint>();
            HashMap<String, String> wayPointURL = new HashMap<String, String>();
            for (int com = 0; com < clients.size(); com++) {
                Client currClient = clients.get(com);
                wayPoints.addAll(wayPointService.getWayPointsForClientId(currClient.getClientId()));
                
                for (int w = 0; w < wayPoints.size(); w++) {
                    try {
                        int length = 2;
                        if (wayPoints.get(w).getWaypointName().length() < length) {
                            length = wayPoints.get(w).getWaypointName().length();
                        }
                        String firstChars = wayPoints.get(w).getWaypointName().substring(0, length);
                        try {
                            Integer firstNum = Integer.parseInt(firstChars);
                            wayPoints.get(w).setIconURL("/GenerateMarkerIcon?text=" + firstNum);
                            wayPointURL.put(firstNum.toString(), firstNum.toString());
                        } catch (Exception exe) {
                            if (wayPointURL.get(firstChars) == null) {
                                wayPoints.get(w).setIconURL("/GenerateMarkerIcon?text=" + firstChars);
                                wayPointURL.put(firstChars, firstChars);
                            } else {
                                for (int c = 1; c < 50; c++) {
                                    if (wayPointURL.get(firstChars + c) == null) {
                                        wayPoints.get(w).setIconURL("/GenerateMarkerIcon?text=" + (firstChars + c));
                                        wayPointURL.put(firstChars + c, firstChars + c);
                                        c = 50;
                                    }
                                }
                            }
                        }
                    } catch (Exception exe) {
                        exe.printStackTrace();
                    }
                }
            }
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    public void loadWayPointScans(Integer wayPoint) {
        try {
            String timeZone = "US/Central";
            try {
                CompanyService compService = new CompanyService();
                timeZone = compService.getBranchById(sessionBean.getSelectedBranchId()).getTimezone();
            } catch (Exception exe) {
            }
            WayPointService wayPointService = new WayPointService(sessionBean.getCompanyId());
            EmployeeService userService = new EmployeeService(sessionBean.getCompanyId());
            wayPointScans = wayPointService.getWayPointScansWithTimezone(wayPoint, startDate, endDate, timeZone);
            HashMap<Integer, Employee> users = new HashMap<Integer, Employee>();
            for (int w = 0; w < wayPointScans.size(); w++) {
                int userId = wayPointScans.get(w).getUserId();
                if (users.get(userId) == null) {
                    Employee myUser = userService.getEmployeeById(userId);
                    users.put(userId, myUser);
                }
                wayPointScans.get(w).setUserObj(users.get(userId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addWayPoint() {
        selectedWayPoint = new WayPoint();
    }

    public void save(ActionEvent actionEvent) {
        try {
            Client currentClient = getSessionBean().getSelectedClient();

            selectedWayPoint.setClientId(currentClient.getClientId());
            selectedWayPoint.setActive(true);
            try {
                if (selectedWayPoint.getLatitude() == null) {
                    selectedWayPoint.setLatitude(new BigDecimal(this.getLatitude()));
                }
            } catch (Exception exe) {
            }
            try {
                if (selectedWayPoint.getLongitude() == null) {
                    selectedWayPoint.setLongitude(new BigDecimal(this.getLongitude()));
                }
            } catch (Exception exe) {
            }

            WayPointService wayPointService = new WayPointService(sessionBean.getCompanyId());
            wayPointService.saveWayPoint(selectedWayPoint);

            selectedWayPoint = new WayPoint();

            this.refreshWayPoints();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onMarkerDrag(MarkerDragEvent event) {
        Marker mark = event.getMarker();
        for (int w = 0; w < this.wayPoints.size(); w++) {
            WayPoint wayPoint = wayPoints.get(w);
            try {
                if (wayPoint.getClientWaypointId().equals(mark.getData())) {
                    LatLng latLng = mark.getLatlng();
                    BigDecimal lat = new BigDecimal(latLng.getLat(), new MathContext(19, RoundingMode.HALF_DOWN));
                    BigDecimal lng = new BigDecimal(latLng.getLng(), new MathContext(19, RoundingMode.HALF_DOWN));
                    wayPoint.setLatitude(lat);
                    wayPoint.setLongitude(lng);
                    WayPointService wayPointService = new WayPointService(sessionBean.getCompanyId());
                    wayPointService.saveWayPoint(wayPoint, false);

                    refreshWayPoints();
                    w = this.wayPoints.size();
                }
            } catch (Exception exe) {
                exe.printStackTrace();
            }
        }
    }

    public void onStateChange(StateChangeEvent event) {
        LatLng center = event.getCenter();

        super.setLatitude(center.getLat());
        super.setLongitude(center.getLng());
    }

    public void delete(WayPoint delete) {
        try {
            delete.setActive(false);
            WayPointService wayPointService = new WayPointService(sessionBean.getCompanyId());
            wayPointService.saveWayPoint(delete);

            this.refreshWayPoints();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Simple refresh for all reports
     */
    public void refresh() {
    }

    public void onRowSelect(SelectEvent event) {
    }

    public void onRowSelectIncident(SelectEvent event) {
    }

    /**
     * @return the sessionBean
     */
    public ClientSessionBean getSessionBean() {
        return sessionBean;
    }

    /**
     * @param sessionBean the sessionBean to set
     */
    public void setSessionBean(ClientSessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public ArrayList<WayPoint> getNonGPSWayPoints() {
        ArrayList<WayPoint> retVal = new ArrayList<WayPoint>();
        for (int w = 0; w < wayPoints.size(); w++) {
            if (wayPoints.get(w).getLatitude() == null || wayPoints.get(w).getLatitude().equals(new BigDecimal(0))) {
                retVal.add(wayPoints.get(w));
            }
        }
        return retVal;
    }

    /**
     * @return the wayPoints
     */
    public ArrayList<WayPoint> getWayPoints() {
        return wayPoints;
    }

    /**
     * @param wayPoints the wayPoints to set
     */
    public void setWayPoints(ArrayList<WayPoint> wayPoints) {
        this.wayPoints = wayPoints;
    }

    /**
     * @return the selectedWayPoint
     */
    public WayPoint getSelectedWayPoint() {
        return selectedWayPoint;
    }

    public void deleteWayPoint() {
        WayPointService wayPointService = new WayPointService(sessionBean.getCompanyId());
        try {
            WayPoint wayPoint = wayPointService.getWayPointById(itemToDelete);
            wayPoint.setActive(false);
            wayPointService.saveWayPoint(wayPoint);

            this.refreshWayPoints();
        } catch (Exception e) {
        }
    }

    /**
     * @param selectedWayPoint the selectedWayPoint to set
     */
    public void setSelectedWayPoint(WayPoint selectedWayPoint) {
        if (selectedWayPoint != null) {
            this.selectedWayPoint = selectedWayPoint;
        }
    }

    public void setSelectedWayPointId(Integer wayPointId) {
        for (int w = 0; w < wayPoints.size(); w++) {
            if (wayPoints.get(w).getClientWaypointId().equals(wayPointId)) {
                this.selectedWayPoint = wayPoints.get(w);
            }
        }
    }

    /**
     * @return the wayPointScans
     */
    public ArrayList<ClientWaypointScan> getWayPointScans() {
        return wayPointScans;
    }

    /**
     * @param wayPointScans the wayPointScans to set
     */
    public void setWayPointScans(ArrayList<ClientWaypointScan> wayPointScans) {
        this.wayPointScans = wayPointScans;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the currentWayPointId
     */
    public Integer getCurrentWayPointId() {
        return currentWayPointId;
    }

    /**
     * @param currentWayPointId the currentWayPointId to set
     */
    public void setCurrentWayPointId(Integer currentWayPointId) {
        this.currentWayPointId = currentWayPointId;
    }

    /**
     * @return the itemToDelete
     */
    public Integer getItemToDelete() {
        return itemToDelete;
    }

    /**
     * @param itemToDelete the itemToDelete to set
     */
    public void setItemToDelete(Integer itemToDelete) {
        this.itemToDelete = itemToDelete;
    }

    /**
     * @return the polylineModel
     */
    public MapModel getPolylineModel() {
        return polylineModel;
    }

    /**
     * @param polylineModel the polylineModel to set
     */
    public void setPolylineModel(MapModel polylineModel) {
        this.polylineModel = polylineModel;
    }
}
