/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.init;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import schedfoxlib.controller.registry.ControllerRegistryAbstract;
import schedfoxlib.services.*;

/**
 *
 * @author user
 */
public class PatrolProInit implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        //ControllerRegistryAbstract.setBillingController(BillingService.class);
        ControllerRegistryAbstract.setClientContractController(ClientContractService.class);
        ControllerRegistryAbstract.setClientController(ClientService.class);
        //ControllerRegistryAbstract.setCompanyController(CompanyService.class);
        ControllerRegistryAbstract.setEmployeeController(EmployeeService.class);
        ControllerRegistryAbstract.setGenericController(GenericService.class);
        ControllerRegistryAbstract.setProblemSolverInterface(ProblemSolverService.class);
        //ControllerRegistryAbstract.setTimeOffController(TimeOffService.class);
        ControllerRegistryAbstract.setUserController(UserService.class);
        ControllerRegistryAbstract.setIncidentReportInterface(IncidentService.class);
        ControllerRegistryAbstract.setScheduleInterface(ScheduleService.class);
        ControllerRegistryAbstract.setOfficerDailyReportInterface(OfficerDailyReportService.class);
        ControllerRegistryAbstract.setEquipmentInterface(EquipmentService.class);
        ControllerRegistryAbstract.setGPSInterface(GPSService.class);
        
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        
    }
    
}
