/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans.admin;

import com.patrolpro.beans.interfaces.BranchRefreshInterface;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.annotation.PostConstruct;
import javax.ejb.Remove;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.WindowContext;
import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.WindowScoped;
import org.primefaces.event.SelectEvent;
import schedfoxlib.model.Branch;
import schedfoxlib.model.Client;
import schedfoxlib.model.ClientRoute;
import schedfoxlib.model.Company;
import schedfoxlib.model.User;
import schedfoxlib.services.ClientService;
import schedfoxlib.services.CompanyService;
import schedfoxlib.services.UserService;

/**
 *
 * @author user
 */
@Named("sessionBean")
@WindowScoped
public class SessionBean implements Serializable {

    private Locale locale = null;

    private ArrayList<Client> loggedInclients;
    private User loggedInUser;
    private Company selectedCompany;
    private Branch selectedBranch;
    private Integer selectedBranchId;
    private Client selectedClient;
    private ArrayList<Company> accessibleCompanies;
    private ArrayList<Branch> accessibleBranches;
    private HashMap<Integer, ArrayList<Client>> cachedClients;
    private ArrayList<ClientRoute> routes;
    private ArrayList<Client> clients;
    private Boolean isLoggedInAsAdmin;
    private Integer selRoute;
    private Integer selClientFromRoute;
    private String referral;

    private Integer adminSelectedEmployeeId;
    private Integer adminSelectedFormId;

    private String companyId = "2";

    @Inject
    private WindowContext conversation;

    /**
     * Creates a new instance of SessionBean
     */
    public SessionBean() {
        cachedClients = new HashMap<Integer, ArrayList<Client>>();

        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        startCal.set(Calendar.DAY_OF_MONTH, 1);
        endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
    }

    @PostConstruct
    public void postConstruct() {
        isLoggedInAsAdmin = false;
//        if (conversation.isTransient()) {
//            try {
//                conversation.end();
//            } catch (Exception exe) {
//            }
//            conversation.begin();
//            conversation.setTimeout(100000000);
//        }
    }
    
    public String getLogoURL() {
        try {
            if (this.referral.equals("skytrack")) {
                return "logo_lt.png";
            }
        } catch (Exception exe) {}
        return "logo.png";
    }

    public static void reloadBundle() {
        ResourceBundle.clearCache(Thread.currentThread().getContextClassLoader());
    }

    public boolean showOverviewAnalyticsTab() {
        boolean retVal = false;

        if (this.getLoggedInUser().getUserId() == 2294 || this.getLoggedInUser().getUserId() == 2808) {
            retVal = true;
        }

        return retVal;
    }

    public boolean showLanguageApplicationLink() {
        boolean retVal = false;

        if (this.getLoggedInUser().getUserId() == 2294 || this.companyId.equals("5092")) {
            retVal = true;
        }

        return retVal;
    }

    @Remove
    public void destroy() {
        System.out.println("Destroy was called!!!!!!!!!!!!!!!!!");
    }

    public boolean hasMultipleClients() {
        try {
            return getLoggedInclient().size() > 1;
        } catch (Exception exe) {
            return false;
        }
    }

    public void confirm() {
//        if (conversation.isTransient()) {
//            conversation.end();
//        }
    }

    public void dateChange(SelectEvent ae) {
        System.out.println("Hello... I am in DateChange");
    }

    public String logOut() {
        try {
//            this.conversation.end();
            this.conversation.close();
        } catch (Exception e) {
        } finally {
            return "loggedOut";
        }
    }

    /**
     * @return the loggedInclient
     */
    public ArrayList<Client> getLoggedInclient() {
        return loggedInclients;
    }

    /**
     * @param loggedInclient the loggedInclient to set
     */
    public void setLoggedInclient(ArrayList<Client> loggedInclient) {
        this.loggedInclients = loggedInclient;
    }

    public Client getActiveClient() {
        if (this.loggedInclients != null && this.loggedInclients.size() > 0) {
            return this.loggedInclients.get(0);
        } else {
            return null;
        }
    }

    public void setSelectedBranch(BranchRefreshInterface bInterface, Integer branchId) {
        this.setSelectedBranchId(branchId);
        if (bInterface != null) {
            bInterface.selectBranch();
        }
    }

    public Integer getSelectedClientId() {
        try {
            return this.loggedInclients.get(0).getClientId();
        } catch (Exception exe) {
        }
        return 0;
    }

    /**
     * @return the loggedInUser
     */
    public User getLoggedInUser() {
        return loggedInUser;
    }

