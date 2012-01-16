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

import static com.ibm.wala.cast.tree.CAstNode.ASSIGN;
import static com.ibm.wala.cast.tree.CAstNode.BLOCK_STMT;
import static com.ibm.wala.cast.tree.CAstNode.EACH_ELEMENT_GET;
import static com.ibm.wala.cast.tree.CAstNode.VAR;

import java.util.Collections;
import java.util.List;

import com.ibm.wala.cast.tree.CAstEntity;
import com.ibm.wala.cast.tree.CAstNode;
import com.ibm.wala.cast.tree.pattern.*;

/**
 * A policy telling a {@link ClosureExtractor} to extract the body of every for-in loop.
 * 
 * TODO: This policy only works with Rhino (<1.7.2), since it specifically matches on the encoding
 * of for-in loops generated by it.
 * 
 * @author mschaefer
 *
 */
public class ForInBodyExtractionPolicy extends ExtractionPolicy {
  public static final ForInBodyExtractionPolicy INSTANCE = new ForInBodyExtractionPolicy();
  
  public static final ExtractionPolicyFactory FACTORY = new ExtractionPolicyFactory() {
    @Override
    public ExtractionPolicy createPolicy(CAstEntity entity) {
      return INSTANCE;
    }
  };
  
  private ForInBodyExtractionPolicy() {}

  @Override
  public List<ExtractionRegion> extract(CAstNode node) {
    SomeConstant loopVar = new SomeConstant();
    /* matches the following pattern:
     * 
     *   BLOCK_STMT
     *     ASSIGN
     *       VAR <loopVar>
     *       EACH_ELEMENT_GET
     *         VAR <forin_tmp>
     *     <loopBody>
     *     
     * TODO: this is too brittle; what if future versions of Rhino encode for-in loops differently?
     */
    if(new NodeOfKind(BLOCK_STMT,
        new NodeOfKind(ASSIGN,
            new NodeOfKind(VAR, loopVar),
            new SubtreeOfKind(EACH_ELEMENT_GET)),
            new AnyNode()).matches(node)) {
      return Collections.singletonList(new ExtractionRegion(1, 2, Collections.singletonList((String)loopVar.getLastMatch())));
    }
    return null;
  }

}
