#SonarQube SPCAF Plugins

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

Installation documentation can be found in the sonar-spcaf-plugin section of this repo.
