/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import com.patrolpro.reports.GenericReport;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author ira
 */
@FacesConverter(value = "reportConverter")
public class GenericReportConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String s) {
        ArrayList<GenericReport> reports = GenericReport.getReport();
        if (s.trim().equals("")) {
            return null;
        } else {
            for (GenericReport lt : reports) {
                if (lt.getReportName().equals(s)) {
                    return lt;
                }
            }
        }
        return reports.get(0);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object o) {
        if (o == null || o.equals("")) {
            return "";
        } else if (o instanceof String) {
            return (String) o;
        } else {
            return String.valueOf(((GenericReport) o).getReportName());
        }
    }

}
