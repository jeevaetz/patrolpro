package com.patrolpro.servlet.utils;

import java.util.*;
import javax.mail.*;

/**
 *
 * @author vnguyen
 */
public class MailAuthenticator extends Authenticator {

    private String username,  password;

    public MailAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * method reads in prints to console messages on the mail server
     * @throws Exception
     */
    public void readMail() throws Exception {

        // Get system properties
        Properties properties = System.getProperties();

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties, this);
        URLName url = new URLName("pop3://test@addressofserver");

        // Get a Store object for the given URLName.
        Store store = session.getStore(url);
        store.connect();

        //Create a Folder object corresponding to the given name.
        Folder folder = store.getFolder("inbox");

        // Open the Folder.
        folder.open(Folder.READ_ONLY);

        Message[] message = folder.getMessages();

        // Display message.
        for (int i = 0; i < message.length; i++) {
            System.out.println("------------ Message " + (i + 1) + " ------------");
            System.out.println("SentDate : " + message[i].getSentDate());
            System.out.println("From : " + message[i].getFrom()[0]);
            System.out.println("Subject : " + message[i].getSubject());
            System.out.println("Content : " + message[i].getContent());
        }
        folder.close(true);
        store.close();
    }

    /**
     *
     * @return
     */
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }
}