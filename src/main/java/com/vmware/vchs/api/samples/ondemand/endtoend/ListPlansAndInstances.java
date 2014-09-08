/*
 * Copyright (c) 2014 VMware, Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.vmware.vchs.api.samples.ondemand.endtoend;

import java.util.Collection;
import java.util.List;

import com.vmware.vchs.api.samples.services.IAM;
import com.vmware.vchs.api.samples.services.ServiceController;
import com.vmware.vchs.sc.instance.v1.InstanceType;
import com.vmware.vchs.sc.service.v1.PlanType;

/**
 * ListPlansAndInstances
 * This sample will log in to OnDemand with the provided username and password, then make a request
 * to the ServiceController API to get any plans the logged in user has access to and display them.
 * It will then make another request to the ServiceController API to get any instances the logged
 * in user has access to and display them. It will use the default command line options
 * 
 * Parameters:
 * 
 * hostname    [required] : url of the vCHS onDeamn web service
 * username    [required] : username for the vCHS OnDemand authentication
 * password    [required] : password for the vCHS OnDemand authentication
 * version     [required] : version of the vCHS OnDemand API
 * 
 * Argument Line:
 * 
 * --hostname [vCHS API url] --username [vCHS username] --password [vCHS password] --version
 * [vCHS API version]
 */
public class ListPlansAndInstances {
    private SampleCommandLineOptions options = null;
    private String authToken = null;

    public static void main(String[] args) {
        ListPlansAndInstances instance = new ListPlansAndInstances();
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

        authToken = IAM
                .login(options.hostname, options.username, options.password, options.version);

        System.out.println("Success\n");

        // Retrieve the collection of compute services which can be of type dedicated cloud or vpc
        // and has VDC in it.
        Collection<PlanType> plans = ServiceController.getPlans(options.hostname, options.version,
                authToken);
        if (null != plans && plans.size() > 0) {
            System.out.println("PLANS");
            System.out.println("-----");
            System.out.println();

            System.out.printf("%-40s %-40s %-50s %-30s %-20s\n", "Name", "Id", "Region",
                    "Service Name", "Plan Version");
            System.out.printf("%-40s %-40s %-50s %-30s %-20s\n",
                    "----------------------------------------",
                    "----------------------------------------",
                    "--------------------------------------------------",
                    "------------------------------", "-------------------");

            for (PlanType plan : plans) {
                System.out.printf("%-40s %-40s %-50s %-30s %-20s\n", plan.getName(), plan.getId(),
                        plan.getRegion(), plan.getServiceName(), plan.getPlanVersion());
            }
        }

        Collection<InstanceType> instances = ServiceController.getInstances(options.hostname,
                options.version, authToken);
        System.out.println("\n");

        if (null != instances && instances.size() > 0) {
            System.out.println("INSTANCES");
            System.out.println("---------");
            System.out.println();

            System.out.printf("%-40s %-40s %-50s %-50s %-50s\n", "Name", "Id", "Region", "Plan Id",
                    "Api Url");
            System.out.printf("%-40s %-40s %-50s %-50s %-50s\n",
                    "----------------------------------------",
                    "----------------------------------------",
                    "--------------------------------------------------",
                    "--------------------------------------------------",
                    "--------------------------------------------------");

            for (InstanceType instance : instances) {
                System.out.printf("%-40s %-40s %-50s %-50s %-50s\n", instance.getName(),
                        instance.getId(), instance.getRegion(), instance.getPlanId(), instance.getApiUrl());
            }
        }

        System.out.println("\n\n");
    }
}