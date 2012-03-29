package com.ibm.wala.cfg.exc;

import com.ibm.wala.cfg.exc.inter.InterprocNullPointerAnalysis;
import com.ibm.wala.cfg.exc.intra.ExplodedCFGNullPointerAnalysis;
import com.ibm.wala.cfg.exc.intra.MethodState;
import com.ibm.wala.cfg.exc.intra.ParameterState;
import com.ibm.wala.cfg.exc.intra.SSACFGNullPointerAnalysis;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.MonitorUtil.IProgressMonitor;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.graph.GraphIntegrity.UnsoundGraphException;

/**
 * Tries to detect impossible (or always appearing) NullPointerExceptions and removes impossible
 * control flow from the CFG.
 * 
 * @author Juergen Graf <graf@kit.edu>
 *
 */
public final class NullPointerAnalysis { 

  public static final TypeReference[] DEFAULT_IGNORE_EXCEPTIONS = {
    TypeReference.JavaLangOutOfMemoryError, 
    TypeReference.JavaLangExceptionInInitializerError, 
    TypeReference.JavaLangNegativeArraySizeException
  };

  private NullPointerAnalysis() {
    throw new IllegalStateException("No instances of this class allowed.");
  }
  
  public static ExceptionPruningAnalysis<SSAInstruction, IExplodedBasicBlock>
  createIntraproceduralExplodedCFGAnalysis(IR ir) {
    return createIntraproceduralExplodedCFGAnalysis(DEFAULT_IGNORE_EXCEPTIONS, ir, null, null);
  }

  public static ExceptionPruningAnalysis<SSAInstruction, IExplodedBasicBlock>
  createIntraproceduralExplodedCFGAnalysis(TypeReference[] ignoredExceptions, IR ir) {
    return createIntraproceduralExplodedCFGAnalysis(ignoredExceptions, ir, null, null);
  }
   
  public static ExceptionPruningAnalysis<SSAInstruction, IExplodedBasicBlock>
  createIntraproceduralExplodedCFGAnalysis(TypeReference[] ignoredExceptions, IR ir, ParameterState paramState, MethodState mState) {
    return new ExplodedCFGNullPointerAnalysis(ignoredExceptions, ir, paramState, mState);
  }

  public static ExceptionPruningAnalysis<SSAInstruction, ISSABasicBlock>
  createIntraproceduralSSACFGAnalyis(IR ir) {
    return createIntraproceduralSSACFGAnalyis(DEFAULT_IGNORE_EXCEPTIONS, ir, null, null);
  }

  public static ExceptionPruningAnalysis<SSAInstruction, ISSABasicBlock>
  createIntraproceduralSSACFGAnalyis(TypeReference[] ignoredExceptions, IR ir) {
    return createIntraproceduralSSACFGAnalyis(ignoredExceptions, ir, null, null);
  }
  
  public static ExceptionPruningAnalysis<SSAInstruction, ISSABasicBlock>
  createIntraproceduralSSACFGAnalyis(TypeReference[] ignoredExceptions, IR ir, ParameterState paramState, MethodState mState) {
    return new SSACFGNullPointerAnalysis(ignoredExceptions, ir, paramState, mState);
  }
  
  public static InterprocAnalysisResult<SSAInstruction, IExplodedBasicBlock>
  computeInterprocAnalysis(final CallGraph cg, final IProgressMonitor progress)
      throws WalaException, UnsoundGraphException, CancelException {
    return computeInterprocAnalysis(DEFAULT_IGNORE_EXCEPTIONS, cg, progress);
  }
  
  public static InterprocAnalysisResult<SSAInstruction, IExplodedBasicBlock>
  computeInterprocAnalysis(final TypeReference[] ignoredExceptions, final CallGraph cg, final IProgressMonitor progress)
      throws WalaException, UnsoundGraphException, CancelException {
    final InterprocNullPointerAnalysis inpa = InterprocNullPointerAnalysis.compute(ignoredExceptions, cg, progress);

    return inpa.getResult();
  }
  
}
