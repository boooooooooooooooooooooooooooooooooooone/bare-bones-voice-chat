package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;

@FunctionalInterface
public interface Command {
    void execute(String[] args, BareBonesVCServer server);
}