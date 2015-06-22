/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ManagedBean;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author user
 */
@ManagedBean(name = "imageBean")
@SessionScoped 
public class ImageBean implements Serializable {
    public StreamedContent bytesToStreamedContent(byte[] bytes) {
        InputStream is = new ByteArrayInputStream(bytes);
        StreamedContent image = new DefaultStreamedContent(is, "image/png");
        return image;
    }
}
