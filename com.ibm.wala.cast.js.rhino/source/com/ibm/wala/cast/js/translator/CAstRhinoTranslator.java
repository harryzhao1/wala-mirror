/******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *****************************************************************************/
package com.ibm.wala.cast.js.translator;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.mozilla.javascript.RhinoToAstTranslator;

import com.ibm.wala.cast.ir.translator.TranslatorToCAst;
import com.ibm.wala.cast.js.translator.PropertyReadExpander.ExpanderKey;
import com.ibm.wala.cast.tree.CAst;
import com.ibm.wala.cast.tree.CAstEntity;
import com.ibm.wala.cast.tree.impl.CAstImpl;
import com.ibm.wala.cast.tree.impl.CAstRewriter;
import com.ibm.wala.cast.tree.impl.CAstRewriter.CopyKey;
import com.ibm.wala.cast.tree.impl.CAstRewriter.RewriteContext;
import com.ibm.wala.cast.tree.impl.CAstRewriterFactory;
import com.ibm.wala.classLoader.SourceFileModule;
import com.ibm.wala.classLoader.SourceModule;

public class CAstRhinoTranslator implements TranslatorToCAst {
  private final List<CAstRewriterFactory> rewriters = new LinkedList<CAstRewriterFactory>();
  private final SourceModule M;

  public CAstRhinoTranslator(SourceModule M) {
    this.M = M;
    this.addRewriter(new CAstRewriterFactory<PropertyReadExpander.RewriteContext, ExpanderKey>() {
      public CAstRewriter<PropertyReadExpander.RewriteContext, ExpanderKey> createCAstRewriter(CAst ast) {
        return new PropertyReadExpander(ast);
      }
    }, true);
  }

  public <C extends RewriteContext<K>, K extends CopyKey<K>> void addRewriter(CAstRewriterFactory<C, K> factory, boolean prepend) {
    if(prepend)
      rewriters.add(0, factory);
    else
      rewriters.add(factory);
  }  

  public CAstEntity translateToCAst() throws IOException {
    String N;
    if (M instanceof SourceFileModule) {
      N = ((SourceFileModule) M).getClassName();
    } else {
      N = M.getName();
    }

    CAstImpl Ast = new CAstImpl();
    CAstEntity entity = new RhinoToAstTranslator(Ast, M, N).translate();
    for(CAstRewriterFactory rwf : rewriters)
      entity = rwf.createCAstRewriter(Ast).rewrite(entity);
    return entity;
  }
}
