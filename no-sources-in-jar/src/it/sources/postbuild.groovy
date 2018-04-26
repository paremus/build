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

String fileContents = new File(basedir, "build.log").text

assert fileContents.contains("com.paremus.build.rule.nosources.NoSourcesInJarRule failed with message:\n" +
"com.paremus.build.rule.nosources.test:sources:jar:0.0.1-SNAPSHOT ->\n" +
"	OSGI-OPT/src/com/paremus/build/rule/nosources/test/Example.java");
