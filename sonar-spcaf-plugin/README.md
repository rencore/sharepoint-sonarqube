#SonarQube SPCAF Plugin
======================

##Core plugin for automatically importing resultsof SPCAF analysis after a MSBuild.

##Using SPCAF With SonarQube & Visual Studio Team Services

Installing the SPCAF SonarQube involves three simple steps

- Install SonarQube Plugin
- Add SPCAF Rules to A Quality Profile
- Configure The Visual Studio Team Services Build Process

##Install SonarQube Plugin

1. On the SonarQube Server stop the SonarQube Service
    - From Services right cilck the SonarQube service and select stop
2. Copy the SPACF plugin jar files into your SonarQube Server and put it in the directory: $SONARQUBE_HOME/extensions/plugins.
    - You may already have certain languages defined, if this is the case only copy languages from SPCAF that are new to your environment
3. Start the SonarQube service
    - From Services right cilck the SonarQube service and select start

## Add SPCAF Rules To A Quality Profile

1. Open the SonarQube administration portal
2. Click Rules in the top menu
3. Choose repositories from the left-hand panel and select SPCAF repository of the language you would like to add.
    - Rules option at the top while logged in as an Administrator role, then it can be found on the side
    - You can also filter all SPCAF rules using the "spcaf" tag
4. Click bulk change in the top right-hand menu
5. Select Activate In
     - The option is top right of the rules tab in SonarQube
6. Starting typing and choose the quality profile that you wish to add SPCAF rules too
    - Activate in has predictive typing
7. Click Apply
8. Repeat steps 3-6 for any other language rules you want to add.

##Configure The Visual Studio Team Services Build Process

1. Open Visual Studio Team Services
2. Under Build Menu edit your build task.
3. Add the SPCAF Build task process between the “Fetch the Quality Profile from SonarQube.” and “Finish the analysis and upload the results to SonarQube” tasks. How to Add SPCAF to VSTS: https://docs.spcaf.com/v6/SPCAF_OVERVIEW_665_HOWTORUNSPCAFINTFS2015.html

Typcial Visual Studio Team Services Build task set-up:
    - **Fetch the Quality Profile from SonarQube**
    - Build solution x
    - Publish symbols path
    - Copy files to staging
    - Publish artifacts drop
    - **SPCAF code analysis for SharePoint and Office Projects**
    - **Finish the analysis and upload the results to SonarQube**

4. Set the Advanced -> Additional Settings Attribute of the "Fetch the Quality Profile from SonarQube to:
    "/d:sonar.spcaf.cq.reportPath=$(build.stagingDirectory)/SPCAFResults/SPCAFReport_Rules.xml"

##SonarQube Advanced options:

5.Ensure you select the XML report for the output In the "SPCAF code analysis for SharePoint and Office Projects" task or SonarQube will not be able to read the results.

* After you have run the Visual Studio Team Services build it will then upload the results to SonarQube server.  You will then be able to go into your SonarQube project via the administration interface and see the SPCAF results.

# Troubleshooting

##Rules Not Showing In SonarQube

- Make sure that plugin jar files have been placed in $SONARQUBE_HOME/extensions/plugins and that you have restarted the SonarQube service.
Check for errors/exceptions in the SonarQube log folder.
- If you don't have the report path set (Step 4) then the plugin won't be launched by SonarQube. The message "SPCAF: Report path key not provided - Presuming analysis unwanted" will be shown for each language in the log for the SonarQube Upload Task.

## Exception showing report is not where it is expected:
- Ensure that the same path is used for the report output, and it is set to XML for SPCAF (Step 5) and this matches the value entered for SonarQube in Step 4.




