/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.patrolpro.beans.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;
import schedfoxlib.model.Client;
import schedfoxlib.model.ClientRoute;
import schedfoxlib.services.ClientService;

/**
 *
 * @author ira
 */
@Named("adminRouteBean")
@javax.faces.view.ViewScoped
public class AdminRouteBean implements Serializable {
    private ArrayList<ClientRoute> routes;
    
    @Inject
    private SessionBean sessionBean;
    
    private ClientRoute selectedRoute;
    private ArrayList<Client> clients;
    private ArrayList<Integer> selectedClients;
    private Integer routeToDelete;
    
    public AdminRouteBean() {
        
    }
    
    @PostConstruct
    public void postConstruct() {
        try {
            ClientService clientService = new ClientService(this.sessionBean.getCompanyId());
            clients = clientService.getActiveClients();
            this.reloadRoutes();
        } catch (Exception exe) {}
    }
    
    public void loadRouteInfo() {
        try {
            String selectedRouteId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedRouteId");
            Integer selectedRouteInt = Integer.parseInt(selectedRouteId);
            for (int r = 0; r < routes.size(); r++) {
                if (routes.get(r).getClientRouteId().equals(selectedRouteInt)) {
                    this.setSelectedRoute(routes.get(r));
                }
            }
        } catch (Exception exe) {}
    }

    public void reloadRoutes() {
        try {
            ClientService clientService = new ClientService(sessionBean.getCompanyId());
            this.routes = clientService.getRoutesWithClientTieIn();
        } catch (Exception exe) {}
    }
    
    public void addNew() {
        this.selectedRoute = new ClientRoute();
        this.selectedClients = new ArrayList<Integer>();
    }
    
    public void setRouteToDelete(ClientRoute route) {
        try {
            routeToDelete = route.getClientRouteId();
        } catch (Exception exe) {}
    }
    
    public void deleteRoute() {
        try {
            ClientService clientService = new ClientService(sessionBean.getCompanyId());
            clientService.deleteClientRoute(routeToDelete);
            this.reloadRoutes();
        } catch (Exception exe) {}
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
     * @return the selectedRoute
     */
    public ClientRoute getSelectedRoute() {
        return selectedRoute;
    }

    /**
     * @param selectedRoute the selectedRoute to set
     */
    public void setSelectedRoute(ClientRoute selectedRoute) {
        this.selectedRoute = selectedRoute;
        try {
            ClientService clientService = new ClientService(this.sessionBean.getCompanyId());
            ArrayList<Client> clients = clientService.getClientsForRoute(this.selectedRoute.getClientRouteId());
            this.selectedClients = new ArrayList<Integer>();
            for (int c = 0; c < clients.size(); c++) {
                selectedClients.add(clients.get(c).getClientId());
            }
        } catch (Exception exe) {}
    }

    public void onRowSelect(SelectEvent event) {
        
    }
 
    public void setClients(ArrayList<Client> clients) {
        this.clients = clients;
    }
    
    public Client loadClientInfo(Integer clientId) {
        for (int c = 0; c < this.clients.size(); c++) {
            if (clientId.equals(clients.get(c).getClientId())) {
                return clients.get(c);
            }
        }
        return new Client(new Date());
    }
    
    public String generateClientList(ArrayList<Integer> selectedClients) {
        if (selectedClients.isEmpty()) {
            return "No Clients Assigned";
        } else {
            StringBuilder retVal = new StringBuilder();
            for (int s = 0; s < selectedClients.size(); s++) {
                Client currClient = loadClientInfo(selectedClients.get(s));
                if (s > 0) {
                    retVal.append(", ");
                }
                retVal.append(currClient.getClientName());
            }
            return retVal.toString();
        }
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
    
    public void save() {
        try {
            ClientService clientService = new ClientService(sessionBean.getCompanyId());
            selectedRoute.setClientRouteId(clientService.saveClientRoute(selectedRoute));
            clientService.assignClientsToRoutes(selectedClients, this.selectedRoute.getClientRouteId());
        } catch (Exception exe) {}
        reloadRoutes();
    }

    /**
     * @return the selectedClients
     */
    public ArrayList<Integer> getSelectedClients() {
        return selectedClients;
    }

    /**
     * @param selectedClients the selectedClients to set
     */
    public void setSelectedClients(ArrayList<Integer> selectedClients) {
        this.selectedClients = selectedClients;
    }

}
