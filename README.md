# Moto server Maven plugin

A simple Maven plugin for easy Moto server mode (https://docs.getmoto.org/en/latest/docs/server_mode.html) deployment.
Uses the official Docker image from https://hub.docker.com/r/motoserver/moto.

## Example POM

```
<plugin>
  <groupId>io.github.jseplae</groupId>
    <artifactId>moto-server-maven-plugin</artifactId>
    <version>0.2.1</version>
    <configuration>
      <containerName>moto-server</containerName>
      <imageTag>5.0.19</imageTag>
    </configuration>
  <executions>
    <execution>
      <id>start</id>
      <phase>clean</phase>
      <goals>
        <goal>start</goal>
      </goals>
    </execution>
    <execution>
      <id>stop</id>
      <phase>site</phase>
      <goals>
        <goal>stop</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

## Configuration

- containerName: the name for the container that is managed by this plugin. Make sure it does not conflict any existing
  containers.
- imageTag: the specific tag of the underlying Moto server docker image used.
- port: the port number of Moto server. Non-default values currently require using the docker interface IP when
  configuring AWS clients.

## Available plugin goals

- start: pulls and runs the Moto server Docker container.
- stop: tries to stop and remove the container using the configured name.
