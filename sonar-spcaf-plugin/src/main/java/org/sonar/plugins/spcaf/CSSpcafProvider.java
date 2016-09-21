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

import com.google.common.collect.ImmutableList;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;

import java.util.List;

public class CSSpcafProvider {

  public static final SpcafConfiguration Spcaf_CONF = new SpcafConfiguration("Spcaf-cs", spcafPlugin.REPORT_PATH_KEY);

  private CSSpcafProvider() {
  }

  public static List extensions() {
    return ImmutableList.of(
      CQSpcafRulesDefinition.class,
      CQSpcafSensor.class,
      CQSpcafProfileExporter.class,
      CQSpcafProfileImporter.class);
  }

  public static class CQSpcafRulesDefinition extends SpcafRulesDefinition {

    public CQSpcafRulesDefinition() {
      super(Spcaf_CONF);
    }

  }

  public static class CQSpcafSensor extends SpcafSensor {

    public CQSpcafSensor(Settings settings, RulesProfile profile, FileSystem fileSystem, ResourcePerspectives perspectives) {
      super(Spcaf_CONF, settings, profile, fileSystem, perspectives);
    }

  }

  public static class CQSpcafProfileExporter extends SpcafProfileExporter {

    public CQSpcafProfileExporter() {
      super(Spcaf_CONF);
    }
  }

  public static class CQSpcafProfileImporter extends SpcafProfileImporter {

    public CQSpcafProfileImporter() {
      super(Spcaf_CONF);
    }
  }

}