    /**
     * @param loggedInUser the loggedInUser to set
     */
    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    /**
     * @return the accessibleCompanies
     */
    public ArrayList<Company> getAccessibleCompanies() {
        if (loggedInUser != null && accessibleCompanies == null) {
            UserService userService = new UserService();
            try {
                accessibleCompanies = userService.getCompaniesForUser(loggedInUser.getId());
                selectedCompany = accessibleCompanies.get(0);
            } catch (Exception exe) {
                exe.printStackTrace();
            }
        }
        return accessibleCompanies;
    }

    public ArrayList<Branch> getAccessibleBranches() {
        if (selectedCompany == null) {
            getAccessibleCompanies();
        }
        if (selectedCompany != null) {
            Vector<Branch> branchesV = selectedCompany.getBranches();
            ArrayList<Branch> retVal = new ArrayList<Branch>();
            retVal.addAll(branchesV);
            if (selectedBranch == null && retVal.size() > 0) {
                for (int b = 0; b < retVal.size(); b++) {
                    if (retVal.get(b).getDefaultBranch()) {
                        selectedBranch = retVal.get(b);
                        selectedBranchId = retVal.get(b).getBranchId();
                    }
                }
                if (selectedBranch == null) {
                    selectedBranch = retVal.get(0);
                    selectedBranchId = retVal.get(0).getBranchId();
                }
            }
            return retVal;
        }

        return new ArrayList<Branch>();
    }

    /**
     * This should be used from the client portal, can grab the route list of
     * clients that are accessible, or alternatively the current client. It
     * figures out if it's a route or a client.
     *
     * @return
     */
    public ArrayList<Client> getCurrentListOfClients() {
        if (clients == null) {
            ArrayList<Client> retVal = new ArrayList<Client>();
            retVal.add(this.selectedClient);
            return retVal;
        } else {
            return clients;
        }
    }

    public String getAccessibleClientsToUrlParam() {
        StringBuilder retVal = new StringBuilder();
        ArrayList<Client> tempClients = getAccessibleClients();
        for (int c = 0; c < tempClients.size(); c++) {
            retVal.append("&clientId=").append(tempClients.get(c).getClientId().toString());
        }
        return retVal.toString();
    }

    public String getGMTOffset() {
        //If it hasn't been loaded or alternatively the branch has changed because the client selected from route has changed.
        if (selectedBranch == null) {
            CompanyService companyService = new CompanyService();
            try {
                selectedBranch = companyService.getBranchById(this.selectedBranchId);
            } catch (Exception exe) {
            }
        }
        if (selectedBranch == null || selectedBranch.getTimezone().equals("EST")) {
            return "US/Eastern";
        }
        return selectedBranch.getTimezone();
    }

    public ArrayList<Client> getAccessibleClients() {
        if (selectedBranch != null) {
            if (cachedClients.get(selectedBranch.getBranchId()) == null) {
                ClientService clientService = new ClientService(this.companyId);
                try {
                    ArrayList<Client> clients = clientService.getClientsByBranch(selectedBranch.getBranchId());
                    cachedClients.put(selectedBranch.getBranchId(), clients);
                } catch (Exception exe) {
                    exe.printStackTrace();
                }
            }
            return cachedClients.get(selectedBranch.getBranchId());
        }
        return new ArrayList<Client>();
    }

    /**
     * @param accessibleCompanies the accessibleCompanies to set
     */
    public void setAccessibleCompanies(ArrayList<Company> accessibleCompanies) {
        this.accessibleCompanies = accessibleCompanies;
    }

    /**
     * @return the selectedCompany
     */
    public Company getSelectedCompany() {
        if (selectedCompany == null) {
            try {
                CompanyService compService = new CompanyService();
                selectedCompany = compService.getCompanyById(Integer.parseInt(this.companyId));
            } catch (Exception exe) {
            }
        }
        return selectedCompany;
    }

    /**
     * @param selectedCompany the selectedCompany to set
     */
    public void setSelectedCompany(Company selectedCompany) {
        this.selectedCompany = selectedCompany;
    }

    public void setSelectedCompanyId(Integer id) {
        for (int c = 0; c < accessibleCompanies.size(); c++) {
            if (id == Integer.parseInt(accessibleCompanies.get(c).getCompId())) {
                selectedCompany = accessibleCompanies.get(c);
                if (accessibleBranches == null) {
                    accessibleBranches = new ArrayList<Branch>();
                }
                accessibleBranches.clear();
                accessibleBranches.addAll(selectedCompany.getBranches());
                if (accessibleBranches.size() > 0) {
                    for (int b = 0; b < accessibleBranches.size(); b++) {
                        if (accessibleBranches.get(b).getDefaultBranch()) {
                            selectedBranch = accessibleBranches.get(b);
                        }
                    }
                    if (selectedBranch == null) {
                        selectedBranch = accessibleBranches.get(0);
                    }
                }
            }
        }
    }

