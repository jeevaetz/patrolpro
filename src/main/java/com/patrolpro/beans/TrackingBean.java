/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;

/**
 *
 * @author user
 */
@Named("trackingBean")
@ViewScoped
public class TrackingBean implements Serializable {

    private Integer activeTab;

    public TrackingBean() {
    }

    public void onTabChange(TabChangeEvent event) {
        TabView tabView = (TabView)event.getComponent();
        activeTab = tabView.getActiveIndex();
    }

    /**
     * @return the activeTab
     */
    public Integer getActiveTab() {
        return activeTab;
    }

    /**
     * @param activeTab the activeTab to set
     */
    public void setActiveTab(Integer activeTab) {
        this.activeTab = activeTab;
    }
}
