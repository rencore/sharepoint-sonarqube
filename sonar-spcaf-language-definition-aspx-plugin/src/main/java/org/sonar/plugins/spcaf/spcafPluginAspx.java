/*
 * SonarQube ASPX Plugin
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
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.List;

public class spcafPluginAspx extends SonarPlugin {

  public static final String ASPX_SUFFIXES_KEY = "sonar.spcaf.aspx.file.suffixes";
  public static final String ASPX_SUFFIXES_DEFAULT_VALUE = ".aspx,.master,.ascx";

  /**
   * {@inheritDoc}
   */
  @Override
  public List getExtensions() {
    ImmutableList.Builder builder = ImmutableList.builder();

    // Plugin properties and languages
    builder.add(
      PropertyDefinition.builder(ASPX_SUFFIXES_KEY)
              .defaultValue(ASPX_SUFFIXES_DEFAULT_VALUE)
              .name("File Suffixes")
              .description("Comma-separated list of suffixes for files to analyze.")
              .subCategory("General")
              .onQualifiers(Qualifiers.PROJECT)
              .build(),
      Aspx.class
    );

    return builder.build();
  }

}
