/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans.admin;

import com.patrolpro.model.BranchSelected;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import schedfoxlib.model.AccessIndividualTypes;
import schedfoxlib.model.Branch;
import schedfoxlib.model.CompanyObj;
import schedfoxlib.model.IncidentReportType;
import schedfoxlib.model.User;
import schedfoxlib.services.AccessService;
import schedfoxlib.services.BranchService;
import schedfoxlib.services.CompanyService;
import schedfoxlib.services.IncidentService;
import schedfoxlib.services.UserService;

/**
 *
 * @author ira
 */
@Named("adminSettingsBean")
@javax.faces.view.ViewScoped
public class AdminSettingsBean implements Serializable {

    @Inject
    private SessionBean sessionBean;

    private ArrayList<IncidentReportType> reportTypes;
    private IncidentReportType reportType;

    private ArrayList<User> users;
    private User selectedUser;

    private ArrayList<AccessIndividualTypes> accessTypes;

    private ArrayList<BranchSelected> branches;
    private Branch selectedBranch;

    private String unencryptedPassword;

    private AccessIndividualTypes accessType;
    private ArrayList<String> timeZoneStrings;

    private String branchButtonValue;
    private boolean branchAllSelected;
    private ArrayList<Integer> selectedBranchList;

    private HtmlSelectBooleanCheckbox selectAllBranchChk;

    public AdminSettingsBean() {

    }

    @PostConstruct
    public void postConstruct() {
        try {
            this.setSelectedUser(new User());
            String[] timeZoneArray = TimeZone.getAvailableIDs();
            setTimeZoneStrings(new ArrayList<String>());
            for (int t = 0; t < timeZoneArray.length; t++) {
                getTimeZoneStrings().add(timeZoneArray[t]);
            }
            Collections.sort(timeZoneStrings);
        } catch (Exception exe) {
        }
        branchButtonValue = "Select All";
        setBranchAllSelected(false);
    }

    public void loadBranch() {
        try {
            String selectedBranchStr = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("branchId");
            Integer selectedBranchInt = Integer.parseInt(selectedBranchStr);
            CompanyService companyService = new CompanyService();
            setSelectedBranch(companyService.getBranchById(selectedBranchInt));
        } catch (Exception exe) {
        }
    }

    public void loadIncidentReport() {
        try {
            String selectedIncidentReport = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("incidentReportTypeId");
            Integer selectedIncidentReportInt = Integer.parseInt(selectedIncidentReport);
            IncidentService incidentService = new IncidentService(getSessionBean().getCompanyId());
            setReportType(incidentService.getIncidentReportTypeById(selectedIncidentReportInt));
        } catch (Exception exe) {
        }
    }

    public void loadUser() {
        try {
            String selectedUserStr = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedUserId");
            Integer selectedUserIdInt = Integer.parseInt(selectedUserStr);
            UserService userService = new UserService(getSessionBean().getCompanyId());
            setSelectedUser(userService.getUserById(selectedUserIdInt));

            ArrayList<Branch> selectedBranches = userService.getBranchesForUserAndCompany(this.selectedUser.getUserId(), Integer.parseInt(getSessionBean().getCompanyId()));
            for (int b = 0; b < branches.size(); b++) {
                branches.get(b).setAmISelected(false);
                for (int s = 0; s < selectedBranches.size(); s++) {
                    if (branches.get(b).getCurrBranch().getBranchId().equals(selectedBranches.get(s).getBranchId())) {
                        branches.get(b).setAmISelected(true);
                    }
                }
            }
            
            boolean allSelected = true;
            for (int b = 0; b < branches.size(); b++) {
                if (!branches.get(b).getAmISelected()) {
                    allSelected = false;
                }
            }
                this.branchAllSelected = allSelected;
        } catch (Exception exe) {
        }
    }

    public void onRowSelect(SelectEvent event) {
    }

    public String getReportTypeText() {
        try {
            return getReportType().getReportType();
        } catch (Exception exe) {
            return "";
        }
    }

    public void setReportTypeText(String text) {
        try {
            this.getReportType().setReportType(text);
        } catch (Exception exe) {
        }
    }

    public ArrayList<IncidentReportType> getReportTypes() {
        try {
            if (this.reportTypes == null) {
                IncidentService incidentService = new IncidentService(getSessionBean().getCompanyId());
                reportTypes = incidentService.getIncidentReportTypes();
            }
        } catch (Exception exe) {
            return new ArrayList<IncidentReportType>();
        }
        return reportTypes;
    }

