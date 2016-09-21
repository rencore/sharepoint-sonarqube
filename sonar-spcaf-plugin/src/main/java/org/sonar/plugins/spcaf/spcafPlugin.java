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
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.List;

public class spcafPlugin extends SonarPlugin {

  // Global constants
  public static final String JS_SUFFIXES_DEFAULT_VALUE = ".js";

  public static final String ASPX_SUFFIXES_DEFAULT_VALUE = ".aspx,.master,.ascx,.html,.htm";

  public static final String PS1_SUFFIXES_DEFAULT_VALUE = ".ps1";

  public static final String XML_SUFFIXES_DEFAULT_VALUE = ".xml,.package,.feature,.webpart";

  public static final String CSS_SUFFIXES_DEFAULT_VALUE = ".css";

  public static final String REPORT_PATH_KEY = "sonar.spcaf.cq.reportPath";

  private static final String CATEGORY = "Spcaf";

  /**
   * {@inheritDoc}
   */
  @Override
  public List getExtensions() {
    ImmutableList.Builder builder = ImmutableList.builder();

    // Plugin properties and languages
    builder.add(
      PropertyDefinition.builder(REPORT_PATH_KEY)
        .name("Spcaf report path for Code Quality")
        .description("Path to the Spcaf report for Code Quality, i.e. reports/SPCAF_Rules.xml")
        .category(CATEGORY)
        .onlyOnQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .build()
    );

    // Report parser
    builder.addAll(ASPXSpcafProvider.extensions());
    builder.addAll(CSSSpcafProvider.extensions());
    builder.addAll(CSSpcafProvider.extensions());
    builder.addAll(JSSpcafProvider.extensions());
    builder.addAll(PS1SpcafProvider.extensions());
    builder.addAll(XMLSpcafProvider.extensions());

    return builder.build();
  }

}
