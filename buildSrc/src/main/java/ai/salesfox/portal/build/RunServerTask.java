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
    private static final int CONTAINER_TIMEOUT_SECONDS = 30;

    private boolean suspend = false;
    private String postgresVersion;
    private String rabbitMQVersion;
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

    public void setRabbitMQVersion(String rabbitMQVersion) {
        this.rabbitMQVersion = rabbitMQVersion;
    }

    @Override
    protected void exec() {
        validateVersionStringParam(postgresVersion, "Postgres");
        validateVersionStringParam(rabbitMQVersion, "RabbitMQ");

        Project project = getProject();
        String projectPath = project.getProjectDir().getAbsolutePath();
        String buildDirectory = project.getBuildDir().getAbsolutePath();

        Map runEnvironment = getEnvironment();
        Map<String, Object> envVars = new HashMap<>();
        envVars.putIfAbsent("PORTAL_BACK_END_URL", "https://://localhost:8443");
        envVars.putIfAbsent("PORTAL_FRONT_END_URL", "https://localhost:3000");

        envVars.putIfAbsent("PORTAL_RESOURCE_BASE_DIR", String.format("%s/tmp", buildDirectory));
        envVars.putIfAbsent("PORTAL_RESOURCE_ICON_DIR", String.format("%s/tmp", buildDirectory));
        envVars.put("PORTAL_RESOURCE_LOGO_PNG", String.format("%s/src/main/resources/images/salesfox_logo.png", projectPath));

        envVars.putIfAbsent("PORTAL_SMTP_HOST", "smtp.sendgrid.net");
        envVars.putIfAbsent("PORTAL_SMTP_PORT", 465);
        envVars.putIfAbsent("PORTAL_SMTP_USER", "apikey");
        envVars.putIfAbsent("PORTAL_SMTP_FROM", "noreply@salesfox.ai");

        envVars.putIfAbsent("PORTAL_SCRIBELESS_API_TESTING", true);

        envVars.putIfAbsent("PORTAL_CORS_ALLOWED_ORIGINS", "*");

        envVars.putIfAbsent("PORTAL_SSL_ENABLED", true);
        envVars.putIfAbsent("PORTAL_SERVER_PORT", 8443);

        runEnvironment.putAll(envVars);

        String version = (String) project.getVersion();
        File jarFile = new File(String.format("%s/libs/portal-%s.jar", buildDirectory, version));

        List<String> commandArray = new ArrayList<>();
        commandArray.add("java");
        commandArray.addAll(getDebugVariables());
        commandArray.addAll(getJMXVariables());
        commandArray.add("-jar");
        commandArray.add(jarFile.getAbsolutePath());
        commandArray.addAll(getSSLVariables());

        if (!envVars.containsKey("PORTAL_AMQP_ADDRESSES")) {
            commandArray.addAll(getRabbitMQVariables());
        }

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
        return List.of(
                "-Dcom.sun.management.jmxremote",
                "-Dcom.sun.management.jmxremote.port=9045",
                "-Dcom.sun.management.jmxremote.local.only=false",
                "-Dcom.sun.management.jmxremote.authenticate=false",
                "-Dcom.sun.management.jmxremote.ssl=false"
        );
    }

    public List<String> getSSLVariables() {
        return List.of(
                "--server.ssl.key-store=classpath:keystore.p12",
                "--server.ssl.key-store-password=changeit",
                "--server.ssl.key-store-type=PKCS12",
                "--server.ssl.key-alias=portal-cert",
                "--server.ssl.protocol=TLS",
                "--server.ssl.enabled-protocols=TLSv1.2,TLSv1.3",
                "--spring.profiles.active=ssl"
        );
    }

    // https://github.com/Playtika/testcontainers-spring-boot/tree/develop/embedded-rabbitmq
    public List<String> getRabbitMQVariables() {
        return List.of(
                "--embedded.rabbitmq.enabled=true",
                "--embedded.rabbitmq.reuseContainer=" + reuseContainer,
                "--embedded.rabbitmq.password=rabbitmq",
                "--embedded.rabbitmq.dockerImage=rabbitmq:" + rabbitMQVersion,
                "--embedded.rabbitmq.waitTimeoutInSeconds=" + CONTAINER_TIMEOUT_SECONDS,

                "--spring.rabbitmq.host=${embedded.rabbitmq.host}",
                "--spring.rabbitmq.port=${embedded.rabbitmq.port}",
                "--spring.rabbitmq.username=${embedded.rabbitmq.user}",
                "--spring.rabbitmq.password=${embedded.rabbitmq.password}"
        );
    }

    public List<String> getApplicationVariables() {
        return List.of(
                "--logging.level.org.springframework.security=DEBUG",
                "--server.error.include-stacktrace=always",

                "--spring.jpa.hibernate.ddl-auto=none",
                "--spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",

                // https://github.com/testcontainers/testcontainers-spring-boot
                // https://github.com/Playtika/testcontainers-spring-boot
                "--embedded.postgresql.enabled=true",
                "--embedded.postgresql.dockerImage=postgres:" + postgresVersion,
                "--embedded.postgresql.reuseContainer=" + reuseContainer,
                "--embedded.postgresql.waitTimeoutInSeconds=" + CONTAINER_TIMEOUT_SECONDS,
                "--embedded.containers.forceShutdown=true",

                "--embedded.postgresql.schema=portal",
                "--embedded.postgresql.user=root",
                "--embedded.postgresql.password=root",
                "--embedded.postgresql.initScriptPath=file:buildSrc/src/main/resources/init_test_db.sql",

                "--spring.liquibase.change-log=classpath:db/changelog/changes/test_data/test_data_master.yaml",
                "--spring.liquibase.user=portaladmin",
                "--spring.liquibase.password=Port@l!23",

                "--hibernate.default_schema=portal",
                "--spring.datasource.username=portaluser",
                "--spring.datasource.password=Port@l!23",
                "--spring.datasource.url=jdbc:postgresql://${embedded.postgresql.host}:${embedded.postgresql.port}/salesfox"
        );
    }

    private void validateVersionStringParam(String param, String paramName) {
        if (null == param || param.trim().length() == 0) {
            throw new RuntimeException(String.format("You must specify a %s version to run with.", paramName));
        }
    }

}
