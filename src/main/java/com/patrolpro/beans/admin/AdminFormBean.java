/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlSelectOneListbox;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.component.selectonelistbox.SelectOneListbox;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import schedfoxlib.model.Client;
import schedfoxlib.model.MobileFormData;
import schedfoxlib.model.MobileFormDataType;
import schedfoxlib.model.MobileForms;
import schedfoxlib.model.MobileFormsType;
import schedfoxlib.services.ClientService;
import schedfoxlib.services.MobileFormService;

/**
 *
 * @author ira
 */
@Named("adminFormBean")
@ViewScoped
public class AdminFormBean implements Serializable {

    @Inject
    private SessionBean sessionBean;

    private ArrayList<Client> clients;
    private ArrayList<Integer> selectedClients;
    private ArrayList<MobileFormDataType> mobileFormDataType;
    private Integer selectedFormType;
    private MobileForms selectedForm;
    private Integer selectedFormDataType;

    private MobileFormData selectedFormData;

    private MobileForms newForm;
    private MobileFormData newData;
    private MobileFormData deleteData;

    private String selectedChoice;
    private String addChoice;

    private HashMap<Integer, ArrayList<MobileFormData>> mobileFormDataHash;

    public AdminFormBean() {

    }

    @PostConstruct
    public void postConstruct() {
        try {
            newForm = new MobileForms();
            newData = new MobileFormData();
            selectedForm = new MobileForms();
            selectedForm.setEntryType(1);

            mobileFormDataHash = new HashMap<Integer, ArrayList<MobileFormData>>();

            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            mobileFormDataType = mobileService.getFormDataTypes();
        } catch (Exception exe) {
        }
    }

    public void actionListener(ActionEvent event) {
        System.out.println("Here");
    }

    public void setupNewSelection() {
        this.addChoice = new String();
    }
    
    public void addSelection() {
        try {
            ArrayList<String> selData = newData.getSelectionData();
            selData.add(this.addChoice);
            newData.setSelectionData(selData);
            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            mobileService.saveFormData(newData);
            addChoice = "";
            RequestContext.getCurrentInstance().execute("$('#AddSelectionForm').modal('hide')");
        } catch (Exception exe) {
        }
    }
    
    public void deleteSelection() {
        try {
            ArrayList<String> selData = newData.getSelectionData();
            for (int i = 0; i < selData.size(); i++) {
                if (selData.get(i).equals(this.selectedChoice)) {
                    selData.remove(i);
                }
            }
            newData.setSelectionData(selData);
            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            mobileService.saveFormData(newData);
        } catch (Exception exe) {
        }
    }

    public void moveSelectionUp() {
        try {
            ArrayList<String> selData = newData.getSelectionData();
            for (int i = 1; i < selData.size(); i++) {
                if (selData.get(i).equals(this.selectedChoice)) {
                    String priorValue = selData.get(i - 1);
                    selData.set(i - 1, selectedChoice);
                    selData.set(i, priorValue);
                }
            }
            newData.setSelectionData(selData);
            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            mobileService.saveFormData(newData);
        } catch (Exception exe) {
        }
    }

    public void moveSelectionDown() {
        try {
            ArrayList<String> selData = newData.getSelectionData();
            for (int i = 0; i < selData.size() - 1; i++) {
                if (selData.get(i).equals(this.selectedChoice)) {
                    String priorValue = selData.get(i + 1);
                    selData.set(i + 1, selectedChoice);
                    selData.set(i, priorValue);
                    break;
                }
            }
            newData.setSelectionData(selData);
            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            mobileService.saveFormData(newData);
        } catch (Exception exe) {
        }
    }

    public void updateForm() {
        try {
            if (selectedForm != null && selectedForm.getMobileFormsId() != null) {
                MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
                mobileService.saveForm(selectedForm);
            }
        } catch (Exception exe) {
        }
    }

    public void saveClients() {
        try {
            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            mobileService.setClientsToMobileForm(selectedForm.getMobileFormsId(), this.selectedClients);
        } catch (Exception exe) {
            exe.printStackTrace();
        }
        RequestContext.getCurrentInstance().execute("$('#ClientSelectModal').modal('hide')");
    }
    
    public boolean isFormLocked() {
        try {
            return this.selectedForm.getReportData() != null;
        } catch (Exception exe) {
            return false;
        }
    }

    public boolean assignClient() {
        if (selectedForm == null || selectedForm.getMobileFormsId() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Client Selection", "Please select a form!"));
            return false;
        }
        try {
            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            selectedClients = mobileService.getClientIdsFormMobileId(selectedForm.getMobileFormsId());
        } catch (Exception exe) {
            exe.printStackTrace();
        }

        RequestContext.getCurrentInstance().execute("$('#ClientSelectModal').modal('show')");

        return true;
    }

