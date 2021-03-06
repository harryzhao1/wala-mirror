/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.analysis.pointers;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.NumberedGraph;
import com.ibm.wala.util.graph.impl.NumberedNodeIterator;
import com.ibm.wala.util.graph.traverse.DFS;
import com.ibm.wala.util.intset.IntSet;

/**
 * A {@link Graph} view of a pointer analysis solution.
 * 
 * Nodes in the Graph are {@link PointerKey}s and {@link InstanceKey}s.
 * 
 * There is an edge from a PointerKey P to an InstanceKey I iff the PointerAnalysis indicates that P may point to I.
 * 
 * There is an edge from an InstanceKey I to a PointerKey P iff - P represents a field of an object instance modeled by I, or - P
 * represents the array contents of array instance I.
 */
@SuppressWarnings("deprecation")
public abstract class HeapGraph implements NumberedGraph<Object> {

  private final PointerAnalysis pa;

  protected HeapGraph(PointerAnalysis pa) {
    if (pa == null) {
      throw new IllegalArgumentException("null pa ");
    }
    this.pa = pa;
  }

  @Override
  public Iterator<Object> iterateNodes(IntSet s) {
    return new NumberedNodeIterator<Object>(s, this);
  }

  public Collection<Object> getReachableInstances(Set<Object> roots) {
    Filter f = new Filter() {
      @Override
      public boolean accepts(Object o) {
        return (o instanceof InstanceKey);
      }
    };
    return DFS.getReachableNodes(this, roots, f);
  }

  @Override
  public void removeNodeAndEdges(Object N) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * @return the heap model used in this pointer analysis.
   */
  public HeapModel getHeapModel() {
    return pa.getHeapModel();
  }

  public PointerAnalysis getPointerAnalysis() {
    return pa;
  }

}
