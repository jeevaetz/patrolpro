/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.patrolpro.model.PolygonGPS;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.MapModel;
import schedfoxlib.model.Client;
import schedfoxlib.model.GeoFencing;
import schedfoxlib.model.GeoFencingContact;
import schedfoxlib.model.GeoFencingPoints;
import schedfoxlib.services.GPSService;
import schedfoxlib.services.SchedfoxLibServiceVariables;

/**
 *
 * @author ira
 */
@Named("polygonBean")
@ViewScoped
public class PolygonBean implements Serializable {

    @Inject
    private ClientSessionBean sessionBean;

    private Double latitude = new Double(32.46);
    private Double longitude = new Double(96.46);
    private MapModel polylineModel;

    private GeoFencingContact newContact;
    private Integer deleteContactId;

    private ArrayList<GeoFencing> geoFencing;
    private GeoFencing selectedGeoFence;
    private Integer geoFenceId;

    public PolygonBean() {

    }

    @PostConstruct
    public void postConstruct() {
        try {
            polylineModel = new DefaultMapModel();
            newContact = new GeoFencingContact();

            refreshMap();

            this.fetchGeoFence();
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }
    
    public void setContactToDelete(Integer contactToDelete) {
        this.deleteContactId = contactToDelete;
        RequestContext.getCurrentInstance().execute("$('#GeoFenceContactDeleteModal').modal('show')");
    }

    public void setSelectedGeoFence() {
        try {
            String selectedGeoFenceId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("geoFenceId");
            Integer geoFenceId = Integer.parseInt(selectedGeoFenceId);
            for (int g = 0; g < geoFencing.size(); g++) {
                if (geoFencing.get(g).getGeoFencingId().equals(geoFenceId)) {
                    selectedGeoFence = geoFencing.get(g);
                }
            }
        } catch (Exception exe) {}
    }
    
    public void refreshSelectedGeoFenceContacts() {
        try {
            GPSService gpsService = new GPSService(sessionBean.getCompanyId());
            ArrayList<GeoFencingContact> geoContacts = gpsService.getGeoFencingContacts(selectedGeoFence.getGeoFencingId());
            selectedGeoFence.setContacts(geoContacts);
        } catch (Exception exe) {}
    }

    public void removeGeoFenceContact(GeoFencingContact contact) {
        contact.setActive(false);
        try {
            GPSService gpsService = new GPSService(sessionBean.getCompanyId());
            gpsService.saveGeoContact(contact);
            refreshSelectedGeoFenceContacts();
        } catch (Exception exe) {
        }
    }
    
    public void removeGeoFenceContactById() {
        try {
            for (int c = 0; c < this.selectedGeoFence.getContacts().size(); c++) {
                GeoFencingContact contact = this.selectedGeoFence.getContacts().get(c);
                if (contact.getGeoFencingContactId().equals(this.deleteContactId)) {
                    this.removeGeoFenceContact(contact);
                }
            }
        } catch (Exception exe) {}
    }

    private void fetchGeoFence() throws Exception {
        Integer clientId = sessionBean.getSelectedClient().getClientId();
        GPSService gpsService = new GPSService(sessionBean.getCompanyId());
        geoFencing = gpsService.getGeoFencing(clientId);

        for (int g = 0; g < geoFencing.size(); g++) {
            GeoFencing geoFence = geoFencing.get(g);
            ArrayList<GeoFencingContact> geoContacts = gpsService.getGeoFencingContacts(geoFence.getGeoFencingId());
            geoFence.setContacts(geoContacts);
        }
    }

    public void refreshMap() {
        try {
            double latitude = 0.0;
            double longitude = 0.0;
            int numberOfValidEntries = 0;
            for (int c = 0; c < sessionBean.getClients().size(); c++) {
                Client currClient = sessionBean.getClients().get(c);
                try {
                    if (currClient.getAddressObj().getLatitude() > -1) {
                        latitude += currClient.getAddressObj().getLatitude();
                        longitude += currClient.getAddressObj().getLongitude();
                        numberOfValidEntries++;
                    }
                } catch (Exception e) {
                }
            }
            this.latitude = latitude / numberOfValidEntries;
            this.longitude = longitude / numberOfValidEntries;
        } catch (Exception e) {
        }

    }

    public void addGeoFenceContact() {
        newContact.setActive(true);
        newContact.setContactType(1);
        newContact.setGeoFenceId(this.selectedGeoFence.getGeoFencingId());

        try {
            GPSService gpsService = new GPSService(sessionBean.getCompanyId());
            gpsService.saveGeoContact(newContact);
            
            newContact = new GeoFencingContact();
            refreshSelectedGeoFenceContacts();
        } catch (Exception exe) {
        }
    }

    public GeoFencing getGeoFenceToDelete() {
        try {
            for (int i = 0; i < geoFencing.size(); i++) {
                if (geoFencing.get(i).getGeoFencingId().equals(this.geoFenceId)) {
                    return geoFencing.get(i);
                }
            }
        } catch (Exception exe) {
        }
        return null;
    }

    public void setItemToDelete(Integer geoFenceId) {
        this.geoFenceId = geoFenceId;
        RequestContext.getCurrentInstance().execute("$('#GeoFenceDeleteModal').modal('show')");

    }

    public void deleteGeoFence() {
        try {
            GeoFencing fence = getGeoFenceToDelete();
            fence.setActive(false);
            GPSService gpsService = new GPSService(sessionBean.getCompanyId());
            gpsService.saveGeoFence(fence);

            geoFencing = gpsService.getGeoFencing(sessionBean.getSelectedClient().getClientId());
        } catch (Exception exe) {
        } finally {
            RequestContext.getCurrentInstance().execute("$('#GeoFenceDeleteModal').modal('hide')");
        }
    }

    public void polygonCompleted() {
        Gson gson = SchedfoxLibServiceVariables.getGson();
        Type collectionType = new TypeToken<Collection<PolygonGPS>>() {
        }.getType();

        Map<String, String> requestParamMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        Object pathData = requestParamMap.get("pathData");

        GeoFencing fence = new GeoFencing();
        fence.setGeoFenceAddedBy(sessionBean.getLoggedInUser().getUserId());
        fence.setGeoFenceName("fence");
        fence.setClientId(sessionBean.getSelectedClient().getClientId());
        fence.setActive(true);
        fence.setGeoFenceType(1);

        try {
            ArrayList<PolygonGPS> polygons = gson.fromJson(pathData.toString(), collectionType);

            GPSService gpsService = new GPSService(sessionBean.getCompanyId());
            Integer fenceId = gpsService.saveGeoFence(fence);
            fence.setGeoFencingId(fenceId);

            for (int p = 0; p < polygons.size(); p++) {
                PolygonGPS polygon = polygons.get(p);
                GeoFencingPoints point = new GeoFencingPoints();
                point.setGeoFencingId(fenceId);
                point.setLatitude(polygon.getLatitude().doubleValue());
                point.setLongitude(polygon.getLongitude().doubleValue());
                point.setPointCount(polygon.getPointCount());

                gpsService.saveGeoFencePoint(point);
            }
            fetchGeoFence();
        } catch (Exception exe) {
        }
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

    /**
     * @return the currentLat
     */
    public Double getCurrentLat() {
        return latitude;
    }

    /**
     * @param currentLat the currentLat to set
     */
    public void setCurrentLat(Double currentLat) {
        this.latitude = currentLat;
    }

    /**
     * @return the currentLng
     */
    public Double getCurrentLng() {
        return longitude;
    }

    /**
     * @param currentLng the currentLng to set
     */
    public void setCurrentLng(Double currentLng) {
        this.longitude = currentLng;
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

    /**
     * @return the geoFencing
     */
    public ArrayList<GeoFencing> getGeoFencing() {
        return geoFencing;
    }

    /**
     * @param geoFencing the geoFencing to set
     */
    public void setGeoFencing(ArrayList<GeoFencing> geoFencing) {
        this.geoFencing = geoFencing;
    }

    /**
     * @return the geoFenceId
     */
    public Integer getGeoFenceId() {
        return geoFenceId;
    }

    /**
     * @param geoFenceId the geoFenceId to set
     */
    public void setGeoFenceId(Integer geoFenceId) {
        if (geoFenceId != null) {
            this.geoFenceId = geoFenceId;
        }
    }

    /**
     * @return the selectedGeoFence
     */
    public GeoFencing getSelectedGeoFence() {
        if (this.selectedGeoFence == null) {
            selectedGeoFence = new GeoFencing();
        }
        return selectedGeoFence;
    }

    /**
     * @param selectedGeoFence the selectedGeoFence to set
     */
    public void setSelectedGeoFence(GeoFencing selectedGeoFence) {
        this.selectedGeoFence = selectedGeoFence;
    }

    public void save() {
        try {
            GPSService gpsService = new GPSService(sessionBean.getCompanyId());
            gpsService.saveGeoFence(selectedGeoFence);
        } catch (Exception e) {
        } finally {
            RequestContext.getCurrentInstance().execute("$('#GeoFenceModal').modal('hide')");
        }
    }

    /**
     * @return the newContact
     */
    public GeoFencingContact getNewContact() {
        return newContact;
    }

    /**
     * @param newContact the newContact to set
     */
    public void setNewContact(GeoFencingContact newContact) {
        this.newContact = newContact;
    }

    /**
     * @return the deleteContactId
     */
    public Integer getDeleteContactId() {
        return deleteContactId;
    }

    /**
     * @param deleteContactId the deleteContactId to set
     */
    public void setDeleteContactId(Integer deleteContactId) {
        this.deleteContactId = deleteContactId;
    }
}
