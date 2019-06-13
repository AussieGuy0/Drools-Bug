# Drools Bug

## Using this repo

### Running
Prereqs: Have Java 11 and Tomcat 9 installed
1. Run `mvn package` in root directory
2. Move `test.war` to tomcat webapps directory
3. Restart tomcat
4. Make a `GET` request to `localhost:8080/test/trigger?drl=big` (`curl localhost:8080/test/trigger?drl=big`)
5. Error will occur and sent back as response

### Files
- `EstimatorSevlet`: A servlet that runs the rule compilation
- `catalina-example.log`: Snippet of tomcat catalina log that shows error messages
- `localhost-example.log`: Snippet of tomcat localhost log that shows error messages

## Bug Description

### Summary
When large rule (.drl) files are complied **with** the security manager turned
on in a servlet container (e.g. Tomcat), it causes `AccessControlExceptions`, which causes `NoClassDefFoundErrors`.

### Steps
Prereqs: Program is run in servlet context (e.g .war file in tomcat)
1. Turn on security manager
2. Provide policy files through the properties `java.security.policy` and `kie.security.policy`
3. Compile a `.drl` file that has more than `parallelRulesBuildThreshold` (default: 10) rules

### Expected Result
Rules are compiled successfully

### Actual Result
No class def error

### Cause
In `KnowledgeBuilderImpl`, a `ForkJoinPool` is created and used for parallel building. 
A `ForkJoinPool` with no `ForkJoinWorkerThreadFactory` specified, it will use a default factory
that provides it's own permissions. These permissions are not sufficient for compiling
drl files in a servlet context.

### Potential Fix
A potential fix is to allow the user to provide their own `ForkJoinWorkerThreadFactory` as a 
configuration option for drools.

