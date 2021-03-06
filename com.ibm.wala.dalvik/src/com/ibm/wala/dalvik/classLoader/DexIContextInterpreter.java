/*
 *
 * Copyright (c) 2009-2012,
 *
 *  Adam Fuchs          <afuchs@cs.umd.edu>
 *  Avik Chaudhuri      <avik@cs.umd.edu>
 *  Steve Suh           <suhsteve@gmail.com>
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *
 */

package com.ibm.wala.dalvik.classLoader;

import java.util.Iterator;

import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.types.FieldReference;

public class DexIContextInterpreter implements SSAContextInterpreter {

    public DexIContextInterpreter(SSAOptions options, AnalysisCache cache)
    {
        this.options = options;
        this.cache = cache;
    }

    private final SSAOptions options;
    private final AnalysisCache cache;


    public boolean understands(CGNode node) {
        if(node.getMethod() instanceof DexIMethod)
            return true;
        return false;
    }

    public boolean recordFactoryType(CGNode node, IClass klass) {
        // TODO what the heck does this mean?
        //com.ibm.wala.core/src/com/ibm/wala/analysis/reflection/JavaLangClassContextInterpreter.java has this set to false
        return false;
//      throw new RuntimeException("not yet implemented");
    }

    public Iterator<NewSiteReference> iterateNewSites(CGNode node) {
        return getIR(node).iterateNewSites();
    }

    public Iterator<FieldReference> iterateFieldsWritten(CGNode node) {
        // TODO implement this!
        throw new RuntimeException("not yet implemented");
    }

    public Iterator<FieldReference> iterateFieldsRead(CGNode node) {
        // TODO implement this!
        throw new RuntimeException("not yet implemented");
    }

    public Iterator<CallSiteReference> iterateCallSites(CGNode node) {
        return getIR(node).iterateCallSites();
    }

    public int getNumberOfStatements(CGNode node) {
        // TODO verify this is correct
        assert understands(node);
        return getIR(node).getInstructions().length;
    }

    public IR getIR(CGNode node) {
//      new Exception("getting IR for method "+node.getMethod().getReference().toString()).printStackTrace();
        return cache.getSSACache().findOrCreateIR(node.getMethod(), node.getContext(), options);
    }

    public DefUse getDU(CGNode node) {
        return cache.getSSACache().findOrCreateDU(getIR(node), node.getContext());
//      return new DefUse(getIR(node));
    }

    public ControlFlowGraph<SSAInstruction, ISSABasicBlock> getCFG(CGNode n) {
        IR ir = getIR(n);
        return ir.getControlFlowGraph();
    }    
};
