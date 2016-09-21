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

import org.sonar.api.profiles.ProfileExporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class SpcafProfileExporter extends ProfileExporter {
  private final SpcafConfiguration configuration;

  public SpcafProfileExporter(SpcafConfiguration configuration) {
    super(configuration.repositoryKey(), "Spcaf Settings");
    this.configuration = configuration;
    setSupportedLanguages("cs", "html", "xml", "js", "ps1", "css");
  }

  @Override
  public void exportProfile(RulesProfile profile, Writer writer) {
    SpcafSettingsWriter SpcafDotSettingsWriter = new SpcafSettingsWriter();
    List<ActiveRule> activeRules = profile.getActiveRulesByRepository(configuration.repositoryKey());
    List<String> activeRuleKeys = new ArrayList<>();
    for (ActiveRule activeRule : activeRules) {
      activeRuleKeys.add(activeRule.getRuleKey());
    }
    try {
      SpcafDotSettingsWriter.write(activeRuleKeys, writer);
    } catch (IOException e) {
      throw new IllegalStateException("SPCAF: Failed to export profile " + profile, e);
    }
  }
}
