SonarQube SPCAF Plugins
=======================

What is SPCAF?
--------------
SPCAF is the SharePoint Code Analysis Framework which is for Microsoft SharePoint and Office. SPCAF scans all files in WSPs, Add-in packages, Javascript and PowerShell script files and applies several hundred rules. If a violation against one of these rules is detected a notification is added to the report.

The notification are reported with their severity e.g. CriticalError, Error, CriticalWarning, Warning or Information

The generated report aggregates the notifications and displays each violation with filename, line number and source code (if available) to make it easier to solve the issue.

Please see [https://www.spcaf.com](https://www.spcaf.com) for more information.

Download
--------
You can download the current release of this plugin [from here](http://url.spcaf.com/dl-spcaf-qc-sq).


SonarQube Integration
---------------------

SonarQube allows you to use MSBuild to run static code analysis tools. This plugin by Rencore GmbH the creators of SharePoint Code Analysis Framework (SPCAF) allows you to configure a build task to import code analysis results after a build has been completed.

The source is included in the repository and it is required that you take the sub version which matches your SPCAF version in order to work.

For example sonar-scpaf-plugin version 1.0.3-beta.6_5_1 is for SPCAF 6.5.1. The language plugin versions are seperate and do not rely on SPCAF versioning.

The reason for this is that the plugins contain references to the rules for that version.

The plugin is seperated into 5 components are 1 prerequisite.

```
|-sonar-spcaf-language-definition-aspx-plugin
|-sonar-spcaf-language-definition-css-plugin
|-sonar-spcaf-language-definition-js-plugin
|-sonar-spcaf-language-definition-ps1-plugin
|-sonar-spcaf-language-definition-xml-plugin
|-sonar-scpaf-plugin
```

The first four plugins are language definitions and should be excluded if you already have a plugin which covers this area.

The exception to this is the XML plugin. SPCAF isn't compatible with any other XML plugin due to the number of propietry xml formats used in SharePoint. With this in mind if you required multiple XML plugins for perhaps other languages and uses it is recommended to have a second server and seperate roles to overcome this limitation of SonarQube.

You will also require the MSBuild SonarQube plugin found 

As we expand analysis you may find a requirement to install further language definition plugins to allow SonarQube to understand that these are analysable file types.

Documentation
---------------------

You can found all the documenatation for this plugin [from here](https://support.rencore.com/hc/en-us/sections/206566547-SonarQube)

A detailed How To can be found [from here](https://support.rencore.com/hc/en-us/articles/229871587-How-to-Install-SPCAF-SonarQube-integration)

