/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans.admin;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import schedfoxlib.model.Client;
import schedfoxlib.model.ClientEquipment;
import schedfoxlib.model.ClientEquipmentContact;
import schedfoxlib.model.ClientEquipmentVendor;
import schedfoxlib.model.ClientEquipmentWithStats;
import schedfoxlib.model.CompanyObj;
import schedfoxlib.model.Employee;
import schedfoxlib.model.Equipment;
import schedfoxlib.model.MessagingCommunication;
import schedfoxlib.services.ClientService;
import schedfoxlib.services.CompanyService;
import schedfoxlib.services.EquipmentService;
import schedfoxlib.services.GPSService;
import schedfoxlib.services.GenericService;
import schedfoxlib.services.MessagingService;

/**
 *
 * @author ira
 */
@Named("adminDashboardBean")
@ViewScoped
public class AdminDashboardBean implements Serializable {

    @Inject
    private SessionBean sessionBean;
    
    private ArrayList<ClientEquipmentWithStats> clientEquipment;
    private ClientEquipment selectedEquipment;
    private ArrayList<String> deviceLegacyFilters;
    private String deviceLegacySelection;
    private String phoneNumber;
    private ArrayList<Client> clients;
    private ArrayList<Equipment> equipment;
    private ArrayList<ClientEquipmentContact> contacts;
    private ArrayList<ClientEquipmentWithStats> filteredClientEquipment;
    private ArrayList<ClientEquipmentVendor> equipmentVendors;
    private static String SHOW_ALL = "Show All";

    /**
     * Creates a new instance of AdminDashboardBean
     */
    public AdminDashboardBean() {
        
    }
    