    public ArrayList<BranchSelected> getBranchesForCompany() {
        try {
            if (branches == null) {
                CompanyService companyService = new CompanyService();
                branches = new ArrayList<BranchSelected>();
                ArrayList<Branch> currBranches = companyService.getBranchesForCompany(Integer.parseInt(getSessionBean().getCompanyId()));
                for (int b = 0; b < currBranches.size(); b++) {
                    BranchSelected currBranch = new BranchSelected();
                    currBranch.setAmISelected(false);
                    currBranch.setCurrBranch(currBranches.get(b));
                    branches.add(currBranch);
                }
            }
            return branches;
        } catch (Exception exe) {
            return new ArrayList<BranchSelected>();
        }
    }

    public void toggleBranch() {
        for (int b = 0; b < branches.size(); b++) {
            branches.get(b).setAmISelected(this.selectAllBranchChk.isSelected());
        }
    }

    public void removeSelectedType() {
        if (getReportType() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Choose an incident report type to delete!"));
        } else {
            IncidentService incidentService = new IncidentService(getSessionBean().getCompanyId());
            this.getReportType().setActive(false);
            try {
                incidentService.saveIncidentReportType(getReportType());
            } catch (Exception exe) {
            }
        }
    }

    public void saveSelectedType() {
        if (getReportType() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error on saving incident type!"));
        } else {
            IncidentService incidentService = new IncidentService(getSessionBean().getCompanyId());
            try {
                incidentService.saveIncidentReportType(getReportType());
                setReportType(null);
                this.setReportTypes(null);
            } catch (Exception exe) {
            }
        }
    }

    public void saveSelectedBranch() {
        if (getSelectedBranch() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error on saving branch!"));
        } else {
            BranchService branchService = new BranchService(getSessionBean().getCompanyId());
            try {
                if (getSelectedBranch().getBranchId() == null) {
                    CompanyService compService = new CompanyService();
                    CompanyObj comp = compService.getCompanyObjById(Integer.parseInt(getSessionBean().getCompanyId()));
                    getSelectedBranch().setBranchManagementId(comp.getCompanyManagementId().shortValue());
                }
                getSelectedBranch().setBranchInfo(null);
                branchService.saveBranch(getSelectedBranch());
                this.setSelectedBranch(new Branch());
                this.setBranches(null);
            } catch (Exception exe) {
            }
        }
    }

    public void saveAccessType() {
        if (getAccessType() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error on saving access type!"));
        } else {
            AccessService incidentService = new AccessService(getSessionBean().getCompanyId());
            try {
                incidentService.saveAccessTypes(getAccessType());
                setAccessType(null);
            } catch (Exception exe) {
            }
        }
    }

    /**
     * @return the reportType
     */
    public IncidentReportType getReportType() {
        return reportType;
    }

    /**
     * @param reportType the reportType to set
     */
    public void setReportType(IncidentReportType reportType) {
        this.reportType = reportType;
    }

    //Add Objects Begin
    public void addUser() {
        this.setSelectedUser(new User());
    }

    public void addBranch() {
        this.setSelectedBranch(new Branch());
        this.getSelectedBranch().setTimezone(getSelectedBranch().getTimezone());
    }

    public void addNewAccessType() {
        setAccessType(new AccessIndividualTypes());
    }

    public void addNewType() {
        setReportType(new IncidentReportType());
        getReportType().setActive(true);
    }
    //Add Objects End

    public void saveUser() {
        try {
            UserService usersService = new UserService(getSessionBean().getCompanyId());
            this.getSelectedUser().setUserMd5("");
            boolean isClearPassword = false;
            if (this.getUnencryptedPassword() != null && this.getUnencryptedPassword().length() > 0) {
                this.getSelectedUser().setUserPassword(getUnencryptedPassword());
                isClearPassword = true;
            }
            try {
                this.getSelectedUser().setUserManagementId(getSessionBean().getLoggedInUser().getUserManagementId());
            } catch (Exception exe) {
            }
            this.getSelectedUser().setUserId(usersService.saveUser(this.getSelectedUser(), isClearPassword));

            ArrayList<Integer> selectedBranchIds = new ArrayList<Integer>();
            for (int b = 0; b < branches.size(); b++) {
                if (branches.get(b).getAmISelected()) {
                    selectedBranchIds.add(branches.get(b).getCurrBranch().getBranchId());
                }
            }
            usersService.clearForCompanyAndSaveBranchInfoForUser(this.getSelectedUser().getUserId(), Integer.parseInt(getSessionBean().getCompanyId()), selectedBranchIds);
            
            setUsers(null);
        } catch (Exception exe) {
            exe.printStackTrace();
        }
        RequestContext.getCurrentInstance().execute("$('#UserModel').modal('hide')");
    }

