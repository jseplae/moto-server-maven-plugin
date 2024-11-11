# Moto server Maven plugin

A simple Maven plugin for easy Moto server mode (https://docs.getmoto.org/en/latest/docs/server_mode.html) deployment. Uses the official Docker image from https://hub.docker.com/r/motoserver/moto.

## Configuration
The following configuration fields are available:
```
<configuration>
    <containerName>moto-server</containerName>
    <imageTag>5.0.19</imageTag>
</configuration>
```
- containerName: the name for the container that is managed by this plugin. Make sure it does not conflict any existing containers.
- imageTag: the specific tag of the underlying Moto server docker image used.

## Available plugin goals
- start: pulls and runs the Moto server Docker container.
- stop: tries to stop and remove the container using the configured name.