    public Integer getSelectedCompanyId() {
        try {
            return Integer.parseInt(selectedCompany.getId());
        } catch (Exception exe) {
            return 0;
        }
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
     * @return the selectedBranch
     */
    public Integer getSelectedBranchId() {
        if (selectedBranch != null) {
            return selectedBranch.getBranchId();
        }
        try {
            ArrayList<Branch> branches = this.getAccessibleBranches();
            if (branches.size() == 1) {
                return branches.get(0).getBranchId();
            }
        } catch (Exception exe) {
        }
        return 0;
    }

    /**
     * @param selectedBranch the selectedBranch to set
     */
    public void setSelectedBranchId(Integer id) {
        if (accessibleBranches == null) {
            accessibleBranches = new ArrayList<Branch>();
            accessibleBranches.addAll(selectedCompany.getBranches());
        }
        for (int c = 0; c < accessibleBranches.size(); c++) {
            if (id.equals(accessibleBranches.get(c).getBranchId())) {
                selectedBranch = accessibleBranches.get(c);
            }
        }
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

    /**
     * @return the companyId
     */
    public String getCompanyId() {
        try {
            if (companyId == null || companyId.trim().length() == 0) {
                FacesContext context = FacesContext.getCurrentInstance();
                FacesContext.getCurrentInstance().addMessage("sessionTimeout", new FacesMessage("Session was lost, please relogin in!"));
                context.getExternalContext().getFlash().setKeepMessages(true);
                context.getExternalContext().redirect(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/login.xhtml?error=session_timeout");
                context.responseComplete();
            }
        } catch (Exception exe) {
        }
        return companyId;
    }

    /**
     * @param companyId the companyId to set
     */
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    /**
     * @return the conversation
     */
    public WindowContext getConversation() {
        return conversation;
    }

    /**
     * @param conversation the conversation to set
     */
    public void setConversation(WindowContext conversation) {
        this.conversation = conversation;
    }

    /**
     * @return the isLoggedInAsAdmin
     */
    public Boolean getIsLoggedInAsAdmin() {
        return isLoggedInAsAdmin;
    }

    /**
     * @param isLoggedInAsAdmin the isLoggedInAsAdmin to set
     */
    public void setIsLoggedInAsAdmin(Boolean isLoggedInAsAdmin) {
        this.isLoggedInAsAdmin = isLoggedInAsAdmin;
    }

    /**
     * @return the routes
     */
    public ArrayList<ClientRoute> getRoutes() {
        return routes;
    }

    /**
     * @param routes the routes to set
     */
    public void setRoutes(ArrayList<ClientRoute> routes) {
        this.routes = routes;
    }

    /**
     * @return the selRoute
     */
    public Integer getSelRoute() {
        return selRoute;
    }

    /**
     * @param selRoute the selRoute to set
     */
    public void setSelRoute(Integer selRoute) {
        this.selRoute = selRoute;
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
     * @return the selClientFromRoute
     */
    public Integer getSelClientFromRoute() {
        return selClientFromRoute;
    }

    /**
     * @param selClientFromRoute the selClientFromRoute to set
     */
    public void setSelClientFromRoute(Integer selClientFromRoute) {
        this.selClientFromRoute = selClientFromRoute;
    }

    /**
     * @return the adminSelectedEmployeeId
     */
    public Integer getAdminSelectedEmployeeId() {
        return adminSelectedEmployeeId;
    }

    /**
     * @param adminSelectedEmployeeId the adminSelectedEmployeeId to set
     */
    public void setAdminSelectedEmployeeId(Integer adminSelectedEmployeeId) {
        this.adminSelectedEmployeeId = adminSelectedEmployeeId;
    }

    /**
     * @return the locale
     */
    public Locale getLocale() {
        if (locale == null) {
            try {
                ArrayList<Branch> branches = this.getAccessibleBranches();
                Integer selectedBranch = this.selectedBranchId;
                if (selectedBranch == null) {
                    locale = new Locale(branches.get(0).getDefaultLanguage());
                } else {
                    locale = new Locale(this.getSelectedBranch().getDefaultLanguage());
                }
            } catch (Exception exe) {
                locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            }
        }
        return locale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void changeLanguage(String language) {
        locale = new Locale(language);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    }

    /**
     * @return the adminSelectedFormId
     */
    public Integer getAdminSelectedFormId() {
        return adminSelectedFormId;
    }

    /**
     * @param adminSelectedFormId the adminSelectedFormId to set
     */
    public void setAdminSelectedFormId(Integer adminSelectedFormId) {
        this.adminSelectedFormId = adminSelectedFormId;
    }

    /**
     * @return the referral
     */
    public String getReferral() {
        return referral;
    }

    /**
     * @param referral the referral to set
     */
    public void setReferral(String referral) {
        this.referral = referral;
    }
}
