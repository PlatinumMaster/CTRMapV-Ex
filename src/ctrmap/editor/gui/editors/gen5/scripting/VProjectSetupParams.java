package ctrmap.editor.gui.editors.gen5.scripting;

import ctrmap.editor.CTRMap;
import ctrmap.editor.system.script.CTRMapIDEHelper;
import ctrmap.editor.system.script.ScriptOpenMode;
import ctrmap.formats.common.GameInfo;
import ctrmap.scriptformats.gen5.VCommandDataBase;
import ctrmap.scriptformats.gen5.VDecompiler;
import ctrmap.scriptformats.gen5.VScriptFile;
<<<<<<< HEAD
=======
import ctrmap.scriptformats.gen5.disasm.DisassembledCall;
import ctrmap.scriptformats.gen5.disasm.DisassembledMethod;
import ctrmap.scriptformats.gen5.disasm.LinkPrototype;
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
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
<<<<<<< HEAD
                                        sb.append(disassembler.dump());
=======
                                        sb.append("// Disassembled output\n\n");

                                        // Build map of instruction pointer -> all public labels
                                        java.util.List<LinkPrototype> pubs = disassembler.getPublics();
                                        java.util.Map<Integer, java.util.List<String>> publicLabels = new java.util.LinkedHashMap<>();
                                        for (int pi = 0; pi < pubs.size(); pi++) {
                                                int targetPtr = pubs.get(pi).targetOffset;
                                                publicLabels.computeIfAbsent(targetPtr, k -> new java.util.ArrayList<>()).add("main_" + (pi + 1));
                                        }

                                        for (DisassembledMethod method : disassembler.methods) {
                                                sb.append("// Method (ptr=0x").append(Integer.toHexString(method.ptr)).append(")\n");
                                                for (DisassembledCall call : method.instructions) {
                                                        // Emit all public labels that target this instruction
                                                        java.util.List<String> pubLabels = publicLabels.get(call.pointer);
                                                        if (pubLabels != null) {
                                                                for (String pl : pubLabels) {
                                                                        sb.append(pl).append(":\n");
                                                                }
                                                        }
                                                        // Emit the instruction's own labels (LABEL_xxxx, sub_xxxx) if different from publics
                                                        if (call.labels != null) {
                                                                for (String lbl : call.labels) {
                                                                        if (pubLabels == null || !pubLabels.contains(lbl)) {
                                                                                sb.append(lbl).append(":\n");
                                                                        }
                                                                }
                                                        }
                                                        sb.append("  ");
                                                        if (call.command != null) {
                                                                sb.append(call.command.name);
                                                        } else {
                                                                sb.append("unknown_cmd");
                                                        }
                                                        for (int i = 0; i < call.args.length; i++) {
                                                                if (call.link != null && call.link.argIdx == i && call.link.target != null) {
                                                                        DisassembledCall target = (DisassembledCall) call.link.target;
                                                                        if (target.labels == null || target.labels.isEmpty()) {
                                                                                if (target.labels == null) {
                                                                                        target.labels = new java.util.ArrayList<>();
                                                                                }
                                                                                target.labels.add("LABEL_" + FormattingUtils.getStrWithLeadingZeros(4, Integer.toHexString(target.pointer)));
                                                                        }
                                                                        sb.append(" ").append(target.labels.get(0));
                                                                } else {
                                                                        sb.append(" ").append(call.args[i]);
                                                                }
                                                        }
                                                        sb.append("\n");
                                                }
                                                sb.append("\n");
                                        }
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
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
