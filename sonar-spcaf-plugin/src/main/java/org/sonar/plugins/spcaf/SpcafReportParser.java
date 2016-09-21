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

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class SpcafReportParser {

  public List<SpcafIssue> parse(File file) {
    return new Parser().parse(file);
  }

  private static class Parser {

    private String latestFileName;
    private File file;
    private XMLStreamReader stream;
    private final ImmutableList.Builder<SpcafIssue> filesBuilder = ImmutableList.builder();
    private static final Logger LOG = LoggerFactory.getLogger(SpcafSensor.class);

    public List<SpcafIssue> parse(File file) {
      this.file = file;

      if(!file.exists()) {
        LOG.info("SPCAF: Report file not found");
        return filesBuilder.build();
      }

      InputStreamReader reader = null;
      XMLInputFactory xmlFactory = XMLInputFactory.newInstance();

      try {
        reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8);
        stream = xmlFactory.createXMLStreamReader(reader);

        while (stream.hasNext()) {
          if (stream.next() == XMLStreamConstants.START_ELEMENT) {
            String tagName = stream.getLocalName();

            if ("Notification".equals(tagName)) {
              handleIssueTag();
            }
            if("Solution".equals(tagName)) {
              latestFileName = getAttribute("Name");
            }
          }
        }
      } catch (IOException | XMLStreamException e) {
        throw Throwables.propagate(e);
      } finally {
        closeXmlStream();
        Closeables.closeQuietly(reader);
      }

      return filesBuilder.build();
    }

    private void closeXmlStream() {
      if (stream != null) {
        try {
          stream.close();
        } catch (XMLStreamException e) {
          throw new IllegalStateException(e);
        }
      }
    }

    private void handleIssueTag() throws XMLStreamException {
      String typeId = getRequiredAttribute("CheckID");
      String fileName = latestFileName;

      Map<String , String> propertiesMap = new HashMap<String, String>();

      try {
        while (stream.nextTag() == XMLEvent.START_ELEMENT) {
          String name = stream.getName().getLocalPart();
          if (!propertiesMap.containsKey(name)) {
            propertiesMap.put(name, stream.getElementText());
          }
        }

        String message = propertiesMap.get("Message");
        String filePath = propertiesMap.get("WSPRelativeLocation");
        Integer line = Integer.parseInt(propertiesMap.get("LineNumber"));

        if(line < 1) {
          line = 1;
        }
        else {
          // Make line 1 by minimum default for xml and dll
          List<String> extensions = new ArrayList<>(Arrays.asList(spcafPlugin.XML_SUFFIXES_DEFAULT_VALUE.split(",")));
          extensions.add(".dll");
          for (String ext : extensions) {
            if (filePath.endsWith(ext)) {
              line = 1;
              break;
            }
          }
        }

        String md5 = propertiesMap.get("FileHash");

        filesBuilder.add(new SpcafIssue(fileName, md5, stream.getLocation().getLineNumber(), typeId, filePath, line, message));
      }
      catch(UnsupportedOperationException ex) {
        LOG.warn("SPCAF: Illegal Operation Exception in SpcafReportParser has occurred");
        LOG.warn("SPCAF: Exception type: " + (ex.getMessage() == null ? "null" : ex.getMessage()));
        LOG.warn("SPCAF: StackTrace type: " + (ex.getStackTrace() == null ? "null" : ex.getStackTrace()));
      }
      catch(IllegalArgumentException ex) {
        LOG.warn("SPCAF: Illegal Argument Exception in SpcafReportParser has occurred");
        LOG.warn("SPCAF: Exception type: " + (ex.getMessage() == null ? "null" : ex.getMessage()));
        LOG.warn("SPCAF: StackTrace type: " + (ex.getStackTrace() == null ? "null" : ex.getStackTrace()));
      }
      catch(NullPointerException ex) {
        LOG.warn("SPCAF: Null Pointer Exception in SpcafReportParser has occurred");
        LOG.warn("SPCAF: Exception type: " + (ex.getMessage() == null ? "null" : ex.getMessage()));
        LOG.warn("SPCAF: StackTrace type: " + (ex.getStackTrace() == null ? "null" : ex.getStackTrace()));
      }
      catch(Exception ex)
      {
        LOG.info("SPCAF: An exception has occurred. " + ex.getClass().getName());
      }
    }

    private String getRequiredAttribute(String name) {
      String value = getAttribute(name);
      if (value == null) {
        throw parseError("SPCAF: Missing attribute \"" + name + "\" in element <" + stream.getLocalName() + ">");
      }
      return value;
    }

    @Nullable
    private String getAttribute(String name) {
      for (int i = 0; i < stream.getAttributeCount(); i++) {
        if (name.equals(stream.getAttributeLocalName(i))) {
          return stream.getAttributeValue(i);
        }
      }
      return null;
    }

    private ParseErrorException parseError(String message) {
      return new ParseErrorException(message + " in " + file.getAbsolutePath() + " at line " + stream.getLocation().getLineNumber());
    }
  }

  private static class ParseErrorException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public ParseErrorException(String message) {
      super(message);
    }
  }

}
