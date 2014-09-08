package com.vmware.vchs.api.samples.ondemand.details;

import java.util.Collection;

import com.google.gson.Gson;
import com.vmware.vchs.api.samples.SampleConstants;
import com.vmware.vchs.api.samples.ondemand.endtoend.SampleCommandLineOptions;
import com.vmware.vchs.api.samples.services.Compute;
import com.vmware.vchs.api.samples.services.IAM;
import com.vmware.vchs.api.samples.services.ServiceController;
import com.vmware.vchs.api.samples.services.helper.InstanceAttribute;
import com.vmware.vchs.sc.instance.v1.InstanceType;
import com.vmware.vcloud.api.rest.schema_v1_5.OrgListType;
import com.vmware.vcloud.api.rest.schema_v1_5.VAppTemplateType;
import com.vmware.vcloud.api.rest.schema_v1_5.VdcType;

/**
 * ListVdcTemplates
 * 
 * This helper class will list all VM templates for a specified VDC. The --vdcname command line
 * argument provides the VDC to find VM templates within.
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
 * --hostname [vCHS webservice url] --username [vCHS username] --password [vCHS password] 
 * --version [vCHS API version]
 */
public class ListVmTemplates {
    private SampleCommandLineOptions options = null;
    private String authToken = null;

    public static void main(String[] args) {
        ListVmTemplates instance = new ListVmTemplates();
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
            Collection<InstanceType> instances = ServiceController.getInstancesForServiceType(
                    options.hostname, options.version, authToken,
                    SampleConstants.COMPUTE_SERVICE_TYPE);

            if (null != instances) {
                for (InstanceType instance : instances) {
                    Gson gson = new Gson();
                    InstanceAttribute ia = gson.fromJson(instance.getInstanceAttributes(),
                            InstanceAttribute.class);

                    // Log in to compute API
                    System.out.print("Logging in to compute region " + instance.getRegion() + "...");
                    String vcdToken = Compute.login(ia.getSessionUri(), options.username,
                            options.password, ia.getOrgName(), options.version);

                    System.out.println("Success.\n");

                    OrgListType org = Compute.getOrgDetails(instance.getApiUrl(), vcdToken,
                            options.version);

                    if (null != org) {
                        // Get the collection of VDCs for the Org
                        Collection<VdcType> vdcs = Compute.getVDCsForOrgs(org, vcdToken,
                                options.version);

                        if (null != vdcs) {
                            for (VdcType vdc : vdcs) {
                                // Retrieve templates for this VDC
                                Collection<VAppTemplateType> templates = Compute
                                        .getTemplatesForVdc(ia.getSessionUri(), vdc,
                                                options.version, vcdToken);

                                System.out.printf("%-30s %-30s\n", "VDC Name", "Template name");
                                System.out.printf("%-30s %-30s\n", "--------", "-------------");
                                for (VAppTemplateType template : templates) {
                                    System.out.printf("%-30s %-30s\n", vdc.getName(),
                                            template.getName());
                                }
                            }
                        }
                    }

                    System.out.println();
                }
            }
        } else {
            System.out.println("Could not log in with provided credentials.\n");
        }
    }
}
