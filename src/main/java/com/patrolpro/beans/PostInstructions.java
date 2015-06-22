/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import com.patrolpro.beans.interfaces.RefreshClientListingInterface;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.FileUploadEvent;
import schedfoxlib.model.Client;
import schedfoxlib.model.Company;
import schedfoxlib.model.DocClass;
import schedfoxlib.model.util.FileLoader;
import schedfoxlib.services.CompanyService;
import schedfoxlib.services.FileService;

/**
 *
 * @author user
 */
@Named("postInstructionsBean")
@ViewScoped
public class PostInstructions implements Serializable, RefreshClientListingInterface {

    @Inject
    private ClientSessionBean sessionBean;

    private HashMap<Client, ArrayList<DocClass>> postInstructions;
    private String selectedPostInstruction;

    public PostInstructions() {

    }

    @PostConstruct
    public void postConstruct() {
        postInstructions = new HashMap<Client, ArrayList<DocClass>>();
        this.refreshData();
    }

    public void deleteFile(String fileName, Integer clientId) {
        try {
            CompanyService companyController = new CompanyService();
            Company comp = companyController.getCompanyById(Integer.parseInt(sessionBean.getCompanyId()));

            FileLoader.removeAdditionalFile(comp.getDB(), "remove_location_additional_files",
                    "_", clientId + "", "1", fileName);

            FacesMessage msg = new FacesMessage("Successful", fileName + " has been deleted.");
            FacesContext.getCurrentInstance().addMessage(null, msg);

            refreshData();
        } catch (Exception exe) {
            FacesMessage msg = new FacesMessage("Problem deleting post instructions, please try again.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void handleFileUpload(FileUploadEvent event) {
        try {
            CompanyService companyController = new CompanyService();
            Company comp = companyController.getCompanyById(Integer.parseInt(sessionBean.getCompanyId()));
            File tempFile = File.createTempFile("postinstr", "tmnp");
            tempFile.deleteOnExit();
            FileOutputStream oStream = new FileOutputStream(tempFile);
            InputStream iStream = event.getFile().getInputstream();
            byte[] buffer = new byte[2048];
            int numRead = 0;
            while ((numRead = iStream.read(buffer)) > -1) {
                oStream.write(buffer, 0, numRead);
            }
            oStream.flush();
            oStream.close();
            iStream.close();

            boolean isSuccesfull = FileLoader.saveAdditionalFile("location_additional_files",
                    comp.getDB(), sessionBean.getSelectedClient().getClientId() + "", event.getFile().getFileName(), tempFile);
            tempFile.delete();

            FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, msg);

            refreshData();
        } catch (Exception exe) {
            FacesMessage msg = new FacesMessage("Problem uploading post instructions, please try a different file.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void refresh() {
        this.refreshData();
    }

    private void refreshData() {
        FileService fileInterface = new FileService(sessionBean.getCompanyId());
        try {

            postInstructions = new HashMap<Client, ArrayList<DocClass>>();
            if (sessionBean.getClients() != null) {
                ArrayList<Integer> clientIds = new ArrayList<Integer>();
                for (int c = 0; c < sessionBean.getClients().size(); c++) {
                    clientIds.add(sessionBean.getClients().get(c).getClientId());
                }
                HashMap<Integer, ArrayList<String>> hashOfPostInstructions = fileInterface.getPostInstructionsForClient(clientIds);

                for (int c = 0; c < sessionBean.getClients().size(); c++) {
                    Client currentClient = sessionBean.getClients().get(c);
                    String clientId = currentClient.getClientId().toString();
                    if (currentClient.getClientId() > -1) {
                        postInstructions.put(currentClient, new ArrayList<DocClass>());
                        if (hashOfPostInstructions.containsKey(clientId)) {
                            ArrayList<String> postInstructionsStr = hashOfPostInstructions.get(clientId);
                            for (int p = 0; p < postInstructionsStr.size(); p++) {
                                ArrayList<DocClass> docs = postInstructions.get(currentClient);
                                String fullName = URLEncoder.encode(postInstructionsStr.get(p), "UTF-8");
                                DocClass docClass = new DocClass();
                                docClass.setClientName(currentClient.getClientName());
                                try {
                                    docClass.setDocName(URLDecoder.decode(fullName.substring(fullName.lastIndexOf("%2F") + 3), "UTF-8"));
                                } catch (Exception exe) {
                                    docClass.setDocName(fullName.substring(fullName.lastIndexOf("%2F") + 3));
                                }
                                docClass.setFullDocName(fullName);
                                docClass.setClientId(currentClient.getClientId());
                                docs.add(docClass);
                            }
                        }
                    }
                }
            }
        } catch (Exception exe) {
            exe.printStackTrace();
        }
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

    /**
     * @return the postInstructions
     */
    public HashMap<Client, ArrayList<DocClass>> getPostInstructions() {
        return postInstructions;
    }

    /**
     * @param postInstructions the postInstructions to set
     */
    public void setPostInstructions(HashMap<Client, ArrayList<DocClass>> postInstructions) {
        this.postInstructions = postInstructions;
    }

    /**
     * @return the selectedPostInstruction
     */
    public String getSelectedPostInstruction() {
        return selectedPostInstruction;
    }

    /**
     * @param selectedPostInstruction the selectedPostInstruction to set
     */
    public void setSelectedPostInstruction(String selectedPostInstruction) {
        this.selectedPostInstruction = selectedPostInstruction;
    }

}
