SonarQube Rencore PowerShell Plugin
============================

Install this if you don't already have a plugin which covers PowerShell in your SonarQube install.

SonarQube can only contain one language definition plugin at a time, so the SPCAF plugin is separated to allow for this.

Example conflicting plugins:
PowerShell Analyzer

Do not install this plugin if you have either this or another plugin that already defines the language. Your SPCAF rules will be added to that rule repository automatically.