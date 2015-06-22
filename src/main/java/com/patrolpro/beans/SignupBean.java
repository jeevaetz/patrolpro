/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import com.patrolpro.beans.admin.SessionBean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.apache.commons.validator.EmailValidator;
import schedfoxlib.model.Branch;
import schedfoxlib.model.CompanyObj;
import schedfoxlib.model.ManagementClient;
import schedfoxlib.model.User;
import schedfoxlib.services.BranchService;
import schedfoxlib.services.CompanyService;
import schedfoxlib.services.UserService;

/**
 *
 * @author ira
 */
@ManagedBean(name = "signupBean")
@SessionScoped
public class SignupBean implements Serializable {

    private ManagementClient managementClient;
    private User rootUser;
    private Integer numberOfDevices;
    private String passwordConfirmation;

    @ManagedProperty("#{sessionBean}")
    private SessionBean sessionBean;
    
    public SignupBean() {
        managementClient = new ManagementClient();
        rootUser = new User();

        numberOfDevices = 10;
    }

    /**
     * @return the managementClient
     */
    public ManagementClient getManagementClient() {
        return managementClient;
    }

    /**
     * @param managementClient the managementClient to set
     */
    public void setManagementClient(ManagementClient managementClient) {
        this.managementClient = managementClient;
    }

