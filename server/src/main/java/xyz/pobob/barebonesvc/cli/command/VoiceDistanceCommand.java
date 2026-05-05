package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVCServer;
import xyz.pobob.barebonesvc.net.ServerUpdateVoiceDistancePacket;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

public class VoiceDistanceCommand implements Command {

    private final VoiceServer server;
    private final ServerUpdateVoiceDistancePacket serverUpdateVoiceDistancePacket = new ServerUpdateVoiceDistancePacket();

    public VoiceDistanceCommand(VoiceServer server) {
        this.server = server;
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            BareBonesVCServer.LOGGER.warning("No voice distance specified");
            return;
        }

        try {
            double voiceDistance = Double.parseDouble(args[0]);
            if (voiceDistance < 1d || voiceDistance > 1_000_000d) {
                BareBonesVCServer.LOGGER.warning("Voice distance must be between 1-1000000");
            } else {
                BareBonesVCServer.LOGGER.info("Set voice distance to " + voiceDistance);

                this.server.config.voiceDistance = voiceDistance;

                this.serverUpdateVoiceDistancePacket.create(voiceDistance);
                this.server.announce(this.serverUpdateVoiceDistancePacket.serialize());
            }
        } catch (NumberFormatException e) {
            BareBonesVCServer.LOGGER.warning("Invalid voice distance");
        }
    }
}
