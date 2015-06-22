/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import com.patrolpro.beans.admin.SessionBean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import schedfoxlib.controller.exceptions.NoUserException;
import schedfoxlib.controller.exceptions.RetrieveDataException;
import schedfoxlib.model.Branch;
import schedfoxlib.model.Client;
import schedfoxlib.model.ClientRoute;
import schedfoxlib.model.Company;
import schedfoxlib.model.CompanyObj;
import schedfoxlib.model.CrossCompanyClient;
import schedfoxlib.model.User;
import schedfoxlib.services.ClientService;
import schedfoxlib.services.CompanyService;
import schedfoxlib.services.UserService;

/**
 *
 * @author user
 */
@ViewScoped
@Named("loginClientBean")
public class LoginClientBean implements Serializable {

    private String userName;
    private String password;

    private String clientId;
    private String companyId;
    private String branchId;
    private String userId;
    private String lang;

    private CompanyObj loadedCompany;

    @Inject
    private SessionBean sessionBean;

    @Inject
    private ClientSessionBean clientSessionBean;

    /**
     * Creates a new instance of LoginClientBean
     */
    public LoginClientBean() {

    }

    public String getAndroidApkLocation() {
        try {
            String path = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
            return path + "/d/" + loadedCompany.getCompanyUrl();
        } catch (Exception exe) {
        }
        return "";
    }

    @PostConstruct
    public void postContruct() {
        clientId = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("clientId");
        companyId = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("companyId");
        branchId = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("branchId");
        userId = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("userId");

        userName = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("username");
        password = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("password");
        lang = FacesContext.getCurrentInstance().getExternalContext().
                getRequestParameterMap().get("lang");

        try {
            String companyPath = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("company");
            if (companyPath != null && companyPath.trim().length() > 0) {
                CompanyService compService = new CompanyService();
                CompanyObj comp = compService.getCompanyByPath(companyPath);
                if (comp != null && comp.getCompanyId() != null) {
                    loadedCompany = comp;
                }
            }
        } catch (Exception exe) {
        }
        if (clientId == null && userName == null) {
            userName = "";
            password = "";
        } else if (userName != null) {
            String result = doLogin();
            try {
                if (result.equals("loginUser")) {
                    FacesContext.getCurrentInstance().getExternalContext().redirect("client/admin/admin_dashboard.xhtml");
                } else {
                    FacesContext.getCurrentInstance().getExternalContext().redirect("client/dashboard.xhtml");
                }
            } catch (Exception exe) {
            }
        } else {
            try {
                //Login from the blue monitor
                ArrayList<Client> loggedInClients = new ArrayList<Client>();

                ClientService clientService = new ClientService(companyId);
                CompanyService companyService = new CompanyService();
                UserService userService = new UserService(companyId);
                Client myClient = clientService.getClientAndAddressById(Integer.parseInt(clientId));
                User user = userService.getUserById(Integer.parseInt(userId));

                Company comp = companyService.getCompanyById(Integer.parseInt(companyId));
                ArrayList<Branch> branches = companyService.getBranchesForCompany(Integer.parseInt(comp.getCompId()));
                Vector<Branch> mybranches = new Vector<Branch>();
                mybranches.addAll(branches);
                comp.setBranches(mybranches);

                loggedInClients.add(myClient);

                clientSessionBean.setCompanyId(companyId);
                clientSessionBean.setLoggedInUser(user);
                clientSessionBean.setSelectedCompany(comp);
                clientSessionBean.setSelectedClient(myClient);
                clientSessionBean.setClients(loggedInClients);
                clientSessionBean.setIsLoggedInAsAdmin(true);
                if (sessionBean.getReferral() != null) {
                    clientSessionBean.setReferral(sessionBean.getReferral());
                }
                try {
                    if (myClient.getLogIntoRoute()) {
                        setupLoginToRoute(myClient, companyId);
                    }
                } catch (Exception exe) {
                }
                clientSessionBean.changeLanguage(lang);

                try {
                    FacesContext.getCurrentInstance().getExternalContext().redirect("client/dashboard.xhtml?windowId=" + clientSessionBean.getConversation().getId());
                } catch (Exception exe) {
                }
            } catch (Exception exe) {
            }
        }
    }

