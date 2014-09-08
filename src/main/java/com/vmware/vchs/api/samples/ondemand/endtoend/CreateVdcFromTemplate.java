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
import com.vmware.vcloud.api.rest.schema_v1_5.LinkType;
import com.vmware.vcloud.api.rest.schema_v1_5.OrgListType;
import com.vmware.vcloud.api.rest.schema_v1_5.ReferenceType;
import com.vmware.vcloud.api.rest.schema_v1_5.TaskType;
/**
 * CreateVdcFromTemplate
 * 
 * This sample will demonstrate how to create a VDC from a VDC template.
 *  
 * Using the ListVdcTemplates helper class will provide the list of templates you can
 * use to create a VDC from.
 * 
 * Parameters:
 * 
 * hostname        [required] : url of the vCHS onDeamn web service
 * username        [required] : username for the vCHS OnDemand authentication
 * password        [required] : password for the vCHS OnDemand authentication
 * version         [required] : version of the vCHS OnDemand API
 * region          [required] : vCHS region the VDC should be created in
 * vdctemplatename [required] : the name of the VDC template to use for creating the VDC from
 * 
 * Argument Line:
 * 
 * --hostname [vCHS webservice url] --username [vCHS username] --password [vCHS password] 
 * --version [vCHS API version] --region [vCHS region]
 * --vdctempltaename [Compute Service VDC template name]
 */
public class CreateVdcFromTemplate {
    private SampleCommandLineOptions options = null;
    private String authToken = null;

    public static void main(String[] args) {
        CreateVdcFromTemplate instance = new CreateVdcFromTemplate();
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

        System.out.println("Success\n");

        // Get a list of any instances already created for the logged in user
        Collection<InstanceType> instances = ServiceController.getInstances(options.hostname,
                options.version, authToken);

        // Loop through all available instances; match the instance region to the command line
        // options.region
        System.out.print("Looking for an instance with a matching region to " + options.region
                + "...");
        for (InstanceType instance : instances) {
            if (instance.getRegion().equalsIgnoreCase(options.region)) {
                System.out.println("Found.\n");

                // Use Gson to convert the JSON String into an instance of InstanceAttribute
                Gson gson = new Gson();
                InstanceAttribute ia = gson.fromJson(instance.getInstanceAttributes(),
                        InstanceAttribute.class);

                // Log in to compute retrieving the auth token in response to be used
                // in subsequent requests to compute.
                System.out.print("Logging in to compute service...");
                String computeAuthToken = Compute.login(ia.getSessionUri(), options.username,
                        options.password, ia.getOrgName(), options.version);
                if (null != computeAuthToken) {
                    System.out.println("Success.\n");

                    // Retrieve the org details using the service provided API url
                    System.out.print("Retrieving Org details...");
                    OrgListType org = Compute.getOrgDetails(instance.getApiUrl(), computeAuthToken,
                            options.version);
                    if (null != org) {
                        System.out.println("Success.\n");

                        String vdcTemplateHref = null;
                        String vdcTemplateInstantiateHref = null;

                        // Loop through the Org links looking for the
                        // application/vnd.vmware.vcloud.instantiateVdcTemplateParams+xml
                        // and the application/vnd.vmware.admin.vdcTemplates+xml link types. Save
                        // both HREF's.
                        for (LinkType link : org.getLink()) {

                            if (link.getType().equalsIgnoreCase(
                                    "application/vnd.vmware.admin.vdcTemplates+xml")) {
                                vdcTemplateHref = link.getHref();
                            } else if (link
                                    .getType()
                                    .equalsIgnoreCase(
                                            "application/vnd.vmware.vcloud.instantiateVdcTemplateParams+xml")) {
                                vdcTemplateInstantiateHref = link.getHref();
                            }
                        }

                        // If both link types were found, we can get the collection of
                        // VDC templates, find the matching template name, then create a new
                        // VDC from the template.
                        if (null != vdcTemplateHref && null != vdcTemplateInstantiateHref) {

                            // Attempt to find a matching VDC template with a name matching
                            // that of the provided options.vdctemplatename.
                            System.out.print("Looking for a matching VDC template with name "
                                    + options.vdctemplatename + "...");
                            ReferenceType vdcTemplateRef = Compute.findVdcTemplateByName(
                                    vdcTemplateHref, options.vdctemplatename, computeAuthToken,
                                    options.version);

                            if (null != vdcTemplateRef) {
                                System.out.println("Found.\n");
                                TaskType task = Compute.createVdcFromVdcTemplate(vdcTemplateRef,
                                        vdcTemplateInstantiateHref, computeAuthToken,
                                        options.version, "NewVDCName", "New VDC Description");

                                if (null != task) {
                                    System.out.print("Waiting for VDC creation to complete...");
                                    Compute.waitForTaskCompletion(task,  options.version,  computeAuthToken,  10);
                                    System.out.println("Created.\n");
                                }
                            } else {
                                System.out.println("Not found.\n");
                            }
                        }
                    }
                } else {
                    System.out.println("Failed.\n");
                }

                break;
            }

            System.out.println("didn't find a matching region.");
        }
    }
}