    @PostConstruct
    public void postConstruct() {
        try {
            EquipmentService equipmentService = new EquipmentService(sessionBean.getCompanyId());
            filteredClientEquipment = new ArrayList<ClientEquipmentWithStats>();
            equipmentVendors = new ArrayList<ClientEquipmentVendor>();

            deviceLegacyFilters = new ArrayList<String>();
            deviceLegacyFilters.add(SHOW_ALL);
            ArrayList<Equipment> allEquipments = equipmentService.getEquipment();
            for (int e = 0; e < allEquipments.size(); e++) {
                deviceLegacyFilters.add(allEquipments.get(e).getEquipmentName());
            }
            

            clients = new ArrayList<Client>();
            if (deviceLegacyFilters.size() == 2) {
                deviceLegacySelection = deviceLegacyFilters.get(1);
            }
            deviceLegacySelection = SHOW_ALL;

            loadData();
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }
    

    public void sendLinks() throws Exception {
        CompanyService companyService = new CompanyService();
        CompanyObj obj = companyService.getCompanyObjById(Integer.parseInt(sessionBean.getCompanyId()));
        
        MessagingCommunication comm = new MessagingCommunication();
        comm.setAttachPdf(false);
        comm.setBody("http://www.schedfox.com/android/" + obj.getCompanyUrl() + "/" + "Patrol Pro v 2.0.apk".replaceAll(" ", "%20"));
        comm.setCcd("");
        comm.setEmployeeId(-1);
        comm.setIsEmail(false);
        comm.setIsSMS(true);
        comm.setSentTo(phoneNumber);
        comm.setUserId(sessionBean.getLoggedInUser().getId());
        
        MessagingService messageService = new MessagingService(sessionBean.getCompanyId());
        messageService.saveMessagingCommunication(comm);
    }
    
    private void loadData() throws Exception {
        EquipmentService equipmentService = new EquipmentService(sessionBean.getCompanyId());
        ClientService clientService = new ClientService(sessionBean.getCompanyId());
        clientEquipment = new ArrayList<ClientEquipmentWithStats>();
        clientEquipment = equipmentService.getClientEquipmentWithStats(-1, -1);

        equipmentVendors = equipmentService.getClientEquipmentVendor();

        equipment = equipmentService.getEquipment();
        clients = clientService.getActiveClients();

        HashMap<Integer, Client> clientHash = new HashMap<Integer, Client>();
        for (int e = 0; e < clientEquipment.size(); e++) {
            ClientEquipmentWithStats currEquip = clientEquipment.get(e);
            if (clientHash.get(currEquip.getClientEquipment().getClientId()) == null && currEquip.getClient() == null) {
                boolean foundClient = false;
                for (int c = 0; c < clients.size(); c++) {
                    if (clients.get(c).getClientId().equals(clientEquipment.get(e).getClientEquipment().getClientId())) {
                        foundClient = true;
                        clientHash.put(currEquip.getClientEquipment().getClientId(), clients.get(c));
                    }
                }
                if (!foundClient) {
                    clientHash.put(currEquip.getClientEquipment().getClientId(), clientService.getClientById(currEquip.getClientEquipment().getClientId()));
                }
            } else if (currEquip.getClient() != null) {
                clientHash.put(currEquip.getClientEquipment().getClientId(), currEquip.getClient());
            }
            currEquip.getClientEquipment().setEntity(clientHash.get(currEquip.getClientEquipment().getClientId()));
            currEquip.getClientEquipment().setContacts(null);
        }
        Collections.sort(clientEquipment, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof ClientEquipmentWithStats && o2 instanceof ClientEquipmentWithStats) {
                    return ((ClientEquipmentWithStats) o1).getClientEquipment().getEntity().getClientName().compareTo(((ClientEquipmentWithStats) o2).getClientEquipment().getEntity().getClientName());
                }
                return 1;
            }
        });
        
        filterSelection();
    }

    public String getContactString(ClientEquipment clientEquipment) {
        String returnValue = "Never contacted server!";
        if (clientEquipment.getContacts(sessionBean.getCompanyId() + "").size() > 0) {
            SimpleDateFormat myFormat = new SimpleDateFormat("MM/dd/yy HH:mm");
            returnValue = myFormat.format(clientEquipment.getContacts(sessionBean.getCompanyId() + "").get(0).getContactTime());
        }
        return returnValue;
    }
    
    public boolean hasGPS(ClientEquipment clientEquipment) {
        boolean retVal = false;
        if (clientEquipment.getContacts(sessionBean.getCompanyId() + "").size() > 0) {
            retVal = clientEquipment.getContacts(sessionBean.getCompanyId() + "").get(0).getGpson();
        }
        return retVal;
    }
    
    public boolean hasNetwork(ClientEquipment clientEquipment) {
        boolean retVal = false;
        if (clientEquipment.getContacts(sessionBean.getCompanyId() + "").size() > 0) {
            retVal = clientEquipment.getContacts(sessionBean.getCompanyId() + "").get(0).getNetworkLocationOn();
        }
        return retVal;
    }
    
    public void loadContactsForEquipment(ClientEquipment selectedEquipment) {
        this.setSelectedEquipment(selectedEquipment);
        contacts = this.selectedEquipment.getContacts(sessionBean.getCompanyId());
    }
     
    /**
     * @param selectedEquipment the selectedEquipment to set
     */
    public void setSelectedEquipment(ClientEquipment selectedEquipment) {
        this.selectedEquipment = selectedEquipment;
        this.selectedEquipment.setContacts(null);
    }
    
    public boolean isDeviceOn(ClientEquipment clientEquipment) {
        boolean retVal = true;
        if (clientEquipment.getContacts(sessionBean.getCompanyId() + "").size() > 0) {
            ArrayList<ClientEquipmentContact> contacts = clientEquipment.getContacts(sessionBean.getCompanyId() + "");
            for (int i = 0; i < contacts.size(); i++) {
                if (contacts.get(i).getPowerOn()) {
                    break;
                }
                if (contacts.get(i).getPowerOff()) {
                    retVal = false;
                    break;
                }
            }
        }
        return retVal;
    }
    
    public String getPercentage(ClientEquipment clientEquipment) {
        String retVal = "Unknown";
        if (clientEquipment.getContacts(sessionBean.getCompanyId() + "").size() > 0) {
            try {
                DecimalFormat df = new DecimalFormat("#.##%");
                retVal = df.format(clientEquipment.getContacts(sessionBean.getCompanyId() + "").get(0).getBatteryPercentage() / 100);
            } catch (Exception exe) {}
        }
        return retVal;
    }
    
    public String getSignalPercentage(ClientEquipment clientEquipment) {
        String retVal = "Unknown";
        if (clientEquipment.getContacts(sessionBean.getCompanyId() + "").size() > 0) {
            try {
                DecimalFormat df = new DecimalFormat("#.##%");
                retVal = df.format(clientEquipment.getContacts(sessionBean.getCompanyId() + "").get(0).getSignalPercentage() / 100);
            } catch (Exception exe) {}
        }
        return retVal;
    }
    
    public boolean hasAirplaneOn(ClientEquipment clientEquipment) {
        boolean retVal = false;
        if (clientEquipment.getContacts(sessionBean.getCompanyId() + "").size() > 0) {
            retVal = clientEquipment.getContacts(sessionBean.getCompanyId() + "").get(0).getInairplanemode();
        }
        return retVal;
    }
    
    public boolean hasGoogleServices(ClientEquipment clientEquipment) {
        boolean retVal = false;
        if (clientEquipment.getContacts(sessionBean.getCompanyId() + "").size() > 0) {
            retVal = clientEquipment.getContacts(sessionBean.getCompanyId() + "").get(0).getHasGooglePlayServices();
        }
        return retVal;
    }
    
    public String getVersion(ClientEquipment clientEquipment) {
        String retVal = "Unknown / Not Setup";
        if (clientEquipment.getContacts(sessionBean.getCompanyId() + "").size() > 0) {
            retVal = clientEquipment.getContacts(sessionBean.getCompanyId() + "").get(0).getVersion();
        }
        return retVal;
    }
    
    public Employee getLastEmployeeUsedDevice(Integer clientEquipmentId) {
        Employee emp = new Employee();
        try {
            GPSService gpsService = new GPSService(sessionBean.getCompanyId());
            emp = gpsService.getLastEmployeeThatUsedDevice(clientEquipmentId);
        } catch (Exception exe) {
        }
        return emp;
    }

    public void addNew() {
        this.selectedEquipment = new ClientEquipment();
    }

    public void save() {
        try {
            EquipmentService equipmentService = new EquipmentService(sessionBean.getCompanyId());
            try {
                if (selectedEquipment.getClientEquipmentId() == null) {
                    GenericService genericService = new GenericService();
                    this.selectedEquipment.setDateIssued(new Date(genericService.getCurrentTimeMillis()));
                }
            } catch (Exception exe) {
            }
            equipmentService.saveClientEquipment(selectedEquipment);
            loadData();
            
            RequestContext.getCurrentInstance().execute("$('#styledModal').modal('hide')");
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    public void onRowSelect(SelectEvent event) {
    }

    public void filterSelection() {
        filteredClientEquipment = new ArrayList<ClientEquipmentWithStats>();
        if (!deviceLegacySelection.equals(SHOW_ALL)) {
            ArrayList<Equipment> allEquipments = this.getEquipment();
            ArrayList<Integer> selectedEquipment = new ArrayList<Integer>();
            for (int a = 0; a < allEquipments.size(); a++) {
                if (allEquipments.get(a).getEquipmentName().equals(deviceLegacySelection)) {
                    selectedEquipment.add(allEquipments.get(a).getEquipmentId());
                }
            }
            for (int c = 0; c < clientEquipment.size(); c++) {
                for (int e = 0; e < selectedEquipment.size(); e++) {
                    if (selectedEquipment.get(e).equals(clientEquipment.get(c).getClientEquipment().getEquipmentId())) {
                        filteredClientEquipment.add(clientEquipment.get(c));
                    }
                }
            }
        } else if (deviceLegacySelection.equals(SHOW_ALL)) {
            filteredClientEquipment.addAll(clientEquipment);
        }
        for (int e = 0; e < filteredClientEquipment.size(); e++) {
            for (int equip = 0; equip < this.equipment.size(); equip++) {
                if (this.equipment.get(equip).getEquipmentId().equals(filteredClientEquipment.get(e).getClientEquipment().getEquipmentId())) {
                    filteredClientEquipment.get(e).getClientEquipment().setEquipment(this.equipment.get(equip));
                }
            }
        }
    }

    /**
     * @return the clientEquipment
     */
    public ArrayList<ClientEquipmentWithStats> getClientEquipment() {
        return filteredClientEquipment;
    }

    /**
     * @param clientEquipment the clientEquipment to set
     */
    public void setClientEquipment(ArrayList<ClientEquipmentWithStats> clientEquipment) {
        this.clientEquipment = clientEquipment;
    }

    /**
     * @return the sessionBean
     */
    public SessionBean getMySessionBean() {
        return sessionBean;
    }

    /**
     * @param mySessionBean the sessionBean to set
     */
    public void setMySessionBean(SessionBean mySessionBean) {
        this.sessionBean = mySessionBean;
    }

    /**
     * @return the selectedEquipment
     */
    public ClientEquipment getSelectedEquipment() {
        if (selectedEquipment == null) {
            selectedEquipment = new ClientEquipment();
            selectedEquipment.setEntity(new Client());
        }
        return selectedEquipment;
    }

    public void handleFileUpload(FileUploadEvent event) {
        FacesMessage msg = new FacesMessage("Successful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        try {
            HSSFWorkbook wb = new HSSFWorkbook(event.getFile().getInputstream());
            HSSFSheet sheet = wb.getSheetAt(0);

            EquipmentService equipmentService = new EquipmentService();
            ClientService clientService = new ClientService();
            
            boolean foundHeaders = false;
            for (int r = 0; r < sheet.getLastRowNum(); r++) {
                HSSFRow row1 = sheet.getRow(r);

                HSSFCell cellA1 = row1.getCell(0);
                HSSFCell cellB1 = row1.getCell(1);
                HSSFCell cellC1 = row1.getCell(2);
                HSSFCell cellD1 = row1.getCell(3);
                HSSFCell cellE1 = row1.getCell(4);
                HSSFCell cellF1 = row1.getCell(5);
                HSSFCell cellG1 = row1.getCell(6);
                HSSFCell cellH1 = row1.getCell(7);
                HSSFCell cellI1 = row1.getCell(8);
                HSSFCell cellJ1 = row1.getCell(9);
                HSSFCell cellK1 = row1.getCell(10);
                HSSFCell cellL1 = row1.getCell(11);
                HSSFCell cellM1 = row1.getCell(12);
                HSSFCell cellN1 = row1.getCell(13);
                
                try {
                    if (cellA1.getStringCellValue().equalsIgnoreCase("Account Number")) {
                        foundHeaders = true;
                    }
                } catch (Exception exe) {}

                if (foundHeaders) {
                    try {
                        String accountNumber = "";
                        String phoneNumber = "";
                        String user = "";
                        String plan = "";
                        String voice = "";
                        String data = "";
                        String text = "";
                        String cost = "";
                        String features = "";
                        String extraCosts = "";
                        String total = "";
                        String expiration = "";
                        String voiceCode = "";
                        String ID = "";
                        
                        if (cellA1 != null) {
                            accountNumber = cellA1.getStringCellValue();
                        }
                        if (cellB1 != null) {
                            phoneNumber = cellB1.getStringCellValue();
                        }
                        if (cellC1 != null) {
                            user = cellC1.getStringCellValue();
                        }
                        if (cellD1 != null) {
                            plan = cellD1.getStringCellValue();
                        }
                        if (cellE1 != null) {
                            voice = cellE1.getStringCellValue();
                        }
                        if (cellF1 != null) {
                            data = cellF1.getStringCellValue();
                        }
                        if (cellG1 != null) {
                            text = cellG1.getStringCellValue();
                        }
                        if (cellH1 != null) {
                            cost = cellH1.getStringCellValue();
                        }
                        if (cellI1 != null) {
                            features = cellI1.getStringCellValue();
                        }
                        if (cellJ1 != null) {
                            extraCosts = cellJ1.getStringCellValue();
                        }
                        if (cellK1 != null) {
                            total = cellK1.getStringCellValue();
                        }
                        if (cellL1 != null) {
                            expiration = cellL1.getStringCellValue();
                        }
                        if (cellM1 != null) {
                            voiceCode = cellM1.getStringCellValue();
                        }
                        if (cellN1 != null) {
                            ID = cellN1.getStringCellValue();
                        }
                        //clientService.getClie

                        ClientEquipment clientEquip = new ClientEquipment();
                        if (ID != null && ID.length() > 0) {
                            clientEquip = equipmentService.getEquipmentByClientId(Integer.parseInt(ID));
                        }
                        clientEquip.setPhoneNumber(phoneNumber);

                        Client client = clientService.getClientByName(user);
                        if (client != null && client.getClientId() != 0) {
                            clientEquip.setClientId(client.getClientId());
                        }

                        equipmentService.saveClientEquipment(clientEquip);
                    } catch (Exception exe) {
                        System.out.println("Could not insert row");
                    }
                }
            }
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    /**
     * @return the deviceLegacyFilters
     */
    public ArrayList<String> getDeviceLegacyFilters() {
        return deviceLegacyFilters;
    }

    /**
     * @param deviceLegacyFilters the deviceLegacyFilters to set
     */
    public void setDeviceLegacyFilters(ArrayList<String> deviceLegacyFilters) {
        this.deviceLegacyFilters = deviceLegacyFilters;
    }

    /**
     * @return the deviceLegacySelection
     */
    public String getDeviceLegacySelection() {
        return deviceLegacySelection;
    }

    /**
     * @param deviceLegacySelection the deviceLegacySelection to set
     */
    public void setDeviceLegacySelection(String deviceLegacySelection) {
        this.deviceLegacySelection = deviceLegacySelection;
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
     * @return the equipment
     */
    public ArrayList<Equipment> getEquipment() {
        return equipment;
    }

    /**
     * @param equipment the equipment to set
     */
    public void setEquipment(ArrayList<Equipment> equipment) {
        this.equipment = equipment;
    }

    /**
     * @return the equipmentVendors
     */
    public ArrayList<ClientEquipmentVendor> getEquipmentVendors() {
        return equipmentVendors;
    }

    /**
     * @param equipmentVendors the equipmentVendors to set
     */
    public void setEquipmentVendors(ArrayList<ClientEquipmentVendor> equipmentVendors) {
        this.equipmentVendors = equipmentVendors;
    }

    /**
     * @return the phoneNumber
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @param phoneNumber the phoneNumber to set
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * @return the contacts
     */
    public ArrayList<ClientEquipmentContact> getContacts() {
        return contacts;
    }

    /**
     * @param contacts the contacts to set
     */
    public void setContacts(ArrayList<ClientEquipmentContact> contacts) {
        this.contacts = contacts;
    }
}
