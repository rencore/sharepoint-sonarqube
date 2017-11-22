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


import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.MessageException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.lang.*;

public class SpcafSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(SpcafSensor.class);

  private final SpcafConfiguration SpcafConf;
  private final Settings settings;
  private final RulesProfile profile;
  private final FileSystem fileSystem;
  private final ResourcePerspectives perspectives;
  private String language;
  private Collection<Path> allFiles = new ArrayList<Path>();

  public SpcafSensor(SpcafConfiguration SpcafConf, Settings settings, RulesProfile profile, FileSystem fileSystem, ResourcePerspectives perspectives) {
    this.SpcafConf = SpcafConf;
    String[] parts = SpcafConf.repositoryKey().split("-");
    language = parts[parts.length-1];
    this.settings = settings;
    this.profile = profile;
    this.fileSystem = fileSystem;
    this.perspectives = perspectives;
  }

  @VisibleForTesting
  protected SpcafConfiguration getConfiguration() {
    return SpcafConf;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    boolean shouldExecute;
    LOG.info("SPCAF: Plugin repo key [" + getConfiguration().repositoryKey() + "]");
    if(!settings.hasKey(SpcafConf.reportPathKey())) {
      LOG.info("SPCAF: Report path key not provided - Presuming analysis unwanted");
      shouldExecute = false;
    } else if (!hasFilesToAnalyze()) {
      LOG.info("SPCAF: No files to analyze");
      shouldExecute = false;
    } else if (profile.getActiveRulesByRepository(getConfiguration().repositoryKey()).isEmpty()) {
      LOG.info("SPCAF: All rules are disabled, skipping execution.");
      shouldExecute = false;
    } else {
      LOG.info("SPCAF: Import will execute");
      shouldExecute = true;
    }
    return shouldExecute;
  }

  private boolean hasFilesToAnalyze() {
    LOG.info("Current language: " + language);
    return fileSystem.files(fileSystem.predicates().hasLanguage(language)).iterator().hasNext();
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    SpcafReportParser parser = new SpcafReportParser();
    analyseReportPath(parser);
  }

  private void analyseReportPath(SpcafReportParser parser) {
    checkProperty(settings, SpcafConf.reportPathKey());
    File reportFile = new File(settings.getString(SpcafConf.reportPathKey()));
    parseReport(parser, reportFile);
  }

  private void parseReport(SpcafReportParser parser, File reportFile) {
    LOG.info("SPCAF: Parsing report: " + reportFile);

    List<SpcafIssue> parse = parser.parse(reportFile);
    LOG.info("SPCAF: Notifications:" + parse.size());

    allFiles.clear();
    addTree(fileSystem.baseDir().toPath(), allFiles);

    LOG.info("");
    LOG.info("--------------------");
    LOG.info("SPCAF: Input files");
    LOG.info("--------------------");
    for(InputFile file : fileSystem.inputFiles(fileSystem.predicates().all()))
    {
      LOG.info(file.absolutePath());
    }
    LOG.info("--------------------");

    List<String> allowedExtensions;
    switch(SpcafConf.repositoryKey().toLowerCase()) {
      case "spcaf-aspx":
        allowedExtensions = Arrays.asList(spcafPlugin.ASPX_SUFFIXES_DEFAULT_VALUE.split(","));
        break;
      case "spcaf-cs":
        allowedExtensions = Arrays.asList(new String[] {".cs",".dll"});
        break;
      case "spcaf-css":
        allowedExtensions = Arrays.asList(spcafPlugin.CSS_SUFFIXES_DEFAULT_VALUE.split(","));
        break;
      case "spcaf-ps1":
        allowedExtensions = Arrays.asList(spcafPlugin.PS1_SUFFIXES_DEFAULT_VALUE.split(","));
        break;
      case "spcaf-js":
        allowedExtensions = Arrays.asList(spcafPlugin.JS_SUFFIXES_DEFAULT_VALUE.split(","));
        break;
      case "spcaf-xml":
        allowedExtensions = Arrays.asList(spcafPlugin.XML_SUFFIXES_DEFAULT_VALUE.split(","));
        break;
      default:
        return;
    }

    LOG.info("Allowed Extensions:" + StringUtils.join(allowedExtensions.toArray(),","));

    for (SpcafIssue issue : parse) {

      try {

        Path p = Paths.get(issue.filePath());
        String fileName = p.getFileName().toString();

        String ext = FilenameUtils.getExtension(fileName);

        // Skip files that we aren't checking under the current repo
        if(!allowedExtensions.contains("." + ext)) {
          continue;
        }

        LOG.info("SPCAF: Issue[" + issue.ruleKey() + "] " + issue.message());
        LOG.info("SPCAF: Issue Filename:" + fileName);

        File file = null;
        // Simplify .cs and .webpart files
        if(ext.equals("cs")) {
          file = p.toFile();
        }
        else if(ext.equals("webpart")) {
          for(File inputFile : fileSystem.files(fileSystem.predicates().all())) {
            if(inputFile.getName().equals(fileName)) {
              file = inputFile;
              break;
            }
          }
        }
        else if(ext.equals("dll")) {
          for(File inputFile : fileSystem.files(fileSystem.predicates().all())) {
            if(inputFile.getName().equals("AssemblyInfo.cs")) {
              file = inputFile;
              break;
            }
          }
        }
        else {
          file = getCorrectFile(issue.md5(), fileName);
        }

        if(file == null) {
          logSkippedIssue(issue, "\"" + fileName + "\" is not in SonarQube Repository.");
          continue;
        }

        String correctedPath = file.getAbsolutePath().replace('\\', '/');
        LOG.info("SPCAF: Physical file located: " + correctedPath);

        InputFile inputFile = null;
        for(InputFile currentInputFile : fileSystem.inputFiles(fileSystem.predicates().all()))
        {
          LOG.info("Comparing physical file location against input file: " + currentInputFile.absolutePath());
          if(currentInputFile.absolutePath().equalsIgnoreCase(correctedPath) && currentInputFile.type().equalsIgnoreCase(InputFile.Type.MAIN))
          {
            inputFile = currentInputFile;
            break;
          }
        }

        if(inputFile == null) {
          logSkippedIssue(issue, "\"" + fileName + "\" is not in SonarQube Input Files.");
          continue;
        }

        LOG.info("SPCAF: Input file path:" + inputFile.absolutePath());

        LOG.info("Repository file located");

        Issuable issuable = perspectives.as(Issuable.class, inputFile);

        if (issuable == null) {
          logSkippedIssue(issue, "\"" + fileName + "\" is not issuable in SonarQube.");
        } else {
          issuable.addIssue(
            issuable.newIssueBuilder()
              .ruleKey(RuleKey.of(SpcafConf.repositoryKey(), issue.ruleKey()))
              .line(issue.line())
              .message(issue.message())
              .build());
          LOG.info("SPCAF: Issue added for " + issue.ruleKey() + " on " + fileName);
        }
      }
      catch(IllegalArgumentException ex) {
        LOG.warn("SPCAF: Illegal Argument Exception in injecting issues has occurred");
        LOG.warn("SPCAF: Current issue: " + issue.toString());
        LOG.warn("SPCAF: Exception type: " + (ex.getMessage() == null ? "null" : ex.getMessage()));
        LOG.warn("SPCAF: StackTrace type: " + (ex.getStackTrace() == null ? "null" : ex.getStackTrace()));
      }
      catch(NullPointerException ex) {
        LOG.warn("SPCAF: Null Pointer Exception in injecting issues has occurred");
        LOG.warn("SPCAF: Current issue:" + issue.toString());
        LOG.warn("SPCAF: Exception type: " + (ex.getMessage() == null ? "null" : ex.getMessage()));
        LOG.warn("SPCAF: StackTrace type: " + (ex.getStackTrace() == null ? "null" : ex.getStackTrace()));
      }
      catch(MessageException ex) {
        LOG.warn("SPCAF: Message Exception in injecting issues has occurred");
        LOG.warn("SPCAF: Current issue: " + issue.toString());
        LOG.warn("SPCAF: Exception type: " + (ex.getMessage() == null ? "null" : ex.getMessage()));
        LOG.warn("SPCAF: StackTrace type: " + (ex.getStackTrace() == null ? "null" : ex.getStackTrace()));
      }
      catch(Exception ex) {
        LOG.warn("SPCAF: General Exception in injecting issues has occurred");
        LOG.warn("SPCAF: Current issue: " + issue.toString());
        LOG.warn("SPCAF: Exception type: " + ex.getClass().getName());
      }
    }
  }

  private List<String> folderExclusions = Arrays.asList(new String[] { /*"obj", "bin", "pkgobj", "pkg"*/ });
  private void addTree(Path file, Collection<Path> all) {
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(file)) {
      for (Path child : ds) {
        if(folderExclusions.contains(file.getFileName().toString())) {
          continue;
        }
        all.add(child);
        if (Files.isDirectory(child)) {
          addTree(child, all);
        }
      }
    }
    catch(IOException ex){
      LOG.info("SPCAF: IO Exception occured while attempted to read file: " + ex.getMessage());
    }
  }

  private File getCorrectFile(String md5, String fileName) {
    try{
      for(Path path : allFiles) {
        if (path.getFileName().toString().equals(fileName)) {
          File file = path.toFile();
          String md5String = getMD5Hash(file);
          if(md5.equals(md5String)) {
            LOG.info("SPCAF: MD5 Matched:" + file.getName());
            file = correctFilePathIfInPackage(file);
            return file;
          }
        }
      }
    } catch (FileNotFoundException ex) {
      LOG.info("SPCAF: File \"" + fileName + "\" is not in SonarQube");
    } catch (IOException ex) {
      LOG.info("SPCAF: File \"" + fileName + "\" is not in SonarQube");
    }
    return null;
  }

  private File correctFilePathIfInPackage(File file) {
    LOG.info("SPCAF: Correcting file path for: " + file.getAbsolutePath());
    String cleanedPath = file.getAbsolutePath().toLowerCase().replace('\\', '/');
    if(!(cleanedPath.contains("/pkg/") || cleanedPath.contains("/pkgobj/".toLowerCase()))) {
      LOG.info("SPCAF: File wasn't in pkg folder.");
      return file;
    }

    String fileName = file.getName().toLowerCase();
    LOG.info("SPCAF: " + fileName + " found in pkg folder");
    File directory = new File(file.getParent());
    if(fileName.equals("elements.xml"))
    {
      SpdataReader spdataReader = new SpdataReader();

      try {
        File redirectFile = spdataReader.redirectElementsFile(directory, allFiles);
        if(redirectFile == null) {
          return file;
        }
        return redirectFile;
      } catch (ParserConfigurationException e) {
        LOG.warn("SPCAF: Parser Configuration exception");
        return file;
      } catch (IOException e) {
        LOG.warn("SPCAF: IO Exception");
        return file;
      } catch (SAXException e) {
        LOG.warn("SPCAF: SAX Exception");
        return file;
      }
    }
    else if(fileName.equals("feature.xml")) {
      LOG.info("SPCAF: Special processing over Elements.xml");
      String directoryName = directory.getName();
      String newFileName = directoryName + ".feature";
      LOG.info("SPCAF: Attempting to find original file: " + newFileName);
      for(Path fileOnDisk : allFiles) {
        if(fileOnDisk.toFile().getAbsolutePath().contains(newFileName)) {
          LOG.info("SPCAF: Original file not found, redirecting issue to the correct original.");
          return fileOnDisk.toFile();
        }
      }
    }
    else if(fileName.equals("manifest.xml")) {
      for(Path fileOnDisk : allFiles) {
        if(fileOnDisk.toFile().getAbsolutePath().contains("Package.package")) {
          LOG.info("SPCAF: Original file not found, redirecting issue to the correct original.");
          return fileOnDisk.toFile();
        }
      }
    }
    else if(fileName.equals("schema.xml")) {
      String directoryName = directory.getName();
      for(Path path : allFiles){
        File currentFile = path.toFile();
        if(currentFile.getAbsolutePath().toLowerCase().endsWith(directoryName.toLowerCase() + "\\schema.xml")) {
          LOG.info("SPCAF: File found.");
          return currentFile;
        }
      }
    }
    else {
      LOG.info("SPCAF: Packaged file not supported:" + fileName);
    }
    LOG.info("SPCAF: File redirection not found, attempting to use original file.");
    return file;
  }

  private String getMD5Hash(File packageFile) throws IOException {

    FileInputStream fis = new FileInputStream(packageFile);

    byte[] md5Bytes = org.apache.commons.codec.digest.DigestUtils.md5(fis);
    fis.close();

    sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
    String md5String = encoder.encode(md5Bytes);
    return md5String;
  }

  private static void logSkippedIssue(SpcafIssue issue, String reason) {
    LOG.info("SPCAF: Skipping the Spcaf issue at line " + issue.reportLine() + " " + reason);
  }

  private static void checkProperty(Settings settings, String property) {
    if (!settings.hasKey(property) || settings.getString(property).isEmpty()) {
      throw new IllegalStateException("SPCAF: The property \"" + property + "\" must be set.");
    }
  }
}
