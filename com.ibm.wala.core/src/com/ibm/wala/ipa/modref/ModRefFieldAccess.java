package com.ibm.wala.ipa.modref;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.types.FieldReference;

/**
 * Computes interprocedural field accesses for a given method.
 * 
 * @author Martin Seidel
 * @author Juergen Graf <juergen.graf@gmail.com>
 *
 */
public final class ModRefFieldAccess {
	
	private class TwoMaps {
		private Map<IClass, Set<IField>> mods;
		private Map<IClass, Set<IField>> refs;
		
		public TwoMaps(Map<IClass, Set<IField>> mods, Map<IClass, Set<IField>> refs) {
			this.mods = mods;
			this.refs = refs;
			if (mods == null) {
				this.mods = new HashMap<IClass, Set<IField>>();
			}
			if (refs == null) {
				this.refs = new HashMap<IClass, Set<IField>>();
			}
		}
		
		public Map<IClass, Set<IField>> getMods() {
			return mods;
		}

		public Map<IClass, Set<IField>> getRefs() {
			return refs;
		}
	}

	private final CallGraph cg;
	
	private Map<CGNode, Map<IClass, Set<IField>>> mods;
	private Map<CGNode, Map<IClass, Set<IField>>> refs;
	private Map<CGNode, Map<IClass, Set<IField>>> tmods;
	private Map<CGNode, Map<IClass, Set<IField>>> trefs;
	
	private List<CGNode> done;

	private ModRefFieldAccess(CallGraph cg) {
		this.cg = cg;
		this.refs = new HashMap<CGNode, Map<IClass, Set<IField>>>();
		this.mods = new HashMap<CGNode, Map<IClass, Set<IField>>>();
		this.trefs = new HashMap<CGNode, Map<IClass, Set<IField>>>();
		this.tmods = new HashMap<CGNode, Map<IClass, Set<IField>>>();
		this.done = new LinkedList<CGNode>();
	}
	
	public static ModRefFieldAccess compute(CallGraph cg) {
		ModRefFieldAccess fa = new ModRefFieldAccess(cg);
		fa.run();
		
		return fa;
	}
	
	public Map<IClass, Set<IField>> getMod(CGNode node) {
		return mods.get(node);
	}
	
	public Map<IClass, Set<IField>> getRef(CGNode node) {
		return refs.get(node);
	}
	
	public Map<IClass, Set<IField>> getTransitiveMod(CGNode node) {
		return tmods.get(node);
	}
	
	public Map<IClass, Set<IField>> getTransitiveRef(CGNode node) {
		return trefs.get(node);
	}
	
	private void run() {

		for (CGNode cgNode : cg) {
			if (!refs.containsKey(cgNode)) {
				refs.put(cgNode, new HashMap<IClass, Set<IField>>());
			}
			if (!mods.containsKey(cgNode)) {
				mods.put(cgNode, new HashMap<IClass, Set<IField>>());
			}
			
			final IR ir = cgNode.getIR();
			
			if (ir == null) {
				continue;
			}
			
			for (Iterator<SSAInstruction> it = ir.iterateNormalInstructions(); it.hasNext();) {
				SSAInstruction instr = it.next();
				if (instr instanceof SSAGetInstruction) {
					SSAGetInstruction get = (SSAGetInstruction) instr;
					FieldReference fref = get.getDeclaredField();
					IField field = cg.getClassHierarchy().resolveField(fref);
					if(field != null) {
						IClass cls = field.getDeclaringClass();
						if(cls != null) {
							if (!refs.get(cgNode).containsKey(cls)) {
								refs.get(cgNode).put(cls, new HashSet<IField>());
							}
							refs.get(cgNode).get(cls).add(field);
						}
					}
				} else if (instr instanceof SSAPutInstruction) {
					SSAPutInstruction put = (SSAPutInstruction) instr;
					FieldReference fput  = put.getDeclaredField();
					IField field = cg.getClassHierarchy().resolveField(fput);
					if(field != null) {
						IClass cls = field.getDeclaringClass();
						if(cls != null) {
							if (!mods.get(cgNode).containsKey(cls)) {
								mods.get(cgNode).put(cls, new HashSet<IField>());
							}
							mods.get(cgNode).get(cls).add(field);
						}
					}
				}
			}
		}
		
		recAdd(cg.getFakeRootNode());
		
	}
	
	private TwoMaps recAdd(CGNode node) {
		if (!trefs.containsKey(node)) {
			trefs.put(node, new HashMap<IClass, Set<IField>>());
		}
		if (!tmods.containsKey(node)) {
			tmods.put(node, new HashMap<IClass, Set<IField>>());
		}
		
		final IR ir = node.getIR();
		if (ir != null) {
			for (Iterator<SSAInstruction> it = ir.iterateNormalInstructions(); it.hasNext();) {
				SSAInstruction instr = it.next();
				if (instr instanceof SSAGetInstruction) {
					SSAGetInstruction get = (SSAGetInstruction) instr;
					FieldReference fref = get.getDeclaredField();
					IField field = cg.getClassHierarchy().resolveField(fref);
					if(field != null) {
						IClass cls = field.getDeclaringClass();
						if(cls != null) {
							if (!trefs.get(node).containsKey(cls)) {
								trefs.get(node).put(cls, new HashSet<IField>());
							}
							trefs.get(node).get(cls).add(field);
						}
					}
				} else if (instr instanceof SSAPutInstruction) {
					SSAPutInstruction put = (SSAPutInstruction) instr;
					FieldReference fput  = put.getDeclaredField();
					IField field = cg.getClassHierarchy().resolveField(fput);
					if(field != null) {
						IClass cls = field.getDeclaringClass();
						if(cls != null) {
							if (!tmods.get(node).containsKey(cls)) {
								tmods.get(node).put(cls, new HashSet<IField>());
							}
							tmods.get(node).get(cls).add(field);
						}
					}
				}
			}
		}
		
		for (Iterator<CGNode> it = cg.getSuccNodes(node); it.hasNext();) {
			CGNode n = it.next();
			if (!done.contains(n)) {
				done.add(n);
				TwoMaps t = recAdd(n);
				for (IClass c : t.getRefs().keySet()) {
					if (trefs.get(node).containsKey(c)) {
						trefs.get(node).get(c).addAll(t.getRefs().get(c));
					} else {
						trefs.get(node).put(c, t.getRefs().get(c));
					}
				}
				for (IClass c : t.getMods().keySet()) {
					if (tmods.get(node).containsKey(c)) {
						tmods.get(node).get(c).addAll(t.getMods().get(c));
					} else {
						tmods.get(node).put(c, t.getMods().get(c));
					}
				}
				
			}
		}
		
		return new TwoMaps(tmods.get(node), trefs.get(node));
	}
	
}
