/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import com.patrolpro.beans.interfaces.RefreshClientListingInterface;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;
import schedfoxlib.model.Employee;
import schedfoxlib.model.MobileFormData;
import schedfoxlib.model.MobileFormDataFillout;
import schedfoxlib.model.MobileFormDataSearch;
import schedfoxlib.model.MobileFormFillout;
import schedfoxlib.model.MobileForms;
import schedfoxlib.services.EmployeeService;
import schedfoxlib.services.MobileFormService;

/**
 *
 * @author ira
 */
@Named("formBean")
@ViewScoped
public class FormBean implements Serializable, RefreshClientListingInterface {

    @Inject
    private ClientSessionBean sessionBean;

    private Integer activeIndex;
    private Integer selectedFormFillout;
    private HashMap<Integer, HashMap<Integer, MobileFormDataFillout>> filledOutData;
    private HashMap<Integer, Employee> employees;
    private MobileFormFillout formFilloutToDelete;

    private ArrayList<MobileForms> loadedForms;
    private ConcurrentHashMap<Integer, ArrayList<MobileFormDataSearch>> loadedColumns;

    private Date dateSelected;

    private MobileForms currentForm;

    private Date startDate;
    private Date endDate;
    private SimpleDateFormat myFormat = new SimpleDateFormat("MM/dd/yyyy");

    public FormBean() {

    }

    public void updateFormControls() {
        try {
            String selectedFormId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedFormId");
            Integer selectedFormIdInt = Integer.parseInt(selectedFormId);
            for (int f = 0; f < loadedForms.size(); f++) {
                if (loadedForms.get(f).getMobileFormsId().equals(selectedFormIdInt)) {
                    this.currentForm = loadedForms.get(f);
                }
            }
        } catch (Exception exe) {}
    }

    public String getFormattedSDate() {
        try {
            return myFormat.format(startDate);
        } catch (Exception exe) {
            return "";
        }
    }

    public String getFormattedEDate() {
        try {
            return myFormat.format(endDate);
        } catch (Exception exe) {
            return "";
        }
    }