    public void setClients(ArrayList<Client> clients) {
        this.clients = clients;
    }

    public ArrayList<Client> getClients() {
        try {
            if (clients == null) {
                ClientService clientService = new ClientService(sessionBean.getCompanyId());
                clients = clientService.getActiveClients();
            }
        } catch (Exception exe) {
        }
        return clients;
    }

    public ArrayList<MobileFormData> getFormData() {
        try {
            if (selectedForm == null || selectedForm.getMobileFormsId() == null) {
                return new ArrayList<MobileFormData>();
            } else if (mobileFormDataHash.get(selectedForm.getMobileFormsId()) == null) {
                MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
                ArrayList<MobileFormData> retVal = mobileService.getFormData(selectedForm.getMobileFormsId(), false);
                String[] reserved = MobileForms.getReservedIdentifiers();
                for (int r = retVal.size() - 1; r >= 0; r--) {
                    boolean hasReserved = false;
                    for (int res = 0; res < reserved.length; res++) {
                        if (reserved[res].equals(retVal.get(r).getDataLabel())) {
                            hasReserved = true;
                        }
                    }
                    if (hasReserved) {
                        retVal.remove(r);
                    }
                }
                mobileFormDataHash.put(selectedForm.getMobileFormsId(), retVal);
            }
            return mobileFormDataHash.get(selectedForm.getMobileFormsId());
        } catch (Exception exe) {
            return new ArrayList<MobileFormData>();
        }
    }

    public void newForm() {
        newForm = new MobileForms();
    }

    public void newMobileFormDataAction() {
        newData = new MobileFormData();
        RequestContext.getCurrentInstance().execute("$('#EditFormDataDialog').modal('show')");
    }

    public ArrayList<MobileFormDataType> getFormDataTypes() {
        try {
            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            return mobileService.getFormDataTypes();
        } catch (Exception exe) {
            return new ArrayList<MobileFormDataType>();
        }
    }

    public ArrayList<MobileForms> getForms() {
        try {
            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            return mobileService.getAllFormsForCompany();
        } catch (Exception exe) {
            return new ArrayList<MobileForms>();
        }
    }

