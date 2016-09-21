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

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.input.ReaderInputStream;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.profiles.ProfileImporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.utils.StaxParser;
import org.sonar.api.utils.ValidationMessages;

import javax.xml.stream.XMLStreamException;
import java.io.Reader;
import java.util.Map;

public class SpcafProfileImporter extends ProfileImporter {

  private static final Logger LOG = LoggerFactory.getLogger(SpcafProfileImporter.class);

  private static final String DO_NOT_SHOW = "DO_NOT_SHOW";
  public static final Map<String, RulePriority>  PRIORITY = ImmutableMap.<String, RulePriority>builder()
    .put("Information", RulePriority.INFO)
    .put("Warning", RulePriority.MINOR)
    .put("CriticalWarning", RulePriority.MAJOR)
    .put("Error", RulePriority.MAJOR)
    .put("CriticalError", RulePriority.CRITICAL)
    .build();
  private static final String SETTINGS_ROOT_ELEMENT = "wpf:ResourceDictionary";
  private static final  String SETTINGS_RULE_PREFIX = "/Default/CodeInspection/Highlighting/InspectionSeverities/=";
  private final SpcafConfiguration configuration;
  private RulesProfile profile;
  private ValidationMessages messages;

  public SpcafProfileImporter(SpcafConfiguration configuration) {
    super(configuration.repositoryKey(), "Spcaf Settings");
    // todo: set supported languages - multiple languages
    // setSupportedLanguages("cs", "html", "xml", "js", "ps", "css");
    this.configuration = configuration;
  }

  @Override
  public RulesProfile importProfile(Reader reader, ValidationMessages messages) {
    this.messages = messages;
    profile = RulesProfile.create();
    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
      @Override
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
        rootCursor.advance();
        parseRootNode(rootCursor);
      }
    });
    try {
      parser.parse(new ReaderInputStream(reader));
    } catch (XMLStreamException e) {
      String errorMessage = "SPCAF: Error parsing content";
      messages.addErrorText(errorMessage + ": " + e);
      LOG.error(errorMessage, e);
    }
    return profile;
  }

  private void parseRootNode(SMHierarchicCursor rootCursor) throws XMLStreamException {
    String name = rootCursor.getLocalName();
    if (SETTINGS_ROOT_ELEMENT.equals(name)) {
      parseChildren(rootCursor.childElementCursor());
      return;
    }
    String message = "SPCAF: Expected element: " + SETTINGS_ROOT_ELEMENT + ", actual: " + name;
    LOG.error(message);
    messages.addErrorText(message);
  }

  private void parseChildren(SMInputCursor cursor) throws XMLStreamException {
    while (cursor.getNext() != null) {
      parseEntry(cursor);
    }
  }

  private void parseEntry(SMInputCursor cursor) throws XMLStreamException {
    if ("s:String".equals(cursor.getLocalName())) {
      String keyValue = cursor.getAttrValue("x:Key");
      if (keyValue != null && keyValue.startsWith(SETTINGS_RULE_PREFIX)) {
        String key = keyValue.substring(SETTINGS_RULE_PREFIX.length());
        int endIndex = key.indexOf('/');
        if (endIndex <= 0) {
          return;
        }
        key = unescapeRuleKey(key.substring(0, endIndex));
        String severity = cursor.getElemStringValue();
        if (PRIORITY.containsKey(severity)) {
          profile.activateRule(Rule.create(configuration.repositoryKey(), key), PRIORITY.get(severity));
        } else if (!DO_NOT_SHOW.equals(severity)) {
          String message = "SPCAF: Skipping rule " + key + " because has an unexpected severity: " + severity;
          LOG.error(message);
          messages.addErrorText(message);
        }
      }
    }
  }

  private static String unescapeRuleKey(String ruleKey) {
    return ruleKey.replace("_002E", ".").replace("_003A", ":");
  }

}
