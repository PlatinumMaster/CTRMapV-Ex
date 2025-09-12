package ctrmap.missioncontrol_ntr.sequence.commands;

import ctrmap.formats.pokemon.gen5.sequence.commands.CommandScrFade;
import ctrmap.missioncontrol_ntr.sequence.player.SequencePlayer;
import xstandard.math.vec.RGBA;

public class ExecScrFade extends ExecutableCommand<CommandScrFade> {

	public ExecScrFade(CommandScrFade param) {
		super(param);
	}

	@Override
	public void execute(SequencePlayer player, int eventCode) {
		player.getFade().set(convertFadeColor(param.srcColor), convertFadeColor(param.fadeColor), param.duration);
		player.getFade().setVisible(true);
	}
	
	public static RGBA convertFadeColor(int value) {
		if (value == 0) {
			return new RGBA(0, 0, 0, 0);
		}
		float intensity = Math.abs(value) / 16f;
		float alpha = intensity;
		
		float rgb = value > 0 ? 1f : 0f;
		
		return new RGBA(rgb, rgb, rgb, alpha);
	}
}