    @PostConstruct
    public void postConstruct() {
        try {
            Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            ResourceBundle rb = ResourceBundle.getBundle("PatrolPro", locale);
            
            myFormat = new SimpleDateFormat(rb.getString("shortDateStyle"));
            
            startDate = new Date();
            Calendar startCal = Calendar.getInstance();
            Calendar endCal = Calendar.getInstance();
            startCal.add(Calendar.DAY_OF_MONTH, -7);
            startDate = startCal.getTime();
            endDate = endCal.getTime();

            this.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refresh() {
        filledOutData = new HashMap<Integer, HashMap<Integer, MobileFormDataFillout>>();
        employees = new HashMap<Integer, Employee>();

        getForms();
    }

    public Employee getEmployeeById(Integer employeeId) {
        if (!employees.containsKey(employeeId)) {
            try {
                EmployeeService empService = new EmployeeService(sessionBean.getCompanyId());
                employees.put(employeeId, empService.getEmployeeById(employeeId));
            } catch (Exception exe) {
            }
        }
        try {
            return employees.get(employeeId);
        } catch (Exception exe) {
            return new Employee();
        }
    }

    public void dateChange(SelectEvent ae) {
        System.out.println("Hello... I am in DateChange");
    }

    public ArrayList<MobileForms> getForms() {
        try {
            if (loadedForms == null) {
                MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
                loadedForms = mobileService.getAllForms(sessionBean.getSelectedClient().getClientId());
            }
            if (loadedForms != null && loadedForms.size() > 0 && this.currentForm == null) {
                this.currentForm = loadedForms.get(0);
            }
        } catch (Exception exe) {
        }
        return loadedForms;
    }

    public boolean isJasperReport(Integer formId) {
        boolean retVal = false;
        for (int f = 0; f < loadedForms.size(); f++) {
            if (formId.equals(loadedForms.get(f).getMobileFormsId())) {
                MobileForms form = loadedForms.get(f);
                retVal = form.getReportData() != null && form.getReportData().length > 0;
            }
        }
        return retVal;
    }

    public ArrayList<MobileFormFillout> getFormFillout(Integer formId) {
        ArrayList<MobileFormFillout> retVal = new ArrayList<MobileFormFillout>();
        try {
            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            retVal = mobileService.getFormFilloutsForClient(sessionBean.getSelectedClient().getClientId(), formId, false);
        } catch (Exception exe) {
        }
        return retVal;

    }

    public MobileFormDataFillout getFormData(Integer mobileFormFilloutId, Integer mobileFormDataId) {
        if (filledOutData.get(mobileFormFilloutId) == null) {
            filledOutData.put(mobileFormFilloutId, new HashMap<Integer, MobileFormDataFillout>());
        }
        if (!filledOutData.get(mobileFormFilloutId).containsKey(mobileFormDataId)) {
            ArrayList<MobileFormDataFillout> data = getFormDataFillout(mobileFormFilloutId);
            for (int d = 0; d < data.size(); d++) {
                filledOutData.get(mobileFormFilloutId).put(data.get(d).getMobileFormDataId(), data.get(d));
            }
        }
        if (!filledOutData.get(mobileFormFilloutId).containsKey(mobileFormDataId)) {
            MobileFormDataFillout tempData = new MobileFormDataFillout();
            tempData.setMobileData("");
            return tempData;
        } else {
            return filledOutData.get(mobileFormFilloutId).get(mobileFormDataId);
        }
    }

    public ArrayList<MobileFormDataSearch> getFormData(Integer formId) {
        ArrayList<MobileFormDataSearch> retVal = new ArrayList<MobileFormDataSearch>();
        try {
            if (loadedColumns == null) {
                loadedColumns = new ConcurrentHashMap<Integer, ArrayList<MobileFormDataSearch>>();
            }
            if (loadedColumns.get(formId) == null) {
                MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
                loadedColumns.put(formId, mobileService.getFormSearchData(formId, sessionBean.getLocale().getLanguage(), false));
            }
            retVal = loadedColumns.get(formId);
        } catch (Exception exe) {
        }
        return retVal;
    }

    public ArrayList<MobileFormDataFillout> getFormDataFillout(Integer formFilloutId) {
        ArrayList<MobileFormDataFillout> retVal = new ArrayList<MobileFormDataFillout>();
        try {
            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            retVal = mobileService.getFormDataFillout(formFilloutId);
        } catch (Exception exe) {
        }
        return retVal;
    }

    public void filterDates() {
        System.out.println("here");
    }

    /**
     * @return the selectedFormFillout
     */
    public Integer getSelectedFormFillout() {
        return selectedFormFillout;
    }

    /**
     * @param selectedFormFillout the selectedFormFillout to set
     */
    public void setSelectedFormFillout(Integer selectedFormFillout) {
        this.selectedFormFillout = selectedFormFillout;
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

    public void deleteFormFillout() {
        try {
            MobileFormService mobileService = new MobileFormService(sessionBean.getCompanyId());
            formFilloutToDelete.setActive(false);
            mobileService.saveFormFillout(formFilloutToDelete);
        } catch (Exception exe) {
        }
    }

    /**
     * @return the formFilloutToDelete
     */
    public MobileFormFillout getFormFilloutToDelete() {
        return formFilloutToDelete;
    }

    /**
     * @param formFilloutToDelete the formFilloutToDelete to set
     */
    public void setFormFilloutToDelete(MobileFormFillout formFilloutToDelete) {
        this.formFilloutToDelete = formFilloutToDelete;
    }

    /**
     * @return the dateSelected
     */
    public Date getDateSelected() {
        return dateSelected;
    }

    /**
     * @param dateSelected the dateSelected to set
     */
    public void setDateSelected(Date dateSelected) {
        this.dateSelected = dateSelected;
    }

    /**
     * @return the activeIndex
     */
    public Integer getActiveIndex() {
        return activeIndex;
    }

    /**
     * @param activeIndex the activeIndex to set
     */
    public void setActiveIndex(Integer activeIndex) {
        this.activeIndex = activeIndex;
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
     * @return the currentForm
     */
    public MobileForms getCurrentForm() {
        return currentForm;
    }

    /**
     * @param currentForm the currentForm to set
     */
    public void setCurrentForm(MobileForms currentForm) {
        this.currentForm = currentForm;
    }

}
