package com.ibm.wala.cast.js.ipa.callgraph;

import java.util.Map;

import com.ibm.wala.cast.ipa.callgraph.AstContextInsensitiveSSAContextInterpreter;
import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.js.loader.JavaScriptLoader;
import com.ibm.wala.cast.js.translator.JSAstTranslator;
import com.ibm.wala.cast.js.translator.JSConstantFoldingRewriter;
import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;
import com.ibm.wala.cast.loader.AstMethod.Retranslatable;
import com.ibm.wala.cast.tree.CAst;
import com.ibm.wala.cast.tree.CAstControlFlowMap;
import com.ibm.wala.cast.tree.CAstEntity;
import com.ibm.wala.cast.tree.CAstNode;
import com.ibm.wala.cast.tree.impl.CAstBasicRewriter;
import com.ibm.wala.cast.tree.impl.CAstImpl;
import com.ibm.wala.cfg.AbstractCFG;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextItem;
import com.ibm.wala.ipa.callgraph.ContextItem.Value;
import com.ibm.wala.ipa.callgraph.ContextKey;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.intset.IntSet;

public class ArgumentSpecialization {

  public static class ArgumentSpecializationContextIntepreter extends AstContextInsensitiveSSAContextInterpreter {

    public ArgumentSpecializationContextIntepreter(AnalysisOptions options, AnalysisCache cache) {
      super(options, cache);
    }

    @Override
    public IR getIR(CGNode node) {
      if (node.getMethod() instanceof Retranslatable) {
        return getAnalysisCache().getSSACache().findOrCreateIR(node.getMethod(), node.getContext(), options.getSSAOptions());
      } else {
        return super.getIR(node);
      }
    }
    
    public DefUse getDU(CGNode node) {
      if (node.getMethod() instanceof Retranslatable) {
        return getAnalysisCache().getSSACache().findOrCreateDU(node.getMethod(), node.getContext(), options.getSSAOptions());
      } else {
        return super.getDU(node);
      }
    }
  }

  public static class ArgumentCountContext implements Context {
    private final Context base;
    private final int argumentCount;

    public static ContextKey ARGUMENT_COUNT = new ContextKey() {
      public String toString() {
        return "argument count key";
      }
    };
    
    public int hashCode() {
      return base.hashCode() + (argumentCount * 4073);
    }
    
    public boolean equals(Object o) {
      return 
        o.getClass() == this.getClass() && 
        base.equals(((ArgumentCountContext)o).base) &&
        argumentCount == ((ArgumentCountContext)o).argumentCount;
    }
    
    public ArgumentCountContext(int argumentCount, Context base) {
      this.argumentCount = argumentCount;
      this.base = base;
    }
    
    public ContextItem get(ContextKey name) {
      return (name == ARGUMENT_COUNT)? ContextItem.Value.make(argumentCount): base.get(name);
    }

    public String toString() {
      return base.toString() + "(nargs:" + argumentCount + ")";
    }
  }

  public static class ArgumentCountContextSelector implements ContextSelector, ContextKey {
    private final ContextSelector base;
    
    public ArgumentCountContextSelector(ContextSelector base) {
      this.base = base;
    }

    public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey[] actualParameters) {
      Context baseContext = base.getCalleeTarget(caller, site, callee, actualParameters);
      if (caller.getMethod() instanceof Retranslatable) {
        int v = -1;
        for (SSAAbstractInvokeInstruction x : caller.getIR().getCalls(site)) {
          if (v == -1) {
            v = x.getNumberOfParameters();
          } else {
            if (v != x.getNumberOfParameters()) {
              return baseContext; 
            }
          }
        }
        
        return new ArgumentCountContext(v, baseContext);
      } else {
        return baseContext;
      }
    }

