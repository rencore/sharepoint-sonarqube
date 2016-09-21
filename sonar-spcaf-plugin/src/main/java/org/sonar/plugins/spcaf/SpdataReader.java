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

import org.apache.commons.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Created by silic on 19/05/2016.
 */
public class SpdataReader {
    private static final Logger LOG = LoggerFactory.getLogger(SpdataReader.class);

    public File redirectElementsFile(File directory, Collection<Path> localFiles) throws ParserConfigurationException, IOException, SAXException {
        if(directory == null) {
            throw new NullArgumentException("SPCAF: redirectElementsFile: Argument must not be null");
        }
        if(!directory.isDirectory()) {
            throw new IllegalArgumentException("SPCAF: redirectElementsFile: Argument must be a directory");
        }
        File spdataFile = null;
        String directoryName = directory.getName();
        for(Path localFile : localFiles) {
            File file = localFile.toFile();
            if(file.getAbsolutePath().toLowerCase().endsWith(directoryName.toLowerCase() + "\\sharepointprojectitem.spdata")) {
                LOG.info("SPCAF: spdata file found: " + file.getAbsolutePath());
                spdataFile = file;
                break;
            }
        }
        if(spdataFile == null) {
            throw new NullPointerException("SPCAF: redirectElementsFile: spdata file not found.");
        }
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(spdataFile);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("ProjectItemFile");
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            String source = node.getAttributes().getNamedItem("Source").getNodeValue();
            String target = node.getAttributes().getNamedItem("Target").getNodeValue();
            if(source.equals("Elements.xml")) {
                LOG.info("SPCAF: Attempting to find: " + target + source);
                for(Path path : localFiles){
                    File file = path.toFile();
                    if(file.getAbsolutePath().endsWith(target + source)) {
                        LOG.info("File found.");
                        return file;
                    }
                }
            }
        }
        LOG.info("SPCAF: spdata file not found");
        return null;
    }
}
