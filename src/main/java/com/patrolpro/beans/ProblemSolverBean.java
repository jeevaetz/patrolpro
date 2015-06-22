/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import schedfoxlib.model.Client;
import schedfoxlib.model.Problemsolver;
import schedfoxlib.model.User;
import schedfoxlib.services.ProblemSolverService;
import schedfoxlib.services.UserService;

/**
 *
 * @author user
 */
@Named("problemSolverBean")
@ViewScoped
public class ProblemSolverBean implements Serializable {
    
    @Inject
    private ClientSessionBean sessionBean;

    private ArrayList<Problemsolver> problemSolvers;
    private Problemsolver selectedProblemSolver;
    private ArrayList<User> problemUsers;
    private Date selectedDate;
    
    private SelectItem[] userSelection;
    
    public ProblemSolverBean() {
        
    }
    
    @PostConstruct
    public void postConstruct() {
        try {
            ProblemSolverService problemService = new ProblemSolverService();
            problemSolvers = problemService.getProblemsForClient(sessionBean.getSelectedClient().getClientId());
        } catch (Exception exe) {
            exe.printStackTrace();
        }
        
        this.refresh();
    }
    
    public User getUserById(Integer userId) {
        User user = new User();
        for (int u = 0; u < problemUsers.size(); u++) {
            if (userId.equals(problemUsers.get(u).getId())) {
                user = problemUsers.get(u);
            }
        }
        return user;
    }
    
    public void refresh(){
        ProblemSolverService problemSolverService = new ProblemSolverService();
        UserService userService = new UserService();
        this.problemSolvers = new ArrayList<Problemsolver>();
        setProblemUsers(new ArrayList<User>());
        try {
            for (int c = 0; c < sessionBean.getClients().size(); c++) {
                Client currentClient = sessionBean.getClients().get(c);

                ArrayList<Problemsolver> problems = problemSolverService.getProblemsForClient(currentClient.getClientId());
                for (int p = 0; p < problems.size(); p++) {
                    if (!getProblemSolvers().contains(problems.get(p))) {
                        getProblemSolvers().add(problems.get(p));
                    }
                }
            }

            for (int p = 0; p < getProblemSolvers().size(); p++) {
                boolean hasUser = false;
                for (int u = 0; u < getProblemUsers().size(); u++) {
                    if (getProblemUsers().get(u).getUserId().equals(getProblemSolvers().get(p).getUserId())) {
                        hasUser = true;
                    }
                }
                if (hasUser == false) {
                    getProblemUsers().add(userService.getUserById(getProblemSolvers().get(p).getUserId()));
                }
            }
            
            userSelection = new SelectItem[getProblemUsers().size() + 1];
            userSelection[0] = new SelectItem("", "Select"); 
            for (int u = 0; u < getProblemUsers().size(); u++) {
                userSelection[u + 1] = new SelectItem(getProblemUsers().get(u), getProblemUsers().get(u).getFullName());
            }
        }catch(Exception e) {
            e.printStackTrace();
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
     * @return the selectedProblemSolver
     */
    public Problemsolver getSelectedProblemSolver() {
        return selectedProblemSolver;
    }

    /**
     * @param selectedProblemSolver the selectedProblemSolver to set
     */
    public void setSelectedProblemSolver(Problemsolver selectedProblemSolver) {
        this.selectedProblemSolver = selectedProblemSolver;
    }

    /**
     * @return the problemSolvers
     */
    public ArrayList<Problemsolver> getProblemSolvers() {
        return problemSolvers;
    }

    /**
     * @param problemSolvers the problemSolvers to set
     */
    public void setProblemSolvers(ArrayList<Problemsolver> problemSolvers) {
        this.problemSolvers = problemSolvers;
    }

    /**
     * @return the problemUsers
     */
    public ArrayList<User> getProblemUsers() {
        return problemUsers;
    }

    /**
     * @param problemUsers the problemUsers to set
     */
    public void setProblemUsers(ArrayList<User> problemUsers) {
        this.problemUsers = problemUsers;
    }

    /**
     * @return the userSelection
     */
    public SelectItem[] getUserSelection() {
        return userSelection;
    }

    /**
     * @param userSelection the userSelection to set
     */
    public void setUserSelection(SelectItem[] problemSelection) {
        this.userSelection = problemSelection;
    }
    
    /**
     * @return the selectedDate
     */
    public Date getSelectedDate() {
        return selectedDate;
    }

    /**
     * @param selectedDate the selectedDate to set
     */
    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }
}
