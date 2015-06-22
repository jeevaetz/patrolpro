/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import com.patrolpro.beans.admin.SessionBean;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import schedfoxlib.model.AccessIndividualTypes;
import schedfoxlib.model.AccessIndividuals;
import schedfoxlib.services.AccessService;

/**
 *
 * @author ira
 */
@ManagedBean(name = "accessBean")
@ViewScoped
public class AccessBean implements Serializable {

    @Inject
    private SessionBean sessionBean;
    
    private AccessIndividuals selectedAccessIndividual;
    private ArrayList<AccessIndividualTypes> accessTypes;
    private ArrayList<AccessIndividuals> accessIndividuals;
    
    private boolean isPermanent = true;
    
    public AccessBean() {
        
    }
    
    @PostConstruct
    public void postConstruct() {
        selectedAccessIndividual = new AccessIndividuals();
        try {
            AccessService accessService = new AccessService(sessionBean.getCompanyId());
            accessTypes = accessService.getAccessTypes();
        } catch (Exception exe) {}
        refreshData();
    }

    public String getAccessTypeName(Integer accessTypeId) {
        String retVal = "";
        for (int a = 0; a < accessTypes.size(); a++) {
            if (accessTypes.get(a).getAccessIndividualTypeId().equals(accessTypeId)) {
                retVal = accessTypes.get(a).getAccessType();
            }
        }
        return retVal;
    }
    
    private void refreshData() {
        try {
            AccessService accessService = new AccessService(sessionBean.getCompanyId());
            accessIndividuals = accessService.getAccessIndividuals(sessionBean.getSelectedClientId());
        } catch (Exception exe) {}
        
    }
    
    public void addAccess() {
        selectedAccessIndividual = new AccessIndividuals();
    }
    
    public ArrayList<AccessIndividuals> getAccessIndividuals(Integer branchId) {
        try {
            AccessService accessService = new AccessService(sessionBean.getCompanyId());
            return accessService.getAccessIndividuals(branchId);
        } catch (Exception exe) {
            return new ArrayList<AccessIndividuals>();
        }
    }

    /**
     * @return the selectedAccessIndividual
     */
    public AccessIndividuals getSelectedAccessIndividual() {
        return selectedAccessIndividual;
    }

    /**
     * @param selectedAccessIndividual the selectedAccessIndividual to set
     */
    public void setSelectedAccessIndividual(AccessIndividuals selectedAccessIndividual) {
        if (selectedAccessIndividual != null) {
            this.selectedAccessIndividual = selectedAccessIndividual;
        }
    }

    /**
     * @return the accessTypes
     */
    public ArrayList<AccessIndividualTypes> getAccessTypes() {
        return accessTypes;
    }

    /**
     * @param accessTypes the accessTypes to set
     */
    public void setAccessTypes(ArrayList<AccessIndividualTypes> accessTypes) {
        this.accessTypes = accessTypes;
    }

    /**
     * @return the isPermanent
     */
    public boolean isIsPermanent() {
        return isPermanent;
    }

    /**
     * @param isPermanent the isPermanent to set
     */
    public void setIsPermanent(boolean isPermanent) {
        this.isPermanent = isPermanent;
    }
    
    public void save() {
        try {
            AccessService accessService = new AccessService(sessionBean.getCompanyId());
            selectedAccessIndividual.setClientId(sessionBean.getSelectedClientId());
            accessService.saveAccessIndividual(selectedAccessIndividual);
            refreshData();
        } catch (Exception exe) {}
    }

    /**
     * @param accessIndividuals the accessIndividuals to set
     */
    public void setAccessIndividuals(ArrayList<AccessIndividuals> accessIndividuals) {
        this.accessIndividuals = accessIndividuals;
    }

    /**
     * @return the accessIndividuals
     */
    public ArrayList<AccessIndividuals> getAccessIndividuals() {
        return accessIndividuals;
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
}
