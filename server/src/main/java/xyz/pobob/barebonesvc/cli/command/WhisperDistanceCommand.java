package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.packet.ServerUpdateVoiceDistancePacket;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;

public class WhisperDistanceCommand implements Command {

    private final ServerUpdateVoiceDistancePacket serverUpdateVoiceDistancePacket = new ServerUpdateVoiceDistancePacket();

    @Override
    public void execute(String[] args, BareBonesVCServer server) {
        if (args.length == 0) {
            BareBonesVC.LOGGER.warning("No whisper distance specified");
            return;
        }

        try {
            double whisperDistance = Double.parseDouble(args[0]);
            if (whisperDistance < 1d || whisperDistance > 1_000_000d) {
                BareBonesVC.LOGGER.warning("Whisper distance must be between 1-1000000");
            } else {
                BareBonesVC.LOGGER.info("Set whisper distance to " + whisperDistance);

                server.config.whisperDistance = whisperDistance;

                this.serverUpdateVoiceDistancePacket.create(whisperDistance, true);
                server.announce(this.serverUpdateVoiceDistancePacket);
            }
        } catch (NumberFormatException e) {
            BareBonesVC.LOGGER.warning("Invalid whisper distance");
        }
    }
}