    public String doLogin() {
        boolean fetchLogin = true;
        if (getUserName().trim().length() == 0) {
            FacesContext.getCurrentInstance().addMessage("usernameError", new FacesMessage(FacesMessage.SEVERITY_ERROR, "User Name Missing!", "Please enter a username!"));
            fetchLogin = false;
        }
        if (getPassword().trim().length() == 0) {
            FacesContext.getCurrentInstance().addMessage("passwordError", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password Missing!", "Please enter a password!"));
            fetchLogin = false;
        }

        if (fetchLogin) {
            ClientService clientService = new ClientService();
            try {
                ArrayList<CrossCompanyClient> clients = clientService.getCrossSchemaPatrolProClientsByLogin(userName, password);
                if (clients != null && !clients.isEmpty()) {
                    CompanyService compService = new CompanyService();
                    //@TODO: Better way of figuring out active companyid if matching multiple schema clients
                    Integer activeCompanyId = clients.get(0).getCompanyId();

                    ArrayList<Client> myClients = new ArrayList<Client>();
                    for (int c = 0; c < clients.size(); c++) {
                        myClients.add(clients.get(c).getClient());
                    }

                    clientSessionBean.setCompanyId(activeCompanyId + "");
                    clientSessionBean.setSelectedCompany(compService.getCompanyById(activeCompanyId));
                    clientSessionBean.setClients(myClients);
                    if (clients.size() > 0) {
                        clientSessionBean.setSelectedClient(myClients.get(0));
                    }

                    for (int c = 0; c < myClients.size(); c++) {
                        if (myClients.get(c).getLogIntoRoute()) {
                            this.setupLoginToRoute(myClients.get(c), activeCompanyId + "");
                        }
                    }
                }

                if (clients == null || clients.isEmpty()) {
                    //Lets fall back to schedfox users now....
                    UserService userService = new UserService();
                    try {
                        User myUser = userService.getUserByLogin(userName, password);
                        ArrayList<Company> companies = userService.getCompaniesForUser(myUser.getId());
                        if (companies.size() == 1) {
                            sessionBean.setCompanyId(companies.get(0).getCompId());
                            sessionBean.setSelectedCompany(companies.get(0));
                        } else if (userName.equals("irajuneau")) {
                            getSessionBean().setCompanyId("2");
                            Company comp = companies.get(0);
                            for (int c = 0; c < companies.size(); c++) {
                                if (companies.get(c).getCompId().equals("2")) {
                                    comp = companies.get(c);
                                }
                            }
                            getSessionBean().setSelectedCompany(comp);
                        } else if (companies.size() > 1) {
                            for (int c = 0; c < companies.size(); c++) {
                                if (companies.get(c).getCompId().equals("2")) {
                                    sessionBean.setCompanyId(companies.get(c).getCompId());
                                    sessionBean.setSelectedCompany(companies.get(c));
                                }
                            }
                        }
                        sessionBean.setReferral(clientSessionBean.getReferral());
                        getSessionBean().setLoggedInUser(myUser);
                        return "loginUser";
                    } catch (NoUserException nsue) {
                        FacesContext.getCurrentInstance().addMessage("password", new FacesMessage("Invalid Login!"));
                        fetchLogin = false;
                    }
                }
            } catch (RetrieveDataException re) {
                FacesContext.getCurrentInstance().addMessage("password", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Login!", "Could not verify login"));
                fetchLogin = false;
            }
        }
        if (fetchLogin) {
            return "loggedin";
        } else {
            return "error";
        }
    }

    private void setupLoginToRoute(Client myClient, String companyId) {
        try {
            ClientService clientService = new ClientService(companyId);
            ArrayList<ClientRoute> routes = clientService.getRoutesAssociatedWithClientWithMinimalClientData(myClient.getClientId());
            clientSessionBean.setSelRoute(routes.get(0));
            clientSessionBean.setRoutes(routes);

            clientSessionBean.setClientsShownForSelectedRoute(routes.get(0).getClients());
            clientSessionBean.setSelClientFromRoute(myClient.getClientId());
        } catch (Exception exe) {
        }
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
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
     * @return the clientSessionBean
     */
    public ClientSessionBean getClientSessionBean() {
        return clientSessionBean;
    }

    /**
     * @param clientSessionBean the clientSessionBean to set
     */
    public void setClientSessionBean(ClientSessionBean clientSessionBean) {
        this.clientSessionBean = clientSessionBean;
    }

    /**
     * @return the loadedCompany
     */
    public CompanyObj getLoadedCompany() {
        return loadedCompany;
    }

    /**
     * @param loadedCompany the loadedCompany to set
     */
    public void setLoadedCompany(CompanyObj loadedCompany) {
        this.loadedCompany = loadedCompany;
    }

}
