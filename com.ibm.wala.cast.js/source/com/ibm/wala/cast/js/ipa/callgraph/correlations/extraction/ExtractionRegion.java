/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.wala.cast.js.ipa.callgraph.correlations.extraction;

import java.util.List;

/**
 * A region for the {@link ClosureExtractor} to extract.
 * 
 * @author mschaefer
 *
 */
public class ExtractionRegion {
  private int start, end;
  private final List<String> parameters;

  public ExtractionRegion(int start, int end, List<String> parameters) {
    super();
    this.start = start;
    this.end = end;
    this.parameters = parameters;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public List<String> getParameters() {
    return parameters;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public void setEnd(int end) {
    this.end = end;
  }
}
