package com.sky.opam.model;

import java.util.Date;

import com.loic.common.sqliteTool.Column;
import com.loic.common.sqliteTool.Model;
import com.sky.opam.service.IntHttpService.HttpServiceErrorEnum;

@Model(tableName = "ClassUpdateInfo")
public class ClassUpdateInfo
{
    public int year;
    public int month;
    @Column(length = 10)
    public String login;
    public Date lastSuccessUpdateDate;
    public int classNumber;
    public int totalTime; //in second
    
    public Date lastFailUpdateDate;
    public HttpServiceErrorEnum errorEnum;
    
    public ClassUpdateInfo()
    {
    }
    
    public ClassUpdateInfo(String login) 
    {
        this.login = login;
    }

    @Override
    public String toString() 
    {
        return "ClassUpdateInfo [year=" + year + ", month=" + month
                + ", login=" + login + ", lastSuccessUpdateDate="
                + lastSuccessUpdateDate + ", classNumber=" + classNumber
                + ", lastFailUpdateDate=" + lastFailUpdateDate + ", errorEnum="
                + errorEnum + "]";
    }
}
