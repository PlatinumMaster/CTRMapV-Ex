package ctrmap.missioncontrol_ntr.sequence;

import ctrmap.missioncontrol_base.debug.IMCDebugger;
import ctrmap.missioncontrol_ntr.sequence.player.SequencePlayer;

public interface VSequencePlaybackDebugger extends IMCDebugger {

	public void attach(VSequenceController ctrl);

	public void onSeqPlaybackStarted(SequencePlayer player);

	public void onPlaybackAdvance(SequencePlayer player);
}
