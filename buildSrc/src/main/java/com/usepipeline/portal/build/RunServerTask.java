package com.usepipeline.portal.build;

import org.gradle.api.Project;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.options.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RunServerTask extends Exec {
    private boolean suspend = false;
    private String postgresVersion;
    private boolean reuseContainer = false;

    @Option(option = "suspend", description = "Suspends the server until a debug connection is made")
    public void setSuspend(boolean suspend) {
        this.suspend = suspend;
    }

    @Option(option = "reuseContainer", description = "Will reuse the database container.")
    public void setReuseContainer(boolean reuseContainer) {
        this.reuseContainer = reuseContainer;
    }

    public void setPostgresVersion(String postgresVersion) {
        this.postgresVersion = postgresVersion;
    }

    @Override
    protected void exec() {
        if (null == postgresVersion || postgresVersion.trim().length() == 0) {
            throw new RuntimeException("You must specify a Postgres version to run with.");
        }

        Project project = getProject();
        String buildDirectory = project.getBuildDir().getAbsolutePath();
        String version = (String) project.getVersion();

        File jarFile = new File(String.format("%s/libs/portal-%s.jar", buildDirectory, version));

        List<String> commandArray = new ArrayList<>();
        commandArray.add("java");
        commandArray.addAll(getDebugVariables());
        commandArray.addAll(getJMXVariables());
        commandArray.add("-jar");
        commandArray.add(jarFile.getAbsolutePath());
        commandArray.addAll(getApplicationVariables());
        commandLine(commandArray);
        super.exec();
    }

    public List<String> getDebugVariables() {
        return List.of(
            "-Xdebug",
            "-Xrunjdwp:transport=dt_socket,server=y,address=9095,suspend=" + (suspend ? "y" : "n")
        );
    }

    public List<String> getJMXVariables() {
        return List.of("-Dcom.sun.management.jmxremote",
            "-Dcom.sun.management.jmxremote.port=9045",
            "-Dcom.sun.management.jmxremote.local.only=false",
            "-Dcom.sun.management.jmxremote.authenticate=false",
            "-Dcom.sun.management.jmxremote.ssl=false"
        );
    }

    public List<String> getApplicationVariables() {
        return List.of(
            "--server.port=8080",
            "--spring.jpa.hibernate.ddl-auto=none",
            "--spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",

            // https://github.com/testcontainers/testcontainers-spring-boot
            "--embedded.postgresql.enabled=true",
            "--embedded.postgresql.dockerImage=postgres:" + postgresVersion,
            "--embedded.postgresql.reuseContainer=" + reuseContainer,
            "--embedded.postgresql.waitTimeoutInSeconds=20",
            "--embedded.containers.forceShutdown=true",

            "--embedded.postgresql.schema=portal",
            "--embedded.postgresql.user=root",
            "--embedded.postgresql.password=root",
            "--embedded.postgresql.initScriptPath=file:src/test/resources/testDatabase/init_test_db.sql",

            "--hibernate.default_schema=${embedded.postgresql.schema=portal}",
            "--spring.datasource.username=${embedded.postgresql.user}",
            "--spring.datasource.password=${embedded.postgresql.password}",
            "--spring.datasource.url=jdbc:postgresql://${embedded.postgresql.host}:${embedded.postgresql.port}/pipeline",
            "--spring.datasource.hikari.jdbc-url=jdbc:postgresql://${embedded.postgresql.host}:${embedded.postgresql.port}/pipeline"
        );
    }

}
