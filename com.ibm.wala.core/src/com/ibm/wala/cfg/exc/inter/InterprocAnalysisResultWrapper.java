package com.ibm.wala.cfg.exc.inter;

import java.util.Map;

import com.ibm.wala.cfg.exc.ExceptionPruningAnalysis;
import com.ibm.wala.cfg.exc.InterprocAnalysisResult;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;

/**
 * A wrapper for the interprocedural analysis result.
 * 
 * @author Juergen Graf <graf@kit.edu>
 *
 */
class InterprocAnalysisResultWrapper implements InterprocAnalysisResult<SSAInstruction, IExplodedBasicBlock> {

  private final Map<CGNode, IntraprocAnalysisState> map;
  
  InterprocAnalysisResultWrapper(final Map<CGNode, IntraprocAnalysisState> map) {
    if (map == null) {
      throw new IllegalArgumentException();
    }

    this.map = map;
  }
  
  public ExceptionPruningAnalysis<SSAInstruction, IExplodedBasicBlock> getResult(final CGNode n) {
    if (!containsResult(n)) {
      return null;
    }
    
    return map.get(n);
  }

  public boolean containsResult(final CGNode n) {
    return map.containsKey(n) && map.get(n).canBeAnalyzed();
  }

}
