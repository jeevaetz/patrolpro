/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import com.patrolpro.beans.admin.SessionBean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;
import schedfoxlib.model.*;
import schedfoxlib.services.EquipmentService;
import schedfoxlib.services.GenericService;

/**
 *
 * @author user
 */
@Named("equipmentBean")
@ViewScoped
public class EquipmentBean implements Serializable {

    @Inject
    private SessionBean sessionBean;
    
    private ArrayList<ClientEquipment> clientEquipment;
    private ClientEquipment selectedEquipment;
    private SelectItem[] incidentTypes;
    
    private ClientEquipment messageEquipment;
    private String messageData;

    public EquipmentBean() {
        
    }
    
    @PostConstruct
    public void postConstruct() {
        try {
            
            clientEquipment = new ArrayList<ClientEquipment>();

            EquipmentService equipmentService = new EquipmentService(sessionBean.getCompanyId());
            clientEquipment = equipmentService.getClientEquipmentByTypeAndId(2, sessionBean.getSelectedClientId(), false);

            selectedEquipment = new ClientEquipment();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addEquipment() {
        selectedEquipment = new ClientEquipment();
    }
    
    public void sendMessage() {
        try {
            GenericService genericService = new GenericService();

            ClientMessaging clientMessaging = new ClientMessaging();
            clientMessaging.setActive(true);
            clientMessaging.setClientEquipmentId(messageEquipment.getClientEquipmentId());
            clientMessaging.setData(messageData);
            clientMessaging.setCommand(ClientMessaging.Command.MSG_TO_DEVICE_FROM_CLIENT);
            clientMessaging.setDateSent(new Date(genericService.getCurrentTimeMillis()));

            EquipmentService equipmentService = new EquipmentService(sessionBean.getCompanyId());
            equipmentService.saveClientEquipmentCommand(clientMessaging);
            
            messageData = "";
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }
    
    public ArrayList<ClientMessaging> getClientMessaging() {
        try {
            EquipmentService equipmentService = new EquipmentService(sessionBean.getCompanyId());
            return equipmentService.getClientMessaging(this.messageEquipment.getClientEquipmentId());
        } catch (Exception exe) {
            return new ArrayList<ClientMessaging>();
        }
    }
    
    public void save(ActionEvent actionEvent) {
        try {
            EquipmentService equipmentService = new EquipmentService(sessionBean.getCompanyId());
            if (selectedEquipment.getDateIssued() == null) {
                selectedEquipment.setDateIssued(new Date());
            }
            Client currentClient = sessionBean.getLoggedInclient().get(0);   
            selectedEquipment.setClientId(currentClient.getClientId());
            selectedEquipment.setEquipmentId(2);
            
            equipmentService.saveClientEquipment(selectedEquipment);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Simple refresh for all reports
     */
    public void refresh() {
    }

    public void message(ClientEquipment clientEquipment) {
        messageEquipment = clientEquipment;
    }
    
    public void onRowSelect(SelectEvent event) {
    }

    public void onRowSelectIncident(SelectEvent event) {
    }

    /**
     * @return the sessionBean
     */
    public SessionBean getSessionBean() {
        return sessionBean;
    }

    /**
     * @param sessionBean the sessionBean to set
     */
    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    /**
     * @return the mySessionBean
     */
    public SessionBean getMySessionBean() {
        return sessionBean;
    }

    /**
     * @param mySessionBean the mySessionBean to set
     */
    public void setMySessionBean(SessionBean mySessionBean) {
        this.sessionBean = mySessionBean;
    }

    /**
     * @return the incidentTypes
     */
    public SelectItem[] getIncidentTypes() {
        return incidentTypes;
    }

    /**
     * @param incidentTypes the incidentTypes to set
     */
    public void setIncidentTypes(SelectItem[] incidentTypes) {
        this.incidentTypes = incidentTypes;
    }

    /**
     * @return the selectedEquipment
     */
    public ClientEquipment getSelectedEquipment() {
        if (selectedEquipment == null) {
            selectedEquipment = new ClientEquipment();
        }
        return selectedEquipment;
    }

    /**
     * @param selectedEquipment the selectedEquipment to set
     */
    public void setSelectedEquipment(ClientEquipment selectedEquipment) {
        if (selectedEquipment != null) {
            this.selectedEquipment = selectedEquipment;
        }
    }

    /**
     * @return the clientEquipment
     */
    public ArrayList<ClientEquipment> getClientEquipment() {
        return clientEquipment;
    }

    /**
     * @param clientEquipment the clientEquipment to set
     */
    public void setClientEquipment(ArrayList<ClientEquipment> clientEquipment) {
        this.clientEquipment = clientEquipment;
    }

    /**
     * @return the messageEquipment
     */
    public ClientEquipment getMessageEquipment() {
        return messageEquipment;
    }

    /**
     * @param messageEquipment the messageEquipment to set
     */
    public void setMessageEquipment(ClientEquipment messageEquipment) {
        this.messageEquipment = messageEquipment;
    }

    /**
     * @return the messageData
     */
    public String getMessageData() {
        return messageData;
    }

    /**
     * @param messageData the messageData to set
     */
    public void setMessageData(String messageData) {
        this.messageData = messageData;
    }
}
