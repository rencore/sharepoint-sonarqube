SonarQube Rencore CSS Plugin
============================

Install this if you don't already have a plugin which covers CSS in your SonarQube install.

SonarQube can only contain one language definition plugin at a time, so the SPCAF plugin is separated to allow for this.

Example conflicting plugins:
CSSLint
CSSSHint

Do not install this plugin if you have either of these or another plugin that already defines the language. Your SPCAF rules will be added to that rule repository automatically.