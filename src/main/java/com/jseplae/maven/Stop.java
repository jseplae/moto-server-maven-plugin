package com.jseplae.maven;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import java.time.Duration;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "stop")
public class Stop extends AbstractMojo {
    @Parameter(name = "containerName", property = "containerName", required = true)
    private String containerName;

    @Override
    public void execute() {
        getLog().info("Shutting down and removing Moto server mode containers...");
        DockerClientConfig standard = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(standard.getDockerHost())
            .sslConfig(standard.getSSLConfig())
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build();
        DockerClient client = DockerClientImpl.getInstance(standard, httpClient);
        List<Container> containers = client.listContainersCmd().withNameFilter(List.of(containerName)).exec();
        getLog().info("Found containers: " + containers.stream().map(Container::getId).toList());
        containers.forEach(
            (
                container -> {
                    getLog().info("Stopping and removing container: " + container.getId());
                    client.stopContainerCmd(container.getId()).exec();
                    client.removeContainerCmd(container.getId()).exec();
                }
            )
        );
        getLog().info("Successfully shut down and removed Moto server mode containers.");
    }
}
