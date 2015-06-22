/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import com.patrolpro.beans.interfaces.RefreshClientListingInterface;
import com.google.gson.Gson;
import com.patrolpro.beans.interfaces.AbstractMappingBean;
import com.patrolpro.model.EmployeeSelected;
import com.patrolpro.model.LatLongObj;
import com.patrolpro.servlet.GPSCoordinatesServlet;
import com.patrolpro.servlet.utils.BarcodeMapGenerator;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.faces.component.html.HtmlInputText;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SlideEndEvent;
import org.primefaces.event.map.StateChangeEvent;
import org.primefaces.model.map.*;
import org.primefaces.model.map.Marker;
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
@Named("mapBean")
@ViewScoped
public class MappingBean extends AbstractMappingBean implements Serializable, RefreshClientListingInterface {

    private ArrayList<EmployeeSelected> employeeListedInMap;
    private MapModel polylineModel;
    private ArrayList<ClientEquipment> clientEquipment;
    private Integer selectedEquipment;
    private Integer numberOfSeconds = 60;
    private Boolean showAllPoints = false;
    private Integer zoom = new Integer(12);
    private Integer accuracyVal = 15;
    private Boolean showTracking;

    private ArrayList<String> timeLineSteps = new ArrayList<String>();
    private ArrayList<Marker> markers = new ArrayList<Marker>();

    @Inject
    private ClientSessionBean sessionBean;

    private Date selectedDate = new Date();
    private HtmlInputText dateInput;
    private String dateInputStr;

    private HashMap<Integer, LatLng> mapOfSelectedCenter;

    private ArrayList<String> employeeColors = new ArrayList<String>();

    public MappingBean() {
        showTracking = true;
    }

