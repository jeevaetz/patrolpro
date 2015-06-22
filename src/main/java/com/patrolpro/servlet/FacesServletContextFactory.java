/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.servlet;

import javax.faces.FactoryFinder;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author ira
 */
public class FacesServletContextFactory {
    
    private static FacesServletContextFactory instance;
    
    private FacesServletContextFactory() {
        
    }
    
    public static FacesServletContextFactory getInstance() {
        if (instance == null) {
            instance = new FacesServletContextFactory();
        }
        return instance;
    }
    
    public FacesContext getFacesContext(
            HttpServletRequest request, HttpServletResponse response) {
        // Get current FacesContext.
        FacesContext facesContext = FacesContext.getCurrentInstance();

        // Check current FacesContext.
        if (facesContext == null) {

            // Create new Lifecycle.
            LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

            // Create new FacesContext.
            javax.faces.context.FacesContextFactory contextFactory = (javax.faces.context.FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
            facesContext = contextFactory.getFacesContext(
                    request.getSession().getServletContext(), request, response, lifecycle);

            // Create new View.
            UIViewRoot view = facesContext.getApplication().getViewHandler().createView(
                    facesContext, "");
            facesContext.setViewRoot(view);

            // Set current FacesContext.
            //FacesContextWrapper.setCurrentInstance(facesContext);
        }

        return facesContext;
    }
}
