package com.ibm.wala.cfg.exc.intra;

import com.ibm.wala.cfg.IBasicBlock;
import com.ibm.wala.ipa.cfg.EdgeFilter;
import com.ibm.wala.util.graph.Graph;

/**
 * An EdgeFilter that ignores all edges contained in a given graph. This ca be used
 * to subtract a subgraph from its main graph.
 * 
 * @author Juergen Graf <graf@kit.edu>
 *
 */
public class NegativeGraphFilter<T extends IBasicBlock<?>> implements EdgeFilter<T> {

  private final Graph<T> deleted;
  
  public NegativeGraphFilter(Graph<T> deleted) {
    this.deleted = deleted;
  }
  
  /* (non-Javadoc)
   * @see com.ibm.wala.ipa.cfg.EdgeFilter#hasExceptionalEdge(com.ibm.wala.cfg.IBasicBlock, com.ibm.wala.cfg.IBasicBlock)
   */
  public boolean hasExceptionalEdge(T src, T dst) {
    return !deleted.hasEdge(src, dst);
  }

  /* (non-Javadoc)
   * @see com.ibm.wala.ipa.cfg.EdgeFilter#hasNormalEdge(com.ibm.wala.cfg.IBasicBlock, com.ibm.wala.cfg.IBasicBlock)
   */
  public boolean hasNormalEdge(T src, T dst) {
    return !deleted.hasEdge(src, dst);
  }
  
}