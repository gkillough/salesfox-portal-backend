package ai.salesfox.portal.build;

import org.gradle.api.Project;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.options.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String projectPath = project.getProjectDir().getAbsolutePath();
        String buildDirectory = project.getBuildDir().getAbsolutePath();

        Map<String, Object> envVars = new HashMap<>();
        envVars.put("PORTAL_RESOURCE_BASE_DIR", String.format("%s/tmp", buildDirectory));
        envVars.put("PORTAL_RESOURCE_ICON_DIR", String.format("%s/tmp", buildDirectory));
        envVars.put("PORTAL_RESOURCE_LOGO_PNG", String.format("%s/src/main/resources/images/salesfox_logo.png", projectPath));

        envVars.put("PORTAL_SMTP_HOST", "smtp.gmail.com");
        envVars.put("PORTAL_SMTP_PORT", 465);
        envVars.put("PORTAL_SMTP_USER", "accounts@getboostr.com");
        envVars.put("PORTAL_SMTP_PASSWORD", "yptjlvfodhkuxdly");
        envVars.put("PORTAL_SMTP_FROM", "accounts@getboostr.com");

        envVars.put("PORTAL_SCRIBELESS_API_TESTING", true);

        envVars.put("PORTAL_CORS_ALLOWED_ORIGINS", "*");

        getEnvironment().putAll(envVars);

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
                "--logging.level.org.springframework.security=DEBUG",
                "--server.error.include-stacktrace=always",

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
                "--embedded.postgresql.initScriptPath=file:buildSrc/src/main/resources/init_test_db.sql",

                "--spring.liquibase.change-log=classpath:db/changelog/changes/test_data/development-master.yaml",
                "--spring.liquibase.user=portaladmin",
                "--spring.liquibase.password=Port@l!23",

                "--hibernate.default_schema=portal",
                "--spring.datasource.username=portaluser",
                "--spring.datasource.password=Port@l!23",
                "--spring.datasource.url=jdbc:postgresql://${embedded.postgresql.host}:${embedded.postgresql.port}/salesfox",
                "--spring.datasource.hikari.jdbc-url=jdbc:postgresql://${embedded.postgresql.host}:${embedded.postgresql.port}/salesfox"
        );
    }

}
