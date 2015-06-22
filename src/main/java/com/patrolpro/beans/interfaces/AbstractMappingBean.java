/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans.interfaces;

import com.patrolpro.beans.ClientSessionBean;
import java.io.Serializable;
import schedfoxlib.model.Client;

/**
 *
 * @author ira
 */
public class AbstractMappingBean implements Serializable {
    
    private Double latitude = new Double(32.46);
    private Double longitude = new Double(96.46);
    
    public void refreshMap(ClientSessionBean sessionBean) {
        try {
            double latitude = 0.0;
            double longitude = 0.0;
            int numberOfValidEntries = 0;

            for (int c = 0; c < sessionBean.getClients().size(); c++) {
                Client currClient = sessionBean.getClients().get(c);
                try {
                    if (currClient.getAddressObj().getLatitude() > -1) {
                        latitude += currClient.getAddressObj().getLatitude();
                        longitude += currClient.getAddressObj().getLongitude();
                        numberOfValidEntries++;
                    }
                } catch (Exception e) {
                }
            }
            this.latitude = latitude / numberOfValidEntries;
            this.longitude = longitude / numberOfValidEntries;
        } catch (Exception e) {
        }

    }

    /**
     * @return the latitude
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
