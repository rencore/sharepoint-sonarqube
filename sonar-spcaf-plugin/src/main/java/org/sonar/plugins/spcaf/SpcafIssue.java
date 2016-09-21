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

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.Nullable;

public class SpcafIssue {

  private final String file;
  private final int reportLine;
  private final String ruleKey;
  private final String filePath;
  private final Integer line;
  private final String message;
  private final String md5;

  public SpcafIssue(String file, String md5, int reportLine, String ruleKey, @Nullable String filePath, @Nullable Integer line, String message) {
    this.file = file;
    this.md5 = md5;
    this.reportLine = reportLine;
    this.ruleKey = ruleKey;
    this.filePath = filePath;
    this.line = line;
    this.message = message;
  }

  public String file() { return file; }

  public int reportLine() {
    return reportLine;
  }

  public String ruleKey() {
    return ruleKey;
  }

  public String filePath() {
    return filePath;
  }

  @Nullable
  public Integer line() {
    return line;
  }

  public String message() {
    return message;
  }

  public String md5() { return md5; }

  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
