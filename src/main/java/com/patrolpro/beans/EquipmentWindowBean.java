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
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import schedfoxlib.model.ClientEquipment;
import schedfoxlib.model.ClientEquipmentCommand;
import schedfoxlib.services.EquipmentService;

/**
 *
 * @author ira
 */
@Named("equipmentWindowBean")
@ViewScoped
public class EquipmentWindowBean implements Serializable {
    
    @Inject
    private SessionBean sessionBean;
    
    private String messageText;
    
    private ArrayList<ClientEquipment> clientEquipments;
    
    public EquipmentWindowBean() {
        
    }
    
    @PostConstruct
    public void postConstruct() {
        try {
            clientEquipments = new ArrayList<ClientEquipment>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void addEquipmentToWindow(ClientEquipment clientEquipment) {
        clientEquipments.add(clientEquipment);
    }

    /**
     * @return the clientEquipments
     */
    public ArrayList<ClientEquipment> getClientEquipments() {
        return clientEquipments;
    }

    /**
     * @param clientEquipments the clientEquipments to set
     */
    public void setClientEquipments(ArrayList<ClientEquipment> clientEquipments) {
        this.clientEquipments = clientEquipments;
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
     * @return the messageText
     */
    public String getMessageText() {
        return messageText;
    }

    /**
     * @param messageText the messageText to set
     */
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
    
    public void sendText(ClientEquipment clientEquipment) {
        ClientEquipmentCommand equipCommand = new ClientEquipmentCommand();
        equipCommand.setActive(true);
        equipCommand.setClientEquipmentId(clientEquipment.getClientEquipmentId());
        equipCommand.setCommand(ClientEquipmentCommand.Command.MSG_TO_DEVICE_FROM_CLIENT);
        equipCommand.setData(messageText);
        equipCommand.setDateSent(new Date());
        
        try {
            EquipmentService equipmentService = new EquipmentService(sessionBean.getCompanyId());
            equipmentService.saveClientEquipmentCommand(equipCommand);
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }
}