    public IntSet getRelevantParameters(CGNode caller, CallSiteReference site) {
      return base.getRelevantParameters(caller, site);
    }

  }

  public static class ArgumentCountIRFactory extends AstIRFactory.AstDefaultIRFactory {
    private final SSAOptions defaultOptions;
    
    public ArgumentCountIRFactory(SSAOptions defaultOptions) {
      this.defaultOptions = defaultOptions;
    }

    @Override
    public boolean contextIsIrrelevant(IMethod method) {
      return method instanceof Retranslatable? false: super.contextIsIrrelevant(method);
    }

    @Override
    public IR makeIR(final IMethod method, Context context, SSAOptions options) {
      if (method instanceof Retranslatable) {
        @SuppressWarnings("unchecked")
        final Value<Integer> v = (Value<Integer>) context.get(ArgumentCountContext.ARGUMENT_COUNT);
        final Retranslatable m = (Retranslatable)method;
        if (v != null) {
          final JavaScriptLoader myloader = (JavaScriptLoader) method.getDeclaringClass().getClassLoader();
          
          class FixedArgumentsRewriter extends CAstBasicRewriter {
            private final CAstEntity e = m.getEntity();
            
            public FixedArgumentsRewriter(CAst Ast) {
              super(Ast, false);
            }

            private boolean isNamedVar(CAstNode n, String name) {
              if (n.getKind() == CAstNode.VAR) {
                String nm = (String) n.getChild(0).getValue();
                return nm.equals(name);
              }
              
              return false;
            }
            
            private Object getIndexFromArgumentRef(CAstNode n) {
              if (n.getKind() == CAstNode.OBJECT_REF || n.getKind() == CAstNode.ARRAY_REF) {
                if (isNamedVar(n.getChild(0), "arguments")) {
                  return n.getChild(1).getValue();
                }
              }
              
              return null;
            }
                
            private Object getIndexFromBaseVar(CAstNode n) {
              if (n.getKind() == CAstNode.BLOCK_EXPR) {
                if (n.getChildCount() == 2) {
                  
                  CAstNode c1 = n.getChild(0);
                  if (c1.getKind() == CAstNode.ASSIGN) {
                    if (isNamedVar(c1.getChild(0), "base")) {
                      if (isNamedVar(c1.getChild(1), "arguments")) {
                        
                        CAstNode c2 = n.getChild(1);
                        if (c2.getKind() == CAstNode.OBJECT_REF || c2.getKind() == CAstNode.ARRAY_REF) {
                          if (isNamedVar(c2.getChild(0), "base")) {
                            return c2.getChild(1).getValue();
                          }
                        }   
                      }
                    }
                  }
                }
              }
              
              return null;
            }
       
            private Object getStaticArgumentIndex(CAstNode n) {
              Object x = getIndexFromArgumentRef(n);
              if (x != null) { 
                return x;
              } else {
                return getIndexFromBaseVar(n);
              }
            }
            

            private CAstNode handleArgumentRef(CAstNode n) {
              Object x = getStaticArgumentIndex(n);
              if (x != null) {
                if (x instanceof Number && ((Number)x).intValue() < v.getValue()-2) {
                  int arg = ((Number)x).intValue() + 3;
                  if (arg < e.getArgumentCount()) {
                    return Ast.makeNode(CAstNode.VAR, Ast.makeConstant(e.getArgumentNames()[arg]));
                  } else {
                    return Ast.makeNode(CAstNode.VAR, Ast.makeConstant("$arg" + arg));                    
                  }
                } else if (x instanceof String && "length".equals(x)) {
                  return Ast.makeConstant(v.getValue());
                }
              }
              
              return null;
            }
            
            @Override
            protected CAstNode copyNodes(CAstNode root, 
                CAstControlFlowMap cfg, 
                NonCopyingContext context,
                Map<Pair<CAstNode, NoKey>, CAstNode> nodeMap)
            {
              CAstNode result = null;
              if (root.getKind() == CAstNode.ARRAY_REF 
                    || root.getKind() == CAstNode.OBJECT_REF
                    || root.getKind() == CAstNode.BLOCK_EXPR) 
              {
                result = handleArgumentRef(root);
               
              } else if (root.getKind() == CAstNode.CONSTANT) {
                result = Ast.makeConstant(root.getValue());

              } else if (root.getKind() == CAstNode.OPERATOR) {
                result = root;
                
              } 
              
              if (result == null) {
                CAstNode children[] = new CAstNode[root.getChildCount()];
                for (int i = 0; i < children.length; i++) {
                  children[i] = copyNodes(root.getChild(i), cfg, context, nodeMap);
                }
                for(Object label: cfg.getTargetLabels(root)) {
                  if (label instanceof CAstNode) {
                    copyNodes((CAstNode)label, cfg, context, nodeMap);
                  }
                }
                CAstNode copy = Ast.makeNode(root.getKind(), children);
                result = copy;
              }

              nodeMap.put(Pair.make(root, context.key()), result);
              return result;

            }
            
          }

          final FixedArgumentsRewriter args = new FixedArgumentsRewriter(new CAstImpl());
          final JSConstantFoldingRewriter fold = new JSConstantFoldingRewriter(new CAstImpl());

          class ArgumentativeTranslator extends JSAstTranslator {
            
            public ArgumentativeTranslator(JavaScriptLoader loader) {
              super(loader);
            }

            private CAstEntity codeBodyEntity;
            private IMethod specializedCode;
            

            @Override
            protected int getArgumentCount(CAstEntity f) {
              return Math.max(super.getArgumentCount(f), v.getValue());
            }

            @Override
            protected String[] getArgumentNames(CAstEntity f) {
              if (super.getArgumentCount(f) >= v.getValue()) {
                return super.getArgumentNames(f);
              } else {
                String[] argNames = new String[ v.getValue() ];
                System.arraycopy(super.getArgumentNames(f), 0, argNames, 0, super.getArgumentCount(f));
                for(int i = super.getArgumentCount(f); i < argNames.length; i++) {
                  argNames[i] = "$arg" + (i+1);
                }
                
                return argNames;
              }
            }

            @Override
            protected String composeEntityName(WalkContext parent, CAstEntity f) {
              if (f == codeBodyEntity) {
                return super.composeEntityName(parent, f) + "_" + v.getValue().intValue();                
              } else {
                return super.composeEntityName(parent, f);
              }
            }

            @Override
            protected void defineFunction(CAstEntity N, WalkContext definingContext, AbstractCFG cfg, SymbolTable symtab,
                boolean hasCatchBlock, TypeReference[][] caughtTypes, boolean hasMonitorOp, AstLexicalInformation LI,
                DebuggingInformation debugInfo) {
              if (N == codeBodyEntity) {
                specializedCode = myloader.makeCodeBodyCode(cfg, symtab, hasCatchBlock, caughtTypes, hasMonitorOp, LI, debugInfo, method.getDeclaringClass());
              } else {
                super.defineFunction(N, definingContext, cfg, symtab, hasCatchBlock, caughtTypes, hasMonitorOp, LI, debugInfo);
              }
            }

            @Override
            public void translate(CAstEntity N, WalkContext context) {
              if (N == m.getEntity()) {
                codeBodyEntity = fold.rewrite(args.rewrite(N));
                super.translate(codeBodyEntity, context);
              } else {
                super.translate(N, context);                
              }
            }
            
          }
          ArgumentativeTranslator a = new ArgumentativeTranslator(myloader);
          m.retranslate(a);
          return super.makeIR(a.specializedCode, context, options);
        }
      }
      
      return super.makeIR(method, context, options);
    }

    @Override
    public ControlFlowGraph makeCFG(IMethod method, Context context) {
      return makeIR(method, context, defaultOptions).getControlFlowGraph();
    }
  }
}
