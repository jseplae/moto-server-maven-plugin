package com.jseplae.maven;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Mojo(name = "start")
public class Start extends AbstractMojo {

    @Parameter(name = "containerName", property = "containerName", required = true)
    private String containerName;

    @Parameter(name = "imageTag", property = "containerName", defaultValue = "latest")
    private String imageTag;

    private DockerClient client;

    private static String RUNNING_MESSAGE = "Moto server is running.";
    private static String STATUS_RUNNING = "running";

    @Override
    public void execute() {
        getLog().info("Launching Moto server Docker container...");
        DockerClientConfig standard = DefaultDockerClientConfig
            .createDefaultConfigBuilder()
            .build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(standard.getDockerHost())
            .sslConfig(standard.getSSLConfig())
            .maxConnections(20)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build();
        client = DockerClientImpl.getInstance(standard, httpClient);
        try {
            buildOrStartExisting();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void buildOrStartExisting() throws InterruptedException {
        List<Container> existingContainers = client
            .listContainersCmd()
            .withShowAll(true)
            .withNameFilter(Set.of("/" + containerName))
            .exec();
        if (existingContainers.isEmpty()) {
            startContainer(containerName);
        } else if (existingContainers.size() > 1) {
            getLog().warn("Found more than one Moto server containers.");
            throw new RuntimeException("More than one Moto server images found");
        } else {
            getLog()
                .warn(
                    "Found an existing Moto server container. Starting it if necessary."
                );
            Container existingContainer = existingContainers.getFirst();
            if (STATUS_RUNNING.equals(existingContainer.getState())) {
                getLog().info("Moto server container already running.");
                return;
            }
            startContainer(containerName);
        }
    }

    private void startContainer(String name) throws InterruptedException {
        client
            .pullImageCmd("motoserver/moto")
            .withTag(imageTag)
            .exec(new PullImageResultCallback())
            .awaitCompletion(30, TimeUnit.SECONDS);
        ExposedPort exposedPort = ExposedPort.tcp(5000);
        Ports bindings = new Ports();
        bindings.bind(exposedPort, Ports.Binding.bindPort(5000));
        String containerId = client
            .createContainerCmd("motoserver/moto:" + imageTag)
            .withName(name)
            .withHostConfig(new HostConfig().withPortBindings(bindings))
            .exec()
            .getId();
        getLog().info("Container ID: " + containerId);
        client.startContainerCmd(containerId).exec();
        getLog().info(RUNNING_MESSAGE);
    }
}