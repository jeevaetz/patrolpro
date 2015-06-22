/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans.admin;

import com.patrolpro.beans.interfaces.BranchRefreshInterface;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import schedfoxlib.model.Address;
import schedfoxlib.model.Client;
import schedfoxlib.model.ClientContact;
import schedfoxlib.model.ClientRoute;
import schedfoxlib.services.AddressService;
import schedfoxlib.services.ClientContractService;
import schedfoxlib.services.ClientService;

/**
 *
 * @author ira
 */
@Named("adminClientBean")
@ViewScoped
public class AdminClientBean implements Serializable, BranchRefreshInterface {

    @Inject
    private SessionBean sessionBean;

    private ArrayList<Client> clients;
    private Client selectedClient;
    private ArrayList<ClientRoute> selectedClientroutes;
    
    private ArrayList<ClientContact> contacts;
    private ClientContact editContact;
    private Boolean showDeleted;
    private Boolean showContactDeleted;
    private String companyId;

    private UIComponent clientNameTxt;
    private UIComponent clientAddress;
    private UIComponent clientAddress2;
    private UIComponent clientCity;
    private UIComponent clientState;
    private UIComponent clientZip;
    private UIComponent clientPhone;

    public AdminClientBean() {

    }

    @PostConstruct
    public void postConstruct() {
        try {
            this.showContactDeleted = false;
            showDeleted = false;
            this.companyId = sessionBean.getCompanyId();
            this.selectBranch();
        } catch (Exception exe) {
        }
    }

    public String selectClient(Client client) {
        ArrayList<Client> selClient = new ArrayList<Client>();
        selClient.add(client);
        sessionBean.setLoggedInclient(selClient);
        return "loggedin";
    }

    public void addNew() {
        selectedClient = new Client();
        try {
            contacts = null;
            selectedClient.setBranchId(sessionBean.getSelectedBranchId());
            selectedClient.setClientStartDate(new Date());
            selectedClient.setClientAddress2("");
            selectedClient.setDefaultNonBillable(false);
        } catch (Exception exe) {
            FacesContext.getCurrentInstance().addMessage("badBranch", new FacesMessage(exe.getMessage()));
        }
    }

    public ArrayList<ClientContact> getClientContacts() {
        if (contacts == null) {
            try {
                ClientContractService clientContractService = new ClientContractService(sessionBean.getCompanyId());
                contacts = clientContractService.getClientContracts(selectedClient.getClientId(), showContactDeleted);
            } catch (Exception exe) {}
        }
        return contacts;
    }

    public void setClientContacts(ArrayList<ClientContact> contacts) {
        this.contacts = contacts;
    }

    public void loadClientInfo() {
        try {
            String selectedClientId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedClientId");
            Integer selectedClientInt = Integer.parseInt(selectedClientId);
            for (int r = 0; r < clients.size(); r++) {
                if (clients.get(r).getClientId().equals(selectedClientInt)) {
                    selectedClient = clients.get(r);
                    selectedClient.setContacts(null);
                    contacts = null;
                }
            }
            ClientService clientService = new ClientService(sessionBean.getCompanyId());
            selectedClientroutes = clientService.getRoutesAssociatedWithClient(Integer.parseInt(selectedClientId));
        } catch (Exception exe) {
        }
    }

