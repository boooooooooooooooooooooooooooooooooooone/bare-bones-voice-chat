package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.packet.ServerUpdateVoiceDistancePacket;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;

public class VoiceDistanceCommand implements Command {

    private final BareBonesVCServer server;
    private final ServerUpdateVoiceDistancePacket serverUpdateVoiceDistancePacket = new ServerUpdateVoiceDistancePacket();

    public VoiceDistanceCommand(BareBonesVCServer server) {
        this.server = server;
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            BareBonesVC.LOGGER.warning("No voice distance specified");
            return;
        }

        try {
            double voiceDistance = Double.parseDouble(args[0]);
            if (voiceDistance < 1d || voiceDistance > 1_000_000d) {
                BareBonesVC.LOGGER.warning("Voice distance must be between 1-1000000");
            } else {
                BareBonesVC.LOGGER.info("Set voice distance to " + voiceDistance);

                this.server.config.voiceDistance = voiceDistance;

                this.serverUpdateVoiceDistancePacket.create(voiceDistance);
                this.server.announce(this.serverUpdateVoiceDistancePacket);
            }
        } catch (NumberFormatException e) {
            BareBonesVC.LOGGER.warning("Invalid voice distance");
        }
    }
}
