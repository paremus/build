/*-
 * #%L
 * No Sources in JAR files Rule
 * %%
 * Copyright (C) 2018 - 2019 Paremus Ltd
 * %%
 * Licensed under the Fair Source License, Version 0.9 (the "License");
 * 
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. You may not use this file 
 * except in compliance with the License. For usage restrictions see the 
 * LICENSE.txt file distributed with this work
 * #L%
 */
package com.paremus.build.rule.nosources;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

public class NoSourcesInJarRule implements EnforcerRule {
    
    private boolean failOnMissing = true;

    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        Log log = helper.getLog();
        
        MavenProject project;
        try {
            project = (MavenProject) helper.evaluate( "${project}" );
        } catch (ExpressionEvaluationException e) {
            log.error("Failed to get the Maven Project", e);
            throw new EnforcerRuleException("Unable to get the current Maven Project", e);
        }

        Map<String, List<String>> locatedSourceFiles = new HashMap<>();
        
        List<Artifact> artifacts = new ArrayList<>();
        artifacts.add(project.getArtifact());
        artifacts.addAll(project.getAttachedArtifacts());
        
        for(Artifact a : artifacts) {
            String type = a.getType();
            String classifier = a.getClassifier();
            
            if("jar".equals(type) && !"sources".equals(classifier) && !"javadoc".equals(classifier)) {
                log.debug("Found an output jar file that isn't sources or javadoc: " + a.getId());
                List<String> sources = checkForSources(a, log);
                
                if(!sources.isEmpty()) {
                    locatedSourceFiles.put(a.getId(), sources);
                }
            }
        }
        
        if(!locatedSourceFiles.isEmpty()) {
            String report = getReport(locatedSourceFiles);
            log.error("Sources were detected in artifacts for this project:\n\n" + report);
            throw new EnforcerRuleException(report);
        }
    }

    private List<String> checkForSources(Artifact a, Log log) {
        File file = a.getFile();
        if(file == null) {
            log.warn("No file available for attached artifact: " + a.getId());
            if(failOnMissing) {
                return Collections.singletonList("No attached file for " + a.getId());
            } else {
                return Collections.emptyList();
            }
        }
        
        try(JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> entries = jar.entries();

            List<String> sourceFiles = new ArrayList<>();
            
            while(entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                
                if(!entry.isDirectory()) {
                    String name = entry.getName();
                    if(name.endsWith(".java")) {
                        sourceFiles.add(name);
                    }
                }
            }
            
            return sourceFiles;
            
        } catch (IOException e) {
            log.error("Failed to read the jar file for artifact " + a.getId(), e);
            return Collections.singletonList("Failed to read the jar file for artifact " + a.getId());
        }
    }

    private String getReport(Map<String, List<String>> locatedSourceFiles) {
        StringWriter sw = new StringWriter();
        
        for(Entry<String, List<String>> e : locatedSourceFiles.entrySet()) {
            sw.write(e.getKey());
            sw.write(" ->\n");
            for(String sourceFile : e.getValue()) {
                sw.write('\t');
                sw.write(sourceFile);
                sw.write('\n');
            }
        }
        
        return sw.toString();
    }

    public String getCacheId() {
        // Not cacheable, so this doesn't matter
        return null;
    }

    public boolean isCacheable() {
        return false;
    }

    public boolean isResultValid(EnforcerRule arg0) {
        // Not cacheable, so this doesn't matter
        return false;
    }

}
