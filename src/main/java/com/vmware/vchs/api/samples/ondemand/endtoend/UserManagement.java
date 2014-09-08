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

import java.util.List;
import java.util.Random;

import org.apache.http.HttpStatus;

import com.vmware.vchs.api.samples.services.IAM;
import com.vmware.vchs.iam.v2.ObjectFactory;
import com.vmware.vchs.iam.v2.Role;
import com.vmware.vchs.iam.v2.Roles;
import com.vmware.vchs.iam.v2.SdpInstance;
import com.vmware.vchs.iam.v2.User;
import com.vmware.vchs.iam.v2.Users;
/**
 * UserManagement
 * 
 * This end to end sample will demonstrate how to use the User management aspect of
 * IAM. It will log in, retrieve all users, get the details of a single user, get the logged in
 * user's *self* details, create a new user, update the user's name, delete the user and then try to
 * retrieve the deleted user.
 * 
 * Parameters:
 * 
 * hostname [required] : url of the vCHS onDeamn web service
 * username [required] : username for the vCHS OnDemand authentication
 * password [required] : password for the vCHS OnDemand authentication
 * version  [required] : version of the vCHS OnDemand API
 * 
 * Argument Line:
 * 
 * --hostname [vCHS webservice url] --username [vCHS username] --password [vCHS password] 
 * --version [vCHS API version]
 */
public class UserManagement {
    private SampleCommandLineOptions options = null;
    private String authToken = null;

    public static void main(String[] args) {
        UserManagement instance = new UserManagement();
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

        if (null != authToken) {
            System.out.println("Success\n");

            // Retrieve the collection of compute services which can be of type dedicated cloud or
            // vpc and has VDC in it.
            System.out.print("Retrieving users...");

            // List all the users displaying their ids, usernames, family/given names, emails and
            // status
            Users allUsers = IAM.getUsers(options.hostname, authToken, options.version);
            if (null != allUsers && null != allUsers.getUsers() && allUsers.getUsers().size() > 0) {
                System.out.println("\n");

                System.out.printf("%-38s %-30s %-40s %-40s %-8s %-20s\n", "Id", "Username",
                        "Given & Family Name", "Email", "State", "Roles");
                System.out.printf("%-38s %-30s %-40s %-40s %-8s %-20s\n", "--", "--------",
                        "-------------------", "-----", "-----", "-----");
                for (User user : allUsers.getUsers()) {
                    Roles allRoles = user.getRoles();
                    List<Role> roles = allRoles.getRoles();
                    StringBuilder sb = new StringBuilder();
                    for (Role role : roles) {
                        sb.append(role.getName()).append(", ");
                    }

                    sb.deleteCharAt(sb.length() - 1);
                    sb.deleteCharAt(sb.length() - 1);

                    System.out.printf("%-38s %-30s %-40s %-40s %-8s %-20s\n", user.getId(),
                            user.getUserName(), user.getGivenName() + " " + user.getFamilyName(),
                            user.getEmail(), user.getState(), sb.toString());
                }
            }

            System.out.println();
            System.out
                    .println("There are a total of " + allUsers.getUsers().size() + " users.\n\n");
            System.out.print("Getting logged in user's details...");
            User self = null;
            for(User user : allUsers.getUsers()) {
                if (user.getUserName().equalsIgnoreCase(options.username)) {
                    self = user;
                    System.out.println("Found\n");
                    break;
                }
            }

            System.out.print("\nCreating new user...");
            Role role = new Role();
            role.setName("End User");

            User user = new ObjectFactory().createUser();
            int num = new Random().nextInt();
            user.setEmail("test_" + num + "@test.com");
            user.setState("Active");
            user.setUserName("test_" + num + "@test.com");
            user.setFamilyName("Test_FamilyName");
            user.getSchemas().add("urn:scim:schemas:core:1.0");
            user.setGivenName("Test_GivenName");
            user.setCompanyId(self.getCompanyId());
            Roles roles = new Roles();
            roles.getRoles().add(role);
            user.setRoles(roles);

            User newUser = IAM.createUser(options.hostname, authToken, user, options.version);
            if (null != newUser) {
                System.out.println("Success.\n");

                // Update user
                newUser.setFamilyName("NewFamilyName");

                System.out.print("Updating Family Name for new user...");
                boolean status = IAM.updateUser(options.hostname, authToken, newUser, options.version);
                if (status) {
                    System.out.println("Success.\n");
                    User updatedUser = IAM.getUser(options.hostname, authToken, newUser.getId(),
                            options.version);
                    if (null != updatedUser) {
                        System.out.println("Updated user family name is now "
                                + updatedUser.getFamilyName() + "\n");
                    }
                } else {
                    System.out.println("Failed.\n");
                }

                System.out.print("Deleting newly created user...");
                status = IAM.deleteUser(options.hostname, authToken, newUser.getId(),
                        options.version);
                if (status) {
                    System.out.println("Success.\n");
                } else {
                    System.out.println("Failed.\n");
                }
            }
        }
    }
}