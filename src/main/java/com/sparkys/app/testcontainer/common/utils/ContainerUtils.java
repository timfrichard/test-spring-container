package com.sparkys.app.testcontainer.common.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.Value;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.function.Consumer;

@UtilityClass
public class ContainerUtils {

    public static final Duration DEFAULT_CONTAINER_WAIT_DURATION = Duration.ofSeconds(60);

    public static int getAvailableMappingPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot find available port for mapping: " + e.getMessage(), e);
        }
    }

    public static Consumer<OutputFrame> containerLogsConsumer(Logger log) {
        return (OutputFrame outputFrame) -> {
            switch (outputFrame.getType()) {
                case STDERR:
                    log.debug(outputFrame.getUtf8String());
                    break;
                case STDOUT:
                case END:
                    log.debug(outputFrame.getUtf8String());
                    break;
                default:
                    log.debug(outputFrame.getUtf8String());
                    break;
            }
        };
    }

    public static String getContainerHostname(GenericContainer container) {
        InspectContainerResponse containerInfo = container.getContainerInfo();
        if (containerInfo == null) {
            containerInfo = container.getDockerClient().inspectContainerCmd(container.getContainerId()).exec();
        }

        return containerInfo.getConfig().getHostName();
    }

    public static ExecCmdResult execCmd(DockerClient dockerClient, String containerId, String[] command) {
        ExecCreateCmdResponse cmd = dockerClient.execCreateCmd(containerId)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd(command)
                .exec();

        String cmdStdout;
        String cmdStderr;

        try (ByteArrayOutputStream stdout = new ByteArrayOutputStream();
             ByteArrayOutputStream stderr = new ByteArrayOutputStream();
             ExecStartResultCallback cmdCallback = new ExecStartResultCallback(stdout, stderr)) {
            dockerClient.execStartCmd(cmd.getId()).exec(cmdCallback).awaitCompletion();
            cmdStdout = stdout.toString(StandardCharsets.UTF_8.name());
            cmdStderr = stderr.toString(StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            String format = String.format("Exception was thrown when executing: %s, for container: %s ", Arrays.toString(command), containerId);
            throw new IllegalStateException(format, e);
        }

        int exitCode = dockerClient.inspectExecCmd(cmd.getId()).exec().getExitCode();
        String output = cmdStdout.isEmpty() ? cmdStderr : cmdStdout;
        return new ExecCmdResult(exitCode, output);
    }

    @Value
    public static class ExecCmdResult {
        int exitCode;
        String output;
    }
}