    public void selectBranch() {
        try {
            ClientService clientService = new ClientService(sessionBean.getCompanyId());
            clients = clientService.getClientsByBranchWithDeviceInfo(sessionBean.getSelectedBranchId(), this.showDeleted);
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    /**
     * @return the clients
     */
    public ArrayList<Client> getClients() {
        return clients;
    }

    /**
     * @param clients the clients to set
     */
    public void setClients(ArrayList<Client> clients) {
        this.clients = clients;
    }

    /**
     * @return the selectedClient
     */
    public Client getSelectedClient() {
        return selectedClient;
    }

    /**
     * @param selectedClient the selectedClient to set
     */
    public void setSelectedClient(Client selectedClient) {
        this.selectedClient = selectedClient;
    }

    public void addContact() {
        editContact = new ClientContact();
        RequestContext.getCurrentInstance().execute("$('#ContactModal').modal('show')");
    }

    public void saveClientContact() {
        ClientContractService contractService = new ClientContractService(sessionBean.getCompanyId());
        try {
            editContact.setClientId(this.selectedClient.getClientId());
            contractService.saveClientContact(editContact);
        } catch (Exception exe) {
            exe.printStackTrace();
        }
        contacts = null;
        RequestContext.getCurrentInstance().execute("$('#ContactModal').modal('hide')");
    }

    public void save() {
        boolean isValid = true;
        try {
            ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", sessionBean.getLocale());
            
            ClientService clientService = new ClientService(sessionBean.getCompanyId());
            FacesContext currContext = FacesContext.getCurrentInstance();
            if (selectedClient.getClientName() == null || selectedClient.getClientName().trim().length() == 0) {
                currContext.addMessage(clientNameTxt.getClientId(currContext), new FacesMessage(FacesMessage.SEVERITY_WARN, null, rb.getString("enterAName")));
                isValid = false;
            }
            if (selectedClient.getClientAddress() == null || selectedClient.getClientAddress().trim().length() == 0) {
                currContext.addMessage(clientAddress.getClientId(currContext), new FacesMessage(FacesMessage.SEVERITY_WARN, null, rb.getString("enterAAddress")));
                isValid = false;
            }
            if ((selectedClient.getClientCity() == null || selectedClient.getClientCity().trim().length() == 0) && clientCity.isRendered()) {
                currContext.addMessage(clientCity.getClientId(currContext), new FacesMessage(FacesMessage.SEVERITY_WARN, null, "Please enter a city!"));
                isValid = false;
            }
            if ((selectedClient.getClientState() == null || selectedClient.getClientState().trim().length() == 0) && clientState.isRendered()) {
                currContext.addMessage(clientState.getClientId(currContext), new FacesMessage(FacesMessage.SEVERITY_WARN, null, "Please enter a state!"));
                isValid = false;
            }
            if ((selectedClient.getClientZip() == null || selectedClient.getClientZip().trim().length() == 0) && clientZip.isRendered()) {
                currContext.addMessage(clientZip.getClientId(currContext), new FacesMessage(FacesMessage.SEVERITY_WARN, null, "Please enter a zip!"));
                isValid = false;
            }
            if (selectedClient.getClientPhone() == null || selectedClient.getClientPhone().trim().length() == 0) {
                currContext.addMessage(clientPhone.getClientId(currContext), new FacesMessage(FacesMessage.SEVERITY_WARN, null, rb.getString("enterAPhone")));
                isValid = false;
            }

            if (isValid) {
                clientService.saveClient(selectedClient);
                AddressService addressController = new AddressService(sessionBean.getCompanyId());
                boolean exists = addressController.checkIfAddressLatLongExists(selectedClient.getAddressObj());
                if (!exists) {
                    try {
                        Address tempAddress = addressController.fetchLatLong(selectedClient.getAddressObj());
                        selectedClient.getAddressObj().setLatitude(tempAddress.getLatitude());
                        selectedClient.getAddressObj().setLongitude(tempAddress.getLongitude());
                        addressController.persistAddressLatLong(selectedClient.getAddressObj());
                    } catch (Exception exe) {
                        exe.printStackTrace();
                    }
                }
            }
        } catch (Exception exe) {
            exe.printStackTrace();
        }
        if (isValid) {
            RequestContext.getCurrentInstance().execute("$('#ClientInfoModal').modal('hide')");
            selectBranch();
        }
    }

    /**
     * @return the showDeleted
     */
    public Boolean getShowDeleted() {
        return showDeleted;
    }

    /**
     * @param showDeleted the showDeleted to set
     */
    public void setShowDeleted(Boolean showDeleted) {
        this.showDeleted = showDeleted;
    }

    /**
     * @return the companyId
     */
    public String getCompanyId() {
        return companyId;
    }

    /**
     * @param companyId the companyId to set
     */
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    /**
     * @return the clientNameTxt
     */
    public UIComponent getClientNameTxt() {
        return clientNameTxt;
    }

    /**
     * @param clientNameTxt the clientNameTxt to set
     */
    public void setClientNameTxt(UIComponent clientNameTxt) {
        this.clientNameTxt = clientNameTxt;
    }

    /**
     * @return the clientAddress
     */
    public UIComponent getClientAddress() {
        return clientAddress;
    }

    /**
     * @param clientAddress the clientAddress1 to set
     */
    public void setClientAddress(UIComponent clientAddress) {
        this.clientAddress = clientAddress;
    }

    /**
     * @return the clientCity
     */
    public UIComponent getClientCity() {
        return clientCity;
    }

    /**
     * @param clientCity the clientCity to set
     */
    public void setClientCity(UIComponent clientCity) {
        this.clientCity = clientCity;
    }

    /**
     * @return the clientState
     */
    public UIComponent getClientState() {
        return clientState;
    }

    /**
     * @param clientState the clientState to set
     */
    public void setClientState(UIComponent clientState) {
        this.clientState = clientState;
    }

    /**
     * @return the clientZip
     */
    public UIComponent getClientZip() {
        return clientZip;
    }

    /**
     * @param clientZip the clientZip to set
     */
    public void setClientZip(UIComponent clientZip) {
        this.clientZip = clientZip;
    }

    /**
     * @return the clientPhone
     */
    public UIComponent getClientPhone() {
        return clientPhone;
    }

    /**
     * @param clientPhone the clientPhone to set
     */
    public void setClientPhone(UIComponent clientPhone) {
        this.clientPhone = clientPhone;
    }

    /**
     * @return the clientAddress2
     */
    public UIComponent getClientAddress2() {
        return clientAddress2;
    }

    /**
     * @param clientAddress2 the clientAddress2 to set
     */
    public void setClientAddress2(UIComponent clientAddress2) {
        this.clientAddress2 = clientAddress2;
    }

    /**
     * @return the editContact
     */
    public ClientContact getEditContact() {
        return editContact;
    }

    /**
     * @param editContact the editContact to set
     */
    public void setEditContact(ClientContact editContact) {
        this.editContact = editContact;
    }

    public void setSelectedContact(ClientContact editContact) {
        this.setEditContact(editContact);
        RequestContext.getCurrentInstance().execute("$('#ContactModal').modal('show')");
    }
    
    public void deleteClientContact() {
        ClientContractService contractService = new ClientContractService(sessionBean.getCompanyId());
        try {
            editContact.setClientContactIsDeleted((short)1);
            contractService.saveClientContact(editContact);
        } catch (Exception exe) {}
        try {
            editContact = null;
            contacts = contractService.getClientContracts(this.selectedClient.getClientId(), showDeleted);
            
            RequestContext.getCurrentInstance().execute("$('#ContactDeleteModal').modal('hide');");
        } catch (Exception exe) {}
    }

    /**
     * @return the showContactDeleted
     */
    public Boolean getShowContactDeleted() {
        return showContactDeleted;
    }

    /**
     * @param showContactDeleted the showContactDeleted to set
     */
    public void setShowContactDeleted(Boolean showContactDeleted) {
        this.showContactDeleted = showContactDeleted;
    }

    /**
     * @return the selectedClientroutes
     */
    public ArrayList<ClientRoute> getSelectedClientroutes() {
        return selectedClientroutes;
    }

    /**
     * @param selectedClientroutes the selectedClientroutes to set
     */
    public void setSelectedClientroutes(ArrayList<ClientRoute> selectedClientroutes) {
        this.selectedClientroutes = selectedClientroutes;
    }

}