    public void deleteFormData() {
        try {
            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            deleteData.setActive(false);
            mobileService.saveFormData(deleteData);
            mobileFormDataHash.remove(deleteData.getMobileFormsId());
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    public void moveUp(MobileFormData formData) {
        try {
            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            ArrayList<MobileFormData> unOrderedData = mobileService.getFormData(formData.getMobileFormsId(), false);
            for (int u = 1; u < unOrderedData.size(); u++) {
                if (unOrderedData.get(u).getMobileFormDataId().equals(formData.getMobileFormDataId())) {
                    MobileFormData replace = unOrderedData.get(u - 1);
                    unOrderedData.set(u - 1, formData);
                    unOrderedData.set(u, replace);
                    u = unOrderedData.size();
                }
            }
            Integer[] orderedData = new Integer[unOrderedData.size()];
            for (int u = 0; u < unOrderedData.size(); u++) {
                orderedData[u] = unOrderedData.get(u).getMobileFormDataId();
            }

            mobileService.saveOrdering(orderedData);
            this.mobileFormDataHash.remove(formData.getMobileFormsId());
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    public void moveDown(MobileFormData formData) {
        try {
            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            ArrayList<MobileFormData> unOrderedData = mobileService.getFormData(formData.getMobileFormsId(), false);
            for (int u = 0; u < unOrderedData.size() - 1; u++) {
                if (unOrderedData.get(u).getMobileFormDataId().equals(formData.getMobileFormDataId())) {
                    MobileFormData replace = unOrderedData.get(u + 1);
                    unOrderedData.set(u + 1, formData);
                    unOrderedData.set(u, replace);
                    u = unOrderedData.size();
                }
            }
            Integer[] orderedData = new Integer[unOrderedData.size()];
            for (int u = 0; u < unOrderedData.size(); u++) {
                orderedData[u] = unOrderedData.get(u).getMobileFormDataId();
            }

            mobileService.saveOrdering(orderedData);
            this.mobileFormDataHash.remove(formData.getMobileFormsId());
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    public void setFormDataToDelete(MobileFormData formData) {
        deleteData = formData;
    }

    public void saveData() {
        try {
            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            newData.setActive(true);
            newData.setMobileFormsId(this.selectedForm.getMobileFormsId());
            mobileService.saveFormData(newData);
            newData = new MobileFormData();
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    public void saveForm() {
        try {
            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            newForm.setActive(true);
            newForm.setClientId(0);
            mobileService.saveForm(newForm);
            RequestContext.getCurrentInstance().execute("$('#createFormDialog').modal('hide')");
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    public ArrayList<MobileFormsType> getFormsType() {
        try {
            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            return mobileService.getFormsType();
        } catch (Exception exe) {
            return new ArrayList<MobileFormsType>();
        }
    }

    public String getFormDataTypeName(Integer type) {
        if (type == 1) {
            return "Access";
        }
        return "";
    }

    public String getFormTypeName(Integer type) {
        for (int t = 0; t < mobileFormDataType.size(); t++) {
            MobileFormDataType currType = mobileFormDataType.get(t);
            if (type.equals(currType.getMobileFormDataTypeId())) {
                return currType.getMobileFormDataTypeName();
            }
        }
        return "Time";
    }

    public void onFormSelect(AjaxBehaviorEvent event) {
        Integer selValue = Integer.parseInt((String) ((SelectOneListbox) event.getComponent()).getValue());
        MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
        try {
            selectedForm = mobileService.getForm(selValue);
            sessionBean.setAdminSelectedFormId(this.selectedForm.getMobileFormsId());
        } catch (Exception exe) {
        }
    }

    public void onRowSelect(SelectEvent event) {
        this.newData = this.selectedFormData;
    }

    /**
     * @return the selectedFormType
     */
    public Integer getSelectedFormType() {
        return selectedFormType;
    }

    /**
     * @param selectedFormType the selectedFormType to set
     */
    public void setSelectedFormType(Integer selectedFormType) {
        this.selectedFormType = selectedFormType;
    }

    /**
     * @return the newForm
     */
    public MobileForms getNewForm() {
        return newForm;
    }

    /**
     * @param newForm the newForm to set
     */
    public void setNewForm(MobileForms newForm) {
        this.newForm = newForm;
    }

    /**
     * @return the selectedForm
     */
    public MobileForms getSelectedForm() {
        return selectedForm;
    }

    /**
     * @param selectedForm the selectedForm to set
     */
    public void setSelectedForm(MobileForms selectedForm) {
        this.selectedForm = selectedForm;
    }

    /**
     * @return the selectedFormDataType
     */
    public Integer getSelectedFormDataType() {
        return selectedFormDataType;
    }

    /**
     * @param selectedFormDataType the selectedFormDataType to set
     */
    public void setSelectedFormDataType(Integer selectedFormDataType) {
        this.selectedFormDataType = selectedFormDataType;
    }

    /**
     * @return the newData
     */
    public MobileFormData getNewData() {
        return newData;
    }

    /**
     * @param newData the newData to set
     */
    public void setNewData(MobileFormData newData) {
        this.newData = newData;
    }

    /**
     * @return the selectedFormData
     */
    public MobileFormData getSelectedFormData() {
        return selectedFormData;
    }

    /**
     * @param selectedFormData the selectedFormData to set
     */
    public void setSelectedFormData() {
        try {
            String selectedMobileData = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedFormDataId");
            Integer selectedMobileDataInt = Integer.parseInt(selectedMobileData);
            ArrayList<MobileFormData> myData = this.mobileFormDataHash.get(this.getSelectedForm().getMobileFormsId());
            for (int i = 0; i < myData.size(); i++) {
                if (myData.get(i).getMobileFormDataId().equals(selectedMobileDataInt)) {
                    this.newData = myData.get(i);
                }
            }
        } catch (Exception exe) {
        }
    }

    /**
     * @return the selectedClients
     */
    public ArrayList<Integer> getSelectedClients() {
        if (selectedClients == null) {
            selectedClients = new ArrayList<Integer>();
        }
        return selectedClients;
    }

    /**
     * @param selectedClients the selectedClients to set
     */
    public void setSelectedClients(ArrayList<Integer> selectedClients) {
        this.selectedClients = selectedClients;
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
     * @return the selectedChoice
     */
    public String getSelectedChoice() {
        return selectedChoice;
    }

    /**
     * @param selectedChoice the selectedChoice to set
     */
    public void setSelectedChoice(String selectedChoice) {
        this.selectedChoice = selectedChoice;
    }

    /**
     * @return the addChoice
     */
    public String getAddChoice() {
        return addChoice;
    }

    /**
     * @param addChoice the addChoice to set
     */
    public void setAddChoice(String addChoice) {
        this.addChoice = addChoice;
    }
}
