package ctrmap.cli;

import ctrmap.editor.CTRMapMenuActions;
import ctrmap.editor.gui.workspace.ROMExportDialog;
import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.formats.ntr.rom.srl.NDSROM;
import ctrmap.util.tools.VFS;
import rpm.elfconv.ELF2RPM;
import rpm.elfconv.ExternalSymbolDB;
import xstandard.cli.ArgumentBuilder;
import xstandard.cli.ArgumentContent;
import xstandard.cli.ArgumentPattern;
import xstandard.cli.ArgumentType;
import xstandard.formats.yaml.Yaml;
import xstandard.formats.yaml.YamlListElement;
import xstandard.formats.yaml.YamlNode;
import rpm.format.rpm.RPM;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.accessors.DiskFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rpm.format.rpm.RPMMetaData;
import rpm.format.rpm.RPMRelTargetType;
import rpm.format.rpm.RPMRelocation;
import rpm.format.rpm.RPMRelocationSource;
import rpm.format.rpm.RPMRelocationTarget;
import rpm.format.rpm.RPMSymbol;
import rpm.format.rpm.RPMSymbolType;
import rpm.util.AutoRelGenerator;
import xstandard.formats.yaml.KeyValuePair;
import xstandard.fs.accessors.MemoryFile;
import xstandard.util.JVMClassSourceChecker;

public class ROMBuilder {
	private static final ArgumentPattern[] ARG_PTNS = new ArgumentPattern[]{
		new ArgumentPattern("input", "Project", ArgumentType.STRING, null, true, "-i", "--input"),
		new ArgumentPattern("output", "ROM", ArgumentType.STRING, null, "-o", "--output"),
	};

	public static void main(String[] args) {
		if (args.length == 0 && !JVMClassSourceChecker.isJAR()) {
			args = new String[]{
				"-i D:\\_REWorkspace\\pokescript_genv\\codeinjection_new\\PMC\\build\\PMC.elf",
				"-o D:\\_REWorkspace\\pokescript_genv\\codeinjection_new\\PMC\\build\\PMC_B2.rpm",
				"--esdb D:\\_REWorkspace\\pokescript_genv\\codeinjection_new\\IREO.yml",
				"--meta D:\\_REWorkspace\\pokescript_genv\\codeinjection_new\\PMC\\BuildFiles\\version_b2.yml",
				"--generate-relocations",};
		}
		ArgumentBuilder bld = new ArgumentBuilder(ARG_PTNS);
		bld.parse(args);

		try {
			ArgumentContent input = bld.getContent("input", true);
                        ArgumentContent output = bld.getContent("output", true);
			if (input == null || output == null) {
                            return;
			}
                        
			if (input.exists()) {
                            String inputPath = input.stringValue();
                            String outputPath = output.stringValue();
                            
                            CTRMapProject proj = new CTRMapProject(inputPath, null);
                                
                            MemoryFile romRoot = new MemoryFile("__ROM");
                            VFS.load(romRoot, proj.wsfs.vfs.getVFSRoot());

                            try {
                                    NDSROM.buildROM(romRoot, new DiskFile(outputPath));
                            } catch (IOException ex) {
                                    Logger.getLogger(CTRMapMenuActions.class.getName()).log(Level.SEVERE, null, ex);
                            }
			} else {
				System.out.println("No input given.\n");
				printHelp(bld);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			printHelp(bld);
		}
	}

	private static void printHelp(ArgumentBuilder bld) {
		System.out.println("RPMTool 1.0.0\n");
		bld.print();
	}

	public static void readRelocationsFromYml(RPM destRPM, FSFile sourceFile) {
		List<RPMRelocation> relocations = new ArrayList<>();

		Yaml yml = new Yaml(sourceFile);

		YamlNode relNode = yml.getRootNodeKeyNode("Relocations");

		for (YamlNode rel : relNode.children) {
			RPMRelocation out = new RPMRelocation();

			RPMSymbol s;

			if (rel.hasChildren("SourceAddress")) {
				int srcAddr = rel.getChildIntValue("SourceAddress");
				s = destRPM.findGlobalSymbolByAddrAbs(srcAddr);
				if (s == null) {
					s = new RPMSymbol(destRPM, "SYM_" + Integer.toHexString(srcAddr), RPMSymbolType.VALUE, srcAddr, true);
					destRPM.symbols.add(s);
				}
			} else {
				String symName = rel.getChildByName("SourceSymbol").getValue();
				s = destRPM.getSymbol(symName);
			}
			if (s != null) {
				out.source = new RPMRelocationSource(destRPM, s);
				RPMRelTargetType tt = RPMRelTargetType.fromName(rel.getChildByName("TargetType").getValue());
				if (tt != null) {
					out.target = new RPMRelocationTarget(rel.getChildByName("TargetAddress").getValueInt(), rel.getChildByName("TargetSegment").getValue(), tt);
					relocations.add(out);
				}
			}
		}
		destRPM.setExternalRelocations(relocations);
	}

	public static void writeRelocationsAsYml(RPM srcRpm, FSFile targetFile) {
		Yaml yml = new Yaml();

		YamlNode hooksNode = yml.getEnsureRootNodeKeyNode("Relocations");

		for (RPMRelocation hk : srcRpm.getExternalRelocations()) {
			YamlNode n = new YamlNode(new YamlListElement());
			n.addChild("TargetType", hk.target.targetType);
			n.addChild("TargetSegment", hk.target.module);
			n.addChild("TargetAddress", hk.target.address, true);
			n.addChild("SourceSymbol", hk.source.symb.name);
			hooksNode.addChild(n);
		}

		yml.writeToFile(targetFile);
	}
}
