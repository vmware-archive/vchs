package com.vmware.vchs.api.samples.ondemand.details;

import java.util.Collection;

import com.google.gson.Gson;
import com.vmware.vchs.api.samples.ondemand.endtoend.SampleCommandLineOptions;
import com.vmware.vchs.api.samples.services.Compute;
import com.vmware.vchs.api.samples.services.IAM;
import com.vmware.vchs.api.samples.services.ServiceController;
import com.vmware.vchs.api.samples.services.helper.InstanceAttribute;
import com.vmware.vchs.sc.instance.v1.InstanceType;
import com.vmware.vcloud.api.rest.schema_v1_5.ReferenceType;

/**
 * ListVdcTemplates
 * 
 * This helper class will provide the list of templates you can use to create a VDC from.
 * 
 * Parameters:
 * 
 * hostname [required]        : url of the vCHS onDeamn web service
 * username [required]        : username for the vCHS OnDemand authentication
 * password [required]        : password for the vCHS OnDemand authentication
 * version [required]         : version of the vCHS OnDemand API
 *
 * Argument Line:
 * 
 * --hostname [vCHS webservice url] --username [vCHS
 * username] --password [vCHS password] --version [vCHS API version]
 */
public class ListVdcTemplates {
    private SampleCommandLineOptions options = null;
    private String authToken = null;

    public static void main(String[] args) {
        ListVdcTemplates instance = new ListVdcTemplates();
        instance.go(args);
    }

    private void go(String[] args) {
        // Disable Java 7 SNI SSL handshake bug as outlined here:
        // (http://stackoverflow.com/questions/7615645/ssl-handshake-alert-unrecognized-name-error-since-upgrade-to-java-1-7-0)
        System.setProperty("jsse.enableSNIExtension", "false");

        options = new SampleCommandLineOptions();

        // process arguments
        options.parseOptions(args);

        // Log in to vCHS API, getting a session in response if login is successful
        System.out.print("\nConnecting to vCHS...");

        authToken = IAM
                .login(options.hostname, options.username, options.password, options.version);

        if (null != authToken) {
            System.out.println("Success\n");

            // Retrieve service controller instances available for authenticated user
            Collection<InstanceType> instances = ServiceController.getInstances(options.hostname,
                    options.version, authToken);

            if (null != instances && instances.size() > 0) {
                for (InstanceType instance : instances) {
                    Gson gson = new Gson();
                    InstanceAttribute ia = gson.fromJson(instance.getInstanceAttributes(),
                            InstanceAttribute.class);

                    // Log in to compute API
                    System.out
                            .print("Logging in to compute region " + instance.getRegion() + "...");
                    String vcdToken = Compute.login(ia.getSessionUri(), options.username,
                            options.password, ia.getOrgName(), options.version);

                    System.out.println("Success.\n");

                    // Get the collection of ReferenceType instances that are vdcTemplate types. The
                    // reason this returns a Collection<ReferenceType> instead of a
                    // Collection<VdcTemplateType> is because to create a VDC from a VDC Template
                    // you need the source reference that the VDC Template comes from. In this case,
                    // the ReferenceType HREF is the source. Returning just the collection of
                    // VdcTemplateTypes would still require another call to be made to get the
                    // ReferenceType match for the specific VdcTemplateType that a VDC Template is
                    // to be created from. There is no current compute API way to do this, so
                    // another call to the same API that this call makes, returning the list of
                    // ReferenceType objects would need to be iterated through to find a matching
                    // name again. Thus, this call returns the ReferenceType and we simply iterate
                    // through it looking for the matching options.vdctemplatename. The collection
                    // returned here is filtered so only ReferenceTypes that are actually
                    // VdcTemplateType objects are returned.
                    Collection<ReferenceType> vdcTemplates = Compute.getVdcTemplates(
                            instance.getApiUrl(), vcdToken, options.version);
                    if (null != vdcTemplates) {
                        System.out.printf("%-30s\n", "VDC Template Name");
                        System.out.printf("%-30s\n", "-----------------");
                        for (ReferenceType vdcTemplate : vdcTemplates) {
                            System.out.printf("%-30s\n", vdcTemplate.getName());
                        }
                    }

                    System.out.println();
                }
            } else {
                System.out.println("No compute instance found");
            }
        } else {
            System.out.println("Could not log in with provided credentials.\n");
        }
    }
}