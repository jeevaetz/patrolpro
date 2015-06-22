/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.patrolpro.beans;

import com.patrolpro.servlet.utils.MailAuthenticator;
import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;

/**
 *
 * @author ira
 */
@Named("contactUsBean")
@ViewScoped
public class ContactUsBean implements Serializable {
    private String name;
    private String email;
    private String phone;
    private String message;
    
    public ContactUsBean() {
        
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
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
    
    public String sendEmail() {
        try {
            StringBuilder emailMessage = new StringBuilder();
            emailMessage.append("From: " + this.name + "\r\n");
            emailMessage.append("Email: " + this.email + "\r\n");
            emailMessage.append("Phone: " + this.phone + "\r\n");
            emailMessage.append(message);
            
            Email htmlEmail = new SimpleEmail();
            htmlEmail.setMsg(message);
            htmlEmail.setFrom("contact@patrolpro.com");
            htmlEmail.setSubject("Contact Us Email");
            htmlEmail.addTo("rharris@ainteractivesolution.com");
            htmlEmail.addCc("ijuneau@ainteractivesolution.com");
            htmlEmail.addCc("jc@champ.net");
            htmlEmail.setMsg(emailMessage.toString());
            
            htmlEmail.setAuthenticator(new MailAuthenticator("schedfox", "Sch3dF0x4m3"));
            htmlEmail.setHostName("mail2.champ.net");
            htmlEmail.setSmtpPort(587);
            htmlEmail.send();
            return "sentContactEmail";
        } catch (Exception exe) {
            
        }
        return "invalid";
    }

    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
