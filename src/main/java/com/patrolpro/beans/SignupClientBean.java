/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import com.patrolpro.servlet.utils.MailAuthenticator;
import java.io.Serializable;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.apache.commons.validator.EmailValidator;

@Named("signupClientBean")
@ViewScoped
public class SignupClientBean implements Serializable {

    private String contactName;
    private String companyName;
    private String contactEmail;
    private String contactPhone;
    private String message;
    
    private UIComponent nameErrorLabel;
    private UIComponent companyNameLabel;
    private UIComponent contactEmailLabel;
    private UIComponent contactPhoneLabel;
    private UIComponent messageLabel;

    public SignupClientBean() {
        try {
            Map<String, String> params
                    = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            this.companyName = params.get("company");
            this.contactName = params.get("full_name");
            this.contactEmail = params.get("email_address");
        } catch (Exception exe) {
        }
    }

    /**
     * @return the contactName
     */
    public String getContactName() {
        return contactName;
    }

    /**
     * @param contactName the contactName to set
     */
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    /**
     * @return the companyName
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * @param companyName the companyName to set
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * @return the contactEmail
     */
    public String getContactEmail() {
        return contactEmail;
    }

    /**
     * @param contactEmail the contactEmail to set
     */
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    /**
     * @return the contactPhone
     */
    public String getContactPhone() {
        return contactPhone;
    }

    /**
     * @param contactPhone the contactPhone to set
     */
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public boolean validateInformation() {
        boolean isValid = true;
        FacesContext myContext = FacesContext.getCurrentInstance();
        if (contactName == null || contactName.length() < 3) {
            myContext.addMessage(this.nameErrorLabel.getClientId(myContext), new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Please enter a your name!"));
            isValid = false;
        }
        if (companyName == null || companyName.length() < 3) {
            myContext.addMessage(this.companyNameLabel.getClientId(myContext), new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Please enter a company name!"));
            isValid = false;
        }
        EmailValidator emailValidator = EmailValidator.getInstance();
        if (contactEmail == null || contactEmail.length() < 3) {
            myContext.addMessage(this.contactEmailLabel.getClientId(myContext), new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Please enter a contact email!"));
            isValid = false;
        } else if (!emailValidator.isValid(contactEmail)) {
            myContext.addMessage(this.contactEmailLabel.getClientId(myContext), new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Please enter a valid email!"));
            isValid = false;
        }
        if (contactPhone == null || contactPhone.length() < 3) {
            myContext.addMessage(this.contactPhoneLabel.getClientId(myContext), new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Please enter a contact phone number!"));
            isValid = false;
        }
        return isValid;
    }

    public String sendEmail() {
        try {
            boolean isValid = validateInformation();

            if (isValid) {
                StringBuilder emailMessage = new StringBuilder();
                emailMessage.append("Contact Name: " + this.contactName + "\r\n");
                emailMessage.append("Contact Email: " + this.contactEmail + "\r\n");
                emailMessage.append("Contact Phone: " + this.contactPhone + "\r\n");
                emailMessage.append("Company Name: " + this.companyName + "\r\n");
                emailMessage.append(message);

                Email htmlEmail = new SimpleEmail();
                if (message == null || message.length() == 0) {
                    message = "No message was provided by the user.";
                }
                htmlEmail.setFrom("signup@patrolpro.com");
                htmlEmail.setSubject("Trial Account Email!");
                htmlEmail.addTo("rharris@ainteractivesolution.com");
                htmlEmail.addTo("ijuneau@ainteractivesolution.com");
                htmlEmail.addCc("jc@champ.net");
                //htmlEmail.setHtmlMsg(emailMessage.toString());
                htmlEmail.setMsg(emailMessage.toString());
                //htmlEmail.setTextMsg(emailMessage.toString());

                htmlEmail.setDebug(true);
                htmlEmail.setAuthenticator(new MailAuthenticator("schedfox", "Sch3dF0x4m3"));
                htmlEmail.setHostName("mail2.champ.net");
                htmlEmail.setSmtpPort(587);
                htmlEmail.send();
                return "sentEmail";
            }
        } catch (Exception exe) {

        }
        return "invalid";
    }

    /**
     * @return the nameErrorLabel
     */
    public UIComponent getNameErrorLabel() {
        return nameErrorLabel;
    }

    /**
     * @param nameErrorLabel the nameErrorLabel to set
     */
    public void setNameErrorLabel(UIComponent nameErrorLabel) {
        this.nameErrorLabel = nameErrorLabel;
    }

    /**
     * @return the companyNameErrorLabel
     */
    public UIComponent getCompanyNameLabel() {
        return companyNameLabel;
    }

    /**
     * @param companyNameErrorLabel the companyNameErrorLabel to set
     */
    public void setCompanyNameLabel(UIComponent companyNameLabel) {
        this.companyNameLabel = companyNameLabel;
    }

    /**
     * @return the contactEmailLabel
     */
    public UIComponent getContactEmailLabel() {
        return contactEmailLabel;
    }

    /**
     * @param contactEmailLabel the contactEmailLabel to set
     */
    public void setContactEmailLabel(UIComponent contactEmailLabel) {
        this.contactEmailLabel = contactEmailLabel;
    }

    /**
     * @return the contactPhoneLabel
     */
    public UIComponent getContactPhoneLabel() {
        return contactPhoneLabel;
    }

    /**
     * @param contactPhoneLabel the contactPhoneLabel to set
     */
    public void setContactPhoneLabel(UIComponent contactPhoneLabel) {
        this.contactPhoneLabel = contactPhoneLabel;
    }

    /**
     * @return the messageLabel
     */
    public UIComponent getMessageLabel() {
        return messageLabel;
    }

    /**
     * @param messageLabel the messageLabel to set
     */
    public void setMessageLabel(UIComponent messageLabel) {
        this.messageLabel = messageLabel;
    }
}
