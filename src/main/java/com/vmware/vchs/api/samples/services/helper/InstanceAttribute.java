package com.vmware.vchs.api.samples.services.helper;
/**
 * This is a helper class to allow a ServiceController InstanceType InstanceAttributes to be
 * converted from the JSON string to this object instance. This class will likely be removed once
 * Compute service works with OAUTH tokens as the login will no longer be required.
 * 
 * @deprecated
 */
public class InstanceAttribute {
    private String orgName;
    private String sessionUri;

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getSessionUri() {
        return sessionUri;
    }

    public void setSessionUri(String sessionUri) {
        this.sessionUri = sessionUri;
    }
}