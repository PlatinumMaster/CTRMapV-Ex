package ctrmap.editor.gui.editors.gen5.scripting;

import ctrmap.editor.CTRMap;
import ctrmap.editor.system.script.CTRMapIDEHelper;
import ctrmap.editor.system.script.ScriptOpenMode;
import ctrmap.formats.common.GameInfo;
import ctrmap.scriptformats.gen5.VCommandDataBase;
import ctrmap.scriptformats.gen5.VDecompiler;
import ctrmap.scriptformats.gen5.VScriptFile;
import ctrmap.scriptformats.gen5.disasm.VDisassembler;
import xstandard.gui.DialogUtils;
import xstandard.text.FormattingUtils;

public class VProjectSetupParams {
        public static class ScriptProjectParams implements CTRMapIDEHelper.ProjectSetupParam {
		private VScriptFile script;
		private VCommandDataBase cdb;
		protected final GameInfo game;
                private final CTRMap cm;
                private int scriptIndex;
                private ScriptOpenMode mode;

		public ScriptProjectParams(CTRMap cm, GameInfo game) {
                    this.cm = cm;
		    this.game = game;
		}
                
                public void setScript(VScriptFile script, int scriptIndex, int[] overlays, ScriptOpenMode mode) {
                    this.script = script;
                    this.scriptIndex = scriptIndex;
                    this.mode = mode;
                    this.cdb = this.cm.ideHelper.createCombCommandDB(overlays);
                }

		@Override
		public String getName() {
                    // TODO: Make it <ProjectName.Scripts>
		    return String.format("Project.Scripts");
		}

		@Override
		public String getMainClassName() {
			return String.format("container_%s", FormattingUtils.getIntWithLeadingZeros(4, this.scriptIndex));
		}

                @Override
		public byte[] getMainClassBytes() {
                    if (game.isGenV()) {
                            if (mode == ScriptOpenMode.DISASSEMBLE) {
                                VDisassembler disassembler = new VDisassembler(script, cdb);
                                disassembler.disassemble();
                                try {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(disassembler.dump());
                                        return sb.toString().getBytes();
                                } catch (Exception ex) {
                                        ex.printStackTrace();
                                        DialogUtils.showErrorMessage("Disassembly failed", "The file failed to disassemble.");
                                }                       
                            } else if (mode == ScriptOpenMode.DECOMPILE) {
                                VDecompiler decompiler = new VDecompiler(script, cdb);
                                decompiler.overrideClassName = getMainClassName();
                                decompiler.decompile();
                                try {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(decompiler.dump());

                                        return sb.toString().getBytes();
                                } catch (Exception ex) {
                                        ex.printStackTrace();
                                        DialogUtils.showErrorMessage("Decompilation failed", "The file failed to decompile. Starting from scratch.");
                                }
                            }
                            
                    } else {
                            throw new UnsupportedOperationException();
                    }
                    
                    return new byte[]{};
		}
	} 
}