    /**
     * @return the users
     */
    public ArrayList<User> getUsers() {
        if (users == null) {
            try {
                UserService usersService = new UserService(getSessionBean().getCompanyId());
                users = usersService.getUsersByCompany(Integer.parseInt(getSessionBean().getCompanyId()));
            } catch (Exception exe) {
                exe.printStackTrace();
            }
        }
        return users;
    }

    /**
     * @param users the users to set
     */
    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    /**
     * @return the selectedUser
     */
    public User getSelectedUser() {
        return selectedUser;
    }

    /**
     * @param selectedUser the selectedUser to set
     */
    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    /**
     * @return the unencryptedPassword
     */
    public String getUnencryptedPassword() {
        return unencryptedPassword;
    }

    /**
     * @param unencryptedPassword the unencryptedPassword to set
     */
    public void setUnencryptedPassword(String unencryptedPassword) {
        this.unencryptedPassword = unencryptedPassword;
    }

    /**
     * @return the accessTypes
     */
    public ArrayList<AccessIndividualTypes> getAccessTypes() {
        AccessService accessService = new AccessService(getSessionBean().getCompanyId());
        try {
            return accessService.getAccessTypes();
        } catch (Exception exe) {
            return new ArrayList<AccessIndividualTypes>();
        }
    }

    /**
     * @param accessTypes the accessTypes to set
     */
    public void setAccessTypes(ArrayList<AccessIndividualTypes> accessTypes) {
        this.accessTypes = accessTypes;
    }

    /**
     * @return the accessType
     */
    public AccessIndividualTypes getAccessType() {
        return accessType;
    }

    /**
     * @param accessType the accessType to set
     */
    public void setAccessType(AccessIndividualTypes accessType) {
        this.accessType = accessType;
    }

    /**
     * @return the selectedBranch
     */
    public Branch getSelectedBranch() {
        return selectedBranch;
    }

    /**
     * @param selectedBranch the selectedBranch to set
     */
    public void setSelectedBranch(Branch selectedBranch) {
        this.selectedBranch = selectedBranch;
    }

    /**
     * @param reportTypes the reportTypes to set
     */
    public void setReportTypes(ArrayList<IncidentReportType> reportTypes) {
        this.reportTypes = reportTypes;
    }

    /**
     * @return the timeZoneStrings
     */
    public ArrayList<String> getTimeZoneStrings() {
        return timeZoneStrings;
    }

    /**
     * @param timeZoneStrings the timeZoneStrings to set
     */
    public void setTimeZoneStrings(ArrayList<String> timeZoneStrings) {
        this.timeZoneStrings = timeZoneStrings;
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
     * @return the branches
     */
    public ArrayList<BranchSelected> getBranches() {
        return branches;
    }

    /**
     * @param branches the branches to set
     */
    public void setBranches(ArrayList<BranchSelected> branches) {
        this.branches = branches;
    }

    /**
     * @return the branchButtonValue
     */
    public String getBranchButtonValue() {
        return branchButtonValue;
    }

    /**
     * @param branchButtonValue the branchButtonValue to set
     */
    public void setBranchButtonValue(String branchButtonValue) {
        this.branchButtonValue = branchButtonValue;
    }

    /**
     * @return the branchAllSelected
     */
    public boolean isBranchAllSelected() {
        return branchAllSelected;
    }

    /**
     * @param branchAllSelected the branchAllSelected to set
     */
    public void setBranchAllSelected(boolean branchAllSelected) {
        this.branchAllSelected = branchAllSelected;
    }

    /**
     * @return the selectedBranchList
     */
    public ArrayList<Integer> getSelectedBranchList() {
        return selectedBranchList;
    }

    /**
     * @param selectedBranchList the selectedBranchList to set
     */
    public void setSelectedBranchList(ArrayList<Integer> selectedBranchList) {
        this.selectedBranchList = selectedBranchList;
    }

    /**
     * @return the selectAllBranchChk
     */
    public HtmlSelectBooleanCheckbox getSelectAllBranchChk() {
        return selectAllBranchChk;
    }

    /**
     * @param selectAllBranchChk the selectAllBranchChk to set
     */
    public void setSelectAllBranchChk(HtmlSelectBooleanCheckbox selectAllBranchChk) {
        this.selectAllBranchChk = selectAllBranchChk;
    }
}
