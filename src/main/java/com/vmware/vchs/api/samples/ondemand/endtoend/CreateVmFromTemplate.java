/*
 * Copyright (c) 2014 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License.    You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,  WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.vmware.vchs.api.samples.ondemand.endtoend;

import java.util.Collection;

import com.google.gson.Gson;
import com.vmware.vchs.api.samples.services.Compute;
import com.vmware.vchs.api.samples.services.IAM;
import com.vmware.vchs.api.samples.services.ServiceController;
import com.vmware.vchs.api.samples.services.helper.InstanceAttribute;
import com.vmware.vchs.sc.instance.v1.InstanceType;
import com.vmware.vcloud.api.rest.schema_v1_5.OrgListType;
import com.vmware.vcloud.api.rest.schema_v1_5.VAppTemplateType;
import com.vmware.vcloud.api.rest.schema_v1_5.VAppType;
import com.vmware.vcloud.api.rest.schema_v1_5.VdcType;
/**
 * CreateVMFromTemplate
 * 
 * This sample will demonstrate how to create a VM in a VDC from a VDC VM template.
 *
 * Using the ListVmTemplates helper class will provide the list of templates you can
 * use to create a VM from.
 * 
 * Parameters:
 * 
 * hostname       [required] : url of the vCHS onDeamn web service
 * username       [required] : username for the vCHS OnDemand authentication
 * password       [required] : password for the vCHS OnDemand authentication
 * version        [required] : version of the vCHS OnDemand API
 * region         [required] : vCHS region the VDC should be created in
 * vdcname        [required] : the name of the VDC where the VM will be created to
 * vmtemplatename [required] : the name of the VDC VM template to use for creating the VM from
 * vmname         [required] : the name of the VM to create in the provided VDC
 *
 * Argument Line:
 * 
 * --hostname [vCHS webservice url] --username [vCHS username] --password [vCHS password] 
 * --version [vCHS API version] --region [vCHS region]
 *  --vmtempltaename [Compute Service VDC template name]
 */
public class CreateVmFromTemplate {
    private SampleCommandLineOptions options = null;
    private String authToken = null;
    private VAppTemplateType matchedTemplate = null;

    public static void main(String[] args) {
        CreateVmFromTemplate instance = new CreateVmFromTemplate();
        instance.go(args);
    }

    private void go(String[] args) {
        // Disable Java 7 SNI SSL handshake bug as outlined here:
        // (http://stackoverflow.com/questions/7615645/ssl-handshake-alert-unrecognized-name-error-since-upgrade-to-java-1-7-0)
        System.setProperty("jsse.enableSNIExtension", "false");

        // process command line arguments
        options = new SampleCommandLineOptions();
        options.parseOptions(args);

        // Log in to vCHS API, getting a session in response if login is successful
        System.out.print("\nConnecting to vCHS...");

        authToken = IAM.login(options.hostname, options.username, options.password, options.version);

        if (null != authToken) {
            System.out.println("Success\n");

            // Retrieve service controller instances available for authenticated user
            Collection<InstanceType> instances = ServiceController.getInstances(options.hostname,
                    options.version, authToken);
            InstanceType computeInstance = null;
            if (null != instances && instances.size() > 0) {
                System.out
                        .print("Searching for matching instance region " + options.region + "...");

                for (InstanceType instance : instances) {
                    if (instance.getRegion().toLowerCase().equalsIgnoreCase(options.region)) {
                        computeInstance = instance;
                        System.out.println("Success.\n");
                        break;
                    }
                }
            }

            if (null != computeInstance) {
                Gson gson = new Gson();
                InstanceAttribute ia = gson.fromJson(computeInstance.getInstanceAttributes(),
                        InstanceAttribute.class);

                // Log in to compute API
                System.out.print("Logging in to compute...");

                String vcdToken = Compute.login(ia.getSessionUri(), options.username,
                        options.password, ia.getOrgName(), options.version);

                if (null != vcdToken) {
                    System.out.println("Success.\n");

                    // Retrieve the Org details for the logged in credentials
                    OrgListType org = Compute.getOrgDetails(computeInstance.getApiUrl(), vcdToken,
                            options.version);

                    if (null != org) {
                        // Retrieve any accessible VDCs for this Org.
                        Collection<VdcType> vdcs = Compute.getVDCsForOrgs(org, vcdToken,
                                options.version);

                        if (null != vdcs && vdcs.size() > 0) {
                            for (VdcType vdc : vdcs) {
                                // search for VDC that matches one we're looking for.
                                if (vdc.getName().equalsIgnoreCase(options.vdcname)
                                        || vdc.getId().toLowerCase()
                                                .equalsIgnoreCase(options.vdcid)) {
                                    // we got it, so lets create a VM
                                    System.out.println("Found.\n");

                                    // First we need to pull a template
                                    Collection<VAppTemplateType> templates = Compute
                                            .getTemplatesForVdc(computeInstance.getApiUrl(), vdc,
                                                    options.version, vcdToken);

                                    System.out.print("Searching for VM template with name " + options.vmtemplatename);
                                    if (null != templates && templates.size() > 0) {
                                        for (VAppTemplateType template : templates) {
                                            if (template.getName().toLowerCase()
                                                    .equalsIgnoreCase(options.vmtemplatename)) {
                                                matchedTemplate = template;
                                            }
                                        }

                                        // If we found a matching template proceed
                                        if (null != matchedTemplate) {
                                            System.out.println("Found.\n");

                                            VAppType vapp = Compute.createVmFromTemplate(vdc,
                                                    matchedTemplate, options.vmname,
                                                    options.version, vcdToken);
                                            if (null != vapp) {
                                                // VApp is created, now we'll wait for it's TaskType
                                                // status to be completed
                                                System.out.print("Waiting for VM creation to complete...");
                                                Compute.waitForTasks(vapp.getTasks(),
                                                        options.version, vcdToken);
                                                System.out.println("Completed.\n");
                                            }
                                        } else {
                                            System.out.println("No match found.\n");
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("Failed.\n");
                }
            } else {
                System.out.println("no matching instance found.\n");
            }
        }
    }
}