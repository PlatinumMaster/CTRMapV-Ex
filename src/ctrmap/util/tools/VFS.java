/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ctrmap.util.tools;

import xstandard.fs.FSFile;
import xstandard.fs.VFSFile;
import xstandard.fs.accessors.MemoryFile;
import xstandard.fs.accessors.ProxyFile;
import xstandard.fs.accessors.arc.ArcFile;
import xstandard.fs.accessors.arc.ArcInput;


public class VFS {
    public static void load(MemoryFile destDir, VFSFile src) {
            for (VFSFile child : src.listFiles()) {
                    FSFile ov = child.getOvFile();
                    FSFile base = child.getBaseFile();
                    if (base == null || !base.exists()) {
                            destDir.linkChild(new ProxyFile(ov, ov.getPathRelativeTo(src.getVFS().getOvFSRoot()))); //use entire overlay file directly
                    } else {
                            //merge ovfs into basefs
                            if (!ov.exists()) {
                                    destDir.linkChild(new ProxyFile(base, base.getPathRelativeTo(src.getVFS().getBaseFSRoot())));
                            } else {
                                    if (base instanceof ArcFile) {
                                            ArcFile arc = (ArcFile) base;
                                            ArcInput[] inputs = src.getVFS().getArcInputs(ov, ov).toArray(new ArcInput[0]);
                                            if (inputs.length > 0) {
                                                    MemoryFile newArc = new MemoryFile(arc.getName(), arc.getBytes());
                                                    ArcFile newArcFileObj = new ArcFile(newArc, src.getVFS().getArcFileAccessor());
                                                    src.getVFS().getArcFileAccessor().writeToArcFile(newArcFileObj, null, inputs);
                                                    arc = newArcFileObj;
                                            }
                                            destDir.linkChild(new ProxyFile(arc.getSource(), base.getPathRelativeTo(src.getVFS().getBaseFSRoot())));
                                    } else if (base.isDirectory()) {
                                            MemoryFile subDir = destDir.createChildDir(base.getName());
                                            VFS.load(subDir, child);
                                    } else {
                                            destDir.linkChild(new ProxyFile(ov, ov.getPathRelativeTo(src.getVFS().getOvFSRoot())));
                                    }
                            }
                    }
            }
    }
}
