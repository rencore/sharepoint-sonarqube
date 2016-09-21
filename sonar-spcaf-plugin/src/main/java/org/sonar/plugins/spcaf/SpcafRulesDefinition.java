/*
 * SonarQube SPCAF Plugin
 * Copyright (C) 2014 SonarSource
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.spcaf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;

import java.nio.charset.StandardCharsets;

public abstract class SpcafRulesDefinition implements RulesDefinition {

  private static final Logger LOG = LoggerFactory.getLogger(SpcafRulesDefinition.class);

  private final RulesDefinitionXmlLoader rulesDefinitionXmlLoader = new RulesDefinitionXmlLoader();
  private final SpcafConfiguration configuration;

  public SpcafRulesDefinition(SpcafConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void define(Context context) {

    try {
      String[] keystring = configuration.repositoryKey().split("-");
      String language = keystring[keystring.length-1];

      LOG.info("SPCAF: Loading rules into repository " + configuration.repositoryKey());
      LOG.info("SPCAF: Resource path: /org/sonar/plugins/spcaf/rules_" + language.toUpperCase() + ".xml");

      // Create language repo context
      NewRepository repository = context
          .createRepository(configuration.repositoryKey(), language)
          .setName(configuration.repositoryKey());

      // Load rule definitions into the context
      rulesDefinitionXmlLoader.load(repository, getClass().getResourceAsStream("/org/sonar/plugins/spcaf/rules_" + language.toUpperCase() + ".xml"), StandardCharsets.UTF_8.name());

      LOG.info("SPCAF: Repository [" + repository.key() + "] size: " + repository.rules().size());

      // todo: Include sqale definitions
      // SqaleXmlLoader.load(repository, "/org/sonar/plugins/spcaf/sqale.xml");

      // Commit the changes
      repository.done();
      LOG.info("SPCAF rules for " + language.toUpperCase() + " Code Quality imported.");
    }
    catch(Exception ex)
    {
      LOG.info("SPCAF: Rule definitions not loaded: [" + ex.getMessage() + "]\r\nStacktrace:" + ex.getStackTrace());
    }
  }
}
