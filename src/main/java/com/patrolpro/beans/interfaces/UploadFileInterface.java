/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.beans.interfaces;

/**
 *
 * @author ira
 */
public interface UploadFileInterface {
    public String processFileUpload(String companyId, byte[] imageData, Object objectKey);
}