    public String nextScreen() {
        EmailValidator emailValidator = EmailValidator.getInstance();
        boolean isValid = true;
        if (!emailValidator.isValid(managementClient.getManagement_billing_email1())) {
            FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "email", "This email address is not valid!"));
            isValid = false;
        }
        if (managementClient.getManagement_client_phone().trim().length() == 0) {
            FacesContext.getCurrentInstance().addMessage("phone", new FacesMessage(FacesMessage.SEVERITY_ERROR, "phone", "This phone number is not valid!"));
            isValid = false;
        }
        if (managementClient.getManagement_client_name().trim().length() == 0) {
            FacesContext.getCurrentInstance().addMessage("companyName", new FacesMessage(FacesMessage.SEVERITY_ERROR, "companyName", "Enter a company name!"));
            isValid = false;
        }
        if (managementClient.getManagement_client_address().trim().length() == 0) {
            FacesContext.getCurrentInstance().addMessage("companyAddress", new FacesMessage(FacesMessage.SEVERITY_ERROR, "companyAddress", "Enter a address!"));
            isValid = false;
        }
        if (managementClient.getManagement_client_city().trim().length() == 0) {
            FacesContext.getCurrentInstance().addMessage("companyAddress", new FacesMessage(FacesMessage.SEVERITY_ERROR, "companyCity", "Enter a city!"));
            isValid = false;
        }
        if (managementClient.getManagement_client_state().trim().length() == 0) {
            FacesContext.getCurrentInstance().addMessage("companyAddress", new FacesMessage(FacesMessage.SEVERITY_ERROR, "companyCity", "Enter a state!"));
            isValid = false;
        }
        if (managementClient.getManagement_client_zip().trim().length() == 0) {
            FacesContext.getCurrentInstance().addMessage("companyAddress", new FacesMessage(FacesMessage.SEVERITY_ERROR, "companyCity", "Enter a zip!"));
            isValid = false;
        }
        if (isValid) {
            return "saved";
        } else {
            return "errorSetup1";
        }
    }

    public String setupUserAndCompany() {
        UserService userService = new UserService();
        boolean isValid = true;
        try {
            EmailValidator emailValidator = EmailValidator.getInstance();
            ArrayList<User> users = userService.getUsersByLogin(rootUser.getUserLogin());
            if (rootUser.getUserFirstName().trim().length() == 0) {
                FacesContext.getCurrentInstance().addMessage("firstName", new FacesMessage(FacesMessage.SEVERITY_ERROR, "firstName", "Please enter a firstname!"));
                isValid = false;
            }
            if (rootUser.getUserLastName().trim().length() == 0) {
                FacesContext.getCurrentInstance().addMessage("lastName", new FacesMessage(FacesMessage.SEVERITY_ERROR, "lastName", "Please enter a lastname!"));
                isValid = false;
            }
            if (rootUser.getUserEmail().trim().length() == 0) {
                FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "email", "Please enter an email!"));
                isValid = false;
            }
            if (!emailValidator.isValid(rootUser.getUserEmail().trim())) {
                FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "email", "Invalid email!"));
                isValid = false;
            }
            if (users != null && users.size() > 0) {
                FacesContext.getCurrentInstance().addMessage("login", new FacesMessage(FacesMessage.SEVERITY_ERROR, "login", "This username cannot be used, please choose another!"));
                isValid = false;
            }
            if (rootUser.getUserPassword().trim().length() == 0) {
                FacesContext.getCurrentInstance().addMessage("password", new FacesMessage(FacesMessage.SEVERITY_ERROR, "password", "Please enter a password!"));
                isValid = false;
            }
            if (passwordConfirmation == null || !passwordConfirmation.equalsIgnoreCase(rootUser.getUserPassword())) {
                FacesContext.getCurrentInstance().addMessage("passwordConf", new FacesMessage(FacesMessage.SEVERITY_ERROR, "passwordConf", "Passwords don't match!"));
                isValid = false;
            }

            if (isValid) {
                CompanyService companyService = new CompanyService();
                BranchService branchService = new BranchService();

                String myCompanyName = managementClient.getManagement_client_name().toLowerCase().replaceAll(" ", "_");
                if (myCompanyName.length() > 15) {
                    myCompanyName = myCompanyName.substring(0, 14);
                }

                String databaseName = companyService.setupCompanySchema(myCompanyName, "template0_db");

                managementClient.setManagement_date_started(new Date());
                managementClient.setManagement_client_email(managementClient.getManagement_billing_email1());
                Integer managementClientId = companyService.saveManagementClient(managementClient);
                managementClient.setManagement_id(managementClientId);

                CompanyObj company = new CompanyObj();
                company.setCompanyManagementId(managementClientId);
                company.setCompanyName(managementClient.getManagement_client_name());
                company.setDateOfCreation(new Date());
                company.setStatusModifieddate(new Date());
                company.setCompanyDb(databaseName);
                company.setCompanyId(companyService.saveCompanyObj(company));

                Branch branch = new Branch();
                branch.setBranchName(managementClient.getManagement_client_name());
                branch.setBranchManagementId(managementClientId.shortValue());
                branch.setBranchId(branchService.saveBranch(branch));

                rootUser.setUserMd5("");
                Integer userId = userService.saveUser(rootUser, true);

                userService.saveUserToBranchAndCompany(userId, branch.getBranchId(), company.getCompanyId());

                LoginClientBean userSessionBean = new LoginClientBean();
                userSessionBean.setSessionBean(sessionBean);
                userSessionBean.setPassword(rootUser.getUserPassword());
                userSessionBean.setUserName(rootUser.getUserLogin());
                return userSessionBean.doLogin();
            }
        } catch (Exception exe) {
            exe.printStackTrace();
        }
        if (isValid) {
            return "setupd";
        } else {
            return "errorSetup2";
        }
    }

    /**
     * @return the numberOfDevices
     */
    public Integer getNumberOfDevices() {
        return numberOfDevices;
    }

    /**
     * @param numberOfDevices the numberOfDevices to set
     */
    public void setNumberOfDevices(Integer numberOfDevices) {
        this.numberOfDevices = numberOfDevices;
    }

    /**
     * @return the rootUser
     */
    public User getRootUser() {
        return rootUser;
    }

    /**
     * @param rootUser the rootUser to set
     */
    public void setRootUser(User rootUser) {
        this.rootUser = rootUser;
    }

    /**
     * @return the passwordConfirmation
     */
    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    /**
     * @param passwordConfirmation the passwordConfirmation to set
     */
    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
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