    @PostConstruct
    public void postConstruct() {
        try {
            ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", sessionBean.getLocale());
            String dateStyle = rb.getString("shortDateStyle");
            selectedDate = new Date();
            dateInputStr = new SimpleDateFormat(dateStyle).format(selectedDate);
        } catch (Exception exe) {
        }

        refreshMap(sessionBean);

        showTracking = true;
        polylineModel = BarcodeMapGenerator.refreshBarCodes(false, this.polylineModel, this.showTracking, this.getSessionBean());

        employeeColors.add("FFB300");
        employeeColors.add("803E75");
        employeeColors.add("FF6800");
        employeeColors.add("A6BDD7");
        employeeColors.add("C10020");
        employeeColors.add("CEA262");
        employeeColors.add("817066");
        employeeColors.add("007D34");
        employeeColors.add("F6768E");
        employeeColors.add("00538A");
        employeeColors.add("FF7A5C");
        employeeColors.add("53377A");
        employeeColors.add("FF8E00");
        employeeColors.add("B32851");
        employeeColors.add("F4C800");
        employeeColors.add("7F180D");
        employeeColors.add("93AA00");
        employeeColors.add("593315");
        employeeColors.add("F13A13");
        employeeColors.add("232C16");

        try {
            employeeListedInMap = new ArrayList<EmployeeSelected>();

            Calendar currentCal = Calendar.getInstance();
            currentCal.set(Calendar.HOUR_OF_DAY, 0);
            currentCal.set(Calendar.MINUTE, 0);

            SimpleDateFormat timeFormat = new SimpleDateFormat("HHmm");
            int dayOfMonth = currentCal.get(Calendar.DAY_OF_MONTH);
            while (dayOfMonth == currentCal.get(Calendar.DAY_OF_MONTH)) {
                timeLineSteps.add(timeFormat.format(currentCal.getTime()));
                currentCal.add(Calendar.MINUTE, GPSCoordinatesServlet.numberOfSeconds);
            }
            timeLineSteps.add("2400");
        } catch (Exception e) {
        }

        try {
            clientEquipment = new ArrayList<ClientEquipment>();
            //clientEquipment = equipmentService.getClientEquipmentByTypeAndId(2, sessionBean.getActiveClient().getClientId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onStateChange(StateChangeEvent event) {
        LatLng center = event.getCenter();

        mapOfSelectedCenter = new HashMap<Integer, LatLng>();
        if (selectedEquipment != null) {
            mapOfSelectedCenter.put(selectedEquipment, center);
            this.setLatitude(center.getLat());
            this.setLongitude(center.getLng());
        }
    }

    public void onSlideChange(SlideEndEvent event) {
        numberOfSeconds = event.getValue();

        this.refreshMap(sessionBean);
    }

    @Override
    public void refresh() {
        this.refreshMap(sessionBean);
    }

    public ArrayList<ClientEquipment> getClientEquipment() {
        return clientEquipment;
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

    public Integer getSelectedEquipment() {
        return this.selectedEquipment;
    }

    public void setSelectedEquipment(Integer clientEquipment) {
        this.selectedEquipment = clientEquipment;
    }

    /**
     * @return the numberOfSeconds
     */
    public Integer getNumberOfSeconds() {
        return numberOfSeconds;
    }

    /**
     * @param numberOfSeconds the numberOfSeconds to set
     */
    public void setNumberOfSeconds(Integer numberOfSeconds) {
        this.numberOfSeconds = numberOfSeconds;
    }

    /**
     * @return the selectedDate
     */
    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(String currentDateStr) {
        System.out.println(currentDateStr);
    }

    /**
     * @param selectedDate the selectedDate to set
     */
    public void setSelectedDate(Date currentDate) {
        this.selectedDate = currentDate;
    }

    /**
     * @return the timeLineSteps
     */
    public ArrayList<String> getTimeLineSteps() {
        return timeLineSteps;
    }

    /**
     * @param timeLineSteps the timeLineSteps to set
     */
    public void setTimeLineSteps(ArrayList<String> timeLineSteps) {
        this.timeLineSteps = timeLineSteps;
    }

    /**
     * @return the markers
     */
    public ArrayList<Marker> getMarkers() {
        return markers;
    }

    /**
     * @param markers the markers to set
     */
    public void setMarkers(ArrayList<Marker> markers) {
        this.markers = markers;
    }

    /**
     * @return the minuteStep
     */
    public Integer getMinuteStep() {
        return GPSCoordinatesServlet.numberOfSeconds;
    }

    /**
     * @param minuteStep the minuteStep to set
     */
    public void setMinuteStep(Integer minuteStep) {

    }

    /**
     * @return the showAllPoints
     */
    public Boolean getShowAllPoints() {
        return showAllPoints;
    }

    /**
     * @param showAllPoints the showAllPoints to set
     */
    public void setShowAllPoints(Boolean showAllPoints) {
        this.showAllPoints = showAllPoints;
    }

    /**
     * @return the zoom
     */
    public Integer getZoom() {
        return zoom;
    }

    /**
     * @param zoom the zoom to set
     */
    public void setZoom(Integer zoom) {
        this.zoom = zoom;
    }

    /**
     * @return the accuracyVal
     */
    public Integer getAccuracyVal() {
        return accuracyVal;
    }

    /**
     * @param accuracyVal the accuracyVal to set
     */
    public void setAccuracyVal(Integer accuracyVal) {
        this.accuracyVal = accuracyVal;
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

    public void generateGPSData() {
        TreeMap<Integer, TreeMap<Integer, ArrayList<LatLongObj>>> markers = new TreeMap<Integer, TreeMap<Integer, ArrayList<LatLongObj>>>();
        try {
            try {
                ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", sessionBean.getLocale());
                String dateStyle = rb.getString("shortDateStyle");
                selectedDate = new SimpleDateFormat(dateStyle).parse(this.dateInput.getValue().toString());
            } catch (Exception exe) {
            }

            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd");

            GPSService gpsService = new GPSService(sessionBean.getCompanyId());
            EmployeeService employeeService = new EmployeeService(sessionBean.getCompanyId());
            EquipmentService equipmentService = new EquipmentService(sessionBean.getCompanyId());
            CompanyService compService = new CompanyService();

            Calendar selectedStartDate = Calendar.getInstance();

            ArrayList<GPSCoordinate> gpsCoordinates = new ArrayList<GPSCoordinate>();
            ArrayList<WayPointCoordinate> wayPointCoordinates = new ArrayList<WayPointCoordinate>();
            HashMap<Integer, EmployeeSelected> employeeHash = new HashMap<Integer, EmployeeSelected>();
            HashMap<Integer, ClientEquipment> equipmentHash = new HashMap<Integer, ClientEquipment>();
            Integer clientId = 0;

            employeeListedInMap = new ArrayList<EmployeeSelected>();
            for (int c = 0; c < sessionBean.getClients().size(); c++) {
                Client currClient = sessionBean.getClients().get(c);
                try {
                    clientId = currClient.getClientId();
                    String timeZone = "US/Central";
                    try {
                        if (this.sessionBean.getSelectedBranchId() == null || this.sessionBean.getSelectedBranchId().equals(0)) {
                            ClientService clientService = new ClientService(sessionBean.getCompanyId());
                            Client myCli = clientService.getClientById(clientId);
                            timeZone = compService.getBranchById(myCli.getBranchId()).getTimezone();
                        } else {
                            timeZone = compService.getBranchById(this.sessionBean.getSelectedBranchId()).getTimezone();
                        }
                    } catch (Exception exe) {
                    }

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
                    for (int cli = 0; cli < clientEquipment.size(); cli++) {
                        clientIds.add(clientEquipment.get(cli).getClientEquipmentId());
                    }

                    gpsCoordinates = gpsService.getCoordinatesForDateForClientAndTimezone(dbFormat.format(selectedDate), dbFormat.format(selectedDate), clientId, timeZone);
                    wayPointCoordinates = gpsService.getWayPointsCoordinateByTimezoneStr(dbFormat.format(selectedStartDate.getTime()), dbFormat.format(selectedEndDate.getTime()), clientId, timeZone);
                } catch (Exception exe) {
                    exe.printStackTrace();
                }

                SimpleDateFormat myFormat = new SimpleDateFormat("HHmm");

                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                SimpleDateFormat compDBFormat = new SimpleDateFormat("yyyy-MM-dd");

                try {
                    String clientTimeZone = "US/Central";
                    dateFormat.setTimeZone(TimeZone.getTimeZone(clientTimeZone));
                    myFormat.setTimeZone(TimeZone.getTimeZone(clientTimeZone));
                    compDBFormat.setTimeZone(TimeZone.getTimeZone(clientTimeZone));
                } catch (Exception exe) {
                }

                Calendar timeOfLastEntry = Calendar.getInstance();
                Calendar timeOfCurrentEntry = Calendar.getInstance();

                Calendar myCal = Calendar.getInstance();
                myCal.setTime(selectedStartDate.getTime());
                myCal.add(Calendar.DAY_OF_MONTH, -1);

                int i = 0;

                String selectedDateStr = dbFormat.format(selectedDate);

                for (int g = 0; g < gpsCoordinates.size(); g++) {
                    try {
                        if (gpsCoordinates.get(g).getRecordedOn().after(myCal.getTime())) {
                            timeOfCurrentEntry.setTime(gpsCoordinates.get(g).getRecordedOn());

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
                                    Employee emp = employeeService.getEmployeeById(gps.getEmployeeId());
                                    EmployeeSelected selEmp = new EmployeeSelected();
                                    selEmp.setEmp(emp);
                                    selEmp.setEmployeeIcon("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|" + this.employeeColors.get(i));
                                    this.employeeListedInMap.add(selEmp);
                                    employeeHash.put(gps.getEmployeeId(), selEmp);
                                    i++;
                                }
                                EmployeeSelected selEmp = employeeHash.get(gps.getEmployeeId());
                                obj.setEmployeeName(selEmp.getEmp().getFullName());
                                obj.setImageUrl(selEmp.getEmp().getEmployeeImageUrl());
                                obj.setIconURL(selEmp.getEmployeeIcon());
                            } catch (Exception e) {
                                EmployeeSelected selEmp = new EmployeeSelected();
                                employeeHash.put(gps.getEmployeeId(), selEmp);
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
                            entryArea = entryArea - entryArea % 5;

                            //If things have wrapped from beginning of the day to end of previous day...
                            if (compDBFormat.format(gps.getRecordedOn()).equals(selectedDateStr)) {
                                if (markers.get(entryArea) == null) {
                                    markers.put(entryArea, new TreeMap<Integer, ArrayList<LatLongObj>>());
                                }
                                TreeMap<Integer, ArrayList<LatLongObj>> tempMarkers = markers.get(entryArea);
                                if (tempMarkers.get(gps.getEmployeeId()) == null) {
                                    tempMarkers.put(gps.getEmployeeId(), new ArrayList<LatLongObj>());
                                }

                                tempMarkers.get(gps.getEmployeeId()).add(obj);
                            }
                        }
                    } catch (Exception exe) {
                        exe.printStackTrace();
                    }

                }
                i = 0;
                for (int w = 0; w < wayPointCoordinates.size(); w++) {
                    WayPointCoordinate coord = wayPointCoordinates.get(w);

                    timeOfCurrentEntry.setTime(coord.getRecordedOn());

                    int temp = Integer.parseInt(myFormat.format(coord.getRecordedOn()));
                    if (temp == 2345) {
                        System.out.println("Here");
                    }
                    int entryArea = Integer.parseInt(myFormat.format(coord.getRecordedOn()));
                    entryArea = entryArea - entryArea % 5;

                    LatLongObj obj = new LatLongObj();
                    obj.setMarkerId(coord.getIdentifier());
                    obj.setIsMarker(true);
                    obj.setLat(coord.getLatitude().doubleValue());
                    obj.setLog(coord.getLongitude().doubleValue());
                    obj.setTime(dateFormat.format(coord.getRecordedOn()));
                    obj.setMarkerScanned(coord.getWayPointScan().getDateScanned());
                    obj.setAccuracy(0);
                    obj.setDeviceNickname("");
                    obj.setSpeed(0);
                    try {
                        if (employeeHash.get(coord.getEmployeeId()) == null) {
                            Employee emp = employeeService.getEmployeeById(coord.getEmployeeId());
                            EmployeeSelected selEmp = new EmployeeSelected();
                            selEmp.setEmp(emp);
                            selEmp.setEmployeeIcon("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|" + this.employeeColors.get(i));
                            this.employeeListedInMap.add(selEmp);
                            employeeHash.put(coord.getEmployeeId(), selEmp);
                            i++;
                        }
                        EmployeeSelected selEmp = employeeHash.get(coord.getEmployeeId());
                        obj.setEmployeeName(selEmp.getEmp().getFullName());
                        obj.setImageUrl(selEmp.getEmp().getEmployeeImageUrl());
                    } catch (Exception e) {
                        obj.setEmployeeName("");
                    }

                    if (compDBFormat.format(coord.getRecordedOn()).equals(selectedDateStr)) {
                        if (markers.get(entryArea) == null) {
                            markers.put(entryArea, new TreeMap<Integer, ArrayList<LatLongObj>>());
                        }
                        TreeMap<Integer, ArrayList<LatLongObj>> tempMarkers = markers.get(entryArea);
                        if (tempMarkers.get(coord.getEmployeeId()) == null) {
                            tempMarkers.put(coord.getEmployeeId(), new ArrayList<LatLongObj>());
                        }

                        tempMarkers.get(coord.getEmployeeId()).add(obj);
                    }
                }
            }
            Collections.sort(employeeListedInMap);

            Iterator<Integer> keys = markers.keySet().iterator();
            while (keys.hasNext()) {
                Integer key = keys.next();
                TreeMap<Integer, ArrayList<LatLongObj>> values = markers.get(key);
                Iterator<Integer> keyIterator = values.keySet().iterator();
                Integer currentEmpKey = keyIterator.next();
                ArrayList<LatLongObj> myKeys = values.get(currentEmpKey);
                double minValue = 10000.0;
                for (int k = myKeys.size() - 1; k >= 0; k--) {
                    LatLongObj currObj = myKeys.get(k);
                    if (currObj.getAccuracy() < minValue + 20.0) {
                        minValue = currObj.getAccuracy();
                    } else {
                        myKeys.remove(k);
                    }
                }

                try {
                    Collections.sort(myKeys);
                } catch (Exception exe) {
                }
            }

            RequestContext reqCtx = RequestContext.getCurrentInstance();

            Gson gson = new Gson();
            reqCtx.addCallbackParam("gpsData", gson.toJson(markers));
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    /**
     * @return the employeeListedInMap
     */
    public ArrayList<EmployeeSelected> getEmployeeListedInMap() {
        return employeeListedInMap;
    }

    /**
     * @param employeeListedInMap the employeeListedInMap to set
     */
    public void setEmployeeListedInMap(ArrayList<EmployeeSelected> employeeListedInMap) {
        this.employeeListedInMap = employeeListedInMap;
    }

    /**
     * @return the employeeColors
     */
    public ArrayList<String> getEmployeeColors() {
        return employeeColors;
    }

    /**
     * @param employeeColors the employeeColors to set
     */
    public void setEmployeeColors(ArrayList<String> employeeColors) {
        this.employeeColors = employeeColors;
    }

    /**
     * @return the showTracking
     */
    public Boolean isShowTracking() {
        return showTracking;
    }

    public Boolean getShowTracking() {
        return showTracking;
    }

    /**
     * @param showTracking the showTracking to set
     */
    public void setShowTracking(Boolean showTracking) {
        this.showTracking = showTracking;
    }

    /**
     * @return the dateInput
     */
    public HtmlInputText getDateInput() {
        return dateInput;
    }

    /**
     * @param dateInput the dateInput to set
     */
    public void setDateInput(HtmlInputText dateInput) {
        this.dateInput = dateInput;
    }

    /**
     * @return the dateInputStr
     */
    public String getDateInputStr() {
        return dateInputStr;
    }

    /**
     * @param dateInputStr the dateInputStr to set
     */
    public void setDateInputStr(String dateInputStr) {
        this.dateInputStr = dateInputStr;
    }
}
