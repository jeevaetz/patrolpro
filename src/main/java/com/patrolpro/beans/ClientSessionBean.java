/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import com.patrolpro.beans.interfaces.RefreshClientListingInterface;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.WindowContext;
import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.WindowScoped;
import schedfoxlib.model.Branch;
import schedfoxlib.model.Client;
import schedfoxlib.model.ClientRoute;
import schedfoxlib.model.Company;
import schedfoxlib.model.User;
import schedfoxlib.services.CompanyService;

/**
 *
 * @author ira
 */
@Named("clientSessionBean")
@WindowScoped
public class ClientSessionBean implements Serializable {

    private Locale locale = null;

    private User loggedInUser;
    private Company selectedCompany;

    //Do not reference directly
    private Branch currentClientBranch;

    private ArrayList<ClientRoute> routes;
    private ArrayList<Client> clients;
    private ArrayList<Client> clientsShownForSelectedRoute;
    private Client selectedClient;
    private Boolean isLoggedInAsAdmin;
    private ClientRoute selRoute;
    private Integer selClientFromRoute;
    private String referral;

    private String companyId = "2";

    @Inject
    private WindowContext conversation;

    public ClientSessionBean() {

    }

    @PostConstruct
    public void postConstruct() {
        locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        try {
            String ref = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("ref");
            if (ref != null && ref.length() > 0) {
                this.referral = ref;
            }
        } catch (Exception exe) {}
    }
    
    public String getLogoURL() {
        try {
            String ref = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("ref");
            if (ref != null && ref.length() > 0) {
                this.referral = ref;
            }
            if (this.referral.equals("skytrack")) {
                return "logo_lt.png";
            }
        } catch (Exception exe) {}
        return "logo.png";
    }

    public void changeLanguage(String language) {
        locale = new Locale(language);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
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

    public String getAccessibleClientsToUrlParam() {
        StringBuilder retVal = new StringBuilder();
        for (int c = 0; c < clients.size(); c++) {
            retVal.append("&clientId=").append(clients.get(c).getClientId().toString());
        }
        return retVal.toString();
    }

    public boolean hasMultipleClients() {
        try {
            return this.clientsShownForSelectedRoute.size() > 1;
        } catch (Exception exe) {
            return false;
        }
    }

    public boolean hasRoutes() {
        if (this.routes == null) {
            return false;
        }
        return true;
    }

    /**
     * Run on selecting a client on the route.
     *
     * @param ae
     */
    public void onClientAction(AjaxBehaviorEvent ae) {
        try {
            for (int c = 0; c < clientsShownForSelectedRoute.size(); c++) {
                if (this.clientsShownForSelectedRoute.get(c).getClientId().equals(selClientFromRoute)) {
                    this.setSelectedClient(clientsShownForSelectedRoute.get(c));
                    this.clients = new ArrayList<Client>();
                    this.clients.add(this.getSelectedClient());
                }
            }
            if (selClientFromRoute == -1) {
                this.clients = this.clientsShownForSelectedRoute;
            }
        } catch (Exception exe) {
        }
    }

    public void addClientToActiveList(Client client) {
        if (this.clients == null) {
            this.clients = new ArrayList<Client>();
        }
        this.clients.add(client);
    }

    /**
     * Returns the selected branch if it is available, if not then we return -1,
     * -1 can be returned when all clients for a route are displayed
     *
     * @return
     */
    public Integer getSelectedBranchId() {
        if (currentClientBranch == null) {
            return -1;
        }
        return currentClientBranch.getBranchId();
    }

    public String getGMTOffset() {
        //If it hasn't been loaded or alternatively the branch has changed because the client selected from route has changed.
        if (currentClientBranch == null || (this.selectedClient != null && !currentClientBranch.getBranchId().equals(this.selectedClient.getBranchId()))) {
            CompanyService companyService = new CompanyService();
            try {
                currentClientBranch = companyService.getBranchById(this.selectedClient.getBranchId());
            } catch (Exception exe) {
            }
        } else if (this.selectedClient == null && currentClientBranch == null) {
            CompanyService companyService = new CompanyService();
            try {
                currentClientBranch = companyService.getBranchById(this.clients.get(0).getBranchId());
            } catch (Exception exe) {
            }
        }
        if (currentClientBranch == null || currentClientBranch.getTimezone().equals("EST")) {
            return "US/Eastern";
        }
        return currentClientBranch.getTimezone();
    }
    
    /**
     * Returns if the client is currently selected
     * @param clientId
     * @return 
     */
    public Boolean isClientSelectedFromRoute(Integer clientId) {
        try {
            for (int c = 0; c < this.clients.size(); c++) {
                if (this.clients.get(c).getClientId().equals(clientId)) {
                    return true;
                }
            }
        } catch (Exception exe) {}
        return false;
    }
    
    public void forceRefreshOfActiveViews() {
        try {
            Iterator<Object> views = FacesContext.getCurrentInstance().getViewRoot().getViewMap().values().iterator();
            while (views.hasNext()) {
                Object view = views.next();
                if (view instanceof RefreshClientListingInterface) {
                    ((RefreshClientListingInterface) view).refresh();
                }
            }
        } catch (Exception exe) {
        }
    }

    public void selectRouteById(Integer routeId) {
        for (int r = 0; r < this.routes.size(); r++) {
            if (routeId.equals(this.routes.get(r).getId())) {
                this.selRoute = this.routes.get(r);
                
                this.clients = this.selRoute.getClients();
                
                forceRefreshOfActiveViews();
            }
        }
    }

    public void selectClientById(Integer clientId) {
        ArrayList<Client> selClients = this.selRoute.getClients();
        for (int r = 0; r < selClients.size(); r++) {
            if (clientId.equals(selClients.get(r).getId())) {
                this.selectedClient = selClients.get(r);
                this.clients = new ArrayList<Client>();
                this.clients.add(selectedClient);
                
                forceRefreshOfActiveViews();
            }
        }
    }

    /**
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
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
     * @return the isLoggedInAsAdmin
     */
    public Boolean getIsLoggedInAsAdmin() {
        if (isLoggedInAsAdmin == null) {
            return false;
        }
        return isLoggedInAsAdmin;
    }

    /**
     * @param isLoggedInAsAdmin the isLoggedInAsAdmin to set
     */
    public void setIsLoggedInAsAdmin(Boolean isLoggedInAsAdmin) {
        this.isLoggedInAsAdmin = isLoggedInAsAdmin;
    }

    /**
     * @return the selRoute
     */
    public ClientRoute getSelRoute() {
        return selRoute;
    }

    /**
     * @param selRoute the selRoute to set
     */
    public void setSelRoute(ClientRoute selRoute) {
        this.selRoute = selRoute;
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
     * @return the selectedCompany
     */
    public Company getSelectedCompany() {
        return selectedCompany;
    }

    /**
     * @param selectedCompany the selectedCompany to set
     */
    public void setSelectedCompany(Company selectedCompany) {
        this.selectedCompany = selectedCompany;
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
     * @return the clientsShownForSelectedRoute
     */
    public ArrayList<Client> getClientsShownForSelectedRoute() {
        return clientsShownForSelectedRoute;
    }

    /**
     * @param clientsShownForSelectedRoute the clientsShownForSelectedRoute to
     * set
     */
    public void setClientsShownForSelectedRoute(ArrayList<Client> clientsShownForSelectedRoute) {
        this.clientsShownForSelectedRoute = clientsShownForSelectedRoute;
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
