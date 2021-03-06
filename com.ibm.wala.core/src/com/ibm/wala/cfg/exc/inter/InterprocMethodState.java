package com.ibm.wala.cfg.exc.inter;

import java.util.Map;

import com.ibm.wala.cfg.exc.intra.MethodState;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;

/**
 * A MethodState for the interprocedural analysis.
 * 
 * This class has been developed as part of a student project "Studienarbeit" by Markus Herhoffer.
 * It has been adapted and integrated into the WALA project by Juergen Graf.
 * 
 * @author Markus Herhoffer <markus.herhoffer@student.kit.edu>
 * @author Juergen Graf <graf@kit.edu>
 * 
 */
class InterprocMethodState extends MethodState {

  private final Map<CGNode, IntraprocAnalysisState> map;
  private final CGNode method;
  private final CallGraph cg;

  InterprocMethodState(final CGNode method, final CallGraph cg, final Map<CGNode, IntraprocAnalysisState> map) {
    this.map = map;
    this.method = method;
    this.cg = cg;
  }

  /*
   * (non-Javadoc)
   * 
   * @see edu.kit.ipd.wala.intra.MethodState#throwsException(com.ibm.wala.ipa.callgraph.CGNode)
   */
  @Override
  public boolean throwsException(final SSAAbstractInvokeInstruction node) {
    for (final CGNode called : cg.getPossibleTargets(method, node.getCallSite())) {
      final IntraprocAnalysisState info = map.get(called);
      
      if (info == null || info.hasExceptions()) {
        return true;
      }
    }

    return false;
  }

}
